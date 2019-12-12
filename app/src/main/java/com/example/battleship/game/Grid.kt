package com.example.battleship.game


class Grid() {
    var field: Array<Array<GridCell>> =
        Array(FIELD_SIZE) { Array(FIELD_SIZE) { GridCell.SEA } }

    private var amountOfShips: Array<Int> = arrayOf(0, 0, 0, 0)

    private var numberOfShips = 0
    fun getNumberOfShips(): Int {
        return numberOfShips
    }

    fun getAmountOfShipsByLength() = amountOfShips

    fun addShip(startIndexX: Int, startIndexY: Int, length: Int, direction: Boolean): Boolean {
        if (startIndexX !in 0 until FIELD_SIZE || startIndexY !in 0 until FIELD_SIZE
            || length !in MINIMAL_SHIP_SIZE..MAXIMAL_SHIP_SIZE
        ) {
            return false
        }

        if (amountOfShips[SHIPS_AMOUNT_HELP - length - 1] >= SHIPS_AMOUNT_HELP - length) {
            return false
        }

        if (direction) {
            if (startIndexX + length - 1 < FIELD_SIZE) {
                for (j in startIndexY - 1..startIndexY + 1) {
                    for (i in startIndexX - 1 until startIndexX + length + 1) {
                        if (i !in 0 until FIELD_SIZE || j !in 0 until FIELD_SIZE) {
                            continue
                        }
                        if (field[i][j] != GridCell.SEA) {
                            return false
                        }
                    }
                }
                for (i in startIndexX until startIndexX + length) {
                    field[i][startIndexY] =
                        GridCell.SHIP
                }
                amountOfShips[SHIPS_AMOUNT_HELP - length - 1]++
            } else {
                return false
            }
        } else {
            if (startIndexY + length - 1 < FIELD_SIZE) {
                for (j in startIndexX - 1..startIndexX + 1) {
                    for (i in startIndexY - 1 until startIndexY + length + 1) {
                        if (i !in 0 until FIELD_SIZE || j !in 0 until FIELD_SIZE) {
                            continue
                        }
                        if (field[j][i] != GridCell.SEA) {
                            return false
                        }
                    }
                }
                for (i in startIndexY until startIndexY + length) {
                    field[startIndexX][i] =
                        GridCell.SHIP
                }
                amountOfShips[SHIPS_AMOUNT_HELP - length - 1]++
            } else {
                return false
            }
        }
        numberOfShips++
        return true
    }

    fun getTotalAmountOfShips(): Int {
        var result = 0
        for (element in amountOfShips) {
            result += element
        }
        return result
    }

    fun updateField(x: Int, y: Int, shotResult: ShotResult): Boolean {
        if (x !in 0 until FIELD_SIZE || y !in 0 until FIELD_SIZE) {
            return false
        }

        if (field[x][y] != GridCell.SEA) {
            return false
        }

        when (shotResult) {
            ShotResult.HIT -> field[x][y] = GridCell.HIT
            ShotResult.MISS -> field[x][y] = GridCell.CHECKED
            ShotResult.DESTROYED -> {
                numberOfShips--
                field[x][y] = GridCell.HIT
                val cells = mutableListOf<Pair<Int, Int>>()
                cells.add(Pair(x, y))
                while (cells.isNotEmpty()) {
                    val cell = cells.first()
                    for (i in -1..1) {
                        for (k in -1..1) {
                            if (i != 0 || k != 0) {
                                if (cell.first + i in 0 until FIELD_SIZE && cell.second + k in 0 until FIELD_SIZE) {
                                    if (field[cell.first + i][cell.second + k] == GridCell.HIT) {
                                        cells.add(Pair(cell.first + i, cell.second + k))
                                    } else {
                                        if (field[cell.first + i][cell.second + k] == GridCell.SEA) {
                                            field[cell.first + i][cell.second + k] =
                                                GridCell.CHECKED
                                        }
                                    }
                                }
                            }
                        }
                    }
                    field[cell.first][cell.second] = GridCell.DESTROYED
                    cells.removeAt(0)
                }
            }
        }

        return true
    }

    fun shoot(x: Int, y: Int): ShotResult {
        if (x !in 0 until FIELD_SIZE || y !in 0 until FIELD_SIZE) {
            return ShotResult.MISS
        }

        return when (field[x][y]) {
            GridCell.SEA -> {
                field[x][y] = GridCell.CHECKED
                ShotResult.MISS
            }
            GridCell.SHIP -> {
                field[x][y] = GridCell.HIT
                if (isShipDestroyed(x, y)) {
                    ShotResult.DESTROYED
                } else {
                    ShotResult.HIT
                }
            }
            else -> ShotResult.MISS
        }
    }

    private fun isShipDestroyed(lastShotX: Int, lastShotY: Int): Boolean {
        val cells = mutableListOf<Pair<Int, Int>>()
        cells.add(Pair(lastShotX, lastShotY))
        val visited = mutableListOf<Pair<Int, Int>>()
        while (cells.isNotEmpty()) {
            val cell = cells.first()
            for (i in -1..1) {
                for (k in -1..1) {
                    if (i + k != 0 && i + k != 2) {
                        if (cell.first + i in 0 until FIELD_SIZE && cell.second + k in 0 until FIELD_SIZE) {
                            if (!visited.contains(Pair(cell.first + i, cell.second + k))) {
                                if (field[cell.first + i][cell.second + k] == GridCell.SHIP) return false
                                else if (field[cell.first + i][cell.second + k] == GridCell.HIT) {
                                    cells.add(Pair(cell.first + i, cell.second + k))
                                }
                            }
                        }
                    }
                }
            }
            visited.add(cell)
            cells.removeAt(0)
        }
        return true
    }

    fun restoreFieldFromString(str: String) {
        require(str.length == 100)

        for(i in 0..9) {
            for (j in 0..9) {
                field[i][j] = when(str[i * 10 + j]) {
                    '1' -> GridCell.CHECKED
                    '2' -> GridCell.SHIP
                    '3' -> GridCell.HIT
                    '4' -> GridCell.DESTROYED
                    else -> GridCell.SEA
                }
            }
        }
    }

    override fun toString(): String {
        val str = StringBuilder("")
        for (i in 0..9) {
            for (j in 0..9) {
                str.append(when(field[i][j]) {
                    GridCell.SEA -> 0
                    GridCell.CHECKED -> 1
                    GridCell.SHIP -> 2
                    GridCell.HIT -> 3
                    GridCell.DESTROYED -> 4
                }.toString())
            }
        }
        return str.toString()
    }

    companion object {
        private const val FIELD_SIZE = 10
        private const val MAXIMAL_SHIP_SIZE = 4
        private const val MINIMAL_SHIP_SIZE = 1
        private const val SHIPS_AMOUNT_HELP = 5
    }
}