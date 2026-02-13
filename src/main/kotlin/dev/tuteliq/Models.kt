package dev.tuteliq

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// =============================================================================
// Context
// =============================================================================

/**
 * Optional context for analysis.
 */
@Serializable
data class AnalysisContext(
    val language: String? = null,
    @SerialName("age_group") val ageGroup: String? = null,
    val relationship: String? = null,
    val platform: String? = null
)

// =============================================================================
// Messages
// =============================================================================

/**
 * Message for grooming detection.
 */
@Serializable
data class GroomingMessage(
    val role: MessageRole,
    val content: String
)

/**
 * Message for emotion analysis.
 */
@Serializable
data class EmotionMessage(
    val sender: String,
    val content: String
)

/**
 * Message for incident reports.
 */
@Serializable
data class ReportMessage(
    val sender: String,
    val content: String
)

// =============================================================================
// Input Types
// =============================================================================

/**
 * Input for bullying detection.
 */
data class DetectBullyingInput(
    val content: String,
    val context: AnalysisContext? = null,
    val externalId: String? = null,
    val metadata: Map<String, Any>? = null
)

/**
 * Input for grooming detection.
 */
data class DetectGroomingInput(
    val messages: List<GroomingMessage>,
    val childAge: Int? = null,
    val context: AnalysisContext? = null,
    val externalId: String? = null,
    val metadata: Map<String, Any>? = null
)

/**
 * Input for unsafe content detection.
 */
data class DetectUnsafeInput(
    val content: String,
    val context: AnalysisContext? = null,
    val externalId: String? = null,
    val metadata: Map<String, Any>? = null
)

/**
 * Input for quick analysis.
 */
data class AnalyzeInput(
    val content: String,
    val context: AnalysisContext? = null,
    val include: List<String>? = null,
    val externalId: String? = null,
    val metadata: Map<String, Any>? = null
)

/**
 * Input for emotion analysis.
 */
data class AnalyzeEmotionsInput(
    val content: String? = null,
    val messages: List<EmotionMessage>? = null,
    val context: AnalysisContext? = null,
    val externalId: String? = null,
    val metadata: Map<String, Any>? = null
)

/**
 * Input for action plan generation.
 */
data class GetActionPlanInput(
    val situation: String,
    val childAge: Int? = null,
    val audience: Audience? = null,
    val severity: Severity? = null,
    val externalId: String? = null,
    val metadata: Map<String, Any>? = null
)

/**
 * Input for incident report generation.
 */
data class GenerateReportInput(
    val messages: List<ReportMessage>,
    val childAge: Int? = null,
    val incidentType: String? = null,
    val externalId: String? = null,
    val metadata: Map<String, Any>? = null
)

// =============================================================================
// Result Types
// =============================================================================

/**
 * Result of bullying detection.
 */
@Serializable
data class BullyingResult(
    @SerialName("is_bullying") val isBullying: Boolean,
    val severity: Severity,
    @SerialName("bullying_type") val bullyingType: List<String>,
    val confidence: Double,
    val rationale: String,
    @SerialName("risk_score") val riskScore: Double,
    @SerialName("recommended_action") val recommendedAction: String,
    @SerialName("external_id") val externalId: String? = null,
    val metadata: JsonObject? = null
)

/**
 * Result of grooming detection.
 */
@Serializable
data class GroomingResult(
    @SerialName("grooming_risk") val groomingRisk: GroomingRisk,
    val flags: List<String>,
    val confidence: Double,
    val rationale: String,
    @SerialName("risk_score") val riskScore: Double,
    @SerialName("recommended_action") val recommendedAction: String,
    @SerialName("external_id") val externalId: String? = null,
    val metadata: JsonObject? = null
)

/**
 * Result of unsafe content detection.
 */
@Serializable
data class UnsafeResult(
    val unsafe: Boolean,
    val categories: List<String>,
    val severity: Severity,
    val confidence: Double,
    val rationale: String,
    @SerialName("risk_score") val riskScore: Double,
    @SerialName("recommended_action") val recommendedAction: String,
    @SerialName("external_id") val externalId: String? = null,
    val metadata: JsonObject? = null
)

/**
 * Result of quick analysis.
 */
data class AnalyzeResult(
    val riskLevel: RiskLevel,
    val riskScore: Double,
    val summary: String,
    val bullying: BullyingResult?,
    val unsafe: UnsafeResult?,
    val recommendedAction: String,
    val externalId: String?,
    val metadata: Map<String, Any>?
)

/**
 * Result of emotion analysis.
 */
@Serializable
data class EmotionsResult(
    @SerialName("dominant_emotions") val dominantEmotions: List<String>,
    val trend: EmotionTrend,
    val intensity: Double,
    @SerialName("concerning_patterns") val concerningPatterns: List<String>,
    @SerialName("recommended_followup") val recommendedFollowup: String,
    @SerialName("external_id") val externalId: String? = null,
    val metadata: JsonObject? = null
)

/**
 * Result of action plan generation.
 */
@Serializable
data class ActionPlanResult(
    val steps: List<String>,
    val tone: String,
    val resources: List<String>,
    val urgency: String,
    @SerialName("external_id") val externalId: String? = null,
    val metadata: JsonObject? = null
)

