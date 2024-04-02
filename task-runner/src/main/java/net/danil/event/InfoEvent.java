package net.danil.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InfoEvent implements RunnerEvent {
    private final String message;
    private final String eventType = "info";
}
