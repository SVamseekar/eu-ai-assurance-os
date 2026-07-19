# Part 4 — OAuth End-to-End (Google + Microsoft)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. TDD preferred for auth routes.

**Goal:** Implement OAuth login end-to-end so enterprise users can sign in with Google and Microsoft, issuing the **same** session cookies as password login.

**Reference implementation:** WorkforceGuard AI  
- `dashboard/backend/auth/oauth.py`  
- OAuth tests under `dashboard/backend/tests/test_oauth*.py`  
- `deploy/oauth-production-smoke-test.md`

**Depends on:** Part 0; existing JWT + refresh + cookie session (already shipped).

**Out of scope for this part:** Full SAML / Okta enterprise IdP marketplace (design for extension).

---

### Task 4.1: Design decisions

- [ ] **Step 1:** OAuth authorization code + PKCE (if SPA-facing) or server-side code flow via Next route handlers + Spring token exchange — **prefer server-side**:
  - Browser hits `GET /api/auth/oauth/{provider}/start`
  - Callback `GET /api/auth/oauth/{provider}/callback`
  - Backend validates ID token / userinfo, maps or provisions user, returns JWT pair
  - Next sets httpOnly cookies (reuse `setSessionCookies`)
- [ ] **Step 2:** Config flags: `OAUTH_AUTO_PROVISION` (default **false** in prod — match WorkforceGuard).  
- [ ] **Step 3:** Providers: `google`, `microsoft` first.  
- [ ] **Step 4:** Document redirect URIs for local + `https://euassuranceai.souravamseekar.com`.

### Task 4.2: Backend

- [ ] **Step 1:** Flyway migration if needed: `users.oauth_provider`, `users.oauth_subject` unique (provider, subject); password nullable for OAuth-only users.  
- [ ] **Step 2:** `OAuthService` — exchange code, verify token, resolve user.  
- [ ] **Step 3:** Controllers or routes under `/auth/oauth/**` as unauthenticated paths in `TenantContextFilter`.  
- [ ] **Step 4:** Tests: happy path, not provisioned, bad state, replay.  
- [ ] **Step 5:** Secrets via env: client IDs/secrets, redirect base URL.

### Task 4.3: Dashboard

- [ ] **Step 1:** Login page buttons: “Continue with Google”, “Continue with Microsoft”.  
- [ ] **Step 2:** Next route handlers for start/callback (or proxy to API).  
- [ ] **Step 3:** Error query params: `auth_error=not_provisioned|denied|state` with user-visible messages.  
- [ ] **Step 4:** Logout still revokes refresh token.

### Task 4.4: Production ops

- [ ] **Step 1:** Write `docs/oauth-production-smoke-test.md` (clone WorkforceGuard table).  
- [ ] **Step 2:** Configure Google Cloud + Azure AD app registrations.  
- [ ] **Step 3:** Vercel + API env vars.  
- [ ] **Step 4:** Run smoke checklist; capture pass/fail with date.

### Task 4.5: Claims update

- [ ] **Step 1:** Only after green smoke: update metrics canon “Auth: JWT + API keys + Google/Microsoft OAuth”.  
- [ ] **Step 2:** Issue channel update instructions; still no “full enterprise SSO” unless true.

### Done when

- [ ] New Google user (when auto-provision on) gets session cookies and can call `/api/proxy/systems`  
- [ ] Unprovisioned user (auto-provision off) gets clear error  
- [ ] Password login still works  
- [ ] Production smoke doc signed off  

---

## Related program parts (no longer “optional”)

These are **required** under the full roadmap program — see `docs/superpowers/plans/2026-07-20-INDEX.md`:

| Concern | Plan |
|---|---|
| Hash-chained audit ledger | Part 6 |
| Evidence pack PDF + seal | Part 7 |
| CI/CD release gate + observability | Part 8 |
| Docker + Terraform | Part 9 |
| Tenant isolation + NFR | Part 10 |
| PRD controls / §6 E2E | Part 5 |
