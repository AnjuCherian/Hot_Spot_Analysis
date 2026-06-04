package cse512

import org.apache.log4j.{ Level, Logger }
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions._

object HotcellAnalysis {
  Logger.getLogger("org.spark_project").setLevel(Level.WARN)
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("com").setLevel(Level.WARN)

  def runHotcellAnalysis(spark: SparkSession, pointPath: String): DataFrame =
    {
      // Load the original data from a data source
      var pickupInfo = spark.read.format("com.databricks.spark.csv").option("delimiter", ";").option("header", "false").load(pointPath);
      pickupInfo.createOrReplaceTempView("nyctaxitrips")
      pickupInfo.show()

      // Assign cell coordinates based on pickup points
      spark.udf.register("CalculateX", (pickupPoint: String) => ((
        HotcellUtils.CalculateCoordinate(pickupPoint, 0))))
      spark.udf.register("CalculateY", (pickupPoint: String) => ((
        HotcellUtils.CalculateCoordinate(pickupPoint, 1))))
      spark.udf.register("CalculateZ", (pickupTime: String) => ((
        HotcellUtils.CalculateCoordinate(pickupTime, 2))))
      pickupInfo = spark.sql("select CalculateX(nyctaxitrips._c5),CalculateY(nyctaxitrips._c5), CalculateZ(nyctaxitrips._c1) from nyctaxitrips")
      var newCoordinateName = Seq("x", "y", "z")
      pickupInfo = pickupInfo.toDF(newCoordinateName: _*)
      pickupInfo.show()

      // Define the min and max of x, y, z
      val minX = -74.50 / HotcellUtils.coordinateStep
      val maxX = -73.70 / HotcellUtils.coordinateStep
      val minY = 40.50 / HotcellUtils.coordinateStep
      val maxY = 40.90 / HotcellUtils.coordinateStep
      val minZ = 1
      val maxZ = 31
      var numCells = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)

      // All calculations will be performed as double
      numCells = numCells.toDouble

      pickupInfo.createOrReplaceTempView("pickupInfo")

      // Counting number of pickups at each pickup location bounded by ((minX, maxX), (minY, maxY), (minZ, maxZ))
      pickupInfo = spark.sql("select x, y, z, count(*) as Xj from pickupInfo where (x between " + minX + " and " + maxX + ") and (y between " + minY + " and " + maxY + ") and (z between " + minZ + " and " + maxZ + ") group by x, y, z")
      pickupInfo.createOrReplaceTempView("pickupInfo")

      // Calculate sum of number of pickups at every cell and sum of square of number of pickups at every cell
      var temp = spark.sql("select sum(Xj), sum(Xj * Xj) from pickupInfo").first()

      var sumXj = temp.getLong(0).toDouble
      var sumXjSquare = temp.getLong(1).toDouble

      // Computing the Xmean and Standard Deviation values
      val X_mean = (sumXj / numCells)
      val SD = math.sqrt((sumXjSquare / numCells) - (X_mean * X_mean))

      val Wij = 1 // Assuming spatial weight between cell i and neighbour j is 1

      // Self-join pickupInfo table with neighbor cells ie; cells that differ by maximum 1.
      spark.udf.register("neighborCount", (x: Int, y: Int, z: Int, minX: Int, maxX: Int, minY: Int, maxY: Int, minZ: Int, maxZ: Int) => HotcellUtils.numberOfNeighbors(x, y, z, minX, maxX, minY, maxY, minZ, maxZ))
      var WijXj_neighborCount = spark.sql("select neighborCount(p1.x, p1.y, p1.z, " +
        +minX + "," + maxX + "," + minY + "," + maxY + "," + minZ + "," + maxZ + ") as sumWij, p1.x " +
        "as x, p1.y as y, p1.z as z, sum(" + Wij + " * p2.Xj) as sumWijXj" +
        " from pickupInfo as p1 inner join pickupInfo as p2  " +
        "on (p2.x = p1.x-1 or p2.x = p1.x or p2.x = p1.x+1) " +
        "and (p2.y = p1.y-1 or p2.y = p1.y or p2.y = p1.y+1)" +
        " and (p2.z = p1.z-1 or p2.z = p1.z or p2.z = p1.z+1)" +
        " group by p1.x, p1.y, p1.z")
      WijXj_neighborCount.createOrReplaceTempView("WijXj_neighborCount")

      // Compute getis-Ord statistic
      spark.udf.register("getisOrd", (sumWijXj: Int, sumWij: Int, numCells: Int, X_mean: Double, SD: Double) => ((HotcellUtils.calculateGetisOrdGi(sumWijXj, sumWij, numCells, X_mean, SD))))

      val hotCells = spark.sql("select getisOrd(sumWijXj, sumWij, " + numCells + ", " + X_mean + ", " + SD + ") as getisOrdStat, x, y, z from WijXj_neighborCount order by getisOrdStat desc");
      hotCells.createOrReplaceTempView("hotCells")

      pickupInfo = spark.sql("select x, y, z from hotCells")
      return pickupInfo
    }
}
