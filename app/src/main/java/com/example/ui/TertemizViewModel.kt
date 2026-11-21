package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.EcoActionLogEntity
import com.example.data.local.HygieneReportEntity
import com.example.data.local.PreferencesManager
import com.example.data.model.CommonArea
import com.example.data.model.DailyTip
import com.example.data.model.DigitalBadgeDef
import com.example.data.model.EcoActionType
import com.example.data.model.EcoRank
import com.example.data.model.HandwashStep
import com.example.data.model.OverallHealthLevel
import com.example.data.model.ReportCondition
import com.example.data.model.TertemizStaticData
import com.example.data.repository.TertemizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class ReportFilter(val label: String) {
    ALL("Tümü"),
    OPEN("Açık"),
    RESOLVED("Çözüldü")
}

enum class BadgeFilter(val label: String) {
    ALL("Tümü"),
    UNLOCKED("Kazanılan"),
    IN_PROGRESS("Hedefler")
}

data class HygieneReportUi(
    val id: Long,
    val area: CommonArea,
    val condition: ReportCondition,
    val note: String,
    val isResolved: Boolean,
    val formattedTime: String,
    val createdAtMillis: Long
)

data class AreaStatusSummary(
    val area: CommonArea,
    val latestCondition: ReportCondition?,
    val hasOpenIssue: Boolean,
    val openCount: Int
)

data class PeriodEcoStats(
    val handwashRounds: Int = 0,
    val waterSavingActions: Int = 0,
    val zeroWasteActions: Int = 0,
    val litersSaved: Double = 0.0
)

data class DigitalBadgeUi(
    val definition: DigitalBadgeDef,
    val currentValue: Int,
    val isUnlocked: Boolean,
    val unlockedAtFormatted: String?,
    val progressFraction: Float
)

private data class PersistentSnapshot(
    val reports: List<HygieneReportEntity>,
    val ecoLogs: List<EcoActionLogEntity>,
    val checkedIds: Set<String>,
    val tipIdx: Int,
    val unlockedBadges: Map<String, Long>
)

private data class FormAndTimerState(
    val selectedArea: CommonArea = CommonArea.FLOOR_1_RESTROOM,
    val selectedCondition: ReportCondition = ReportCondition.SUPPLY_MISSING,
    val noteInput: String = "",
    val reportFilter: ReportFilter = ReportFilter.ALL,
    val badgeFilter: BadgeFilter = BadgeFilter.ALL,
    val timerSecondsRemaining: Int = 20,
    val isTimerRunning: Boolean = false,
    val isTimerCompleted: Boolean = false
)

data class TertemizUiState(
    val reports: List<HygieneReportUi> = emptyList(),
    val filteredReports: List<HygieneReportUi> = emptyList(),
    val reportFilter: ReportFilter = ReportFilter.ALL,
    val selectedArea: CommonArea = CommonArea.FLOOR_1_RESTROOM,
    val selectedCondition: ReportCondition = ReportCondition.SUPPLY_MISSING,
    val noteInput: String = "",
    val totalReportsCount: Int = 0,
    val openReportsCount: Int = 0,
    val resolvedReportsCount: Int = 0,
    val overallHealthLevel: OverallHealthLevel = OverallHealthLevel.CLEAN,
    val areaSummaries: List<AreaStatusSummary> = emptyList(),
    val totalHandwashRounds: Int = 0,
    val totalWaterSavingActions: Int = 0,
    val totalZeroWasteActions: Int = 0,
    val totalLitersSaved: Double = 0.0,
    val todayStats: PeriodEcoStats = PeriodEcoStats(),
    val weeklyStats: PeriodEcoStats = PeriodEcoStats(),
    val checkedChecklistIds: Set<String> = emptySet(),
    val currentTip: DailyTip = TertemizStaticData.dailyTips.first(),
    val tipIndex: Int = 0,
    val timerSecondsRemaining: Int = 20,
    val isTimerRunning: Boolean = false,
    val isTimerCompleted: Boolean = false,
    val currentHandwashStep: HandwashStep = TertemizStaticData.handwashSteps.first(),
    val badges: List<DigitalBadgeUi> = emptyList(),
    val filteredBadges: List<DigitalBadgeUi> = emptyList(),
    val badgeFilter: BadgeFilter = BadgeFilter.ALL,
    val unlockedBadgesCount: Int = 0,
    val totalBadgesCount: Int = TertemizStaticData.digitalBadges.size,
    val ecoRank: EcoRank = TertemizStaticData.getEcoRank(0)
)

class TertemizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TertemizRepository

    init {
        val db = AppDatabase.getInstance(application)
        val prefs = PreferencesManager(application)
        repository = TertemizRepository(db.hygieneDao(), prefs)
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    private val formAndTimer = MutableStateFlow(FormAndTimerState())
    private var timerJob: Job? = null
    private val notifiedNewBadges = mutableSetOf<String>()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 6)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private val persistentFlow = combine(
        repository.reportsFlow,
        repository.ecoLogsFlow,
        repository.checkedChecklistIdsFlow,
        repository.selectedTipIndexFlow,
        repository.unlockedBadgesFlow
    ) { reports, ecoLogs, checkedIds, tipIdx, unlockedBadges ->
        PersistentSnapshot(
            reports = reports,
            ecoLogs = ecoLogs,
            checkedIds = checkedIds,
            tipIdx = tipIdx,
            unlockedBadges = unlockedBadges
        )
    }

    val uiState: StateFlow<TertemizUiState> = combine(
        persistentFlow,
        formAndTimer
    ) { snapshot, local ->
        buildUiState(snapshot, local)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TertemizUiState()
    )

    private fun buildUiState(
        snapshot: PersistentSnapshot,
        local: FormAndTimerState
    ): TertemizUiState {
        val reportsUi = snapshot.reports.map { entity ->
            HygieneReportUi(
                id = entity.id,
                area = CommonArea.fromName(entity.areaName),
                condition = ReportCondition.fromName(entity.conditionName),
                note = entity.note,
                isResolved = entity.isResolved,
                formattedTime = formatTimestamp(entity.createdAtMillis),
                createdAtMillis = entity.createdAtMillis
            )
        }

        val openReports = reportsUi.filter { !it.isResolved }
        val resolvedReportsCount = reportsUi.size - openReports.size
        val overallLevel = when {
            openReports.any {
                it.condition == ReportCondition.CLEANING_NEEDED ||
                    it.condition == ReportCondition.WATER_ISSUE
            } -> OverallHealthLevel.ACTION_NEEDED

            openReports.any {
                it.condition == ReportCondition.SUPPLY_MISSING
            } -> OverallHealthLevel.SUPPLY_NEEDED

            else -> OverallHealthLevel.CLEAN
        }

        val areaSummaries = CommonArea.entries.map { area ->
            val areaReports = reportsUi.filter { it.area == area }
            val openForArea = areaReports.filter { !it.isResolved }
            val latest = openForArea.firstOrNull() ?: areaReports.firstOrNull()
            AreaStatusSummary(
                area = area,
                latestCondition = latest?.condition,
                hasOpenIssue = openForArea.isNotEmpty(),
                openCount = openForArea.size
            )
        }

        val filtered = when (local.reportFilter) {
            ReportFilter.ALL -> reportsUi
            ReportFilter.OPEN -> reportsUi.filter { !it.isResolved }
            ReportFilter.RESOLVED -> reportsUi.filter { it.isResolved }
        }

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val sevenDaysAgo = startOfToday - 6L * 86_400_000L

        val todayLogs = snapshot.ecoLogs.filter { it.createdAtMillis >= startOfToday }
        val weeklyLogs = snapshot.ecoLogs.filter { it.createdAtMillis >= sevenDaysAgo }
        val todayStats = computePeriodStats(todayLogs)
        val weeklyStats = computePeriodStats(weeklyLogs)

        val totalHandwashRounds = snapshot.ecoLogs.count { it.actionType == EcoActionType.HANDWASH_20S.name }
        val totalWaterSavingActions = snapshot.ecoLogs.count { it.actionType == EcoActionType.WATER_SAVING.name }
        val totalZeroWasteActions = snapshot.ecoLogs.count { it.actionType == EcoActionType.ZERO_WASTE.name }
        val totalLitersSaved = snapshot.ecoLogs.sumOf { it.litersSaved }

        // Her rozetin ilerleme değerini yerel verilerden hesaplar ve yeni açılan rozetleri kalıcı kaydeder
        val newlyUnlocked = mutableMapOf<String, Long>()
        val nowMillis = System.currentTimeMillis()

        val badgesUi = TertemizStaticData.digitalBadges.map { def ->
            val rawProgress = when (def.id) {
                "badge_first_report" -> reportsUi.size
                "badge_five_reports" -> reportsUi.size
                "badge_problem_solver" -> resolvedReportsCount
                "badge_handwash_master" -> totalHandwashRounds
                "badge_week_water_saver" -> weeklyStats.handwashRounds + weeklyStats.waterSavingActions
                "badge_water_guardian" -> totalLitersSaved.roundToInt()
                "badge_zero_waste_ambassador" -> totalZeroWasteActions
                "badge_full_checklist" -> snapshot.checkedIds.size
                else -> 0
            }

            val savedTs = snapshot.unlockedBadges[def.id]
            val unlockedNow = rawProgress >= def.targetValue
            val isUnlocked = savedTs != null || unlockedNow

            if (unlockedNow && savedTs == null) {
                newlyUnlocked[def.id] = nowMillis
            }

            val displayValue = if (isUnlocked) {
                maxOf(rawProgress, def.targetValue)
            } else {
                rawProgress.coerceAtMost(def.targetValue)
            }

            val effectiveTs = savedTs ?: if (unlockedNow) nowMillis else null
            DigitalBadgeUi(
                definition = def,
                currentValue = displayValue,
                isUnlocked = isUnlocked,
                unlockedAtFormatted = effectiveTs?.let { formatTimestamp(it) },
                progressFraction = (displayValue.toFloat() / def.targetValue.toFloat()).coerceIn(0f, 1f)
            )
        }

        if (newlyUnlocked.isNotEmpty()) {
            val toNotify = newlyUnlocked.keys.filter { it !in notifiedNewBadges }
            notifiedNewBadges.addAll(toNotify)
            viewModelScope.launch {
                repository.recordUnlockedBadges(newlyUnlocked)
                toNotify.firstOrNull()?.let { firstId ->
                    val badgeTitle = TertemizStaticData.digitalBadges
                        .firstOrNull { it.id == firstId }?.title
                    if (badgeTitle != null) {
                        _snackbarMessages.tryEmit("Yeni rozet kazandın: $badgeTitle!")
                    }
                }
            }
        }

        val filteredBadges = when (local.badgeFilter) {
            BadgeFilter.ALL -> badgesUi
            BadgeFilter.UNLOCKED -> badgesUi.filter { it.isUnlocked }
            BadgeFilter.IN_PROGRESS -> badgesUi.filter { !it.isUnlocked }
        }
        val unlockedCount = badgesUi.count { it.isUnlocked }
        val safeTipIndex = snapshot.tipIdx.mod(TertemizStaticData.dailyTips.size)

        return TertemizUiState(
            reports = reportsUi,
            filteredReports = filtered,
            reportFilter = local.reportFilter,
            selectedArea = local.selectedArea,
            selectedCondition = local.selectedCondition,
            noteInput = local.noteInput,
            totalReportsCount = reportsUi.size,
            openReportsCount = openReports.size,
            resolvedReportsCount = resolvedReportsCount,
            overallHealthLevel = overallLevel,
            areaSummaries = areaSummaries,
            totalHandwashRounds = totalHandwashRounds,
            totalWaterSavingActions = totalWaterSavingActions,
            totalZeroWasteActions = totalZeroWasteActions,
            totalLitersSaved = totalLitersSaved,
            todayStats = todayStats,
            weeklyStats = weeklyStats,
            checkedChecklistIds = snapshot.checkedIds,
            currentTip = TertemizStaticData.dailyTips[safeTipIndex],
            tipIndex = safeTipIndex,
            timerSecondsRemaining = local.timerSecondsRemaining,
            isTimerRunning = local.isTimerRunning,
            isTimerCompleted = local.isTimerCompleted,
            currentHandwashStep = TertemizStaticData.getStepForRemainingSeconds(local.timerSecondsRemaining),
            badges = badgesUi,
            filteredBadges = filteredBadges,
            badgeFilter = local.badgeFilter,
            unlockedBadgesCount = unlockedCount,
            totalBadgesCount = badgesUi.size,
            ecoRank = TertemizStaticData.getEcoRank(unlockedCount)
        )
    }

    private fun computePeriodStats(logs: List<EcoActionLogEntity>): PeriodEcoStats {
        return PeriodEcoStats(
            handwashRounds = logs.count { it.actionType == EcoActionType.HANDWASH_20S.name },
            waterSavingActions = logs.count { it.actionType == EcoActionType.WATER_SAVING.name },
            zeroWasteActions = logs.count { it.actionType == EcoActionType.ZERO_WASTE.name },
            litersSaved = logs.sumOf { it.litersSaved }
        )
    }

    // Zaman damgasını okunaklı Türkçe saat/tarih formatına çevirir
    private fun formatTimestamp(millis: Long): String {
        val nowCal = Calendar.getInstance()
        val targetCal = Calendar.getInstance().apply { timeInMillis = millis }
        val timeFmt = SimpleDateFormat("HH:mm", Locale.forLanguageTag("tr-TR"))
        val dateFmt = SimpleDateFormat("d MMM HH:mm", Locale.forLanguageTag("tr-TR"))

        return if (
            nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
            nowCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
        ) {
            "Bugün ${timeFmt.format(Date(millis))}"
        } else {
            dateFmt.format(Date(millis))
        }
    }

    fun selectArea(area: CommonArea) {
        formAndTimer.update { it.copy(selectedArea = area) }
    }

    fun selectCondition(condition: ReportCondition) {
        formAndTimer.update { it.copy(selectedCondition = condition) }
    }

    fun updateNoteInput(note: String) {
        formAndTimer.update { it.copy(noteInput = note.take(160)) }
    }

    fun setReportFilter(filter: ReportFilter) {
        formAndTimer.update { it.copy(reportFilter = filter) }
    }

    fun setBadgeFilter(filter: BadgeFilter) {
        formAndTimer.update { it.copy(badgeFilter = filter) }
    }

    fun submitReport() {
        val snapshot = formAndTimer.value
        viewModelScope.launch {
            repository.addReport(
                area = snapshot.selectedArea,
                condition = snapshot.selectedCondition,
                note = snapshot.noteInput
            )
            formAndTimer.update { it.copy(noteInput = "") }
            _snackbarMessages.tryEmit("${snapshot.selectedArea.displayName} bildirimi kaydedildi.")
        }
    }

    fun toggleReportResolved(report: HygieneReportUi) {
        viewModelScope.launch {
            repository.toggleReportResolution(report.id, report.isResolved)
            val newStateText = if (!report.isResolved) "Çözüldü" else "Açık"
            _snackbarMessages.tryEmit("Bildirim durumu \"$newStateText\" olarak güncellendi.")
        }
    }

    fun deleteReport(reportId: Long) {
        viewModelScope.launch {
            repository.deleteReport(reportId)
            _snackbarMessages.tryEmit("Bildirim listeden kaldırıldı.")
        }
    }

    // 20 saniyelik el yıkama sayacını başlatır ve her saniye adımı günceller
    fun startHandwashTimer(restartIfCompleted: Boolean = false) {
        val current = formAndTimer.value
        if (current.isTimerRunning) return

        if (current.isTimerCompleted || current.timerSecondsRemaining == 0 || restartIfCompleted) {
            if (current.isTimerCompleted || current.timerSecondsRemaining == 0) {
                formAndTimer.update {
                    it.copy(
                        timerSecondsRemaining = 20,
                        isTimerCompleted = false
                    )
                }
            }
        }

        formAndTimer.update { it.copy(isTimerRunning = true, isTimerCompleted = false) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (formAndTimer.value.timerSecondsRemaining > 0 && formAndTimer.value.isTimerRunning) {
                delay(1000L)
                if (!formAndTimer.value.isTimerRunning) break
                val next = (formAndTimer.value.timerSecondsRemaining - 1).coerceAtLeast(0)
                if (next == 0) {
                    formAndTimer.update {
                        it.copy(
                            timerSecondsRemaining = 0,
                            isTimerRunning = false,
                            isTimerCompleted = true
                        )
                    }
                    repository.recordEcoAction(EcoActionType.HANDWASH_20S)
                    _snackbarMessages.tryEmit("Tebrikler! 20 sn el yıkama tamamlandı (+2,5 L su tasarrufu).")
                } else {
                    formAndTimer.update { it.copy(timerSecondsRemaining = next) }
                }
            }
        }
    }

    fun pauseHandwashTimer() {
        timerJob?.cancel()
        formAndTimer.update { it.copy(isTimerRunning = false) }
    }

    fun resetHandwashTimer() {
        timerJob?.cancel()
        formAndTimer.update {
            it.copy(
                timerSecondsRemaining = 20,
                isTimerRunning = false,
                isTimerCompleted = false
            )
        }
    }

    fun logWaterSaving() {
        viewModelScope.launch {
            repository.recordEcoAction(EcoActionType.WATER_SAVING)
            _snackbarMessages.tryEmit("Su tasarrufu aksiyonu eklendi (+3,0 L).")
        }
    }

    fun logZeroWaste() {
        viewModelScope.launch {
            repository.recordEcoAction(EcoActionType.ZERO_WASTE)
            _snackbarMessages.tryEmit("Sıfır atık aksiyonu kaydedildi.")
        }
    }

    fun toggleChecklistItem(id: String) {
        viewModelScope.launch {
            repository.toggleChecklistItem(id)
        }
    }

    fun nextDailyTip() {
        viewModelScope.launch {
            repository.cycleDailyTip(uiState.value.tipIndex)
        }
    }

    fun resetAllData() {
        timerJob?.cancel()
        notifiedNewBadges.clear()
        formAndTimer.value = FormAndTimerState()
        viewModelScope.launch {
            repository.resetAllData()
            _snackbarMessages.tryEmit("Tüm bildirimler, rozetler ve sayaçlar sıfırlandı.")
        }
    }

    fun restoreSampleData() {
        notifiedNewBadges.clear()
        viewModelScope.launch {
            repository.restoreSampleData()
            _snackbarMessages.tryEmit("Örnek başlangıç verileri yüklendi.")
        }
    }
}
