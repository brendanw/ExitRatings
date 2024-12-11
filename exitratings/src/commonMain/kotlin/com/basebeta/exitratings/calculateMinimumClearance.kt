package com.basebeta.exitratings

import kotlin.math.abs

/**
 * @param exitProfile
 * @param referenceFlight list of flysight rows making up the reference flight. first row should be first moment the
 * user pushes. last row should be touch-down/landing.
 */
internal fun calculateMinimumClearance(
   exitProfile: List<Point>,
   referenceFlight: List<FlysightRow>
): Double? {
   var minClearance = Double.MAX_VALUE
   exitProfile.forEach { terrainRow ->
      if (terrainRow.x > 3) {
         val flightX = deriveXAtGivenY(terrainRow.y.toDouble(), referenceFlight) ?: return null
         val clearance = flightX - terrainRow.x
         minClearance = minOf(minClearance, clearance)
      }
   }

   return minClearance
}

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
         val hasStartedFlying = !hasNotStartedFlying
         if (startRow != null && (i == (startIndex + 10)) && hasNotStartedFlying) {
            lastPoint = DPoint(0.0, 0.0)
            startRow = null
            unrolledDistance = 0.0
            unrolledHeight = 0.0
         }

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