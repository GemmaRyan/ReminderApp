package com.example.reminderapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.ui.components.TopAppBarComponent
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit,
    onDateSelected: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    // For bottom sheet
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    // Reminders for selected date
    val remindersForDate by viewModel.remindersForSelectedDate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Pick a Date",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->

        // -----------------------
        // MAIN SCREEN CONTENT
        // -----------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            MonthYearSelector(
                currentMonth = currentMonth,
                onPreviousMonth = {
                    currentMonth = (currentMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                    }
                },
                onNextMonth = {
                    currentMonth = (currentMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { date -> selectedDate = date }
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    selectedDate?.let { date ->

                        // Load reminders for that day
                        viewModel.setSelectedDate(date.timeInMillis)

                        // Open sheet
                        showSheet = true
                        scope.launch { sheetState.show() }
                    }
                },
                enabled = selectedDate != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    "Find Reminders",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ----------------------------
    // BOTTOM SHEET SHOWING RESULTS
    // ----------------------------
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {

            Text(
                text = "Reminders for selected date",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            if (remindersForDate.isEmpty()) {

                Text(
                    text = "No reminders for this date",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    items(remindersForDate) { reminder ->
                        ReminderRow(reminder)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun ReminderRow(reminder: Reminder) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(reminder.title, fontWeight = FontWeight.Bold)
        Text(reminder.time, style = MaterialTheme.typography.bodyMedium)
        if (reminder.location.isNotBlank()) {
            Text("📍 ${reminder.location}")
        }
    }
}

@Composable
private fun MonthYearSelector(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
        }

        Text(monthYear, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Calendar?,
    onDateSelected: (Calendar) -> Unit
) {
    val days = getDaysInMonth(currentMonth)
    val today = Calendar.getInstance()

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { day ->
                if (day != null) {
                    DayCell(
                        day = day,
                        isSelected = selectedDate?.isSameDay(day) == true,
                        isToday = today.isSameDay(day),
                        onClick = { onDateSelected(day) }
                    )
                } else {
                    Box(Modifier.size(40.dp))
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Calendar,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val color = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(day.get(Calendar.DAY_OF_MONTH).toString(), color = color)
    }
}

private fun getDaysInMonth(month: Calendar): List<Calendar?> {
    val list = mutableListOf<Calendar?>()
    val cal = month.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val offset = cal.get(Calendar.DAY_OF_WEEK) - 1
    repeat(offset) { list.add(null) }

    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (d in 1..maxDay) {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, d)
        list.add(c)
    }
    return list
}

private fun Calendar.isSameDay(other: Calendar): Boolean {
    return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}
