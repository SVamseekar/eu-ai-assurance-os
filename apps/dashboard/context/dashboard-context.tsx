"use client";

import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import type { AiSystem, DataContract, DriftEvent, AuditEvent, DataContractStatus, ReleaseDecision } from "@/lib/types";
import { MOCK_SYSTEMS, MOCK_CONTRACTS, MOCK_DRIFT_EVENTS, MOCK_AUDIT_EVENTS } from "@/lib/mock-data";
import { normaliseDecision } from "@/lib/utils";

interface DashboardContextType {
  // Tenant & Actor selector
  activeTenant: string;
  setActiveTenant: (t: string) => void;
  activeRole: string;
  setActiveRole: (r: string) => void;

  // Selected state
  selectedSystem: AiSystem | null;
  setSelectedSystem: (s: AiSystem | null) => void;
  selectedContract: DataContract | null;
  setSelectedContract: (c: DataContract | null) => void;

  // Custom systems registry
  customSystems: AiSystem[];
  registerSystem: (sys: AiSystem) => void;
  allSystems: AiSystem[];

  // Drift Events
  driftEvents: DriftEvent[];
  allAudits: AuditEvent[];
  acknowledgeDrift: (id: string) => void;
  resolveDrift: (id: string) => void;
  contractsList: DataContract[];

  // Manual release gate overrides
  overrideGate: (systemId: string, justification: string) => void;

  // Custom datasets
  evalDatasets: string[];
  registerDataset: (name: string) => void;

  openSystemDetails: (id: string) => void;
  openContractDetails: (id: string) => void;
}

const DashboardContext = createContext<DashboardContextType | undefined>(undefined);

const INITIAL_DATASETS = ["golden-eu-claims-v4", "hr-candidate-screening-v2", "customer-support-rag-v8"];

