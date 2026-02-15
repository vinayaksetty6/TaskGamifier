package com.example.taskgamifier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isCompleted: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis()
)

data class DailyTaskStats(
    val date: Long,
    val tasks: List<Task>,
    val completionRate: Double,
    val coinsEarned: Int
)

class TaskViewModel : ViewModel() {
    private val _todayTasks = MutableStateFlow<List<Task>>(emptyList())
    val todayTasks: StateFlow<List<Task>> = _todayTasks

    private val _totalCoins = MutableStateFlow(0)
    val totalCoins: StateFlow<Int> = _totalCoins

    private val _allTasksByDate = MutableStateFlow<Map<Long, DailyTaskStats>>(emptyMap())
    val allTasksByDate: StateFlow<Map<Long, DailyTaskStats>> = _allTasksByDate

    private val _reportData = MutableStateFlow<List<DailyTaskStats>>(emptyList())
    val reportData: StateFlow<List<DailyTaskStats>> = _reportData

    init {
        loadTasksForToday()
        recalculateTotalCoins()
    }

    fun addTask(taskName: String) {
        viewModelScope.launch {
            val currentDate = getDateKey(System.currentTimeMillis())
            val newTask = Task(name = taskName, dateCreated = currentDate)
            
            val currentTasks = _todayTasks.value.toMutableList()
            currentTasks.add(newTask)
            _todayTasks.emit(currentTasks)

            updateDailyStats(currentDate, currentTasks)
        }
    }

    fun editTask(task: Task, newName: String) {
        viewModelScope.launch {
            val currentTasks = _todayTasks.value.toMutableList()
            val index = currentTasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                currentTasks[index] = task.copy(name = newName)
                _todayTasks.emit(currentTasks)
                updateDailyStats(getDateKey(System.currentTimeMillis()), currentTasks)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val currentTasks = _todayTasks.value.toMutableList()
            currentTasks.removeAll { it.id == task.id }
            _todayTasks.emit(currentTasks)
            updateDailyStats(getDateKey(System.currentTimeMillis()), currentTasks)
            recalculateTotalCoins()
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val currentTasks = _todayTasks.value.toMutableList()
            val index = currentTasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                currentTasks[index] = task.copy(isCompleted = !task.isCompleted)
                _todayTasks.emit(currentTasks)
                updateDailyStats(getDateKey(System.currentTimeMillis()), currentTasks)
                recalculateTotalCoins()
            }
        }
    }

    private suspend fun loadTasksForToday() {
        val todayKey = getDateKey(System.currentTimeMillis())
        val stats = _allTasksByDate.value[todayKey]
        if (stats != null) {
            _todayTasks.emit(stats.tasks)
        } else {
            _todayTasks.emit(emptyList())
        }
    }

    private suspend fun updateDailyStats(dateKey: Long, tasks: List<Task>) {
        val completedCount = tasks.count { it.isCompleted }
        val totalCount = tasks.size
        val completionRate = if (totalCount > 0) {
            (completedCount.toDouble() / totalCount) * 100
        } else {
            0.0
        }

        val coinsEarned = calculateCoins(completionRate, completedCount)

        val stats = DailyTaskStats(
            date = dateKey,
            tasks = tasks,
            completionRate = completionRate,
            coinsEarned = coinsEarned
        )

        val updatedMap = _allTasksByDate.value.toMutableMap()
        updatedMap[dateKey] = stats
        _allTasksByDate.emit(updatedMap)
    }

    private fun calculateCoins(completionRate: Double, completedCount: Int): Int {
        return when {
            completionRate < 30 -> completedCount * 1
            completionRate < 60 -> completedCount * 2
            completionRate < 90 -> completedCount * 3
            else -> completedCount * 4
        }
    }

    private suspend fun recalculateTotalCoins() {
        val total = _allTasksByDate.value.values.sumOf { it.coinsEarned }
        _totalCoins.emit(total)
    }

    fun setSelectedReportMonth(monthDate: Long) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = monthDate
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startOfMonth = calendar.timeInMillis

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endOfMonth = calendar.timeInMillis

            val monthStats = _allTasksByDate.value.values.filter {
                it.date in startOfMonth..endOfMonth
            }.sortedBy { it.date }

            _reportData.emit(monthStats)
        }
    }

    private fun getDateKey(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
