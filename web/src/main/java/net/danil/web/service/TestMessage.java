package net.danil.web.service;

import org.danil.model.Language;

public record TestMessage(Long solutionId, String code, String testSlug, Language language) {
}
