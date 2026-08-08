# 🧭 TripCompass — AI Multi-City Trip Feasibility & Itinerary Planner

> A multi-agent AI system built in Java with [Embabel](https://github.com/embabel/embabel-agent) and Spring Boot. Describe your multi-city trip — it checks whether the plan is actually realistic before you book anything, restructures it if it isn't, and builds out the budget and packing list to match.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Embabel](https://img.shields.io/badge/Embabel-Agent%20Platform-7F77DD)](https://github.com/embabel/embabel-agent)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#-license)

---

## 📖 The Problem

"2 days in Paris, 1 day in Brussels, 2 days in Amsterdam" looks completely reasonable written down. It's the kind of plan that gets built in a spreadsheet or a group chat, ticket prices get checked, and everyone nods along — right up until day 3 turns out to be mostly a train station, a rushed lunch, and half an afternoon in a city everyone's too tired to actually enjoy.

The mistake isn't the cities. It's that **transit time between them is almost never accounted for honestly**. A "1 day" stop that requires a 4-hour train each way isn't really a day — it's a few hours, bookended by exhaustion. Multiply that across a multi-city trip and a plan that looked balanced on paper turns into one nobody actually enjoyed living through.

**TripCompass** exists to catch this before you book anything — not with a vague "that sounds like a lot," but with an actual calculated ratio of how much of your trip's usable time transit is going to consume.

---

## 🧠 Overview

TripCompass is a Java-based AI agent, built on the [Embabel Agent Framework](https://github.com/embabel/embabel-agent) on top of Spring Boot and Spring AI, that takes a free-text description of a multi-city trip and produces:

- **realistic inter-city transit estimates** — computed from real average speeds and fixed overhead per transport mode, not a bare guess,
- a **deterministic feasibility verdict** — whether the plan's transit load is reasonable for the traveler's stated pace, backed by an actual computed ratio,
- a **day-by-day itinerary** that gets genuinely restructured (not just flagged) if the original plan wasn't realistic,
- a **budget allocated by category** with the dollar amounts computed by formula, and destination-aware guidance layered on top, and
- a **packing list that spans every destination's climate**, not just one city's weather.

**The design principle behind the numbers:** TripCompass treats "how long does this transit leg actually take" and "is this overall plan realistic" as calculations with correct answers, not vibes. Average speed plus fixed overhead (airport processes, station transfers) gives a real time estimate per leg; the ratio of total transit time to total usable trip time, checked against a pace-specific tolerance, gives a real feasibility verdict. Both are computed in plain Java. The model's job is everything that genuinely requires judgment: which activities fit each city, how to restructure a plan that doesn't work, how a specific city's cost of living should shape a budget category.

**Who this is for:**
- Travelers planning a multi-city trip who want to know if their plan is realistic before booking flights and hotels
- Developers exploring Embabel with an example that separates real arithmetic from genuine planning judgment across a whole pipeline
- Anyone who's landed exhausted on day 3 of a trip and wondered where the time went

---

## ✨ Features

### 🚄 Realistic Transit Estimation
- **Mode-aware time calculation** — flight, train, car, and bus each use a real average-speed-plus-overhead formula (airport check-in and security for flights, station transfers for trains), not a flat guess
- **Full leg-by-leg breakdown** — every inter-city hop in the trip gets its own estimated duration, not just a total

### ✅ Deterministic Feasibility Checking
- **A real computed ratio, not an impression** — total transit hours against total usable trip hours, checked against a tolerance specific to the traveler's stated pace (relaxed/moderate/packed)
- **Domain-specific error handling** — a dedicated `TripDetailsIncompleteException` fires if no destinations could be identified at all, instead of silently reporting an empty trip as perfectly feasible

### 🗺️ Honest Itinerary Building
- **Restructuring, not just warnings** — if the feasibility check fails, the itinerary step is explicitly instructed to cut a city, combine nearby stops, or extend the trip — not present the original overpacked plan with a caveat attached
- **Transit-aware daily planning** — a day with a multi-hour transit leg gets a lighter sightseeing plan than a full day in one place

### 💰 Grounded Budgeting
- **Category amounts computed by formula** — a fixed percentage split by travel style (standard, backpacking, luxury), applied by real arithmetic to the stated total budget
- **Destination-aware guidance, never re-computed amounts** — the model adds context about which specific cities run expensive or cheap, without ever second-guessing the underlying dollar figures

### 🎒 Multi-Climate Packing
- **Spans the whole trip, not one city** — accounts for climate variation across every destination on the itinerary, flagging when layering makes sense for a trip that crosses climate zones

---

## 🧩 How It Works

TripCompass isn't a fixed script — it's a set of declarative **actions**, each stating what type it needs and what type it produces. Embabel's planner resolves the dependency graph and runs only what's needed to reach whichever goal is requested. Ask for just the packing list, and it never touches transit estimation or feasibility at all.

**The flow, in prose:**

1. **Extraction** — the trip's planned stops and the traveler's preferences (pace, budget, interests, travel style) are parsed independently from the same input text.
2. **Transit estimation** *(deterministic formula)* — for each consecutive pair of stops, the model estimates distance and likely transport mode from its geographic knowledge; a real Java tool converts that into a time estimate using average speed and fixed overhead per mode.
3. **Feasibility check** *(deterministic rule)* — a plain rule engine computes the ratio of total transit hours to total usable trip hours and checks it against a tolerance specific to the traveler's pace. This is a calculation with one correct answer, not a judgment call.
4. **Itinerary generation** *(Goal 1)* — if the plan failed the feasibility check, it gets genuinely restructured; otherwise the day-by-day plan is built as originally allocated, with transit legs explicitly factored into each day's pacing.
5. **Budget allocation** *(Goal 2)* — an independent, downstream deliverable. Category amounts are split by a fixed percentage table in Java; the model only adds destination-specific guidance on top.
6. **Packing list** *(Goal 3)* — another independent downstream deliverable, spanning every destination's likely climate rather than assuming one city's weather for the whole trip.

**Text-based pipeline map:**

```
UserInput (multi-city trip + preferences)
   │
   ├──► extractItineraryOutline ───────────► ItineraryOutline
   └──► extractTravelerPreferences ────────► TravelerPreferences
                    │
                    ▼
   estimateTransitTimes (deterministic formula) ──► TransitEstimates
                    │
                    ▼
         checkFeasibility (deterministic rule) ───► FeasibilityReport
                    │
                    ▼
              generateItinerary               🎯 GOAL  ──► DayByDayItinerary


   ItineraryOutline + TravelerPreferences
                    │
                    ├──► allocateBudget (BudgetPlannerAgent)      🎯 GOAL  ──► BudgetAllocation
                    └──► generatePackingList (PackingAdvisorAgent) 🎯 GOAL  ──► PackingList
```

Three focused agent classes contribute actions toward these goals:

| Agent | Responsibility |
|---|---|
| `TripPlanningAgent` | Owns the core spine — extraction, deterministic transit/feasibility calculation, and the final itinerary |
| `BudgetPlannerAgent` | Independent goal — budget split by formula, refined with destination context |
| `PackingAdvisorAgent` | Independent goal — a packing list spanning every destination's climate |

---

## 🗂️ Project Structure

```
tripcompass/
├── src/main/java/com/tripcompass/
│   ├── TripCompassApplication.java        # Spring Boot entry point
│   │
│   ├── agent/
│   │   ├── TripPlanningAgent.java         # Core: extraction → transit calc → feasibility → itinerary
│   │   ├── BudgetPlannerAgent.java        # Deterministic budget split + destination notes
│   │   └── PackingAdvisorAgent.java       # Multi-climate packing list
│   │
│   ├── config/
│   │   └── TripCompassProperties.java     # Max packing items per category
│   │
│   ├── exception/
│   │   └── TripDetailsIncompleteException.java
│   │
│   ├── model/
│   │   ├── DestinationStop.java
│   │   ├── ItineraryOutline.java
│   │   ├── TravelerPreferences.java
│   │   ├── TransitLeg.java
│   │   ├── TransitEstimates.java
│   │   ├── FeasibilityReport.java
│   │   ├── DayByDayItinerary.java
│   │   ├── BudgetCategoryAllocation.java
│   │   ├── BudgetAllocation.java
│   │   └── PackingList.java
│   │
│   ├── persona/
│   │   └── Personas.java                  # SEASONED_TRAVELER, BUDGET_ADVISOR
│   │
│   └── tool/
│       ├── TransitTimeEstimatorTool.java   # @LlmTool — real speed + overhead formula
│       ├── FeasibilityRuleTool.java        # Plain @Component — NOT exposed to the LLM
│       └── BudgetAllocatorTool.java        # Plain @Component — NOT exposed to the LLM
│
└── src/main/resources/
    └── application.yml                     # Spring, Embabel model, and TripCompass config
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
git clone https://github.com/drissiOmar98/tripcompass.git
cd tripcompass
```

**2. Set your environment variables**

```bash
export ANTHROPIC_API_KEY=your-anthropic-key
```

**3. Configure `application.yml`** (default shown below — adjust as needed)

```yaml
trip-compass:
  max-packing-items-per-category: 6   # items per packing list category

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

Once running, submit a free-text description of your trip and preferences, through however your Embabel setup exposes it (CLI prompt, Shell, or REST endpoint, depending on your platform configuration) — for example:

```
6-day trip: 2 days Paris, 1 day Brussels, 3 days Amsterdam. We love
museums and good food, moderate pace, total budget $2500, mid-range
comfort travel style.
```

**Sample `TransitEstimates` output:**

```json
{
  "legs": [
    { "fromCity": "Paris", "toCity": "Brussels", "distanceKm": 265, "mode": "TRAIN", "estimatedHours": 2.7 },
    { "fromCity": "Brussels", "toCity": "Amsterdam", "distanceKm": 210, "mode": "TRAIN", "estimatedHours": 2.3 }
  ],
  "totalTransitHours": 5.0
}
```

**Sample `FeasibilityReport` output:**

```json
{
  "isFeasible": false,
  "transitToUsableRatio": 0.104,
  "verdict": "At about 10% of usable trip time spent in transit, this plan is actually within a reasonable range for a moderate pace, but the single day in Brussels leaves very little real time there once the two train legs bracket it."
}
```

*(Note: the numeric ratio can pass the overall threshold while a specific single-day stop is still too short in practice — the itinerary step accounts for both.)*

**Sample `DayByDayItinerary` output** *(excerpt)*:

```json
{
  "dailyPlans": [
    "Day 1 (Paris): Arrive, settle in, evening walk near the Seine and a relaxed dinner.",
    "Day 2 (Paris): Louvre in the morning, free afternoon for a food market, evening at leisure.",
    "Day 3 (Paris -> Brussels): Morning train to Brussels (~2.7h) - light afternoon exploring the Grand Place and a waffle stop, given limited remaining daylight.",
    "Day 4 (Brussels -> Amsterdam): Morning train to Amsterdam (~2.3h) - afternoon at the Rijksmuseum, easing into Amsterdam.",
    "Day 5 (Amsterdam): Full day - Anne Frank House (book ahead) and a canal-side lunch.",
    "Day 6 (Amsterdam): Van Gogh Museum, free afternoon, departure prep."
  ],
  "wasAdjustedForFeasibility": false
}
```

**Sample `BudgetAllocation` output:**

```json
{
  "categories": [
    { "category": "Lodging", "amountUsd": 1000.0, "percentOfTotal": 40.0 },
    { "category": "Food", "amountUsd": 625.0, "percentOfTotal": 25.0 },
    { "category": "Transit", "amountUsd": 375.0, "percentOfTotal": 15.0 },
    { "category": "Activities", "amountUsd": 375.0, "percentOfTotal": 15.0 },
    { "category": "Miscellaneous", "amountUsd": 125.0, "percentOfTotal": 5.0 }
  ],
  "notes": "Amsterdam and Paris both run above-average on lodging - consider leaning toward the higher end of that allocation for those two stops, and the lower end for the single night in Brussels."
}
```

---

## 🗺️ Roadmap

- [ ] Real routing data (maps API or web search) instead of LLM-estimated distances feeding the transit formula
- [ ] Multi-currency budget support
- [ ] Seasonal weather awareness for the packing list, tied to actual travel dates
- [ ] Group trip support (differing paces/interests across travelers)
- [ ] REST API layer for external integrations

---

## 🤝 Contributing

Issues and pull requests are welcome. If you're proposing a new action or agent, please:

1. Keep it focused on a single responsibility (one action, one job)
2. Add Javadoc explaining what it consumes and produces
3. If a number has exactly one correct value under a known formula (a time estimate, a ratio, a budget split), compute it in plain Java — reserve the LLM for genuine planning judgment, following the pattern in `tool/`

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.

---

## 👤 Author

Built by **[Omar Drissi](https://github.com/drissiOmar98)** — feel free to open an issue, star the repo ⭐, or connect on GitHub.
