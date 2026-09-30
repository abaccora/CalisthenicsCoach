package com.rushd.calisthenicscoach.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rushd.calisthenicscoach.R
import com.rushd.calisthenicscoach.data.SetLogEntity
import com.rushd.calisthenicscoach.data.SetPerformance
import com.rushd.calisthenicscoach.domain.Exercise
import com.rushd.calisthenicscoach.domain.MeasurementType

@Composable
fun WorkoutActionPanel(
    restSeconds: Int,
    allCompleted: Boolean,
    doneSets: Int,
    exercise: Exercise,
    nextExerciseName: String?,
    repsInput: String,
    rpeInput: String,
    onRepsInputChange: (String) -> Unit,
    onRpeInputChange: (String) -> Unit,
    onSkipRest: () -> Unit,
    onAddRest: () -> Unit,
    onRecordSet: () -> Unit,
    onNextExercise: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (restSeconds > 0) {
                RestPanel(
                    seconds = restSeconds,
                    nextExerciseName = nextExerciseName,
                    onSkip = onSkipRest,
                    onAdd = onAddRest
                )
                return@Column
            }

            when {
                allCompleted -> {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.weight(0.04f))
                        Text(
                            stringResource(R.string.workout_finish),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                doneSets >= exercise.sets -> {
                    Button(
                        onClick = onNextExercise,
                        enabled = nextExerciseName != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            nextExerciseName?.let {
                                stringResource(R.string.workout_next_named, it)
                            } ?: stringResource(R.string.workout_complete),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }

                else -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactNumberField(
                            label = when (exercise.measurementType) {
                                MeasurementType.HOLD_SECONDS ->
                                    stringResource(R.string.workout_hold_seconds)
                                MeasurementType.LEFT_RIGHT_REPS,
                                MeasurementType.UNILATERAL_REPS ->
                                    stringResource(R.string.workout_per_side)
                                else -> stringResource(R.string.workout_reps)
                            },
                            value = repsInput,
                            onValueChange = onRepsInputChange,
                            modifier = Modifier.weight(1f),
                            maxDigits = 3
                        )
                        CompactNumberField(
                            label = stringResource(R.string.workout_rpe),
                            value = rpeInput,
                            onValueChange = onRpeInputChange,
                            modifier = Modifier.weight(1f),
                            maxDigits = 2
                        )
                    }

                    Button(
                        onClick = onRecordSet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.weight(0.04f))
                        Text(
                            stringResource(
                                R.string.workout_record_set,
                                doneSets + 1,
                                exercise.sets
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RestPanel(
    seconds: Int,
    nextExerciseName: String?,
    onSkip: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Timer, contentDescription = null)
        Spacer(Modifier.weight(0.04f))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.workout_rest_seconds, seconds),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            if (nextExerciseName != null) {
                Text(
                    stringResource(R.string.workout_next_preview, nextExerciseName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        TextButton(onClick = onAdd) {
            Text(stringResource(R.string.workout_add_30))
        }
        TextButton(onClick = onSkip) {
            Text(stringResource(R.string.workout_skip_rest))
        }
    }
}

@Composable
fun PreviousPerformanceComparison(
    previous: List<SetLogEntity>,
    current: List<SetPerformance>,
    targetSets: Int,
    measurementType: MeasurementType,
    modifier: Modifier = Modifier
) {
    if (previous.isEmpty() && current.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                stringResource(R.string.workout_previous_performance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.workout_set_short),
                    modifier = Modifier.weight(0.45f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.workout_previous_short),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.workout_today_short),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider()

            repeat(targetSets) { setIndex ->
                val before = previous.firstOrNull { it.setIndex == setIndex }
                val now = current.firstOrNull { it.setIndex == setIndex }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${setIndex + 1}",
                        modifier = Modifier.weight(0.45f)
                    )
                    Text(
                        formatPrevious(before, measurementType),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatCurrent(now, measurementType),
                        modifier = Modifier.weight(1f),
                        fontWeight = if (now != null) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun formatPrevious(
    item: SetLogEntity?,
    measurementType: MeasurementType
): String {
    if (item == null) return "—"
    return when (measurementType) {
        MeasurementType.HOLD_SECONDS ->
            stringResource(R.string.workout_seconds_value, item.holdSeconds ?: item.reps ?: 0)
        MeasurementType.LEFT_RIGHT_REPS,
        MeasurementType.UNILATERAL_REPS ->
            stringResource(
                R.string.workout_side_value,
                minOf(item.leftReps ?: item.reps ?: 0, item.rightReps ?: item.reps ?: 0)
            )
        else ->
            stringResource(R.string.workout_reps_value, item.reps ?: 0)
    }
}

@Composable
private fun formatCurrent(
    item: SetPerformance?,
    measurementType: MeasurementType
): String {
    if (item == null) return "—"
    return when (measurementType) {
        MeasurementType.HOLD_SECONDS ->
            stringResource(R.string.workout_seconds_value, item.holdSeconds ?: item.reps)
        MeasurementType.LEFT_RIGHT_REPS,
        MeasurementType.UNILATERAL_REPS ->
            stringResource(
                R.string.workout_side_value,
                minOf(item.leftReps ?: item.reps, item.rightReps ?: item.reps)
            )
        else ->
            stringResource(R.string.workout_reps_value, item.reps)
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
