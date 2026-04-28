FROM denoland/deno:2.6.5
LABEL authors="Danil"

WORKDIR /workspace

COPY deno.json /workspace/deno.json
COPY index.ts /workspace/index.ts

CMD ["deno", "lsp"]