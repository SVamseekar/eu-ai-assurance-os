package os.assurance.eu.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.core.env.Environment;
import os.assurance.eu.api.eval.EvalDataset;
import static os.assurance.eu.api.tenant.ApiKeyHasher.sha256Hex;
import os.assurance.eu.api.eval.EvalDatasetEntity;
import os.assurance.eu.api.eval.EvalDatasetJpaRepository;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemJpaRepository;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.DataContractStatus;
import os.assurance.eu.api.system.ReleaseDecision;
import os.assurance.eu.api.system.ReleaseGateService;
import os.assurance.eu.api.system.RiskClass;
import os.assurance.eu.api.tenant.ApiKeyEntity;
import os.assurance.eu.api.tenant.ApiKeyJpaRepository;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.TenantEntity;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapData implements CommandLineRunner {
  private final TenantJpaRepository tenants;
  private final UserJpaRepository users;
  private final ApiKeyJpaRepository apiKeyRepo;
  private final AiSystemJpaRepository systemStore;
  private final AiSystemRepository systems;
  private final EvalDatasetJpaRepository evalDatasets;
  private final ReleaseGateService releaseGateService;
  private final Environment environment;
  private final TenantContext tenantContext;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

  public BootstrapData(
      TenantJpaRepository tenants,
      UserJpaRepository users,
      ApiKeyJpaRepository apiKeyRepo,
      AiSystemJpaRepository systemStore,
      AiSystemRepository systems,
      EvalDatasetJpaRepository evalDatasets,
      ReleaseGateService releaseGateService,
      Environment environment,
      TenantContext tenantContext) {
    this.tenants = tenants;
    this.users = users;
    this.apiKeyRepo = apiKeyRepo;
    this.systemStore = systemStore;
    this.systems = systems;
    this.evalDatasets = evalDatasets;
    this.releaseGateService = releaseGateService;
    this.environment = environment;
    this.tenantContext = tenantContext;
  }

  @Override
  @Transactional
  public void run(String... args) {
    tenantContext.setOverrides(TenantContext.DEFAULT_TENANT_ID, TenantContext.DEFAULT_USER_ID);
    try {
      seedBootstrapData();
    } finally {
      tenantContext.clearOverrides();
    }
  }

  private void seedBootstrapData() {
    Instant now = Instant.now();
    tenants.findById(TenantContext.DEFAULT_TENANT_ID)
        .orElseGet(() -> tenants.save(new TenantEntity(
            TenantContext.DEFAULT_TENANT_ID,
            "MVP Tenant",
            "starter",
            "EU",
            now)));
    users.findById(TenantContext.DEFAULT_USER_ID)
        .orElseGet(() -> users.save(new UserEntity(
            TenantContext.DEFAULT_USER_ID,
            TenantContext.DEFAULT_TENANT_ID,
            "compliance@example.com",
            UserRole.COMPLIANCE_OFFICER,
            passwordEncoder.encode("dev-local-password-only"),
            now)));
    seedUser("00000000-0000-0000-0000-000000000102",
        "engineering@example.com", UserRole.AI_ENGINEERING_LEAD, now);
    seedUser("00000000-0000-0000-0000-000000000103",
        "auditor@example.com", UserRole.AUDITOR, now);
    seedUser("00000000-0000-0000-0000-000000000104",
        "legal@example.com", UserRole.LEGAL_COUNSEL, now);
    seedUser("00000000-0000-0000-0000-000000000105",
        "admin@example.com", UserRole.ADMIN, now);
    // Only seed the fixed dev/test API key when NOT running against real Postgres
    if (!Arrays.asList(environment.getActiveProfiles()).contains("postgres")) {
      UUID defaultApiKey = UUID.fromString("00000000-0000-0000-0000-000000000a01");
      apiKeyRepo.findById(defaultApiKey)
          .orElseGet(() -> apiKeyRepo.save(new ApiKeyEntity(
              defaultApiKey,
              sha256Hex(defaultApiKey.toString()),
              TenantContext.DEFAULT_TENANT_ID,
              TenantContext.DEFAULT_USER_ID,
              now)));
    }
    if (!evalDatasets.existsByTenantIdAndNameAndVersion(
        TenantContext.DEFAULT_TENANT_ID,
        "golden-eu-claims-v4",
        "2026-06")) {
      evalDatasets.save(new EvalDatasetEntity(
          TenantContext.DEFAULT_TENANT_ID,
          new EvalDataset(
              UUID.randomUUID(),
              "golden-eu-claims-v4",
              "2026-06",
              240,
              true,
              now)));
    }

    if (systemStore.existsByTenantId(TenantContext.DEFAULT_TENANT_ID)) {
      return;
    }

    seed(
        "Claims Triage AI",
        "Insurance Ops",
        "Prioritize and route insurance claims",
        RiskClass.HIGH,
        "Eligibility and access to essential private services",
        72,
        78,
        DataContractStatus.BREACH,
        List.of("Human oversight SOP missing", "Bias eval below threshold"));
    seed(
        "Support RAG Copilot",
        "Customer Success",
        "Answer customer support questions with cited sources",
        RiskClass.LIMITED,
        "Customer-facing assistant with transparency duty",
        88,
        86,
        DataContractStatus.HEALTHY,
        List.of("Update chatbot disclosure copy"));
  }

  private void seedUser(String id, String email, UserRole role, Instant now) {
    UUID userId = UUID.fromString(id);
    users.findById(userId)
        .orElseGet(() -> users.save(new UserEntity(
            userId,
            TenantContext.DEFAULT_TENANT_ID,
            email,
            role,
            passwordEncoder.encode("dev-local-password-only"),
            now)));
  }

  private void seed(
      String name,
      String owner,
      String purpose,
      RiskClass riskClass,
      String riskBasis,
      int evidenceCoverage,
      int evalScore,
      DataContractStatus dataContractStatus,
      List<String> openGaps) {
    Instant now = Instant.now();
    AiSystem draft = new AiSystem(
        UUID.randomUUID(),
        name,
        owner,
        purpose,
        riskClass,
        riskBasis,
        "EU",
        evidenceCoverage,
        evalScore,
        dataContractStatus,
        ReleaseDecision.REVIEW,
        new ArrayList<>(openGaps),
        now,
        now);
    ReleaseDecision decision = releaseGateService.calculate(draft).decision();
    systems.save(new AiSystem(
        draft.id(),
        draft.name(),
        draft.owner(),
        draft.purpose(),
        draft.riskClass(),
        draft.riskBasis(),
        draft.deploymentRegion(),
        draft.evidenceCoverage(),
        draft.evalScore(),
        draft.dataContractStatus(),
        decision,
        draft.openGaps(),
        draft.createdAt(),
        draft.updatedAt()));
  }
}
