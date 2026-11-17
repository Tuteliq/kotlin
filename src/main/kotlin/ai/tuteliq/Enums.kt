package ai.tuteliq

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

/**
 * Supported languages for analysis. English is stable; the others are beta.
 */
@Serializable
enum class Language(@Suppress("unused") val code: String) {
    @SerialName("en") EN("en"),
    @SerialName("es") ES("es"),
    @SerialName("pt") PT("pt"),
    @SerialName("uk") UK("uk"),
    @SerialName("sv") SV("sv"),
    @SerialName("no") NO("no"),
    @SerialName("da") DA("da"),
    @SerialName("fi") FI("fi"),
    @SerialName("de") DE("de"),
    @SerialName("fr") FR("fr"),
    @SerialName("nl") NL("nl"),
    @SerialName("pl") PL("pl"),
    @SerialName("it") IT("it"),
    @SerialName("tr") TR("tr"),
    @SerialName("ro") RO("ro"),
    @SerialName("el") EL("el"),
    @SerialName("cs") CS("cs"),
    @SerialName("hu") HU("hu"),
    @SerialName("bg") BG("bg"),
    @SerialName("hr") HR("hr"),
    @SerialName("sk") SK("sk"),
    @SerialName("lt") LT("lt"),
    @SerialName("lv") LV("lv"),
    @SerialName("et") ET("et"),
    @SerialName("sl") SL("sl"),
    @SerialName("mt") MT("mt"),
    @SerialName("ga") GA("ga"),
    @SerialName("zh") ZH("zh"),
    @SerialName("ja") JA("ja"),
    @SerialName("ko") KO("ko"),
    @SerialName("ar") AR("ar"),
    @SerialName("ru") RU("ru");

    companion object {
        val all: List<Language> = entries.toList()
    }
}

/**
 * Status of language support.
 */
@Serializable
enum class LanguageStatus {
    @SerialName("stable") STABLE,
    @SerialName("beta") BETA
}

/**
 * Detection endpoint types.
 */
@Serializable
enum class Detection(@Suppress("unused") val endpoint: String) {
    @SerialName("bullying") BULLYING("bullying"),
    @SerialName("grooming") GROOMING("grooming"),
    @SerialName("unsafe") UNSAFE("unsafe"),
    @SerialName("social-engineering") SOCIAL_ENGINEERING("social-engineering"),
    @SerialName("app-fraud") APP_FRAUD("app-fraud"),
    @SerialName("romance-scam") ROMANCE_SCAM("romance-scam"),
    @SerialName("mule-recruitment") MULE_RECRUITMENT("mule-recruitment"),
    @SerialName("gambling-harm") GAMBLING_HARM("gambling-harm"),
    @SerialName("coercive-control") COERCIVE_CONTROL("coercive-control"),
    @SerialName("vulnerability-exploitation") VULNERABILITY_EXPLOITATION("vulnerability-exploitation"),
    @SerialName("radicalisation") RADICALISATION("radicalisation"),
    @SerialName("distress-signals") DISTRESS_SIGNALS("distress-signals"),
    @SerialName("tfgbv") TFGBV("tfgbv"),
    @SerialName("synthetic-content") SYNTHETIC_CONTENT("synthetic-content")
}

/**
 * Subscription tier levels.
 */
@Serializable
enum class Tier {
    @SerialName("starter") STARTER,
    @SerialName("indie") INDIE,
    @SerialName("pro") PRO,
    @SerialName("business") BUSINESS,
    @SerialName("enterprise") ENTERPRISE
}

/**
 * Verification mode for age/identity verification.
 */
@Serializable
enum class VerificationMode {
    @SerialName("age") AGE,
    @SerialName("identity") IDENTITY
}

/**
 * Document types accepted for verification.
 */
@Serializable(with = DocumentTypeSerializer::class)
enum class DocumentType(val value: String) {
    PASSPORT("passport"),
    NATIONAL_ID("national_id"),
    @Deprecated("Use NATIONAL_ID. Kept as an alias; it sends national_id.", ReplaceWith("DocumentType.NATIONAL_ID"))
    ID_CARD("national_id"),
    DRIVERS_LICENSE("drivers_license")
}

internal object DocumentTypeSerializer : WireEnumSerializer<DocumentType>(
    "ai.tuteliq.DocumentType", DocumentType.entries, { it.value }, null
)

/**
 * How a passport or ID card chip read counts as document evidence.
 * - [OPTIONAL] (API default): the document photo is required and a chip read adds to it.
 * - [REQUIRED]: the photo and a valid chip read are both required.
 * - [INSTEAD_OF_PHOTO]: a chip read that proves itself genuine replaces the photo;
 *   otherwise the session asks for a photo.
 */
@Serializable(with = ChipPolicySerializer::class)
enum class ChipPolicy(val value: String) {
    OPTIONAL("optional"),
    REQUIRED("required"),
    INSTEAD_OF_PHOTO("instead_of_photo"),
    /** A value this SDK version does not know yet. */
    UNKNOWN("unknown")
}

internal object ChipPolicySerializer : WireEnumSerializer<ChipPolicy>(
    "ai.tuteliq.ChipPolicy", ChipPolicy.entries, { it.value }, ChipPolicy.UNKNOWN
)

