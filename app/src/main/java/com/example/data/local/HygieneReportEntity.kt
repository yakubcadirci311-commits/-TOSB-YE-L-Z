package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hygiene_reports")
data class HygieneReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val areaName: String,
    val conditionName: String,
    val note: String,
    val isResolved: Boolean,
    val createdAtMillis: Long
)
