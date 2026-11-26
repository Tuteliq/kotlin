package ai.tuteliq

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/**
 * Tuteliq API client for child safety analysis.
 *
 * Example:
 * ```kotlin
 * val client = Tuteliq("your-api-key")
 * val result = client.detectBullying("Some text to analyze")
 * if (result.isBullying) {
 *     println("Severity: ${result.severity}")
 * }
 * client.close()
 * ```
 *
 * @property apiKey Your Tuteliq API key.
 * @property timeout Request timeout in milliseconds.
 * @property maxRetries Number of retry attempts for transient failures.
 * @property retryDelay Initial retry delay in milliseconds.
 * @property baseUrl API base URL.
 */
class Tuteliq internal constructor(
    private val apiKey: String,
    private val timeout: Long,
    private val maxRetries: Int,
    private val retryDelay: Long,
    private val baseUrl: String,
    engine: HttpClientEngine?
) {
    constructor(
        apiKey: String,
        timeout: Long = 30_000L,
        maxRetries: Int = 3,
        retryDelay: Long = 1_000L,
        baseUrl: String = "https://api.tuteliq.ai"
    ) : this(apiKey, timeout, maxRetries, retryDelay, baseUrl, null)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val clientConfig: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = timeout
            connectTimeoutMillis = timeout
        }
        defaultRequest {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
        }
    }

    private val client = if (engine != null) HttpClient(engine, clientConfig) else HttpClient(CIO, clientConfig)

    /** Current usage statistics (updated after each request). */
    var usage: Usage? = null
        private set

    /** Request ID from the last API call. */
    var lastRequestId: String? = null
        private set

    init {
        require(apiKey.isNotBlank()) { "API key is required" }
        require(apiKey.length >= 10) { "API key appears to be invalid" }
    }

    companion object {
        private const val SDK_IDENTIFIER = "Kotlin SDK"
    }

    private fun resolvePlatform(platform: String? = null): String {
        return if (!platform.isNullOrEmpty()) "$platform - $SDK_IDENTIFIER" else SDK_IDENTIFIER
    }

    /**
     * Create a voice streaming session over WebSocket.
     */
    fun voiceStream(
        config: VoiceStreamConfig? = null,
        handlers: VoiceStreamHandlers? = null
    ): VoiceStreamSession {
        return VoiceStreamSession(apiKey, config, handlers)
    }

    /**
     * Close the HTTP client.
     */
    fun close() {
        client.close()
    }

    // =========================================================================
    // Safety Detection
    // =========================================================================

    /**
     * Detect bullying in content.
     *
     * @param content Text content to analyze.
     * @param context Optional analysis context.
     * @param externalId Your identifier for correlation.
     * @param metadata Custom metadata.
     * @return BullyingResult with detection details.
     */
    suspend fun detectBullying(
        content: String,
        context: AnalysisContext? = null,
        externalId: String? = null,
        metadata: Map<String, Any>? = null
    ): BullyingResult {
        val body = buildJsonObject {
            put("text", content)
            val ctx = context ?: AnalysisContext()
            put("context", json.encodeToJsonElement(ctx.copy(platform = resolvePlatform(ctx.platform))))
            externalId?.let { put("external_id", it) }
            metadata?.let { put("metadata", mapToJsonObject(it)) }
        }

        return request("/api/v1/safety/bullying", body)
    }

    /**
     * Detect bullying in content using input object.
     */
    suspend fun detectBullying(input: DetectBullyingInput): BullyingResult {
        return detectBullying(
            content = input.content,
            context = input.context,
            externalId = input.externalId,
            metadata = input.metadata
        )
    }

    /**
     * Detect grooming patterns in a conversation.
     *
     * @param input DetectGroomingInput with messages and context.
     * @return GroomingResult with detection details.
     */
    suspend fun detectGrooming(input: DetectGroomingInput): GroomingResult {
        val body = buildJsonObject {
            putJsonArray("messages") {
                input.messages.forEach { msg ->
                    addJsonObject {
                        put("sender_role", msg.role.name.lowercase())
                        put("text", msg.content)
                    }
                }
            }
            val contextObj = buildJsonObject {
                put("platform", resolvePlatform(input.context?.platform))
                input.childAge?.let { put("child_age", it) }
                input.context?.let {
                    it.language?.let { lang -> put("language", lang) }
                    it.ageGroup?.let { ag -> put("age_group", ag) }
                    it.relationship?.let { rel -> put("relationship", rel) }
                }
            }
            put("context", contextObj)
            input.externalId?.let { put("external_id", it) }
            input.metadata?.let { put("metadata", mapToJsonObject(it)) }
        }

        return request("/api/v1/safety/grooming", body)
    }

    /**
     * Detect unsafe content.
     *
     * @param content Text content to analyze.
     * @param context Optional analysis context.
     * @param externalId Your identifier for correlation.
     * @param metadata Custom metadata.
     * @return UnsafeResult with detection details.
     */
    suspend fun detectUnsafe(
        content: String,
        context: AnalysisContext? = null,
        externalId: String? = null,
        metadata: Map<String, Any>? = null
    ): UnsafeResult {
        val body = buildJsonObject {
            put("text", content)
            val ctx = context ?: AnalysisContext()
            put("context", json.encodeToJsonElement(ctx.copy(platform = resolvePlatform(ctx.platform))))
            externalId?.let { put("external_id", it) }
            metadata?.let { put("metadata", mapToJsonObject(it)) }
        }

        return request("/api/v1/safety/unsafe", body)
    }

    /**
     * Detect unsafe content using input object.
     */
    suspend fun detectUnsafe(input: DetectUnsafeInput): UnsafeResult {
        return detectUnsafe(
            content = input.content,
            context = input.context,
            externalId = input.externalId,
            metadata = input.metadata
        )
    }

    /**
     * Quick analysis - runs bullying and unsafe detection in parallel.
     *
     * @param content Text content to analyze.
     * @param context Optional analysis context.
     * @param include Which checks to run (default: ["bullying", "unsafe"]).
     * @param externalId Your identifier for correlation.
     * @param metadata Custom metadata.
     * @return AnalyzeResult with combined results.
     */
    suspend fun analyze(
        content: String,
        context: AnalysisContext? = null,
        include: List<String>? = null,
        externalId: String? = null,
        metadata: Map<String, Any>? = null
    ): AnalyzeResult {
        val checks = include ?: listOf("bullying", "unsafe")

        var bullyingResult: BullyingResult? = null
        var unsafeResult: UnsafeResult? = null
        var maxRiskScore = 0.0

        if ("bullying" in checks) {
            bullyingResult = detectBullying(content, context, externalId, metadata)
            maxRiskScore = maxOf(maxRiskScore, bullyingResult.riskScore)
        }

        if ("unsafe" in checks) {
            unsafeResult = detectUnsafe(content, context, externalId, metadata)
            maxRiskScore = maxOf(maxRiskScore, unsafeResult.riskScore)
        }

        val riskLevel = when {
            maxRiskScore >= 0.9 -> RiskLevel.CRITICAL
            maxRiskScore >= 0.7 -> RiskLevel.HIGH
            maxRiskScore >= 0.5 -> RiskLevel.MEDIUM
            maxRiskScore >= 0.3 -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }

        val findings = mutableListOf<String>()
        if (bullyingResult?.isBullying == true) {
            findings.add("Bullying detected (${bullyingResult.severity.name.lowercase()})")
        }
        if (unsafeResult?.unsafe == true) {
            findings.add("Unsafe content: ${unsafeResult.categories.joinToString(", ")}")
        }
        val summary = if (findings.isEmpty()) "No safety concerns detected." else findings.joinToString(". ")

        val actions = listOfNotNull(
            bullyingResult?.recommendedAction,
            unsafeResult?.recommendedAction
        )
        val recommendedAction = when {
            "immediate_intervention" in actions -> "immediate_intervention"
            "flag_for_moderator" in actions -> "flag_for_moderator"
            "monitor" in actions -> "monitor"
            else -> "none"
        }

        return AnalyzeResult(
            riskLevel = riskLevel,
            riskScore = maxRiskScore,
            summary = summary,
            bullying = bullyingResult,
            unsafe = unsafeResult,
            recommendedAction = recommendedAction,
            externalId = externalId,
            metadata = metadata
        )
    }

    /**
     * Quick analysis using input object.
     */
    suspend fun analyze(input: AnalyzeInput): AnalyzeResult {
        return analyze(
            content = input.content,
            context = input.context,
            include = input.include,
            externalId = input.externalId,
            metadata = input.metadata
        )
    }

    // =========================================================================
    // Emotion Analysis
    // =========================================================================

    /**
     * Analyze emotions in content or conversation.
     *
     * @param content Text content to analyze.
     * @param context Optional analysis context.
     * @param externalId Your identifier for correlation.
     * @param metadata Custom metadata.
     * @return EmotionsResult with emotion analysis.
     */
    suspend fun analyzeEmotions(
        content: String,
        context: AnalysisContext? = null,
        externalId: String? = null,
        metadata: Map<String, Any>? = null
    ): EmotionsResult {
        val body = buildJsonObject {
            putJsonArray("messages") {
                addJsonObject {
                    put("sender", "user")
                    put("text", content)
                }
            }
            val ctx = context ?: AnalysisContext()
            put("context", json.encodeToJsonElement(ctx.copy(platform = resolvePlatform(ctx.platform))))
            externalId?.let { put("external_id", it) }
            metadata?.let { put("metadata", mapToJsonObject(it)) }
        }

        return request("/api/v1/analysis/emotions", body)
    }

    /**
     * Analyze emotions using input object.
     */
    suspend fun analyzeEmotions(input: AnalyzeEmotionsInput): EmotionsResult {
        val body = buildJsonObject {
            when {
                input.content != null -> {
                    putJsonArray("messages") {
                        addJsonObject {
                            put("sender", "user")
                            put("text", input.content)
                        }
                    }
                }
                input.messages != null -> {
                    putJsonArray("messages") {
                        input.messages.forEach { msg ->
                            addJsonObject {
                                put("sender", msg.sender)
                                put("text", msg.content)
                            }
                        }
                    }
                }
            }
            val ctx = input.context ?: AnalysisContext()
            put("context", json.encodeToJsonElement(ctx.copy(platform = resolvePlatform(ctx.platform))))
            input.externalId?.let { put("external_id", it) }
            input.metadata?.let { put("metadata", mapToJsonObject(it)) }
        }

        return request("/api/v1/analysis/emotions", body)
    }

    // =========================================================================
    // Guidance
    // =========================================================================

    /**
     * Get age-appropriate action guidance.
     *
     * @param input GetActionPlanInput with situation details.
     * @return ActionPlanResult with guidance steps.
     */
    suspend fun getActionPlan(input: GetActionPlanInput): ActionPlanResult {
        val body = buildJsonObject {
            put("role", (input.audience ?: Audience.PARENT).name.lowercase())
            put("situation", input.situation)
            put("context", buildJsonObject { put("platform", resolvePlatform()) })
            input.childAge?.let { put("child_age", it) }
            input.severity?.let { put("severity", it.name.lowercase()) }
            input.country?.let { put("country", it) }
            input.externalId?.let { put("external_id", it) }
            input.metadata?.let { put("metadata", mapToJsonObject(it)) }
        }

        return request("/api/v1/guidance/action-plan", body)
    }

    // =========================================================================
    // Reports
    // =========================================================================

    /**
     * Generate an incident report.
     *
     * @param input GenerateReportInput with messages and details.
     * @return ReportResult with incident summary.
     */
    suspend fun generateReport(input: GenerateReportInput): ReportResult {
        val body = buildJsonObject {
            putJsonArray("messages") {
                input.messages.forEach { msg ->
                    addJsonObject {
                        put("sender", msg.sender)
                        put("text", msg.content)
                    }
                }
            }
            put("context", buildJsonObject { put("platform", resolvePlatform()) })
            val metaObj = buildJsonObject {
                input.childAge?.let { put("child_age", it) }
                input.incidentType?.let { put("type", it) }
            }
            if (metaObj.isNotEmpty()) {
                put("meta", metaObj)
            }
            input.externalId?.let { put("external_id", it) }
            input.metadata?.let { put("metadata", mapToJsonObject(it)) }
        }

        return request("/api/v1/reports/incident", body)
    }

    // =========================================================================
    // Account Management (GDPR)
    // =========================================================================

    /**
     * Delete all account data (GDPR Article 17 — Right to Erasure).
     */
    suspend fun deleteAccountData(): AccountDeletionResult {
        return request("DELETE", "/api/v1/account/data")
    }

    /**
     * Export all account data as JSON (GDPR Article 20 — Right to Data Portability).
     */
    suspend fun exportAccountData(): AccountExportResult {
        return request("GET", "/api/v1/account/export")
    }

    /**
     * Record user consent (GDPR Article 7).
     */
    suspend fun recordConsent(input: RecordConsentInput): ConsentActionResult {
        val body = buildJsonObject {
            put("consent_type", input.consentType.name.lowercase())
            put("version", input.version)
        }
        return request("/api/v1/account/consent", body)
    }

    /**
     * Get current consent status (GDPR Article 7).
     */
    suspend fun getConsentStatus(type: ConsentType? = null): ConsentStatusResult {
        val query = if (type != null) "?type=${type.name.lowercase()}" else ""
        return request("GET", "/api/v1/account/consent$query")
    }

    /**
     * Withdraw consent (GDPR Article 7.3).
     */
    suspend fun withdrawConsent(type: ConsentType): ConsentActionResult {
        return request("DELETE", "/api/v1/account/consent/${type.name.lowercase()}")
    }

    /**
     * Rectify user data (GDPR Article 16 — Right to Rectification).
     */
    suspend fun rectifyData(input: RectifyDataInput): RectifyDataResult {
        val body = buildJsonObject {
            put("collection", input.collection)
            put("document_id", input.documentId)
            put("fields", mapToJsonObject(input.fields))
        }
        return requestWithMethod("PATCH", "/api/v1/account/data", body)
    }

    /**
     * Get audit logs (GDPR Article 15 — Right of Access).
     */
    suspend fun getAuditLogs(action: AuditAction? = null, limit: Int? = null): AuditLogsResult {
        val params = mutableListOf<String>()
        if (action != null) params.add("action=${action.name.lowercase()}")
        if (limit != null) params.add("limit=$limit")
        val query = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
        return request("GET", "/api/v1/account/audit-logs$query")
    }

    // =========================================================================
    // Breach Management (GDPR Article 33/34)
    // =========================================================================

    /**
     * Log a new data breach.
     */
    suspend fun logBreach(input: LogBreachInput): LogBreachResult {
        val body = buildJsonObject {
            put("title", input.title)
            put("description", input.description)
            put("severity", input.severity.name.lowercase())
            putJsonArray("affected_user_ids") {
                input.affectedUserIds.forEach { add(it) }
            }
            putJsonArray("data_categories") {
                input.dataCategories.forEach { add(it) }
            }
            put("reported_by", input.reportedBy)
        }
        return request("/api/v1/admin/breach", body)
    }

    /**
     * List data breaches.
     */
    suspend fun listBreaches(status: BreachStatus? = null, limit: Int? = null): BreachListResult {
        val params = mutableListOf<String>()
        if (status != null) params.add("status=${status.name.lowercase()}")
        if (limit != null) params.add("limit=$limit")
        val query = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
        return request("GET", "/api/v1/admin/breach$query")
    }

    /**
     * Get a single breach by ID.
     */
    suspend fun getBreach(id: String): BreachResult {
        return request("GET", "/api/v1/admin/breach/$id")
    }

    /**
     * Update a breach's status.
     */
    suspend fun updateBreachStatus(id: String, input: UpdateBreachInput): BreachResult {
        val body = buildJsonObject {
            put("status", input.status.name.lowercase())
            input.notificationStatus?.let { put("notification_status", it.name.lowercase()) }
            input.notes?.let { put("notes", it) }
        }
        return requestWithMethod("PATCH", "/api/v1/admin/breach/$id", body)
    }

    // =========================================================================
    // Voice Analysis
    // =========================================================================

    /**
     * Analyze voice content for safety concerns.
     *
     * @param file Audio file bytes.
     * @param filename Name of the audio file.
     * @param analysisType Type of analysis: "all", "transcription", or "safety".
     * @param fileId Optional file identifier.
     * @param externalId Your identifier for correlation.
     * @param customerId Customer identifier.
     * @param metadata Custom metadata.
     * @param ageGroup Age group of the speaker.
     * @param language Language of the audio.
     * @param platform Platform identifier.
     * @param childAge Age of the child.
     * @param contentType MIME type of the audio. Defaults to the type for the filename's extension.
     * @return VoiceAnalysisResult with transcription and safety analysis.
     */
    suspend fun analyzeVoice(
        file: ByteArray,
        filename: String,
        analysisType: String = "all",
        fileId: String? = null,
        externalId: String? = null,
        customerId: String? = null,
        metadata: Map<String, Any>? = null,
        ageGroup: String? = null,
        language: String? = null,
        platform: String? = null,
        childAge: Int? = null,
        contentType: String? = null,
    ): VoiceAnalysisResult {
        val data = multipartRequest("/api/v1/safety/voice") {
            appendFile(file, filename, contentType)
            append("analysis_type", analysisType)
            append("platform", resolvePlatform(platform))
            fileId?.let { append("file_id", it) }
            externalId?.let { append("external_id", it) }
            customerId?.let { append("customer_id", it) }
            metadata?.let { append("metadata", json.encodeToString(mapToJsonObject(it))) }
            ageGroup?.let { append("age_group", it) }
            language?.let { append("language", it) }
            childAge?.let { append("child_age", it.toString()) }
        }
        return json.decodeFromJsonElement(data)
    }

    // =========================================================================
    // Image Analysis
    // =========================================================================

    /**
     * Analyze image content for safety concerns.
     *
     * @param file Image file bytes.
     * @param filename Name of the image file.
     * @param analysisType Type of analysis: "all", "vision", or "safety".
     * @param fileId Optional file identifier.
     * @param externalId Your identifier for correlation.
     * @param customerId Customer identifier.
     * @param metadata Custom metadata.
     * @param ageGroup Age group context.
     * @param platform Platform identifier.
     * @param contentType MIME type of the image. Defaults to the type for the filename's extension.
     * @return ImageAnalysisResult with vision and safety analysis.
     */
    suspend fun analyzeImage(
        file: ByteArray,
        filename: String,
        analysisType: String = "all",
        fileId: String? = null,
        externalId: String? = null,
        customerId: String? = null,
        metadata: Map<String, Any>? = null,
        ageGroup: String? = null,
        platform: String? = null,
        contentType: String? = null,
    ): ImageAnalysisResult {
        val data = multipartRequest("/api/v1/safety/image") {
            appendFile(file, filename, contentType)
            append("analysis_type", analysisType)
            append("platform", resolvePlatform(platform))
            fileId?.let { append("file_id", it) }
            externalId?.let { append("external_id", it) }
            customerId?.let { append("customer_id", it) }
            metadata?.let { append("metadata", json.encodeToString(mapToJsonObject(it))) }
            ageGroup?.let { append("age_group", it) }
        }
        return json.decodeFromJsonElement(data)
    }

    // =========================================================================
    // Fraud Detection
    // =========================================================================

    /**
     * Detect social engineering attempts.
     */
    suspend fun detectSocialEngineering(input: DetectionInput): DetectionResult =
        request("/api/v1/fraud/social-engineering", buildDetectionBody(input))

    /**
     * Detect social engineering attempts.
     */
    suspend fun detectSocialEngineering(
        content: String,
        context: AnalysisContext? = null,
        includeEvidence: Boolean = false,
        externalId: String? = null,
        customerId: String? = null,
        metadata: Map<String, Any?>? = null
    ): DetectionResult = detectSocialEngineering(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    /**
     * Detect app fraud patterns.
     */
    suspend fun detectAppFraud(input: DetectionInput): DetectionResult =
        request("/api/v1/fraud/app-fraud", buildDetectionBody(input))

    /**
     * Detect app fraud patterns.
     */
    suspend fun detectAppFraud(
        content: String,
        context: AnalysisContext? = null,
        includeEvidence: Boolean = false,
        externalId: String? = null,
        customerId: String? = null,
        metadata: Map<String, Any?>? = null
    ): DetectionResult = detectAppFraud(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    /**
     * Detect romance scam patterns.
     */
    suspend fun detectRomanceScam(input: DetectionInput): DetectionResult =
        request("/api/v1/fraud/romance-scam", buildDetectionBody(input))

    /**
     * Detect romance scam patterns.
     */
    suspend fun detectRomanceScam(
        content: String,
        context: AnalysisContext? = null,
        includeEvidence: Boolean = false,
        externalId: String? = null,
        customerId: String? = null,
        metadata: Map<String, Any?>? = null
    ): DetectionResult = detectRomanceScam(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    /**
     * Detect mule recruitment patterns.
     */
    suspend fun detectMuleRecruitment(input: DetectionInput): DetectionResult =
        request("/api/v1/fraud/mule-recruitment", buildDetectionBody(input))

    /**
     * Detect mule recruitment patterns.
     */
    suspend fun detectMuleRecruitment(
        content: String,
        context: AnalysisContext? = null,
        includeEvidence: Boolean = false,
        externalId: String? = null,
        customerId: String? = null,
        metadata: Map<String, Any?>? = null
    ): DetectionResult = detectMuleRecruitment(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    // =========================================================================
    // Safety Extended
    // =========================================================================

    /**
     * Detect gambling harm patterns.
     */
    suspend fun detectGamblingHarm(input: DetectionInput): DetectionResult =
        request("/api/v1/safety/gambling-harm", buildDetectionBody(input))

    /**
     * Detect gambling harm patterns.
     */
    suspend fun detectGamblingHarm(
        content: String, context: AnalysisContext? = null, includeEvidence: Boolean = false,
        externalId: String? = null, customerId: String? = null, metadata: Map<String, Any?>? = null
    ): DetectionResult = detectGamblingHarm(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    /**
     * Detect coercive control patterns.
     */
    suspend fun detectCoerciveControl(input: DetectionInput): DetectionResult =
        request("/api/v1/safety/coercive-control", buildDetectionBody(input))

    /**
     * Detect coercive control patterns.
     */
    suspend fun detectCoerciveControl(
        content: String, context: AnalysisContext? = null, includeEvidence: Boolean = false,
        externalId: String? = null, customerId: String? = null, metadata: Map<String, Any?>? = null
    ): DetectionResult = detectCoerciveControl(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    /**
     * Detect vulnerability exploitation patterns.
     */
    suspend fun detectVulnerabilityExploitation(input: DetectionInput): DetectionResult =
        request("/api/v1/safety/vulnerability-exploitation", buildDetectionBody(input))

    /**
     * Detect vulnerability exploitation patterns.
     */
    suspend fun detectVulnerabilityExploitation(
        content: String, context: AnalysisContext? = null, includeEvidence: Boolean = false,
        externalId: String? = null, customerId: String? = null, metadata: Map<String, Any?>? = null
    ): DetectionResult = detectVulnerabilityExploitation(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    /**
     * Detect radicalisation patterns.
     */
    suspend fun detectRadicalisation(input: DetectionInput): DetectionResult =
        request("/api/v1/safety/radicalisation", buildDetectionBody(input))

    /**
     * Detect radicalisation patterns.
     */
    suspend fun detectRadicalisation(
        content: String, context: AnalysisContext? = null, includeEvidence: Boolean = false,
        externalId: String? = null, customerId: String? = null, metadata: Map<String, Any?>? = null
    ): DetectionResult = detectRadicalisation(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    /**
     * Detect distress signals.
     */
    suspend fun detectDistressSignals(input: DetectionInput): DetectionResult =
        request("/api/v1/safety/distress-signals", buildDetectionBody(input))

    /**
     * Detect distress signals.
     */
    suspend fun detectDistressSignals(
        content: String, context: AnalysisContext? = null, includeEvidence: Boolean = false,
        externalId: String? = null, customerId: String? = null, metadata: Map<String, Any?>? = null
    ): DetectionResult = detectDistressSignals(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    /**
     * Detect technology-facilitated gender-based violence.
     */
    suspend fun detectTfgbv(input: DetectionInput): DetectionResult =
        request("/api/v1/safety/tfgbv", buildDetectionBody(input))

    /**
     * Detect technology-facilitated gender-based violence.
     */
    suspend fun detectTfgbv(
        content: String, context: AnalysisContext? = null, includeEvidence: Boolean = false,
        externalId: String? = null, customerId: String? = null, metadata: Map<String, Any?>? = null
    ): DetectionResult = detectTfgbv(DetectionInput(content, context, includeEvidence, externalId, customerId, metadata))

    // =========================================================================
    // Nickname
    // =========================================================================

    /**
     * Review a display name. Returns a signal about the name, not a verdict on the person.
     *
     * @param input DetectNicknameInput with the name and optional context.
     * @return NicknameResult with the signal and a recommendation.
     */
    suspend fun detectNickname(input: DetectNicknameInput): NicknameResult {
        val body = buildJsonObject {
            put("name", input.name)
            input.strictness?.let { put("strictness", it.value) }
            val context = buildJsonObject {
                input.language?.let { put("language", it) }
                input.ageGroup?.let { put("age_group", it) }
                input.conversationType?.let { put("conversation_type", it) }
                input.platformReserved?.let { putJsonArray("platform_reserved") { it.forEach { n -> add(n) } } }
            }
            if (context.isNotEmpty()) put("context", context)
        }
        return request("/api/v1/safety/nickname", body)
    }

    // =========================================================================
    // Synthetic Content
    // =========================================================================

    /**
     * Detect AI-generated or synthetic text.
     */
    suspend fun detectSyntheticText(input: DetectSyntheticTextInput): SyntheticContentResult {
        val body = buildJsonObject {
            put("text", input.content)
            putJsonObject("context") {
                input.context?.let { ctx ->
                    ctx.language?.let { put("language", it) }
                    ctx.ageGroup?.let { put("age_group", it) }
                    ctx.relationship?.let { put("relationship", it) }
                }
                put("platform", resolvePlatform(input.context?.platform))
            }
            input.supportThreshold?.let { putJsonObject("options") { put("support_threshold", it) } }
            input.externalId?.let { put("external_id", it) }
            input.customerId?.let { put("customer_id", it) }
            input.metadata?.let { put("metadata", mapToJsonObject(it)) }
        }
        return request("/api/v1/safety/synthetic-content", body)
    }

    /**
     * Detect an AI-generated or manipulated image.
     */
    suspend fun detectSyntheticImage(input: DetectSyntheticMediaInput): SyntheticContentResult =
        syntheticMediaRequest("/api/v1/safety/synthetic-content/image", input)

    /**
     * Detect synthetic or cloned audio.
     */
    suspend fun detectSyntheticAudio(input: DetectSyntheticMediaInput): SyntheticContentResult =
        syntheticMediaRequest("/api/v1/safety/synthetic-content/audio", input)

    /**
     * Detect a synthetic or manipulated video.
     */
    suspend fun detectSyntheticVideo(input: DetectSyntheticMediaInput): SyntheticContentResult =
        syntheticMediaRequest("/api/v1/safety/synthetic-content/video", input)

    private suspend fun syntheticMediaRequest(path: String, input: DetectSyntheticMediaInput): SyntheticContentResult {
        val data = multipartRequest(path) {
            appendFile(input.file, input.filename, input.contentType)
            append("platform", resolvePlatform(input.platform))
            input.maxFrames?.let { append("max_frames", it.toString()) }
            input.ageGroup?.let { append("age_group", it) }
            input.language?.let { append("language", it) }
            input.fileId?.let { append("file_id", it) }
            input.externalId?.let { append("external_id", it) }
            input.customerId?.let { append("customer_id", it) }
            input.metadata?.let { append("metadata", json.encodeToString(mapToJsonObject(it))) }
        }
        return json.decodeFromJsonElement(data)
    }

    // =========================================================================
    // Multi-Endpoint Analysis
    // =========================================================================

    /**
     * Analyse content across multiple detection endpoints simultaneously.
     *
     * @param input AnalyseMultiInput with content and target detections.
     * @return AnalyseMultiResult with combined results and summary.
     */
    suspend fun analyseMulti(input: AnalyseMultiInput): AnalyseMultiResult {
        val body = buildJsonObject {
            put("text", input.content)
            putJsonArray("endpoints") { input.detections.forEach { add(it.endpoint) } }
            putJsonObject("context") {
                input.context?.let { ctx ->
                    ctx.language?.let { put("language", it) }
                    ctx.ageGroup?.let { put("age_group", it) }
                    ctx.relationship?.let { put("relationship", it) }
                }
                put("platform", resolvePlatform(input.context?.platform))
            }
            if (input.includeEvidence) {
                putJsonObject("options") { put("include_evidence", true) }
            }
            input.externalId?.let { put("external_id", it) }
            input.customerId?.let { put("customer_id", it) }
            input.metadata?.let { put("metadata", mapToJsonObject(it)) }
        }
        return request("/api/v1/analyse/multi", body)
    }

    // =========================================================================
    // Video Analysis
    // =========================================================================

    /**
     * Analyze video content for safety concerns.
     *
     * @param file Video file bytes.
     * @param filename Name of the video file.
     * @param fileId Optional file identifier.
     * @param externalId Your identifier for correlation.
     * @param customerId Customer identifier.
     * @param metadata Custom metadata.
     * @param ageGroup Age group context.
     * @param platform Platform identifier.
     * @param contentType MIME type of the video. Defaults to the type for the filename's extension.
     * @return VideoAnalysisResult with safety findings.
     */
    suspend fun analyzeVideo(
        file: ByteArray,
        filename: String,
        fileId: String? = null,
        externalId: String? = null,
        customerId: String? = null,
        metadata: Map<String, Any?>? = null,
        ageGroup: String? = null,
        platform: String? = null,
        contentType: String? = null
    ): VideoAnalysisResult {
        val data = multipartRequest("/api/v1/safety/video") {
            appendFile(file, filename, contentType)
            append("platform", resolvePlatform(platform))
            fileId?.let { append("file_id", it) }
            externalId?.let { append("external_id", it) }
            customerId?.let { append("customer_id", it) }
            metadata?.let { append("metadata", json.encodeToString(mapToJsonObject(it))) }
            ageGroup?.let { append("age_group", it) }
        }
        return json.decodeFromJsonElement(data)
    }

    // =========================================================================
    // Document Analysis
    // =========================================================================

    /**
     * Analyze a PDF, or a single png, jpeg or webp image, across several detectors.
     * Each page is analyzed on its own; the result has per-page findings and an overall assessment.
     *
     * @param input AnalyzeDocumentInput with the file and options.
     * @return DocumentAnalysisResult with per-page and overall findings.
     */
    suspend fun analyzeDocument(input: AnalyzeDocumentInput): DocumentAnalysisResult {
        val data = multipartRequest("/api/v1/safety/document") {
            appendFile(input.file, input.filename, input.contentType)
            append("platform", resolvePlatform(input.platform))
            input.endpoints?.let { append("endpoints", json.encodeToString(it)) }
            input.fileId?.let { append("file_id", it) }
            input.ageGroup?.let { append("age_group", it) }
            input.language?.let { append("language", it) }
            input.supportThreshold?.let { append("support_threshold", it) }
            if (input.splitBySender) append("split_by_sender", "true")
            input.externalId?.let { append("external_id", it) }
            input.customerId?.let { append("customer_id", it) }
            input.metadata?.let { append("metadata", json.encodeToString(mapToJsonObject(it))) }
        }
        return json.decodeFromJsonElement(data)
    }

    // =========================================================================
    // Verification
    // =========================================================================

    /**
     * Create a verification session.
     *
     * @param input Session configuration with mode and optional document type.
     * @return VerificationSession with URL to redirect user.
     */
    suspend fun createVerificationSession(input: CreateVerificationSessionInput): VerificationSession {
        val body = buildJsonObject {
            put("mode", input.mode.name.lowercase())
            input.documentType?.let { put("document_type", it.value) }
            input.chipPolicy?.let { put("chip_policy", it.value) }
            input.autoCapture?.let { put("auto_capture", it) }
        }
        // One attempt only: each session reserves a verification, so a retry after a
        // lost response could create and reserve a second one.
        return performRequest("POST", "/api/v1/verify/session", body)
    }

    /**
     * Get the current status of a verification session.
     *
     * @param id The session ID from createVerificationSession.
     * @return VerificationSessionResult with current status.
     */
    suspend fun getVerificationSession(id: String): VerificationSessionResult {
        return request("GET", "/api/v1/verify/session/${id.encodeURLPathPart()}")
    }

    /**
     * Cancel a verification session.
     *
     * @param id The session ID to cancel.
     * @return CancelVerificationSessionResult; the session then reports [VerificationSessionStatus.EXPIRED].
     */
    suspend fun cancelVerificationSession(id: String): CancelVerificationSessionResult {
        return request("DELETE", "/api/v1/verify/session/${id.encodeURLPathPart()}")
    }

    /**
     * Retrieve a past age verification result.
     *
     * @param id The verification ID.
     * @return VerificationRetrieveResult with verification details.
     */
    suspend fun getAgeVerification(id: String): VerificationRetrieveResult {
        return request("GET", "/api/v1/verify/age/${id.encodeURLPathPart()}")
    }

    /**
     * Retrieve a past identity verification result.
     *
     * @param id The verification ID.
     * @return IdentityRetrieveResult with verification details.
     */
    suspend fun getIdentityVerification(id: String): IdentityRetrieveResult {
        return request("GET", "/api/v1/verify/identity/${id.encodeURLPathPart()}")
    }

    /**
     * Retrieve any past verification result by ID.
     *
     * @param id The verification ID.
     * @return VerificationRecord with the decision and its signals. No personal data is returned.
     */
    suspend fun getVerification(id: String): VerificationRecord {
        return request("GET", "/api/v1/verify/${id.encodeURLPathPart()}")
    }

    /**
     * Get the age verification configuration that applies to a market.
     *
     * @param country ISO 3166-1 alpha-2 country code, for example "DE".
     * @param region Region or state code, for example "CA".
     * @return VerificationConfig with the check and age threshold.
     */
    suspend fun getVerificationConfig(country: String? = null, region: String? = null): VerificationConfig {
        return request("GET", "/api/v1/verify/age/config${query("country" to country, "region" to region)}")
    }

    /**
     * Run face match, age, forensic and synthetic checks on an image.
     *
     * @param input VerifyImageMatchInput with the image and an optional reference face.
     * @return VerifyImageMatchResult with a section per check that ran.
     */
    suspend fun verifyImageMatch(input: VerifyImageMatchInput): VerifyImageMatchResult {
        val encoder = java.util.Base64.getEncoder()
        val body = buildJsonObject {
            put("image", encoder.encodeToString(input.image))
            input.referenceImage?.let { put("reference_image", encoder.encodeToString(it)) }
            input.checks?.let { putJsonArray("checks") { it.forEach { c -> add(c.value) } } }
            input.externalId?.let { put("external_id", it) }
            input.customerId?.let { put("customer_id", it) }
            input.metadata?.let { put("metadata", mapToJsonObject(it)) }
        }
        return request("/api/v1/verify-image-match", body)
    }

    // =========================================================================
    // Incidents
    // =========================================================================

    /**
     * List incidents, newest first, one page at a time.
     *
     * @param input Optional filters and paging.
     * @return IncidentListResult; pass its nextCursor to get the next page.
     */
    suspend fun listIncidents(input: ListIncidentsInput = ListIncidentsInput()): IncidentListResult {
        val q = query(
            "category" to input.category,
            "severity" to input.severity,
            "status" to input.status,
            "source" to input.source,
            "from" to input.from,
            "to" to input.to,
            "platform" to input.platform,
            "external_id" to input.externalId,
            "customer_id" to input.customerId,
            "limit" to input.limit,
            "cursor" to input.cursor,
            "include_summary" to (if (input.includeSummary) "true" else null)
        )
        return request("GET", "/api/v1/incidents$q")
    }

    /**
     * Get one incident in full.
     */
    suspend fun getIncident(incidentId: String): IncidentDetail {
        return request("GET", "/api/v1/incidents/${incidentId.encodeURLPathPart()}")
    }

    /**
     * Get incident counts over a window.
     *
     * @param from Start of the window, ISO 8601.
     * @param to End of the window, ISO 8601.
     */
    suspend fun getIncidentsOverview(from: String? = null, to: String? = null): IncidentsOverview {
        return request("GET", "/api/v1/incidents/overview${query("from" to from, "to" to to)}")
    }

    /**
     * Get incident counts over time.
     *
     * @param bucket Bucket size. API default: day.
     * @param from Start of the window, ISO 8601.
     * @param to End of the window, ISO 8601.
     */
    suspend fun getIncidentTrends(
        bucket: IncidentTrendBucket? = null,
        from: String? = null,
        to: String? = null
    ): IncidentTrends {
        return request("GET", "/api/v1/incidents/trends${query("bucket" to bucket?.value, "from" to from, "to" to to)}")
    }

    /**
     * Record a moderator review of an incident.
     */
    suspend fun reviewIncident(incidentId: String, input: ReviewIncidentInput): ReviewIncidentResult {
        val body = buildReviewBody(
            input.action, input.reasonCode, input.moderatorExternalId, input.reasonComment,
            input.newRiskLevel, input.newRiskCategory, input.retentionClass
        )
        return request("/api/v1/incidents/${incidentId.encodeURLPathPart()}/review", body)
    }

    /**
     * Apply one moderator review to up to 100 incidents.
     */
    suspend fun batchReviewIncidents(input: BatchReviewIncidentsInput): BatchReviewIncidentsResult {
        require(input.incidentIds.isNotEmpty()) { "incidentIds must not be empty" }
        require(input.incidentIds.size <= 100) { "At most 100 incidents per call" }
        val review = buildReviewBody(
            input.action, input.reasonCode, input.moderatorExternalId, input.reasonComment,
            input.newRiskLevel, input.newRiskCategory, input.retentionClass
        )
        val body = buildJsonObject {
            putJsonArray("incident_ids") { input.incidentIds.forEach { add(it) } }
            review.forEach { (key, value) -> put(key, value) }
        }
        return request("/api/v1/incidents/batch-review", body)
    }

    private fun buildReviewBody(
        action: ModeratorAction,
        reasonCode: ModeratorReasonCode,
        moderatorExternalId: String,
        reasonComment: String?,
        newRiskLevel: String?,
        newRiskCategory: String?,
        retentionClass: RetentionClass?
    ): JsonObject {
        require(moderatorExternalId.isNotBlank()) { "moderatorExternalId is required" }
        require(action != ModeratorAction.RECLASSIFY || newRiskCategory != null) {
            "RECLASSIFY requires newRiskCategory"
        }
        return buildJsonObject {
            put("action", action.value)
            put("reason_code", reasonCode.value)
            put("moderator_external_id", moderatorExternalId)
            reasonComment?.let { put("reason_comment", it) }
            newRiskLevel?.let { put("new_risk_level", it) }
            newRiskCategory?.let { put("new_risk_category", it) }
            retentionClass?.let { put("retention_class", it.value) }
        }
    }

    // =========================================================================
    // Privacy
    // =========================================================================

    /**
     * Remove personal data from a text, or from every message of one conversation
     * with consistent placeholders. Nothing is stored.
     *
     * @param input RedactInput with text or messages.
     * @return RedactResult with the redacted text and counts per type.
     */
    suspend fun redact(input: RedactInput): RedactResult {
        val body = buildJsonObject {
            input.text?.let { put("text", it) }
            input.messages?.let { putJsonArray("messages") { it.forEach { m -> add(m) } } }
            input.language?.let { put("language", it) }
            input.mode?.let { put("mode", it.value) }
            input.strictAnonymisation?.let { put("strict_anonymisation", it) }
        }
        return request("/api/v1/privacy/redact", body)
    }

    // =========================================================================
    // Webhooks
    // =========================================================================

    /**
     * List all configured webhooks.
     *
     * @return WebhookListResult with all webhooks.
     */
    suspend fun listWebhooks(): WebhookListResult {
        val data: WebhookListResult = request("GET", "/api/v1/webhooks")
        return data
    }

    /**
     * Create a new webhook.
     *
     * @param input CreateWebhookInput with name, URL and events.
     * @return CreateWebhookResult with the created webhook and its signing secret.
     */
    suspend fun createWebhook(input: CreateWebhookInput): CreateWebhookResult {
        val body = buildJsonObject {
            put("name", input.name)
            put("url", input.url)
            putJsonArray("events") { input.events.forEach { add(it) } }
            input.headers?.let { put("headers", mapToJsonObject(it)) }
        }
        return request("/api/v1/webhooks", body)
    }

    /**
     * Update an existing webhook.
     *
     * @param webhookId ID of the webhook to update.
     * @param input UpdateWebhookInput with fields to update.
     * @return UpdateWebhookResult with the updated webhook.
     */
    suspend fun updateWebhook(webhookId: String, input: UpdateWebhookInput): UpdateWebhookResult {
        val body = buildJsonObject {
            input.name?.let { put("name", it) }
            input.url?.let { put("url", it) }
            input.events?.let { putJsonArray("events") { it.forEach { e -> add(e) } } }
            input.active?.let { put("is_active", it) }
            input.headers?.let { put("headers", mapToJsonObject(it)) }
        }
        return requestWithMethod("PUT", "/api/v1/webhooks/${webhookId.encodeURLPathPart()}", body)
    }

    /**
     * Delete a webhook.
     *
     * @param webhookId ID of the webhook to delete.
     * @return DeleteWebhookResult confirmation.
     */
    suspend fun deleteWebhook(webhookId: String): DeleteWebhookResult {
        return request("DELETE", "/api/v1/webhooks/${webhookId.encodeURLPathPart()}")
    }

    /**
     * Send a test event to a webhook.
     *
     * @param webhookId ID of the webhook to test.
     * @return TestWebhookResult with delivery status.
     */
    suspend fun testWebhook(webhookId: String): TestWebhookResult {
        return request("/api/v1/webhooks/test", buildJsonObject { put("webhook_id", webhookId) })
    }

    /**
     * Regenerate the signing secret for a webhook.
     *
     * @param webhookId ID of the webhook.
     * @return RegenerateSecretResult with the new secret.
     */
    suspend fun regenerateWebhookSecret(webhookId: String): RegenerateSecretResult {
        return requestWithMethod("POST", "/api/v1/webhooks/${webhookId.encodeURLPathPart()}/regenerate-secret", null)
    }

    // =========================================================================
    // Pricing
    // =========================================================================

    /**
     * Get available pricing plans.
     *
     * @return PricingResult with plan summaries.
     */
    suspend fun getPricing(): PricingResult {
        return request("GET", "/api/v1/pricing")
    }

    /**
     * Get detailed pricing information including limits and features.
     *
     * @return PricingDetailsResult with detailed plan information.
     */
    suspend fun getPricingDetails(): PricingDetailsResult {
        return request("GET", "/api/v1/pricing/details")
    }

    // =========================================================================
    // Usage
    // =========================================================================

    /**
     * Get usage history.
     *
     * @param days Number of days to retrieve (default: 30).
     * @return UsageHistoryResult with daily usage data.
     */
    suspend fun getUsageHistory(days: Int? = null): UsageHistoryResult {
        val params = if (days != null) "?days=$days" else ""
        return request("GET", "/api/v1/usage/history$params")
    }

    /**
     * Get usage breakdown by tool/endpoint.
     *
     * @param date Date to query in YYYY-MM-DD format (default: today).
     * @return UsageByToolResult with per-tool usage counts.
     */
    suspend fun getUsageByTool(date: String? = null): UsageByToolResult {
        val params = if (date != null) "?date=$date" else ""
        return request("GET", "/api/v1/usage/by-tool$params")
    }

    /**
     * Get monthly usage summary with billing and rate limit info.
     *
     * @return UsageMonthlyResult with monthly overview.
     */
    suspend fun getUsageMonthly(): UsageMonthlyResult {
        return request("GET", "/api/v1/usage/monthly")
    }

    /**
     * Get the daily usage summary for this API key.
     *
     * @param date Date to query in YYYY-MM-DD format (default: today).
     * @return UsageSummaryResult with request counts and quota.
     */
    suspend fun getUsageSummary(date: String? = null): UsageSummaryResult {
        return request("GET", "/api/v1/usage/summary${query("date" to date)}")
    }

    /**
     * Get the rate limit and quota state for this API key.
     *
     * @return UsageQuotaResult with limits, current counts and what remains.
     */
    suspend fun getQuota(): UsageQuotaResult {
        return request("GET", "/api/v1/usage/quota")
    }

    // =========================================================================
    // Private Methods
    // =========================================================================

    private suspend inline fun <reified T> request(path: String, body: JsonObject): T {
        return requestWithMethod("POST", path, body)
    }

    private suspend inline fun <reified T> request(method: String, path: String): T {
        return requestWithMethod(method, path, null)
    }

    private suspend inline fun <reified T> requestWithMethod(method: String, path: String, body: JsonObject?): T {
        var lastException: Exception? = null

        for (attempt in 0 until maxRetries) {
            try {
                return performRequest(method, path, body)
            } catch (e: AuthenticationException) {
                throw e
            } catch (e: ValidationException) {
                throw e
            } catch (e: NotFoundException) {
                throw e
            } catch (e: QuotaExceededException) {
                throw e
            } catch (e: TierAccessException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(retryDelay * (1L shl attempt))
                }
            }
        }

        throw lastException ?: TuteliqException("Request failed after retries")
    }

    private suspend inline fun <reified T> performRequest(method: String, path: String, body: JsonObject?): T {
        val response: HttpResponse
        try {
            response = client.request("$baseUrl$path") {
                this.method = HttpMethod.parse(method)
                if (body != null) setBody(body)
            }
        } catch (e: HttpRequestTimeoutException) {
            throw TimeoutException("Request timed out after ${timeout}ms")
        } catch (e: Exception) {
            throw NetworkException(e.message ?: "Network error")
        }

        lastRequestId = response.headers["x-request-id"]

        // Monthly usage headers
        val limit = response.headers["x-monthly-limit"]?.toIntOrNull()
        val used = response.headers["x-monthly-used"]?.toIntOrNull()
        val remaining = response.headers["x-monthly-remaining"]?.toIntOrNull()

        if (limit != null && used != null && remaining != null) {
            usage = Usage(limit, used, remaining)
        }

        if (!response.status.isSuccess()) {
            handleErrorResponse(response)
        }

        return response.body()
    }

    private suspend fun handleErrorResponse(response: HttpResponse): Nothing {
        val status = response.status.value
        val body = response.bodyAsText()
        handleError(status, body)
    }

    private fun handleError(status: Int, body: String): Nothing {
        val (message, details) = try {
            val data = json.parseToJsonElement(body).jsonObject
            val error = data["error"]?.jsonObject
            val msg = error?.get("message")?.jsonPrimitive?.content ?: "Request failed"
            val det = error?.get("details")
            msg to det
        } catch (e: Exception) {
            "Request failed" to null
        }

        throw when (status) {
            400 -> ValidationException(message, details)
            401 -> AuthenticationException(message, details)
            403 -> {
                val msg = message.lowercase()
                when {
                    msg.contains("quota") -> QuotaExceededException(message, details)
                    msg.contains("tier") -> TierAccessException(message, details)
                    else -> TierAccessException(message, details)
                }
            }
            404 -> NotFoundException(message, details)
            429 -> RateLimitException(message, details)
            in 500..599 -> ServerException(message, status, details)
            else -> TuteliqException(message, details)
        }
    }

    private fun updateUsage(response: HttpResponse) {
        lastRequestId = response.headers["x-request-id"]

        val limit = response.headers["x-monthly-limit"]?.toIntOrNull()
        val used = response.headers["x-monthly-used"]?.toIntOrNull()
        val remaining = response.headers["x-monthly-remaining"]?.toIntOrNull()

        if (limit != null && used != null && remaining != null) {
            usage = Usage(limit, used, remaining)
        }
    }

    private suspend fun multipartRequest(path: String, formBuilder: FormBuilder.() -> Unit): JsonObject {
        val url = "$baseUrl$path"
        for (attempt in 0..maxRetries) {
            try {
                val response = client.submitFormWithBinaryData(
                    url = url,
                    formData = formData(formBuilder)
                ) {
                    header("X-API-Key", apiKey)
                }
                val body = response.bodyAsText()
                if (response.status.value >= 400) {
                    handleError(response.status.value, body)
                }
                updateUsage(response)
                return json.parseToJsonElement(body).jsonObject
            } catch (e: TuteliqException) {
                throw e
            } catch (e: Exception) {
                if (attempt == maxRetries) throw NetworkException(e.message ?: "Network error")
                delay(retryDelay * (1L shl attempt))
            }
        }
        throw NetworkException("Max retries exceeded")
    }

    /**
     * Appends the upload as the `file` part. The part needs a Content-Type: the API
     * checks it against the types each endpoint accepts and rejects a part without one.
     */
    private fun FormBuilder.appendFile(file: ByteArray, filename: String, contentType: String?) {
        append("file", file, Headers.build {
            append(HttpHeaders.ContentDisposition, "filename=\"${escapeFilename(filename)}\"")
            append(HttpHeaders.ContentType, contentType ?: mimeTypeForFilename(filename) ?: "application/octet-stream")
        })
    }

    private fun buildDetectionBody(input: DetectionInput): JsonObject {
        return buildJsonObject {
            put("text", input.content)
            putJsonObject("context") {
                input.context?.let { ctx ->
                    ctx.language?.let { put("language", it) }
                    ctx.ageGroup?.let { put("age_group", it) }
                    ctx.relationship?.let { put("relationship", it) }
                }
                put("platform", resolvePlatform(input.context?.platform))
            }
            if (input.includeEvidence) put("include_evidence", true)
            input.externalId?.let { put("external_id", it) }
            input.customerId?.let { put("customer_id", it) }
            input.metadata?.let { put("metadata", mapToJsonObject(it)) }
        }
    }

    /** Query string from the non-null pairs, encoded; empty when every value is null. */
    private fun query(vararg pairs: Pair<String, Any?>): String {
        val parameters = Parameters.build {
            pairs.forEach { (key, value) -> if (value != null) append(key, value.toString()) }
        }
        return if (parameters.isEmpty()) "" else "?${parameters.formUrlEncode()}"
    }

    private fun mapToJsonObject(map: Map<String, Any?>): JsonObject {
        return buildJsonObject {
            map.forEach { (key, value) ->
                put(key, toJsonElement(value))
            }
        }
    }

    private fun toJsonElement(value: Any?): JsonElement {
        return when (value) {
            null -> JsonNull
            is Map<*, *> -> {
                buildJsonObject {
                    value.forEach { (k, v) ->
                        put(k.toString(), toJsonElement(v))
                    }
                }
            }
            is List<*> -> {
                buildJsonArray {
                    value.forEach { add(toJsonElement(it)) }
                }
            }
            is Boolean -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            is JsonElement -> value
            else -> JsonPrimitive(value.toString())
        }
    }
}

private val UPLOAD_MIME_TYPES = mapOf(
    "png" to "image/png", "jpg" to "image/jpeg", "jpeg" to "image/jpeg",
    "gif" to "image/gif", "webp" to "image/webp",
    "mp3" to "audio/mpeg", "wav" to "audio/wav", "m4a" to "audio/m4a",
    "ogg" to "audio/ogg", "flac" to "audio/flac",
    "mp4" to "video/mp4", "webm" to "video/webm", "mov" to "video/quicktime", "avi" to "video/x-msvideo",
    "pdf" to "application/pdf"
)

/** Filename safe inside a quoted header value: line breaks dropped, backslash and quote escaped. */
internal fun escapeFilename(filename: String): String =
    filename.replace("\r", "").replace("\n", "").replace("\\", "\\\\").replace("\"", "\\\"")

/** MIME type for an upload, from its filename's extension; null when the extension is unknown. */
internal fun mimeTypeForFilename(filename: String): String? =
    UPLOAD_MIME_TYPES[filename.substringAfterLast('.', "").lowercase()]
