FROM denoland/deno:2.6.5

WORKDIR /usr/run

USER root
RUN mkdir -p /usr/run /usr/test && \
    chown -R deno:deno /usr/run /usr/test

USER deno
COPY deno.json /usr/run/deno.json
COPY deno.json /usr/test/deno.json
RUN deno install --config /usr/run/deno.json && \
    deno install --config /usr/test/deno.json

CMD ["deno", "lsp"]
