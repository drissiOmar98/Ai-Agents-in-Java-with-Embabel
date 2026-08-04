# ✈️ SkyClaim — AI Flight Disruption & Compensation Agent

> A multi-agent AI system built in Java with [Embabel](https://github.com/embabel/embabel-agent) and Spring Boot. Describe your flight delay or cancellation — it calculates exactly what EU261 provides for, judges whether you're likely eligible, and drafts the claim letter, while telling you what you're owed right now regardless of how that claim turns out.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Embabel](https://img.shields.io/badge/Embabel-Agent%20Platform-7F77DD)](https://github.com/embabel/embabel-agent)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#-license)

---

## ⚠️ Important: Informational Guidance, Not Legal Advice

SkyClaim helps travelers understand what EU Regulation 261/2004 may provide for and drafts a starting-point claim letter. **It is not a substitute for legal advice.** Every eligibility assessment it produces is a judgment based on the facts described to it — it cannot verify weather records, maintenance logs, or an airline's actual internal justification. Always confirm eligibility with the airline directly or your national enforcement body before relying on any figure or assessment SkyClaim produces. This principle isn't a footnote here — it's built into the data model itself (see [`EligibilityAssessment`](#-how-it-works) below).

---

## 📖 The Problem

Most air passengers have no real sense of what a delay or cancellation entitles them to. EU261 is a real, enforceable regulation with specific compensation amounts — but almost nobody remembers the distance/delay table, and the process of actually claiming it is deliberately tedious: airlines often deny or lowball claims by leaning on "extraordinary circumstances," hoping the traveler won't push back or won't know the difference between a genuine exemption and a stretch.

Two separate things get conflated when people try to sort this out themselves:

1. **What the compensation amount actually is** — this is a fixed number determined by flight distance and delay length. There's no ambiguity here; most people just don't know the table.
2. **Whether you're actually eligible for it** — this genuinely is ambiguous, and depends on case law nuance (a routine mechanical fault usually doesn't excuse the airline; a bird strike or an ATC strike usually does).

Treating both as equally uncertain leads travelers to either give up entirely or accept whatever the airline tells them. **SkyClaim keeps these separate on purpose.**

---

## 🧠 Overview

SkyClaim is a Java-based AI agent, built on the [Embabel Agent Framework](https://github.com/embabel/embabel-agent) on top of Spring Boot and Spring AI, that takes a free-text description of a flight disruption and produces:

- the **exact compensation figure** EU261's Article 7 table specifies for the flight's distance and delay, computed by rule,
- a **jurisdiction and extraordinary-circumstance assessment** — a genuine judgment call, made explicitly and with a disclaimer, not glossed over,
- a **draft compensation claim letter** ready to review and send,
- a summary of **immediate care entitlements** (meals, hotel, communication) the airline owes right now, independently of the compensation question, and
- **practical next-step advice** for rebooking and preserving evidence.

**The core design decision, worth understanding:** SkyClaim treats "what does the law say the amount is" and "does this situation actually qualify" as two fundamentally different kinds of work. The compensation amount is decided by a plain Java rule engine — the regulation's distance/delay table has exactly one correct answer, and an LLM "calculating" it risks a confident, wrong figure. Whether the airline's stated reason plausibly counts as an extraordinary circumstance is a real judgment call the regulation doesn't reduce to a formula, so that stays with the model — and every judgment it makes comes with an explicit reminder that it isn't a legal determination.

**Who this is for:**
- Travelers who've had a flight delayed or cancelled and don't know where to start
- Developers exploring Embabel with an example centered on knowing precisely which parts of a problem are rule-based and which require genuine reasoning
- Anyone curious how to build an AI tool in a consumer-rights space responsibly, with disclaimers as a structural part of the data model rather than an afterthought

---

## ✨ Features

### 🔢 Rule-Based Compensation
- **The exact EU261 Article 7 amount** — €250 / €400 / €600 (with a reduced long-haul tier) computed by a real rule engine based on flight distance and arrival delay, not an LLM's approximation
- **Domain-specific error handling** — a dedicated `DisruptionDetailsIncompleteException` fires if no disruption details could be identified at all, instead of silently calculating compensation for nothing

### ⚖️ Honest Eligibility Assessment
- **Jurisdiction analysis** — assesses whether the flight plausibly falls under EU261 based on departure/arrival country and the operating airline
- **Extraordinary-circumstance judgment** — weighs the airline's stated reason against real case-law patterns (routine mechanical issues typically don't exempt the airline; weather, ATC restrictions, and security threats typically do)
- **Structural disclaimer** — every assessment includes an explicit, required statement that this is informational guidance, not a legal determination

