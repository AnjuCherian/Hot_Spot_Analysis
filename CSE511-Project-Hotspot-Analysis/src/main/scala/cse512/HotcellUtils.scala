package cse512

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar

object HotcellUtils {
  val coordinateStep = 0.01

  def CalculateCoordinate(inputString: String, coordinateOffset: Int): Int =
    {
      // Configuration variable:
      // Coordinate step is the size of each cell on x and y
      var result = 0
      coordinateOffset match {
        case 0 => result = Math.floor((inputString.split(",")(0).replace("(", "").toDouble / coordinateStep)).toInt
        case 1 => result = Math.floor(inputString.split(",")(1).replace(")", "").toDouble / coordinateStep).toInt
        // We only consider the data from 2009 to 2012 inclusively, 4 years in total. Week 0 Day 0 is 2009-01-01
        case 2 => {
          val timestamp = HotcellUtils.timestampParser(inputString)
          result = HotcellUtils.dayOfMonth(timestamp) // Assume every month has 31 days
        }
      }
      return result
    }

  def timestampParser(timestampString: String): Timestamp =
    {
      val dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
      val parsedDate = dateFormat.parse(timestampString)
      val timeStamp = new Timestamp(parsedDate.getTime)
      return timeStamp
    }

  def dayOfYear(timestamp: Timestamp): Int =
    {
      val calendar = Calendar.getInstance
      calendar.setTimeInMillis(timestamp.getTime)
      return calendar.get(Calendar.DAY_OF_YEAR)
    }

  def dayOfMonth(timestamp: Timestamp): Int =
    {
      val calendar = Calendar.getInstance
      calendar.setTimeInMillis(timestamp.getTime)
      return calendar.get(Calendar.DAY_OF_MONTH)
    }

  def numberOfNeighbors(x: Int, y: Int, z: Int, minX: Int, maxX: Int, minY: Int, maxY: Int, minZ: Int, maxZ: Int): Int =
    {
      // Possible positions for (x,y,z):
      // Center - 27 neighbors
      // Face - 18 neighbors
      // Edge - 12 neighbors
      // Corner - 8 neighbors

      var noOfIntersections = 0 // number of intersections (x, y, z) have with ((minX, maxX), (minY, maxY), (minZ, maxZ))

      if (x == minX || x == maxX)
        noOfIntersections = noOfIntersections + 1

      if (y == minY || y == maxY)
        noOfIntersections = noOfIntersections + 1

      if (z == minZ || z == maxZ)
        noOfIntersections = noOfIntersections + 1

      noOfIntersections match {
        case 0 => return 27 // Center
        case 1 => return 18 // Face
        case 2 => return 12 // Edge
        case 3 => return 8 // Corner
      }

    }

    def calculateGetisOrdGi(sumWijXj: Int, sumWij: Int, numCells: Int, mean: Double, SD: Double): Double ={

      var a = sumWijXj.toDouble - (mean * sumWij.toDouble)
      var b = SD * math.sqrt(((numCells.toDouble * sumWij.toDouble) - math.pow(sumWij.toDouble, 2)) / (numCells.toDouble - 1.0))
      var result = (a/b).toDouble

      return result

    }


}
