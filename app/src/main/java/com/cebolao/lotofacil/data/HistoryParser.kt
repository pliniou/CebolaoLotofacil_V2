package com.cebolao.lotofacil.data

import android.util.Log

object HistoryParser {
    private const val TAG = "HistoryParser"
    // Regex simplificada para capturar o número do concurso e uma lista de números separados por vírgula.
    // Aceita 1 ou 2 dígitos e permite espaços.
    private val lineRegex = """^\s*\d+\s*-\s*[\d, ]+\s*""".toRegex()

    /**
     * Parse a single line in the format "CONCURSO - N1,N2,N3,..."
     * Returns HistoricalDraw or null when line is invalid.
     */
    fun parseLine(line: String): HistoricalDraw? {
        try {
            val trimmed = line.trim()
            if (!lineRegex.matches(trimmed)) {
                Log.w(TAG, "Linha rejeitada pela regex: $line")
                return null
            }

            val parts = trimmed.split("-", limit = 2)
            if (parts.size != 2) return null

            val contestNumber = parts[0].trim().toIntOrNull() ?: return null
            if (contestNumber <= 0) {
                Log.w(TAG, "Número de concurso inválido: $contestNumber")
                return null
            }

            val numberStrings = parts[1].split(",").map { it.trim() }

            // CORREÇÃO: Validar a contagem de dezenas ANTES de convertê-las.
            // A linha deve conter exatamente o número de dezenas do jogo (15).
            if (numberStrings.size != LotofacilConstants.GAME_SIZE) {
                Log.w(TAG, "Quantidade de dezenas inválida na linha: ${numberStrings.size} em $line")
                return null
            }

            val numbers = numberStrings
                .mapNotNull { it.toIntOrNull() }
                .filter { it in LotofacilConstants.VALID_NUMBER_RANGE }
                .toSet()

            // Após a filtragem, o conjunto deve ainda ter 15 números, o que implica que todos eram válidos.
            if (numbers.size != LotofacilConstants.GAME_SIZE) {
                Log.w(TAG, "Dezenas inválidas (fora do intervalo 1-25) encontradas em: $line")
                return null
            }

            return HistoricalDraw(contestNumber, numbers)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse line: $line", e)
            return null
        }
    }
}