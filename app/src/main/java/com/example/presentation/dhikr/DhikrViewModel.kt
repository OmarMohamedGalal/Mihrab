package com.example.presentation.dhikr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.AppContainer
import com.example.data.azkar.AzkarRepository
import com.example.data.local.CustomDhikrEntity
import com.example.data.local.TasbeehStatsEntity
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DhikrUiState(
    val prefs: UserPreferences = UserPreferences(),
    val customDhikrs: List<CustomDhikrEntity> = emptyList(),
    val todayTotal: Int = 0,
    val activeDhikrArabic: String = AzkarRepository.tasbeehPresets.first().arabicText,
    val activeDhikrMeaning: String = AzkarRepository.tasbeehPresets.first().transliteration,
    val activeTargetCount: Int = AzkarRepository.tasbeehPresets.first().targetCount,
    val currentCount: Int = 0,
    val azkarProgressMap: Map<String, Int> = emptyMap()
)

class DhikrViewModel(private val container: AppContainer) : ViewModel() {

    private val activeArabic = MutableStateFlow(AzkarRepository.tasbeehPresets.first().arabicText)
    private val activeMeaning = MutableStateFlow(AzkarRepository.tasbeehPresets.first().transliteration)
    private val activeTarget = MutableStateFlow(AzkarRepository.tasbeehPresets.first().targetCount)
    private val currentCount = MutableStateFlow(0)
    private val azkarProgress = MutableStateFlow<Map<String, Int>>(emptyMap())

    val uiState: StateFlow<DhikrUiState> = combine(
        container.preferencesDataStore.preferencesFlow,
        container.azkarRepository.observeCustomDhikrs(),
        container.azkarRepository.observeTodayStats(),
        activeArabic,
        activeMeaning,
        activeTarget,
        currentCount,
        azkarProgress
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val stats = values[2] as TasbeehStatsEntity?
        DhikrUiState(
            prefs = values[0] as UserPreferences,
            customDhikrs = values[1] as List<CustomDhikrEntity>,
            todayTotal = stats?.totalCount ?: 0,
            activeDhikrArabic = values[3] as String,
            activeDhikrMeaning = values[4] as String,
            activeTargetCount = values[5] as Int,
            currentCount = values[6] as Int,
            azkarProgressMap = values[7] as Map<String, Int>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DhikrUiState()
    )

    fun selectPreset(arabic: String, meaning: String, target: Int) {
        activeArabic.value = arabic
        activeMeaning.value = meaning
        activeTarget.value = target
        currentCount.value = 0
    }

    fun incrementTasbeeh() {
        val nextCurrent = currentCount.value + 1
        currentCount.value = if (nextCurrent > activeTarget.value) 1 else nextCurrent
        val newToday = uiState.value.todayTotal + 1
        viewModelScope.launch {
            container.azkarRepository.recordTodayTasbeehTotal(newToday)
        }
    }

    fun resetTasbeeh() {
        currentCount.value = 0
    }

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch {
            container.preferencesDataStore.updateTasbeehGoal(goal)
        }
    }

    fun addCustomDhikr(arabic: String, meaning: String, target: Int) {
        if (arabic.isBlank()) return
        viewModelScope.launch {
            container.azkarRepository.saveCustomDhikr(arabic, meaning, target)
            selectPreset(arabic.trim(), meaning.trim(), target.coerceAtLeast(1))
        }
    }

    fun deleteCustomDhikr(item: CustomDhikrEntity) {
        viewModelScope.launch {
            container.azkarRepository.deleteCustomDhikr(item)
        }
    }

    fun tapAzkarItem(id: String, target: Int) {
        val currentMap = azkarProgress.value.toMutableMap()
        val prev = currentMap[id] ?: 0
        if (prev < target) {
            currentMap[id] = prev + 1
            azkarProgress.value = currentMap
            val newToday = uiState.value.todayTotal + 1
            viewModelScope.launch {
                container.azkarRepository.recordTodayTasbeehTotal(newToday)
            }
        }
    }

    fun resetAzkarCategory(ids: List<String>) {
        val currentMap = azkarProgress.value.toMutableMap()
        ids.forEach { currentMap.remove(it) }
        azkarProgress.value = currentMap
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DhikrViewModel(container) as T
        }
    }
}
