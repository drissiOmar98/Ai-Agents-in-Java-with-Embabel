# 🧭 CareerLens — AI Career Assistant

> A multi-agent AI system built in Java with [Embabel](https://github.com/embabel/embabel-agent) and Spring Boot. Give it a job posting and your background — it matches your skills against the role, tailors your resume highlights, scores your ATS keyword coverage, writes your cover letter, and preps you for the interview.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Embabel](https://img.shields.io/badge/Embabel-Agent%20Platform-7F77DD)](https://github.com/embabel/embabel-agent)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#-license)

---

## 📖 Overview

**CareerForge** is a Java-based AI agent that turns a job posting and a candidate's background into a complete, tailored application package. Paste in a job description alongside a summary of your experience, and CareerForge will:

- figure out exactly what the role requires,
- honestly assess how well you match it — gaps included,
- rewrite your strongest experience into resume bullets aimed at *this specific job*,
- score how well those bullets would survive an ATS keyword scan,
- write a cover letter that naturally works in what's currently missing, and
- generate the interview questions you're most likely to actually get asked.

It's built on the [Embabel Agent Framework](https://github.com/embabel/embabel-agent) on top of Spring Boot and Spring AI, and — like its sibling projects — is structured as a clean example of **goal-directed multi-agent composition** in Java: independent, single-responsibility actions that Embabel's planner assembles into whichever outcome is requested, rather than one script or one giant prompt trying to do everything at once.

**Who this is for:**
- Job seekers who want tailored application materials without starting from a blank page each time
- Developers exploring Embabel with a genuinely useful, non-trivial multi-goal example
- Anyone who wants a reference for combining deterministic tools (ATS scoring) with LLM judgment in one clean pipeline

**What CareerForge is *not*:** it doesn't invent experience you don't have. Every persona and prompt is explicitly instructed to work only with what you actually provide — the goal is *framing*, not fabrication.

---

## ✨ Features

### 🎯 Matching & Analysis
- **Job requirement extraction** — parses a raw posting into title, company, required skills, nice-to-have skills, and seniority level
- **Candidate profile extraction** — parses your background into skills, experience level, and a summary, independently from the same input
- **Skill match/gap analysis** — an honest 0–100 fit score, a list of what matches, and a list of what's genuinely missing — no inflated scores

### 📝 Resume & Cover Letter
- **Tailored resume highlights** — up to N achievement-oriented bullet points, generated to emphasize your strongest matches for *this* role, never inventing accomplishments you didn't mention
- **ATS-aware cover letter** — written with visibility into your current keyword gaps, and instructed to work them in only where they honestly reflect real experience

### 🛡️ Quality & Scoring
- **Deterministic ATS keyword scoring** — a real case-insensitive keyword match calculation (not an LLM guess) reports your match percentage and exactly which required keywords are still missing
- **Domain-specific error handling** — a dedicated `CandidateProfileIncompleteException` fires if no skills could be extracted at all, instead of quietly producing garbage downstream

### 🎤 Interview Preparation
- **Likely technical questions** — generated from the job's required skills, weighted toward the areas where your profile shows gaps (the questions you're actually most likely to get)
- **Likely behavioral questions** — tailored to the role's seniority level
- **Targeted prep tips** — specific, actionable advice for shoring up your weakest areas before the interview

---

## 🧠 How It Works

CareerForge isn't a fixed, linear script — it's a set of declarative **actions**, each stating what type it needs and what type it produces. Embabel's planner resolves the dependency graph and runs only the actions required to reach whichever goal is requested. Ask for just interview prep, and the resume/cover-letter branch never runs at all.

**The flow, in prose:**

1. **Extraction** — the job posting and the candidate's background are parsed independently from the same input text: one action reads out the job's requirements, another reads out the candidate's profile.
2. **Matching** — the two are compared to produce an honest fit analysis: what matches, what's missing, and why.
3. **Resume tailoring** — the candidate's real experience is rewritten into bullet points that lead with the strongest matches for this specific role.
4. **ATS scoring** *(side check)* — a deterministic tool counts how many of the job's required keywords actually appear in those bullets, and lists what's still missing.
5. **Cover letter** *(Goal 1)* — written with full visibility into the ATS gap report, so it can naturally close keyword gaps without fabricating experience.
6. **Interview prep** *(Goal 2)* — generated independently from the job requirements and the match analysis, weighted toward the candidate's actual gaps.

**Text-based pipeline map:**

```
UserInput (job posting + candidate background)
   │
   ├──► extractJobRequirements ──────► JobRequirements
   └──► extractCandidateProfile ─────► CandidateProfile
                    │
                    ▼
         matchCandidateProfile ───────► CandidateMatch
                    │
                    ▼
         tailorResumeHighlights ──────► ResumeHighlights
                    │
                    ▼
   scoreAtsMatch (ApplicationQualityAgent) ──► AtsScoreReport
                    │
                    ▼
              writeCoverLetter            🎯 GOAL  ──► CoverLetter


         JobRequirements + CandidateMatch
                    │
                    ▼
   generateInterviewQuestions (InterviewPrepAgent)  🎯 GOAL  ──► InterviewPrep
```

Three focused agent classes contribute actions toward these goals:

