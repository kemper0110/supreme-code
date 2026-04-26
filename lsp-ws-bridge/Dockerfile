ARG BASE_IMAGE=ubuntu:22.04
FROM ${BASE_IMAGE}
LABEL authors="Danil"

COPY bun /opt/bun/bun

ENV BUN_INSTALL=/opt/bun
ENV PATH="$BUN_INSTALL:$PATH"

WORKDIR /usr/ws-bridge
COPY package.json .
COPY bun.lock .
RUN bun install

COPY tsconfig.json .
COPY main.ts .
