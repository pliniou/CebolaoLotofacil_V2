package com.cebolao.lotofacil.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class NumberFrequency(
    val number: Int,
    val frequency: Int
)