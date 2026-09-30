package com.rushd.calisthenicscoach.ui

import android.net.Uri
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.rushd.calisthenicscoach.data.AppDatabase
import com.rushd.calisthenicscoach.data.WorkoutHistory
import com.rushd.calisthenicscoach.data.SetPerformance
import com.rushd.calisthenicscoach.data.ExerciseHistoryStat
import com.rushd.calisthenicscoach.data.ActiveWorkoutSession
import com.rushd.calisthenicscoach.data.WorkoutSessionRepository
import com.rushd.calisthenicscoach.data.UserPreferencesRepository
import com.rushd.calisthenicscoach.data.OnboardingDraft
import com.rushd.calisthenicscoach.domain.SkillStateEngine
import com.rushd.calisthenicscoach.data.CoachRepository
import com.rushd.calisthenicscoach.domain.ExerciseVideo
import com.rushd.calisthenicscoach.domain.ExerciseVideoCatalog
import com.rushd.calisthenicscoach.domain.SamplePrograms
import com.rushd.calisthenicscoach.domain.ProgramEngine
import com.rushd.calisthenicscoach.domain.UserProfile
import com.rushd.calisthenicscoach.domain.WorkoutPlan
import com.rushd.calisthenicscoach.domain.SkillProgressionGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private sealed class Tab(val route: String, val label: String) {
    data object Today : Tab("today", "اليوم")
    data object Plan : Tab("plan", "الخطة")
    data object Library : Tab("library", "المكتبة")
    data object Progress : Tab("progress", "تقدمي")
}

private enum class DayState { Done, Today, Upcoming, Rest }

private data class WeekDay(
    val day: String,
    val title: String,
    val subtitle: String,
    val state: DayState,
    val planId: String? = null
)

@Composable
fun CalisthenicsApp() {
    val context = LocalContext.current
    val repository = remember { UserPreferencesRepository(context) }
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(false) }
    var savedProfile by remember { mutableStateOf<UserProfile?>(null) }
    var draft by remember { mutableStateOf(OnboardingDraft()) }

    LaunchedEffect(repository) {
        savedProfile = repository.profile.first()
        draft = repository.onboardingDraft.first()
        loaded = true
    }

    if (!loaded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (savedProfile?.onboardingCompleted != true) {
        OnboardingScreen(
            initialDraft = draft,
            onDraftChange = { step, profile ->
                draft = OnboardingDraft(step, profile)
                scope.launch { repository.saveDraft(step, profile) }
            },
            onComplete = { completed ->
                scope.launch {
                    repository.save(completed)
                    savedProfile = completed
                }
            }
        )
        return
    }

    CoachSetupGate(profile = savedProfile!!)
}

@Composable
private fun CoachSetupGate(profile: UserProfile) {
    val context = LocalContext.current
    val coachRepository = remember { CoachRepository(AppDatabase.get(context)) }
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(false) }
    var assessments by remember { mutableStateOf(emptyList<com.rushd.calisthenicscoach.data.AssessmentResultEntity>()) }
    var showGeneratedPlan by remember { mutableStateOf(false) }

    LaunchedEffect(coachRepository) {
        assessments = coachRepository.assessments.first()
        loaded = true
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (assessments.isEmpty()) {
        GuidedAssessmentScreen(
            profile = profile,
            onComplete = { results ->
                scope.launch {
                    coachRepository.saveAssessment(results)
                    coachRepository.saveSkillStates(
                        SkillStateEngine.initialStates(profile, results)
                    )
                    assessments = results
                    showGeneratedPlan = true
                }
            }
        )
        return
    }

    val assessmentMap = remember(assessments) { assessments.associate { it.metricId to it.value } }
    val assessedProfile = remember(profile, assessmentMap) {
        profile.copy(
            maxPushUps = assessmentMap["push_reps"]?.toInt() ?: profile.maxPushUps,
            maxPullUps = assessmentMap["pull_reps"]?.toInt() ?: profile.maxPullUps,
            maxDips = assessmentMap["dip_reps"]?.toInt() ?: profile.maxDips,
            hollowHoldSec = assessmentMap["hollow_hold"]?.toInt() ?: profile.hollowHoldSec
        )
    }

    if (showGeneratedPlan) {
        GeneratedPlanScreen(
            profile = assessedProfile,
            blueprint = ProgramEngine.build(assessedProfile),
            onStart = { showGeneratedPlan = false }
        )
        return
    }

    MainExperience(profile = assessedProfile)
}

