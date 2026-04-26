package org.supremecode.testrunner;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.WaitResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

record WaitResult(int exitCode) {
}

@RequiredArgsConstructor
@Slf4j
class WaitCallback implements ResultCallback<WaitResponse> {
    final protected String containerId;
    final protected CompletableFuture<WaitResult> resultFuture;

    @Override
    public void onComplete() {
    }

    @Override
    public void onStart(Closeable closeable) {

    }

    @Override
    public void onNext(WaitResponse waitResponse) {
        int statusCode = waitResponse.getStatusCode();
        log.debug("container({})-wait: complete", containerId);
        resultFuture.complete(new WaitResult(statusCode));
        log.info("container({})-wait: status-code={}", containerId, waitResponse.getStatusCode());
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("container({})-wait: error {}", containerId, throwable.getMessage());
        resultFuture.completeExceptionally(throwable);
    }

    @Override
    public void close() throws IOException {

    }
}
