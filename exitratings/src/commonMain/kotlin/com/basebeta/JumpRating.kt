package com.basebeta

enum class JumpRating(val rating: Int) {
   Green(0),
   SingleBlue(1),
   DoubleBlue(2),
   TripleBlue(3),
   SingleBlack(4),
   DoubleBlack(5),
   TripleBlack(6),
   Red(7)
}

fun Int.toJumpRating(): JumpRating {
   return JumpRating.entries.first { it.rating == this }
}