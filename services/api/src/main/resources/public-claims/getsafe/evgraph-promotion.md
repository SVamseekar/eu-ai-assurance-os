# Evgraph Report — scan_2026-09-14T19:03:02.305752+00:00_829fc7

Graph observed 3 node(s) and 2 edge(s).

## Findings (1)

### approval-precedes-deployment

- **Outcome:** Inconclusive
- **Level:** CONSISTENCY
- **Statement:** DeploymentRecord n3 or HumanApproval n2 is missing a timestamp needed to compare deployment and approval order.
- **Cited evidence:** n3, n2
- **Trace:** n3 --REQUIRES_APPROVAL--> n2

