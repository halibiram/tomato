package com.tomatomediacenter.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placeholders")
data class PlaceholderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
