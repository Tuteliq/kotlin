package dev.tuteliq

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Severity level for detected issues.
 */
@Serializable
enum class Severity {
    @SerialName("low") LOW,
    @SerialName("medium") MEDIUM,
    @SerialName("high") HIGH,
    @SerialName("critical") CRITICAL
}

/**
 * Risk level for grooming detection.
 */
@Serializable
enum class GroomingRisk {
    @SerialName("none") NONE,
    @SerialName("low") LOW,
    @SerialName("medium") MEDIUM,
    @SerialName("high") HIGH,
    @SerialName("critical") CRITICAL
}

/**
 * Overall risk level for content analysis.
 */
@Serializable
enum class RiskLevel {
    @SerialName("safe") SAFE,
    @SerialName("low") LOW,
    @SerialName("medium") MEDIUM,
    @SerialName("high") HIGH,
    @SerialName("critical") CRITICAL
}

/**
 * Emotion trend direction.
 */
@Serializable
enum class EmotionTrend {
    @SerialName("improving") IMPROVING,
    @SerialName("stable") STABLE,
    @SerialName("worsening") WORSENING
}

/**
 * Target audience for action plans.
 */
@Serializable
enum class Audience {
    @SerialName("child") CHILD,
    @SerialName("parent") PARENT,
    @SerialName("educator") EDUCATOR,
    @SerialName("platform") PLATFORM
}

/**
 * Role of message sender in grooming detection.
 */
@Serializable
enum class MessageRole {
    @SerialName("adult") ADULT,
    @SerialName("child") CHILD,
    @SerialName("unknown") UNKNOWN
}
