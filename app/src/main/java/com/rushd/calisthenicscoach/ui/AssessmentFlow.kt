package com.rushd.calisthenicscoach.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rushd.calisthenicscoach.data.AssessmentResultEntity
import com.rushd.calisthenicscoach.domain.AssessmentEngine
import com.rushd.calisthenicscoach.domain.ExerciseVideoCatalog
import com.rushd.calisthenicscoach.domain.MeasurementType
import com.rushd.calisthenicscoach.domain.TrainingBlueprint
import com.rushd.calisthenicscoach.domain.UserProfile

@Composable
fun GuidedAssessmentScreen(
    profile: UserProfile,
    onComplete: (List<AssessmentResultEntity>) -> Unit
) {
    val plan = remember(profile) { AssessmentEngine.build(profile) }
    var index by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateMapOf<String, String>() }
    val metric = plan.metrics[index]
    val value = answers[metric.id].orEmpty()
    val video = metric.exerciseName?.let { ExerciseVideoCatalog.forExercise(it) }

    Scaffold(
        topBar = {
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("تقييم البداية", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "الاختبار ${index + 1} من ${plan.metrics.size}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { (index + 1f) / plan.metrics.size.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (index > 0) {
                        FilledTonalButton(
                            onClick = { index-- },
                            modifier = Modifier.weight(1f).height(54.dp)
                        ) {
                            Text("السابق")
                        }
                    }
                    Button(
                        onClick = {
                            if (index < plan.metrics.lastIndex) {
                                index++
                            } else {
                                val results = plan.metrics.mapNotNull { item ->
                                    answers[item.id]?.toDoubleOrNull()?.let { amount ->
                                        AssessmentResultEntity(
                                            metricId = item.id,
                                            value = amount,
                                            unit = item.unit,
                                            goalContext = profile.goal
                                        )
                                    }
                                }
                                onComplete(results)
                            }
                        },
                        enabled = metric.optional || value.toDoubleOrNull() != null,
                        modifier = Modifier.weight(1f).height(54.dp)
                    ) {
                        Text(if (index == plan.metrics.lastIndex) "إنهاء التقييم" else "التالي")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    metric.titleAr,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    assessmentInstruction(metric.measurementType),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (video != null) {
                item {
                    ExerciseVideoPlayer(
                        video = video,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                        showControls = false,
                        autoPlay = true
                    )
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { raw ->
                                answers[metric.id] = raw.filter { it.isDigit() }.take(3)
                            },
                            label = { Text(assessmentUnitLabel(metric.unit)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (metric.optional) {
                            TextButton(
                                onClick = {
                                    answers[metric.id] = "0"
                                    if (index < plan.metrics.lastIndex) index++ else {
                                        val results = plan.metrics.mapNotNull { item ->
                                            answers[item.id]?.toDoubleOrNull()?.let { amount ->
                                                AssessmentResultEntity(
                                                    metricId = item.id,
                                                    value = amount,
                                                    unit = item.unit,
                                                    goalContext = profile.goal
                                                )
                                            }
                                        }
                                        onComplete(results)
                                    }
                                }
                            ) {
                                Text("لا أستطيع أداء هذا الاختبار الآن")
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "استخدم أداءً نظيفًا يمكنك تنفيذه بأمان. لا نحتاج إلى دفعك للفشل الكامل كي نحدد نقطة البداية.",
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratedPlanScreen(
    profile: UserProfile,
    blueprint: TrainingBlueprint,
    onStart: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(58.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ابدأ خطتي", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "تم إعداد برنامجك",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "بدأ البرنامج من مستواك الحالي، معداتك، وقتك وهدفك التدريبي.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(blueprint.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        Text(blueprint.phaseTitle, style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider()
                        PlanFact("مدة البرنامج", "${blueprint.totalWeeks} أسابيع")
                        PlanFact("أيام التدريب", "${profile.daysPerWeek} أيام أسبوعيًا")
                        PlanFact("مدة الجلسة", "${profile.sessionMinutes} دقيقة تقريبًا")
                        PlanFact("عدد الجلسات", "${blueprint.weeklyDays.size} جلسات هذا الأسبوع")
                    }
                }
            }

            item {
                Text("لماذا هذه البداية؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(blueprint.rationale, color = MaterialTheme.colorScheme.onSurfaceVariant)
                blueprint.readinessNotes.forEach { note ->
                    Text("• $note", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                Text("جلسات الأسبوع الأول", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(blueprint.weeklyDays.size) { dayIndex ->
                val day = blueprint.weeklyDays[dayIndex]
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${dayIndex + 1}", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(day.title, fontWeight = FontWeight.Bold)
                            Text(day.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanFact(label: String, value: String) {
    Row {
        Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
        Spacer(Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold)
    }
}

private fun assessmentInstruction(type: MeasurementType): String = when (type) {
    MeasurementType.REPS,
    MeasurementType.QUALITY_REPS ->
        "نفّذ أكبر عدد من التكرارات النظيفة مع بقاء التقنية سليمة."
    MeasurementType.HOLD_SECONDS ->
        "اثبت في الوضعية الصحيحة وسجل المدة بالثواني."
    MeasurementType.UNILATERAL_REPS,
    MeasurementType.LEFT_RIGHT_REPS ->
        "نفّذ التكرارات لكل جهة وسجل عدد التكرارات للجهة الأضعف."
}

private fun assessmentUnitLabel(unit: String): String = when (unit) {
    "reps" -> "عدد التكرارات"
    "seconds" -> "المدة بالثواني"
    "reps_per_side" -> "التكرارات لكل جهة"
    else -> "النتيجة"
}
