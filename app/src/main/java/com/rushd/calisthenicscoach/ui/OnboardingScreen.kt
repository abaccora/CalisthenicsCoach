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
                        if (step < 6) "متابعة" else "الانتقال إلى تقييم البداية",
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
                    subtitle = "الهدف يحدد توزيع تمارين الدفع والسحب والجذع والعمل المهاري خلال الأسبوع.",
                    options = listOf("قوة عامة", "Pull-up", "Muscle-up", "Handstand", "Front Lever", "Planche"),
                    selected = goal,
                    onSelect = { goal = it }
                )

                2 -> ExperienceStep(
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
                    helper = "يمكنك تركها فارغة؛ سيبني التطبيق الخطة من تمارين وزن الجسم التي لا تحتاج إلى أدوات."
                )

                5 -> MultiChoiceStep(
                    eyebrow = "القيود الحركية",
                    title = "هل توجد آلام أو قيود حركية ينبغي مراعاتها؟",
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
        "لنبدأ ببياناتك الأساسية ونمط نشاطك",
        "العمر وبنية الجسم ومستوى النشاط اليومي عوامل تساعد على ضبط حجم التدريب وفترات الراحة وسرعة التدرج."
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
    options.chunked(2).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rowItems.forEach { option ->
                SelectCard(
                    modifier = Modifier.weight(1f),
                    title = displayOption(option),
                    selected = option == selected,
                    onClick = { onSelect(option) }
                )
            }
            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
        }
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
    options.chunked(2).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rowItems.forEach { option ->
                SelectCard(
                    modifier = Modifier.weight(1f),
                    title = displayOption(option),
                    selected = option in selected,
                    onClick = { onToggle(option) },
                    radio = false
                )
            }
            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
        }
    }
    Text(
        helper,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ExperienceStep(
    experience: String,
    onExperience: (String) -> Unit
) {
    StepHeader(
        "الخبرة السابقة",
        "كيف تصف خبرتك الحالية في تمارين وزن الجسم؟",
        "سنستخدم هذا كعامل مساعد فقط. بعد الإعداد ستنفذ تقييم بداية فعليًا لتحديد مستواك بالأداء."
    )

    listOf(
        "مبتدئ" to "خبرة محدودة أو عودة بعد انقطاع",
        "متوسط" to "أتدرب بانتظام وأتقن الحركات الأساسية",
        "متقدم" to "لدي قاعدة قوة جيدة وخبرة في التدرجات المهارية"
    ).forEach { (level, description) ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExperience(level) },
            shape = RoundedCornerShape(22.dp),
            color = if (experience == level)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(level, fontWeight = FontWeight.Bold)
                    Text(
                        description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                RadioButton(
                    selected = experience == level,
                    onClick = { onExperience(level) }
                )
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            "لن نطلب منك تقدير عدد التكرارات هنا. سيقيس تقييم البداية أداءك الفعلي بالفيديو ثم يحدد نقطة البداية في الخطة ومسارات المهارات.",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
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
        "اختر المهارات التي تريد تطويرها على المدى المتوسط",
        "ستُدمج المهارات داخل البرنامج إلى جانب بناء القوة الأساسية والتحكم الحركي."
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
            Text("ملامح خطتك قبل تقييم البداية", style = MaterialTheme.typography.labelLarge)
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
        "بعد تقييم البداية سيحدد التطبيق مستوى الانطلاق بدقة. ويمكن تعديل الهدف والجدول لاحقًا دون فقدان سجل التدريب.",
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
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    radio: Boolean = true
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
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

private fun displayOption(value: String): String = when {
    value in setOf("Pull-up", "Muscle-up", "Handstand", "Front Lever", "Planche", "L-sit", "Pistol Squat") ->
        com.rushd.calisthenicscoach.domain.ArabicTerminology.goal(value)
    value in setOf("Pull-up Bar", "Parallel Bars", "Rings", "Resistance Band", "Bench") ->
        com.rushd.calisthenicscoach.domain.ArabicTerminology.equipment(value)
    else -> value
}
