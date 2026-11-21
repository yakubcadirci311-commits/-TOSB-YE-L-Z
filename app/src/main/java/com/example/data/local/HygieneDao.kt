package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HygieneDao {
    @Query("SELECT * FROM hygiene_reports ORDER BY createdAtMillis DESC")
    fun observeAllReports(): Flow<List<HygieneReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: HygieneReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<HygieneReportEntity>)

    @Query("UPDATE hygiene_reports SET isResolved = :resolved WHERE id = :reportId")
    suspend fun updateResolvedState(reportId: Long, resolved: Boolean)

    @Query("DELETE FROM hygiene_reports WHERE id = :reportId")
    suspend fun deleteReportById(reportId: Long)

    @Query("DELETE FROM hygiene_reports")
    suspend fun clearAllReports()

    @Query("SELECT * FROM eco_action_logs ORDER BY createdAtMillis DESC")
    fun observeAllEcoLogs(): Flow<List<EcoActionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEcoLog(log: EcoActionLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEcoLogs(logs: List<EcoActionLogEntity>)

    @Query("DELETE FROM eco_action_logs")
    suspend fun clearAllEcoLogs()
}
