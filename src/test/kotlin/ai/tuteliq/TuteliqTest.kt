package ai.tuteliq

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class TuteliqTest {

    // =========================================================================
    // Client Initialization
    // =========================================================================

    @Test
    fun `client creation succeeds with valid API key`() {
        val client = Tuteliq("test-api-key-12345")
        assertNotNull(client)
        client.close()
    }

    @Test
    fun `client creation fails with empty API key`() {
        assertFailsWith<IllegalArgumentException> {
            Tuteliq("")
        }
    }

    @Test
    fun `client creation fails with short API key`() {
        assertFailsWith<IllegalArgumentException> {
            Tuteliq("short")
        }
    }

    @Test
    fun `client accepts custom options`() {
        val client = Tuteliq(
            apiKey = "test-api-key-12345",
            timeout = 60_000L,
            maxRetries = 5,
            retryDelay = 2_000L
        )
        assertNotNull(client)
        client.close()
    }

    // =========================================================================
    // Enum Values
    // =========================================================================

    @Test
    fun `Severity enum has correct values`() {
        assertEquals("low", Severity.LOW.name.lowercase())
        assertEquals("medium", Severity.MEDIUM.name.lowercase())
        assertEquals("high", Severity.HIGH.name.lowercase())
        assertEquals("critical", Severity.CRITICAL.name.lowercase())
    }

    @Test
    fun `GroomingRisk enum has correct values`() {
        assertEquals("none", GroomingRisk.NONE.name.lowercase())
        assertEquals("low", GroomingRisk.LOW.name.lowercase())
        assertEquals("medium", GroomingRisk.MEDIUM.name.lowercase())
        assertEquals("high", GroomingRisk.HIGH.name.lowercase())
        assertEquals("critical", GroomingRisk.CRITICAL.name.lowercase())
    }

    @Test
    fun `RiskLevel enum has correct values`() {
        assertEquals("safe", RiskLevel.SAFE.name.lowercase())
        assertEquals("low", RiskLevel.LOW.name.lowercase())
        assertEquals("medium", RiskLevel.MEDIUM.name.lowercase())
        assertEquals("high", RiskLevel.HIGH.name.lowercase())
        assertEquals("critical", RiskLevel.CRITICAL.name.lowercase())
    }

    @Test
    fun `EmotionTrend enum has correct values`() {
        assertEquals("improving", EmotionTrend.IMPROVING.name.lowercase())
        assertEquals("stable", EmotionTrend.STABLE.name.lowercase())
        assertEquals("worsening", EmotionTrend.WORSENING.name.lowercase())
    }

    @Test
    fun `Audience enum has correct values`() {
        assertEquals("child", Audience.CHILD.name.lowercase())
        assertEquals("parent", Audience.PARENT.name.lowercase())
        assertEquals("educator", Audience.EDUCATOR.name.lowercase())
        assertEquals("platform", Audience.PLATFORM.name.lowercase())
    }

    @Test
    fun `MessageRole enum has correct values`() {
        assertEquals("adult", MessageRole.ADULT.name.lowercase())
        assertEquals("child", MessageRole.CHILD.name.lowercase())
        assertEquals("unknown", MessageRole.UNKNOWN.name.lowercase())
    }

    // =========================================================================
    // Model Creation
    // =========================================================================

    @Test
    fun `AnalysisContext creation works`() {
        val context = AnalysisContext(
            language = "en",
            ageGroup = "11-13",
            relationship = "classmates",
            platform = "chat"
        )
        assertEquals("en", context.language)
        assertEquals("11-13", context.ageGroup)
        assertEquals("classmates", context.relationship)
        assertEquals("chat", context.platform)
    }

    @Test
    fun `DetectBullyingInput creation works`() {
        val input = DetectBullyingInput(
            content = "Test message",
            externalId = "msg_123",
            metadata = mapOf("user_id" to "user_456")
        )
        assertEquals("Test message", input.content)
        assertEquals("msg_123", input.externalId)
        assertEquals("user_456", input.metadata?.get("user_id"))
    }

    @Test
    fun `GroomingMessage creation works`() {
        val msg = GroomingMessage(
            role = MessageRole.ADULT,
            content = "Hello"
        )
        assertEquals(MessageRole.ADULT, msg.role)
        assertEquals("Hello", msg.content)
    }

    @Test
    fun `DetectGroomingInput creation works`() {
        val input = DetectGroomingInput(
            messages = listOf(
                GroomingMessage(role = MessageRole.ADULT, content = "Hello"),
                GroomingMessage(role = MessageRole.CHILD, content = "Hi")
            ),
            childAge = 12
        )
        assertEquals(2, input.messages.size)
        assertEquals(12, input.childAge)
    }

    @Test
    fun `EmotionMessage creation works`() {
        val msg = EmotionMessage(
            sender = "user",
            content = "I feel happy"
        )
        assertEquals("user", msg.sender)
        assertEquals("I feel happy", msg.content)
    }

    @Test
    fun `GetActionPlanInput creation works`() {
        val input = GetActionPlanInput(
            situation = "Someone is spreading rumors",
            childAge = 12,
            audience = Audience.CHILD,
            severity = Severity.MEDIUM
        )
        assertEquals("Someone is spreading rumors", input.situation)
        assertEquals(12, input.childAge)
        assertEquals(Audience.CHILD, input.audience)
        assertEquals(Severity.MEDIUM, input.severity)
    }

    @Test
    fun `ReportMessage creation works`() {
        val msg = ReportMessage(
            sender = "user1",
            content = "Threatening message"
        )
        assertEquals("user1", msg.sender)
        assertEquals("Threatening message", msg.content)
    }

    @Test
    fun `GenerateReportInput creation works`() {
        val input = GenerateReportInput(
            messages = listOf(
                ReportMessage(sender = "user1", content = "Bad message"),
                ReportMessage(sender = "child", content = "Please stop")
            ),
            childAge = 14,
            incidentType = "bullying"
        )
        assertEquals(2, input.messages.size)
        assertEquals(14, input.childAge)
        assertEquals("bullying", input.incidentType)
    }

    // =========================================================================
    // Verification Enums
    // =========================================================================

    @Test
    fun `VerificationMode enum has correct values`() {
        assertEquals("age", VerificationMode.AGE.name.lowercase())
        assertEquals("identity", VerificationMode.IDENTITY.name.lowercase())
    }

    @Test
    fun `DocumentType enum has correct values`() {
        assertEquals("passport", DocumentType.PASSPORT.value)
        assertEquals("national_id", DocumentType.NATIONAL_ID.value)
        @Suppress("DEPRECATION")
        assertEquals("national_id", DocumentType.ID_CARD.value)
        assertEquals("drivers_license", DocumentType.DRIVERS_LICENSE.value)
    }

    @Test
    fun `VerificationStatus enum has correct values`() {
        assertEquals("verified", VerificationStatus.VERIFIED.value)
        assertEquals("failed", VerificationStatus.FAILED.value)
        assertEquals("needs_review", VerificationStatus.NEEDS_REVIEW.value)
    }

    @Test
    fun `VerificationSessionStatus enum has correct values`() {
        assertEquals("pending", VerificationSessionStatus.PENDING.value)
        assertEquals("in_progress", VerificationSessionStatus.IN_PROGRESS.value)
        assertEquals("document_required", VerificationSessionStatus.DOCUMENT_REQUIRED.value)
        assertEquals("completed", VerificationSessionStatus.COMPLETED.value)
        assertEquals("failed", VerificationSessionStatus.FAILED.value)
        assertEquals("expired", VerificationSessionStatus.EXPIRED.value)
    }

    // =========================================================================
    // Verification Models
    // =========================================================================

    @Test
    fun `CreateVerificationSessionInput creation works`() {
        val input = CreateVerificationSessionInput(
            mode = VerificationMode.AGE,
            documentType = DocumentType.PASSPORT
        )
        assertEquals(VerificationMode.AGE, input.mode)
        assertEquals(DocumentType.PASSPORT, input.documentType)
    }

    @Test
    fun `VerificationSession deserialization works`() {
        val jsonStr = """
            {
                "session_id": "vs_abc123",
                "mobile_url": "https://verify.tuteliq.ai/session/vs_abc123",
                "expires_at": 1772712000000,
                "mode": "age"
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }
        val session = json.decodeFromString<VerificationSession>(jsonStr)

        assertEquals("vs_abc123", session.sessionId)
        assertEquals("https://verify.tuteliq.ai/session/vs_abc123", session.url)
        assertEquals(1772712000000L, session.expiresAt)
        assertEquals(VerificationMode.AGE, session.mode)
    }

    @Test
    fun `AgeVerificationResult deserialization works`() {
        val jsonStr = """
            {
                "verification_id": "vrf_123",
                "status": "verified",
                "age_bracket": "18-25",
                "is_minor": false,
                "credits_used": 10,
                "liveness": {"valid": true},
                "face_match": {"matched": true, "confidence": 0.95, "distance": 0.12}
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }
        val result = json.decodeFromString<AgeVerificationResult>(jsonStr)

        assertEquals("vrf_123", result.verificationId)
        assertEquals(VerificationStatus.VERIFIED, result.status)
        assertEquals("18-25", result.ageBracket)
        assertEquals(false, result.isMinor)
        assertEquals(10, result.creditsUsed)
        assertEquals(true, result.liveness?.valid)
        assertEquals(true, result.faceMatch?.matched)
        assertEquals(0.95, result.faceMatch?.confidence)
    }

    @Test
    fun `IdentityVerificationResult deserialization works`() {
        val jsonStr = """
            {
                "verification_id": "vrf_456",
                "status": "verified",
                "full_name": "John Doe",
                "date_of_birth": "1990-01-15",
                "document_type": "passport",
                "country_code": "US",
                "credits_used": 15
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }
        val result = json.decodeFromString<IdentityVerificationResult>(jsonStr)

        assertEquals("vrf_456", result.verificationId)
        assertEquals(VerificationStatus.VERIFIED, result.status)
        assertEquals("John Doe", result.fullName)
        assertEquals("1990-01-15", result.dateOfBirth)
        assertEquals("passport", result.documentType)
        assertEquals("US", result.countryCode)
    }

    @Test
    fun `VerificationSessionResult deserialization works`() {
        val jsonStr = """
            {
                "session_id": "vs_abc123",
                "status": "completed",
                "created_at": 1772704800000,
                "expires_at": 1772712000000
            }
        """.trimIndent()
        val json = Json { ignoreUnknownKeys = true }
        val result = json.decodeFromString<VerificationSessionResult>(jsonStr)

        assertEquals("vs_abc123", result.sessionId)
        assertEquals(VerificationSessionStatus.COMPLETED, result.status)
        assertEquals(1772712000000L, result.expiresAt)
    }
}