/**
 * Result of incident report generation.
 */
@Serializable
data class ReportResult(
    val summary: String,
    @SerialName("risk_level") val riskLevel: RiskLevel,
    val timeline: List<String>,
    @SerialName("key_evidence") val keyEvidence: List<String>,
    @SerialName("recommended_next_steps") val recommendedNextSteps: List<String>,
    @SerialName("external_id") val externalId: String? = null,
    val metadata: JsonObject? = null
)

// =============================================================================
// Account Management (GDPR)
// =============================================================================

/**
 * Result of account data deletion (GDPR Article 17).
 */
@Serializable
data class AccountDeletionResult(
    val message: String,
    @SerialName("deleted_count") val deletedCount: Int
)

/**
 * Result of account data export (GDPR Article 20).
 */
@Serializable
data class AccountExportResult(
    val userId: String,
    val exportedAt: String,
    val data: JsonObject
)

// =============================================================================
// Consent Management (GDPR Article 7)
// =============================================================================

@Serializable
enum class ConsentType {
    @SerialName("data_processing") DATA_PROCESSING,
    @SerialName("analytics") ANALYTICS,
    @SerialName("marketing") MARKETING,
    @SerialName("third_party_sharing") THIRD_PARTY_SHARING,
    @SerialName("child_safety_monitoring") CHILD_SAFETY_MONITORING
}

@Serializable
enum class ConsentStatus {
    @SerialName("granted") GRANTED,
    @SerialName("withdrawn") WITHDRAWN
}

data class RecordConsentInput(
    val consentType: ConsentType,
    val version: String
)

@Serializable
data class ConsentRecord(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("consent_type") val consentType: String,
    val status: String,
    val version: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class ConsentActionResult(
    val message: String,
    val consent: ConsentRecord
)

@Serializable
data class ConsentStatusResult(
    val consents: List<ConsentRecord>
)

// =============================================================================
// Right to Rectification (GDPR Article 16)
// =============================================================================

data class RectifyDataInput(
    val collection: String,
    val documentId: String,
    val fields: Map<String, Any?>
)

@Serializable
data class RectifyDataResult(
    val message: String,
    @SerialName("updated_fields") val updatedFields: List<String>
)

// =============================================================================
// Audit Logs (GDPR Article 15)
// =============================================================================

@Serializable
enum class AuditAction {
    @SerialName("data_access") DATA_ACCESS,
    @SerialName("data_export") DATA_EXPORT,
    @SerialName("data_deletion") DATA_DELETION,
    @SerialName("data_rectification") DATA_RECTIFICATION,
    @SerialName("consent_granted") CONSENT_GRANTED,
    @SerialName("consent_withdrawn") CONSENT_WITHDRAWN,
    @SerialName("breach_notification") BREACH_NOTIFICATION
}

@Serializable
data class AuditLogEntry(
    val id: String,
    @SerialName("user_id") val userId: String,
    val action: String,
    val details: JsonObject? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class AuditLogsResult(
    @SerialName("audit_logs") val auditLogs: List<AuditLogEntry>
)

/**
 * API usage information.
 */
data class Usage(
    val limit: Int,
    val used: Int,
    val remaining: Int
)

// =============================================================================
// Breach Management (GDPR Article 33/34)
// =============================================================================

@Serializable
enum class BreachSeverity {
    @SerialName("low") LOW,
    @SerialName("medium") MEDIUM,
    @SerialName("high") HIGH,
    @SerialName("critical") CRITICAL
}

@Serializable
enum class BreachStatus {
    @SerialName("detected") DETECTED,
    @SerialName("investigating") INVESTIGATING,
    @SerialName("contained") CONTAINED,
    @SerialName("reported") REPORTED,
    @SerialName("resolved") RESOLVED
}

@Serializable
enum class BreachNotificationStatus {
    @SerialName("pending") PENDING,
    @SerialName("users_notified") USERS_NOTIFIED,
    @SerialName("dpa_notified") DPA_NOTIFIED,
    @SerialName("completed") COMPLETED
}

data class LogBreachInput(
    val title: String,
    val description: String,
    val severity: BreachSeverity,
    val affectedUserIds: List<String>,
    val dataCategories: List<String>,
    val reportedBy: String
)

data class UpdateBreachInput(
    val status: BreachStatus,
    val notificationStatus: BreachNotificationStatus? = null,
    val notes: String? = null
)

@Serializable
data class BreachRecord(
    val id: String,
    val title: String,
    val description: String,
    val severity: BreachSeverity,
    val status: BreachStatus,
    @SerialName("notification_status") val notificationStatus: BreachNotificationStatus,
    @SerialName("affected_user_ids") val affectedUserIds: List<String>,
    @SerialName("data_categories") val dataCategories: List<String>,
    @SerialName("reported_by") val reportedBy: String,
    @SerialName("notification_deadline") val notificationDeadline: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class LogBreachResult(
    val message: String,
    val breach: BreachRecord
)

@Serializable
data class BreachListResult(
    val breaches: List<BreachRecord>
)

@Serializable
data class BreachResult(
    val breach: BreachRecord
)