### 📝 Claim Support
- **Draft claim letter** — a formal, factual letter citing EU261 and the specific amount owed, ready for the traveler to review, personalize, and send
- **Care entitlements, independent of the compensation question** — meals, hotel, and communication rights that apply during a long delay regardless of whether the airline can later claim an exemption from cash compensation
- **Practical next steps** — rebooking options, a realistic response timeline, and exactly what evidence to preserve if the claim is disputed

---

## 🧩 How It Works

SkyClaim isn't a fixed script — it's a set of declarative **actions**, each stating what type it needs and what type it produces. Embabel's planner resolves the dependency graph and runs only what's needed to reach whichever goal is requested. A traveler at the airport can ask for just their immediate care entitlements without ever touching the compensation question.

**The flow, in prose:**

1. **Extraction** — the disruption's route, distance, delay, and stated reason are parsed from the traveler's description, independently from their own context (name, countries involved, booking reference).
2. **Compensation calculation** *(deterministic)* — a real Java rule engine computes the exact amount EU261's distance/delay table specifies. No model judgment happens here — only rule application.
3. **Eligibility assessment** — an LLM, prompted with a consumer-rights-advocate persona, judges jurisdiction and whether the stated reason plausibly qualifies as an extraordinary circumstance, always attaching an explicit non-legal-advice disclaimer.
4. **Claim letter** *(Goal 1)* — a formal letter is drafted from the computed amount and the eligibility judgment, careful not to overstate certainty where the assessment itself is uncertain.
5. **Care entitlements** *(Goal 2)* — an independent, downstream deliverable, reachable directly from the disruption facts alone — a traveler needs to know this immediately, before any compensation question is resolved.
6. **Rebooking advice** *(Goal 3)* — another independent downstream deliverable covering practical next steps and what evidence to keep.

**Text-based pipeline map:**

```
UserInput (flight disruption + traveler context)
   │
   ├──► extractFlightDisruption ──────────► FlightDisruption
   └──► extractTravelerContext ───────────► TravelerContext
                    │
                    ▼
   calculateCompensation (deterministic rule) ──► CompensationCalculation
                    │
                    ▼
         assessEligibility (judgment call) ───────► EligibilityAssessment
                    │
                    ▼
              draftClaimLetter                🎯 GOAL  ──► ClaimLetter


   FlightDisruption
                    │
                    ▼
   summarizeCareEntitlements (CareEntitlementsAgent)  🎯 GOAL  ──► CareEntitlementsSummary


   FlightDisruption + EligibilityAssessment
                    │
                    ▼
   generateRebookingAdvice (RebookingAdvisorAgent)    🎯 GOAL  ──► RebookingAdvice
```

Three focused agent classes contribute actions toward these goals:

| Agent | Responsibility |
|---|---|
| `DisruptionAnalysisAgent` | Owns the core spine — extraction, rule-based compensation, eligibility judgment, and the claim letter |
| `CareEntitlementsAgent` | Independent goal — immediate care rights, reachable without an eligibility judgment |
| `RebookingAdvisorAgent` | Independent goal — practical next steps and evidence guidance |

---

## 🗂️ Project Structure

```
skyclaim/
├── src/main/java/com/skyclaim/
│   ├── SkyClaimApplication.java           # Spring Boot entry point
│   │
│   ├── agent/
│   │   ├── DisruptionAnalysisAgent.java   # Core: extraction → compensation → eligibility → claim letter
│   │   ├── CareEntitlementsAgent.java     # Immediate care entitlements
│   │   └── RebookingAdvisorAgent.java     # Rebooking and next-step advice
│   │
│   ├── config/
│   │   └── SkyClaimProperties.java        # Max rebooking steps, max care entitlements
│   │
│   ├── exception/
│   │   └── DisruptionDetailsIncompleteException.java
│   │
│   ├── model/
│   │   ├── FlightDisruption.java
│   │   ├── TravelerContext.java
│   │   ├── CompensationCalculation.java
│   │   ├── EligibilityAssessment.java     # Carries a mandatory legal disclaimer field
│   │   ├── ClaimLetter.java
│   │   ├── CareEntitlementsSummary.java
│   │   └── RebookingAdvice.java
│   │
│   ├── persona/
│   │   └── Personas.java                  # CONSUMER_RIGHTS_ADVOCATE, TRAVEL_SUPPORT_AGENT
│   │
│   └── tool/
│       └── Eu261CompensationRuleTool.java  # Plain @Component — NOT exposed to the LLM
│
└── src/main/resources/
    └── application.yml                     # Spring, Embabel model, and SkyClaim config
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
git clone https://github.com/drissiOmar98/skyclaim.git
cd skyclaim
```

