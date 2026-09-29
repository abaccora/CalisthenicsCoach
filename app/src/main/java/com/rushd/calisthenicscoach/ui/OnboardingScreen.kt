package com.rushd.calisthenicscoach.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rushd.calisthenicscoach.domain.ProgramEngine
import com.rushd.calisthenicscoach.domain.UserProfile

@Composable
fun OnboardingScreen(onComplete: (UserProfile) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var goal by remember { mutableStateOf("قوة عامة") }
    var experience by remember { mutableStateOf("مبتدئ") }
    var days by remember { mutableIntStateOf(3) }
    var minutes by remember { mutableIntStateOf(45) }
    var equipment by remember { mutableStateOf(setOf<String>()) }
    var skills by remember { mutableStateOf(setOf<String>()) }
    var pushUps by remember { mutableIntStateOf(10) }
    var pullUps by remember { mutableIntStateOf(0) }
    var dips by remember { mutableIntStateOf(0) }
    var hollow by remember { mutableIntStateOf(20) }

    val draft = UserProfile(
        goal = goal,
        experience = experience,
        daysPerWeek = days,
        sessionMinutes = minutes,
        equipment = equipment,
        targetSkills = skills,
        maxPushUps = pushUps,
        maxPullUps = pullUps,
        maxDips = dips,
        hollowHoldSec = hollow,
        onboardingCompleted = true
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (step > 0) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${step + 1} / 5",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                LinearProgressIndicator(
                    progress = { (step + 1) / 5f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        if (step < 4) step++ else onComplete(draft)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(58.dp)
                ) {
                    Text(
                        if (step < 4) "متابعة" else "أنشئ خطتي",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (step < 4) Icons.Default.ArrowForward else Icons.Default.Check,
                        contentDescription = null
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            when (step) {
                0 -> ChoiceStep(
                    eyebrow = "الهدف",
                    title = "ما الذي تريد الوصول إليه؟",
                    subtitle = "نستخدم الهدف لاختيار نوع الجلسات وتسلسل المهارات.",
                    options = listOf("قوة عامة", "Muscle-up", "Pull-up", "Handstand", "Front Lever", "Planche"),
                    selected = goal,
                    onSelect = { goal = it }
                )
                1 -> AssessmentStep(
                    pushUps = pushUps,
                    pullUps = pullUps,
                    dips = dips,
                    hollow = hollow,
                    onPushUps = { pushUps = it },
                    onPullUps = { pullUps = it },
                    onDips = { dips = it },
                    onHollow = { hollow = it },
                    experience = experience,
                    onExperience = { experience = it }
                )
                2 -> ScheduleStep(
                    days = days,
                    minutes = minutes,
                    onDays = { days = it },
                    onMinutes = { minutes = it }
                )
                3 -> MultiChoiceStep(
                    eyebrow = "المعدات",
                    title = "ما المتاح لديك؟",
                    subtitle = "اختر كل ما يمكنك استخدامه بانتظام.",
                    options = listOf("Pull-up Bar", "Parallel Bars", "Rings", "Resistance Band", "Bench"),
                    selected = equipment,
                    onToggle = { value ->
                        equipment = if (value in equipment) equipment - value else equipment + value
                    }
                )
                else -> ReviewStep(
                    profile = draft,
                    selectedSkills = skills,
                    onToggleSkill = { value ->
                        skills = if (value in skills) skills - value else skills + value
                    }
                )
            }
        }
    }
}

@Composable
private fun ChoiceStep(
    eyebrow: String,
    title: String,
    subtitle: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    StepHeader(eyebrow, title, subtitle)
    options.forEach { option ->
        SelectCard(
            title = option,
            selected = option == selected,
            onClick = { onSelect(option) }
        )
    }
}

@Composable
private fun MultiChoiceStep(
    eyebrow: String,
    title: String,
    subtitle: String,
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    StepHeader(eyebrow, title, subtitle)
    options.forEach { option ->
        SelectCard(
            title = option,
            selected = option in selected,
            onClick = { onToggle(option) }
        )
    }
    Text(
        "يمكنك المتابعة دون معدات؛ سيُبنى البرنامج بوزن الجسم.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun AssessmentStep(
    pushUps: Int,
    pullUps: Int,
    dips: Int,
    hollow: Int,
    onPushUps: (Int) -> Unit,
    onPullUps: (Int) -> Unit,
    onDips: (Int) -> Unit,
    onHollow: (Int) -> Unit,
    experience: String,
    onExperience: (String) -> Unit
) {
    StepHeader(
        "تقييم سريع",
        "لنبدأ من مستواك الحقيقي",
        "اكتب أفضل أداء نظيف يمكنك تنفيذه الآن. لا تحتاج لاختبار مرهق."
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf("مبتدئ", "متوسط", "متقدم")) { item ->
            FilterChip(
                selected = experience == item,
                onClick = { onExperience(item) },
                label = { Text(item) }
            )
        }
    }

    CounterRow("Push-ups", "تكرار", pushUps, onPushUps)
    CounterRow("Pull-ups", "تكرار", pullUps, onPullUps)
    CounterRow("Dips", "تكرار", dips, onDips)
    CounterRow("Hollow hold", "ثانية", hollow, onHollow, step = 5)
}

@Composable
private fun ScheduleStep(
    days: Int,
    minutes: Int,
    onDays: (Int) -> Unit,
    onMinutes: (Int) -> Unit
) {
    StepHeader(
        "الجدول",
        "خطة قابلة للاستمرار",
        "سنوزع الجهد على أيام يمكنك الالتزام بها فعلًا."
    )

    Text("أيام التدريب أسبوعيًا", fontWeight = FontWeight.Bold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf(2, 3, 4, 5)) { value ->
            FilterChip(
                selected = days == value,
                onClick = { onDays(value) },
                label = { Text("$value أيام") }
            )
        }
    }

    Text("مدة الجلسة", fontWeight = FontWeight.Bold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf(30, 45, 60)) { value ->
            FilterChip(
                selected = minutes == value,
                onClick = { onMinutes(value) },
                label = { Text("$value دقيقة") }
            )
        }
    }
}

