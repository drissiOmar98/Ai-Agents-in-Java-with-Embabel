# 🎪 FestivalScout — AI Music Festival Planning Agent

> A multi-agent AI system built in Java with [Embabel](https://github.com/embabel/embabel-agent) and Spring Boot. Tell it which festival you're going to — Tomorrowland, Ultra, or anything else — and it researches the real lineup, builds you a personalized set schedule with conflicts checked, packs your bag, estimates your budget, and keeps you safe.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Embabel](https://img.shields.io/badge/Embabel-Agent%20Platform-7F77DD)](https://github.com/embabel/embabel-agent)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#-license)

---

## 📖 Overview

**FestivalScout** is a Java-based AI agent that turns a single free-text request — *"I'm going to Tomorrowland, I love techno and melodic house, traveling from Casablanca on a mid-range budget"* — into a complete festival plan:

- the **actual current lineup**, researched via live web search (not stale training data),
- a **personalized must-see schedule** matched to your taste,
- every pick **checked for time conflicts** before it reaches you,
- a **day-by-day itinerary** with meals and rest breaks built in,
- a **tailored packing list** for the festival's climate and format,
- a **realistic budget estimate**, itemized and summed with real arithmetic,
- and **practical safety guidance** for the specific type of event you're attending.

It's built on the [Embabel Agent Framework](https://github.com/embabel/embabel-agent) on top of Spring Boot and Spring AI, and follows the same **goal-directed multi-agent architecture** as its sibling projects: independent, single-responsibility actions that Embabel's planner assembles into whichever outcome is requested, rather than one script trying to do everything.

**Who this is for:**
- Festival-goers who want a real plan instead of a vague "check the app when you get there"
- Developers exploring Embabel with an example that mixes live web research, deterministic tools, and LLM judgment in one pipeline
- Anyone curious how to keep an agent's output grounded in *this year's* facts rather than whatever the model happened to memorize during training

---

## ✨ Features

### 🎧 Lineup Research & Personalization
- **Live lineup research** — web search grounds the festival's headliners, rising artists, and genre breakdown in current information, since lineups change every edition and stale training data would recommend artists who aren't even playing
- **Personalized must-see picks** — matches the attendee's favorite genres and named artists against the real lineup, mixing marquee headliners with smaller acts genuinely suited to their taste
- **Day-by-day itinerary** — assembles the final schedule with sensible meal and rest breaks, not just a bare list of set times

### 🛡️ Quality & Reliability
- **Deterministic schedule-conflict detection** — real time-range math (not an LLM's eyeballing) flags any two picks that overlap on the same day, *before* the final itinerary is built
- **Domain-specific error handling** — a dedicated `LineupUnavailableException` fires if lineup research comes back empty, instead of silently generating picks against nothing
- **Real arithmetic for budgeting** — line-item costs are summed by actual code, not an LLM doing mental math

### 🎒 Logistics & Preparation
- **Tailored packing lists** — split into essentials, weather/season-specific items, and comfort items experienced attendees know to bring but first-timers forget
- **Budget estimation** — itemized cost breakdown (ticket, travel, lodging/camping, food, misc) scaled to the attendee's stated budget level
- **Safety & wellbeing guidance** — practical tips specific to the event's format (heat/hydration for outdoor camping festivals, crowd navigation for dense downtown venues) plus emergency/separation guidance

---

## 🧠 How It Works

FestivalScout isn't a fixed script — it's a set of declarative **actions**, each stating what type it needs and what type it produces. Embabel's planner resolves the dependency graph and runs only what's needed to reach whichever goal is requested. Ask for just a packing list, and the lineup research, schedule matching, and conflict-checking branch never runs.

**The flow, in prose:**

1. **Extraction** — the festival's identity (name, location, dates, format) and the attendee's preferences (genres, favorite artists, budget, departure city) are extracted independently from the same free-text request.
2. **Lineup research** — the festival's actual lineup is researched via live web search, since a hardcoded or memorized lineup would go stale the moment a new edition is announced.
3. **Personalized picks** — the researched lineup is matched against the attendee's taste to produce a prioritized must-see list, each with a day, stage, and estimated time slot.
4. **Conflict check** *(side gate)* — a deterministic tool compares every pick's time range against every other pick on the same day and flags overlaps — you can't be at two stages at once.
5. **Itinerary** *(Goal 1)* — the final day-by-day plan is built, explicitly resolving any flagged conflicts rather than leaving a double-booking in the output.
6. **Packing, budget, and safety** *(Goals 2, 3, 4)* — three independent downstream goals, each reachable directly from the festival's basic info and preferences, without needing the lineup or itinerary to exist first.

**Text-based pipeline map:**

```
UserInput (festival + preferences)
   │
   ├──► extractFestivalInfo ────────────► FestivalBasicInfo
   └──► extractAttendeePreferences ─────► AttendeePreferences
                    │
                    ▼
         researchLineup (web search) ────► LineupHighlights
                    │
                    ▼
         pickMustSeeArtists ─────────────► MustSeePicks
                    │
                    ▼
   checkScheduleConflicts (ScheduleQualityAgent) ──► ScheduleConflictReport
                    │
                    ▼
              buildItinerary              🎯 GOAL  ──► FestivalItinerary


         FestivalBasicInfo + AttendeePreferences
                    │
                    ├──► generatePackingList (LogisticsAgent)  🎯 GOAL  ──► PackingList
                    ├──► estimateBudget (LogisticsAgent)       🎯 GOAL  ──► BudgetEstimate
                    └──► generateSafetyTips (LogisticsAgent)   🎯 GOAL  ──► SafetyTips
```

Three focused agent classes contribute actions toward these goals:

| Agent | Responsibility |
|---|---|
| `FestivalPlannerAgent` | Owns the core spine — extraction → lineup research → personalized picks → itinerary |
| `ScheduleQualityAgent` | Independent, deterministic time-conflict check, fed back into the itinerary step |
| `LogisticsAgent` | Three independent downstream goals — packing list, budget estimate, safety guidance |

---

## 🗂️ Project Structure

```
festivalscout/
├── src/main/java/com/festivalscout/
│   ├── FestivalScoutApplication.java      # Spring Boot entry point
│   │
│   ├── agent/
│   │   ├── FestivalPlannerAgent.java      # Core: extraction → lineup → picks → itinerary
│   │   ├── ScheduleQualityAgent.java      # Deterministic schedule conflict check
│   │   └── LogisticsAgent.java            # Packing list, budget, safety tips
│   │
│   ├── config/
│   │   └── FestivalScoutProperties.java   # Max artists, max packing items per category
│   │
│   ├── exception/
│   │   └── LineupUnavailableException.java
│   │
│   ├── model/
│   │   ├── FestivalBasicInfo.java
│   │   ├── AttendeePreferences.java
│   │   ├── LineupHighlights.java
│   │   ├── ArtistSetPick.java
│   │   ├── MustSeePicks.java
│   │   ├── ScheduleConflictReport.java
│   │   ├── FestivalItinerary.java
│   │   ├── PackingList.java
│   │   ├── BudgetEstimate.java
│   │   └── SafetyTips.java
│   │
│   ├── persona/
│   │   └── Personas.java                  # MUSIC_CURATOR, FESTIVAL_VETERAN
│   │
│   └── tool/
│       ├── ScheduleConflictCheckerTool.java  # Real time-range overlap math
│       └── BudgetCalculatorTool.java          # Real line-item summation
│
└── src/main/resources/
    └── application.yml                     # Spring, Embabel model, and FestivalScout config
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Application framework | Spring Boot 3.x |
| Agent framework | [Embabel](https://github.com/embabel/embabel-agent) |
| LLM integration | Spring AI |
| Default model | Claude Sonnet |
| Web research | Embabel's built-in web tool group |
| Build tool | Maven |

---

## ✅ Prerequisites

- **Java 21+**
- **Maven 3.9+**
- An **Anthropic** (or other Spring AI-supported provider) API key, matching whichever model you configure under `embabel.models.default-llm`
- Web search access configured for Embabel's tool group (see your Embabel setup for provider-specific configuration)

---

## 🚀 Getting Started

**1. Clone the repository**

```bash
git clone https://github.com/drissiOmar98/festivalscout.git
cd festivalscout
```

**2. Set your environment variables**

```bash
export ANTHROPIC_API_KEY=your-anthropic-key
```

**3. Configure `application.yml`** (defaults shown below — adjust as needed)

```yaml
festival-scout:
  max-must-see-artists: 8             # prioritized artist picks generated
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

Once running, submit a free-text request describing the festival and your preferences, through however your Embabel setup exposes it (CLI prompt, Shell, or REST endpoint, depending on your platform configuration) — for example:

```
"I'm going to Tomorrowland this year. I love techno and melodic house,
I'd really like to catch Charlotte de Witte if she's playing. Traveling
from Casablanca, mid-range budget."
```

**Sample `MustSeePicks` output** *(excerpt)*:

```json
{
  "picks": [
    {
      "artist": "Charlotte de Witte",
      "day": "Day 2",
      "stage": "The Core",
      "startTime": "23:00",
      "endTime": "00:30",
      "reason": "Explicitly requested by the attendee and a perfect match for their techno preference."
    },
    {
      "artist": "Amelie Lens",
      "day": "Day 1",
      "stage": "Freedom Stage",
      "startTime": "22:00",
      "endTime": "23:30",
      "reason": "A driving techno headliner closely aligned with the attendee's genre taste."
    }
  ]
}
```

**Sample `ScheduleConflictReport` output:**

```json
{
  "conflicts": [],
  "hasConflicts": false
}
```

**Sample `BudgetEstimate` output:**

```json
{
  "estimatedTotalUsd": 1170,
  "lineItems": [
    "Ticket: $400",
    "Flights: $650",
    "Camping gear: $120"
  ]
}
```

**Sample `SafetyTips` output** *(excerpt)*:

```json
{
  "tips": [
    "Refill a reusable water bottle at marked hydration stations throughout the day, not just when you feel thirsty.",
    "Agree on a specific meeting point with your group before each day starts, in case phones die or signal drops."
  ],
  "emergencyAdvice": "Head to the nearest marked medical or info tent — festival staff are trained to help and can coordinate with security if you're separated from your group."
}
```

---

## 🗺️ Roadmap

- [ ] Overnight set handling in the conflict checker (sets crossing midnight)
- [ ] Multi-attendee group planning (shared itinerary across a group's differing tastes)
- [ ] Weather forecast integration for packing list refinement closer to the event date
- [ ] REST API layer for external integrations
- [ ] Calendar export (.ics) for the final itinerary

---

## 🤝 Contributing

Issues and pull requests are welcome. If you're proposing a new action or agent, please:

1. Keep it focused on a single responsibility (one action, one job)
2. Add Javadoc explaining what it consumes and produces
3. Ground anything with a "real number" (times, costs, scores) in an actual deterministic tool rather than an LLM estimate, following the pattern in `tool/`

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.

---

## 👤 Author

Built by **[Omar Drissi](https://github.com/drissiOmar98)** — feel free to open an issue, star the repo ⭐, or connect on GitHub.
