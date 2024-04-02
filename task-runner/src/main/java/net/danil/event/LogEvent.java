package net.danil.event;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LogEvent implements RunnerEvent {
    private final String message;
    private final String eventType = "log";
}
