package com.example.todoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.ui.theme.ToDoAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ─── Colors ───────────────────────────────────────────────────────────────────

val BgColor       = Color(0xFFF7F8FC)
val CardBg        = Color(0xFFFFFFFF)
val TextPrimary   = Color(0xFF1A1A2E)
val TextSecondary = Color(0xFF8E8EA0)
val AccentPurple  = Color(0xFF6C63FF)
val AccentPink    = Color(0xFFFF6584)
val AccentGreen   = Color(0xFF43C6AC)
val AccentOrange  = Color(0xFFFFB347)

// ─── Data Models ──────────────────────────────────────────────────────────────

data class Task(
    val id: Int,
    val title: String,
    val subTaskCount: Int,
    val deadline: String,
    val color: Color,
    val isCompleted: Boolean = false,
    val category: String = ""
)

data class NavItem(
    val icon: ImageVector,
    val label: String
)

// ─── Sample Tasks ─────────────────────────────────────────────────────────────

val sampleTasks = listOf(
    Task(1, "Design wireframes",    4, "Today, 3:00 PM",     Color(0xFF6C63FF), category = "Work"),
    Task(2, "Team standup meeting", 2, "Today, 10:00 AM",    Color(0xFFFF6584), category = "Meetings"),
    Task(3, "Review pull requests", 3, "Tomorrow, 12:00 PM", Color(0xFF43C6AC), category = "Dev"),
    Task(4, "Grocery shopping",     6, "Today, 6:00 PM",     Color(0xFFFFB347), category = "Personal"),
    Task(5, "Write unit tests",     5, "Jun 2, 9:00 AM",     Color(0xFF56CCF2), category = "Dev"),
    Task(6, "Update portfolio",     3, "Jun 3, 5:00 PM",     Color(0xFFFF9A9E), category = "Personal"),
)

// ─── Picker Options ───────────────────────────────────────────────────────────

val categoryOptions = listOf("Work", "Personal", "Dev", "Meetings", "Health", "Other")

val colorOptions = listOf(
    Color(0xFF6C63FF),
    Color(0xFFFF6584),
    Color(0xFF43C6AC),
    Color(0xFFFFB347),
    Color(0xFF56CCF2),
    Color(0xFFFF9A9E)
)

// ─── Main Activity ────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDoAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen()
                }
            }
        }
    }
}

