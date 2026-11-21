package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eco_action_logs")
data class EcoActionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionType: String,
    val litersSaved: Double,
    val createdAtMillis: Long
)
