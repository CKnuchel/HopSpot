package com.kickpaws.hopspot.data.remote.api

import com.kickpaws.hopspot.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface HopSpotApi {

    // Auth Endpoints
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponseDto

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    )

    @POST("api/v1/auth/refresh-fcm-token")
    suspend fun refreshFcmToken(
        @Body request: RefreshFCMTokenRequest
    )

    // User Endpoints
    @GET("api/v1/users/me")
    suspend fun getMe(): UserDto

    @PATCH("api/v1/users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): UserDto

    @POST("api/v1/users/me/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    )

    // Spot Endpoints
    @GET("api/v1/spots")
    suspend fun getSpots(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("has_toilet") hasToilet: Boolean? = null,
        @Query("has_trash_bin") hasTrashBin: Boolean? = null,
        @Query("min_rating") minRating: Int? = null,
        @Query("search") search: String? = null,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("radius") radius: Int? = null
    ): ApiResponse<PaginatedSpotsDto>

    @GET("api/v1/spots/{id}")
    suspend fun getSpot(
        @Path("id") id: Int
    ): ApiResponse<SpotDto>

    @GET("api/v1/spots/random")
    suspend fun getRandomSpot(): ApiResponse<SpotDto>

    // Favorite Endpoints
    @GET("api/v1/spots/{id}/favorite")
    suspend fun checkFavorite(
        @Path("id") spotId: Int
    ): Map<String, Boolean>

    @POST("api/v1/spots/{id}/favorite")
    suspend fun addFavorite(
        @Path("id") spotId: Int
    ): Map<String, Boolean>

    @DELETE("api/v1/spots/{id}/favorite")
    suspend fun removeFavorite(
        @Path("id") spotId: Int
    ): Map<String, Boolean>

    @GET("api/v1/favorites")
    suspend fun getFavorites(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): PaginatedFavoritesDto

    @POST("api/v1/spots")
    suspend fun createSpot(
        @Body request: CreateSpotRequest
    ): ApiResponse<SpotDto>

    @PATCH("api/v1/spots/{id}")
    suspend fun updateSpot(
        @Path("id") id: Int,
        @Body request: UpdateSpotRequest
    ): ApiResponse<SpotDto>

    @DELETE("api/v1/spots/{id}")
    suspend fun deleteSpot(
        @Path("id") id: Int
    )

    // Photo Endpoints
    @GET("api/v1/spots/{id}/photos")
    suspend fun getPhotos(
        @Path("id") spotId: Int
    ): List<PhotoDto>

    @Multipart
    @POST("api/v1/spots/{id}/photos")
    suspend fun uploadPhoto(
        @Path("id") spotId: Int,
        @Part photo: MultipartBody.Part,
        @Part("is_main") isMain: RequestBody
    ): PhotoDto

    @DELETE("api/v1/photos/{id}")
    suspend fun deletePhoto(
        @Path("id") photoId: Int
    )

    @PATCH("api/v1/photos/{id}/main")
    suspend fun setMainPhoto(
        @Path("id") photoId: Int
    )

    @GET("api/v1/photos/{id}/url")
    suspend fun getPhotoUrl(
        @Path("id") photoId: Int,
        @Query("size") size: String? = null
    ): Map<String, String>

    // Visit Endpoints
    @GET("api/v1/visits")
    suspend fun getVisits(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("spot_id") spotId: Int? = null,
        @Query("sort_order") sortOrder: String? = null
    ): PaginatedVisitsDto

    @POST("api/v1/visits")
    suspend fun createVisit(
        @Body request: CreateVisitRequest
    ): VisitDto

    @DELETE("api/v1/visits/{id}")
    suspend fun deleteVisit(
        @Path("id") visitId: Int
    )

    @GET("api/v1/spots/{id}/visits/count")
    suspend fun getVisitCount(
        @Path("id") spotId: Int
    ): VisitCountDto

    // Weather Endpoints
    @GET("api/v1/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): WeatherDto

    // Activity Endpoints
    @GET("api/v1/activities")
    suspend fun getActivities(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("action_type") actionType: String? = null
    ): PaginatedActivitiesDto

    // Admin Endpoints
    @GET("api/v1/admin/users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("is_active") isActive: Boolean? = null,
        @Query("role") role: String? = null,
        @Query("search") search: String? = null
    ): PaginatedUsersDto

    @PATCH("api/v1/admin/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: AdminUpdateUserRequest
    ): UserDto

    @DELETE("api/v1/admin/users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int
    )

    @GET("api/v1/admin/invitation-codes")
    suspend fun getInvitationCodes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("is_redeemed") isRedeemed: Boolean? = null
    ): PaginatedInvitationCodesDto

    @POST("api/v1/admin/invitation-codes")
    suspend fun createInvitationCode(
        @Body request: CreateInvitationCodeRequest
    ): InvitationCodeDto

    @DELETE("api/v1/admin/invitation-codes/{id}")
    suspend fun deleteInvitationCode(@Path("id") id: Int): Response<Unit>
}