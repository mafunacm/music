package com.musicplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentEntity(
    @PrimaryKey
    @ColumnInfo(name = "songPath")
    val songPath: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
