# 🎬 CineScout — AI Movie Research Agent

> A multi-action AI agent built in Java with [Embabel](https://github.com/embabel/embabel-agent) and Spring Boot. Give it a movie name — it identifies it, casts it, classifies it, summarizes it (spoiler-free), and recommends what to watch next.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Embabel](https://img.shields.io/badge/Embabel-Agent%20Platform-7F77DD)](https://github.com/embabel/embabel-agent)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#-license)

---

## 📖 Overview

**CineScout** is a Java-based AI agent that turns a single free-text request — *"Tell me about Inception"* — into a complete, structured movie profile: title, release date, director, principal cast, genres, and a spoiler-free plot summary. From there, it can go a step further and recommend similar movies based on theme, tone, genre, and director style.

It's built on the [Embabel Agent Framework](https://github.com/embabel/embabel-agent) on top of Spring Boot and Spring AI, and is designed as a clean example of **goal-directed agent composition** in Java: instead of one prompt trying to do everything, each fact (cast, genre, plot, director) is resolved by its own independent action, and Embabel's planner figures out the execution order needed to reach the requested goal.

**Who this is for:**
- Developers exploring the Embabel agent framework with a small, focused, real example
- Anyone who wants a working reference for structuring a multi-action Java agent cleanly
- Movie recommendation / metadata tooling as a starting point for something bigger

---

## ✨ Features

### 🎞️ Movie Profile Resolution
- **Identity extraction** — parses a free-text request into a confirmed title and release date, used to disambiguate every subsequent lookup
- **Cast lookup** — returns the principal cast, most notable first, capped at a configurable size
- **Genre classification** — identifies primary and secondary genres
- **Spoiler-free plot summary** — a 2-3 sentence synopsis of the premise only, written in a film critic's voice, with an explicit instruction to stop before twists or the ending
- **Director lookup** — resolved against the *confirmed* title (not the raw user input) to reduce ambiguity between similarly named films

### 🍿 Recommendations
- **Similar-movie suggestions** — recommends other titles based on the completed profile's genre, director, and tone, with a short rationale explaining the connection — excludes the movie itself and its direct sequels/prequels

### 🛡️ Reliability
- **Domain-specific error handling** — a dedicated `MovieDataUnavailableException` is thrown when a required lookup (e.g. director) comes back empty, instead of a generic assertion failure with no context
- **Configurable output size** — cast list length and recommendation count are both externalized to `application.yml`, not hardcoded

---

## 🧠 How It Works

CineScout is not a linear script — it's a set of declarative **actions**, each stating what type it needs and what type it produces. Embabel's planner resolves the dependency graph and runs the actions in the order required to reach whichever goal is requested. That's what makes this an *agent* rather than a fixed pipeline: the same building blocks can be recombined toward either goal below without any hand-wired control flow.

**The flow, in prose:**

1. **Basic info** — the user's request is parsed into a confirmed movie title and release date. This is the anchor fact everything else either reads from directly or depends on.
2. **Cast, genres, and plot** — three independent lookups run against the original request in parallel from the planner's perspective: principal cast, genre classification, and a spoiler-free synopsis.
3. **Director & assembly** *(Goal 1)* — the director is looked up against the *confirmed* title rather than the raw request, then combined with every other resolved fact into a complete `Movie` profile.
4. **Recommendations** *(Goal 2)* — once a `Movie` profile exists, similar titles can be recommended based on its genre, director, and tone.

**Text-based pipeline map:**

```
UserInput
   │
   ├──► getMovieBasicInfo ─────► MovieBasicInfo
   ├──► getMovieActors ────────► MovieActors
   ├──► getMovieGenres ────────► MovieGenres
   └──► getPlotSummary ────────► MoviePlotSummary
                                       │
                                       ▼
                    getMovieInfo (+ director lookup)   🎯 GOAL
                                       │
                                       ▼
                                    Movie
                                       │
                                       ▼
                          getSimilarMovies              🎯 GOAL
                                       │
                                       ▼
                          MovieRecommendations
```

A caller can request just the profile, just the recommendations (which transparently pulls in the profile as a prerequisite), or both — Embabel resolves whichever path the requested goal needs.

---

## 🗂️ Project Structure

```
cinescout/
├── src/main/java/com/cinescout/
│   ├── CineScoutApplication.java          # Spring Boot entry point
│   │
│   ├── agent/
│   │   └── MovieInfoAgent.java            # All actions: lookups, assembly, recommendations
│   │
│   ├── config/
│   │   └── CineScoutProperties.java       # @ConfigurationProperties (max actors, max recommendations)
│   │
│   ├── exception/
│   │   └── MovieDataUnavailableException.java  # Thrown when a required lookup returns nothing
│   │
│   ├── model/
│   │   ├── Movie.java                     # Final assembled profile
│   │   ├── MovieBasicInfo.java
│   │   ├── MovieActors.java
│   │   ├── MovieDirector.java
│   │   ├── MovieGenres.java
│   │   ├── MoviePlotSummary.java
│   │   └── MovieRecommendations.java
│   │
│   └── persona/
│       └── Personas.java                  # FILM_CRITIC prompt contributor
│
└── src/main/resources/
    └── application.yml                     # Spring, Embabel model, and CineScout config
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
git clone https://github.com/drissiOmar98/cinescout.git
cd cinescout
```

**2. Set your environment variables**

```bash
export ANTHROPIC_API_KEY=your-anthropic-key
```

**3. Configure `application.yml`** (defaults shown below — adjust as needed)

```yaml
cinescout:
  max-actors: 5           # cast members returned per movie
  max-similar-movies: 3   # recommendations generated per movie

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

Once running, invoke the agent with a free-text movie request through however your Embabel setup exposes it (CLI prompt, Shell, or REST endpoint, depending on your platform configuration) — for example:

```
"Tell me about Inception"
```

**Sample `Movie` output:**

```json
{
  "name": "Inception",
  "releaseDate": "2010-07-16",
  "director": "Christopher Nolan",
  "actors": [
    "Leonardo DiCaprio",
    "Joseph Gordon-Levitt",
    "Elliot Page",
    "Tom Hardy",
    "Marion Cotillard"
  ],
  "genres": ["Sci-Fi", "Thriller", "Heist"],
  "plotSummary": "A skilled thief who steals secrets from people's subconscious minds through dream-sharing technology is offered a chance at redemption: implant an idea instead of stealing one. To pull off this 'inception,' he must assemble a team capable of navigating dreams within dreams."
}
```

**Sample `MovieRecommendations` output** (once the second goal is reached):

```json
{
  "similarMovies": ["Shutter Island", "The Prestige", "Tenet"],
  "reason": "All share Christopher Nolan's fascination with fractured perception and reality, paired with tightly constructed, puzzle-box plotting."
}
```

---

## 🗺️ Roadmap

- [ ] Streaming availability lookup (where to watch, by region)
- [ ] Rating aggregation (critic and audience scores)
- [ ] REST API layer for external integrations
- [ ] Franchise/series awareness (sequels, shared universes)
- [ ] Caching layer to avoid re-resolving well-known movies

---

## 🤝 Contributing

Issues and pull requests are welcome. If you're proposing a new lookup or action, please:

1. Keep it focused on a single responsibility (one action, one fact)
2. Add Javadoc explaining what it consumes and produces
3. Wire its output into `getMovieInfo` or a new goal, rather than duplicating existing logic

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.

---

## 👤 Author

Built by **[Omar Drissi](https://github.com/drissiOmar98)** — feel free to open an issue, star the repo ⭐, or connect on GitHub.
