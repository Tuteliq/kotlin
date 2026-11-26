## [Unreleased]

### Added
- `GetActionPlanInput.country`: the family's country as an ISO 3166-1 alpha-2 code, so the plan names that country's helplines and emergency number.
- `ActionPlanResult.generalGuidance`: true when the plan is general guidance for any harm, given because a plan tailored to the situation could not be written.

## [3.0.0] - 2026-11-17

Versions 2.3.0, 2.4.0 and 2.5.0 were never published to Maven Central; this release includes their changes. It is a major version because a few public types changed (see Breaking changes).

### Breaking changes
- `VerificationSession.expiresAt` and `VerificationSessionResult.createdAt`/`expiresAt` are `Long` (epoch ms), no longer strings.
- `VerificationSessionResult.result` is a typed `VerificationResult`, no longer a `JsonObject`.
- Webhook result models are flat, as the API returns them.
- New enum entries (including `UNKNOWN`) may require an `else` branch in exhaustive `when`.

### Fixed
- IDs in request paths (verification sessions, age/identity lookups, webhooks) are URL-encoded, and upload filenames are escaped in the part header.
- File uploads (`analyzeImage`, `analyzeVoice`, `analyzeVideo`) now send the file's MIME type, taken from the filename or the new `contentType` parameter. The API rejected uploads without one.
- `VerificationSessionStatus` decodes `document_required`; unknown verification statuses decode to `UNKNOWN` instead of throwing. `CANCELLED` is deprecated: a cancelled session reports `EXPIRED`.
- `cancelVerificationSession` accepts the API's `{ "ok": true }` response.
- Webhooks: `Webhook.active` reads `is_active`; `createWebhook` sends the required `name`; `updateWebhook` uses PUT and sends `is_active`; `testWebhook` calls `POST /api/v1/webhooks/test`; result models match the API.
- `DocumentType.ID_CARD` sent a value the API rejects. Use `DocumentType.NATIONAL_ID`; `ID_CARD` is a deprecated alias that sends `national_id`.
- `getAgeVerification` and `getIdentityVerification` decode `face_matched`, `face_confidence`, `liveness_valid` and `failure_reasons`; fields the API never returned are deprecated.
- `createVerificationSession` is no longer retried, so a lost response cannot create a second session.

### Added
- `createVerificationSession`: `chipPolicy` and `autoCapture`; the response has `chipPolicy`, `autoCapture` and `recommendedImageWidth`. `getVerificationSession` returns a typed `result` and `verificationMode`, `documentEvidence`, `stepProgress`, `chipPolicy`.
- `FaceMatchResult.matchScore` (0.0 to 1.0, 0.5 at the match decision).
- `getVerification`, `getVerificationConfig`, `verifyImageMatch`.
- `analyzeDocument` for PDFs and images.
- `detectSyntheticText`, `detectSyntheticImage`, `detectSyntheticAudio`, `detectSyntheticVideo`.
- `detectDistressSignals`, `detectTfgbv`, `detectNickname`.
- Incidents: `listIncidents`, `getIncident`, `getIncidentsOverview`, `getIncidentTrends`, `reviewIncident`, `batchReviewIncidents`.
- `redact` for privacy redaction.
- `getUsageSummary`, `getQuota`.
- `Language` lists all 32 supported languages; `Detection` adds `DISTRESS_SIGNALS`, `TFGBV`, `SYNTHETIC_CONTENT`.

### Changed
- `CreateWebhookInput` takes a `name`. The old constructor is deprecated and uses the URL's host as the name.

## [1.2.0] - 2026-02-16

### Added
- Voice streaming support via WebSocket (`voiceStream()`)
- `creditsUsed` field on all result types
