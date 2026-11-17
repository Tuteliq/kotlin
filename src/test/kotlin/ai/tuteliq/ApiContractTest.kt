package ai.tuteliq

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Request and response shapes checked against a stub of the API.
 * Every value here is invented.
 */
class ApiContractTest {

    private class Stub(private val responseBody: String, private val status: HttpStatusCode = HttpStatusCode.OK) {
        val requests = mutableListOf<HttpRequestData>()
        val bodies = mutableListOf<String>()

        val engine = MockEngine { request ->
            requests += request
            bodies += String(request.body.toByteArray())
            respond(responseBody, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }

        fun client() = Tuteliq("test-api-key-12345", 5_000L, 1, 1L, "https://api.test", engine)

        val last get() = requests.last()
        val lastJson get() = Json.parseToJsonElement(bodies.last()).jsonObject
    }

    // =========================================================================
    // Uploads
    // =========================================================================

    private val imageResponse = """{"overall_risk_score":0.1,"overall_severity":"none"}"""

    @Test
    fun `image upload part carries the image content type`() = runTest {
        val stub = Stub(imageResponse)
        stub.client().analyzeImage(byteArrayOf(1, 2, 3), "photo.png")
        assertTrue(stub.bodies.last().contains("Content-Type: image/png"), stub.bodies.last())
        assertEquals(ContentType.MultiPart.FormData, stub.last.body.contentType?.withoutParameters())
        assertNull(stub.last.headers[HttpHeaders.ContentType], stub.last.headers.toString())
    }

    @Test
    fun `voice upload part carries the audio content type`() = runTest {
        val stub = Stub("""{"overall_risk_score":0.1}""")
        stub.client().analyzeVoice(byteArrayOf(1, 2, 3), "note.mp3")
        assertTrue(stub.bodies.last().contains("Content-Type: audio/mpeg"), stub.bodies.last())
    }

    @Test
    fun `video upload part carries the video content type`() = runTest {
        val stub = Stub("""{"frames_analyzed":1,"safety_findings":[],"overall_risk_score":0.0,"overall_severity":"none"}""")
        stub.client().analyzeVideo(byteArrayOf(1, 2, 3), "clip.mov")
        assertTrue(stub.bodies.last().contains("Content-Type: video/quicktime"), stub.bodies.last())
    }

    // =========================================================================
    // Verification sessions
    // =========================================================================

    @Test
    fun `session status document_required decodes`() = runTest {
        val stub = Stub("""{"session_id":"sess_1","status":"document_required","chip_policy":"required","created_at":1763380000000,"expires_at":1763380900000}""")
        val result = stub.client().getVerificationSession("sess_1")
        assertEquals(VerificationSessionStatus.DOCUMENT_REQUIRED, result.status)
    }

    @Test
    fun `an unknown session status decodes instead of throwing`() = runTest {
        val stub = Stub("""{"session_id":"sess_1","status":"some_future_state"}""")
        val result = stub.client().getVerificationSession("sess_1")
        assertEquals(VerificationSessionStatus.UNKNOWN, result.status)
    }

    @Test
    fun `cancelVerificationSession accepts the ok body`() = runTest {
        val stub = Stub("""{"ok":true}""")
        val result = stub.client().cancelVerificationSession("sess_1")
        assertTrue(result.ok)
        assertEquals(HttpMethod.Delete, stub.last.method)
        assertEquals("/api/v1/verify/session/sess_1", stub.last.url.encodedPath)
    }

    @Test
    fun `ID_CARD sends national_id`() = runTest {
        val stub = Stub("""{"session_id":"sess_1","mobile_url":"https://verify.example/age/","expires_at":1763380900000,"mode":"age"}""")
        stub.client().createVerificationSession(CreateVerificationSessionInput(VerificationMode.AGE, DocumentType.ID_CARD))
        assertEquals("national_id", stub.lastJson["document_type"]?.jsonPrimitive?.content)
    }

    @Test
    fun `age verification record decodes the fields the API returns`() = runTest {
        val stub = Stub("""{"verification_id":"ver_1","status":"verified","is_minor":false,"face_matched":true,"liveness_valid":true,"failure_reasons":[],"created_at":"2026-11-01T10:00:00.000Z"}""")
        val result = stub.client().getAgeVerification("ver_1")
        assertEquals(true, result.faceMatched)
        assertEquals(true, result.livenessValid)
        assertEquals(emptyList(), result.failureReasons)
    }