export function DashboardProvider({ children }: { children: ReactNode }) {
  // Shared roles & headers
  const [activeTenant, setActiveTenant] = useState("tenant-premium");
  const [activeRole, setActiveRole] = useState("actor-priya");

  // Selected drawers
  const [selectedSystem, setSelectedSystem] = useState<AiSystem | null>(null);
  const [selectedContract, setSelectedContract] = useState<DataContract | null>(null);

  // Dynamic entities lists
  const [customSystems, setCustomSystems] = useState<AiSystem[]>([]);
  const [driftEvents, setDriftEvents] = useState<DriftEvent[]>(MOCK_DRIFT_EVENTS);
  const [contractsList, setContractsList] = useState<DataContract[]>(MOCK_CONTRACTS);
  const [evalDatasets, setEvalDatasets] = useState<string[]>(INITIAL_DATASETS);
  const [overriddenSystems, setOverriddenSystems] = useState<Record<string, string>>({}); // systemId -> justification
  const [customAudits, setCustomAudits] = useState<AuditEvent[]>([]);

  // Set headers in localStorage on change
  useEffect(() => {
    localStorage.setItem("eu-ai-tenant-id", activeTenant);
    localStorage.setItem("eu-ai-actor-id", activeRole);
  }, [activeTenant, activeRole]);

  function registerSystem(sys: AiSystem) {
    setCustomSystems((p) => [...p, sys]);
  }

  function registerDataset(name: string) {
    setEvalDatasets((p) => [...p, name]);
  }

  function acknowledgeDrift(eventId: string) {
    setDriftEvents((p) =>
      p.map((e) => (e.id === eventId ? { ...e, status: "ACKNOWLEDGED", updatedAt: new Date().toISOString() } : e))
    );
  }

  function resolveDrift(eventId: string) {
    setDriftEvents((p) =>
      p.map((e) => (e.id === eventId ? { ...e, status: "RESOLVED", updatedAt: new Date().toISOString() } : e))
    );
  }

  function overrideGate(systemId: string, justification: string) {
    setOverriddenSystems((p) => ({ ...p, [systemId]: justification }));

    const newEvent: AuditEvent = {
      id: `audit-${Math.floor(Math.random() * 9000) + 1000}`,
      systemId,
      actorId: activeRole,
      eventType: "RELEASE_GATE_CALCULATED",
      resourceType: "ai_system",
      resourceId: systemId,
      payload: { decision: "PASS", reason: `Manual override: ${justification}` },
      createdAt: new Date().toISOString(),
    };
    setCustomAudits((p) => [newEvent, ...p]); // Prepended so it shows at the top
  }

  const allAudits = [...customAudits, ...MOCK_AUDIT_EVENTS];

  // Recalculate contract status dynamically based on resolved drift events
  const calculatedContracts = contractsList.map((contract) => {
    const events = driftEvents.filter((e) => e.contractId === contract.id && e.status !== "RESOLVED");
    const hasBreach = events.some((e) => e.severity === "BREACH");
    const hasWarning = events.some((e) => e.severity === "WARNING");
    const status = (hasBreach ? "BREACH" : hasWarning ? "WARNING" : "HEALTHY") as DataContractStatus;
    return { ...contract, status };
  });

  // Recalculate system release decision based on contract status and manual overrides
  const allSystems = [...MOCK_SYSTEMS, ...customSystems].map((sys) => {
    // If manually overridden
    if (overriddenSystems[sys.id]) {
      return {
        ...sys,
        dataContractStatus: "HEALTHY" as const,
        releaseDecision: "pass" as const,
        openGaps: [],
      };
    }

    const linkedContracts = calculatedContracts.filter((c) => c.systemId === sys.id);
    const hasBreaches = linkedContracts.some((c) => c.status === "BREACH");
    const hasWarnings = linkedContracts.some((c) => c.status === "WARNING");
    const dataContractStatus = (hasBreaches ? "BREACH" : hasWarnings ? "WARNING" : "HEALTHY") as DataContractStatus;

    // Clean resolved gaps from lists
    let openGaps = [...sys.openGaps];
    if (!hasBreaches) {
      openGaps = openGaps.filter(
        (g) =>
          !g.toLowerCase().includes("phi redaction") &&
          !g.toLowerCase().includes("denial_reason_category") &&
          !g.toLowerCase().includes("diagnosis_code_icd11")
      );
    }

    let releaseDecision = sys.releaseDecision;
    if (dataContractStatus === "BREACH" || sys.evalScore < 75) {
      releaseDecision = "blocked" as ReleaseDecision;
    } else if (dataContractStatus === "WARNING" || sys.evalScore < 85 || openGaps.length > 0) {
      releaseDecision = "review" as ReleaseDecision;
    } else {
      releaseDecision = "pass" as ReleaseDecision;
    }

    return {
      ...sys,
      dataContractStatus,
      releaseDecision,
      openGaps,
    };
  });

  // Update selected drawers with recalculated values
  const updatedSelectedSystem = selectedSystem
    ? allSystems.find((s) => s.id === selectedSystem.id) || selectedSystem
    : null;

  const updatedSelectedContract = selectedContract
    ? calculatedContracts.find((c) => c.id === selectedContract.id) || selectedContract
    : null;

  function openSystemDetails(id: string) {
    const found = allSystems.find((s) => s.id === id);
    if (found) {
      setSelectedSystem(found);
      setSelectedContract(null);
    }
  }

  function openContractDetails(id: string) {
    const found = calculatedContracts.find((c) => c.id === id);
    if (found) {
      setSelectedContract(found);
      setSelectedSystem(null);
    }
  }

  return (
    <DashboardContext.Provider
      value={{
        activeTenant,
        setActiveTenant,
        activeRole,
        setActiveRole,
        selectedSystem: updatedSelectedSystem,
        setSelectedSystem,
        selectedContract: updatedSelectedContract,
        setSelectedContract,
        customSystems,
        registerSystem,
        allSystems,
        driftEvents,
        acknowledgeDrift,
        resolveDrift,
        contractsList: calculatedContracts,
        overrideGate,
        evalDatasets,
        registerDataset,
        openSystemDetails,
        openContractDetails,
        allAudits,
      }}
    >
      {children}
    </DashboardContext.Provider>
  );
}

export function useDashboard() {
  const ctx = useContext(DashboardContext);
  if (!ctx) throw new Error("useDashboard must be used within DashboardProvider");
  return ctx;
}
