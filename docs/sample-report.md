# EDI reconciliation report

- **Reference environment:** env-a
- **Candidate environment:** env-b
- **Line of business:** medical
- **Clients compared:** 3
- **Differences found:** 6

| Client | Members in env-a | Members in env-b | Result |
|---|---|---|---|
| ALPHA | 5 | 6 | 5 difference(s) |
| BRAVO | 3 | 0 | 1 difference(s) |
| DELTA | 2 | 2 | match |

## ALPHA: Alpha Manufacturing (sample)

| Type | Member | Detail | Likely cause |
|---|---|---|---|
| DUPLICATE | M1003 | appears 2 times in env-b (identical copies) | The same record was written twice. Likely a file processed twice or a re-run without de-duplication. |
| CHANGED | M1002 | DTP: env-a `DTP*356*D8*20260101` vs env-b `DTP*356*D8*20260102` | Only dates differ. Check date mapping, time zone and cut-off rules between the environments. |
| MISSING | M1004 | in env-a (ALPHA-medical-20260105.edi) but not in env-b | The record was filtered out or rejected in env-b. Check eligibility and mapping rules, and the load log for validation errors. |
| CHANGED | M1005 | HD: env-a `HD*030**HLT*PLAN-A` vs env-b `HD*030**HLT*PLAN-B` | Segment HD differs. Check the mapping rule that populates it in each environment. |
| EXTRA | M1099 | in env-b (ALPHA-medical-20260105.edi) but not in env-a | The record exists only in env-b. Likely a stale record that was not purged, or a load that has not run in env-a. |

## BRAVO: Bravo Logistics (sample)

| Type | Member | Detail | Likely cause |
|---|---|---|---|
| NO_FILES | (all) | env-b has no files; 3 member(s) in env-a could not be compared | No files were produced or delivered in env-b. Check that the export job ran and the file transfer completed. |

## Skipped clients

- CHARLIE: enabled only in env-a
- ECHO: 'medical' is not configured in both environments

_Likely causes are rule-based hints for a developer to confirm._