    @Test
    fun `identity verification record decodes the fields the API returns`() = runTest {
        val stub = Stub("""{"verification_id":"ver_2","status":"failed","face_matched":false,"face_confidence":0.31,"liveness_valid":true,"failure_reasons":["face_mismatch"],"created_at":"2026-11-01T10:00:00.000Z"}""")
        val result = stub.client().getIdentityVerification("ver_2")
        assertEquals(false, result.faceMatched)
        assertEquals(0.31, result.faceConfidence)
        assertEquals(listOf("face_mismatch"), result.failureReasons)
    }

    @Test
    fun `face match decodes match_score without a distance`() {
        val json = Json { ignoreUnknownKeys = true }
        val match = json.decodeFromString(FaceMatchResult.serializer(), """{"matched":true,"confidence":0.62,"match_score":0.81}""")
        assertEquals(0.81, match.matchScore)
        assertNull(match.distance)
    }

    // =========================================================================
    // Webhooks
    // =========================================================================

    private val webhookRow = """{"id":"wh_1","name":"Moderation","url":"https://hooks.example.com/t","events":["incident.created"],"is_active":true,"failure_count":0,"last_triggered_at":null,"last_error":null,"created_at":"2026-11-01T10:00:00.000Z","updated_at":"2026-11-01T10:00:00.000Z"}"""

