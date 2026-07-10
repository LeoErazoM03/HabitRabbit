package com.app.habitrabbit

import com.app.habitrabbit.data.local.Habit
import com.app.habitrabbit.data.local.HabitDao
import com.app.habitrabbit.data.local.HabitLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHabitDao : HabitDao {

    private val habits = mutableListOf<Habit>()
    private val logs = mutableListOf<HabitLog>()
    private val habitsFlow = MutableStateFlow<List<Habit>>(emptyList())

    override suspend fun insertHabit(habit: Habit): Long {
        habits.add(habit)
        habitsFlow.value = habits.toList()
        return habits.size.toLong()
    }

    override suspend fun updateHabit(habit: Habit) {}
    override suspend fun deleteHabit(habit: Habit) {}
    override fun getActiveHabits(): Flow<List<Habit>> = habitsFlow
    override fun getHabitById(habitId: Long): Flow<Habit?> = MutableStateFlow(habits.find { it.id == habitId })

    override suspend fun insertLog(log: HabitLog): Long {
        logs.removeAll { it.habitId == log.habitId && it.date == log.date }
        logs.add(log)
        return logs.size.toLong()
    }

    override suspend fun deleteLog(habitId: Long, date: String) {
        logs.removeAll { it.habitId == habitId && it.date == date }
    }

    override fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> =
        MutableStateFlow(logs.filter { it.habitId == habitId })

    override suspend fun getLogForDate(habitId: Long, date: String): HabitLog? =
        logs.find { it.habitId == habitId && it.date == date }

    override fun getLogsForDate(date: String): Flow<List<HabitLog>> =
        MutableStateFlow(logs.filter { it.date == date })

    override suspend fun getCompletedDatesForHabit(habitId: Long): List<String> =
        logs.filter { it.habitId == habitId && it.completed }
            .map { it.date }
            .sorted()
}