"use client";

import "@xyflow/react/dist/style.css";
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  type Node,
  type Edge,
} from "@xyflow/react";
import { useMemo } from "react";
import { SourceNode, ContractNode, SystemNode } from "./lineage-nodes";
import type { AiSystem, DataContract } from "@/lib/types";
import { normaliseDecision } from "@/lib/utils";

const NODE_TYPES = {
  source: SourceNode,
  contract: ContractNode,
  system: SystemNode,
};

interface LineageGraphProps {
  systems: AiSystem[];
  contracts: DataContract[];
}

export function LineageGraph({ systems, contracts }: LineageGraphProps) {
  const { nodes, edges } = useMemo(() => {
    const nodes: Node[] = [];
    const edges: Edge[] = [];

    const sources = Array.from(new Set(contracts.map((c) => c.owner)));

    sources.forEach((owner, i) => {
      nodes.push({
        id: `source-${owner}`,
        type: "source",
        position: { x: 0, y: i * 120 },
        data: { label: owner, owner: "Data Platform" },
      });
    });

    contracts.forEach((contract, i) => {
      nodes.push({
        id: `contract-${contract.id}`,
        type: "contract",
        position: { x: 260, y: i * 130 },
        data: {
          label: contract.name,
          version: contract.version,
          coverage: contract.coverage,
          status: contract.status,
        },
      });

      edges.push({
        id: `edge-source-${contract.id}`,
        source: `source-${contract.owner}`,
        target: `contract-${contract.id}`,
        animated: contract.status !== "HEALTHY",
        style: {
          stroke:
            contract.status === "BREACH"
              ? "#b42318"
              : contract.status === "WARNING"
              ? "#b54708"
              : "#667085",
        },
      });
    });

    systems.forEach((system, i) => {
      const decision = normaliseDecision(system.releaseDecision);
      nodes.push({
        id: `system-${system.id}`,
        type: "system",
        position: { x: 540, y: i * 110 },
        data: { label: system.name, owner: system.owner, decision },
      });

      const linked = contracts.filter((c) => c.systemId === system.id);
      linked.forEach((contract) => {
        edges.push({
          id: `edge-contract-${contract.id}-system-${system.id}`,
          source: `contract-${contract.id}`,
          target: `system-${system.id}`,
          animated: contract.status !== "HEALTHY",
          style: {
            stroke:
              contract.status === "BREACH"
                ? "#b42318"
                : contract.status === "WARNING"
                ? "#b54708"
                : "#667085",
          },
        });
      });
    });

    return { nodes, edges };
  }, [systems, contracts]);

  return (
    <div className="h-[520px] rounded-lg border border-border overflow-hidden bg-muted/10">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={NODE_TYPES}
        fitView
        fitViewOptions={{ padding: 0.2 }}
        nodesDraggable
        nodesConnectable={false}
        elementsSelectable
        proOptions={{ hideAttribution: true }}
      >
        <Background gap={16} size={1} />
        <Controls showInteractive={false} />
        <MiniMap nodeStrokeWidth={3} zoomable pannable />
      </ReactFlow>
    </div>
  );
}
