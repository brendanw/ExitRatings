package com.basebeta.exitratings

import kotlin.math.abs

/**
 * @param exitProfile
 * @param referenceFlight list of flysight rows making up the reference flight. first row should be first moment the
 * user pushes. last row should be touch-down/landing.
 * @param range the range of x values to calculate minimum clearance over
 */
internal fun calculateMinimumClearance(
   exitProfile: List<Point>,
   referenceFlight: List<FlysightRow>,
   range: IntRange,
   mode: ClearanceMode = ClearanceMode.xDistance
): Double? {
   var minClearance = Double.MAX_VALUE
   exitProfile.forEach { terrainRow ->
      // Reference flights might be divey (eg Stiggbottonhornet for flights where HMSL<1500m)
      // Given that and the fact that glideHeuristic captures everything after first 250 horizontal meters, we can
      // reduce the horizontal analysis window. Though, it would be better to find a glidy reference flight.
      if (terrainRow.x > range.start && terrainRow.x < range.last) {
         if (mode == ClearanceMode.xDistance) {
            val flightX = deriveXAtGivenY(terrainRow.y.toDouble(), referenceFlight)

            if (flightX == null) {
               return null
            }

            val clearance = flightX - terrainRow.x
            minClearance = minOf(minClearance, clearance)
         } else {
            val flightY = deriveYAtGivenX(terrainRow.x.toDouble(), referenceFlight)

            if (flightY == null) {
               return null
            }

            val clearance = flightY - terrainRow.y
            minClearance = minOf(minClearance, clearance)
         }
      }

   }

   return minClearance
}

internal enum class ClearanceMode { xDistance, yDistance }

/**
 * Given y=-50, using interpolation what is the x-value for the user's flight?
 */
private fun deriveXAtGivenY(
   input: Double,
   rows: List<FlysightRow>
): Double? {
   try {
      var startRow: FlysightRow? = null
      var previousRow: FlysightRow? = null
      var unrolledDistance = 0.0
      var unrolledHeight = 0.0

      var rolledDistance = 0.0
      var rolledHeight = 0.0

      var lastPoint = DPoint(0.0, 0.0)
      var startHeight = 0.0
      var startIndex = 0

      rows.forEachIndexed { i, row ->
         // We've potentially identified row where user jumps off cliff
         if (startRow == null
            && i > 1
            && (findHypotenuse(abs(row.velN), abs(row.velE)) >= 1)
            && abs(row.velD) >= 1
            && row.hAcc < 15
         ) {
            startRow = row
            startHeight = row.hMSL
            startIndex = i
         }

         // reset values if 10 rows later and we are still not seeing changes in height indicative of flight
         val hasNotStartedFlying = ((startHeight - row.hMSL) < 5) || abs(row.velD) < 10
         if (startRow != null && (i == (startIndex + 10)) && hasNotStartedFlying) {
            lastPoint = DPoint(0.0, 0.0)
            startRow = null
            unrolledDistance = 0.0
            unrolledHeight = 0.0
         }

         val hasStartedFlying = startRow != null

         // Get distance
         if (previousRow != null && startRow != null) {
            unrolledDistance += getDistX(previousRow!!, row)
            unrolledHeight += getDistY(previousRow!!, row)
            rolledDistance = getDistX(startRow!!, row)
            rolledHeight = getDistY(startRow!!, row)
         }

         // populate ledge map
         if (hasStartedFlying && previousRow != null) {
            val newPoint = DPoint(rolledDistance, -rolledHeight)

            if (newPoint.y <= input) {
               val x = getXFromPointSlope(input, lastPoint, newPoint)
               return x.round(1)
            }

            lastPoint = newPoint
         }

         previousRow = row
      }

   } catch (e: Exception) {
      e.printStackTrace()
      return null
   }

   return null
}

/**
 * Given x=50, using interpolation what is the y-value for the user's flight?
 */
private fun deriveYAtGivenX(
   input: Double,
   rows: List<FlysightRow>
): Double? {
   try {
      var startRow: FlysightRow? = null
      var previousRow: FlysightRow? = null
      var unrolledDistance = 0.0
      var unrolledHeight = 0.0

      var rolledDistance = 0.0
      var rolledHeight = 0.0

      var lastPoint = DPoint(0.0, 0.0)
      var startHeight = 0.0
      var startIndex = 0

      rows.forEachIndexed { i, row ->
         // We've potentially identified row where user jumps off cliff
         if (startRow == null
            && i > 1
            && (findHypotenuse(abs(row.velN), abs(row.velE)) >= 1)
            && abs(row.velD) >= 1
            && row.hAcc < 15
         ) {
            startRow = row
            startHeight = row.hMSL
            startIndex = i
         }

         // reset values if 10 rows later and we are still not seeing changes in height indicative of flight
         val hasNotStartedFlying = ((startHeight - row.hMSL) < 5) || abs(row.velD) < 10
         if (startRow != null && (i == (startIndex + 10)) && hasNotStartedFlying) {
            lastPoint = DPoint(0.0, 0.0)
            startRow = null
            unrolledDistance = 0.0
            unrolledHeight = 0.0
         }

         val hasStartedFlying = startRow != null

         // Get distance
         if (previousRow != null && startRow != null) {
            unrolledDistance += getDistX(previousRow!!, row)
            unrolledHeight += getDistY(previousRow!!, row)
            rolledDistance = getDistX(startRow!!, row)
            rolledHeight = getDistY(startRow!!, row)
         }

         // populate ledge map
         if (hasStartedFlying && previousRow != null) {
            val newPoint = DPoint(rolledDistance, -rolledHeight)

            if (newPoint.x >= input) {
               val y = getYFromPointSlope(input, lastPoint, newPoint)
               return y.round(1)
            }

            lastPoint = newPoint
         }

         previousRow = row
      }

   } catch (e: Exception) {
      e.printStackTrace()
      return null
   }

   return null
}

/**
 * Helper function to calculate Y coordinate given X coordinate and two points
 */
internal fun getYFromPointSlope(inputX: Double, p1: DPoint, p2: DPoint): Double {
   // Ensure the points are not the same to avoid division by zero
   if (p1.x == p2.x) throw IllegalArgumentException("The x-coordinates of p1 and p2 must not be the same.")

   // Calculate the y-coordinate using the rearranged point-slope form equation
   return p1.y + ((inputX - p1.x) * (p2.y - p1.y)) / (p2.x - p1.x)
}