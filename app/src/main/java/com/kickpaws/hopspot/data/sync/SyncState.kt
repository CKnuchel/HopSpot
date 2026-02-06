package com.kickpaws.hopspot.data.sync

sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data class Error(val message: String) : SyncState()
    data class Success(val lastSyncTime: Long) : SyncState()
}

data class SyncProgress(
    val state: SyncState = SyncState.Idle,
    val pendingSpotChanges: Int = 0,
    val pendingVisitChanges: Int = 0,
    val pendingPhotoUploads: Int = 0,
    val lastSyncTime: Long? = null
) {
    val totalPendingChanges: Int
        get() = pendingSpotChanges + pendingVisitChanges + pendingPhotoUploads

    val hasPendingChanges: Boolean
        get() = totalPendingChanges > 0
}
