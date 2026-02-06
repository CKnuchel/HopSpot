package com.kickpaws.hopspot.data.remote.error

import com.google.gson.Gson
import com.kickpaws.hopspot.data.remote.dto.ErrorResponseDto
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Sealed class representing different types of API errors.
 */
sealed class ApiError : Exception() {
    /** No network connection available */
    data object NoNetwork : ApiError()

    /** Request timed out */
    data object Timeout : ApiError()

    /** Server returned an error with an error code */
    data class ServerError(
        val errorCode: String,
        val httpStatus: Int,
        val serverMessage: String? = null
    ) : ApiError()

    /** Unknown or unexpected error */
    data class Unknown(override val message: String?) : ApiError()
}

/**
 * Parser that converts exceptions to ApiError instances.
 */
object ApiErrorParser {
    private val gson = Gson()

    /**
     * Parses a throwable into an ApiError.
     *
     * @param throwable The exception to parse
     * @return The corresponding ApiError
     */
    fun parse(throwable: Throwable): ApiError {
        return when (throwable) {
            // Already an ApiError, return as-is
            is ApiError -> throwable

            // Network connectivity issues
            is UnknownHostException -> ApiError.NoNetwork
            is IOException -> {
                if (throwable.message?.contains("Unable to resolve host") == true) {
                    ApiError.NoNetwork
                } else {
                    ApiError.Unknown(throwable.message)
                }
            }

            // Timeout
            is SocketTimeoutException -> ApiError.Timeout

            // HTTP errors from Retrofit
            is HttpException -> parseHttpException(throwable)

            // Fallback for unknown errors
            else -> ApiError.Unknown(throwable.message)
        }
    }

    private fun parseHttpException(exception: HttpException): ApiError {
        val errorBody = exception.response()?.errorBody()?.string()

        return if (errorBody != null) {
            try {
                val errorResponse = gson.fromJson(errorBody, ErrorResponseDto::class.java)
                ApiError.ServerError(
                    errorCode = errorResponse.errorCode,
                    httpStatus = exception.code(),
                    serverMessage = errorResponse.message
                )
            } catch (e: Exception) {
                // Failed to parse error response, try legacy format
                tryParseLegacyError(errorBody, exception.code())
            }
        } else {
            // No error body, create generic server error
            ApiError.ServerError(
                errorCode = getGenericErrorCode(exception.code()),
                httpStatus = exception.code(),
                serverMessage = exception.message()
            )
        }
    }

    /**
     * Tries to parse legacy error format: {"error": "message"}
     * This maintains backwards compatibility during migration.
     */
    private fun tryParseLegacyError(errorBody: String, httpStatus: Int): ApiError {
        return try {
            val legacyError = gson.fromJson(errorBody, LegacyErrorDto::class.java)
            ApiError.ServerError(
                errorCode = getGenericErrorCode(httpStatus),
                httpStatus = httpStatus,
                serverMessage = legacyError.error
            )
        } catch (e: Exception) {
            ApiError.ServerError(
                errorCode = getGenericErrorCode(httpStatus),
                httpStatus = httpStatus,
                serverMessage = errorBody
            )
        }
    }

    /**
     * Maps HTTP status codes to generic error codes when no specific code is provided.
     */
    private fun getGenericErrorCode(httpStatus: Int): String {
        return when (httpStatus) {
            400 -> "VALIDATION_INVALID_REQUEST"
            401 -> "AUTH_INVALID_TOKEN"
            403 -> "AUTH_FORBIDDEN"
            404 -> "SYSTEM_NOT_FOUND"
            409 -> "SYSTEM_CONFLICT"
            429 -> "SYSTEM_RATE_LIMITED"
            in 500..599 -> "SYSTEM_INTERNAL_ERROR"
            else -> "SYSTEM_INTERNAL_ERROR"
        }
    }

    /**
     * Legacy error DTO for backwards compatibility.
     */
    private data class LegacyErrorDto(val error: String?)
}