    @Test
    fun `listWebhooks decodes is_active`() = runTest {
        val stub = Stub("""{"webhooks":[$webhookRow]}""")
        val result = stub.client().listWebhooks()
        assertTrue(result.webhooks.single().active)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `createWebhook sends a name and no active field`() = runTest {
        val stub = Stub("""{"id":"wh_1","name":"hooks.example.com","url":"https://hooks.example.com/t","secret":"whsec_invented","events":["incident.created"],"is_active":true,"created_at":"2026-11-01T10:00:00.000Z"}""")
        stub.client().createWebhook(CreateWebhookInput("https://hooks.example.com/t", listOf("incident.created")))
        val body = stub.lastJson
        assertEquals("hooks.example.com", body["name"]?.jsonPrimitive?.content)
        assertFalse("active" in body)
    }

    @Test
    fun `createWebhook sends the given name and headers and returns the secret`() = runTest {
        val stub = Stub("""{"id":"wh_1","name":"Moderation","url":"https://hooks.example.com/t","secret":"whsec_invented","events":["incident.created"],"is_active":true,"created_at":"2026-11-01T10:00:00.000Z"}""")
        val result = stub.client().createWebhook(
            CreateWebhookInput("Moderation", "https://hooks.example.com/t", listOf("incident.created"), mapOf("X-Team" to "safety"))
        )
        val body = stub.lastJson
        assertEquals("Moderation", body["name"]?.jsonPrimitive?.content)
        assertEquals("safety", body["headers"]?.jsonObject?.get("X-Team")?.jsonPrimitive?.content)
        assertEquals("whsec_invented", result.secret)
    }

    @Test
    fun `updateWebhook sends is_active with PUT`() = runTest {
        val stub = Stub("""{"id":"wh_1","name":"Moderation","url":"https://hooks.example.com/t","events":["incident.created"],"is_active":false,"updated_at":"2026-11-01T10:00:00.000Z"}""")
        stub.client().updateWebhook("wh_1", UpdateWebhookInput(active = false))
        assertEquals(HttpMethod.Put, stub.last.method)
        assertEquals(false, stub.lastJson["is_active"]?.jsonPrimitive?.boolean)
    }

    @Test
    fun `testWebhook posts the webhook id to the test route`() = runTest {
        val stub = Stub("""{"success":true,"status_code":200,"latency_ms":42}""")
        val result = stub.client().testWebhook("wh_1")
        assertEquals(HttpMethod.Post, stub.last.method)
        assertEquals("/api/v1/webhooks/test", stub.last.url.encodedPath)
        assertEquals("wh_1", stub.lastJson["webhook_id"]?.jsonPrimitive?.content)
        assertEquals(200, result.statusCode)
    }

    @Test
    fun `regenerateWebhookSecret decodes the secret-only body`() = runTest {
        val stub = Stub("""{"secret":"whsec_invented"}""")
        val result = stub.client().regenerateWebhookSecret("wh_1")
        assertEquals("/api/v1/webhooks/wh_1/regenerate-secret", stub.last.url.encodedPath)
        assertEquals("whsec_invented", result.secret)
    }

    // =========================================================================
    // Languages
    // =========================================================================

    @Test
    fun `Language lists the 32 supported codes`() {
        val expected = listOf(
            "en", "es", "pt", "uk", "sv", "no", "da", "fi", "de", "fr", "nl", "pl", "it", "tr", "ro", "el",
            "cs", "hu", "bg", "hr", "sk", "lt", "lv", "et", "sl", "mt", "ga", "zh", "ja", "ko", "ar", "ru"
        )
        assertEquals(expected, Language.all.map { it.code })
    }

    // =========================================================================
    // Session creation
    // =========================================================================

    @Test
    fun `createVerificationSession sends chip policy and auto capture and decodes the response`() = runTest {
        val stub = Stub("""{"session_id":"sess_1","mobile_url":"https://verify.example/age/?session=sess_1","expires_at":1763380900000,"mode":"identity","verification_mode":"full","chip_policy":"instead_of_photo","auto_capture":false,"recommended_image_width":1600}""")
        val session = stub.client().createVerificationSession(
            CreateVerificationSessionInput(VerificationMode.IDENTITY, DocumentType.PASSPORT, ChipPolicy.INSTEAD_OF_PHOTO, autoCapture = false)
        )
        val body = stub.lastJson
        assertEquals("identity", body["mode"]?.jsonPrimitive?.content)
        assertEquals("passport", body["document_type"]?.jsonPrimitive?.content)
        assertEquals("instead_of_photo", body["chip_policy"]?.jsonPrimitive?.content)
        assertEquals(false, body["auto_capture"]?.jsonPrimitive?.boolean)
        assertEquals(1763380900000L, session.expiresAt)
        assertEquals(ChipPolicy.INSTEAD_OF_PHOTO, session.chipPolicy)
        assertEquals(false, session.autoCapture)
        assertEquals(1600, session.recommendedImageWidth)
        assertEquals("https://verify.example/age/?session=sess_1", session.url)
    }

    @Test
    fun `createVerificationSession is not retried`() = runTest {
        val stub = Stub("""{"error":{"message":"Internal error"}}""", HttpStatusCode.InternalServerError)
        val client = Tuteliq("test-api-key-12345", 5_000L, 3, 1L, "https://api.test", stub.engine)
        runCatching { client.createVerificationSession(CreateVerificationSessionInput(VerificationMode.AGE)) }
        assertEquals(1, stub.requests.size)
    }

    @Test
    fun `session result decodes the typed outcome`() = runTest {
        val stub = Stub("""{"session_id":"sess_1","status":"completed","mode":"age","verification_mode":"liveness_only","chip_policy":"optional","result":{"status":"verified","age":24,"age_range":{"low":21,"high":27},"is_minor":false,"face_match":null,"liveness":{"valid":true},"failure_reasons":null},"created_at":1763380000000,"expires_at":1763380900000}""")
        val result = stub.client().getVerificationSession("sess_1")
        assertEquals(VerificationCheck.LIVENESS_ONLY, result.verificationMode)
        assertEquals(VerificationStatus.VERIFIED, result.result?.status)
        assertEquals(21.0, result.result?.ageRange?.low)
        assertEquals(true, result.result?.liveness?.valid)
    }

    @Test
    fun `getVerification and getVerificationConfig use the right routes`() = runTest {
        val stub = Stub("""{"verification_id":"ver_1","status":"needs_review","age":null,"is_minor":null,"face_matched":null,"face_confidence":null,"liveness_valid":true,"failure_reasons":[],"created_at":"2026-11-01T10:00:00.000Z"}""")
        val record = stub.client().getVerification("ver_1")
        assertEquals("/api/v1/verify/ver_1", stub.last.url.encodedPath)
        assertEquals(VerificationStatus.NEEDS_REVIEW, record.status)

        val config = Stub("""{"mode":"liveness_only","age_threshold":18,"country":"DE"}""")
        val result = config.client().getVerificationConfig(country = "DE")
        assertEquals("/api/v1/verify/age/config", config.last.url.encodedPath)
        assertEquals("DE", config.last.url.parameters["country"])
        assertEquals(VerificationCheck.LIVENESS_ONLY, result.mode)
    }

    // =========================================================================
    // Document analysis
    // =========================================================================

    @Test
    fun `analyzeDocument uploads a typed PDF part with its options`() = runTest {
        val stub = Stub("""{"document_hash":"ab12","total_pages":2,"pages_analyzed":2,"extraction_summary":{"text_layer_pages":2,"ocr_pages":0,"failed_pages":0,"average_ocr_confidence":0},"page_results":[{"page_number":1,"text_preview":"Invented page","extraction_method":"text_layer","results":[{"endpoint":"unsafe","detected":false,"severity":0,"confidence":0.9,"risk_score":0,"level":"none","categories":[],"evidence":[],"recommended_action":"none","rationale":"Nothing found."}],"page_risk_score":0,"page_severity":"none"}],"overall_risk_score":0,"overall_severity":"none","detected_endpoints":[],"flagged_pages":[],"credits_used":4,"processing_time_ms":812}""")
        val result = stub.client().analyzeDocument(
            AnalyzeDocumentInput(byteArrayOf(37, 80, 68, 70), "report.pdf", endpoints = listOf("unsafe", "grooming"), splitBySender = true)
        )
        val body = stub.bodies.last()
        assertEquals("/api/v1/safety/document", stub.last.url.encodedPath)
        assertTrue(body.contains("Content-Type: application/pdf"), body)
        assertTrue(body.contains("[\"unsafe\",\"grooming\"]"), body)
        assertTrue(body.contains("name=split_by_sender"), body)
        assertEquals(2, result.totalPages)
        assertEquals("unsafe", result.pageResults.single().results.single().endpoint)
    }

    // =========================================================================
    // Incidents
    // =========================================================================

    @Test
    fun `listIncidents sends encoded filters and decodes the page`() = runTest {
        val stub = Stub("""{"incidents":[{"id":"inc_1","risk_category":"grooming","risk_level":"high","confidence_score":0.9,"detected_patterns":[],"recommended_actions":[],"platform":"chat","language":"en","source":"text","file_id":null,"status":"new","policy_action":null,"recommended_status":null,"external_id":null,"customer_id":null,"created_at":"2026-11-01T10:00:00.000Z"}],"next_cursor":"cur_2","total_returned":1}""")
        val result = stub.client().listIncidents(
            ListIncidentsInput(category = "grooming", from = "2026-11-01T00:00:00+01:00", limit = 10, includeSummary = true)
        )
        val url = stub.last.url
        assertEquals("/api/v1/incidents", url.encodedPath)
        assertEquals("grooming", url.parameters["category"])
        assertEquals("2026-11-01T00:00:00+01:00", url.parameters["from"])
        assertEquals("10", url.parameters["limit"])
        assertEquals("true", url.parameters["include_summary"])
        assertEquals("cur_2", result.nextCursor)
        assertEquals("high", result.incidents.single().riskLevel)
    }

    @Test
    fun `incident detail, overview and trends use the right routes`() = runTest {
        val detail = Stub("""{"id":"inc_1","risk_category":"bullying","risk_level":"low","status":"reviewed","summary":"Invented summary","message_analysis":[{"message_index":0,"risk_score":0.2,"flags":[]}]}""")
        assertEquals("bullying", detail.client().getIncident("inc_1").riskCategory)
        assertEquals("/api/v1/incidents/inc_1", detail.last.url.encodedPath)

        val overview = Stub("""{"timeframe":{"from":"2026-10-01T00:00:00.000Z","to":"2026-11-01T00:00:00.000Z"},"total_incidents":3,"requires_review_count":1,"last_24h_count":0,"last_7d_count":1,"last_30d_count":3,"counts_by_category":{"bullying":3},"counts_by_severity":{"low":3},"counts_by_source":{"text":3},"counts_by_status":{"new":1},"top_platforms":[{"platform":"chat","count":3}]}""")
        assertEquals(3, overview.client().getIncidentsOverview(from = "2026-10-01").totalIncidents)
        assertEquals("/api/v1/incidents/overview", overview.last.url.encodedPath)

        val trends = Stub("""{"bucket_size":"week","timeframe":{"from":"2026-10-01T00:00:00.000Z","to":"2026-11-01T00:00:00.000Z"},"series":[{"bucket_start":"2026-10-06T00:00:00.000Z","total":2,"by_severity":{"low":2}}]}""")
        assertEquals(2, trends.client().getIncidentTrends(IncidentTrendBucket.WEEK).series.single().total)
        assertEquals("week", trends.last.url.parameters["bucket"])
    }

    @Test
    fun `reviewIncident and batchReviewIncidents send the review`() = runTest {
        val single = Stub("""{"incident_id":"inc_1","original":{"risk_category":"bullying","risk_level":"high"},"revised":{"risk_category":"bullying","risk_level":"low"},"audit_receipt":null}""")
        val result = single.client().reviewIncident(
            "inc_1", ReviewIncidentInput(ModeratorAction.DOWNGRADE, ModeratorReasonCode.INSUFFICIENT_SEVERITY, "mod_7", newRiskLevel = "low")
        )
        assertEquals("/api/v1/incidents/inc_1/review", single.last.url.encodedPath)
        assertEquals("downgrade", single.lastJson["action"]?.jsonPrimitive?.content)
        assertEquals("insufficient_severity", single.lastJson["reason_code"]?.jsonPrimitive?.content)
        assertEquals("mod_7", single.lastJson["moderator_external_id"]?.jsonPrimitive?.content)
        assertEquals("low", result.revised?.riskLevel)

        val batch = Stub("""{"total":2,"succeeded":1,"failed":1,"results":[{"incident_id":"inc_1","ok":true},{"incident_id":"inc_2","ok":false,"error":"not found"}]}""")
        val batchResult = batch.client().batchReviewIncidents(
            BatchReviewIncidentsInput(listOf("inc_1", "inc_2"), ModeratorAction.DISMISS, ModeratorReasonCode.FALSE_POSITIVE, "mod_7")
        )
        assertEquals("/api/v1/incidents/batch-review", batch.last.url.encodedPath)
        assertEquals(2, batch.lastJson["incident_ids"]?.jsonArray?.size)
        assertEquals(1, batchResult.failed)
    }

    @Test
    fun `reclassify without a new category is refused before any request`() = runTest {
        val stub = Stub("{}")
        val error = runCatching {
            stub.client().reviewIncident("inc_1", ReviewIncidentInput(ModeratorAction.RECLASSIFY, ModeratorReasonCode.INCORRECT_CATEGORY, "mod_7"))
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertEquals(0, stub.requests.size)
    }

    // =========================================================================
    // Other endpoints
    // =========================================================================

    @Test
    fun `detectNickname sends the name and context`() = runTest {
        val stub = Stub("""{"signal":"none","categories":[],"parts":[{"text":"Star","reading":"ordinary","category":null}],"rationale":"An ordinary name.","recommendation":"allow","recommendation_detail":"Nothing to do.","confidence":0.95,"language":"en","strictness":"balanced","reading_complete":true,"credits_used":1}""")
        val result = stub.client().detectNickname(DetectNicknameInput("Star", NicknameStrictness.STRICT, conversationType = "gaming_chat"))
        assertEquals("/api/v1/safety/nickname", stub.last.url.encodedPath)
        assertEquals("strict", stub.lastJson["strictness"]?.jsonPrimitive?.content)
        assertEquals("gaming_chat", stub.lastJson["context"]?.jsonObject?.get("conversation_type")?.jsonPrimitive?.content)
        assertEquals("allow", result.recommendation)
    }

    @Test
    fun `synthetic text and media use their routes`() = runTest {
        val text = Stub("""{"endpoint":"synthetic-content","detected":false,"severity":0,"level":"none","classification":"unknown","confidence":0.5,"risk_score":0,"categories":[],"recommended_action":"none","rationale":"Invented."}""")
        val result = text.client().detectSyntheticText(DetectSyntheticTextInput("Invented sample text", supportThreshold = "high"))
        assertEquals("/api/v1/safety/synthetic-content", text.last.url.encodedPath)
        assertEquals("high", text.lastJson["options"]?.jsonObject?.get("support_threshold")?.jsonPrimitive?.content)
        assertEquals("unknown", result.classification)

        val image = Stub("""{"detected":false,"classification":"confirmed_authentic","input_type":"image","semantic_analysis":{"is_likely_synthetic":false}}""")
        image.client().detectSyntheticImage(DetectSyntheticMediaInput(byteArrayOf(1), "a.webp"))
        assertEquals("/api/v1/safety/synthetic-content/image", image.last.url.encodedPath)
        assertTrue(image.bodies.last().contains("Content-Type: image/webp"))

        val video = Stub("""{"detected":false,"input_type":"video"}""")
        video.client().detectSyntheticVideo(DetectSyntheticMediaInput(byteArrayOf(1), "a.mp4", maxFrames = 8))
        assertEquals("/api/v1/safety/synthetic-content/video", video.last.url.encodedPath)
        assertTrue(video.bodies.last().contains("name=max_frames"))

        val audio = Stub("""{"detected":false,"input_type":"audio"}""")
        audio.client().detectSyntheticAudio(DetectSyntheticMediaInput(byteArrayOf(1), "a.wav"))
        assertEquals("/api/v1/safety/synthetic-content/audio", audio.last.url.encodedPath)
        assertTrue(audio.bodies.last().contains("Content-Type: audio/wav"))
    }

    private val detectionResponse = """{"endpoint":"x","detected":false,"severity":0,"confidence":0.9,"risk_score":0,"level":"none","categories":[],"recommended_action":"none","rationale":"Invented.","language":"en","language_status":"stable"}"""

    @Test
    fun `distress signals and tfgbv use their routes`() = runTest {
        val distress = Stub(detectionResponse)
        distress.client().detectDistressSignals("Invented message")
        assertEquals("/api/v1/safety/distress-signals", distress.last.url.encodedPath)

        val tfgbv = Stub(detectionResponse)
        tfgbv.client().detectTfgbv("Invented message")
        assertEquals("/api/v1/safety/tfgbv", tfgbv.last.url.encodedPath)
    }

    @Test
    fun `verifyImageMatch sends base64 images and decodes the checks`() = runTest {
        val stub = Stub("""{"schema_version":"1.0","checks_run":["face_match","age"],"face_match":{"matched":true,"similarity":0.6,"match_score":0.74},"age":{"estimated_age":31,"confidence":0.99},"credits_used":3,"processing_ms":420}""")
        val result = stub.client().verifyImageMatch(
            VerifyImageMatchInput(byteArrayOf(1, 2, 3), byteArrayOf(4, 5, 6), listOf(ImageMatchCheck.FACE_MATCH, ImageMatchCheck.AGE))
        )
        assertEquals("/api/v1/verify-image-match", stub.last.url.encodedPath)
        assertEquals("AQID", stub.lastJson["image"]?.jsonPrimitive?.content)
        assertEquals("BAUG", stub.lastJson["reference_image"]?.jsonPrimitive?.content)
        assertEquals(0.74, result.faceMatch?.matchScore)
    }

    @Test
    fun `usage summary and quota decode`() = runTest {
        val summary = Stub("""{"apiKeyId":"key_1","tier":"pro","date":"2026-11-17","usage":{"totalRequests":12,"successRequests":11,"errorRequests":1},"quota":{"requestsPerMinute":120,"requestsPerMonth":-1,"requestsPerDay":-1,"remainingToday":-1}}""")
        val s = summary.client().getUsageSummary("2026-11-17")
        assertEquals("/api/v1/usage/summary", summary.last.url.encodedPath)
        assertEquals("2026-11-17", summary.last.url.parameters["date"])
        assertEquals(12, s.usage.totalRequests)
        assertEquals(-1, s.quota.requestsPerMonth)

        val quota = Stub("""{"apiKeyId":"key_1","tier":"pro","resetsInSeconds":30,"limits":{"requestsPerMinute":120,"requestsPerMonth":-1,"requestsPerDay":-1},"current":{"requestsThisMinute":3,"requestsToday":12},"remaining":{"requestsThisMinute":117,"requestsToday":-1}}""")
        val q = quota.client().getQuota()
        assertEquals("/api/v1/usage/quota", quota.last.url.encodedPath)
        assertEquals(117, q.remaining.requestsThisMinute)
    }

    @Test
    fun `redact sends text or messages`() = runTest {
        val stub = Stub("""{"messages":["Hi [NAME_1]","Bye [NAME_1]"],"counts":{"name":2},"entities_total":2,"mode":"precise","strict_anonymisation":false,"model_applied":true,"credits_used":1}""")
        val result = stub.client().redact(RedactInput(messages = listOf("Hi Alex", "Bye Alex"), mode = RedactMode.PRECISE))
        assertEquals("/api/v1/privacy/redact", stub.last.url.encodedPath)
        assertEquals(2, stub.lastJson["messages"]?.jsonArray?.size)
        assertFalse("text" in stub.lastJson)
        assertEquals("precise", stub.lastJson["mode"]?.jsonPrimitive?.content)
        assertEquals(2, result.counts["name"])
    }

    @Test
    fun `RedactInput needs exactly one of text or messages`() {
        assertTrue(runCatching { RedactInput() }.isFailure)
        assertTrue(runCatching { RedactInput(text = "a", messages = listOf("b")) }.isFailure)
    }
}
