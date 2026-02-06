package com.kickpaws.hopspot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kickpaws.hopspot.data.local.dao.PendingPhotoDao
import com.kickpaws.hopspot.data.local.dao.SpotDao
import com.kickpaws.hopspot.data.local.dao.UserDao
import com.kickpaws.hopspot.data.local.dao.VisitDao
import com.kickpaws.hopspot.data.local.entity.PendingPhotoEntity
import com.kickpaws.hopspot.data.local.entity.SpotEntity
import com.kickpaws.hopspot.data.local.entity.UserEntity
import com.kickpaws.hopspot.data.local.entity.VisitEntity

@Database(
    entities = [
        SpotEntity::class,
        VisitEntity::class,
        UserEntity::class,
        PendingPhotoEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HopSpotDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao
    abstract fun visitDao(): VisitDao
    abstract fun userDao(): UserDao
    abstract fun pendingPhotoDao(): PendingPhotoDao
}
