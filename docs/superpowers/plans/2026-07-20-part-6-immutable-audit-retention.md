# Part 6 — Immutable Hash-Chained Audit + Retention

> **For agentic workers:** TDD preferred. Do not break existing audit list APIs without versioning.

**Goal:** Make the audit ledger **immutable after append** and **tamper-evident** (hash chain), with **configurable retention ≥ 7 years** (PRD §7).

**Sources:** PRD §2 Auditor, §7; ARCHITECTURE “Immutable Audit Store”; ROADMAP Phase 6.

**Depends on:** Part 0.

---

### Task 6.1: Hash-chain schema

- [ ] **Step 1:** Migration `V1x__audit_hash_chain.sql`:
  - `prev_event_hash varchar(64)` (nullable for genesis)
  - `event_hash varchar(64) not null`
  - optional `chain_key_id` if HMAC secret versioned
- [ ] **Step 2:** Define canonical serialization for hash input:  
  `tenantId|id|prevHash|actorId|eventType|resourceType|resourceId|payloadCanonicalJson|createdAt`
- [ ] **Step 3:** `event_hash = hex(HMAC-SHA-256(AUDIT_CHAIN_SECRET, canonical))` or SHA-256 of canonical + prev (document choice; prefer HMAC with rotated secret for enterprise).

### Task 6.2: Append-only service changes

- [ ] **Step 1:** `AuditService.append` loads last hash for tenant (or tenant+system stream — document stream key; recommend **per-tenant chain**).
- [ ] **Step 2:** Compute and store hashes transactionally.
- [ ] **Step 3:** **No update/delete APIs** for audit rows. Add DB role note / reject JPA updates (read-only entity for updates).
- [ ] **Step 4:** Tests: sequential hashes link; tamper detection utility fails on mutated payload.

### Task 6.3: Verify endpoint

- [ ] **Step 1:** `GET /api/v1/audit/verify` (ADMIN) → `{ valid, checkedCount, firstBreakId? }`
- [ ] **Step 2:** Optional verify for single system stream if used.
- [ ] **Step 3:** Dashboard audit page: “Chain valid” badge (call verify).

### Task 6.4: Retention policy (PRD §7 — ≥ 7 years)

- [ ] **Step 1:** Config `assurance.audit.retention-years` default **7**.
- [ ] **Step 2:** On append set `retain_until = createdAt + retention` column (migration).
- [ ] **Step 3:** **No automatic delete** in MVP of this part — retention is legal hold metadata; document that purge is admin/legal process only.
- [ ] **Step 4:** Optional scheduled job **disabled by default** for post-retain_until archival (do not enable in prod without legal review).
- [ ] **Step 5:** Tests for retain_until calculation.

### Task 6.5: Evidence pack + metrics claims

- [ ] **Step 1:** Evidence pack includes last event hash or chain head for system events.
- [ ] **Step 2:** Update honest wording for Part 2 metrics: “HMAC-SHA-256 hash-chained audit ledger” **only after this lands**.

### Done when

- [ ] Every new audit event has prev + event hash  
- [ ] Verify endpoint detects tampering in tests  
- [ ] retain_until set for ≥ 7 years by default  
- [ ] No public API mutates audit history  
