package com.omar.blog_agent.model;

import java.util.List;

/**
 * Multiple candidate titles for the post, with one selected as the
 * strongest and a short rationale for the choice.
 *
 * <p>Generating several options before committing tends to beat asking an
 * LLM for a single title outright &mdash; it has room to compare tradeoffs
 * (clarity vs. curiosity, length vs. specificity) instead of anchoring on
 * its first idea.</p>
 *
 * @param options       all candidate titles that were considered
 * @param selectedTitle the strongest candidate, used as the post's actual title
 * @param rationale     a brief explanation of why {@code selectedTitle} was chosen
 */
public record TitleOptions(List<String> options, String selectedTitle, String rationale) {
}
