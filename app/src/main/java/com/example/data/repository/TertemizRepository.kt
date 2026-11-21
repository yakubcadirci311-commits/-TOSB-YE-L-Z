package com.example.data.repository

import com.example.data.local.EcoActionLogEntity
import com.example.data.local.HygieneDao
import com.example.data.local.HygieneReportEntity
import com.example.data.local.PreferencesManager
import com.example.data.model.CommonArea
import com.example.data.model.EcoActionType
import com.example.data.model.ReportCondition
import com.example.data.model.TertemizStaticData
import kotlinx.coroutines.flow.Flow

class TertemizRepository(
    private val dao: HygieneDao,
    private val preferencesManager: PreferencesManager
) {
    val reportsFlow: Flow<List<HygieneReportEntity>> = dao.observeAllReports()
    val ecoLogsFlow: Flow<List<EcoActionLogEntity>> = dao.observeAllEcoLogs()
    val checkedChecklistIdsFlow: Flow<Set<String>> = preferencesManager.checkedChecklistIds
    val selectedTipIndexFlow: Flow<Int> = preferencesManager.selectedTipIndex
    val unlockedBadgesFlow: Flow<Map<String, Long>> = preferencesManager.unlockedBadges

    // İlk açılışta ekranların boş görünmemesi için örnek başlangıç verisi ekler
    suspend fun seedInitialDataIfNeeded() {
        if (!preferencesManager.hasInitialized()) {
            insertSampleData()
            preferencesManager.markInitialized()
        }
    }

    suspend fun restoreSampleData() {
        dao.clearAllReports()
        dao.clearAllEcoLogs()
        insertSampleData()
        preferencesManager.markInitialized()
    }

    private suspend fun insertSampleData() {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 86_400_000L

        val sampleReports = listOf(
            HygieneReportEntity(
                areaName = CommonArea.FLOOR_1_RESTROOM.name,
                conditionName = ReportCondition.SUPPLY_MISSING.name,
                note = "Kız/erkek girişindeki sıvı sabunluk azalmış.",
                isResolved = false,
                createdAtMillis = now - 2 * hour
            ),
            HygieneReportEntity(
                areaName = CommonArea.FLOOR_2_RESTROOM.name,
                conditionName = ReportCondition.CLEAN.name,
                note = "Sabah kontrolünde temiz ve eksiksiz.",
                isResolved = true,
                createdAtMillis = now - 5 * hour
            ),
            HygieneReportEntity(
                areaName = CommonArea.CANTEEN.name,
                conditionName = ReportCondition.CLEANING_NEEDED.name,
                note = "Öğle arası sonrası masaların silinmesi gerekiyor.",
                isResolved = false,
                createdAtMillis = now - 1 * hour
            ),
            HygieneReportEntity(
                areaName = CommonArea.GARDEN_FOUNTAIN.name,
                conditionName = ReportCondition.WATER_ISSUE.name,
                note = "Sağ taraftaki musluk tam kapanmıyor, damlatıyor.",
                isResolved = true,
                createdAtMillis = now - 1 * day
            ),
            HygieneReportEntity(
                areaName = CommonArea.GROUND_COMMON.name,
                conditionName = ReportCondition.CLEAN.name,
                note = "Geri dönüşüm kutuları düzenli durumda.",
                isResolved = true,
                createdAtMillis = now - 2 * day
            )
        )

        val sampleLogs = listOf(
            EcoActionLogEntity(
                actionType = EcoActionType.HANDWASH_20S.name,
                litersSaved = EcoActionType.HANDWASH_20S.defaultLitersSaved,
                createdAtMillis = now - 3 * hour
            ),
            EcoActionLogEntity(
                actionType = EcoActionType.HANDWASH_20S.name,
                litersSaved = EcoActionType.HANDWASH_20S.defaultLitersSaved,
                createdAtMillis = now - 1 * hour
            ),
            EcoActionLogEntity(
                actionType = EcoActionType.WATER_SAVING.name,
                litersSaved = EcoActionType.WATER_SAVING.defaultLitersSaved,
                createdAtMillis = now - 4 * hour
            ),
            EcoActionLogEntity(
                actionType = EcoActionType.ZERO_WASTE.name,
                litersSaved = 0.0,
                createdAtMillis = now - 2 * hour
            ),
            EcoActionLogEntity(
                actionType = EcoActionType.HANDWASH_20S.name,
                litersSaved = EcoActionType.HANDWASH_20S.defaultLitersSaved,
                createdAtMillis = now - 1 * day
            ),
            EcoActionLogEntity(
                actionType = EcoActionType.WATER_SAVING.name,
                litersSaved = EcoActionType.WATER_SAVING.defaultLitersSaved,
                createdAtMillis = now - 2 * day
            ),
            EcoActionLogEntity(
                actionType = EcoActionType.ZERO_WASTE.name,
                litersSaved = 0.0,
                createdAtMillis = now - 3 * day
            )
        )

        // Başlangıç verileriyle kazanılan rozetlerin zaman damgalarını önce kaydeder
        preferencesManager.setCheckedChecklistIds(setOf("chk_tap_off", "chk_reusable_bottle"))
        preferencesManager.setUnlockedBadges(
            mapOf(
                "badge_first_report" to (now - 2 * day),
                "badge_five_reports" to (now - 1 * hour),
                "badge_problem_solver" to (now - 5 * hour)
            )
        )
        dao.insertReports(sampleReports)
        dao.insertEcoLogs(sampleLogs)
    }

    suspend fun addReport(
        area: CommonArea,
        condition: ReportCondition,
        note: String
    ) {
        // "Temiz / Sorun Yok" seçildiyse başlangıçta çözülmüş kabul edilir; sorun varsa "Açık" olarak kaydedilir
        val resolvedByDefault = !condition.needsAction
        dao.insertReport(
            HygieneReportEntity(
                areaName = area.name,
                conditionName = condition.name,
                note = note.trim(),
                isResolved = resolvedByDefault,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleReportResolution(reportId: Long, currentResolved: Boolean) {
        dao.updateResolvedState(reportId, !currentResolved)
    }

    suspend fun deleteReport(reportId: Long) {
        dao.deleteReportById(reportId)
    }

    suspend fun recordEcoAction(type: EcoActionType) {
        dao.insertEcoLog(
            EcoActionLogEntity(
                actionType = type.name,
                litersSaved = type.defaultLitersSaved,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleChecklistItem(id: String) {
        preferencesManager.toggleChecklistId(id)
    }

    suspend fun cycleDailyTip(currentIndex: Int) {
        val next = (currentIndex + 1) % TertemizStaticData.dailyTips.size
        preferencesManager.setSelectedTipIndex(next)
    }

    suspend fun recordUnlockedBadges(newEntries: Map<String, Long>) {
        preferencesManager.addUnlockedBadges(newEntries)
    }

    // Tüm yerel bildirimleri, sayaç geçmişini, rozetleri ve kontrol listesini sıfırlar
    suspend fun resetAllData() {
        dao.clearAllReports()
        dao.clearAllEcoLogs()
        preferencesManager.resetUserPreferences()
    }
}
