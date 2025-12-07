package com.example.reminderapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.reminderapp.ui.components.TopAppBarComponent
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
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

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Pick a Date",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
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

            Spacer(modifier = Modifier.height(16.dp))

            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = date
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedDate?.let { date ->
                        viewModel.setSelectedDate(date.timeInMillis)
                        onDateSelected()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedDate != null,
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Find Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Previous month"
            )
        }

        Text(
            text = monthYear,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next month"
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Calendar?,
    onDateSelected: (Calendar) -> Unit
) {
    val daysInMonth = getDaysInMonth(currentMonth)
    val today = Calendar.getInstance()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(daysInMonth) { day ->
                if (day != null) {
                    DayCell(
                        day = day,
                        isSelected = selectedDate?.isSameDay(day) == true,
                        isToday = today.isSameDay(day),
                        onClick = { onDateSelected(day) }
                    )
                } else {
                    Box(modifier = Modifier.size(40.dp))
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
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.get(Calendar.DAY_OF_MONTH).toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun getDaysInMonth(month: Calendar): List<Calendar?> {
    val days = mutableListOf<Calendar?>()
    val calendar = month.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    repeat(firstDayOfWeek) {
        days.add(null)
    }

    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (day in 1..maxDay) {
        val dayCal = calendar.clone() as Calendar
        dayCal.set(Calendar.DAY_OF_MONTH, day)
        days.add(dayCal)
    }

    return days
}

private fun Calendar.isSameDay(other: Calendar): Boolean {
    return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}
