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
    fun `dummy test`() = runTest {
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

        assertNotEquals(rating?.rating, null)
        assertEquals(rating?.rating, JumpRating.SingleBlue.rating)
    }


}