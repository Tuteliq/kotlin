package ai.tuteliq

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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

/**
 * Input for fraud/safety extended detection.
 */
data class DetectionInput(
    val content: String,
    val context: AnalysisContext? = null,
    val includeEvidence: Boolean = false,
    val externalId: String? = null,
    val customerId: String? = null,
    val metadata: Map<String, Any?>? = null
)

/**
 * Input for multi-endpoint analysis.
 */
data class AnalyseMultiInput(
    val content: String,
    val detections: List<Detection>,
    val context: AnalysisContext? = null,
    val includeEvidence: Boolean = false,
    val externalId: String? = null,
    val customerId: String? = null,
    val metadata: Map<String, Any?>? = null
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
    val language: String? = null,
    @SerialName("language_status") val languageStatus: String? = null,
    @SerialName("external_id") val externalId: String? = null,
    val metadata: JsonObject? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
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
    val language: String? = null,
    @SerialName("message_analysis") val messageAnalysis: List<MessageAnalysis>? = null,
    @SerialName("language_status") val languageStatus: String? = null,
    @SerialName("external_id") val externalId: String? = null,
    val metadata: JsonObject? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
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
    val language: String? = null,
    @SerialName("language_status") val languageStatus: String? = null,
    @SerialName("external_id") val externalId: String? = null,
    val metadata: JsonObject? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
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
    val metadata: Map<String, Any>?,
    val creditsUsed: Int? = null
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
    val metadata: JsonObject? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
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
    val metadata: JsonObject? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
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
    val metadata: JsonObject? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
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

// =============================================================================
// Detection Results (Fraud / Safety Extended / Multi)
// =============================================================================

/**
 * Per-message analysis from conversation-aware detection.
 */
@Serializable
data class MessageAnalysis(
    @SerialName("message_index") val messageIndex: Int,
    @SerialName("risk_score") val riskScore: Double,
    val flags: List<String>,
    val summary: String
)

/**
 * A detection category with tag, label, and confidence.
 */
@Serializable
data class DetectionCategory(
    val tag: String,
    val label: String,
    val confidence: Double
)

/**
 * Evidence supporting a detection result.
 */
@Serializable
data class DetectionEvidence(
    val text: String,
    val tactic: String,
    val weight: Double
)

/**
 * Age calibration applied to a detection.
 */
@Serializable
data class AgeCalibration(
    val applied: Boolean,
    @SerialName("age_group") val ageGroup: String? = null,
    val multiplier: Double? = null
)

/**
 * Result of a single detection endpoint.
 */
@Serializable
data class DetectionResult(
    val endpoint: String,
    val detected: Boolean,
    val severity: Double,
    val confidence: Double,
    @SerialName("risk_score") val riskScore: Double,
    val level: String,
    val categories: List<DetectionCategory>,
    @SerialName("recommended_action") val recommendedAction: String,
    val rationale: String,
    val language: String,
    @SerialName("language_status") val languageStatus: String,
    val evidence: List<DetectionEvidence>? = null,
    @SerialName("age_calibration") val ageCalibration: AgeCalibration? = null,
    @SerialName("message_analysis") val messageAnalysis: List<MessageAnalysis>? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null,
    @SerialName("processing_time_ms") val processingTimeMs: Double? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    val metadata: JsonObject? = null
)

/**
 * Summary of multi-endpoint analysis.
 */
@Serializable
data class AnalyseMultiSummary(
    @SerialName("total_endpoints") val totalEndpoints: Int,
    @SerialName("detected_count") val detectedCount: Int,
    @SerialName("highest_risk") val highestRisk: JsonObject,
    @SerialName("overall_risk_level") val overallRiskLevel: String
)

/**
 * Result of multi-endpoint analysis.
 */
@Serializable
data class AnalyseMultiResult(
    val results: List<DetectionResult>,
    val summary: AnalyseMultiSummary,
    @SerialName("cross_endpoint_modifier") val crossEndpointModifier: Double? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    val metadata: JsonObject? = null
)

// =============================================================================
// Video Analysis
// =============================================================================

/**
 * A safety finding from video frame analysis.
 */
@Serializable
data class VideoSafetyFinding(
    @SerialName("frame_index") val frameIndex: Int,
    val timestamp: Double,
    val description: String,
    val categories: List<String>,
    val severity: Double
)

/**
 * Result of video safety analysis.
 */
@Serializable
data class VideoAnalysisResult(
    @SerialName("file_id") val fileId: String? = null,
    @SerialName("frames_analyzed") val framesAnalyzed: Int,
    @SerialName("safety_findings") val safetyFindings: List<VideoSafetyFinding>,
    @SerialName("overall_risk_score") val overallRiskScore: Double,
    @SerialName("overall_severity") val overallSeverity: String,
    @SerialName("credits_used") val creditsUsed: Int? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    val metadata: JsonObject? = null
)

// =============================================================================
// Voice Analysis
// =============================================================================

/**
 * A segment of a voice transcription with timestamps.
 */
@Serializable
data class TranscriptionSegment(
    val start: Double,
    val end: Double,
    val text: String
)

/**
 * Result of voice transcription.
 */
@Serializable
data class TranscriptionResult(
    val text: String,
    val language: String? = null,
    val duration: Double? = null,
    val segments: List<TranscriptionSegment>? = null
)

/**
 * Result of voice safety analysis.
 */
@Serializable
data class VoiceAnalysisResult(
    @SerialName("file_id") val fileId: String? = null,
    val transcription: TranscriptionResult? = null,
    val analysis: JsonObject? = null,
    @SerialName("overall_risk_score") val overallRiskScore: Double? = null,
    @SerialName("overall_severity") val overallSeverity: String? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    val metadata: JsonObject? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
)

// =============================================================================
// Image Analysis
// =============================================================================

/**
 * Vision analysis details for an image.
 */
@Serializable
data class VisionResult(
    @SerialName("extracted_text") val extractedText: String? = null,
    @SerialName("visual_categories") val visualCategories: List<String>? = null,
    @SerialName("visual_severity") val visualSeverity: String? = null,
    @SerialName("visual_confidence") val visualConfidence: Double? = null,
    @SerialName("visual_description") val visualDescription: String? = null,
    @SerialName("contains_text") val containsText: Boolean? = null,
    @SerialName("contains_faces") val containsFaces: Boolean? = null
)

/**
 * Result of image safety analysis.
 */
@Serializable
data class ImageAnalysisResult(
    @SerialName("file_id") val fileId: String? = null,
    val vision: VisionResult? = null,
    @SerialName("text_analysis") val textAnalysis: JsonObject? = null,
    @SerialName("overall_risk_score") val overallRiskScore: Double? = null,
    @SerialName("overall_severity") val overallSeverity: String? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    val metadata: JsonObject? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
)

// =============================================================================
// Webhooks
// =============================================================================

/**
 * A webhook configuration.
 *
 * [active] maps from the API's `is_active` field.
 */
@Serializable
data class Webhook(
    val id: String,
    val url: String,
    val events: List<String> = emptyList(),
    @SerialName("is_active") val active: Boolean = true,
    @Deprecated("The API returns the secret only when a webhook is created or its secret is regenerated.")
    val secret: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val name: String? = null,
    @SerialName("failure_count") val failureCount: Int = 0,
    @SerialName("last_triggered_at") val lastTriggeredAt: String? = null,
    @SerialName("last_error") val lastError: String? = null
)

/**
 * Result of listing webhooks.
 */
@Serializable
data class WebhookListResult(val webhooks: List<Webhook>)

/**
 * Input for creating a webhook. A new webhook is always active.
 *
 * @property name A label for the webhook (1-100 characters).
 * @property url HTTPS endpoint that receives the events.
 * @property events Event types to deliver (1-5).
 * @property headers Extra headers sent with every delivery.
 */
data class CreateWebhookInput(
    val name: String,
    val url: String,
    val events: List<String>,
    val headers: Map<String, String>? = null
) {
    /**
     * Earlier form without a name. The API requires one, so the URL's host is used.
     * A new webhook is always active, so [active] is not sent.
     */
    @Deprecated("Pass a name: CreateWebhookInput(name, url, events).")
    constructor(url: String, events: List<String>, @Suppress("UNUSED_PARAMETER") active: Boolean = true) :
        this(name = hostOf(url), url = url, events = events)

    private companion object {
        fun hostOf(url: String): String =
            url.substringAfter("://").substringBefore('/').substringBefore('?').ifBlank { url }.take(100)
    }
}

/**
 * Result of creating a webhook. [secret] is returned only here; store it to verify deliveries.
 */
@Serializable
data class CreateWebhookResult(
    val id: String,
    val name: String? = null,
    val url: String,
    val secret: String,
    val events: List<String> = emptyList(),
    @SerialName("is_active") val active: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Input for updating a webhook. Only the fields you set are sent.
 *
 * @property active Turn deliveries on or off (sent as `is_active`). Turning a webhook
 * back on also clears its failure count.
 */
data class UpdateWebhookInput(
    val url: String? = null,
    val events: List<String>? = null,
    val active: Boolean? = null,
    val name: String? = null,
    val headers: Map<String, String>? = null
)

/**
 * Result of updating a webhook.
 */
@Serializable
data class UpdateWebhookResult(
    val id: String,
    val name: String? = null,
    val url: String,
    val events: List<String> = emptyList(),
    @SerialName("is_active") val active: Boolean = true,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Result of deleting a webhook.
 */
@Serializable
data class DeleteWebhookResult(
    val success: Boolean = true,
    val message: String? = null
)

/**
 * Result of testing a webhook.
 */
@Serializable
data class TestWebhookResult(
    val success: Boolean,
    @SerialName("status_code") val statusCode: Int? = null,
    @SerialName("latency_ms") val latencyMs: Double? = null,
    val error: String? = null
)

/**
 * Result of regenerating a webhook secret.
 */
@Serializable
data class RegenerateSecretResult(val secret: String)

// =============================================================================
// Pricing
// =============================================================================

/**
 * A pricing plan summary.
 */
@Serializable
data class PricingPlan(
    val name: String,
    val price: String,
    val messages: String,
    val features: List<String>
)

/**
 * Result of listing pricing plans.
 */
@Serializable
data class PricingResult(val plans: List<PricingPlan>)

/**
 * A detailed pricing plan.
 */
@Serializable
data class PricingDetailPlan(
    val name: String,
    val tier: String,
    val price: JsonObject,
    val limits: JsonObject,
    val features: JsonObject,
    val endpoints: List<String>
)

/**
 * Result of listing detailed pricing plans.
 */
@Serializable
data class PricingDetailsResult(val plans: List<PricingDetailPlan>)

// =============================================================================
// Usage
// =============================================================================

/**
 * Usage data for a single day.
 */
@Serializable
data class UsageDay(
    val date: String,
    @SerialName("total_requests") val totalRequests: Int,
    @SerialName("success_requests") val successRequests: Int,
    @SerialName("error_requests") val errorRequests: Int
)

/**
 * Result of usage history query.
 */
@Serializable
data class UsageHistoryResult(
    @SerialName("api_key_id") val apiKeyId: String,
    val days: List<UsageDay>
)

/**
 * Result of usage breakdown by tool.
 */
@Serializable
data class UsageByToolResult(
    val date: String,
    val tools: Map<String, Int>,
    val endpoints: Map<String, Int>
)

/**
 * Result of monthly usage summary.
 */
@Serializable
data class UsageMonthlyResult(
    val tier: String,
    @SerialName("tier_display_name") val tierDisplayName: String,
    val billing: JsonObject,
    val usage: JsonObject,
    @SerialName("rate_limit") val rateLimit: JsonObject,
    val recommendations: JsonObject? = null,
    val links: JsonObject
)

// =============================================================================
// Verification
// =============================================================================

/**
 * Input for creating a verification session.
 *
 * @property mode Age or identity verification.
 * @property documentType Document the user is asked for. Omit to let them choose.
 * @property chipPolicy How a passport or ID card chip read counts as document evidence. API default: [ChipPolicy.OPTIONAL].
 * @property autoCapture Whether the hosted page takes the document photo itself once the document is
 * framed, steady and sharp. API default: true. With false, the user taps Capture.
 */
data class CreateVerificationSessionInput(
    val mode: VerificationMode,
    val documentType: DocumentType? = null,
    val chipPolicy: ChipPolicy? = null,
    val autoCapture: Boolean? = null
)

/**
 * Verification session details.
 * The `url` property maps from the API's `mobile_url` field.
 *
 * @property expiresAt When the session expires, in epoch milliseconds.
 * @property recommendedImageWidth Capture width, in pixels, the API recommends for document and selfie images.
 */
@Serializable
data class VerificationSession(
    @SerialName("session_id") val sessionId: String,
    @SerialName("mobile_url") val url: String,
    @SerialName("expires_at") val expiresAt: Long,
    val mode: VerificationMode,
    /** The check the session rests on: `full` or `liveness_only`. See [VerificationCheck]. */
    @SerialName("verification_mode") val verificationMode: String? = null,
    @SerialName("chip_policy") val chipPolicy: ChipPolicy? = null,
    @SerialName("auto_capture") val autoCapture: Boolean? = null,
    @SerialName("recommended_image_width") val recommendedImageWidth: Int? = null
)

/**
 * Face match result from verification.
 *
 * @property matchScore How strongly the faces match (0.0-1.0): 0.5 at the match decision,
 * high for a clear match, near 0 for different people. Prefer it over [confidence].
 * @property confidence 1 minus the face distance. Kept for compatibility.
 */
@Serializable
data class FaceMatchResult(
    val matched: Boolean,
    val confidence: Double,
    val distance: Double? = null,
    @SerialName("match_score") val matchScore: Double? = null
)

/**
 * Liveness check result from verification.
 */
@Serializable
data class LivenessResult(
    val valid: Boolean,
    val reason: String? = null
)

/**
 * Estimated age range.
 */
@Serializable
data class AgeRange(
    val low: Double,
    val high: Double
)

/**
 * Result of age verification.
 */
@Serializable
data class AgeVerificationResult(
    @SerialName("verification_id") val verificationId: String,
    val status: VerificationStatus,
    @SerialName("age_bracket") val ageBracket: String? = null,
    @SerialName("is_minor") val isMinor: Boolean? = null,
    val liveness: LivenessResult? = null,
    @SerialName("face_match") val faceMatch: FaceMatchResult? = null,
    @SerialName("failure_reasons") val failureReasons: List<String>? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
)

/**
 * Result of identity verification.
 */
@Serializable
data class IdentityVerificationResult(
    @SerialName("verification_id") val verificationId: String,
    val status: VerificationStatus,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("document_type") val documentType: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val liveness: LivenessResult? = null,
    @SerialName("face_match") val faceMatch: FaceMatchResult? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
)

/**
 * The outcome of a verification session.
 */
@Serializable
data class VerificationResult(
    val status: VerificationStatus,
    val age: Double? = null,
    @SerialName("age_range") val ageRange: AgeRange? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("is_minor") val isMinor: Boolean? = null,
    @SerialName("document_evidence") val documentEvidence: DocumentEvidence? = null,
    val document: JsonObject? = null,
    @SerialName("face_match") val faceMatch: FaceMatchResult? = null,
    val liveness: LivenessResult? = null,
    @SerialName("failure_reasons") val failureReasons: List<String>? = null
)

/**
 * Result of polling a verification session.
 *
 * @property createdAt When the session was created, in epoch milliseconds.
 * @property expiresAt When the session expires, in epoch milliseconds.
 * @property capture Self-reported capture details from the device, when present.
 */
@Serializable
data class VerificationSessionResult(
    @SerialName("session_id") val sessionId: String,
    val status: VerificationSessionStatus,
    val result: VerificationResult? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("expires_at") val expiresAt: Long? = null,
    val mode: VerificationMode? = null,
    @SerialName("verification_mode") val verificationMode: VerificationCheck? = null,
    @SerialName("document_evidence") val documentEvidence: DocumentEvidence? = null,
    @SerialName("step_progress") val stepProgress: String? = null,
    @SerialName("chip_policy") val chipPolicy: ChipPolicy? = null,
    val capture: JsonObject? = null
)

/**
 * Result of retrieving an age verification by ID. No personal data is returned.
 */
@Serializable
data class VerificationRetrieveResult(
    @SerialName("verification_id") val verificationId: String,
    val status: VerificationStatus,
    @Deprecated("Not returned by the API.")
    @SerialName("age_bracket") val ageBracket: String? = null,
    @SerialName("is_minor") val isMinor: Boolean? = null,
    @Deprecated("Not returned by the API. Use livenessValid.", ReplaceWith("livenessValid"))
    val liveness: LivenessResult? = null,
    @Deprecated("Not returned by the API. Use faceMatched.", ReplaceWith("faceMatched"))
    @SerialName("face_match") val faceMatch: FaceMatchResult? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("face_matched") val faceMatched: Boolean? = null,
    @SerialName("liveness_valid") val livenessValid: Boolean? = null,
    @SerialName("failure_reasons") val failureReasons: List<String> = emptyList()
)

/**
 * Result of retrieving an identity verification by ID.
 */
@Serializable
data class IdentityRetrieveResult(
    @SerialName("verification_id") val verificationId: String,
    val status: VerificationStatus,
    @Deprecated("Not returned by the API.")
    @SerialName("full_name") val fullName: String? = null,
    @Deprecated("Not returned by the API.")
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @Deprecated("Not returned by the API.")
    @SerialName("document_type") val documentType: String? = null,
    @Deprecated("Not returned by the API.")
    @SerialName("country_code") val countryCode: String? = null,
    @Deprecated("Not returned by the API. Use livenessValid.", ReplaceWith("livenessValid"))
    val liveness: LivenessResult? = null,
    @Deprecated("Not returned by the API. Use faceMatched and faceConfidence.", ReplaceWith("faceMatched"))
    @SerialName("face_match") val faceMatch: FaceMatchResult? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("face_matched") val faceMatched: Boolean? = null,
    @SerialName("face_confidence") val faceConfidence: Double? = null,
    @SerialName("liveness_valid") val livenessValid: Boolean? = null,
    @SerialName("failure_reasons") val failureReasons: List<String> = emptyList()
)

/**
 * Result of retrieving any verification by ID (GET /api/v1/verify/{id}).
 */
@Serializable
data class VerificationRecord(
    @SerialName("verification_id") val verificationId: String,
    val status: VerificationStatus,
    val age: Double? = null,
    @SerialName("is_minor") val isMinor: Boolean? = null,
    @SerialName("ocr_confidence") val ocrConfidence: Double? = null,
    @SerialName("face_matched") val faceMatched: Boolean? = null,
    @SerialName("face_confidence") val faceConfidence: Double? = null,
    @SerialName("liveness_valid") val livenessValid: Boolean? = null,
    @SerialName("failure_reasons") val failureReasons: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Age verification configuration for a market.
 */
@Serializable
data class VerificationConfig(
    val mode: VerificationCheck,
    @SerialName("age_threshold") val ageThreshold: Int,
    val country: String? = null,
    val market: String? = null
)

/**
 * Result of cancelling a verification session.
 */
@Serializable
data class CancelVerificationSessionResult(
    val ok: Boolean = true,
    @Deprecated("Not returned by the API.")
    val message: String? = null
)

// =============================================================================
// Document Analysis
// =============================================================================

/**
 * Input for document analysis: a PDF, or a single png, jpeg or webp image.
 *
 * @property endpoints Detectors to run on each page, for example `unsafe`, `grooming`. Omit for the default set.
 * @property splitBySender Images only: split a messaging screenshot into messages with a side
 * attributed to each. Costs one additional vision call.
 * @property supportThreshold Minimum severity (`low`, `medium`, `high`, `critical`) to include crisis support resources.
 * @property contentType MIME type of the file. Defaults to the type for the filename's extension.
 */
data class AnalyzeDocumentInput(
    val file: ByteArray,
    val filename: String,
    val endpoints: List<String>? = null,
    val fileId: String? = null,
    val ageGroup: String? = null,
    val language: String? = null,
    val platform: String? = null,
    val supportThreshold: String? = null,
    val splitBySender: Boolean = false,
    val externalId: String? = null,
    val customerId: String? = null,
    val metadata: Map<String, Any?>? = null,
    val contentType: String? = null
)

/**
 * How text was extracted across the document's pages.
 */
@Serializable
data class DocumentExtractionSummary(
    @SerialName("text_layer_pages") val textLayerPages: Int = 0,
    @SerialName("ocr_pages") val ocrPages: Int = 0,
    @SerialName("failed_pages") val failedPages: Int = 0,
    @SerialName("average_ocr_confidence") val averageOcrConfidence: Double? = null
)

/**
 * Detection result for one endpoint on one page.
 */
@Serializable
data class DocumentPageEndpointResult(
    val endpoint: String,
    val detected: Boolean,
    val severity: Double = 0.0,
    val confidence: Double = 0.0,
    @SerialName("risk_score") val riskScore: Double = 0.0,
    val level: String? = null,
    val categories: List<JsonObject> = emptyList(),
    val evidence: List<JsonObject> = emptyList(),
    @SerialName("recommended_action") val recommendedAction: String? = null,
    val rationale: String? = null
)

/**
 * Analysis result for one page.
 */
@Serializable
data class DocumentPageResult(
    @SerialName("page_number") val pageNumber: Int,
    @SerialName("text_preview") val textPreview: String? = null,
    @SerialName("extraction_method") val extractionMethod: String? = null,
    @SerialName("ocr_confidence") val ocrConfidence: Double? = null,
    val results: List<DocumentPageEndpointResult> = emptyList(),
    @SerialName("page_risk_score") val pageRiskScore: Double = 0.0,
    @SerialName("page_severity") val pageSeverity: String? = null
)

/**
 * A page with a finding.
 */
@Serializable
data class DocumentFlaggedPage(
    @SerialName("page_number") val pageNumber: Int,
    @SerialName("risk_score") val riskScore: Double,
    val severity: String? = null,
    @SerialName("detected_endpoints") val detectedEndpoints: List<String> = emptyList()
)

/**
 * Result of document analysis.
 *
 * @property documentHash SHA-256 of the uploaded file, for chain of custody.
 */
@Serializable
data class DocumentAnalysisResult(
    @SerialName("file_id") val fileId: String? = null,
    @SerialName("document_hash") val documentHash: String? = null,
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("pages_analyzed") val pagesAnalyzed: Int = 0,
    @SerialName("extraction_summary") val extractionSummary: DocumentExtractionSummary? = null,
    @SerialName("page_results") val pageResults: List<DocumentPageResult> = emptyList(),
    @SerialName("overall_risk_score") val overallRiskScore: Double = 0.0,
    @SerialName("overall_severity") val overallSeverity: String? = null,
    @SerialName("detected_endpoints") val detectedEndpoints: List<String> = emptyList(),
    @SerialName("flagged_pages") val flaggedPages: List<DocumentFlaggedPage> = emptyList(),
    @SerialName("credits_used") val creditsUsed: Int? = null,
    @SerialName("processing_time_ms") val processingTimeMs: Double? = null,
    val language: String? = null,
    @SerialName("language_status") val languageStatus: String? = null,
    val support: JsonObject? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    val metadata: JsonObject? = null
)

// =============================================================================
// Incidents
// =============================================================================

/**
 * Filters for listing incidents. Every field is optional.
 *
 * @property from Start of the window, ISO 8601.
 * @property to End of the window, ISO 8601.
 * @property limit Page size.
 * @property cursor `nextCursor` from the previous page.
 * @property includeSummary Include each incident's summary in the list.
 */
data class ListIncidentsInput(
    val category: String? = null,
    val severity: String? = null,
    val status: String? = null,
    val source: String? = null,
    val from: String? = null,
    val to: String? = null,
    val platform: String? = null,
    val externalId: String? = null,
    val customerId: String? = null,
    val limit: Int? = null,
    val cursor: String? = null,
    val includeSummary: Boolean = false
)

/**
 * An incident as listed. Category, level, source and status are strings so new values never fail decoding.
 */
@Serializable
data class IncidentSummary(
    val id: String,
    @SerialName("risk_category") val riskCategory: String? = null,
    @SerialName("risk_level") val riskLevel: String? = null,
    @SerialName("confidence_score") val confidenceScore: Double? = null,
    @SerialName("detected_patterns") val detectedPatterns: List<String> = emptyList(),
    @SerialName("recommended_actions") val recommendedActions: List<String> = emptyList(),
    val platform: String? = null,
    val language: String? = null,
    val source: String? = null,
    @SerialName("file_id") val fileId: String? = null,
    val status: String? = null,
    @SerialName("policy_action") val policyAction: JsonObject? = null,
    @SerialName("recommended_status") val recommendedStatus: String? = null,
    @SerialName("is_test") val isTest: Boolean? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val summary: JsonElement? = null
)

/**
 * A page of incidents.
 */
@Serializable
data class IncidentListResult(
    val incidents: List<IncidentSummary> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("total_returned") val totalReturned: Int = 0
)

/**
 * Per-message scores in an incident.
 */
@Serializable
data class IncidentMessageAnalysis(
    @SerialName("message_index") val messageIndex: Int,
    @SerialName("risk_score") val riskScore: Double,
    val flags: List<String> = emptyList()
)

/**
 * Full incident detail.
 */
@Serializable
data class IncidentDetail(
    val id: String,
    @SerialName("risk_category") val riskCategory: String? = null,
    @SerialName("risk_level") val riskLevel: String? = null,
    @SerialName("confidence_score") val confidenceScore: Double? = null,
    @SerialName("detected_patterns") val detectedPatterns: List<String> = emptyList(),
    @SerialName("recommended_actions") val recommendedActions: List<String> = emptyList(),
    val platform: String? = null,
    val language: String? = null,
    val source: String? = null,
    @SerialName("file_id") val fileId: String? = null,
    val status: String? = null,
    @SerialName("policy_action") val policyAction: JsonObject? = null,
    @SerialName("recommended_status") val recommendedStatus: String? = null,
    @SerialName("is_test") val isTest: Boolean? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val summary: JsonElement? = null,
    val metadata: JsonElement? = null,
    @SerialName("source_data") val sourceData: JsonObject? = null,
    @SerialName("visual_categories") val visualCategories: List<String>? = null,
    @SerialName("visual_severity") val visualSeverity: String? = null,
    @SerialName("visual_confidence") val visualConfidence: Double? = null,
    @SerialName("emotional_indicators") val emotionalIndicators: List<String>? = null,
    val review: JsonObject? = null,
    @SerialName("message_analysis") val messageAnalysis: List<IncidentMessageAnalysis>? = null
)

/**
 * A start and end time, ISO 8601.
 */
@Serializable
data class Timeframe(val from: String, val to: String)

/**
 * Incident count for one platform.
 */
@Serializable
data class PlatformCount(val platform: String, val count: Int)

/**
 * Incident counts over a window.
 */
@Serializable
data class IncidentsOverview(
    val timeframe: Timeframe? = null,
    @SerialName("total_incidents") val totalIncidents: Int = 0,
    @SerialName("requires_review_count") val requiresReviewCount: Int = 0,
    @SerialName("last_24h_count") val last24hCount: Int = 0,
    @SerialName("last_7d_count") val last7dCount: Int = 0,
    @SerialName("last_30d_count") val last30dCount: Int = 0,
    @SerialName("counts_by_category") val countsByCategory: Map<String, Int> = emptyMap(),
    @SerialName("counts_by_severity") val countsBySeverity: Map<String, Int> = emptyMap(),
    @SerialName("counts_by_source") val countsBySource: Map<String, Int> = emptyMap(),
    @SerialName("counts_by_status") val countsByStatus: Map<String, Int> = emptyMap(),
    @SerialName("top_platforms") val topPlatforms: List<PlatformCount> = emptyList()
)

/**
 * Incident counts in one time bucket.
 */
@Serializable
data class IncidentTrendsBucket(
    @SerialName("bucket_start") val bucketStart: String,
    val total: Int = 0,
    @SerialName("by_severity") val bySeverity: Map<String, Int> = emptyMap()
)

/**
 * Incident counts over time.
 */
@Serializable
data class IncidentTrends(
    @SerialName("bucket_size") val bucketSize: String? = null,
    val timeframe: Timeframe? = null,
    val series: List<IncidentTrendsBucket> = emptyList()
)

/**
 * A moderator review of one incident.
 *
 * @property moderatorExternalId Your identifier for the moderator.
 * @property newRiskCategory Required when [action] is [ModeratorAction.RECLASSIFY].
 * @property newRiskLevel New level, for example `low`, `high`.
 */
data class ReviewIncidentInput(
    val action: ModeratorAction,
    val reasonCode: ModeratorReasonCode,
    val moderatorExternalId: String,
    val reasonComment: String? = null,
    val newRiskLevel: String? = null,
    val newRiskCategory: String? = null,
    val retentionClass: RetentionClass? = null
)

/**
 * Category and level of an incident.
 */
@Serializable
data class IncidentClassification(
    @SerialName("risk_category") val riskCategory: String? = null,
    @SerialName("risk_level") val riskLevel: String? = null
)

/**
 * Signed receipt for a moderator review.
 */
@Serializable
data class ReviewAuditReceipt(
    @SerialName("request_id") val requestId: String,
    val timestamp: String,
    val signature: String
)

/**
 * Result of reviewing an incident.
 */
@Serializable
data class ReviewIncidentResult(
    @SerialName("incident_id") val incidentId: String,
    val original: IncidentClassification? = null,
    val revised: IncidentClassification? = null,
    @SerialName("audit_receipt") val auditReceipt: ReviewAuditReceipt? = null
)

/**
 * The same moderator review applied to up to 100 incidents.
 */
data class BatchReviewIncidentsInput(
    val incidentIds: List<String>,
    val action: ModeratorAction,
    val reasonCode: ModeratorReasonCode,
    val moderatorExternalId: String,
    val reasonComment: String? = null,
    val newRiskLevel: String? = null,
    val newRiskCategory: String? = null,
    val retentionClass: RetentionClass? = null
)

/**
 * Outcome for one incident in a batch review.
 */
@Serializable
data class BatchReviewItemResult(
    @SerialName("incident_id") val incidentId: String,
    val ok: Boolean,
    val original: IncidentClassification? = null,
    val revised: IncidentClassification? = null,
    @SerialName("audit_receipt") val auditReceipt: ReviewAuditReceipt? = null,
    val error: String? = null
)

/**
 * Result of a batch review.
 */
@Serializable
data class BatchReviewIncidentsResult(
    val total: Int = 0,
    val succeeded: Int = 0,
    val failed: Int = 0,
    val results: List<BatchReviewItemResult> = emptyList()
)

// =============================================================================
// Nickname
// =============================================================================

/**
 * Input for nickname review.
 *
 * @property name The display name as typed (1-64 characters).
 * @property conversationType The setting the name is shown in, for example `gaming_chat`.
 * @property platformReserved Names reserved by your platform (up to 20).
 */
data class DetectNicknameInput(
    val name: String,
    val strictness: NicknameStrictness? = null,
    val language: String? = null,
    val ageGroup: String? = null,
    val conversationType: String? = null,
    val platformReserved: List<String>? = null
)

/**
 * How one part of a nickname was read.
 */
@Serializable
data class NicknamePart(
    val text: String,
    val reading: String,
    val category: String? = null
)

/**
 * Result of nickname review: a signal about the name, not a verdict on the person.
 *
 * @property signal `none`, `low` or `review`.
 * @property recommendation `allow`, `allow_and_watch`, `ask_to_rename` or `hold_for_review`.
 * @property readingComplete False when the reading did not finish; then nothing was analysed, so retry.
 */
@Serializable
data class NicknameResult(
    val signal: String,
    val categories: List<String> = emptyList(),
    val parts: List<NicknamePart> = emptyList(),
    val rationale: String? = null,
    val recommendation: String? = null,
    @SerialName("recommendation_detail") val recommendationDetail: String? = null,
    val confidence: Double = 0.0,
    val language: String? = null,
    val strictness: String? = null,
    @SerialName("reading_complete") val readingComplete: Boolean = true,
    @SerialName("credits_used") val creditsUsed: Int? = null
)

// =============================================================================
// Synthetic Content
// =============================================================================

/**
 * Input for synthetic text detection.
 */
data class DetectSyntheticTextInput(
    val content: String,
    val context: AnalysisContext? = null,
    val supportThreshold: String? = null,
    val externalId: String? = null,
    val customerId: String? = null,
    val metadata: Map<String, Any?>? = null
)

/**
 * Input for synthetic image, audio or video detection.
 *
 * @property maxFrames Video only: the most frames to analyse.
 * @property contentType MIME type of the file. Defaults to the type for the filename's extension.
 */
data class DetectSyntheticMediaInput(
    val file: ByteArray,
    val filename: String,
    val ageGroup: String? = null,
    val language: String? = null,
    val platform: String? = null,
    val fileId: String? = null,
    val maxFrames: Int? = null,
    val externalId: String? = null,
    val customerId: String? = null,
    val metadata: Map<String, Any?>? = null,
    val contentType: String? = null
)

/**
 * Result of synthetic content detection.
 *
 * @property classification `confirmed_synthetic`, `suspected_synthetic`, `unknown` or `confirmed_authentic`.
 * @property inputType `image`, `audio` or `video` for media; absent for text.
 * The media-specific sections are kept as JSON so new fields never fail decoding.
 */
@Serializable
data class SyntheticContentResult(
    val endpoint: String? = null,
    val detected: Boolean,
    val severity: Double = 0.0,
    val level: String? = null,
    val classification: String? = null,
    val confidence: Double = 0.0,
    @SerialName("risk_score") val riskScore: Double = 0.0,
    val categories: List<JsonObject> = emptyList(),
    val evidence: List<JsonObject>? = null,
    @SerialName("recommended_action") val recommendedAction: String? = null,
    @SerialName("action_detail") val actionDetail: String? = null,
    val rationale: String? = null,
    @SerialName("input_type") val inputType: String? = null,
    @SerialName("semantic_analysis") val semanticAnalysis: JsonObject? = null,
    @SerialName("metadata_analysis") val metadataAnalysis: JsonObject? = null,
    val provenance: JsonObject? = null,
    @SerialName("forensic_signals") val forensicSignals: JsonObject? = null,
    val transcription: JsonObject? = null,
    @SerialName("audio_stats") val audioStats: JsonObject? = null,
    val video: JsonObject? = null,
    @SerialName("processing_time_ms") val processingTimeMs: Double? = null,
    val language: String? = null,
    @SerialName("language_status") val languageStatus: String? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null,
    val support: JsonObject? = null,
    @SerialName("file_id") val fileId: String? = null,
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    val metadata: JsonObject? = null
)

// =============================================================================
// Image Match
// =============================================================================

/**
 * Input for verifyImageMatch.
 *
 * @property image The image to check (raw bytes; sent base64-encoded).
 * @property referenceImage A reference face to match against. Without it, face_match does not run.
 * @property checks Checks to run. Omit for all that apply.
 */
data class VerifyImageMatchInput(
    val image: ByteArray,
    val referenceImage: ByteArray? = null,
    val checks: List<ImageMatchCheck>? = null,
    val externalId: String? = null,
    val customerId: String? = null,
    val metadata: Map<String, Any?>? = null
)

/**
 * Face match between the image and the reference.
 *
 * @property matchScore How strongly the faces match (0.0-1.0): 0.5 at the match decision.
 * @property reason Why matching failed, for example `no_face_detected`.
 */
@Serializable
data class ImageFaceMatch(
    val matched: Boolean,
    val similarity: Double? = null,
    @SerialName("match_score") val matchScore: Double? = null,
    val reason: String? = null
)

/**
 * Estimated age of the detected face.
 */
@Serializable
data class ImageAgeEstimate(
    @SerialName("estimated_age") val estimatedAge: Double,
    val confidence: Double
)

/**
 * Result of verifyImageMatch. Sections whose check did not run are null.
 */
@Serializable
data class VerifyImageMatchResult(
    @SerialName("schema_version") val schemaVersion: String? = null,
    @SerialName("checks_run") val checksRun: List<String> = emptyList(),
    @SerialName("face_match") val faceMatch: ImageFaceMatch? = null,
    val age: ImageAgeEstimate? = null,
    val forensic: JsonObject? = null,
    val synthetic: JsonObject? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null,
    @SerialName("processing_ms") val processingMs: Double? = null
)

// =============================================================================
// Usage summary and quota
// =============================================================================

/**
 * Request counts for a day.
 */
@Serializable
data class UsageCounts(
    val totalRequests: Int = 0,
    val successRequests: Int = 0,
    val errorRequests: Int = 0
)

/**
 * Quota for a key. -1 means no limit.
 */
@Serializable
data class UsageSummaryQuota(
    val requestsPerMinute: Int = 0,
    val requestsPerMonth: Int = 0,
    val requestsPerDay: Int = 0,
    val remainingToday: Int = 0
)

/**
 * Daily usage summary for the API key.
 */
@Serializable
data class UsageSummaryResult(
    val apiKeyId: String? = null,
    val tier: String? = null,
    val date: String? = null,
    val usage: UsageCounts = UsageCounts(),
    val quota: UsageSummaryQuota = UsageSummaryQuota(),
    @SerialName("messages_used") val messagesUsed: Int? = null,
    @SerialName("message_limit") val messageLimit: Int? = null,
    @SerialName("purchased_credits") val purchasedCredits: Int? = null,
    @SerialName("total_available") val totalAvailable: Int? = null,
    @SerialName("usage_percentage") val usagePercentage: Double? = null
)

/**
 * Request limits. -1 means no limit.
 */
@Serializable
data class QuotaLimits(
    val requestsPerMinute: Int = 0,
    val requestsPerMonth: Int = 0,
    val requestsPerDay: Int = 0
)

/**
 * Request counts in the current minute and day.
 */
@Serializable
data class QuotaCounts(
    val requestsThisMinute: Int = 0,
    val requestsToday: Int = 0
)

/**
 * Rate limit and quota state for the API key.
 */
@Serializable
data class UsageQuotaResult(
    val apiKeyId: String? = null,
    val tier: String? = null,
    val limits: QuotaLimits = QuotaLimits(),
    val current: QuotaCounts = QuotaCounts(),
    val remaining: QuotaCounts = QuotaCounts(),
    val resetsInSeconds: Int? = null
)

// =============================================================================
// Privacy Redaction
// =============================================================================

/**
 * Input for redaction. Set exactly one of [text] or [messages].
 *
 * @property messages The messages of one conversation, in order. The same value gets the
 * same placeholder in every message.
 * @property strictAnonymisation Also removes age, school year, gender, nationality, everyday
 * places and health. Use it only for text shared after analysis.
 */
data class RedactInput(
    val text: String? = null,
    val messages: List<String>? = null,
    val language: String? = null,
    val mode: RedactMode? = null,
    val strictAnonymisation: Boolean? = null
) {
    init {
        require((text == null) != (messages == null)) { "Set exactly one of text or messages" }
    }
}

/**
 * Result of redaction. Nothing is stored.
 *
 * @property counts Replaced occurrences per type.
 * @property modelApplied True when full name and place recognition ran.
 */
@Serializable
data class RedactResult(
    val text: String? = null,
    val messages: List<String>? = null,
    val counts: Map<String, Int> = emptyMap(),
    @SerialName("entities_total") val entitiesTotal: Int = 0,
    val mode: String? = null,
    @SerialName("strict_anonymisation") val strictAnonymisation: Boolean = false,
    @SerialName("model_applied") val modelApplied: Boolean? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null
)
