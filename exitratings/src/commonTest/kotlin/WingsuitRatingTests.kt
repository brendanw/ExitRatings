package com.basebeta

import com.basebeta.exitratings.JumpRating
import com.basebeta.exitratings.Point
import com.basebeta.wingsuit.assembleWingsuitRating
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class WingsuitRatingTests {

   @Test
   fun `no rating generated for exit without laser profile`() = runTest {
      val rating = assembleWingsuitRating(
         exitProfile = emptyList(),
         flyableAltitude = 1000,
         minimumRequiredGlide = 2.0,
         hmsl = 2000
      )

      assertEquals(rating?.rating, null)
   }

   @Test
   fun `vuardes rating`() = runTest {
      val vuardesProfile = listOf(
         Point(0, 0),
         Point(21, -200),
         Point(42, -250),
         Point(84, -300),
         Point(126, -350),
         Point(168, -400),
         Point(211, -450),
         Point(274, -500),
         Point(337, -550),
         Point(379, -600),
         Point(442, -650),
         Point(484, -700),
         Point(568, -750),
         Point(653, -800),
         Point(737, -850),
         Point(842, -900),
         Point(989, -950)
      )

      val rating = assembleWingsuitRating(
         exitProfile = vuardesProfile,
         flyableAltitude = 1036,
         minimumRequiredGlide = 2.1,
         hmsl = 1462
      )

      assertEquals(
         expected = JumpRating.SingleBlue.rating,
         actual = rating?.rating
      )
   }

   /**
    * This exit feels more visual than a single blue...probably because heading performance matters here
    * That calls for the need for a yes/no question to bump a rating up
    * End of ledge 1 is at 16m,-74m
    * End of ledge 2 is at 47m,-164m
    */
   @Test
   fun `Romsdalseggen`() = runTest {
      val romsdalseggen = listOf(
         Point(1, -22),
         Point(3, -48),
         Point(4, -48),
         Point(5, -49),
         Point(10, -59),
         Point(12, -74),
         Point(14, -74),
         Point(16, -74),
         Point(47, -164),
         Point(51, -222),
         Point(54, -223),
         Point(57, -232),
         Point(63, -240),
         Point(68, -240),
         Point(69, -243),
         Point(85, -239),
         Point(88, -239),
         Point(92, -241)
      )

      val rating = assembleWingsuitRating(
         exitProfile = romsdalseggen,
         flyableAltitude = 1116,
         minimumRequiredGlide = 2.0,
         hmsl = 1125
      )

      assertEquals(
         expected = JumpRating.SingleBlue.rating,
         actual = rating?.rating
      )
   }

   @Test
   fun `Nunn -- Mid Mountain`() = runTest {
      val nunnProfile = listOf(
         Point(0, 0),
         Point(1, -38),
         Point(3, -47),
         Point(8, -84),
         Point(9, -84),
         Point(10, -85),
         Point(13, -97),
         Point(15, -99),
         Point(23, -134),
         Point(29, -134),
         Point(51, -159),
         Point(59, -166),
         Point(89, -187),
      )

      val rating = assembleWingsuitRating(
         exitProfile = nunnProfile,
         flyableAltitude = 1020,
         minimumRequiredGlide = 2.5,
         hmsl = 2450
      )

      assertEquals(
         expected = JumpRating.TripleBlue.rating,
         actual = rating?.rating
      )
   }

   @Test
   fun `Squaw Back`() = runTest {
      val squawBackProfile = listOf(
         Point(0, 0),
         Point(8, -50),
         Point(10, -53),
         Point(14, -66),
         Point(16, -69),
         Point(19, -73),
         Point(39, -145),
         Point(43, -146),
         Point(47, -147),
         Point(50, -153),
         Point(53, -159),
         Point(63, -160),
         Point(65, -164),
         Point(74, -164)
      )

      val rating = assembleWingsuitRating(
         exitProfile = squawBackProfile,
         flyableAltitude = 821,
         minimumRequiredGlide = 2.29,
         hmsl = 2396
      )

      assertEquals(
         expected = JumpRating.SingleBlack.rating,
         actual = rating?.rating
      )
   }

   @Test
   fun `Mt Buller`() = runTest {
      val buller = listOf(
         Point(0, 0),
         Point(6, -123),
         Point(7, -123),
         Point(11, -132),
         Point(16, -152),
         Point(19, -165),
         Point(25, -177),
         Point(28, -180),
         Point(29, -179),
         Point(36, -186),
         Point(42, -191),
         Point(45, -189),
         Point(85, -325),
         Point(116, -332),
         Point(121, -334),
         Point(125, -346),
         Point(157, -375),
         Point(169, -381),
         Point(179, -386),
         Point(179, -394),
         Point(195, -407),
         Point(212, -417),
         Point(236, -432),
         Point(245, -459),
         Point(260, -461),
         Point(284, -478),
         Point(307, -496),
         Point(340, -532),
         Point(467, -603)
      )

      val rating = assembleWingsuitRating(
         exitProfile = buller,
         flyableAltitude = 1100,
         minimumRequiredGlide = 2.65,
         hmsl = 2802
      )

      assertEquals(
         expected = JumpRating.SingleBlack.rating,
         actual = rating?.rating
      )
   }

   @Test
   fun `turret head south`() = runTest {
      val turretHeadProfile = listOf(
         Point(0, 0),
         Point(6, -37),
         Point(17, -106),
         Point(19, -113),
         Point(27, -135),
         Point(31, -146),
         Point(33, -151),
         Point(36, -160),
         Point(41, -165),
         Point(51, -175),
         Point(70, -200),
         Point(120, -250),
         Point(175, -300),
      )

      val rating = assembleWingsuitRating(
         exitProfile = turretHeadProfile,
         flyableAltitude = 1801,
         minimumRequiredGlide = 1.7,
         hmsl = 2301
      )

      assertEquals(expected = JumpRating.DoubleBlack.rating, actual = rating?.rating)
   }

   @Test
   fun `Timpanogos Cave`() = runTest {
      val timpanogosCave = listOf(
         Point(0, 0),
         Point(32, -112),
         Point(36, -116),
         Point(48, -129),
         Point(55, -137),
         Point(86, -158),
         Point(148, -198),
         Point(206, -239),
         Point(270, -275),
         Point(338, -333),
         Point(782, -447),
         Point(1505, -794),
         Point(1660, -804)
      )

      val rating = assembleWingsuitRating(
         exitProfile = timpanogosCave,
         flyableAltitude = 800,
         minimumRequiredGlide = 2.0,
         hmsl = 2369
      )

      assertEquals(expected = JumpRating.TripleBlack.rating, actual = rating?.rating)
   }

   /**
    * This one feels odd as the red is generated due to the 5m,-25m ledge which is a tighter ledge to clear, but after
    * that the rest of the flight is really nice.
    */
   @Test
   fun `Cascade`() = runTest {
      val cascade = listOf(
         Point(0, 0),
         Point(5, -25),
         Point(32, -143),
         Point(89, -208),
         Point(146, -258)
      )

      val rating = assembleWingsuitRating(
         exitProfile = cascade,
         flyableAltitude = 1494,
         minimumRequiredGlide = 2.2,
         hmsl = 2917
      )

      assertEquals(expected = JumpRating.Red.rating, actual = rating?.rating)
   }

   @Test
   fun `Helios`() = runTest {
      val heliosProfile = listOf(
         Point(0, 0),
         Point(2, -10),
         Point(13, -52),
         Point(14, -57),
         Point(30, -124),
         Point(50, -136),
         Point(72, -151),
         Point(90, -157),
         Point(60, -140),
         Point(170, -211),
         Point(296, -281),
         Point(555, -422),
         Point(1328, -700),
         Point(1435, -700)
      )

      val rating = assembleWingsuitRating(
         exitProfile = heliosProfile,
         flyableAltitude = 875,
         minimumRequiredGlide = 2.8,
         hmsl = 2916
      )

      assertEquals(expected = JumpRating.Red.rating, actual = rating?.rating)
   }

   @Test
   fun `Nunn -- High`() = runTest {
      val highNunnProfile = listOf(
         Point(0, 0),
         Point(12, -55),
         Point(20, -97),
         Point(25, -111),
         Point(34, -120),
         Point(62, -142),
         Point(87, -147),
         Point(126, -203),
      )

      val rating = assembleWingsuitRating(
         exitProfile = highNunnProfile,
         flyableAltitude = 1801,
         minimumRequiredGlide = 2.5,
         hmsl = 2635
      )

      assertEquals(expected = JumpRating.Red.rating, actual = rating?.rating)
   }

}