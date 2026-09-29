package com.rushd.calisthenicscoach.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rushd.calisthenicscoach.domain.ProgramEngine
import com.rushd.calisthenicscoach.domain.UserProfile
import com.rushd.calisthenicscoach.data.OnboardingDraft

@Composable
fun OnboardingScreen(
    initialDraft: OnboardingDraft,
    onDraftChange: (Int, UserProfile) -> Unit,
    onComplete: (UserProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(initialDraft.step.coerceIn(0, 6)) }

    var age by remember { mutableIntStateOf(initialDraft.profile.age) }
    var height by remember { mutableIntStateOf(initialDraft.profile.heightCm) }
    var weight by remember { mutableIntStateOf(initialDraft.profile.weightKg) }
    var activity by remember { mutableStateOf(initialDraft.profile.activityLevel) }

    var goal by remember { mutableStateOf(initialDraft.profile.goal) }
    var experience by remember { mutableStateOf(initialDraft.profile.experience) }

    var days by remember { mutableIntStateOf(initialDraft.profile.daysPerWeek) }
    var minutes by remember { mutableIntStateOf(initialDraft.profile.sessionMinutes) }

    var equipment by remember { mutableStateOf(initialDraft.profile.equipment) }
    var limitations by remember { mutableStateOf(initialDraft.profile.limitations) }
    var skills by remember { mutableStateOf(initialDraft.profile.targetSkills) }

    var pushUps by remember { mutableIntStateOf(initialDraft.profile.maxPushUps) }
    var pullUps by remember { mutableIntStateOf(initialDraft.profile.maxPullUps) }
    var dips by remember { mutableIntStateOf(initialDraft.profile.maxDips) }
    var hollow by remember { mutableIntStateOf(initialDraft.profile.hollowHoldSec) }

    val draft = UserProfile(
        age = age,
        heightCm = height,
        weightKg = weight,
        activityLevel = activity,
        goal = goal,
        experience = experience,
        daysPerWeek = days,
        sessionMinutes = minutes,
        equipment = equipment,
        limitations = limitations,
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
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (step > 0) {
                        IconButton(onClick = {
                            step--
                            onDraftChange(step, draft)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "إعداد خطتك",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "الخطوة ${step + 1} من 7",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { (step + 1) / 7f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        if (step < 6) {
                            val next = step + 1
                            step = next
                            onDraftChange(next, draft)
                        } else {
                            onComplete(draft)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(58.dp)
                ) {
                    Text(
                        if (step < 6) "متابعة" else "أنشئ خطتي الشخصية",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (step < 6) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                0 -> BodyProfileStep(
                    age = age,
                    height = height,
                    weight = weight,
                    activity = activity,
                    onAge = { age = it.coerceIn(14, 85) },
                    onHeight = { height = it.coerceIn(130, 220) },
                    onWeight = { weight = it.coerceIn(35, 200) },
                    onActivity = { activity = it }
                )

                1 -> ChoiceStep(
                    eyebrow = "الهدف الرئيسي",
                    title = "ما النتيجة التي تريد بناء البرنامج حولها؟",
                    subtitle = "الهدف يغير توزيع الدفع والسحب والجذع والمهارة داخل الأسبوع.",
                    options = listOf("قوة عامة", "Pull-up", "Muscle-up", "Handstand", "Front Lever", "Planche"),
                    selected = goal,
                    onSelect = { goal = it }
                )

                2 -> AssessmentStep(
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

                3 -> ScheduleStep(
                    days = days,
                    minutes = minutes,
                    onDays = { days = it },
                    onMinutes = { minutes = it }
                )

                4 -> MultiChoiceStep(
                    eyebrow = "المعدات",
                    title = "ما المتاح لديك بانتظام؟",
                    subtitle = "لن نضع تمرينًا يحتاج أداة لا تملكها.",
                    options = listOf("Pull-up Bar", "Parallel Bars", "Rings", "Resistance Band", "Bench"),
                    selected = equipment,
                    onToggle = { value ->
                        equipment = if (value in equipment) equipment - value else equipment + value
                    },
                    helper = "يمكنك تركها فارغة؛ سيُبنى البرنامج من تمارين وزن الجسم المتاحة في المكتبة."
                )

                5 -> MultiChoiceStep(
                    eyebrow = "الراحة والحركة",
                    title = "هل لديك مناطق تريد أن نتعامل معها بحذر؟",
                    subtitle = "هذا ليس تشخيصًا طبيًا؛ نستخدمه فقط لتجنب بعض الحركات في الخطة.",
                    options = listOf(
                        "ألم/انزعاج في الكتف",
                        "ألم/انزعاج في الركبة",
                        "ألم/انزعاج في المعصم",
                        "ألم/انزعاج أسفل الظهر"
                    ),
                    selected = limitations,
                    onToggle = { value ->
                        limitations = if (value in limitations) limitations - value else limitations + value
                    },
                    helper = "إذا كان الألم مستمرًا أو حادًا، يلزم تقييم مختص قبل التدريب."
                )

                else -> ReviewStep(
                    profile = draft,
                    selectedSkills = skills,
                    onToggleSkill = { value ->
                        skills = if (value in skills) skills - value else skills + value
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BodyProfileStep(
    age: Int,
    height: Int,
    weight: Int,
    activity: String,
    onAge: (Int) -> Unit,
    onHeight: (Int) -> Unit,
    onWeight: (Int) -> Unit,
    onActivity: (String) -> Unit
) {
    StepHeader(
        "الملف الرياضي",
        "لنبدأ بطبيعة جسمك ونمط يومك",
        "العمر وبنية الجسم والنشاط اليومي تؤثر في حجم الجلسة، فترات الراحة وسرعة التدرج."
    )

    NumericInput("العمر", "سنة", age, onAge, 14..85)
    NumericInput("الطول", "سم", height, onHeight, 130..220)
    NumericInput("الوزن", "كغ", weight, onWeight, 35..200)

    Text("طبيعة النشاط اليومي", fontWeight = FontWeight.Bold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf("منخفض", "متوسط", "مرتفع")) { item ->
            FilterChip(
                selected = activity == item,
                onClick = { onActivity(item) },
                label = { Text(item) }
            )
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
            title = displayOption(option),
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
    onToggle: (String) -> Unit,
    helper: String
) {
    StepHeader(eyebrow, title, subtitle)
    options.forEach { option ->
        SelectCard(
            title = displayOption(option),
            selected = option in selected,
            onClick = { onToggle(option) },
            radio = false
        )
    }
    Text(
        helper,
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
        "المستوى الحالي",
        "ما الذي تستطيع تنفيذه الآن؟",
        "سنبني نقطة البداية من الأداء الفعلي، لا من وصف مبتدئ أو متقدم وحده."
    )

    Text("خبرتك السابقة", fontWeight = FontWeight.Bold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf("مبتدئ", "متوسط", "متقدم")) { item ->
            FilterChip(
                selected = experience == item,
                onClick = { onExperience(item) },
                label = { Text(item) }
            )
        }
    }

    NumericInput("تمرين الضغط", "تكرار نظيف", pushUps, onPushUps, 0..100)
    NumericInput("العقلة", "تكرار نظيف", pullUps, onPullUps, 0..50)
    NumericInput("المتوازي", "تكرار نظيف", dips, onDips, 0..50)
    NumericInput("الثبات المجوف", "ثانية", hollow, onHollow, 0..180)
}

@Composable
private fun ScheduleStep(
    days: Int,
    minutes: Int,
    onDays: (Int) -> Unit,
    onMinutes: (Int) -> Unit
) {
    StepHeader(
        "الوقت المتاح",
        "الخطة الجيدة هي التي تستطيع الاستمرار بها",
        "سنوزع الجهد والاستشفاء على عدد الأيام والزمن الذي يمكنك الالتزام به."
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
        "المهارات والخطة",
        "اختر المهارات التي تريد تطويرها",
        "ستبقى المهارة جزءًا من البرنامج، وليست تمرينًا منفصلًا عن قاعدة القوة."
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf("Pull-up", "Handstand", "L-sit", "Muscle-up", "Front Lever")) { skill ->
            FilterChip(
                selected = skill in selectedSkills,
                onClick = { onToggleSkill(skill) },
                label = { Text(displayOption(skill)) }
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
            Text("الخطة التي بُنيت لك", style = MaterialTheme.typography.labelLarge)
            Text(
                blueprint.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                "${blueprint.totalWeeks} أسابيع · ${profile.daysPerWeek} أيام أسبوعيًا · ${profile.sessionMinutes} دقيقة"
            )
            Text(
                blueprint.rationale,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            blueprint.readinessNotes.forEach { note ->
                Text("• $note", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    Text(
        "يمكن تعديل الهدف والجدول لاحقًا وإعادة بناء الخطة دون فقدان سجل التدريب.",
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
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
private fun SelectCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    radio: Boolean = true
) {
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
            if (radio) {
                RadioButton(selected = selected, onClick = onClick)
            } else {
                Checkbox(checked = selected, onCheckedChange = { onClick() })
            }
        }
    }
}

@Composable
private fun NumericInput(
    title: String,
    unit: String,
    value: Int,
    onValue: (Int) -> Unit,
    range: IntRange
) {
    var text by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            val filtered = raw.filter { it.isDigit() }.take(3)
            text = filtered
            filtered.toIntOrNull()?.let { onValue(it.coerceIn(range.first, range.last)) }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(title) },
        supportingText = { Text(unit) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            Text(
                unit,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        },
        shape = RoundedCornerShape(18.dp)
    )
}

private fun displayOption(value: String): String = when (value) {
    "Pull-up" -> "العقلة"
    "Muscle-up" -> "المسل أب"
    "Handstand" -> "الوقوف على اليدين"
    "Front Lever" -> "الفرونت ليفر"
    "Planche" -> "البلانش"
    "L-sit" -> "إل-سِت"
    "Pull-up Bar" -> "بار العقلة"
    "Parallel Bars" -> "المتوازي"
    "Rings" -> "الحلقات"
    "Resistance Band" -> "حزام مقاومة"
    "Bench" -> "مقعد"
    else -> value
}
