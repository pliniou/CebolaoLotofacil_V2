package com.cebolao.lotofacil.data

import android.util.Log

private const val TAG = "HistoryParser"

object HistoryParser {
    private val lineRegex = """^\s*(\d+)\s*-\s*([\d,\s]+)$""".toRegex()

    fun parseLine(line: String): HistoricalDraw? {
        return runCatching {
            val (contestStr, numbersStr) = lineRegex.find(line.trim())?.destructured
                ?: return@runCatching null

            val contestNumber = contestStr.toIntOrNull()?.takeIf { it > 0 }
                ?: return@runCatching null

            val numbers = numbersStr.split(',')
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in LotofacilConstants.NUMBER_RANGE }
                .toSet()

            if (numbers.size == LotofacilConstants.GAME_SIZE) {
                HistoricalDraw(contestNumber, numbers)
            } else {
                Log.w(
                    TAG,
                    "Invalid history line for contest $contestNumber: invalid numbers or incorrect count. Line: '$line'"
                )
                null
            }
        }.onFailure { e ->
            Log.w(TAG, "Failed to parse history line: $line", e)
        }.getOrNull()
    }
}