/**
 * The check a verification rests on: a document and a live face ([FULL]),
 * or a live face alone ([LIVENESS_ONLY]).
 */
@Serializable(with = VerificationCheckSerializer::class)
enum class VerificationCheck(val value: String) {
    FULL("full"),
    LIVENESS_ONLY("liveness_only"),
    /** A value this SDK version does not know yet. */
    UNKNOWN("unknown")
}

internal object VerificationCheckSerializer : WireEnumSerializer<VerificationCheck>(
    "ai.tuteliq.VerificationCheck", VerificationCheck.entries, { it.value }, VerificationCheck.UNKNOWN
)

/**
 * What the document evidence was, when a result rests on a document.
 */
@Serializable(with = DocumentEvidenceSerializer::class)
enum class DocumentEvidence(val value: String) {
    PHOTO("photo"),
    CHIP("chip"),
    PHOTO_AND_CHIP("photo_and_chip"),
    /** A value this SDK version does not know yet. */
    UNKNOWN("unknown")
}

internal object DocumentEvidenceSerializer : WireEnumSerializer<DocumentEvidence>(
    "ai.tuteliq.DocumentEvidence", DocumentEvidence.entries, { it.value }, DocumentEvidence.UNKNOWN
)

/**
 * Verification result status.
 */
@Serializable(with = VerificationStatusSerializer::class)
enum class VerificationStatus(val value: String) {
    VERIFIED("verified"),
    FAILED("failed"),
    NEEDS_REVIEW("needs_review"),
    /** A value this SDK version does not know yet. */
    UNKNOWN("unknown")
}

internal object VerificationStatusSerializer : WireEnumSerializer<VerificationStatus>(
    "ai.tuteliq.VerificationStatus", VerificationStatus.entries, { it.value }, VerificationStatus.UNKNOWN
)

/**
 * Verification session status.
 */
@Serializable(with = VerificationSessionStatusSerializer::class)
enum class VerificationSessionStatus(val value: String) {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    /** The session needs a document photo, for example after a chip read that could not be used. */
    DOCUMENT_REQUIRED("document_required"),
    COMPLETED("completed"),
    FAILED("failed"),
    /** Expired, or cancelled with cancelVerificationSession. */
    EXPIRED("expired"),
    @Deprecated("The API never returns this status; a cancelled session reports EXPIRED.", ReplaceWith("VerificationSessionStatus.EXPIRED"))
    CANCELLED("cancelled"),
    /** A value this SDK version does not know yet. */
    UNKNOWN("unknown")
}

internal object VerificationSessionStatusSerializer : WireEnumSerializer<VerificationSessionStatus>(
    "ai.tuteliq.VerificationSessionStatus", VerificationSessionStatus.entries, { it.value }, VerificationSessionStatus.UNKNOWN
)

// =============================================================================
// Incidents
// =============================================================================

/**
 * Moderator action on an incident.
 */
enum class ModeratorAction(val value: String) {
    CONFIRM("confirm"),
    DOWNGRADE("downgrade"),
    ESCALATE("escalate"),
    RECLASSIFY("reclassify"),
    DISMISS("dismiss"),
    RESOLVE("resolve"),
    REOPEN("reopen")
}

/**
 * Reason recorded with a moderator action.
 */
enum class ModeratorReasonCode(val value: String) {
    CONFIRMED_ACCURATE("confirmed_accurate"),
    FALSE_POSITIVE("false_positive"),
    OUT_OF_CONTEXT("out_of_context"),
    INSUFFICIENT_SEVERITY("insufficient_severity"),
    INCORRECT_CATEGORY("incorrect_category"),
    REQUIRES_LAW_ENFORCEMENT("requires_law_enforcement"),
    PARENT_NOTIFIED("parent_notified"),
    OTHER("other")
}

/**
 * Retention class recorded with a moderator review.
 */
enum class RetentionClass(val value: String) {
    BIOMETRIC_HIGH_RISK("biometric-high-risk"),
    SAFETY_HIGH_RISK("safety-high-risk"),
    LIMITED_RISK("limited-risk"),
    MINIMAL_RISK("minimal-risk")
}

/**
 * Time bucket for incident trends.
 */
enum class IncidentTrendBucket(val value: String) {
    HOUR("hour"),
    DAY("day"),
    WEEK("week")
}

// =============================================================================
// Nickname, image match and redaction
// =============================================================================

/**
 * How readily a doubtful nickname reading is acted on. A clear concern is acted on at every level.
 */
enum class NicknameStrictness(val value: String) {
    CAUTIOUS("cautious"),
    BALANCED("balanced"),
    STRICT("strict")
}

/**
 * Checks available on verifyImageMatch.
 */
enum class ImageMatchCheck(val value: String) {
    FACE_MATCH("face_match"),
    AGE("age"),
    FORENSIC("forensic"),
    SYNTHETIC("synthetic")
}

/**
 * Redaction mode.
 * - [PROTECTIVE] (default) also removes near misses such as a mistyped card, IBAN, ID or phone number.
 * - [PRECISE] leaves them.
 */
enum class RedactMode(val value: String) {
    PROTECTIVE("protective"),
    PRECISE("precise")
}
