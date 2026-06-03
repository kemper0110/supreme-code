package org.supremecode.testrunner;

import java.util.Map;

public class RunnerRegistry {
    private final Map<Language, Runner> runners;

    public RunnerRegistry(Map<Language, Runner> runners) {
        this.runners = runners;
    }

    public Runner get(Language language) {
        return runners.get(language);
    }
}
