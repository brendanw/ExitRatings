package com.basebeta.wingsuit

import com.basebeta.exitratings.ClearanceMode
import com.basebeta.exitratings.FlysightRow
import com.basebeta.exitratings.JumpRating
import com.basebeta.exitratings.Point
import com.basebeta.exitratings.calculateMinimumClearance
import com.basebeta.exitratings.toJumpRating
import com.basebeta.exitratings.wingsuit.referenceflights.ReferenceFlights

// North-facing Le Pleurer (3700m) Aura 3 August 6, 2018
const val nonIdeal3700 = "3700m-northFacing-aura3.csv"

// North-facing Aspen Grove (3200m) Aura 5 September 12, 2023
const val nonIdeal3200 = "3200m-northFacing-aura5.csv"

// North-facing Rote Wand (2500m) Aura 5 Sept 24, 2024
const val nonIdeal2500 = "2500m-northFacing-aura5.csv"

// North-facing Godzilla (1900m) Aura5 Sept 20, 2024
const val nonIdeal1900 = "1900m-northFacing-aura5.csv"

/**
 * @param exitProfile list of x-y measurements taken by laser from exit point [(5m,-20m), (10m,-50m), (30m,-100m), (40m,-120m)]
 * @param flyableAltitude difference in hmsl recorded from point where flyer pushes and point where flyer lands
 * @param minimumRequiredGlide glide ratio that pilot must be capable of sustaining through the duration of the flight
 * to deploy canopy at least 135m above the ground
 * @param hmsl elevation above sea level (in meters) for point where flyer pushes from exit
 */
suspend fun assembleWingsuitRating(
   exitProfile: List<Point>,
   flyableAltitude: Int,
   minimumRequiredGlide: Double,
   hmsl: Int
): JumpRating? {
   val filename = when {
      hmsl <= 1900 -> nonIdeal1900
      hmsl <= 2450 -> nonIdeal2500
      hmsl <= 3000 -> nonIdeal3200
      hmsl <= 3400 -> nonIdeal3700
      else -> nonIdeal3700
   }

   val nonIdealFlight = getNonIdealFlight(filename)

   return generateWingsuitRating(
      exitProfile = exitProfile,
      flyableAltitude = flyableAltitude,
      minimumRequiredGlide = minimumRequiredGlide,
      nonIdealFlight = nonIdealFlight
   )
}

/**
 * There are a lot of nuances around bundling/packaging files in the resources directory
 *
 * related youtrack: https://youtrack.jetbrains.com/issue/KT-49981
 */
internal suspend fun getNonIdealFlight(filename: String): List<FlysightRow> {
   try {
      val resultStr = ReferenceFlights.flightMap[filename]!!

      val rows = resultStr.split("\n")
      val list = mutableListOf<FlysightRow>()
      rows.forEach { line ->
         val elements = line.split(",")
         val flySightRow = FlysightRow()
         if (elements.size > 6 && elements[LAT].toDoubleOrNull() != null &&
            elements[LAT] != "lat" && elements[LAT] != "(deg)"
         ) {
            flySightRow.time = elements[TIME]
            flySightRow.lat = elements[LAT].toDouble()
            flySightRow.lon = elements[LON].toDouble()
            flySightRow.hMSL = elements[HMSL].toDouble()
            flySightRow.velN = elements[VELN].toDouble()
            flySightRow.velE = elements[VELE].toDouble()
            flySightRow.velD = elements[VELD].toDouble()
            flySightRow.hAcc = elements[HACC].toDouble()
            flySightRow.vAcc = elements[VACC].toDouble()
            flySightRow.sAcc = elements[SACC].toDouble()
            flySightRow.heading = elements[HEADING].toDouble()
            flySightRow.cAcc = elements[CACC].toDouble()
            flySightRow.gpsFix = elements[GPSFIX].toDouble()
            flySightRow.numSV = elements[NUMSV].toInt()
            list.add(flySightRow)
         }
      }

      return list
   } catch (e: Exception) {
      e.printStackTrace()
      return emptyList()
   }
}

/**
 * @param exitProfile list of x,y pairs defining the elevation profile of the start (units are meters)
 * @param flyableAltitude the difference in hmsl from push thru landing (units are meters)
 * @param minimumRequiredGlide glide ratio that pilot must be capable of sustaining through the duration of the flight
 * to deploy canopy at least 135m above the ground
 * @param nonIdealFlight list of flysight rows where first row is the user pushing from exit. flight should be
 * from a north-facing exit w/ large suit from exit point with an HMSL value no greater than 500m different from exit
 */
