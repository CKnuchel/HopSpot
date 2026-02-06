package com.kickpaws.hopspot.data.remote.error

import android.content.Context
import com.kickpaws.hopspot.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps API errors to user-friendly German messages.
 */
@Singleton
class ErrorMessageMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Gets a user-friendly message for the given API error.
     *
     * @param error The API error to get a message for
     * @return A localized, user-friendly error message
     */
    fun getMessage(error: ApiError): String {
        return when (error) {
            is ApiError.NoNetwork -> context.getString(R.string.error_no_network)
            is ApiError.Timeout -> context.getString(R.string.error_timeout)
            is ApiError.ServerError -> getServerErrorMessage(error.errorCode)
            is ApiError.Unknown -> error.message ?: context.getString(R.string.error_unknown)
        }
    }

    /**
     * Gets a user-friendly message for a throwable.
     * Parses the throwable to ApiError first, then gets the message.
     *
     * @param throwable The exception to get a message for
     * @return A localized, user-friendly error message
     */
    fun getMessage(throwable: Throwable): String {
        val apiError = ApiErrorParser.parse(throwable)
        return getMessage(apiError)
    }

    private fun getServerErrorMessage(errorCode: String): String {
        return when (errorCode) {
            // Authentication errors
            "AUTH_INVALID_CREDENTIALS" -> context.getString(R.string.error_auth_invalid_credentials)
            "AUTH_INVALID_TOKEN" -> context.getString(R.string.error_auth_invalid_token)
            "AUTH_TOKEN_EXPIRED" -> context.getString(R.string.error_auth_token_expired)
            "AUTH_INVALID_REFRESH_TOKEN" -> context.getString(R.string.error_auth_invalid_refresh_token)
            "AUTH_ACCOUNT_DEACTIVATED" -> context.getString(R.string.error_auth_account_deactivated)
            "AUTH_FORBIDDEN" -> context.getString(R.string.error_auth_forbidden)
            "AUTH_ADMIN_REQUIRED" -> context.getString(R.string.error_auth_admin_required)

            // User errors
            "USER_NOT_FOUND" -> context.getString(R.string.error_user_not_found)
            "USER_EMAIL_EXISTS" -> context.getString(R.string.error_user_email_exists)
            "USER_CANNOT_DELETE_SELF" -> context.getString(R.string.error_user_cannot_delete_self)

            // Invitation errors
            "INVITATION_INVALID_CODE" -> context.getString(R.string.error_invitation_invalid_code)
            "INVITATION_ALREADY_REDEEMED" -> context.getString(R.string.error_invitation_already_redeemed)
            "INVITATION_NOT_FOUND" -> context.getString(R.string.error_invitation_not_found)
            "INVITATION_CANNOT_DELETE_REDEEMED" -> context.getString(R.string.error_invitation_cannot_delete_redeemed)

            // Bench errors
            "BENCH_NOT_FOUND" -> context.getString(R.string.error_bench_not_found)
            "BENCH_FORBIDDEN" -> context.getString(R.string.error_bench_forbidden)

            // Photo errors
            "PHOTO_NOT_FOUND" -> context.getString(R.string.error_photo_not_found)
            "PHOTO_MAX_REACHED" -> context.getString(R.string.error_photo_max_reached)
            "PHOTO_FILE_TOO_LARGE" -> context.getString(R.string.error_photo_file_too_large)
            "PHOTO_INVALID_TYPE" -> context.getString(R.string.error_photo_invalid_type)
            "PHOTO_FORBIDDEN" -> context.getString(R.string.error_photo_forbidden)

            // Visit errors
            "VISIT_NOT_FOUND" -> context.getString(R.string.error_visit_not_found)
            "VISIT_FORBIDDEN" -> context.getString(R.string.error_visit_forbidden)

            // Favorite errors
            "FAVORITE_NOT_FOUND" -> context.getString(R.string.error_favorite_not_found)
            "FAVORITE_ALREADY_EXISTS" -> context.getString(R.string.error_favorite_already_exists)

            // Validation errors
            "VALIDATION_INVALID_REQUEST" -> context.getString(R.string.error_validation_invalid_request)
            "VALIDATION_INVALID_ID" -> context.getString(R.string.error_validation_invalid_id)
            "VALIDATION_INVALID_EMAIL" -> context.getString(R.string.error_validation_invalid_email)
            "VALIDATION_PASSWORD_TOO_SHORT" -> context.getString(R.string.error_validation_password_too_short)
            "VALIDATION_FIELD_REQUIRED" -> context.getString(R.string.error_validation_field_required)

            // System errors
            "SYSTEM_INTERNAL_ERROR" -> context.getString(R.string.error_system_internal)
            "SYSTEM_DATABASE_ERROR" -> context.getString(R.string.error_system_database)
            "SYSTEM_RATE_LIMITED" -> context.getString(R.string.error_system_rate_limited)

            // Fallback for unknown error codes
            else -> context.getString(R.string.error_unknown)
        }
    }
}
