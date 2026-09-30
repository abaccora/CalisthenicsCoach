package com.rushd.calisthenicscoach.domain

enum class CoachDecision {
    MAINTAIN,
    INCREASE_REPS,
    INCREASE_VOLUME,
    PROGRESS_VARIATION,
    REGRESS_VARIATION,
    DELOAD
}

data class PerformanceSample(
    val measurement: MeasurementType,
    val value: Int,
    val rpe: Int?,
    val completed: Boolean = true
)

data class SessionPerformance(
    val nodeId: String,
    val samples: List<PerformanceSample>,
    val perceivedDifficulty: String? = null,
    val painReported: Boolean = false,
    val completedAt: Long = System.currentTimeMillis()
)

data class CoachRecommendation(
    val decision: CoachDecision,
    val reasonAr: String,
    val currentNode: ProgressionNode,
    val targetNode: ProgressionNode? = null,
    val volumeMultiplier: Double = 1.0,
    val restAdjustmentSec: Int = 0
)

object ProgressionEngine {

    fun evaluate(
        nodeId: String,
        recentSessions: List<SessionPerformance>
    ): CoachRecommendation {
        val node = SkillGraphs.node(nodeId)
            ?: error("Unknown progression node: $nodeId")

        val recent = recentSessions
            .filter { it.nodeId == nodeId }
            .sortedByDescending { it.completedAt }
            .take(6)

        if (recent.isEmpty()) {
            return CoachRecommendation(
                decision = CoachDecision.MAINTAIN,
                reasonAr = "نحتاج أولًا إلى بيانات فعلية من جلساتك قبل تعديل مستوى الحركة.",
                currentNode = node
            )
        }

        if (recent.any { it.painReported }) {
            return CoachRecommendation(
                decision = CoachDecision.REGRESS_VARIATION,
                reasonAr = "تم الإبلاغ عن ألم أو انزعاج؛ الأفضل الرجوع إلى نسخة أقل تطلبًا حتى إعادة التقييم.",
                currentNode = node,
                targetNode = SkillGraphs.previous(node.id),
                volumeMultiplier = 0.75,
                restAdjustmentSec = 20
            )
        }

        val lastThree = recent.take(3)
        val completionRate = lastThree.map { session ->
            masteryCompletion(
                samples = session.samples,
                measurement = node.measurementType,
                rule = node.masteryRule
            )
        }.average()

        val avgRpe = lastThree
            .flatMap { it.samples }
            .filter { it.measurement == node.measurementType }
            .mapNotNull { it.rpe }
            .takeIf { it.isNotEmpty() }
            ?.average()

        val successfulSessions = recent.count { session ->
            isMasteredSession(
                samples = session.samples,
                measurement = node.measurementType,
                rule = node.masteryRule
            )
        }

        val recentDifficulty = lastThree.mapNotNull { it.perceivedDifficulty }
        val hardCount = recentDifficulty.count { it == "صعبة جدًا" }
        val easyCount = recentDifficulty.count { it == "سهلة" }

        if (hardCount >= 2 && completionRate < 0.9) {
            return CoachRecommendation(
                decision = CoachDecision.DELOAD,
                reasonAr = "صنفت أكثر من جلسة أخيرة بأنها صعبة جدًا ولم يكتمل الهدف بثبات؛ سنخفض الحمل مؤقتًا.",
                currentNode = node,
                volumeMultiplier = 0.75,
                restAdjustmentSec = 20
            )
        }

        if (successfulSessions >= node.masteryRule.successfulSessionsRequired) {
            val next = SkillGraphs.next(node.id)
            if (next != null) {
                return CoachRecommendation(
                    decision = CoachDecision.PROGRESS_VARIATION,
                    reasonAr = "حققت معيار الإتقان في أكثر من جلسة بجودة مناسبة؛ حان وقت الانتقال إلى النسخة التالية.",
                    currentNode = node,
                    targetNode = next
                )
            }
        }

        if (avgRpe != null && avgRpe >= 9.2 && completionRate < 0.8) {
            return CoachRecommendation(
                decision = CoachDecision.DELOAD,
                reasonAr = "الشدة مرتفعة والهدف لم يكتمل في الجلسات الأخيرة؛ سنخفف الحجم مؤقتًا ونزيد الراحة.",
                currentNode = node,
                volumeMultiplier = 0.7,
                restAdjustmentSec = 30
            )
        }

        if (completionRate < 0.55) {
            val previous = SkillGraphs.previous(node.id)
            return CoachRecommendation(
                decision = if (previous != null) CoachDecision.REGRESS_VARIATION else CoachDecision.MAINTAIN,
                reasonAr = if (previous != null)
                    "الهدف الحالي ما يزال أعلى من القدرة المستقرة؛ سنرجع خطوة لبناء قاعدة أقوى."
                else
                    "سنثبت المستوى الحالي ونخفض حجم العمل حتى يتحسن التنفيذ.",
                currentNode = node,
                targetNode = previous,
                volumeMultiplier = 0.8,
                restAdjustmentSec = 20
            )
        }

        if (
            completionRate >= 0.95 &&
            (avgRpe == null || avgRpe <= 7.0) &&
            (easyCount >= 1 || recentDifficulty.isEmpty())
        ) {
            return CoachRecommendation(
                decision = CoachDecision.INCREASE_VOLUME,
                reasonAr = "أنهيت العمل المطلوب بسهولة نسبية؛ سنزيد الحمل تدريجيًا قبل الانتقال إلى نسخة أصعب.",
                currentNode = node,
                volumeMultiplier = 1.15
            )
        }

        if (completionRate >= 0.85 && (avgRpe == null || avgRpe <= 8.0)) {
            return CoachRecommendation(
                decision = CoachDecision.INCREASE_REPS,
                reasonAr = "المستوى مناسب ومستقر؛ سنرفع التكرارات أو مدة الثبات تدريجيًا.",
                currentNode = node
            )
        }

        return CoachRecommendation(
            decision = CoachDecision.MAINTAIN,
            reasonAr = "المستوى الحالي مناسب الآن؛ سنحافظ عليه حتى يصبح الأداء أكثر استقرارًا.",
            currentNode = node
        )
    }

    private fun masteryCompletion(
        samples: List<PerformanceSample>,
        measurement: MeasurementType,
        rule: MasteryRule
    ): Double {
        val matching = samples
            .filter { it.completed && it.measurement == measurement }
            .take(rule.minSets)

        if (matching.isEmpty()) return 0.0

        val achieved = matching.sumOf { sample ->
            minOf(sample.value.toDouble() / rule.targetValue.toDouble(), 1.0)
        }
        val setCoverage = matching.size.toDouble() / rule.minSets.toDouble()
        return (achieved / matching.size.toDouble()) * setCoverage
    }

    private fun isMasteredSession(
        samples: List<PerformanceSample>,
        measurement: MeasurementType,
        rule: MasteryRule
    ): Boolean {
        val matching = samples
            .filter { it.completed && it.measurement == measurement }
            .take(rule.minSets)

        if (matching.size < rule.minSets) return false

        return matching.all { sample ->
            sample.value >= rule.targetValue &&
                (sample.rpe == null || sample.rpe <= rule.maxRpe)
        }
    }
}
