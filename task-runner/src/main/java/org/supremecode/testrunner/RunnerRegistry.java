package org.supremecode.testrunner;

import java.util.Map;

public class RunnerRegistry {
    private final Map<String, Runner> runners;

    public RunnerRegistry(Map<String, Runner> runners) {
        this.runners = runners;
    }

    public Runner get(String language) {
        return runners.get(language);
    }
}
