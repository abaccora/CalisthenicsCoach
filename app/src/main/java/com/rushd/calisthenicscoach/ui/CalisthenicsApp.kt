package com.rushd.calisthenicscoach.ui

import android.net.Uri
import android.os.SystemClock
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.rushd.calisthenicscoach.data.AppDatabase
import com.rushd.calisthenicscoach.data.WorkoutHistory
import com.rushd.calisthenicscoach.domain.ExerciseVideo
import com.rushd.calisthenicscoach.domain.ExerciseVideoCatalog
import com.rushd.calisthenicscoach.domain.SamplePrograms
import com.rushd.calisthenicscoach.domain.WorkoutPlan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed class Tab(val route: String, val label: String) {
    data object Home : Tab("home", "الرئيسية")
    data object Programs : Tab("programs", "البرامج")
    data object Library : Tab("library", "التمارين")
    data object Skills : Tab("skills", "المهارات")
    data object Progress : Tab("progress", "التقدم")
}

@Composable
fun CalisthenicsApp() {
    val nav = rememberNavController()
    val tabs = listOf(Tab.Home, Tab.Programs, Tab.Library, Tab.Skills, Tab.Progress)
    Scaffold(
        bottomBar = {
            NavigationBar {
                val entry by nav.currentBackStackEntryAsState()
                val route = entry?.destination?.route
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                launchSingleTop = true
                                popUpTo(Tab.Home.route) { saveState = true }
                                restoreState = true
                            }
                        },
                        icon = { Icon(tabIcon(tab), contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = Tab.Home.route, modifier = Modifier.padding(padding)) {
            composable(Tab.Home.route) { HomeScreen(onStart = { nav.navigate("workout/${SamplePrograms.foundation.id}") }) }
            composable(Tab.Programs.route) { ProgramsScreen(onOpen = { nav.navigate("workout/${it.id}") }) }
            composable(Tab.Library.route) { ExerciseLibraryScreen() }
            composable(Tab.Skills.route) { SkillsScreen() }
            composable(Tab.Progress.route) { ProgressScreen() }
            composable("workout/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStack ->
                val id = backStack.arguments?.getString("id")
                val plan = listOf(SamplePrograms.foundation, SamplePrograms.strength)
                    .firstOrNull { it.id == id } ?: SamplePrograms.foundation
                WorkoutScreen(plan = plan, onDone = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun HomeScreen(onStart: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Calisthenics Coach", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("تدريب واضح، تدريجي، وقابل للقياس مع فيديوهات تقنية تعمل دون اتصال.", style = MaterialTheme.typography.bodyLarge)
        }
        item {
            ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("جلسة اليوم", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${SamplePrograms.foundation.title} · ${SamplePrograms.foundation.durationMin} دقيقة · ${SamplePrograms.foundation.level}")
                    LinearProgressIndicator(progress = { 0.35f }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("ابدأ التمرين") }
                }
            }
        }
        item {
            ElevatedCard {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("مكتبة فيديو محلية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${ExerciseVideoCatalog.all.size} فيديوًا لتمارين وزن الجسم، الكارديو، والإطالات؛ تعمل من داخل التطبيق دون الحاجة إلى Google Drive أثناء التدريب.")
                }
            }
        }
        item {
            Text("مبدأ التقدم", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("أكمل الحد الأعلى من التكرارات بتقنية نظيفة في جميع المجموعات مرتين، ثم انتقل إلى نسخة أصعب أو أضف مقاومة بسيطة.")
        }
    }
}

@Composable
private fun ProgramsScreen(onOpen: (WorkoutPlan) -> Unit) {
    val plans = listOf(SamplePrograms.foundation, SamplePrograms.strength)
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("البرامج", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        items(plans) { plan ->
            ElevatedCard(onClick = { onOpen(plan) }) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(plan.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(plan.subtitle)
                    Text("${plan.level} · ${plan.durationMin} دقيقة · ${plan.exercises.size} تمارين", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ExerciseLibraryScreen() {
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<ExerciseVideo?>(null) }
    val filtered = remember(query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) ExerciseVideoCatalog.all
        else ExerciseVideoCatalog.all.filter {
            it.nameEn.lowercase().contains(q) || it.nameAr.contains(query.trim()) || it.category.lowercase().contains(q)
        }
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("مكتبة التمارين", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("${ExerciseVideoCatalog.all.size} فيديوًا محليًا")
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("ابحث عن تمرين") }
            )
        }
        items(filtered, key = { it.key }) { video ->
            ElevatedCard(onClick = { selected = video }) {
                ListItem(
                    headlineContent = { Text(video.nameAr, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("${video.nameEn} · ${video.category}") },
                    leadingContent = { Icon(Icons.Default.PlayCircle, contentDescription = null) }
                )
            }
        }
    }

    selected?.let { video ->
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("إغلاق") } },
            title = { Column { Text(video.nameAr); Text(video.nameEn, style = MaterialTheme.typography.bodyMedium) } },
            text = { ExerciseVideoPlayer(video = video, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) }
        )
    }
}

@Composable
private fun ExerciseVideoPlayer(video: ExerciseVideo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uri = remember(video.resId) { Uri.parse("android.resource://${context.packageName}/${video.resId}") }
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
private fun SkillsScreen() {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("المهارات", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        items(SamplePrograms.skills) { skill ->
            ElevatedCard {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(skill.name, fontWeight = FontWeight.Bold)
                        Text("${skill.progress}%")
                    }
                    LinearProgressIndicator(progress = { skill.progress / 100f }, modifier = Modifier.fillMaxWidth())
                    Text("الخطوة التالية: ${skill.nextStep}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ProgressScreen() {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).workoutDao() }
    val history by dao.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val totalMinutes = history.sumOf { it.durationSec } / 60
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("التقدم", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("${history.size} جلسات · $totalMinutes دقيقة تدريب")
        }
        if (history.isEmpty()) item { Text("أكمل أول جلسة وسيظهر سجلك هنا.") }
        items(history) { item ->
            ListItem(
                headlineContent = { Text(item.title) },
                supportingContent = { Text("${item.completedExercises} تمارين · ${item.durationSec / 60} دقيقة") },
                leadingContent = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
            )
            HorizontalDivider()
        }
    }
}

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
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LinearProgressIndicator(progress = { (index + 1f) / plan.exercises.size }, modifier = Modifier.fillMaxWidth())
        Text(plan.title, style = MaterialTheme.typography.titleMedium)
        Text(exercise.ArabicName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
        video?.let { ExerciseVideoPlayer(video = it, modifier = Modifier.fillMaxWidth().height(190.dp)) }
        ElevatedCard {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${exercise.sets} مجموعات × ${exercise.reps}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(exercise.cue)
                Text("الراحة: ${exercise.restSec} ثانية")
            }
        }
        if (rest > 0) {
            Text("راحة  $rest ث", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { rest = 0 }, modifier = Modifier.fillMaxWidth()) { Text("تخطي الراحة") }
        } else {
            Button(onClick = { rest = exercise.restSec }, modifier = Modifier.fillMaxWidth()) { Text("ابدأ الراحة") }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                if (index < plan.exercises.lastIndex) index += 1 else {
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
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text(if (index < plan.exercises.lastIndex) "تم · التالي" else "إنهاء وحفظ الجلسة") }
    }
}

private fun tabIcon(tab: Tab) = when (tab) {
    Tab.Home -> Icons.Default.Home
    Tab.Programs -> Icons.Default.FitnessCenter
    Tab.Library -> Icons.Default.VideoLibrary
    Tab.Skills -> Icons.Default.EmojiEvents
    Tab.Progress -> Icons.Default.ShowChart
}