**2. Set your environment variables**

```bash
export ANTHROPIC_API_KEY=your-anthropic-key
```

**3. Configure `application.yml`** (defaults shown below — adjust as needed)

```yaml
sky-claim:
  max-rebooking-steps: 8       # ordered next-step recommendations generated
  max-care-entitlements: 6     # immediate care entitlement items listed

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

Once running, submit a free-text description of the disruption and your own context, through however your Embabel setup exposes it (CLI prompt, Shell, or REST endpoint, depending on your platform configuration) — for example:

```
My name is Sara Bennett. I was on Lufthansa flight LH441 from Frankfurt
(Germany) to New York JFK (USA), about 6200 km. The flight was scheduled
to depart at 10:15am but was delayed and I landed 5 hours late. The
airline said it was due to a technical issue with the aircraft. My
booking reference is XJ4K9P.
```

**Sample `CompensationCalculation` output:**

```json
{
  "amountEur": 600.0,
  "ruleApplied": "long-haul (>3500km), delay >= 4h",
  "explanation": "Because this flight covered roughly 6200 km and arrived 5 hours late, it falls into the long-haul, full-compensation tier, which provides for €600."
}
```

**Sample `EligibilityAssessment` output:**

```json
{
  "likelyEligible": true,
  "jurisdictionBasis": "The flight departed Frankfurt, an EU airport, so EU261 applies regardless of the airline's nationality or the destination.",
  "extraordinaryCircumstanceAssessment": "A 'technical issue' is often, though not always, attributable to the airline's own maintenance responsibilities rather than a genuine extraordinary circumstance - EU case law (Wallentin-Hermann) generally does not treat routine technical faults as exempting the airline. This looks likely to favor the traveler, but the airline may dispute the characterization.",
  "assessmentDisclaimer": "This is informational guidance, not legal advice. Confirm with the airline or your national enforcement body before relying on this assessment."
}
```

**Sample `ClaimLetter` output** *(excerpt)*:

```
Subject: EU261 Compensation Claim - Flight LH441, 12 August 2026

Dear Lufthansa Customer Relations,

I am writing to request compensation under EU Regulation 261/2004 for
flight LH441 from Frankfurt to New York JFK on 12 August 2026, which
arrived approximately 5 hours behind schedule. Under Article 7 of the
Regulation, this delay entitles me to €600 in compensation...
```

**Sample `CareEntitlementsSummary` output:**

```json
{
  "entitlements": [
    "Meals and refreshments appropriate to the wait time",
    "Two free phone calls, emails, or faxes",
    "Hotel accommodation and transport to/from it, since this delay involves an overnight stay"
  ],
  "note": "These care entitlements apply regardless of the separate compensation eligibility question - the airline owes this assistance even if it later disputes paying cash compensation."
}
```

---

## 🗺️ Roadmap

- [ ] US DOT rule support for domestic and US-bound disruptions, alongside EU261
- [ ] Direct submission integration with common airline claim portals
- [ ] National enforcement body lookup by country, for escalation if the airline doesn't respond
- [ ] Multi-passenger claims (families/groups traveling together)
- [ ] REST API layer for external integrations

---

## 🤝 Contributing

Issues and pull requests are welcome. If you're proposing a new action or agent, please:

1. Keep it focused on a single responsibility (one action, one job)
2. Add Javadoc explaining what it consumes and produces
3. Preserve the disclaimer structure — anything resembling a legal or eligibility determination must clearly state it's informational guidance, not legal advice
4. If a number has exactly one correct value under a known rule (like the compensation table), compute it in plain Java rather than asking the LLM to recall it

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.

This project is a technical demonstration and personal tool. It is not a legal service, and using it does not create any advisory relationship. Always verify compensation eligibility with the airline or the relevant national enforcement body.

---

## 👤 Author

Built by **[Omar Drissi](https://github.com/drissiOmar98)** — feel free to open an issue, star the repo ⭐, or connect on GitHub.
