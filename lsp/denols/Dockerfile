FROM ws-bridge AS ws-bridge

FROM denoland/deno:2.6.5
LABEL authors="Danil"

COPY --from=ws-bridge /opt/bun/bun /opt/bun/bun
COPY --from=ws-bridge /usr/ws-bridge /usr/ws-bridge

COPY deno.json /workspace/deno.json
COPY index.ts /workspace/index.ts

WORKDIR /usr/ws-bridge
CMD ["/opt/bun/bun", "run", "main.ts"]