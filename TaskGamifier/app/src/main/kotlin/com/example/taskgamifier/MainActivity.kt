package com.example.taskgamifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskgamifier.ui.theme.TaskGamifierTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskGamifierTheme {
                TaskGamifierApp()
            }
        }
    }
}

@Composable
fun TaskGamifierApp(
    viewModel: TaskViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.TaskList) }
    val totalCoins by viewModel.totalCoins.collectAsState()

    Scaffold(
        topBar = {
            CoinTopBar(totalCoins = totalCoins)
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Edit, "Tasks") },
                    label = { Text("Tasks") },
                    selected = currentScreen == Screen.TaskList,
                    onClick = { currentScreen = Screen.TaskList }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, "Report") },
                    label = { Text("Report") },
                    selected = currentScreen == Screen.Report,
                    onClick = { currentScreen = Screen.Report }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when (currentScreen) {
                Screen.TaskList -> TaskListScreen(viewModel)
                Screen.Report -> ReportScreen(viewModel)
            }
        }
    }
}

@Composable
fun CoinTopBar(totalCoins: Int) {
    TopAppBar(
        title = {
            Text(
                "Task Gamifier",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .background(
                        color = Color(0xFFFFD700),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "💰",
                    fontSize = 18.sp
                )
                Text(
                    totalCoins.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF6200EE)
        )
    )
}

@Composable
fun TaskListScreen(viewModel: TaskViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit: Task? by remember { mutableStateOf(null) }
    
    val tasks by viewModel.todayTasks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Today's Tasks",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tasks for today. Add one to get started!",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks.size) { index ->
                    TaskItem(
                        task = tasks[index],
                        onToggleCompletion = {
                            viewModel.toggleTaskCompletion(tasks[index])
                        },
                        onDelete = {
                            viewModel.deleteTask(tasks[index])
                        },
                        onEdit = {
                            taskToEdit = tasks[index]
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Task")
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAddTask = { taskName ->
                viewModel.addTask(taskName)
                showAddDialog = false
            }
        )
    }

    if (taskToEdit != null) {
        EditTaskDialog(
            task = taskToEdit!!,
            onDismiss = { taskToEdit = null },
            onEditTask = { newName ->
                viewModel.editTask(taskToEdit!!, newName)
                taskToEdit = null
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleCompletion() }
                )
                Text(
                    task.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) Color.Gray else Color.Black
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String) -> Unit
) {
    var taskName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            TextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Task name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskName.isNotBlank()) {
                        onAddTask(taskName)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onEditTask: (String) -> Unit
) {
    var taskName by remember { mutableStateOf(task.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            TextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Task name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskName.isNotBlank()) {
                        onEditTask(taskName)
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReportScreen(viewModel: TaskViewModel = viewModel()) {
    var selectedMonth by remember { mutableStateOf(System.currentTimeMillis()) }
    val reportData by viewModel.reportData.collectAsState()

    LaunchedEffect(selectedMonth) {
        viewModel.setSelectedReportMonth(selectedMonth)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MonthSelector(
            selectedMonth = selectedMonth,
            onPreviousMonth = {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedMonth
                calendar.add(Calendar.MONTH, -1)
                selectedMonth = calendar.timeInMillis
            },
            onNextMonth = {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedMonth
                calendar.add(Calendar.MONTH, 1)
                selectedMonth = calendar.timeInMillis
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SummaryCards(reportData = reportData)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Daily Details",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (reportData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No data for this month",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reportData.size) { index ->
                    DailyReportItem(reportData[index])
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    selectedMonth: Long,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(selectedMonth))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous month",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                monthName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next month",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryCards(reportData: List<DailyTaskStats>) {
    val totalCoins = reportData.sumOf { it.coinsEarned }
    val completedTasks = reportData.sumOf { stats ->
        stats.tasks.count { it.isCompleted }
    }
    val totalTasks = reportData.sumOf { it.tasks.size }
    val overallCompletionRate = if (totalTasks > 0) {
        (completedTasks.toDouble() / totalTasks) * 100
    } else {
        0.0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total Coins",
            value = totalCoins.toString(),
            icon = "💰",
            backgroundColor = Color(0xFFFFD700),
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Completed",
            value = "$completedTasks/$totalTasks",
            icon = "✓",
            backgroundColor = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Completion Rate",
            value = String.format("%.1f%%", overallCompletionRate),
            icon = "📊",
            backgroundColor = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Days Active",
            value = reportData.size.toString(),
            icon = "📅",
            backgroundColor = Color(0xFF9C27B0),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                icon,
                fontSize = 28.sp
            )
            Text(
                title,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
        }
    }
}

@Composable
fun DailyReportItem(stat: DailyTaskStats) {
    val completedCount = stat.tasks.count { it.isCompleted }
    val totalCount = stat.tasks.size
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dateString = dateFormat.format(Date(stat.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dateString,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                CompletionBadge(completionRate = stat.completionRate)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Tasks: $completedCount/$totalCount",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Coins: ${stat.coinsEarned}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD700)
                    )
                }

                ProgressBar(
                    progress = stat.completionRate / 100.0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CompletionBadge(completionRate: Double) {
    val backgroundColor = when {
        completionRate < 30 -> Color(0xFFEF5350)
        completionRate < 60 -> Color(0xFFFFA726)
        completionRate < 90 -> Color(0xFF66BB6A)
        else -> Color(0xFF29B6F6)
    }

    Card(
        modifier = Modifier
            .background(backgroundColor, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            String.format("%.0f%%", completionRate),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun ProgressBar(
    progress: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(8.dp)
            .background(Color(0xFFE0E0E0), shape = MaterialTheme.shapes.small),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0), shape = MaterialTheme.shapes.small)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.toFloat())
                    .background(Color(0xFF4CAF50), shape = MaterialTheme.shapes.small),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
            ) {}
        }
    }
}

sealed class Screen {
    data object TaskList : Screen()
    data object Report : Screen()
}
