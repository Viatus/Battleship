package com.example.battleship.game

class Match {
    val primaryGrid = Grid()
    val trackingGrid = Grid()

    fun isGameLost(): Boolean {
        return primaryGrid.getTotalAmountOfShips() == 0
    }

}