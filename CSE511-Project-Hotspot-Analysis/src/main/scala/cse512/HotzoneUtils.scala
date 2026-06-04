package cse512

object HotzoneUtils {

  def ST_Contains(queryRectangle: String, pointString: String): Boolean = {

    val rectangle_coordinates = queryRectangle.split(",")
    val point_coordinates = pointString.split(",")

    val rectangle_tx1: Double = rectangle_coordinates(0).trim.toDouble
    val rectangle_ty1: Double = rectangle_coordinates(1).trim.toDouble
    val rectangle_tx2: Double = rectangle_coordinates(2).trim.toDouble
    val rectangle_ty2: Double = rectangle_coordinates(3).trim.toDouble

    var rect_x1: Double = 0
    var rect_x2: Double = 0
    var rect_y1: Double = 0
    var rect_y2: Double = 0

    if (rectangle_tx1 < rectangle_tx2) {
      rect_x1 = rectangle_tx1
      rect_x2 = rectangle_tx2
    } else {
      rect_x1 = rectangle_tx2
      rect_x2 = rectangle_tx1
    }

    if (rectangle_ty1 < rectangle_ty2) {
      rect_y1 = rectangle_ty1
      rect_y2 = rectangle_ty2
    } else {
      rect_y1 = rectangle_ty2
      rect_y2 = rectangle_ty1
    }

    val point_x: Double = point_coordinates(0).trim.toDouble
    val point_y: Double = point_coordinates(1).trim.toDouble

    if (point_x >= rect_x1 && point_x <= rect_x2 && point_y >= rect_y1 && point_y <= rect_y2) {
      return true
    }
    return false
  }
  
}

