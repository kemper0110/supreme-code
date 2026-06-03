package org.supremecode.testrunner;

import java.util.Arrays;
import java.util.Optional;

public enum Language {
    Cpp("cpp"),
    Java("java"),
    Javascript("javascript");

    private final String platformId;

    Language(String platformId) {
        this.platformId = platformId;
    }

    public static Optional<Language> fromPlatformId(String platformId) {
        return Arrays.stream(values())
                .filter(language -> language.platformId.equals(platformId))
                .findFirst();
    }
}
