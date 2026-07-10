package com.app.habitrabbit

import com.app.habitrabbit.data.local.HabitLog
import com.app.habitrabbit.data.repository.HabitRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class HabitRepositoryTest {

    private lateinit var dao: FakeHabitDao
    private lateinit var repository: HabitRepository

    @Before
    fun setup() {
        dao = FakeHabitDao()
        repository = HabitRepository(dao)
    }

    @Test
    fun `current streak counts consecutive days ending today`() = runTest {
        val habitId = 1L
        val today = LocalDate.now()

        // Simula 3 días consecutivos completados, incluyendo hoy
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(2).toString()))
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(1).toString()))
        dao.insertLog(HabitLog(habitId = habitId, date = today.toString()))

        val streak = repository.getCurrentStreak(habitId)
        assertEquals(3, streak)
    }

    @Test
    fun `current streak still counts if today not marked yet`() = runTest {
        val habitId = 1L
        val today = LocalDate.now()

        // Solo ayer y antes de ayer, hoy no se ha marcado todavía
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(2).toString()))
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(1).toString()))

        val streak = repository.getCurrentStreak(habitId)
        assertEquals(2, streak)
    }

    @Test
    fun `current streak resets if there is a gap`() = runTest {
        val habitId = 1L
        val today = LocalDate.now()

        // Hay un hueco de un día -> la racha actual debe ser solo el día de hoy
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(5).toString()))
        dao.insertLog(HabitLog(habitId = habitId, date = today.toString()))

        val streak = repository.getCurrentStreak(habitId)
        assertEquals(1, streak)
    }

    @Test
    fun `longest streak finds the best historical run`() = runTest {
        val habitId = 1L
        val today = LocalDate.now()

        // Racha vieja de 4 días, luego hueco, luego racha actual de 2 días
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(10).toString()))
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(9).toString()))
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(8).toString()))
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(7).toString()))
        dao.insertLog(HabitLog(habitId = habitId, date = today.minusDays(1).toString()))
        dao.insertLog(HabitLog(habitId = habitId, date = today.toString()))

        val longest = repository.getLongestStreak(habitId)
        assertEquals(4, longest)
    }
}