@Composable
private fun ReviewStep(
    profile: UserProfile,
    selectedSkills: Set<String>,
    onToggleSkill: (String) -> Unit
) {
    val blueprint = remember(profile, selectedSkills) {
        ProgramEngine.build(profile.copy(targetSkills = selectedSkills))
    }

    StepHeader(
        "المهارات",
        "اختر ما تريد تطويره",
        "سنربط المهارات بالمرحلة الأساسية بدل تدريبها عشوائيًا."
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf("Pull-up", "Handstand", "L-sit", "Muscle-up", "Front Lever")) { skill ->
            FilterChip(
                selected = skill in selectedSkills,
                onClick = { onToggleSkill(skill) },
                label = { Text(skill) }
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("معاينة خطتك", style = MaterialTheme.typography.labelLarge)
            Text(
                blueprint.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text("${blueprint.totalWeeks} أسابيع · ${profile.daysPerWeek} أيام أسبوعيًا · ${profile.sessionMinutes} دقيقة")
            Text(
                blueprint.rationale,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun StepHeader(eyebrow: String, title: String, subtitle: String) {
    Text(
        eyebrow,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black
    )
    Text(
        subtitle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun SelectCard(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@Composable
private fun CounterRow(
    title: String,
    unit: String,
    value: Int,
    onValue: (Int) -> Unit,
    step: Int = 1
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(unit, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(
                onClick = { onValue((value - step).coerceAtLeast(0)) },
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) { Text("−") }
            Text(
                "$value",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            FilledTonalButton(
                onClick = { onValue(value + step) },
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) { Text("+") }
        }
    }
}
