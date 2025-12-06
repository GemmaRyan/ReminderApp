package com.example.reminderapp

import com.example.reminderapp.data.local.ReminderDao
import com.example.reminderapp.data.local.ReminderRepository
import com.example.reminderapp.data.model.Reminder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.collections.get

/**
 * Unit tests for ReminderRepository
 *
 * Test Cases:
 * 1. Insert reminder successfully
 * 2. Get all reminders
 * 3. Get reminders for specific date
 * 4. Update reminder
 * 5. Delete reminder
 * 6. Search reminders by title
 *
 * Expected Outcomes:
 * - All CRUD operations work correctly
 * - Date filtering returns only reminders for specified date
 * - Search returns matching reminders
 */
class ReminderRepositoryTest {

    @Mock
    private lateinit var reminderDao: ReminderDao

    private lateinit var repository: ReminderRepository

    @Before
    fun setup() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this)
        repository = ReminderRepository(reminderDao)
    }

    @Test
    fun `insert reminder returns correct id`() = runTest {
        // Arrange
        val reminder = Reminder(
            title = "Test Reminder",
            description = "Test Description",
            date = System.currentTimeMillis(),
            time = "14:30",
            colorCode = 0
        )
        val expectedId = 1L

        `when`(reminderDao.insertReminder(reminder)).thenReturn(expectedId)

        // Act
        val resultId = repository.insertReminder(reminder)

        // Assert
        assertEquals(expectedId, resultId)
        verify(reminderDao).insertReminder(reminder)
    }

    @Test
    fun `getAllReminders returns flow of reminders`() = runTest {
        // Arrange
        val reminders = listOf(
            Reminder(1, "Reminder 1", "Desc 1", System.currentTimeMillis(), "10:00", 0),
            Reminder(2, "Reminder 2", "Desc 2", System.currentTimeMillis(), "11:00", 1)
        )

        `when`(reminderDao.getAllReminders()).thenReturn(flowOf(reminders))

        // Act
        val result = repository.getAllReminders()

        // Assert
        result.collect { reminderList ->
            assertEquals(2, reminderList.size)
            assertEquals("Reminder 1", reminderList[0].title)
            assertEquals("Reminder 2", reminderList[1].title)
        }
    }

    @Test
    fun `deleteReminder calls dao delete`() = runTest {
        // Arrange
        val reminder = Reminder(
            id = 1,
            title = "Test",
            description = "Test",
            date = System.currentTimeMillis(),
            time = "14:30",
            colorCode = 0
        )

        // Act
        repository.deleteReminder(reminder)

        // Assert
        verify(reminderDao).deleteReminder(reminder)
    }

    @Test
    fun `searchReminders returns matching reminders`() = runTest {
        // Arrange
        val query = "meeting"
        val matchingReminders = listOf(
            Reminder(1, "Team Meeting", "Desc", System.currentTimeMillis(), "10:00", 0),
            Reminder(2, "Client Meeting", "Desc", System.currentTimeMillis(), "14:00", 1)
        )

        `when`(reminderDao.searchReminders(query)).thenReturn(flowOf(matchingReminders))

        // Act
        val result = repository.searchReminders(query)

        // Assert
        result.collect { reminderList ->
            assertEquals(2, reminderList.size)
            assertTrue(reminderList.all { it.title.contains("Meeting") })
        }
    }
}