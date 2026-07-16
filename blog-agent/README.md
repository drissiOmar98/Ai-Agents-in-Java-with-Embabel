# ✍️ Blog Agent — Multi-Agent AI Blog Writer

> An autonomous, multi-agent content pipeline built in Java with [Embabel](https://github.com/embabel/embabel-agent) and Spring Boot. Give it a topic — it researches it, plans it, writes it, fact-checks it, scores it, illustrates it, publishes it, and promotes it.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Embabel](https://img.shields.io/badge/Embabel-Agent%20Platform-7F77DD)](https://github.com/embabel/embabel-agent)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#-license)

---

## 📖 Overview

**Blog Agent** is a Java-based AI agent application that turns a single topic string into a fully polished, publish-ready Markdown blog post — complete with front matter, a hero image prompt, a readability score, and social media promotion copy.

It's built as a demonstration (and a genuinely usable tool) of what a **multi-agent, goal-directed architecture** looks like in Java, using the [Embabel Agent Framework](https://github.com/embabel/embabel-agent) on top of Spring Boot and Spring AI — as opposed to a single giant prompt or a rigid linear script.

Instead of one model call doing everything, the work is broken into focused, single-responsibility steps — research, outline, title, hook, draft, fact-check, review, readability, front matter, thumbnail, and promotion — each handled by its own action, each independently tunable, testable, and swappable.

**Who this is for:**
- Developers who want to see a real, non-trivial Embabel multi-agent system
- Technical writers/DevRel folks who want an actual working content pipeline
- Anyone curious how to structure an LLM-powered Spring Boot application that doesn't collapse into a 500-line prompt-stuffing mess

---

## ✨ Features

### 📝 Content Creation
- **Structured outlining** — commits to a specific angle and section structure *before* any prose is written, so the draft doesn't wander
- **Catchy title generation** — generates 5 distinct candidate titles (direct, curiosity-driven, number-based, problem/solution) and picks the strongest one, with a rationale
- **Hook writing** — the opening paragraph is written as its own focused task, not left to chance inside a long drafting prompt
- **Draft generation** — a beginner-friendly first draft that continues naturally from the pre-written hook and follows the outline's sections

### 🔍 SEO & Discovery
- **Front matter generation** — description, tags, and SEO keywords (count configurable), plus an accurate word count and read time
- **Social media copy** — a punchy Twitter/X post and a longer, professional LinkedIn post generated from the *final published* article

### ✅ Quality & Polish
- **Automated fact-checking** — extracts the technical/factual claims in the draft, verifies each one against live web search, and returns a `VERIFIED` / `UNVERIFIED` / `INCORRECT` verdict per claim
- **Fact-check-aware review** — the review step is handed the fact-check findings directly, so anything flagged gets *corrected*, not just noted
- **Readability scoring** — a deterministic Flesch Reading Ease score (real syllable-counting math, not an LLM guess) paired with concrete, targeted simplification suggestions

### 🚀 Publishing Pipeline
- **Outline-first drafting** — structure is locked in before prose, avoiding unfocused, rambling first drafts
- **Thumbnail prompt generation** — a detailed, ready-to-paste image-generation prompt (composition, style, palette) plus accessibility alt text for the post's hero image
- **Automatic file publishing** — the finished Markdown file, front matter and all, is written straight to disk with a URL-safe slug filename

---

## 🧠 How It Works

Blog Agent is not one script running top-to-bottom — it's a set of declarative **actions**, each stating what type it needs and what type it produces. Embabel's planner resolves the dependency graph and executes the actions in the right order to reach the requested goal. That's what makes it an *agent* rather than a pipeline script: add, remove, or reorder capabilities without hand-wiring control flow.

**The flow, in prose:**

1. **Research** — the topic is researched with capped web search (max 3 queries, to stay polite to rate limits), producing a concise summary.
2. **Plan** — the research is turned into a specific angle and an ordered list of sections. No prose yet — just structure.
3. **Title & hook** — five title candidates are generated and the strongest is selected with a rationale; a focused opening paragraph is written against that title and angle.
4. **Draft** — the full first draft is written, continuing from the hook and following the outline's sections, in plain, beginner-friendly Markdown.
5. **Fact-check** — the draft's technical claims are extracted and verified against web sources independently of the writing step.
6. **Review** — a separate reviewer-role model tightens the prose *and* fixes anything the fact-checker flagged.
7. **Score & summarize** — a TLDR is generated and prepended; a deterministic readability score is computed and paired with simplification suggestions.
8. **Illustrate & publish** — a hero image prompt and alt text are generated, front matter is assembled (title, slug, date, description, tags, keywords, read time, reading level, hero image), and the final Markdown file is written to disk.
9. **Promote** — once published, Twitter/X and LinkedIn posts are generated to help get the article in front of readers.

**Text-based pipeline map:**

```
UserInput
   │
   ▼
researchTopic ──────────────────► ResearchedTopic
   │
   ▼
createOutline ──────────────────► Outline
   │
   ▼
generateTitleOptions ───────────► TitleOptions
   │
   ▼
writeHook ──────────────────────► Hook
   │
   ▼
writeDraft ─────────────────────► DraftPost
   │
   ├──► factCheckDraft (ContentQualityAgent) ──► FactCheckReport ──┐
   │                                                                ▼
   └──────────────────────────────────────────────────────► reviewDraft ──► ReviewedPost
                                                                       │
                                        scoreReadability (ContentQualityAgent) ──► ReadabilityReport
                                                                       │
                                                                       ▼
                                                                   addTldr ──► FinalPost
                                                                       │
                                  generateThumbnailPrompt (PromotionAgent) ──► ThumbnailPrompt
                                                                       │
                                                                       ▼
                                                            addFrontMatter  🎯 GOAL
                                                                       │
                                                                       ▼
                                                                PublishedPost
                                                                       │
                                       generateSocialPosts (PromotionAgent) ──► SocialPosts  🎯 GOAL
```

Three focused agent classes contribute actions toward these goals:

| Agent | Responsibility |
|---|---|
| `BlogWriterAgent` | Owns the core authoring spine — research → outline → title → hook → draft → review → TLDR → front matter/publish |
| `ContentQualityAgent` | Independent quality gates — fact-checking and readability scoring, fed back into the core spine |
| `PromotionAgent` | Downstream distribution — hero image prompt and social media copy |

---

## 🗂️ Project Structure

```
blog-agent/
├── src/main/java/com/danvega/blogagent/
│   ├── BlogAgentApplication.java        # Spring Boot entry point
│   │
│   ├── agent/
│   │   ├── BlogWriterAgent.java         # Core pipeline: research → draft → publish
│   │   ├── ContentQualityAgent.java     # Fact-checking & readability scoring
│   │   └── PromotionAgent.java          # Thumbnail prompt & social copy
│   │
│   ├── config/
│   │   └── BlogAgentProperties.java     # @ConfigurationProperties (output dir, keyword count)
│   │
│   ├── model/
│   │   ├── BlogPost.java                # Sealed interface — the post lifecycle
│   │   ├── ResearchedTopic.java
│   │   ├── Outline.java
│   │   ├── TitleOptions.java
│   │   ├── Hook.java
│   │   ├── DraftPost.java
│   │   ├── FactCheckFinding.java
│   │   ├── FactCheckReport.java
│   │   ├── ReviewedPost.java
│   │   ├── ReadabilityReport.java
│   │   ├── FinalPost.java
│   │   ├── ThumbnailPrompt.java
│   │   ├── FrontMatter.java
│   │   ├── PublishedPost.java
│   │   └── SocialPosts.java
│   │
│   ├── persona/
│   │   └── Personas.java                # Reusable RoleGoalBackstory prompt contributors
│   │
│   ├── tool/
│   │   ├── ReadingStatsTool.java         # Word count / read time (@LlmTool)
│   │   └── ReadabilityTool.java          # Flesch Reading Ease score (@LlmTool)
│   │
│   └── util/
│       └── Slugs.java                    # Title → URL/filename-safe slug
│
└── src/main/resources/
    └── application.yml                   # Spring, MCP, and Embabel model config
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Application framework | Spring Boot 3.x |
| Agent framework | [Embabel](https://github.com/embabel/embabel-agent) |
| LLM integration | Spring AI |
| Models | Claude Sonnet (default), Claude Opus (reviewer role) |
| Web research | Brave Search via MCP (stdio connector) |
| Build tool | Maven |

---

## ✅ Prerequisites

- **Java 21+**
- **Maven 3.9+**
- **Node.js + npx** — required to run the Brave Search MCP server (`@modelcontextprotocol/server-brave-search`)
- An **Anthropic** and/or **OpenAI** API key, depending on which models you configure
- A **Brave Search API key** ([brave.com/search/api](https://brave.com/search/api/))

---

## 🚀 Getting Started

**1. Clone the repository**

```bash
git clone https://github.com/drissiOmar98/blog-agent.git
cd blog-agent
```

**2. Set your environment variables**

```bash
export OPENAI_API_KEY=your-openai-key
export ANTHROPIC_API_KEY=your-anthropic-key
export BRAVE_API_KEY=your-brave-search-key
```

**3. Configure `application.yml`** (defaults shown below — adjust as needed)

```yaml
blog-agent:
  output-dir: blog-posts      # where published posts are written
  number-of-keywords: 5       # max SEO keywords per post

embabel:
  models:
    default-llm: claude-sonnet-4-6   # used for research, drafting, TLDR, front matter
    llms:
      reviewer: claude-opus-4-6      # used only for the review step
```

**4. Build and run**

```bash
./mvnw clean install
./mvnw spring-boot:run
```

---

## ▶️ Usage

Once running, invoke the agent with a topic through however your Embabel setup exposes it (CLI prompt, Shell, or REST endpoint, depending on your platform configuration) — for example:

```
"Why Java Virtual Threads change how you should think about thread pools"
```

**What happens next:**

1. The topic is researched, outlined, titled, and hooked
2. A full first draft is written
3. Technical claims are fact-checked against the web
4. The draft is reviewed and tightened
5. A TLDR and readability score are added
6. A hero image prompt is generated
7. The finished post is published to `blog-posts/` as a slugified `.md` file
8. Twitter/X and LinkedIn promotion copy is generated

**Sample output front matter:**

```yaml
---
title: "Why Java Virtual Threads Change How You Think About Thread Pools"
slug: why-java-virtual-threads-change-how-you-think-about-thread-pools
date: "2026-07-16T08:00:00.000Z"
published: true
description: "Virtual threads make thread-per-request scale again — here's what actually changes in how you design concurrent Java apps."
author: "Dan Vega"
readTime: "6 min read"
readingLevel: "Standard (Flesch 64)"
heroImagePrompt: "Flat vector illustration of thousands of lightweight thread icons flowing through a narrow pipe into a single CPU core, teal and purple palette, minimalist, no text"
heroImageAlt: "Illustration of many lightweight threads flowing efficiently through a CPU"
tags:
  - java
  - concurrency
  - spring-boot
keywords:
  - virtual threads
  - project loom
  - java 21
  - thread pools
  - jep 444
---
```

---

## 🗺️ Roadmap

- [ ] REST API layer for triggering the agent from external tools
- [ ] Pluggable publishing targets (Dev.to, Hashnode, static-site generators)
- [ ] Image generation integration to act directly on the thumbnail prompt
- [ ] Multi-topic batch runs with a shared editorial calendar
- [ ] Configurable persona packs for different blogs/audiences

---

## 🤝 Contributing

Issues and pull requests are welcome. If you're proposing a new agent or action, please:

1. Keep it focused on a single responsibility (one action, one job)
2. Add Javadoc explaining what it consumes and produces
3. Wire its output into the existing pipeline rather than duplicating an existing stage

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.

---

## 👤 Author

Built by **[Omar Drissi](https://github.com/drissiOmar98)** — feel free to open an issue, star the repo ⭐, or connect on GitHub.
