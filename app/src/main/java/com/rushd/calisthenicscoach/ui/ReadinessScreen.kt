package com.rushd.calisthenicscoach.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.rushd.calisthenicscoach.R
import androidx.compose.ui.unit.dp
import com.rushd.calisthenicscoach.domain.DailyReadiness
import com.rushd.calisthenicscoach.domain.EnergyLevel
import com.rushd.calisthenicscoach.domain.UserProfile
import com.rushd.calisthenicscoach.domain.ArabicTerminology

@Composable
fun ReadinessScreen(
    profile: UserProfile,
    onStart: (DailyReadiness) -> Unit,
    onCancel: () -> Unit
) {
    var energy by remember { mutableStateOf(EnergyLevel.NORMAL) }
    var minutes by remember { mutableIntStateOf(profile.sessionMinutes) }
    var equipment by remember { mutableStateOf(profile.equipment) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.readiness_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "سيضبط التطبيق جلسة اليوم فقط مع بقاء البرنامج الأساسي كما هو.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onCancel) { Text(stringResource(R.string.readiness_cancel)) }
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    onStart(
                        DailyReadiness(
                            energy = energy,
                            minutesAvailable = minutes,
                            equipmentAvailable = equipment
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
            ) {
                Text(stringResource(R.string.readiness_prepare), fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("كيف هي جاهزيتك اليوم؟", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    EnergyLevel.HIGH to "ممتاز",
                    EnergyLevel.NORMAL to "جيد",
                    EnergyLevel.LOW to "مرهق"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = energy == value,
                        onClick = { energy = value },
                        label = { Text(label) },
                        leadingIcon = if (energy == value) {
                            { Icon(Icons.Default.Bolt, contentDescription = null) }
                        } else null
                    )
                }
            }

            Text("كم لديك من الوقت؟", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(20, 30, 45, 60)) { value ->
                    FilterChip(
                        selected = minutes == value,
                        onClick = { minutes = value },
                        label = { Text("$value دقيقة") },
                        leadingIcon = if (minutes == value) {
                            { Icon(Icons.Default.Schedule, contentDescription = null) }
                        } else null
                    )
                }
            }

            if (profile.equipment.isNotEmpty()) {
                Text("ما الأدوات المتاحة لك الآن؟", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                profile.equipment.forEach { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            equipment = if (item in equipment) equipment - item else equipment + item
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item in equipment,
                                onCheckedChange = {
                                    equipment = if (item in equipment) equipment - item else equipment + item
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(readinessEquipmentAr(item), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    when {
                        energy == EnergyLevel.LOW ->
                            "سيخفّض التطبيق عدد المجموعات قليلًا ويزيد فترات الراحة اليوم."
                        minutes < profile.sessionMinutes ->
                            "سيختصر التطبيق الجلسة مع الحفاظ على التمارين الأعلى أولوية."
                        equipment != profile.equipment ->
                            "سيستبدل التطبيق الحركات التي تتطلب أدوات غير متاحة اليوم."
                        else ->
                            "جلسة اليوم مطابقة تقريبًا للبرنامج الأساسي."
                    },
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private fun readinessEquipmentAr(value: String): String = ArabicTerminology.equipment(value)
