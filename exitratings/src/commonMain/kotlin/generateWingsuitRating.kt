package io.github.kotlin.fibonacci

// North-facing Le Pleurer (3700m) Aura 3 August 6, 2018
const val nonIdeal3700 = "https://basebeta-east.s3.amazonaws.com/tracks/5903c6c041de5f0004bddcb2/fixed-fixedJump.CSV"

// North-facing Aspen Grove (3200m) Aura 5 September 12, 2023
const val nonIdeal3200 = "https://basebeta-east.s3.amazonaws.com/tracks/5903c6c041de5f0004bddcb2/75df24-16-42-38.CSV"

// North-facing Mt Buller (2800m) Corvid2 July 12, 2024
const val nonIdeal2800 = "https://basebeta.com/tracks/view/6691cc2ce6cd3e6d6d4fded7"

// North-facing Rote Wand (2500m) Aura 5 Sept 24, 2024
const val nonIdeal2500 = "https://basebeta-east.s3.amazonaws.com/tracks/5903c6c041de5f0004bddcb2/6ce575-roteWandRock.CSV"

// North-facing Schonangerspitze (2300m) Aura 5 Oct 1, 2024
const val nonIdeal2300 = "https://basebeta-east.s3.amazonaws.com/tracks/5903c6c041de5f0004bddcb2/88a6d9-10-10-30.CSV"

// North-facing Adobe Point (2100m) Corvid2 Nov 12, 2022
const val nonIdeal2100 = "https://basebeta-east.s3.amazonaws.com/tracks/5903c6c041de5f0004bddcb2/5pelak-adobePoint1.CSV"

// North-facing Godzilla (1900m) Aura5 Sept 20, 2024
const val nonIdeal1900 = "https://basebeta-east.s3.amazonaws.com/tracks/5903c6c041de5f0004bddcb2/08c139-10-18-54.CSV"

// North-facing Stiggbothornet (1500m) Aura5 Aug 24, 2023
const val nonIdeal1500 = "https://basebeta-east.s3.amazonaws.com/tracks/5903c6c041de5f0004bddcb2/a01db7-stigbot-newflysight.CSV"

suspend fun assembleWingsuitRating(exit: ExitEntity, client: HttpClient, db: MongoDatabase): JumpRating? {
   val referenceFlight = exit.referenceFlights.firstOrNull() ?: return null

   val hmsl = exit.jumpMetrics.exitPointMSL
   val nonIdealFlightUrl = when {
      hmsl <= 1700 -> nonIdeal1500
      hmsl <= 1900 -> nonIdeal1900
      hmsl <= 2000 -> nonIdeal2100
      hmsl <= 2200 -> nonIdeal2300
      hmsl <= 2450 -> nonIdeal2500
      hmsl <= 2650 -> nonIdeal2800
      hmsl <= 3000 -> nonIdeal3200
      hmsl <= 3400 -> nonIdeal3700
      else -> nonIdeal3700
   }

   val nonIdealFlight = getNonIdealFlight(nonIdealFlightUrl, client)

   val track = db.getCollection<TrackEntity>("tracks").findOneById(referenceFlight.trackId) ?: run {
      println("no reference track for ${exit.name}")
      return null
   }

   return generateWingsuitRating(
      exit = exit,
      glideHeuristic = track.glideHeuristic,
      nonIdealFlight = nonIdealFlight
   )
}

suspend fun getNonIdealFlight(flightUrl: String, client: HttpClient): List<FlysightRow> {
   try {
      val resultStr = client.get(urlString = flightUrl).bodyAsText()
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
 * @param glideHeuristic glideHeuristic value taken from a track flown from the very same exit
 * @param nonIdealFlight list of flysight rows where first row is the user pushing from exit. flight should be
 * from a north-facing exit w/ large suit from exit point with an HMSL value no greater than 500m different from exit
 */
fun generateWingsuitRating(
   exitProfile: List<Point>,
   flyableAltitude: Int,
   glideHeuristic: Double,
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
      if (glideHeuristic > 2.0) {
         curRating = maxOf(curRating, JumpRating.SingleBlue.rating)
      }

      if (glideHeuristic > 2.2) {
         curRating = maxOf(curRating, JumpRating.DoubleBlue.rating)
      }

      if (glideHeuristic > 2.5) {
         curRating = maxOf(curRating, JumpRating.SingleBlack.rating)
      }

      if (glideHeuristic > 2.7) {
         curRating = maxOf(curRating, JumpRating.DoubleBlack.rating)
      }

      if (glideHeuristic >= 3.0) {
         curRating = maxOf(curRating, JumpRating.Red.rating)
      }
   }

   // Establish min rating by clearance of a median start in poor conditions (north-facing weak push)
   val minimumClearance: Double? = calculateMinimumClearance(
      exitProfile = exitProfile,
      referenceFlight = nonIdealFlight
   )

   // If the algo is unable to calculate minimum clearance, then do not auto generate a rating
   if (minimumClearance == null) {
      return null
   }

   if (minimumClearance < 0.0) {
      curRating = maxOf(curRating, JumpRating.Red.rating)
   }

   if (minimumClearance < 5.0) {
      curRating = maxOf(curRating, JumpRating.TripleBlack.rating)
   }

   if (minimumClearance < 10.0) {
      curRating = maxOf(curRating, JumpRating.DoubleBlack.rating)
   }

   if (minimumClearance < 20.0) {
      curRating = maxOf(curRating, JumpRating.SingleBlack.rating)
   }

   if (minimumClearance < 30.0) {
      curRating = maxOf(curRating, JumpRating.TripleBlue.rating)
   }

   if (minimumClearance < 40.0) {
      curRating = maxOf(curRating, JumpRating.DoubleBlue.rating)
   }

   if (minimumClearance < 50.0) {
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