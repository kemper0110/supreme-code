package org.supremecode.testrunner;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
class LogCallback implements ResultCallback<Frame> {
    final protected String containerId;

    final protected StringBuilder builder = new StringBuilder();

    public String getLogs() {
        return builder.toString();
    }

    @Override
    public void onStart(Closeable closeable) {
        log.debug("container({})-logging: started", containerId);
    }

    @Override
    public void onNext(Frame frame) {
        builder.append(new String(frame.getPayload()));
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("container({})-logging: error {}", containerId, throwable.getMessage());
    }

    @Override
    public void onComplete() {
        log.debug("container({})-logging: completed", containerId);
    }

    @Override
    public void close() throws IOException {

    }
}