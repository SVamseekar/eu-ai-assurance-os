# Sector packs + integration SPI

**Product claim:** **3 sector packs + SPI** — not “all industries integrated.”

Sector packs are **vertical overlays** for AI systems registered with a matching `sector` field. They are **not** live production connectors to proprietary vendors (Workday, Guidewire, Core banking platforms, etc.) unless a real OAuth app and credentials are implemented later.

## Enabled packs

Configured via:

```properties
assurance.sector.packs=insurance,hr,finance
```

| Pack id | Display name | Primary keys | Depth focus |
|---|---|---|---|
| `insurance` | Insurance / Claims | insurance, claims, underwriting | Claims triage fairness, adverse decision review, model cards |
| `hr` | HR / Employment | hr, employment, recruiting | Hiring transparency, employment oversight, candidate notice |
| `finance` | Financial services / KYC | finance, kyc, banking | KYC logging intensity, fraud flag review, explainability |

## What each pack provides

1. **Control overlays** — extra catalog codes attached when `AiSystem.sector` matches the pack (risk-class applicability still applies).
2. **Questionnaire defaults** — suggested answers for assisted determination / register UX.
3. **Sample evidence templates** — markdown starters under `classpath:sector/{packId}/` (illustrative, not legal advice).
4. **Optional stubs** — insurance claims-model register webhook maps external model metadata → system registry fields.

## SPI

### `SectorPack`

Java interface (`os.assurance.eu.api.sector.SectorPack`):

- `id()`, `displayName()`, `summary()`, `sectorKeys()`
- `extraControls()`, `questionnaireDefaults()`, `sampleEvidenceTemplates()`

Implement as a Spring `@Component`. Enable by adding the pack id to `assurance.sector.packs`.

### `IntegrationConnector`

Generic boundary for external systems of record:

- `pushReleaseDecision(systemId, decision, context)`
- `pullModelInventory()`

v1 ships `LoggingIntegrationConnector` only (logs / empty inventory).

### How a real vendor connector would plug in

1. Implement `IntegrationConnector` (or a sector-specific extension) with OAuth client credentials for the vendor.
2. Register as a Spring bean; keep the logging stub for local/dev.
3. Call `pushReleaseDecision` from release-gate transitions when the tenant has the connector configured.
4. Map `pullModelInventory` results into `POST /api/v1/systems` or the insurance claims-model-register stub pattern.
5. **Do not** market the product as “integrated with Workday/Guidewire” until OAuth apps, sandboxes, and contractual access exist.

## HTTP APIs

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/sector-packs` | List enabled packs + honest metrics label |
| `GET` | `/api/v1/sector-packs/{id}` | Pack detail |
| `GET` | `/api/v1/sector-packs/resolve?sector=` | Resolve pack for a registry sector string |
| `GET` | `/api/v1/sector-packs/{id}/templates/{templateId}` | Load sample markdown template |
| `POST` | `/api/v1/integrations/insurance/claims-model-register` | Stub: external model → system registry |
| `GET` | `/api/v1/integrations/connectors/model-inventory` | Stub inventory (empty) |

## Control codes (overlay)

**Insurance:** `INS_CLAIMS_FAIRNESS`, `INS_ADVERSE_DECISION_REVIEW`, `INS_CLAIMS_EXPLAINABILITY`, `INS_MODEL_CARD_CLAIMS`

**HR:** `HR_HIRING_TRANSPARENCY`, `HR_HUMAN_OVERSIGHT_EMPLOYMENT`, `HR_CANDIDATE_NOTICE`

**Finance:** `FIN_KYC_LOGGING`, `FIN_FRAUD_FALSE_POSITIVE_REVIEW`, `FIN_CREDIT_EXPLAINABILITY`

## Dashboard

- Sector selector on system register
- Pack badge on system cards when sector resolves to an enabled pack

## Disclaimers

Packs do not constitute legal advice, conformity assessment, or certification. Connector stubs do not call proprietary vendor APIs.