// ─── Home Screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val tasks      = remember { mutableStateListOf(*sampleTasks.toTypedArray()) }
    var selectedNav by remember { mutableIntStateOf(0) }
    var nextId     by remember { mutableIntStateOf(sampleTasks.size + 1) }
    var showSheet  by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope      = rememberCoroutineScope()

    val navItems = listOf(
        NavItem(Icons.Outlined.Home,              "Home"),
        NavItem(Icons.Outlined.CalendarMonth,     "Calendar"),
        NavItem(Icons.Filled.Add,                 "Add"),
        NavItem(Icons.Outlined.StickyNote2,       "Notes"),
        NavItem(Icons.Outlined.NotificationsNone, "Reminder")
    )

    // Sheet lives here — outside Scaffold, at the top level
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState       = sheetState,
            containerColor   = CardBg,
            shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            AddTaskSheet(
                onAdd = { newTask ->
                    tasks.add(newTask.copy(id = nextId))
                    nextId++
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                }
            )
        }
    }

    Scaffold(
        containerColor = BgColor,
        bottomBar = {
            BottomNavBar(
                items         = navItems,
                selectedIndex = selectedNav,
                onItemClick   = { index ->
                    if (index == 2) {
                        showSheet = true
                    } else {
                        selectedNav = index
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                TopBar()
            }
            item { StatsGrid(tasks = tasks) }
            item { DeadlineSection(tasks = tasks) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("All Tasks",           fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${tasks.size} tasks", fontSize = 13.sp, color = TextSecondary)
                }
            }
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task       = task,
                    onComplete = {
                        val idx = tasks.indexOfFirst { it.id == task.id }
                        if (idx != -1) tasks[idx] = tasks[idx].copy(isCompleted = true)
                    },
                    onDelete = { tasks.removeIf { it.id == task.id } }
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ─── Add Task Sheet ───────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskSheet(onAdd: (Task) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current

    var title         by remember { mutableStateOf("") }
    var selectedCat   by remember { mutableStateOf("Work") }
    var selectedColor by remember { mutableStateOf(colorOptions[0]) }
    var subTaskCount  by remember { mutableIntStateOf(0) }
    var deadlineText  by remember { mutableStateOf("No deadline set") }
    var titleError    by remember { mutableStateOf(false) }

    // Store picked date parts so time picker can use them
    var pickedDateLabel by remember { mutableStateOf("") }

    fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val h    = if (hour % 12 == 0) 12 else hour % 12
                val m    = minute.toString().padStart(2, '0')
                deadlineText = "$pickedDateLabel, $h:$m $amPm"
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val today  = Calendar.getInstance()
                val picked = Calendar.getInstance().apply { set(year, month, day) }
                pickedDateLabel = when {
                    isSameDay(picked, today)  -> "Today"
                    isTomorrow(picked, today) -> "Tomorrow"
                    else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(picked.time)
                }
                showTimePicker()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))
        Text("New Task", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(20.dp))

        Text("Task Title", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value         = title,
            onValueChange = { title = it; titleError = false },
            placeholder   = { Text("What do you need to do?", color = TextSecondary) },
            isError       = titleError,
            singleLine    = true,
            shape         = RoundedCornerShape(14.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = AccentPurple,
                unfocusedBorderColor = Color(0xFFE8E8F0),
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        if (titleError) {
            Text("Please enter a task title", fontSize = 11.sp, color = AccentPink)
        }
        Spacer(Modifier.height(18.dp))

        Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            categoryOptions.forEach { cat ->
                val isSelected = cat == selectedCat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AccentPurple else Color(0xFFF0F0F8))
                        .clickable { selectedCat = cat }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        cat,
                        fontSize   = 12.sp,
                        color      = if (isSelected) Color.White else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))

        Text("Color", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            colorOptions.forEach { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                            else Modifier
                        )
                        .clickable { selectedColor = color }
                )
            }
        }
        Spacer(Modifier.height(18.dp))

        Text("Deadline", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF0F0F8))
                .clickable { showDatePicker() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                deadlineText,
                fontSize   = 14.sp,
                color      = if (deadlineText == "No deadline set") TextSecondary else TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint     = AccentPurple,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(18.dp))

        Text("Subtasks", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (subTaskCount > 0) AccentPurple else Color(0xFFDDDDDD))
                    .clickable { if (subTaskCount > 0) subTaskCount-- },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F8))
                    .padding(horizontal = 28.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("$subTaskCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AccentPurple)
                    .clickable { subTaskCount++ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                if (title.isBlank()) {
                    titleError = true
                } else {
                    onAdd(
                        Task(
                            id           = 0,
                            title        = title.trim(),
                            subTaskCount = subTaskCount,
                            deadline     = deadlineText,
                            color        = selectedColor,
                            category     = selectedCat
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
        ) {
            Text("Add Task", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ─── Date Helpers ─────────────────────────────────────────────────────────────

fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR)        == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

fun isTomorrow(a: Calendar, today: Calendar): Boolean {
    val tomorrow = today.clone() as Calendar
    tomorrow.add(Calendar.DAY_OF_YEAR, 1)
    return isSameDay(a, tomorrow)
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
fun TopBar() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else      -> "Good evening"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(greeting, fontSize = 14.sp, color = TextSecondary)
            Text("Alex 👋", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AccentPurple, AccentPink))),
            contentAlignment = Alignment.Center
        ) {
            Text("A", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Stats Grid ───────────────────────────────────────────────────────────────

@Composable
fun StatsGrid(tasks: List<Task>) {
    val completedToday = tasks.count { it.isCompleted && it.deadline.startsWith("Today") }
    val scheduledToday = tasks.count { it.deadline.startsWith("Today") }
    val dueToday       = tasks.count { it.deadline.startsWith("Today") && !it.isCompleted }
    val total          = tasks.size

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Completed Today", "$completedToday / $scheduledToday", AccentGreen,  Modifier.weight(1f))
            StatCard("Scheduled",       "$scheduledToday tasks",             AccentPurple, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Due Today",   "$dueToday urgent", AccentPink,   Modifier.weight(1f))
            StatCard("Total Tasks", "$total overall",   AccentOrange, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier.height(90.dp),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
            Column {
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(title, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
            }
        }
    }
}

// ─── Deadline Section ─────────────────────────────────────────────────────────

@Composable
fun DeadlineSection(tasks: List<Task>) {
    val todayTasks = tasks.filter { it.deadline.startsWith("Today") && !it.isCompleted }
    val today = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deadlines & Reminders", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(2.dp))
            Text(today, fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(12.dp))
            if (todayTasks.isEmpty()) {
                Text("🎉 No pending deadlines today!", fontSize = 13.sp, color = TextSecondary)
            } else {
                todayTasks.take(3).forEach { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(task.color))
                            Spacer(Modifier.width(10.dp))
                            Text(task.title, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            task.deadline.removePrefix("Today, "),
                            fontSize = 12.sp, color = AccentPink, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ─── Task Card ────────────────────────────────────────────────────────────────

@Composable
fun TaskCard(task: Task, onComplete: () -> Unit, onDelete: () -> Unit) {
    var offsetX    by remember { mutableFloatStateOf(0f) }
    var isExpanded by remember { mutableStateOf(false) }
    val threshold  = 200f

    val animatedOffset by animateFloatAsState(targetValue = offsetX, animationSpec = tween(100), label = "offset")
    val bgColor by animateColorAsState(
        targetValue = when {
            offsetX >  60f -> Color(0xFFE8F5E9)
            offsetX < -60f -> Color(0xFFFFEBEE)
            else           -> CardBg
        },
        animationSpec = tween(200), label = "bg"
    )
    val draggableState = rememberDraggableState { delta ->
        offsetX = (offsetX + delta).coerceIn(-threshold * 1.5f, threshold * 1.5f)
    }

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))) {
        Row(
            modifier = Modifier.matchParentSize().clip(RoundedCornerShape(18.dp)).background(bgColor),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Check,  contentDescription = "Complete", tint = Color(0xFF4CAF50), modifier = Modifier.padding(start = 20.dp))
            Icon(Icons.Filled.Delete, contentDescription = "Delete",   tint = Color(0xFFF44336), modifier = Modifier.padding(end   = 20.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = animatedOffset.dp)
                .draggable(
                    state       = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        when {
                            offsetX >  threshold -> { onComplete(); offsetX = 0f }
                            offsetX < -threshold -> { onDelete();   offsetX = 0f }
                            else                 -> { offsetX = 0f }
                        }
                    }
                )
                .clickable { isExpanded = !isExpanded },
            shape     = RoundedCornerShape(18.dp),
            colors    = CardDefaults.cardColors(containerColor = if (task.isCompleted) Color(0xFFF0F0F0) else CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(4.dp)).background(if (task.isCompleted) Color.LightGray else task.color))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                task.title,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (task.isCompleted) TextSecondary else TextPrimary,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Text(task.deadline, fontSize = 12.sp, color = if (task.isCompleted) Color.LightGray else TextSecondary)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(task.color.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(task.category, fontSize = 10.sp, color = task.color, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Expand", tint = TextSecondary, modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (isExpanded) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = task.color, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${task.subTaskCount} subtasks", fontSize = 13.sp, color = TextSecondary)
                        }
                        Text(
                            if (task.isCompleted) "✓ Completed" else "In Progress",
                            fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color    = if (task.isCompleted) AccentGreen else AccentOrange
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress   = { if (task.isCompleted) 1f else 0.4f },
                        modifier   = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(4.dp)),
                        color      = task.color,
                        trackColor = task.color.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

// ─── Bottom Nav Bar ───────────────────────────────────────────────────────────

@Composable
fun BottomNavBar(items: List<NavItem>, selectedIndex: Int, onItemClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 20.dp,
                shape        = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor    = Color.Black.copy(alpha = 0.25f)
            )
            .background(CardBg, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                if (index == 2) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .offset(y = (-10).dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AccentPurple, AccentPink)))
                            .clickable { onItemClick(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, contentDescription = item.label, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                } else {
                    val isSelected = selectedIndex == index
                    val iconColor by animateColorAsState(
                        targetValue   = if (isSelected) AccentPurple else TextSecondary,
                        animationSpec = tween(200), label = "navColor"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onItemClick(index) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(item.icon, contentDescription = item.label, tint = iconColor, modifier = Modifier.size(24.dp))
                        if (isSelected) {
                            Spacer(Modifier.height(3.dp))
                            Box(Modifier.size(4.dp).clip(CircleShape).background(AccentPurple))
                        }
                    }
                }
            }
        }
    }
}