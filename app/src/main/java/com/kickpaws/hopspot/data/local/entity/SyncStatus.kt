package com.kickpaws.hopspot.data.local.entity

enum class SyncStatus {
    SYNCED,           // Entspricht Server
    PENDING_CREATE,   // Offline erstellt
    PENDING_UPDATE,   // Offline bearbeitet
    PENDING_DELETE    // Zum Loeschen markiert
}
