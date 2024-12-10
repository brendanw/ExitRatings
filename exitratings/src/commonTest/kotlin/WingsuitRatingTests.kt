package com.basebeta

import com.basebeta.wingsuit.assembleWingsuitRating
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
}