internal fun generateWingsuitRating(
   exitProfile: List<Point>,
   flyableAltitude: Int,
   minimumRequiredGlide: Double,
   nonIdealFlight: List<FlysightRow>
): JumpRating? {
   // Exit must have a laser profile in order for a rating to be auto-generated
   if (exitProfile.isEmpty()) {
      return null
   }

   // Jump must have a detailed laser profile in order for a rating to be auto-generated
   if (exitProfile.size < 5) {
      return null
   }

   // Greens should only be set manually since there are many factors beyond what is practical for defining a green
   var curRating = JumpRating.SingleBlue.rating

   // Establish min rating by virtue of flyable altitude
   if (flyableAltitude < 900.0) {
      curRating = JumpRating.SingleBlue.rating
   }

   if (flyableAltitude < 800.0) {
      curRating = JumpRating.DoubleBlue.rating
   }

   if (flyableAltitude < 600.0) {
      curRating = JumpRating.SingleBlack.rating
   }

   if (flyableAltitude < 500.0) {
      curRating = JumpRating.DoubleBlack.rating
   }

   if (flyableAltitude < 450.0) {
      curRating = JumpRating.TripleBlack.rating
   }

   // Establish min rating by glideHeuristic of reference track
   // Note we do not look at tracks below 600m by glide heuristic due to methodology
   if (flyableAltitude > 600.0) {
      if (minimumRequiredGlide > 2.0) {
         curRating = maxOf(curRating, JumpRating.SingleBlue.rating)
      }

      if (minimumRequiredGlide > 2.2) {
         curRating = maxOf(curRating, JumpRating.DoubleBlue.rating)
      }

      if (minimumRequiredGlide > 2.5) {
         curRating = maxOf(curRating, JumpRating.SingleBlack.rating)
      }

      if (minimumRequiredGlide > 2.7) {
         curRating = maxOf(curRating, JumpRating.DoubleBlack.rating)
      }

      if (minimumRequiredGlide > 2.9) {
         curRating = maxOf(curRating, JumpRating.TripleBlack.rating)
      }

      if (minimumRequiredGlide >= 3.0) {
         curRating = maxOf(curRating, JumpRating.Red.rating)
      }
   }

   val minimumClearanceWindow1: Double? = calculateMinimumClearance(
      exitProfile = exitProfile,
      referenceFlight = nonIdealFlight,
      range = 3..30
   )

   // If the algo is unable to calculate minimum clearance, then do not auto generate a rating
   if (minimumClearanceWindow1 == null) {
      return null
   }

   if (minimumClearanceWindow1 < 3.0) {
      curRating = maxOf(curRating, JumpRating.Red.rating)
   }

   if (minimumClearanceWindow1 < 4.0) {
      curRating = maxOf(curRating, JumpRating.TripleBlack.rating)
   }

   if (minimumClearanceWindow1 < 5.0) {
      curRating = maxOf(curRating, JumpRating.DoubleBlack.rating)
   }

   if (minimumClearanceWindow1 < 7.0) {
      curRating = maxOf(curRating, JumpRating.SingleBlack.rating)
   }

   if (minimumClearanceWindow1 < 9.0) {
      curRating = maxOf(curRating, JumpRating.TripleBlue.rating)
   }

   if (minimumClearanceWindow1 < 11.0) {
      curRating = maxOf(curRating, JumpRating.DoubleBlue.rating)
   }

   if (minimumClearanceWindow1 < 13.0) {
      curRating = maxOf(curRating, JumpRating.SingleBlue.rating)
   }

   // Establish min rating by clearance of a median start in poor conditions (north-facing weak push)
   val minimumClearanceWindow2: Double? = calculateMinimumClearance(
      exitProfile = exitProfile,
      referenceFlight = nonIdealFlight,
      range = 30..250,
      mode = ClearanceMode.yDistance
   )

   // If the algo is unable to calculate minimum clearance, then do not auto generate a rating
   if (minimumClearanceWindow2 == null) {
      return null
   }

   if (minimumClearanceWindow2 < 5.0) {
      curRating = maxOf(curRating, JumpRating.Red.rating)
   }

   if (minimumClearanceWindow2 < 10.0) {
      curRating = maxOf(curRating, JumpRating.TripleBlack.rating)
   }

   if (minimumClearanceWindow2 < 20.0) {
      curRating = maxOf(curRating, JumpRating.DoubleBlack.rating)
   }

   if (minimumClearanceWindow2 < 30.0) {
      curRating = maxOf(curRating, JumpRating.SingleBlack.rating)
   }

   if (minimumClearanceWindow2 < 40.0) {
      curRating = maxOf(curRating, JumpRating.TripleBlue.rating)
   }

   if (minimumClearanceWindow2 < 50.0) {
      curRating = maxOf(curRating, JumpRating.DoubleBlue.rating)
   }

   if (minimumClearanceWindow2 < 60.0) {
      curRating = maxOf(curRating, JumpRating.SingleBlue.rating)
   }

   return curRating.toJumpRating()
}

private const val TIME = 0
private const val LAT = 1 // latitude (degrees)
private const val LON = 2 // longitude (degrees)
private const val HMSL = 3 // height MSL (m)
private const val VELN = 4 // velocity north (m/s)
private const val VELE = 5 // velocity east (m/s)
private const val VELD = 6 // velocity down (m/s)
private const val HACC = 7 //	Horizontal accuracy (m)
private const val VACC = 8 //	Vertical accuracy (m)
private const val SACC = 9 //	Speed accuracy (m/s)
private const val GPSFIX = 10 //	GPS fix type (3 = 3D)
private const val HEADING = 11 // UNKNOWN
private const val CACC = 12 // UNKNOWN
private const val NUMSV = 13 //	Number of satellites used in fix