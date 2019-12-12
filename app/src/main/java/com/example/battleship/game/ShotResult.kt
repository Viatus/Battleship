package com.example.battleship.game

enum class ShotResult {
    MISS {
        override fun toString(): String {
            return "MISS"
        }
    }, HIT {
        override fun toString(): String {
            return "HIT"
        }
    }, DESTROYED {
        override fun toString(): String {
            return "DESTR"
        }
    }
}