# Part 14 — Regulatory Change Monitoring (Live Feed MVP)

> **For agentic workers:** Start with a **curated + fetchable** change feed, not a full legislative NLP firehose.

**Goal:** Former PRD §4 “real-time regulatory change monitoring” → **in program** as Phase 7 feed + impact hints.

**Depends on:** Part 5 (controls), Part 12 (obligation rules versioning).

---

### Task 14.1: Data model

- [ ] **Step 1:** Tables:
  - `reg_sources` — name, url, poll_interval, enabled
  - `reg_items` — source_id, external_id, title, summary, published_at, url, content_hash, fetched_at
  - `reg_impact_hints` — reg_item_id, control_code or obligation_code, impact_note
- [ ] **Step 2:** Seed sources (manual curated list acceptable for v1):
  - EU Official Journal RSS / EUR-Lex search URLs (document legal ToS/rate limits)
  - Static curated YAML/JSON in repo for bootstrap when network blocked

### Task 14.2: Ingestion worker

- [ ] **Step 1:** Scheduled poller (Spring `@Scheduled`) similar to eval worker cadence; config `assurance.reg-monitor.enabled`.
- [ ] **Step 2:** SSRF-safe fetch (reuse evidence DNS pin patterns).
- [ ] **Step 3:** Dedupe on content_hash; audit `reg_item.ingested`.
- [ ] **Step 4:** Tests with WireMock/fixture HTML/RSS.

### Task 14.3: Impact mapping (v1 heuristic)

- [ ] **Step 1:** Keyword / tag map from item title/summary → control codes (conservative; prefer UNCERTAIN impact).
- [ ] **Step 2:** Optional: notify workflow admins via `WorkflowNotification` “REG_CHANGE_RELEVANT”.
- [ ] **Step 3:** Do **not** auto-change risk class or control status without human.

### Task 14.4: API + UI

- [ ] **Step 1:** `GET /api/v1/reg-monitor/items?since=`
- [ ] **Step 2:** `GET /api/v1/systems/{id}/reg-monitor/relevant` — items matching system sector/controls.
- [ ] **Step 3:** Dashboard page `/reg-monitor` or section under command: feed + “review impact” actions.
- [ ] **Step 4:** Mark item as reviewed (tenant-scoped).

### Task 14.5: Ops honesty

- [ ] **Step 1:** Document latency (“near-real-time poll every N minutes”, not magical real-time law).
- [ ] **Step 2:** Landing disclaimer: feed is assistive; not official legal bulletin.

### Done when

- [ ] Poller + fixtures tests  
- [ ] UI feed + per-system relevance  
- [ ] No auto legal mutations  
