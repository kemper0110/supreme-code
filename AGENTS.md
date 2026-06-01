# Supreme Code Agent Guide

## Project Overview

Supreme Code is an online programming-problem platform. The repository is a mixed Java/Kotlin and Node/TypeScript workspace with these main parts:

- `web` - Spring Boot WebFlux backend, gateway/API, persistence, security, Keycloak integration, MinIO, Kafka, Redis, observability.
- `task-runner` - Spring Boot service that runs user code through Docker.
- `test-runner` - Spring Boot/Kotlin service for validating submissions; includes language-specific Docker test images.
- `shared` - shared Kotlin models/configuration utilities.
- `plugin-sdk` and `common-plugins` - Java plugin API and common plugin implementations.
- `frontend` - React + Vite + Mantine UI application with Monaco/Yjs collaboration features.
- `lsp-manager2`, `lsp-manager-docker`, `lsp-ws-bridge`, `signaling-server`, `mcp` - TypeScript/Node services around LSP, WebSocket signaling, and MCP integration.
- `config`, `otel`, `kubernetes`, `init-rdb` - local infrastructure, observability, Kubernetes, and database initialization files.

## Working Rules

- Prefer small, focused changes that match nearby code style. This repo mixes Java, Kotlin, TypeScript, YAML, and generated artifacts.
- Do not edit generated/build output unless the source of truth requires it. Avoid touching `target`, `dist`, `node_modules`, and generated dashboard output except where explicitly required.
- Preserve existing local changes. Check `git status --short` before broad edits.
- Use `rg`/`rg --files` for searching.
- Use repository-local wrappers and package managers where present:
  - Maven: `./mvnw` or `mvnw.cmd`.
  - Frontend: Yarn 4 from `frontend/package.json`.
  - Other Node services: use the lockfile/package manager already present in that folder.

## Common Commands

Run from repository root unless a command says otherwise.

ENV REQUIRED!!!

`$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\jbr'; `

```bash
./mvnw test
./mvnw -pl web test
./mvnw -pl test-runner test
./mvnw -pl task-runner test
./mvnw -pl shared test
./mvnw -pl plugin-sdk test
./mvnw -pl common-plugins test
```

On Windows PowerShell, use `.\mvnw.cmd` instead of `./mvnw`.

> Now it's not working!! Say user to build Maven with IntelliJ IDEA by himself. 

Frontend:

```bash
cd frontend
yarn install
yarn dev
yarn build
yarn lint
yarn test
```

TypeScript services:

```bash
cd mcp && npm run build
cd mcp && npm run start
cd lsp-manager2 && yarn start
cd lsp-manager-docker && npm run start
cd signaling-server && npm run start
```

Local infrastructure:

```bash
docker compose up -d
docker compose ps
docker compose logs -f web test-runner task-runner
```

Grafana dashboard sources:

```bash
node config/grafana/dashboard-src/build-supreme-code.mjs
```

Edit dashboard source fragments under `config/grafana/dashboard-src/supreme-code`; rebuild `config/grafana/dashboards/supreme-code.json` after source changes.

## Java/Kotlin Notes

- The parent `pom.xml` is an aggregator for `task-runner`, `web`, `test-runner`, `shared`, `common-plugins`, and `plugin-sdk`.
- Most service modules target Java 21; `plugin-sdk` targets Java 17; `shared` currently has Kotlin JVM target 1.8.
- `web` and `test-runner` compile Kotlin through the Kotlin Maven plugin while also using Java source directories.
- `test-runner` generates Java sources from XSD files into `target/generated-sources/xsd`; do not edit generated classes directly.
- Spring services use WebFlux in several places. Keep blocking I/O contained and avoid introducing it into reactive flows without checking nearby patterns.

## Frontend Notes

- The frontend is React 18, Vite, TypeScript, Mantine, React Query, Monaco, Yjs/SyncedStore, and Zustand.
- Prefer existing Mantine components and local patterns before adding new UI dependencies.
- Validate user-facing frontend changes with at least `yarn lint` or `yarn build` from `frontend` when feasible.
- The app has collaboration/editor behavior; be careful around Monaco, Yjs, WebSocket, and IndexedDB state code.

## Runtime Dependencies

The local compose stack includes:

- Kafka on `localhost:9092`
- Redis on `localhost:6379`
- PostgreSQL on `localhost:5432`
- MinIO API on `localhost:9000`, console on `localhost:9001`
- Keycloak on `localhost:8070`
- Prometheus on `localhost:9090`
- Grafana on `localhost:3000`
- Loki on `localhost:3100`
- Tempo on `localhost:3200`, OTLP gRPC on `localhost:4317`

Default local credentials are declared in `docker-compose.yml` and initialization SQL/scripts live in `init-rdb`.

## Testing Guidance

- For Java/Kotlin changes, run the narrow Maven module test first, then broader tests if the change crosses modules.
- For frontend changes, run `yarn lint`, `yarn test`, or `yarn build` depending on the risk and touched files.
- For TypeScript service changes, run `npm run build` or `tsc` where the package exposes it; some small services only have `start` or placeholder `test` scripts.
- For Docker/test image changes under `test-runner/*-test-image` or `lsp/*`, build the relevant image when Docker is available.
- If tests require Docker, Kafka, Postgres, Keycloak, or MinIO, make that explicit in the final report.

## Files To Treat Carefully

- `platform.yaml` - platform/problem/plugin configuration.
- `docker-compose.yml` - local dependency topology and port bindings.
- `init-rdb` - database initialization.
- `web/src/main/resources/application.yaml`, `test-runner/src/main/resources/application.yaml`, `task-runner/src/main/resources/application.yml` - service configuration.
- `config/grafana/dashboard-src/**` - source of truth for Grafana dashboard edits.
- `config/grafana/dashboards/supreme-code.json` - generated dashboard output.
- Lockfiles: preserve package-manager-specific lockfiles and avoid regenerating them unless dependencies changed.

## Before Finishing

- Summarize changed files and the reason for each change.
- Report exact verification commands run, or state clearly why verification was skipped.
- Mention any required local services if the user needs them to reproduce behavior.
