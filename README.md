<p align="center">
  <img src="./assets/logo.png" alt="Tuteliq" width="200" />
</p>

<h1 align="center">Tuteliq Kotlin SDK</h1>

<p align="center">
  <strong>Official Kotlin SDK for the Tuteliq API</strong><br>
  AI-powered child safety analysis
</p>

<p align="center">
  <a href="https://search.maven.org/artifact/dev.tuteliq/tuteliq"><img src="https://img.shields.io/maven-central/v/dev.tuteliq/tuteliq.svg" alt="Maven Central"></a>
  <a href="https://github.com/Tuteliq/kotlin/actions"><img src="https://img.shields.io/github/actions/workflow/status/Tuteliq/kotlin/ci.yml" alt="build status"></a>
  <a href="https://github.com/Tuteliq/kotlin/blob/main/LICENSE"><img src="https://img.shields.io/github/license/Tuteliq/kotlin.svg" alt="license"></a>
</p>

<p align="center">
  <a href="https://api.tuteliq.ai/docs">API Docs</a> •
  <a href="https://tuteliq.app">Dashboard</a> •
  <a href="https://discord.gg/7kbTeRYRXD">Discord</a>
</p>

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("dev.tuteliq:tuteliq:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'dev.tuteliq:tuteliq:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>dev.tuteliq</groupId>
    <artifactId>tuteliq</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Requirements

- Kotlin 1.9+
- Java 17+

---

## Quick Start

```kotlin
import dev.tuteliq.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val client = Tuteliq(apiKey = "your-api-key")

    // Quick safety analysis
    val result = client.analyze("Message to check")

    if (result.riskLevel != RiskLevel.SAFE) {
        println("Risk: ${result.riskLevel}")
        println("Summary: ${result.summary}")
    }

    client.close()
}
```

---

## API Reference

### Initialization

```kotlin
import dev.tuteliq.Tuteliq

// Simple
val client = Tuteliq(apiKey = "your-api-key")

// With options
val client = Tuteliq(
    apiKey = "your-api-key",
    timeout = 30_000L,     // Request timeout in milliseconds
    maxRetries = 3,        // Retry attempts
    retryDelay = 1_000L,   // Initial retry delay in milliseconds
)
```

### Bullying Detection

```kotlin
val result = client.detectBullying("Nobody likes you, just leave")

if (result.isBullying) {
    println("Severity: ${result.severity}")       // Severity.MEDIUM
    println("Types: ${result.bullyingType}")      // ["exclusion", "verbal_abuse"]
    println("Confidence: ${result.confidence}")   // 0.92
    println("Rationale: ${result.rationale}")
}
```

### Grooming Detection

```kotlin
import dev.tuteliq.*

val result = client.detectGrooming(
    DetectGroomingInput(
        messages = listOf(
            GroomingMessage(role = MessageRole.ADULT, content = "This is our secret"),
            GroomingMessage(role = MessageRole.CHILD, content = "Ok I won't tell"),
        ),
        childAge = 12,
    )
)

if (result.groomingRisk == GroomingRisk.HIGH) {
    println("Flags: ${result.flags}")  // ["secrecy", "isolation"]
}
```

### Unsafe Content Detection

```kotlin
val result = client.detectUnsafe("I don't want to be here anymore")

if (result.unsafe) {
    println("Categories: ${result.categories}")  // ["self_harm", "crisis"]
    println("Severity: ${result.severity}")      // Severity.CRITICAL
}
```

### Quick Analysis

Runs bullying and unsafe detection:

```kotlin
val result = client.analyze("Message to check")

println("Risk Level: ${result.riskLevel}")   // RiskLevel.SAFE/LOW/MEDIUM/HIGH/CRITICAL
println("Risk Score: ${result.riskScore}")   // 0.0 - 1.0
println("Summary: ${result.summary}")
println("Action: ${result.recommendedAction}")
```

### Emotion Analysis

```kotlin
val result = client.analyzeEmotions("I'm so stressed about everything")

println("Emotions: ${result.dominantEmotions}")  // ["anxiety", "sadness"]
println("Trend: ${result.trend}")                 // EmotionTrend.WORSENING
println("Followup: ${result.recommendedFollowup}")
```

### Action Plan

```kotlin
import dev.tuteliq.*

val plan = client.getActionPlan(
    GetActionPlanInput(
        situation = "Someone is spreading rumors about me",
        childAge = 12,
        audience = Audience.CHILD,
        severity = Severity.MEDIUM,
    )
)

println("Steps: ${plan.steps}")
println("Tone: ${plan.tone}")
```

### Incident Report

```kotlin
import dev.tuteliq.*

val report = client.generateReport(
    GenerateReportInput(
        messages = listOf(
            ReportMessage(sender = "user1", content = "Threatening message"),
            ReportMessage(sender = "child", content = "Please stop"),
        ),
        childAge = 14,
    )
)

println("Summary: ${report.summary}")
println("Risk: ${report.riskLevel}")
println("Next Steps: ${report.recommendedNextSteps}")
```

---

## Tracking Fields

All methods support `externalId` and `metadata` for correlating requests:

```kotlin
val result = client.detectBullying(
    content = "Test message",
    externalId = "msg_12345",
    metadata = mapOf("user_id" to "usr_abc", "session" to "sess_xyz"),
)

// Echoed back in response
println(result.externalId)  // "msg_12345"
println(result.metadata)    // {"user_id": "usr_abc", ...}
```

---

## Usage Tracking

```kotlin
val result = client.detectBullying("test")

// Access usage stats after any request
client.usage?.let { usage ->
    println("Limit: ${usage.limit}")
    println("Used: ${usage.used}")
    println("Remaining: ${usage.remaining}")
}

// Request metadata
println("Request ID: ${client.lastRequestId}")
```

---

## Error Handling

```kotlin
import dev.tuteliq.*

try {
    val result = client.detectBullying("test")
} catch (e: AuthenticationException) {
    println("Auth error: ${e.message}")
} catch (e: RateLimitException) {
    println("Rate limited: ${e.message}")
} catch (e: ValidationException) {
    println("Invalid input: ${e.message}, details: ${e.details}")
} catch (e: ServerException) {
    println("Server error ${e.statusCode}: ${e.message}")
} catch (e: TimeoutException) {
    println("Timeout: ${e.message}")
} catch (e: NetworkException) {
    println("Network error: ${e.message}")
} catch (e: TuteliqException) {
    println("Error: ${e.message}")
}
```

---

## Android Example

```kotlin
import dev.tuteliq.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SafetyChecker(private val apiKey: String) {
    private val client = Tuteliq(apiKey = apiKey)

    suspend fun checkMessage(message: String): AnalyzeResult {
        return withContext(Dispatchers.IO) {
            client.analyze(message)
        }
    }

    fun close() {
        client.close()
    }
}
```

---

## Best Practices

### Message Batching

The **bullying** and **unsafe content** methods analyze a single `text` field per request. If your app receives messages one at a time, concatenate a **sliding window of recent messages** into one string before calling the API. Single words or short fragments lack context for accurate detection and can be exploited to bypass safety filters.

```kotlin
// Bad — each message analyzed in isolation, easily evaded
messages.forEach { msg ->
    client.detectBullying(text = msg)
}

// Good — recent messages analyzed together
val window = recentMessages.takeLast(10).joinToString(" ")
client.detectBullying(text = window)
```

The **grooming** method already accepts a `messages` list and analyzes the full conversation in context.

### PII Redaction

Enable `PII_REDACTION_ENABLED=true` on your Tuteliq API to automatically strip emails, phone numbers, URLs, social handles, IPs, and other PII from detection summaries and webhook payloads. The original text is still analyzed in full — only stored outputs are scrubbed.

---

## Support

- **API Docs**: [api.tuteliq.ai/docs](https://api.tuteliq.ai/docs)
- **Discord**: [discord.gg/7kbTeRYRXD](https://discord.gg/7kbTeRYRXD)
- **Email**: support@tuteliq.ai
- **Issues**: [GitHub Issues](https://github.com/Tuteliq/kotlin/issues)

---

## License

MIT License - see [LICENSE](LICENSE) for details.

---

## The Mission: Why This Matters

Before you decide to contribute or sponsor, read these numbers. They are not projections. They are not estimates from a pitch deck. They are verified statistics from the University of Edinburgh, UNICEF, NCMEC, and Interpol.

- **302 million** children are victims of online sexual exploitation and abuse every year. That is **10 children every second**. *(Childlight / University of Edinburgh, 2024)*
- **1 in 8** children globally have been victims of non-consensual sexual imagery in the past year. *(Childlight, 2024)*
- **370 million** girls and women alive today experienced rape or sexual assault in childhood. An estimated **240–310 million** boys and men experienced the same. *(UNICEF, 2024)*
- **29.2 million** incidents of suspected child sexual exploitation were reported to NCMEC's CyberTipline in 2024 alone — containing **62.9 million files** (images, videos). *(NCMEC, 2025)*
- **546,000** reports of online enticement (adults grooming children) in 2024 — a **192% increase** from the year before. *(NCMEC, 2025)*
- **1,325% increase** in AI-generated child sexual abuse material reports between 2023 and 2024. The technology that should protect children is being weaponized against them. *(NCMEC, 2025)*
- **100 sextortion reports per day** to NCMEC. Since 2021, at least **36 teenage boys** have taken their own lives because they were victimized by sextortion. *(NCMEC, 2025)*
- **84%** of reports resolve outside the United States. This is not an American problem. This is a **global emergency**. *(NCMEC, 2025)*

End-to-end encryption is making platforms blind. In 2024, platforms reported **7 million fewer incidents** than the year before — not because abuse stopped, but because they can no longer see it. The tools that catch known images are failing. The systems that rely on human moderators are overwhelmed. The technology to detect behavior — grooming patterns, escalation, manipulation — in real-time text conversations **exists right now**. It is running at [api.tuteliq.ai](https://api.tuteliq.ai).

The question is not whether this technology is possible. The question is whether we build the company to put it everywhere it needs to be.

**Every second we wait, another child is harmed.**

We have the technology. We need the support.

If this mission matters to you, consider [sponsoring our open-source work](https://github.com/sponsors/Tuteliq) so we can keep building the tools that protect children — and keep them free and accessible for everyone.

---

<p align="center">
  <sub>Built with care for child safety by the <a href="https://tuteliq.ai">Tuteliq</a> team</sub>
</p>
