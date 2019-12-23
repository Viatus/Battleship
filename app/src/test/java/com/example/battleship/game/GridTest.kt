package com.example.battleship.game

import org.junit.Test

import org.junit.Assert.*

class GridTest {

    @Test
    fun addShip() {
        val grid = Grid()
        for (i in 0..9) {
            for (k in 0..9) {
                assertEquals(GridCell.SEA, grid.field[i][k])
            }
        }
        assertTrue(grid.addShip(0, 0, 1, true))
        assertEquals(GridCell.SHIP, grid.field[0][0])
        assertFalse(grid.addShip(0, 0, 1, true))
        assertFalse(grid.addShip(0, 0, 4, true))

        assertFalse(grid.addShip(10, 0, 1, true))
        assertFalse(grid.addShip(0, 10, 1, true))
        assertFalse(grid.addShip(9, 7, 2, true))
        assertFalse(grid.addShip(7, 9, 2, false))

        assertTrue(grid.addShip(7, 9, 2, true))
        for (i in 7..8) {
            assertEquals(GridCell.SHIP, grid.field[i][9])
        }

        assertTrue(grid.addShip(5, 7, 2, false))
        for (i in 7..8) {
            assertEquals(GridCell.SHIP, grid.field[5][i])
        }

        assertFalse(grid.addShip(5, 9, 4, true))
        assertFalse(grid.addShip(2, 2, 5, true))
    }

    @Test
    fun updateField() {
        val grid = Grid()
        assertEquals(listOf(Pair(0, 0)), grid.updateField(0, 0, ShotResult.MISS))
        assertEquals(GridCell.CHECKED, grid.field[0][0])

        assertEquals(null, grid.updateField(0, 0, ShotResult.HIT))

        assertEquals(listOf(Pair(0, 1)), grid.updateField(0, 1, ShotResult.HIT))
        assertEquals(GridCell.HIT, grid.field[0][1])
        assertEquals(listOf(Pair(0, 2)), grid.updateField(0, 2, ShotResult.HIT))
        assertEquals(GridCell.HIT, grid.field[0][2])
        assertEquals(
            listOf(
                Pair(0, 4),
                Pair(1, 2),
                Pair(1, 3),
                Pair(1, 4),
                Pair(0, 3),
                Pair(1, 1),
                Pair(0, 2),
                Pair(1, 0),
                Pair(0, 1)
            ),
            grid.updateField(0, 3, ShotResult.DESTROYED)
        )
        for (i in 1..3) {
            assertEquals(GridCell.DESTROYED, grid.field[0][i])
        }
        for (i in 0..3) {
            assertEquals(GridCell.CHECKED, grid.field[1][i])
        }
        assertEquals(GridCell.CHECKED, grid.field[0][4])
        assertEquals(GridCell.CHECKED, grid.field[1][4])
    }

    @Test
    fun shoot() {
        val grid = Grid()
        grid.addShip(0, 0, 3, true)

        assertEquals(ShotResult.MISS, grid.shoot(1, 2))
        assertEquals(ShotResult.HIT, grid.shoot(2, 0))
        assertEquals(ShotResult.HIT, grid.shoot(0, 0))
        assertEquals(ShotResult.DESTROYED, grid.shoot(1, 0))
    }
}