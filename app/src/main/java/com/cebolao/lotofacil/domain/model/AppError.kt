package com.cebolao.lotofacil.domain.model

import androidx.annotation.StringRes

/**
 * Hierarquia de erros da aplicação.
 * Facilita tratamento centralizado e logging.
 */
sealed class AppError(
    @StringRes val messageResId: Int,
    val throwable: Throwable? = null
) {
    
    data class Network(
        @StringRes val resId: Int,
        val exception: Throwable? = null
    ) : AppError(resId, exception)
    
    data class Database(
        @StringRes val resId: Int,
        val exception: Throwable? = null
    ) : AppError(resId, exception)
    
    data class Validation(
        @StringRes val resId: Int
    ) : AppError(resId)
    
    data class Unknown(
        @StringRes val resId: Int,
        val exception: Throwable? = null
    ) : AppError(resId, exception)
}