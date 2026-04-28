FROM ubuntu:22.04
LABEL authors="Danil"

RUN apt-get update
RUN apt-get install -y golang-go
RUN apt-get install -y gopls

WORKDIR /workspace

COPY go.mod .
COPY main.go .

CMD ["gopls", "-v", "-rpc.trace", "-logfile=/gopls.log"]