| Agent | Responsibility |
|---|---|
| `ApplicationAgent` | Owns the core spine — extraction → matching → resume tailoring → cover letter |
| `ApplicationQualityAgent` | Independent ATS keyword scoring, fed back into the cover letter step |
| `InterviewPrepAgent` | Downstream interview question generation, reachable on its own |

---

## 🗂️ Project Structure

```
careerforge/
├── src/main/java/com/careerforge/
│   ├── CareerForgeApplication.java        # Spring Boot entry point
│   │
│   ├── agent/
│   │   ├── ApplicationAgent.java          # Core: extraction → matching → resume → cover letter
│   │   ├── ApplicationQualityAgent.java   # ATS keyword scoring
│   │   └── InterviewPrepAgent.java        # Interview question generation
│   │
│   ├── config/
│   │   └── CareerForgeProperties.java     # Max bullets, max questions, ATS target %
│   │
│   ├── exception/
│   │   └── CandidateProfileIncompleteException.java
│   │
│   ├── model/
│   │   ├── JobRequirements.java
│   │   ├── CandidateProfile.java
│   │   ├── CandidateMatch.java
│   │   ├── ResumeHighlights.java
│   │   ├── AtsScoreReport.java
│   │   ├── CoverLetter.java
│   │   └── InterviewPrep.java
│   │
│   ├── persona/
│   │   └── Personas.java                  # CAREER_COACH, HIRING_MANAGER
│   │
│   └── tool/
│       └── AtsKeywordScoreTool.java        # Deterministic keyword-overlap calculator
│
└── src/main/resources/
    └── application.yml                     # Spring, Embabel model, and CareerForge config
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
git clone https://github.com/drissiOmar98/careerforge.git
cd careerforge
```

**2. Set your environment variables**

```bash
export ANTHROPIC_API_KEY=your-anthropic-key
```

**3. Configure `application.yml`** (defaults shown below — adjust as needed)

```yaml
career-forge:
  max-resume-bullets: 5          # tailored resume bullets generated
  max-interview-questions: 6     # questions generated per category (technical/behavioral)
  target-ats-match-percent: 70   # "good" ATS coverage threshold

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

Once running, submit a single free-text input containing **both** the job posting and your background, through however your Embabel setup exposes it (CLI prompt, Shell, or REST endpoint, depending on your platform configuration) — for example:

```
Job Posting:
Senior Backend Engineer at Acme Corp. Must have 5+ years with Java and
Spring Boot, strong experience with Kafka and event-driven systems,
PostgreSQL. Nice to have: Kubernetes, GraphQL.

My Background:
I'm a backend engineer with 4 years of experience building microservices
in Java and Spring Boot. I've built event-driven systems using Kafka and
worked extensively with PostgreSQL. I haven't used Kubernetes or GraphQL
professionally but have experimented with both.
```

**Sample `CandidateMatch` output:**

```json
{
  "matchingSkills": ["Java", "Spring Boot", "Kafka", "PostgreSQL", "event-driven systems"],
  "missingSkills": ["Kubernetes (professional experience)", "GraphQL (professional experience)"],
  "fitScorePercent": 78,
  "assessment": "Strong technical alignment on the core required stack, with 4 years of experience just under the stated 5+ year threshold. Kubernetes and GraphQL are nice-to-haves the candidate has only experimented with, not blocking gaps."
}
```

**Sample `AtsScoreReport` output:**

```json
{
  "matchPercent": 80,
  "missingKeywords": ["Kubernetes"]
}
```

**Sample `CoverLetter` output** *(excerpt)*:

```
Dear Hiring Team,

In four years building backend systems in Java and Spring Boot, I've
spent most of that time deep in event-driven architecture — designing
and running Kafka-based services backed by PostgreSQL in production.
That's squarely the stack this role is built on...
```

**Sample `InterviewPrep` output** *(excerpt)*:

```json
{
  "technicalQuestions": [
    "Walk me through how you'd design a Kafka consumer group for exactly-once processing.",
    "How would you approach containerizing a Spring Boot service you'd previously only run on VMs?"
  ],
  "behavioralQuestions": [
    "Tell me about a time you had to debug a production issue in a distributed system."
  ],
  "prepTips": "Since Kubernetes is a stated gap, be ready to speak concretely about your hands-on experimentation with it rather than avoiding the topic — hiring managers respond well to honest 'here's what I've explored and how I'd ramp up' answers."
}
```

---

## 🗺️ Roadmap

- [ ] LinkedIn profile summary generation alongside the resume/cover letter
- [ ] Multi-posting comparison (rank several job postings against one profile)
- [ ] Follow-up email drafting for post-interview thank-you notes
- [ ] REST API layer for external integrations
- [ ] Export tailored resume highlights directly into a `.docx` template

---

## 🤝 Contributing

Issues and pull requests are welcome. If you're proposing a new action or agent, please:

1. Keep it focused on a single responsibility (one action, one job)
2. Add Javadoc explaining what it consumes and produces
3. Never let a prompt fabricate candidate experience — every generation step should stay grounded in what the user actually provided

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.

---

## 👤 Author

Built by **[Omar Drissi](https://github.com/drissiOmar98)** — feel free to open an issue, star the repo ⭐, or connect on GitHub.
