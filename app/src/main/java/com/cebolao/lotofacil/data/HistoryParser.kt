package com.cebolao.lotofacil.data

import android.util.Log

object HistoryParser {
    private const val TAG = "HistoryParser"
    private val lineRegex = """^\s*(\d+)\s*-\s*([\d,\s]+)$""".toRegex()

    fun parseLine(line: String): HistoricalDraw? {
        try {
            val matchResult = lineRegex.find(line.trim()) ?: return null

            val (contestStr, numbersStr) = matchResult.destructured
            val contestNumber = contestStr.toIntOrNull() ?: return null

            if (contestNumber <= 0) {
                return null
            }

            val numbers = numbersStr.split(',')
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in LotofacilConstants.VALID_NUMBER_RANGE }
                .toSet()

            return if (numbers.size == LotofacilConstants.GAME_SIZE) {
                HistoricalDraw(contestNumber, numbers)
            } else {
                Log.w(TAG, "Linha de histórico inválida para o concurso $contestNumber: dezenas inválidas ou contagem incorreta. Linha: '$line'")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao analisar a linha do histórico: $line", e)
            return null
        }
    }
}