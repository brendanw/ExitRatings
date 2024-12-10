package com.basebeta.exitratings

data class FlysightRow(
   var time: String = "", //Time in ISO8601 format
   var lat: Double = 0.0, //Latitude (degrees)
   var lon: Double = 0.0, //Longitude (degrees)
   var hMSL: Double = 0.0, //Height above sea level (m)
   var velN: Double = 0.0, //	Velocity north (m/s)
   var velE: Double = 0.0, //	Velocity east (m/s)
   var velD: Double = 0.0, //	Velocity down (m/s)
   var hAcc: Double = 0.0, //	Horizontal accuracy (m)
   var vAcc: Double = 0.0, //	Vertical accuracy (m)
   var sAcc: Double = 0.0, //	Speed accuracy (m/s)
   var heading: Double = 0.0, // unknown
   var cAcc: Double = 0.0, // unknown
   var gpsFix: Double = 0.0, //	GPS fix type (3 = 3D)
   var numSV: Int = 0 //	Number of satellites used in fix
) {
   override fun toString(): String {
      return "$time,$lat,$lon,$hMSL,$velN,$velE,$velD,$hAcc,$vAcc,$sAcc,$heading,$cAcc,$gpsFix,$numSV"
   }
}