# ⚡ WattWise — AI Home Energy Tracker & Savings Advisor

> A multi-agent AI system built in Java with [Embabel](https://github.com/embabel/embabel-agent) and Spring Boot. Describe your household's appliances and your electricity plan — it calculates exactly where your money goes, tells you what's actually worth fixing, and computes real payback periods instead of vague promises.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Embabel](https://img.shields.io/badge/Embabel-Agent%20Platform-7F77DD)](https://github.com/embabel/embabel-agent)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#-license)

---

## 📖 The Problem

Most people know their electricity bill "seems high" without knowing why. Utility bills report a total, not a breakdown. Generic energy-saving advice ("unplug unused devices," "use LED bulbs") is technically true and almost useless, because it isn't scaled to *your* actual appliances, *your* actual tariff, or *your* actual usage habits.

Two things usually go wrong when people try to fix this themselves:

1. **The math is tedious and error-prone** — manually calculating watts × hours × rate for a dozen appliances, then comparing peak vs. off-peak pricing, is exactly the kind of repetitive arithmetic people give up on halfway through.
2. **Advice isn't prioritized by actual dollar impact** — a list of ten generic tips with no sense of which one saves $2/month and which one saves $40/month leads to fixing the wrong thing first.

**WattWise** exists to replace both of these with real numbers: a grounded, appliance-by-appliance cost breakdown, and a savings plan ranked by actual impact — including honest payback periods for anything that requires spending money to save money.

---

## 🧠 Overview

WattWise is a Java-based AI agent, built on the [Embabel Agent Framework](https://github.com/embabel/embabel-agent) on top of Spring Boot and Spring AI, that takes a free-text description of a household's appliances and electricity tariff and produces:

- a **real, calculated cost breakdown** — per appliance, in kWh and dollars, computed by actual arithmetic,
- **cost-significant efficiency findings** — not generic tips, but issues that are genuinely moving the needle on this specific bill,
- a **ranked savings plan** with monthly savings estimates and payback periods computed deterministically,
- a **plain-language bill explanation** anyone can understand without thinking in kilowatt-hours, and
- an **off-peak scheduling guide** for flexible appliances, where the tariff plan makes that worthwhile.

**What makes the numbers trustworthy:** WattWise treats "calculate the cost" and "estimate a savings plan's judgment calls" as two different kinds of work. Multiplying watts by hours by rate across a dozen appliances, and later dividing an upfront cost by a monthly savings figure, are both exact arithmetic with one correct answer — so both happen in real Java code, not as an LLM's mental math. The model's job is judgment: which inefficiencies are actually worth mentioning, which savings actions are realistic, how to explain it all in plain language. That division of labor is what lets you trust the dollar figures instead of treating them as a rough AI guess.

**Who this is for:**
- Homeowners or renters who want to know where their electricity bill actually comes from, not just that it's high
- Developers exploring Embabel with an example centered on trustworthy numeric output, not just generated text
- Anyone comparing a time-of-use electricity plan against a flat-rate one and wondering if shifting habits is actually worth it

---

## ✨ Features

### 🔢 Grounded Cost Calculation
- **Real per-appliance arithmetic** — kWh and dollar cost for every appliance are computed by actual code (wattage × hours × price), not estimated by an LLM across a long list
- **Peak/off-peak aware** — supports flat-rate and time-of-use tariff structures, including a fixed monthly charge

### 🔍 Efficiency Analysis
- **Cost-significant findings only** — flags real, meaningful inefficiencies (always-on high-draw devices, usage patterns worth reconsidering) rather than padding the list with negligible advice
- **Domain-specific error handling** — a dedicated `HouseholdProfileIncompleteException` fires if no appliances could be identified at all, instead of silently calculating a $0 bill

### 💰 Ranked, Justified Savings
- **Impact-ordered savings plan** — actions ranked by actual estimated dollar savings, not a flat checklist
- **Real payback periods** — for any action requiring an upfront purchase, the payback period is computed by exact division in plain Java, never left to the model's arithmetic across several actions at once

### 🗣️ Plain-Language Reporting
- **Bill explanation anyone can read** — translates the cost breakdown into a summary a non-technical homeowner immediately understands, not a table of kWh figures
- **Off-peak scheduling guidance** — recommends specific timing shifts for flexible appliances (laundry, EV charging) where a peak/off-peak price gap makes it worthwhile, and says so plainly when it doesn't

---

## 🧩 How It Works

WattWise isn't a fixed script — it's a set of declarative **actions**, each stating what type it needs and what type it produces. Embabel's planner resolves the dependency graph and runs only what's needed to reach whichever goal is requested. Ask for just the bill explanation, and it still needs the cost breakdown underneath it — but never generates a savings plan or schedule along the way.

**The flow, in prose:**

1. **Extraction** — the household's appliance list and its electricity tariff are parsed independently from the same input text.
2. **Cost calculation** *(deterministic)* — a real Java tool computes each appliance's monthly kWh consumption and cost from its wattage, usage hours, and rate — exact arithmetic, not an estimate.
3. **Efficiency analysis** — an LLM, prompted with an energy-auditor persona, reviews the calculated breakdown alongside stated usage patterns and flags only the inefficiencies that are actually cost-significant.
4. **Savings planning** *(Goal 1)* — the model proposes ranked actions with estimated savings and, where relevant, an upfront cost; a plain Java calculator then computes the exact payback period for each — a division the model never has to get right on its own.
5. **Bill explanation** *(Goal 2)* — an independent, downstream deliverable translating the cost breakdown into plain language, reachable without generating a savings plan.
6. **Appliance scheduling** *(Goal 3)* — another independent downstream deliverable, recommending timing shifts into off-peak hours where the tariff plan actually makes that worthwhile.

**Text-based pipeline map:**

```
UserInput (appliances + electricity tariff)
   │
   ├──► extractHouseholdProfile ──────────► HouseholdProfile
   └──► extractTariffPlan ────────────────► TariffPlan
                    │
                    ▼
   calculateCostBreakdown (deterministic) ──► CostBreakdownReport
                    │
                    ▼
         identifyEfficiencyIssues ──────────► EfficiencyReport
                    │
                    ▼
              generateSavingsPlan            🎯 GOAL  ──► SavingsPlan
              (payback periods computed in plain Java)


   HouseholdProfile + TariffPlan + CostBreakdownReport
                    │
                    ├──► generateBillBreakdownReport (ReportingAgent)  🎯 GOAL  ──► BillBreakdownReport
                    └──► generateApplianceSchedule (SchedulingAgent)   🎯 GOAL  ──► ApplianceSchedule
```

Three focused agent classes contribute actions toward these goals:

| Agent | Responsibility |
|---|---|
| `EnergyAnalysisAgent` | Owns the core spine — extraction, deterministic cost calculation, efficiency analysis, and the ranked savings plan |
| `ReportingAgent` | Independent downstream goal — a plain-language bill explanation |
| `SchedulingAgent` | Independent downstream goal — off-peak timing recommendations |

---

## 🗂️ Project Structure

```
wattwise/
├── src/main/java/com/wattwise/
│   ├── WattWiseApplication.java           # Spring Boot entry point
│   │
│   ├── agent/
│   │   ├── EnergyAnalysisAgent.java       # Core: extraction → cost calc → efficiency → savings plan
│   │   ├── ReportingAgent.java            # Plain-language bill explanation
│   │   └── SchedulingAgent.java           # Off-peak appliance scheduling
│   │
│   ├── config/
│   │   └── WattWiseProperties.java        # Max savings actions, max schedule recommendations
│   │
│   ├── exception/
│   │   └── HouseholdProfileIncompleteException.java
│   │
│   ├── model/
│   │   ├── ApplianceUsage.java
│   │   ├── HouseholdProfile.java
│   │   ├── TariffPlan.java
│   │   ├── ApplianceCostBreakdown.java
│   │   ├── CostBreakdownReport.java
│   │   ├── EfficiencyFinding.java
│   │   ├── EfficiencyReport.java
│   │   ├── SavingsAction.java
│   │   ├── SavingsPlan.java
│   │   ├── BillBreakdownReport.java
│   │   └── ApplianceSchedule.java
│   │
│   ├── persona/
│   │   └── Personas.java                  # ENERGY_AUDITOR, HOME_ENERGY_COACH
│   │
│   └── tool/
│       ├── EnergyCostCalculatorTool.java   # @LlmTool — real kWh/cost arithmetic
│       └── PaybackPeriodTool.java          # Plain @Component — NOT exposed to the LLM
│
└── src/main/resources/
    └── application.yml                     # Spring, Embabel model, and WattWise config
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
git clone https://github.com/drissiOmar98/wattwise.git
cd wattwise
```

**2. Set your environment variables**

```bash
export ANTHROPIC_API_KEY=your-anthropic-key
```

**3. Configure `application.yml`** (defaults shown below — adjust as needed)

```yaml
watt-wise:
  max-savings-actions: 8            # ranked savings actions generated
  max-schedule-recommendations: 6   # appliance timing shifts generated

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

Once running, submit a free-text description of your appliances and electricity plan, through however your Embabel setup exposes it (CLI prompt, Shell, or REST endpoint, depending on your platform configuration) — for example:

```
We're a family of 4 in Phoenix, Arizona. Our fridge runs 24/7 at about
150 watts. We run central AC around 12 hours a day in summer at 3000
watts. Washing machine runs about 5 hours a week at 500 watts, and we
have an old chest freezer in the garage running 24/7 at 200 watts.

Our electricity plan is time-of-use: $0.12/kWh off-peak, $0.28/kWh
during peak hours (weekdays 3pm-8pm), plus a $10 fixed monthly charge.
```

**Sample `CostBreakdownReport` output** *(excerpt)*:

```json
{
  "breakdown": [
    { "applianceName": "Fridge", "estimatedKwhPerMonth": 108.0, "estimatedCostPerMonthUsd": 12.96 },
    { "applianceName": "Central AC", "estimatedKwhPerMonth": 1080.0, "estimatedCostPerMonthUsd": 129.60 },
    { "applianceName": "Chest freezer", "estimatedKwhPerMonth": 144.0, "estimatedCostPerMonthUsd": 17.28 }
  ],
  "totalEstimatedMonthlyCostUsd": 168.44
}
```

**Sample `EfficiencyReport` output** *(excerpt)*:

```json
{
  "findings": [
    {
      "appliance": "Chest freezer",
      "issue": "An old, always-on chest freezer draws a steady $17/month for likely limited storage need.",
      "recommendation": "Consider consolidating into the main fridge/freezer or replacing with a modern, more efficient model."
    }
  ]
}
```

**Sample `SavingsPlan` output** *(excerpt)*:

```json
{
  "actions": [
    {
      "action": "Replace the old chest freezer with a modern Energy Star model",
      "estimatedMonthlySavingsUsd": 10.0,
      "estimatedUpfrontCostUsd": 250.0,
      "effortLevel": "requires a one-time purchase",
      "paybackPeriod": "2.1 years"
    },
    {
      "action": "Raise the AC thermostat by 2°F during peak hours",
      "estimatedMonthlySavingsUsd": 15.0,
      "estimatedUpfrontCostUsd": 0.0,
      "effortLevel": "no cost, just a habit change",
      "paybackPeriod": "N/A"
    }
  ],
  "totalEstimatedMonthlySavingsUsd": 25.0
}
```

**Sample `ApplianceSchedule` output** *(excerpt)*:

```json
{
  "scheduleRecommendations": [
    "Run the washing machine after 8pm on weekdays instead of during the 3pm-8pm peak window - roughly $0.08 saved per load at this rate difference."
  ]
}
```

---

## 🗺️ Roadmap

- [ ] Solar panel / battery ROI estimation as an additional goal
- [ ] Historical bill import for trend comparison over time
- [ ] Regional average comparison (how this household stacks up locally)
- [ ] Multi-month seasonal modeling (summer AC vs. winter heating load)
- [ ] REST API layer for external integrations

---

## 🤝 Contributing

Issues and pull requests are welcome. If you're proposing a new action or agent, please:

1. Keep it focused on a single responsibility (one action, one job)
2. Add Javadoc explaining what it consumes and produces
3. If a number has exactly one correct value (a calculation, a division, a sum), compute it in plain Java — reserve the LLM for genuine judgment calls, following the pattern in `tool/`

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.

---

## 👤 Author

Built by **[Omar Drissi](https://github.com/drissiOmar98)** — feel free to open an issue, star the repo ⭐, or connect on GitHub.
