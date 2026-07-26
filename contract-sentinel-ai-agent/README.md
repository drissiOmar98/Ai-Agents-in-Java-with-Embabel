# 🛰️ ContractSentinel — AI API Contract Guardian

> A multi-agent AI system built in Java with [Embabel](https://github.com/embabel/embabel-agent) and Spring Boot. Give it the previous and current version of an API contract — it tells you exactly what breaks, computes the correct semver bump by rule instead of guesswork, and writes the migration guide and changelog before your consumers find out the hard way.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Embabel](https://img.shields.io/badge/Embabel-Agent%20Platform-7F77DD)](https://github.com/embabel/embabel-agent)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#-license)

---

## 📖 The Problem

Every team running a public or internal API eventually ships a "small" change that quietly breaks a consumer: a response field gets renamed, an endpoint gets removed, a request field that used to be optional becomes required. Nobody meant to break anything — it just wasn't caught before release.

Three things usually go wrong at once:

1. **Nobody carefully diffs the old and new contract** — reviewers eyeball a pull request, not the full shape of every endpoint.
2. **The version bump is a guess** — teams argue about whether a change is "really" a major or not, and the decision is inconsistent from release to release.
3. **The changelog is an afterthought** — written in five minutes right before release, or skipped entirely.

**ContractSentinel** exists to make all three of these mechanical instead of hopeful.

---

## 🧠 Overview

ContractSentinel is a Java-based AI agent, built on the [Embabel Agent Framework](https://github.com/embabel/embabel-agent) on top of Spring Boot and Spring AI, that takes a free-text description of an API's previous and current contract and produces:

- a **mechanically computed structural diff** — endpoints added, removed, or changed,
- a **consumer-impact classification** of every change (breaking / non-breaking / deprecation),
- the **objectively correct semantic version bump**, decided by a rule engine — not an LLM guess,
- a **step-by-step migration guide** for existing consumers, and
- a **Keep-a-Changelog-style entry** ready to paste into your release notes.

**What makes this different from "ask an LLM to compare two API specs":** ContractSentinel treats the comparison itself as a job for real code, not a language model. Comparing two lists of endpoints for exact additions, removals, and field-level changes is precisely the kind of multi-item, exact-matching task LLMs get unreliable at past a handful of items. So the diff runs in plain Java, and the model's job is judgment on top of a ground truth — deciding what a change *means* for a consumer, not deciding what changed in the first place.

**Who this is for:**
- Backend/API teams who want a second pair of eyes before every release, especially in microservices architectures where one team's "minor" change is another team's outage
- Developers exploring Embabel with an example that demonstrates a real architectural principle: not every step in an agent pipeline needs to call an LLM
- Anyone tired of semver bumps being decided by vibes

---

## ✨ Features

### 🔍 Structural Diffing
- **Deterministic endpoint comparison** — real set-based diffing (not an LLM eyeballing two lists) identifies exactly which endpoints were added, removed, or had their request/response fields changed
- **Field-level precision** — changes are reported down to which specific request or response field was added or removed on a given endpoint

### 🚨 Breaking-Change Intelligence
- **Consumer-impact classification** — every change is judged as `BREAKING`, `NON_BREAKING`, or `DEPRECATION`, with a concrete explanation of what actually happens to an existing integration
- **Domain-specific error handling** — a dedicated `ApiSnapshotIncompleteException` fires if either contract version can't be extracted at all, instead of silently diffing two empty lists and reporting "no changes"

### 🔢 Rule-Based Versioning
- **Semver bump computed by rule, not guessed** — any breaking change forces a major bump, any new endpoint alone forces a minor bump, otherwise it's a patch — decided by a plain rule engine with exactly one correct answer per [semver.org](https://semver.org)
- **LLM used only for phrasing, never for the decision** — the model explains *why* the bump is correct in plain language, grounded in the actual computed counts, but never gets a vote on *what* the bump should be

### 📋 Consumer-Facing Output
- **Step-by-step migration guide** — concrete, ordered actions an existing consumer team needs to take, explicitly naming every breaking change and how to adapt to it
- **Keep-a-Changelog-style entry** — a standard Added/Changed/Removed/Deprecated structure, written to be skimmed in 30 seconds by a consuming team checking "does this affect me?"

---

## 🧩 How It Works

ContractSentinel isn't a fixed script — it's a set of declarative **actions**, each stating what type it needs and what type it produces. Embabel's planner resolves the dependency graph and runs only what's needed to reach whichever goal is requested. Ask for just the changelog, and it still needs the diff and classification underneath it — but never generates a migration guide along the way.

**The flow, in prose:**

1. **Extraction** — the previous and current contract descriptions are parsed independently from the same input text into two distinct snapshot types, so the planner can treat "old" and "new" as separate, unambiguous facts.
2. **Structural diff** *(deterministic)* — a real Java tool compares the two endpoint sets by exact key and field-name matching, producing a mechanical list of what was added, removed, and changed. No model judgment happens here — only exact comparison.
3. **Breaking-change classification** — an LLM, prompted with an API-architect persona, reads the mechanical diff and judges the real consumer impact of each item: does this actually break an existing integration, or just add something new?
4. **Semver recommendation** *(rule-based)* — given the classified counts, a plain rule engine decides the bump type outright. The LLM is invoked afterward only to write a one-sentence explanation of a decision that's already been made.
5. **Migration guide** *(Goal 1)* — a concrete, ordered set of steps for consumers, built from the classification and the semver rationale.
6. **Changelog entry** *(Goal 2)* — an independent, downstream deliverable built from the same diff and classification data, reachable without ever generating a migration guide.

**Text-based pipeline map:**

```
UserInput (previous + current contract description)
   │
   ├──► extractPreviousSnapshot ───────► PreviousApiSnapshot
   └──► extractCurrentSnapshot ────────► CurrentApiSnapshot
                    │
                    ▼
         compareSnapshots (deterministic diff) ───► ApiDiffReport
                    │
                    ▼
         classifyBreakingChanges ───────────────────► BreakingChangeReport
                    │
                    ▼
   recommendSemverBump (VersioningAgent, rule-based) ──► SemverRecommendation
                    │
                    ▼
              generateMigrationGuide           🎯 GOAL  ──► MigrationGuide


   ApiDiffReport + BreakingChangeReport + SemverRecommendation
                    │
                    ▼
   generateChangelogEntry (ChangelogAgent)      🎯 GOAL  ──► ChangelogEntry
```

Three focused agent classes contribute actions toward these goals:

| Agent | Responsibility |
|---|---|
| `ApiDiffAgent` | Owns the core spine — extraction → structural diff → classification → migration guide |
| `VersioningAgent` | Rule-based semver decision; the LLM only phrases the rationale, never makes the call |
| `ChangelogAgent` | Independent downstream goal — a Keep-a-Changelog entry, reachable on its own |

---

## 🗂️ Project Structure

```
contractsentinel/
├── src/main/java/com/contractsentinel/
│   ├── ContractSentinelApplication.java   # Spring Boot entry point
│   │
│   ├── agent/
│   │   ├── ApiDiffAgent.java              # Core: extraction → diff → classify → migration guide
│   │   ├── VersioningAgent.java           # Rule-based semver bump + LLM rationale
│   │   └── ChangelogAgent.java            # Changelog entry generation
│   │
│   ├── config/
│   │   └── ContractSentinelProperties.java # Max migration steps
│   │
│   ├── exception/
│   │   └── ApiSnapshotIncompleteException.java
│   │
│   ├── model/
│   │   ├── ApiEndpoint.java
│   │   ├── PreviousApiSnapshot.java
│   │   ├── CurrentApiSnapshot.java
│   │   ├── ApiDiffReport.java
│   │   ├── BreakingChange.java
│   │   ├── BreakingChangeReport.java
│   │   ├── SemverRecommendation.java
│   │   ├── MigrationGuide.java
│   │   └── ChangelogEntry.java
│   │
│   ├── persona/
│   │   └── Personas.java                  # API_ARCHITECT, TECHNICAL_WRITER
│   │
│   └── tool/
│       ├── ApiDiffTool.java                # @LlmTool — real structural diff (set comparison)
│       └── SemverRuleTool.java             # Plain rule engine — NOT exposed to the LLM
│
└── src/main/resources/
    └── application.yml                     # Spring, Embabel model, and ContractSentinel config
```

---

## 🛠️ Tech Stack

| Layer | Technology                                          |
|---|-----------------------------------------------------|
| Language | Java 21                                             |
| Application framework | Spring Boot 4.x                                     |
| Agent framework | [Embabel](https://github.com/embabel/embabel-agent) |
| LLM integration | Spring AI                                           |
| Default model | Claude Sonnet                                       |
| Build tool | Maven                                               |

---

## ✅ Prerequisites

- **Java 21+**
- **Maven 3.9+**
- An **Anthropic** (or other Spring AI-supported provider) API key, matching whichever model you configure under `embabel.models.default-llm`

---

## 🚀 Getting Started

**1. Clone the repository**

```bash
git clone https://github.com/drissiOmar98/contractsentinel.git
cd contractsentinel
```

**2. Set your environment variables**

```bash
export ANTHROPIC_API_KEY=your-anthropic-key
```

**3. Configure `application.yml`** (default shown below — adjust as needed)

```yaml
contract-sentinel:
  max-migration-steps: 10   # ordered steps generated in the consumer migration guide

embabel:
  models:
    default-llm: claude-sonnet-4-6
```

**4. Build and run**

```bash
./mvnw clean install
./mvnw spring-boot:run
```

---

## ▶️ Usage

Once running, submit a free-text description of both contract versions, through however your Embabel setup exposes it (CLI prompt, Shell, or REST endpoint, depending on your platform configuration) — for example:

```
Previous version:
GET /users/{id} - request: (none) - response: id:Long, name:String, email:String
DELETE /users/{id} - request: (none) - response: (none)

Current version:
GET /users/{id} - request: (none) - response: id:Long, name:String, emailAddress:String
GET /users/{id}/orders - request: (none) - response: orderIds:List<Long>
```

**Sample `ApiDiffReport` output:**

```json
{
  "addedEndpoints": ["GET /users/{id}/orders"],
  "removedEndpoints": ["DELETE /users/{id}"],
  "changedEndpoints": [
    "GET /users/{id} [response fields removed=[email]; response fields added=[emailAddress]]"
  ]
}
```

**Sample `BreakingChangeReport` output** *(excerpt)*:

```json
{
  "changes": [
    {
      "change": "DELETE /users/{id}",
      "severity": "BREAKING",
      "consumerImpact": "Any consumer currently calling this endpoint to delete a user will receive a 404/405 - the operation no longer exists."
    },
    {
      "change": "GET /users/{id} response field 'email' removed, 'emailAddress' added",
      "severity": "BREAKING",
      "consumerImpact": "Consumers deserializing the response and reading 'email' will get a null or missing field; the data now arrives under a different key."
    }
  ],
  "hasBreakingChanges": true
}
```

**Sample `SemverRecommendation` output:**

```json
{
  "bumpType": "MAJOR",
  "rationale": "With 2 breaking changes detected, semver requires a major version bump regardless of the 1 new endpoint also added."
}
```

**Sample `MigrationGuide` output** *(excerpt)*:

```json
{
  "steps": [
    "Update any code calling DELETE /users/{id} - this operation has been removed with no replacement; confirm whether user deletion is still needed and raise it with the API team if so.",
    "Update response deserialization for GET /users/{id}: replace references to the 'email' field with 'emailAddress'.",
    "Optionally integrate the new GET /users/{id}/orders endpoint if your service needs order data - this is additive and does not require any changes to existing calls."
  ]
}
```

**Sample `ChangelogEntry` output:**

```json
{
  "added": ["GET /users/{id}/orders returns a user's order IDs"],
  "changed": [],
  "removed": ["DELETE /users/{id} has been removed"],
  "deprecated": []
}
```

---

## 🗺️ Roadmap

- [ ] Direct OpenAPI/Swagger spec file ingestion instead of free-text descriptions
- [ ] Consumer registry integration — flag which known consumer services are actually affected by a given breaking change
- [ ] GitHub Action / CI integration to run automatically on pull requests touching API contracts
- [ ] Historical changelog aggregation across multiple releases
- [ ] Support for gRPC/protobuf contract diffing alongside REST

---

## 🤝 Contributing

Issues and pull requests are welcome. If you're proposing a new action or agent, please:

1. Keep it focused on a single responsibility (one action, one job)
2. Add Javadoc explaining what it consumes and produces
3. If a step's answer can be computed deterministically from data the code already has, compute it in plain Java — don't route it through the LLM just because other steps do

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.

---

## 👤 Author

Built by **[Omar Drissi](https://github.com/drissiOmar98)** — feel free to open an issue, star the repo ⭐, or connect on GitHub.
