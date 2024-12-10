package com.basebeta

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

const val PI = 3.14159265358979323846

// from degree to  rad
fun Double.degToRad(): Double {
   return this * (PI / 180)
}

fun getDistanceFromLatLonInKM(
   startLat: Double,
   startLon: Double,
   currentLat: Double,
   currentLon: Double
): Double {
   val r = 6371
   val dLat = (currentLat - startLat).degToRad()
   val dLon = (currentLon - startLon).degToRad()
   val a =
      (sin(dLat / 2) * sin(dLat / 2)) + (cos(startLat.degToRad()) * cos(currentLat.degToRad()) * sin(dLon / 2) * sin(
         dLon / 2
      ))
   val c = 2 * atan2(sqrt(a), sqrt(1 - a))
   return r * c // distance in km
}

fun getDistX(start: FlysightRow, current: FlysightRow): Double {
   val distKM = getDistanceFromLatLonInKM(start.lat, start.lon, current.lat, current.lon)
   return distKM * 1000 // distance in m
}

fun getDistY(start: FlysightRow, current: FlysightRow): Double {
   return (start.hMSL - current.hMSL) // height in m
}

fun findHypotenuse(a: Double, b: Double): Double {
   return sqrt(((a * a) + (b * b)))
}

fun getYFromPointSlope(inputX: Int, p1: Array<Int>, p2: Array<Int>): Int {
   val m = (p2[1] - p1[1]) / (p2[0] - p1[0])
   val b = p2[1] - (m * p2[0])
   val y = (m * inputX) + b
   return y
}

fun getXFromPointSlope(inputY: Double, p1: DPoint, p2: DPoint): Double {
   // Ensure the points are not the same to avoid division by zero
   if (p1.y == p2.y) throw IllegalArgumentException("The y-coordinates of p1 and p2 must not be the same.")

   // Calculate the x-coordinate using the rearranged point-slope form equation
   return p1.x + ((inputY - p1.y) * (p2.x - p1.x)) / (p2.y - p1.y)
}

fun Double.round(decimals: Int): Double {
   val numberAsString = this.toString()
   val decimalIndex = numberAsString.indexOf(".")

   // Check if the number is a decimal
   if (decimalIndex != -1) {
      // Determine the index up to which the string should be kept based on the decimals parameter
      val endIndex = decimalIndex + 1 + decimals

      // Check if the actual number of decimals is greater than the requested number of decimals
      if (numberAsString.length > endIndex) {
         // Create a substring of the required length and convert it back to Double
         return numberAsString.substring(0, endIndex).toDouble()
      }
   }

   // If the number is not a decimal or the number of actual decimals is less than or equal to the requested decimals
   return this
}