package com.cebolao.lotofacil.data

import android.util.Log

object HistoryParser {
    private const val TAG = "HistoryParser"
    private val lineRegex = """^\s*\d+\s*-\s*[\d]{1,2}(?:\s*,\s*\d{1,2})*\s*""".toRegex()

    /**
     * Parse a single line in the format "CONCURSO - N1,N2,N3,..."
     * Returns HistoricalDraw or null when line is invalid.
     */
    fun parseLine(line: String): HistoricalDraw? {
        try {
            val trimmed = line.trim()
            if (!lineRegex.matches(trimmed)) return null

            val parts = trimmed.split("-", limit = 2)
            if (parts.size != 2) return null

            val contestNumber = parts[0].trim().toIntOrNull() ?: return null
            val numbers = parts[1].split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in LotofacilConstants.VALID_NUMBER_RANGE }
                .toSet()

            if (contestNumber <= 0) return null
            if (numbers.size != LotofacilConstants.GAME_SIZE) {
                return null
            }

            return HistoricalDraw(contestNumber, numbers)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse line: $line", e)
            return null
        }
    }
}