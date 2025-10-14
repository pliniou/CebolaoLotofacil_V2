package com.cebolao.lotofacil.data

import kotlinx.serialization.Serializable

@Serializable
enum class FilterType(
    val title: String,
    val description: String,
    val fullRange: ClosedFloatingPointRange<Float>,
    val defaultRange: ClosedFloatingPointRange<Float>,
    val category: FilterCategory,
    val historicalSuccessRate: Float
) {
    SOMA_DEZENAS(
        title = "Soma das Dezenas",
        description = "A soma dos 15 números. A grande maioria dos sorteios (cerca de 72%) possui uma soma entre 170 e 220. Este é um dos filtros mais eficazes para eliminar combinações improváveis.",
        fullRange = 120f..270f, // Soma mínima (1..15) e máxima (11..25)
        defaultRange = 170f..220f,
        category = FilterCategory.MATHEMATICAL,
        historicalSuccessRate = 0.72f
    ),

    PARES(
        title = "Números Pares",
        description = "A quantidade de números pares. O ideal é um equilíbrio. Cerca de 78% dos sorteios têm entre 6 e 9 números pares.",
        fullRange = 0f..12f, // Existem 12 números pares de 1 a 25
        defaultRange = 6f..9f,
        category = FilterCategory.DISTRIBUTION,
        historicalSuccessRate = 0.78f
    ),

    PRIMOS(
        title = "Números Primos",
        description = "A quantidade de números primos. Existem 9 primos (2,3,5,7,11,13,17,19,23). Cerca de 74% dos sorteios têm entre 4 e 7 primos.",
        fullRange = 0f..9f,
        defaultRange = 4f..7f,
        category = FilterCategory.MATHEMATICAL,
        historicalSuccessRate = 0.74f
    ),

    MOLDURA(
        title = "Dezenas na Moldura",
        description = "A quantidade de números nas bordas do volante (16 números no total). Cerca de 76% dos sorteios têm entre 8 e 11 números da moldura.",
        fullRange = 0f..15f, // Máximo de 15 números no jogo
        defaultRange = 8f..11f,
        category = FilterCategory.POSITIONAL,
        historicalSuccessRate = 0.76f
    ),

    RETRATO(
        title = "Dezenas no Retrato",
        description = "A quantidade de números no centro do volante (9 números no total). Cerca de 71% dos sorteios têm entre 4 e 7 números do retrato.",
        fullRange = 0f..9f,
        defaultRange = 4f..7f,
        category = FilterCategory.POSITIONAL,
        historicalSuccessRate = 0.71f
    ),

    FIBONACCI(
        title = "Sequência de Fibonacci",
        description = "A quantidade de números da sequência de Fibonacci (1,2,3,5,8,13,21). Cerca de 68% dos sorteios têm entre 3 e 5 números de Fibonacci.",
        fullRange = 0f..7f,
        defaultRange = 3f..5f,
        category = FilterCategory.MATHEMATICAL,
        historicalSuccessRate = 0.68f
    ),

    MULTIPLOS_DE_3(
        title = "Múltiplos de 3",
        description = "A quantidade de números que são múltiplos de 3. Cerca de 69% dos sorteios têm entre 3 e 6 múltiplos de 3.",
        fullRange = 0f..8f, // Existem 8 múltiplos de 3
        defaultRange = 3f..6f,
        category = FilterCategory.MATHEMATICAL,
        historicalSuccessRate = 0.69f
    ),

    REPETIDAS_CONCURSO_ANTERIOR(
        title = "Repetidas do Anterior",
        description = "A quantidade de números que se repetiram do concurso anterior. Este é um padrão muito forte: 84% dos sorteios repetem entre 8 e 10 números.",
        fullRange = 0f..15f,
        defaultRange = 8f..10f,
        category = FilterCategory.TEMPORAL,
        historicalSuccessRate = 0.84f
    );
}

@Serializable
enum class FilterCategory {
    MATHEMATICAL,
    DISTRIBUTION,
    POSITIONAL,
    TEMPORAL
}

data class FilterPreset(
    val name: String,
    val description: String,
    val settings: Map<FilterType, ClosedFloatingPointRange<Float>?>
)

val filterPresets = listOf(
    FilterPreset(
        name = "Conservador",
        description = "Filtros mais comuns e com maior chance de sucesso.",
        settings = mapOf(
            FilterType.SOMA_DEZENAS to (180f..220f),
            FilterType.PARES to (7f..9f),
            FilterType.REPETIDAS_CONCURSO_ANTERIOR to (8f..10f),
            FilterType.MOLDURA to (9f..11f)
        )
    ),
    FilterPreset(
        name = "Equilibrado",
        description = "Uma boa mistura de filtros populares.",
        settings = mapOf(
            FilterType.SOMA_DEZENAS to (170f..220f),
            FilterType.PARES to (6f..9f),
            FilterType.PRIMOS to (4f..7f),
            FilterType.REPETIDAS_CONCURSO_ANTERIOR to (8f..10f)
        )
    ),
    FilterPreset(
        name = "Ousado",
        description = "Busca padrões menos frequentes.",
        settings = mapOf(
            FilterType.FIBONACCI to (5f..6f),
            FilterType.PRIMOS to (6f..8f),
            FilterType.PARES to (5f..7f),
            FilterType.REPETIDAS_CONCURSO_ANTERIOR to (10f..11f)
        )
    )
)