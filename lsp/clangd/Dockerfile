FROM ws-bridge AS ws-bridge

FROM ubuntu:22.04
LABEL authors="Danil"

# Устанавливаем необходимые зависимости и clangd
RUN apt-get update
RUN apt-get install -y \
        clangd-14 \
        clang-tidy-14
RUN apt-get clean
RUN rm -rf /var/lib/apt/lists/*

# Создаем симлинк для удобного вызова
RUN ln -s /usr/bin/clangd-14 /usr/bin/clangd

COPY --from=ws-bridge /opt/bun/bun /opt/bun/bun
COPY --from=ws-bridge /usr/ws-bridge /usr/ws-bridge

WORKDIR /workspace

COPY main.cpp .

WORKDIR /usr/ws-bridge
CMD ["/opt/bun/bun", "run", "main.ts"]