package com.app.habitrabbit.data.repository

import com.app.habitrabbit.data.local.Habit
import com.app.habitrabbit.data.local.HabitDao
import com.app.habitrabbit.data.local.HabitLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(private val dao: HabitDao) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // "yyyy-MM-dd"

    fun getActiveHabits(): Flow<List<Habit>> = dao.getActiveHabits()

    fun getHabitById(habitId: Long): Flow<Habit?> = dao.getHabitById(habitId)

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> = dao.getLogsForHabit(habitId)

    suspend fun addHabit(habit: Habit): Long = dao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)

    // Marca o desmarca el cumplimiento de HOY (o de una fecha específica)
    suspend fun toggleCompletion(habitId: Long, date: LocalDate = LocalDate.now()) {
        val dateStr = date.format(dateFormatter)
        val existing = dao.getLogForDate(habitId, dateStr)
        if (existing != null) {
            dao.deleteLog(habitId, dateStr) // ya estaba marcado -> lo desmarca
        } else {
            dao.insertLog(HabitLog(habitId = habitId, date = dateStr, completed = true))
        }
    }

    suspend fun isCompletedOnDate(habitId: Long, date: LocalDate = LocalDate.now()): Boolean {
        val dateStr = date.format(dateFormatter)
        return dao.getLogForDate(habitId, dateStr) != null
    }

    // ---- Cálculo de racha actual ----
    suspend fun getCurrentStreak(habitId: Long): Int {
        val completedDates = dao.getCompletedDatesForHabit(habitId)
            .map { LocalDate.parse(it, dateFormatter) }
            .toSet()

        if (completedDates.isEmpty()) return 0

        var streak = 0
        var day = LocalDate.now()

        // Si hoy no está completado, la racha "actual" empieza a contar desde ayer
        // (para no romper la racha si el usuario aún no marcó el día de hoy)
        if (day !in completedDates) {
            day = day.minusDays(1)
        }

        while (day in completedDates) {
            streak++
            day = day.minusDays(1)
        }

        return streak
    }

    // ---- Racha más larga histórica ----
    suspend fun getLongestStreak(habitId: Long): Int {
        val completedDates = dao.getCompletedDatesForHabit(habitId)
            .map { LocalDate.parse(it, dateFormatter) }
            .sorted()

        if (completedDates.isEmpty()) return 0

        var longest = 1
        var current = 1

        for (i in 1 until completedDates.size) {
            val diff = java.time.temporal.ChronoUnit.DAYS.between(completedDates[i - 1], completedDates[i])
            current = if (diff == 1L) current + 1 else 1
            if (current > longest) longest = current
        }

        return longest
    }
}