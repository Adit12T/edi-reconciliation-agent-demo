# EDI reconciliation agent (demo)

Compares a client's EDI files between two environments and reports what is missing, duplicated,
extra or changed, with a likely cause for each difference.

> **About this repository.** This is a clean-room demo of a pattern I built and use professionally.
> It was written from scratch for this repository and contains no employer code, data, mappings or rules.
> All clients, members and files are synthetic, and the file format is a simplified X12-style layout,
> not a compliant 834 implementation.

## The problem

Before a release, someone has to confirm that a new environment produces the same EDI output as the current one.
Doing that by hand means opening files side by side, client by client. It is slow, and a single missing
or duplicated record is easy to overlook.

## What it does

```
client lists ─▶ clients enabled in both ─▶ load EDI files per line of business ─▶ compare member by member ─▶ report + likely causes
```

1. **Match clients.** Reads each environment's client list and keeps only clients enabled in both,
   so every comparison is like-for-like. Clients enabled in one environment only are listed as skipped.
2. **Load files.** Reads the client's files for the chosen line of business and splits them into member loops.
3. **Compare.** Matches members by id and classifies each difference:

   | Type | Meaning |
   |---|---|
   | `MISSING` | In the reference environment, not in the candidate |
   | `EXTRA` | In the candidate, not in the reference |
   | `DUPLICATE` | Same member id more than once in one environment |
   | `CHANGED` | Same member, different segment content |
   | `NO_FILES` | The candidate has no files at all for the client |

4. **Explain.** Attaches a likely cause to each difference, for example "only dates differ: check date mapping
   and cut-off rules".

## Run it

Requires JDK 17 or newer. There are no runtime dependencies.

With Maven:

```bash
mvn test                 # unit tests + tests on the sample data
mvn compile exec:java    # compares the sample environments for 'medical'
```

Without Maven:

```bash
javac -d build $(find src/main/java -name '*.java')
java -cp build io.github.adit12t.edirecon.EdiReconciler
```

Output:

```
ALPHA    5 difference(s)
  DUPLICATE M1003  appears 2 times in env-b (identical copies)
  CHANGED   M1002  DTP: env-a `DTP*356*D8*20260101` vs env-b `DTP*356*D8*20260102`
  MISSING   M1004  in env-a (ALPHA-medical-20260105.edi) but not in env-b
  CHANGED   M1005  HD: env-a `HD*030**HLT*PLAN-A` vs env-b `HD*030**HLT*PLAN-B`
  EXTRA     M1099  in env-b (ALPHA-medical-20260105.edi) but not in env-a
BRAVO    1 difference(s)
  NO_FILES  (all)  env-b has no files; 3 member(s) in env-a could not be compared
DELTA    match
CHARLIE  skipped: enabled only in env-a
ECHO     skipped: 'medical' is not configured in both environments
Report: out/report.md
```

The full report for the sample data is in [docs/sample-report.md](docs/sample-report.md).

### Options

| Option | Default | Meaning |
|---|---|---|
| `--reference` | `sample-data/env-a` | The environment treated as correct |
| `--candidate` | `sample-data/env-b` | The environment being checked |
| `--lob` | `medical` | Line of business to compare |
| `--out` | `out` | Where the report is written |

With Maven, pass them as `-Dexec.args="--lob dental"`.

Each environment folder holds a `clients.json` and files under `edi/{client}/{lineOfBusiness}/*.edi`.

## Where the agent fits

This repository is the deterministic core: parsing, comparison and rule-based cause hints.

In the version I use at work, that core lives in a Spring Boot MCP server, which exposes it as tools.
A Claude agent connects over MCP, calls those tools, and explains the results to the developer,
drawing on internal documentation and known past cases. The model never does the comparison itself,
so every count and member id in a report comes from code, not from the model.

Here the comparison runs from the command line with no API key. The same code is also exposed as the
`compare_edi_environments` MCP tool in [spring-boot-mcp-server](https://github.com/Adit12T/spring-boot-mcp-server).

## Design decisions

- **Only compare what is enabled in both environments.** Otherwise the report fills with noise from
  clients that were never meant to match.
- **Classify, don't just diff.** "Missing", "duplicate" and "changed" lead to different fixes,
  so the report names the type instead of dumping a text diff.
- **A missing file is one finding, not hundreds.** If a client has no files in the candidate,
  that is reported once instead of as one missing record per member.
- **Causes are hints.** The report says so. A developer confirms them.

## Layout

```
src/main/java/.../edirecon   the reconciler (EdiReconciler is the entry point)
src/test/java/.../edirecon   JUnit 5 tests
sample-data/env-a, env-b     synthetic client lists and EDI files for two environments
docs/sample-report.md        example report
```

## License

Copyright © 2026 Aditya Tripathi. All rights reserved. The code is published for viewing only; see [LICENSE](LICENSE). Security reports: see [SECURITY.md](SECURITY.md).