@Composable
private fun MainExperience(profile: UserProfile) {
    val context = LocalContext.current
    val sessionRepo = remember { WorkoutSessionRepository(context) }
    val history by sessionRepo.history.collectAsStateWithLifecycle(initialValue = emptyList())
    val effortHistory = remember(history) { history.associate { item -> item.exerciseName to item.avgRpe } }
    val blueprint = remember(profile, effortHistory) { ProgramEngine.build(profile, effortHistory) }
    val nav = rememberNavController()
    val tabs = listOf(Tab.Today, Tab.Plan, Tab.Library, Tab.Progress)
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route.orEmpty()
    val showBottomBar = route in tabs.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    launchSingleTop = true
                                    popUpTo(Tab.Today.route) { saveState = true }
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tabIcon(tab), contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Tab.Today.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Today.route) {
                TodayScreen(
                    profile = profile,
                    effortHistory = effortHistory,
                    onStart = {
                        val firstPlan = blueprint.weeklyDays.firstOrNull { it.planId != null }?.planId
                            ?: blueprint.plans.first().id
                        nav.navigate("workout/$firstPlan")
                    },
                    onPlan = { nav.navigate(Tab.Plan.route) },
                    onLibrary = { nav.navigate(Tab.Library.route) },
                    onProfile = { nav.navigate("profile") }
                )
            }
            composable(Tab.Plan.route) {
                PlanScreen(profile = profile, effortHistory = effortHistory, onOpenWorkout = { nav.navigate("workout/${it.id}") })
            }
            composable(Tab.Library.route) { ExerciseLibraryScreen() }
            composable(Tab.Progress.route) { ProgressScreen() }
            composable("profile") {
                AthleteProfileScreen(
                    profile = profile,
                    history = history,
                    onClose = { nav.popBackStack() }
                )
            }
            composable(
                "workout/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("id")
                val plan = blueprint.plans.firstOrNull { it.id == id } ?: blueprint.plans.first()
                WorkoutScreen(plan = plan, onDone = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun TodayScreen(
    profile: UserProfile,
    effortHistory: Map<String, Double>,
    onStart: () -> Unit,
    onPlan: () -> Unit,
    onLibrary: () -> Unit,
    onProfile: () -> Unit
) {
    val blueprint = remember(profile, effortHistory) { ProgramEngine.build(profile, effortHistory) }
    val nextSession = blueprint.weeklyDays.firstOrNull { it.planId != null }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "خطة اليوم",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "تمرينك اليوم جاهز. ابدأ من حيث توقفت.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clickable(onClick = onProfile),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = "الملف الرياضي")
                    }
                }
            }
        }

        item {
            HeroWorkoutCard(
                title = nextSession?.title ?: "جلسة اليوم",
                subtitle = nextSession?.subtitle ?: blueprint.phaseTitle,
                minutes = profile.sessionMinutes,
                sessionCount = blueprint.weeklyDays.count { it.planId != null },
                level = profile.experience,
                onStart = onStart
            )
        }

        item {
            SectionHeader(
                title = "جدول هذا الأسبوع",
                action = "عرض الخطة",
                onAction = onPlan
            )
        }

        item { WeekOverview(blueprint.weeklyDays.map { it.dayNumber }.toSet()) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    value = "${blueprint.weeklyDays.count { it.planId != null }}",
                    label = "جلسات الأسبوع"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    value = "${profile.sessionMinutes}",
                    label = "دقيقة اليوم"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.TrendingUp,
                    value = "${(blueprint.progress * 100).toInt()}%",
                    label = "تقدم المرحلة"
                )
            }
        }

        item {
            SectionHeader(
                title = "راجع طريقة أداء التمارين",
                action = "المكتبة",
                onAction = onLibrary
            )
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLibrary),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${ExerciseVideoCatalog.all.size} فيديو تقني",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "شاهد طريقة الأداء الصحيحة داخل الجلسة أو من مكتبة التمارين.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun HeroWorkoutCard(
    title: String,
    subtitle: String,
    minutes: Int,
    sessionCount: Int,
    level: String,
    onStart: () -> Unit
) {
    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "اليوم · الجلسة 1 من $sessionCount",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.FitnessCenter, contentDescription = null)
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                InfoChip(Icons.Default.Schedule, "$minutes دقيقة")
                InfoChip(Icons.Default.FitnessCenter, "5 تمارين")
                InfoChip(Icons.Default.Speed, level)
            }

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ابدأ الجلسة", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WeekOverview(trainingDays: Set<Int>) {
    val labels = listOf("س", "ح", "ن", "ث", "ر", "خ", "ج")
    val currentTrainingDay = trainingDays.minOrNull()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        (1..7).forEach { dayNumber ->
            val isTraining = dayNumber in trainingDays
            val isCurrent = dayNumber == currentTrainingDay

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    labels[dayNumber - 1],
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = when {
                        isCurrent -> MaterialTheme.colorScheme.primary
                        isTraining -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isTraining && !isCurrent) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                dayNumber.toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent)
                                    MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanScreen(
    profile: UserProfile,
    effortHistory: Map<String, Double>,
    onOpenWorkout: (WorkoutPlan) -> Unit
) {
    val blueprint = remember(profile, effortHistory) { ProgramEngine.build(profile, effortHistory) }
    val dayNames = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
    val sessionsByDay = blueprint.weeklyDays.associateBy { it.dayNumber }
    val firstTrainingDay = blueprint.weeklyDays.firstOrNull { it.planId != null }?.dayNumber
    val weekDays = (1..7).map { dayNumber ->
        val session = sessionsByDay[dayNumber]
        if (session != null) {
            WeekDay(
                day = dayNames[dayNumber - 1],
                title = session.title,
                subtitle = session.subtitle,
                state = if (dayNumber == firstTrainingDay) DayState.Today else DayState.Upcoming,
                planId = session.planId
            )
        } else {
            WeekDay(
                day = dayNames[dayNumber - 1],
                title = "استشفاء / راحة",
                subtitle = "حركة خفيفة ونوم جيد",
                state = DayState.Rest
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "خطة التدريب",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "الأسبوع ${blueprint.week} من ${blueprint.totalWeeks} · ${blueprint.phaseTitle}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "تقدم المرحلة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text("${(blueprint.progress * 100).toInt()}%", fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { blueprint.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        blueprint.rationale,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                "جدول الأسبوع",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(weekDays) { item ->
            PlanDayCard(item = item) {
                val plan = blueprint.plans.firstOrNull { it.id == item.planId }
                plan?.let(onOpenWorkout)
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            Text(
                "كيف يتقدم البرنامج؟",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item { PhaseTimeline() }
    }
}

@Composable
private fun PlanDayCard(item: WeekDay, onClick: () -> Unit) {
    val interactive = item.planId != null
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (interactive) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = if (item.state == DayState.Today)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(16.dp),
                color = when (item.state) {
                    DayState.Done -> MaterialTheme.colorScheme.primaryContainer
                    DayState.Today -> MaterialTheme.colorScheme.primary
                    DayState.Rest -> MaterialTheme.colorScheme.surfaceVariant
                    DayState.Upcoming -> MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when (item.state) {
                            DayState.Done -> Icons.Default.Check
                            DayState.Today -> Icons.Default.PlayArrow
                            DayState.Rest -> Icons.Default.SelfImprovement
                            DayState.Upcoming -> Icons.Default.FitnessCenter
                        },
                        contentDescription = null
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (interactive) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PhaseTimeline() {
    val phases = listOf(
        "التأسيس" to "الأسبوع 1–2",
        "القوة" to "الأسبوع 3–4",
        "التحكم والمهارة" to "الأسبوع 5",
        "التقييم" to "الأسبوع 6"
    )

    Column {
        phases.forEachIndexed { index, phase ->
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = if (index == 0)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (index == 0) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text("${index + 1}", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    if (index < phases.lastIndex) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(52.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.padding(top = 2.dp)) {
                    Text(phase.first, fontWeight = FontWeight.Bold)
                    Text(
                        phase.second,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseLibraryScreen() {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("الكل") }
    var selected by remember { mutableStateOf<ExerciseVideo?>(null) }

    val categories = listOf("الكل", "صدر", "ظهر", "أرجل", "جذع", "كارديو", "مرونة")
    val categoryMap = mapOf(
        "صدر" to "Chest",
        "ظهر" to "Back",
        "أرجل" to "Legs",
        "جذع" to "Core",
        "كارديو" to "Cardio",
        "مرونة" to "Mobility"
    )

    val filtered = remember(query, category) {
        val q = query.trim().lowercase()
        ExerciseVideoCatalog.all.filter { video ->
            val matchesQuery = q.isBlank() ||
                video.nameEn.lowercase().contains(q) ||
                video.nameAr.contains(query.trim()) ||
                video.category.lowercase().contains(q)
            val matchesCategory = category == "الكل" || video.category == categoryMap[category]
            matchesQuery && matchesCategory
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "مكتبة التمارين",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${ExerciseVideoCatalog.all.size} فيديو تقني يعمل دون اتصال",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("ابحث عن تمرين أو عضلة") },
                shape = RoundedCornerShape(20.dp)
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = { category = item },
                        label = { Text(item) }
                    )
                }
            }
        }

        items(filtered, key = { it.key }) { video ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selected = video },
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            video.nameAr,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${video.nameEn} · ${video.category}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                }
            }
        }
    }

    selected?.let { video ->
        ModalBottomSheet(onDismissRequest = { selected = null }) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    video.nameAr,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(video.nameEn, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ExerciseVideoPlayer(
                    video = video,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(22.dp))
                )
                Text(
                    "شاهد الحركة كاملة قبل الأداء، وركز على الإيقاع والتحكم والمدى المناسب.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ExerciseVideoPlayer(
    video: ExerciseVideo,
    modifier: Modifier = Modifier,
    showControls: Boolean = false,
    autoPlay: Boolean = true
) {
    val context = LocalContext.current
    val uri = remember(video.resId, context.packageName) {
        Uri.parse("android.resource://${context.packageName}/${video.resId}")
    }

    val player = remember(video.resId) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            playWhenReady = autoPlay
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    LaunchedEffect(autoPlay) {
        player.playWhenReady = autoPlay
        if (autoPlay) player.play() else player.pause()
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = showControls
                this.player = player
                keepScreenOn = true
            }
        },
        update = { view ->
            view.useController = showControls
            view.player = player
        }
    )
}

@Composable
private fun ProgressScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val history by db.workoutDao().observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val skillStates by db.coachDao().observeSkillStates().collectAsStateWithLifecycle(initialValue = emptyList())
    val totalMinutes = history.sumOf { it.durationSec } / 60
    val masteredTracks = skillStates.count { state ->
        val track = SkillProgressionGraph.track(state.skillId)
        val node = SkillProgressionGraph.node(state.currentNodeId)
        track != null && node != null && node.level >= (track.nodes.maxOfOrNull { it.level } ?: Int.MAX_VALUE)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "تقدمي",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "تابع تطور القوة والمهارات، لا عدد الجلسات فقط.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    value = "${history.size}",
                    label = "جلسة"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    value = "$totalMinutes",
                    label = "دقيقة"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.EmojiEvents,
                    value = "$masteredTracks",
                    label = "مهارة مكتملة"
                )
            }
        }

        item {
            Text(
                "مسارات المهارات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "كل مسار يوضح مستواك الحالي والخطوة التالية المطلوبة.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (skillStates.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "أكمل تقييم البداية ليحدد التطبيق موقعك في مسارات المهارات.",
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(skillStates) { state ->
                val track = SkillProgressionGraph.track(state.skillId)
                val node = SkillProgressionGraph.node(state.currentNodeId)
                if (track != null && node != null) {
                    val maxLevel = track.nodes.maxOfOrNull { it.level } ?: node.level
                    val next = SkillProgressionGraph.next(node.id)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(track.titleAr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                    Text(
                                        "المستوى ${node.level} من $maxLevel · ${node.titleAr}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    "${((node.level.toFloat() / maxLevel.toFloat()) * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { node.level.toFloat() / maxLevel.toFloat() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                if (next != null)
                                    "الخطوة التالية: ${next.titleAr} · معيار الانتقال ${next.mastery.sets} مجموعات × ${next.mastery.target}"
                                else
                                    "أنت في أعلى مستوى معرف حاليًا لهذا المسار.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "سجل الجلسات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (history.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(34.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("أكمل أول جلسة ليظهر سجلك هنا.")
                    }
                }
            }
        }

        items(history) { item ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                ListItem(
                    headlineContent = { Text(item.title, fontWeight = FontWeight.Bold) },
                    supportingContent = {
                        Text("${item.completedExercises} تمارين · ${item.durationSec / 60} دقيقة")
                    },
                    leadingContent = {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutScreen(plan: WorkoutPlan, onDone: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).workoutDao() }
    val sessionRepo = remember { WorkoutSessionRepository(context) }
    val scope = rememberCoroutineScope()

    var loadedSession by remember { mutableStateOf(false) }
    var index by remember { mutableIntStateOf(0) }
    val completedSets = remember { mutableStateMapOf<Int, Int>() }
    val performances = remember { mutableStateListOf<SetPerformance>() }
    val substitutions = remember { mutableStateMapOf<Int, String>() }
    var startedAt by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var rest by remember { mutableIntStateOf(0) }
    var restOwner by remember { mutableIntStateOf(-1) }
    var repsInput by remember { mutableStateOf("8") }
    var rpeInput by remember { mutableStateOf("7") }
    var showSubstitute by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }

    LaunchedEffect(plan.id) {
        val saved = sessionRepo.activeSession.first()
        if (saved != null && saved.planId == plan.id) {
            index = saved.currentExerciseIndex.coerceIn(0, plan.exercises.lastIndex)
            completedSets.putAll(saved.completedSets)
            performances.addAll(saved.setPerformances)
            substitutions.putAll(saved.substitutions)
            startedAt = saved.startedAt
        } else {
            startedAt = System.currentTimeMillis()
            sessionRepo.saveSession(
                ActiveWorkoutSession(
                    planId = plan.id,
                    currentExerciseIndex = 0,
                    completedSets = emptyMap(),
                    setPerformances = emptyList(),
                    startedAt = startedAt
                )
            )
        }
        loadedSession = true
    }

    fun currentExerciseAt(i: Int): com.rushd.calisthenicscoach.domain.Exercise {
        val base = plan.exercises[i]
        val replacement = substitutions[i]?.let { name ->
            ExerciseVideoCatalog.all.firstOrNull { it.nameEn == name }
        }
        return if (replacement != null) {
            base.copy(name = replacement.nameEn, ArabicName = replacement.nameAr)
        } else base
    }

    fun nextIncomplete(after: Int = index): Int? {
        if (plan.exercises.isEmpty()) return null
        for (offset in 1..plan.exercises.size) {
            val candidate = (after + offset) % plan.exercises.size
            if ((completedSets[candidate] ?: 0) < plan.exercises[candidate].sets) return candidate
        }
        return null
    }

    fun persist(newIndex: Int = index) {
        if (!loadedSession) return
        scope.launch {
            sessionRepo.saveSession(
                ActiveWorkoutSession(
                    planId = plan.id,
                    currentExerciseIndex = newIndex,
                    completedSets = completedSets.toMap(),
                    setPerformances = performances.toList(),
                    startedAt = startedAt,
                    substitutions = substitutions.toMap()
                )
            )
        }
    }

    LaunchedEffect(rest) {
        if (rest > 0) {
            delay(1000)
            rest -= 1
        } else if (restOwner in plan.exercises.indices) {
            val owner = restOwner
            restOwner = -1
            val ownerFinished = (completedSets[owner] ?: 0) >= plan.exercises[owner].sets
            if (ownerFinished) {
                nextIncomplete(owner)?.let { next ->
                    index = next
                    repsInput = suggestedReps(currentExerciseAt(next).reps).toString()
                    rpeInput = "7"
                    persist(next)
                }
            }
        }
    }

    if (!loadedSession) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val exercise = currentExerciseAt(index)
    val doneSets = completedSets[index] ?: 0
    val video = remember(exercise.name) { ExerciseVideoCatalog.forExercise(exercise.name) }
    val totalSets = plan.exercises.sumOf { it.sets }
    val finishedSets = completedSets.values.sum()
    val allCompleted = plan.exercises.indices.all { i ->
        (completedSets[i] ?: 0) >= plan.exercises[i].sets
    }
    val durationSec = ((System.currentTimeMillis() - startedAt) / 1000L).toInt().coerceAtLeast(0)

    fun recordSet() {
        if (doneSets >= exercise.sets) return
        val reps = repsInput.toIntOrNull()?.coerceAtLeast(0) ?: suggestedReps(exercise.reps)
        val rpe = rpeInput.toIntOrNull()?.coerceIn(1, 10) ?: 7
        completedSets[index] = doneSets + 1
        performances.removeAll { it.exerciseName == exercise.name && it.setIndex == doneSets }
        performances.add(
            SetPerformance(
                exerciseIndex = index,
                exerciseName = exercise.name,
                setIndex = doneSets,
                reps = reps,
                rpe = rpe
            )
        )
        rest = exercise.restSec
        restOwner = index
        persist(index)
    }

    fun finishSession() {
        scope.launch {
            dao.insert(
                WorkoutHistory(
                    planId = plan.id,
                    title = plan.title,
                    completedAt = System.currentTimeMillis(),
                    durationSec = durationSec,
                    completedExercises = plan.exercises.indices.count { i ->
                        (completedSets[i] ?: 0) >= plan.exercises[i].sets
                    }
                )
            )
            sessionRepo.mergeHistory(performances.toList())
            sessionRepo.clearSession()
            showSummary = true
        }
    }

    if (showSummary) {
        WorkoutSummaryScreen(
            title = plan.title,
            durationSec = durationSec,
            performances = performances.toList(),
            completedExercises = plan.exercises.indices.count { i ->
                (completedSets[i] ?: 0) >= plan.exercises[i].sets
            },
            totalExercises = plan.exercises.size,
            onDone = onDone
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(plan.title, fontWeight = FontWeight.Bold)
                        Text(
                            "$finishedSets من $totalSets مجموعة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        persist(index)
                        onDone()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "حفظ والخروج")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Column(
                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rest > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("راحة $rest ث", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { rest = 0 }) { Text("تخطي") }
                            TextButton(onClick = { rest = (rest + 30).coerceAtMost(300) }) { Text("+30") }
                        }
                    }

                    if (!allCompleted && doneSets < exercise.sets) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CompactNumberField(
                                label = "التكرارات",
                                value = repsInput,
                                onValueChange = { repsInput = it },
                                modifier = Modifier.weight(1f),
                                maxDigits = 3
                            )
                            CompactNumberField(
                                label = "الجهد RPE",
                                value = rpeInput,
                                onValueChange = { rpeInput = it },
                                modifier = Modifier.weight(1f),
                                maxDigits = 2
                            )
                        }
                    }

                    when {
                        allCompleted -> {
                            Button(
                                onClick = { finishSession() },
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("إنهاء الجلسة وعرض الملخص", fontWeight = FontWeight.Bold)
                            }
                        }
                        doneSets >= exercise.sets -> {
                            val next = nextIncomplete(index)
                            Button(
                                onClick = {
                                    if (next != null) {
                                        rest = 0
                                        restOwner = -1
                                        index = next
                                        repsInput = suggestedReps(currentExerciseAt(next).reps).toString()
                                        rpeInput = "7"
                                        persist(next)
                                    }
                                },
                                enabled = next != null,
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            ) {
                                Text(
                                    if (next != null) "التمرين التالي · ${currentExerciseAt(next).ArabicName}"
                                    else "اكتملت الجلسة",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        }
                        else -> {
                            Button(
                                onClick = { recordSet() },
                                enabled = rest == 0,
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (rest > 0) "استراحة · $rest ث"
                                    else "تسجيل المجموعة ${doneSets + 1} من ${exercise.sets}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LinearProgressIndicator(
                    progress = {
                        if (totalSets == 0) 0f else finishedSets.toFloat() / totalSets.toFloat()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(plan.exercises.indices.toList()) { exerciseIndex ->
                        val item = currentExerciseAt(exerciseIndex)
                        val itemDone = completedSets[exerciseIndex] ?: 0
                        FilterChip(
                            selected = index == exerciseIndex,
                            onClick = {
                                index = exerciseIndex
                                repsInput = suggestedReps(item.reps).toString()
                                rpeInput = "7"
                                persist(exerciseIndex)
                            },
                            label = {
                                Text(
                                    "${exerciseIndex + 1}. ${item.ArabicName}  $itemDone/${plan.exercises[exerciseIndex].sets}",
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                if (itemDone >= plan.exercises[exerciseIndex].sets) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            exercise.ArabicName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "المجموعة ${doneSets + 1} من ${exercise.sets}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showSubstitute = true }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("استبدال")
                    }
                }
            }

            item {
                if (video != null) {
                    ExerciseVideoPlayer(
                        video = video,
                        modifier = Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(24.dp)),
                        showControls = false,
                        autoPlay = true
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill("${exercise.sets}", "مجموعات", Modifier.weight(1f))
                    StatPill(exercise.reps, "الهدف", Modifier.weight(1f))
                    StatPill("${exercise.restSec}", "راحة/ث", Modifier.weight(1f))
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("ملاحظة الأداء", fontWeight = FontWeight.Bold)
                            Text(exercise.cue, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Text("المجموعات المسجلة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                performances.filter { it.exerciseName == exercise.name }.sortedBy { it.setIndex }.forEach { item ->
                    ListItem(
                        headlineContent = { Text("المجموعة ${item.setIndex + 1}") },
                        supportingContent = { Text("${item.reps} تكرار · RPE ${item.rpe}") },
                        leadingContent = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                    )
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    if (showSubstitute) {
        val baseVideo = ExerciseVideoCatalog.forExercise(plan.exercises[index].name)
        val alternatives = ExerciseVideoCatalog.all.filter { alt ->
            alt.key != baseVideo?.key && (baseVideo == null || alt.category == baseVideo.category)
        }.take(12)

        ModalBottomSheet(onDismissRequest = { showSubstitute = false }) {
            Column(
                Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("استبدال ${exercise.ArabicName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "اختر حركة من الفئة نفسها. ستبقى المجموعات والراحة كما هي.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                alternatives.forEach { alt ->
                    ListItem(
                        modifier = Modifier.clickable {
                            substitutions[index] = alt.nameEn
                            completedSets[index] = 0
                            performances.removeAll { it.exerciseName == exercise.name }
                            repsInput = suggestedReps(plan.exercises[index].reps).toString()
                            persist(index)
                            showSubstitute = false
                        },
                        headlineContent = { Text(alt.nameAr, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(alt.nameEn) },
                        leadingContent = { Icon(Icons.Default.SwapHoriz, contentDescription = null) }
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CompactNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxDigits: Int = 3
) {
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            onValueChange(raw.filter { it.isDigit() }.take(maxDigits))
        },
        modifier = modifier,
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun WorkoutSummaryScreen(
    title: String,
    durationSec: Int,
    performances: List<SetPerformance>,
    completedExercises: Int,
    totalExercises: Int,
    onDone: () -> Unit
) {
    val totalReps = performances.sumOf { it.reps }
    val avgRpe = if (performances.isEmpty()) 0.0 else performances.map { it.rpe }.average()

    Scaffold(
        bottomBar = {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
            ) {
                Text("العودة إلى اليوم", fontWeight = FontWeight.Bold)
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
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Text("اكتملت الجلسة", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(Modifier.weight(1f), Icons.Default.Schedule, "${durationSec / 60}", "دقيقة")
                    MetricCard(Modifier.weight(1f), Icons.Default.FitnessCenter, "${performances.size}", "مجموعة")
                    MetricCard(Modifier.weight(1f), Icons.Default.Repeat, "$totalReps", "تكرار")
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ملخص الأداء", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("$completedExercises من $totalExercises تمارين مكتملة")
                        Text("متوسط الجهد RPE ${String.format("%.1f", avgRpe)}")
                    }
                }
            }
        }
    }
}

private fun suggestedReps(target: String): Int {
    val values = Regex("\\d+").findAll(target).mapNotNull { it.value.toIntOrNull() }.toList()
    return when {
        values.size >= 2 -> ((values[0] + values[1]) / 2).coerceAtLeast(1)
        values.size == 1 -> values[0].coerceAtLeast(1)
        else -> 8
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onAction) {
            Text(action)
            Icon(Icons.Default.ChevronLeft, contentDescription = null)
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun StatPill(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun tabIcon(tab: Tab) = when (tab) {
    Tab.Today -> Icons.Default.Home
    Tab.Plan -> Icons.Default.CalendarMonth
    Tab.Library -> Icons.Default.VideoLibrary
    Tab.Progress -> Icons.Default.Insights
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AthleteProfileScreen(
    profile: UserProfile,
    history: List<ExerciseHistoryStat>,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الملف الرياضي", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "بياناتك المحفوظة",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "يستخدم التطبيق هذه البيانات مع سجل التمرين لتعديل الخطة.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileLine("العمر", "${profile.age} سنة")
                        ProfileLine("الطول", "${profile.heightCm} سم")
                        ProfileLine("الوزن", "${profile.weightKg} كغ")
                        ProfileLine("النشاط", profile.activityLevel)
                        ProfileLine("الهدف", displayProfileValue(profile.goal))
                        ProfileLine("أيام التدريب", "${profile.daysPerWeek} أيام أسبوعيًا")
                        ProfileLine("مدة الجلسة", "${profile.sessionMinutes} دقيقة")
                    }
                }
            }
            item {
                Text(
                    "التكيف مع الأداء",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (history.isEmpty())
                        "سيبدأ التطبيق بتعديل حجم التدريب والراحة بعد تسجيل عدة مجموعات."
                    else
                        "تم تسجيل ${history.sumOf { it.totalSets }} مجموعة. تستخدم قيم RPE المسجلة لتخفيف أو زيادة حجم التدريب في الجلسات التالية.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (history.isNotEmpty()) {
                items(history.take(8)) { item ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        ListItem(
                            headlineContent = { Text(item.exerciseName) },
                            supportingContent = {
                                Text("أفضل تكرارات ${item.bestReps} · متوسط RPE ${String.format("%.1f", item.avgRpe)}")
                            },
                            leadingContent = {
                                Icon(Icons.Default.Insights, contentDescription = null)
                            }
                        )
                    }
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        "الحساب السحابي والمزامنة بين الأجهزة غير مفعّلين في هذه النسخة. بياناتك محفوظة محليًا على هذا الجهاز.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileLine(label: String, value: String) {
    Row {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold)
    }
}

private fun displayProfileValue(value: String): String = when (value) {
    "Pull-up" -> "العقلة"
    "Muscle-up" -> "المسل أب"
    "Handstand" -> "الوقوف على اليدين"
    "Front Lever" -> "الفرونت ليفر"
    "Planche" -> "البلانش"
    else -> value
}
