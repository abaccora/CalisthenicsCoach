package com.rushd.calisthenicscoach.ui

import android.net.Uri
import android.os.SystemClock
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.rushd.calisthenicscoach.data.AppDatabase
import com.rushd.calisthenicscoach.data.WorkoutHistory
import com.rushd.calisthenicscoach.data.UserPreferencesRepository
import com.rushd.calisthenicscoach.domain.ExerciseVideo
import com.rushd.calisthenicscoach.domain.ExerciseVideoCatalog
import com.rushd.calisthenicscoach.domain.SamplePrograms
import com.rushd.calisthenicscoach.domain.ProgramEngine
import com.rushd.calisthenicscoach.domain.UserProfile
import com.rushd.calisthenicscoach.domain.WorkoutPlan
import kotlinx.coroutines.delay
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
    val profile by repository.profile.collectAsStateWithLifecycle(initialValue = UserProfile())
    val scope = rememberCoroutineScope()

    if (profile?.onboardingCompleted != true) {
        OnboardingScreen(
            onComplete = { completed ->
                scope.launch { repository.save(completed) }
            }
        )
        return
    }

    MainExperience(profile = profile!!)
}

@Composable
private fun MainExperience(profile: UserProfile) {
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
                    onStart = { nav.navigate("workout/${ProgramEngine.build(profile).weeklyDays.firstOrNull { it.planId != null }?.planId ?: SamplePrograms.foundation.id}") },
                    onPlan = { nav.navigate(Tab.Plan.route) },
                    onLibrary = { nav.navigate(Tab.Library.route) }
                )
            }
            composable(Tab.Plan.route) {
                PlanScreen(profile = profile, onOpenWorkout = { nav.navigate("workout/${it.id}") })
            }
            composable(Tab.Library.route) { ExerciseLibraryScreen() }
            composable(Tab.Progress.route) { ProgressScreen() }
            composable(
                "workout/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("id")
                val plan = listOf(SamplePrograms.foundation, SamplePrograms.strength)
                    .firstOrNull { it.id == id } ?: SamplePrograms.foundation
                WorkoutScreen(plan = plan, onDone = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun TodayScreen(
    profile: UserProfile,
    onStart: () -> Unit,
    onPlan: () -> Unit,
    onLibrary: () -> Unit
) {
    val blueprint = remember(profile) { ProgramEngine.build(profile) }
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
                        "جلسة واضحة. تقدم محسوب.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null)
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
                title = "أين أنت في الأسبوع؟",
                action = "الخطة كاملة",
                onAction = onPlan
            )
        }

        item { WeekOverview() }

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
                title = "تعلم الحركة قبل تكرارها",
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
                            "الفيديو الصحيح يظهر داخل الجلسة نفسها، ويمكن فتح المكتبة في أي وقت.",
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
private fun WeekOverview() {
    val days = listOf(
        Triple("س", "1", DayState.Done),
        Triple("ح", "2", DayState.Rest),
        Triple("ن", "3", DayState.Today),
        Triple("ث", "4", DayState.Rest),
        Triple("ر", "5", DayState.Upcoming),
        Triple("خ", "6", DayState.Rest),
        Triple("ج", "7", DayState.Rest)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (day, number, state) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = when (state) {
                        DayState.Done -> MaterialTheme.colorScheme.primaryContainer
                        DayState.Today -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (state == DayState.Done) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        } else {
                            Text(
                                number,
                                fontWeight = FontWeight.Bold,
                                color = if (state == DayState.Today)
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
private fun PlanScreen(profile: UserProfile, onOpenWorkout: (WorkoutPlan) -> Unit) {
    val blueprint = remember(profile) { ProgramEngine.build(profile) }
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
                val plan = when (item.planId) {
                    SamplePrograms.foundation.id -> SamplePrograms.foundation
                    SamplePrograms.strength.id -> SamplePrograms.strength
                    else -> null
                }
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
private fun ExerciseVideoPlayer(video: ExerciseVideo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uri = remember(video.resId) {
        Uri.parse("android.resource://${context.packageName}/${video.resId}")
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            VideoView(ctx).apply {
                val controller = MediaController(ctx)
                controller.setAnchorView(this)
                setMediaController(controller)
                setVideoURI(uri)
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    seekTo(1)
                }
            }
        },
        update = { view ->
            if (view.tag != uri.toString()) {
                view.tag = uri.toString()
                view.setVideoURI(uri)
                view.seekTo(1)
            }
        }
    )
}

@Composable
private fun ProgressScreen() {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).workoutDao() }
    val history by dao.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val totalMinutes = history.sumOf { it.durationSec } / 60

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
                "الاستمرارية والتقنية أهم من السرعة.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    value = "${history.size}",
                    label = "جلسات"
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
                    value = "4",
                    label = "مهارات"
                )
            }
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
                    Text(
                        "تقدم المهارات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    SamplePrograms.skills.forEach { skill ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text(skill.name, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                Text("${skill.progress}%")
                            }
                            LinearProgressIndicator(
                                progress = { skill.progress / 100f },
                                modifier = Modifier.fillMaxWidth()
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
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )
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
    val scope = rememberCoroutineScope()
    var index by remember { mutableIntStateOf(0) }
    var rest by remember { mutableIntStateOf(0) }
    val started = remember { SystemClock.elapsedRealtime() }

    LaunchedEffect(rest) {
        if (rest > 0) {
            delay(1000)
            rest -= 1
        }
    }

    val exercise = plan.exercises[index]
    val video = remember(exercise.name) { ExerciseVideoCatalog.forExercise(exercise.name) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(plan.title, fontWeight = FontWeight.Bold)
                        Text(
                            "التمرين ${index + 1} من ${plan.exercises.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        if (index < plan.exercises.lastIndex) {
                            index += 1
                            rest = 0
                        } else {
                            val seconds = ((SystemClock.elapsedRealtime() - started) / 1000).toInt()
                            scope.launch {
                                dao.insert(
                                    WorkoutHistory(
                                        planId = plan.id,
                                        title = plan.title,
                                        completedAt = System.currentTimeMillis(),
                                        durationSec = seconds,
                                        completedExercises = plan.exercises.size
                                    )
                                )
                                onDone()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                ) {
                    Text(
                        if (index < plan.exercises.lastIndex) "تم · التالي"
                        else "إنهاء وحفظ الجلسة",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LinearProgressIndicator(
                    progress = { (index + 1f) / plan.exercises.size },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        exercise.ArabicName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        exercise.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            video?.let {
                item {
                    ExerciseVideoPlayer(
                        video = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(26.dp))
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill("${exercise.sets}", "مجموعات", Modifier.weight(1f))
                    StatPill(exercise.reps, "تكرارات", Modifier.weight(1f))
                    StatPill("${exercise.restSec}", "راحة/ث", Modifier.weight(1f))
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        Modifier.padding(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("مفتاح الأداء", fontWeight = FontWeight.Bold)
                            Text(
                                exercise.cue,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                if (rest > 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(
                            Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "وقت الراحة",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "$rest",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            TextButton(onClick = { rest = 0 }) {
                                Text("تخطي")
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { rest = exercise.restSec },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("ابدأ مؤقت الراحة")
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
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
