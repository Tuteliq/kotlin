package dev.tuteliq

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

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
}
