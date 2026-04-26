FROM ws-bridge AS ws-bridge

FROM ubuntu:22.04
LABEL authors="Danil"

RUN apt-get update
RUN apt-get install -y golang-go
RUN apt-get install -y gopls

COPY --from=ws-bridge /opt/bun/bun /opt/bun/bun
COPY --from=ws-bridge /usr/ws-bridge /usr/ws-bridge

WORKDIR /workspace

COPY go.mod .
COPY main.go .

WORKDIR /usr/ws-bridge
CMD ["/opt/bun/bun", "run", "main.ts"]