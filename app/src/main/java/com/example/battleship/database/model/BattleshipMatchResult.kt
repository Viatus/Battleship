package com.example.battleship.database.model

class BattleshipMatchResult {
    var id: Int = 0
    var result: Int? = null
    var playerField: String? = null
    var opponentField: String? = null
    var duration: Int? = null
    var opponentName: String? = null

    constructor(
        id: Int, result: Int, playerField: String, opponentField: String,
        duration: Int, opponentName: String
    ) {
        this.id = id
        this.result = result
        this.playerField = playerField
        this.opponentField = opponentField
        this.duration = duration
        this.opponentName = opponentName
    }

    constructor(
        result: Int, playerField: String, opponentField: String,
        duration: Int, opponentName: String
    ) {
        this.result = result
        this.playerField = playerField
        this.opponentField = opponentField
        this.duration = duration
        this.opponentName = opponentName
    }

    companion object {
        const val BATTLESHIP_WIN = 1
        const val BATTLESHIP_LOSE = -1
    }
}