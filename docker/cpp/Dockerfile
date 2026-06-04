FROM gcc:13.2.0

RUN apt-get -o Acquire::Retries=5 update -y && \
    apt-get -o Acquire::Retries=5 install -y --no-install-recommends \
        clangd \
        cmake && \
    rm -rf /var/lib/apt/lists/*

RUN groupadd -r appgroup && useradd -r -g appgroup appuser && \
    mkdir -p /usr/run /usr/test && \
    chown -R appuser:appgroup /usr/run /usr/test

WORKDIR /usr/test
COPY CMakeLists.txt sc_test.cc solution.hpp ./
COPY CMakeLists.txt /usr/run/CMakeLists.txt

WORKDIR /usr/run
RUN printf 'int main() { return 0; }\n' > main.cpp && \
    cmake -S . -B build && \
    rm -f main.cpp

WORKDIR /usr/test
RUN cmake -S . -B build && cmake --build build && \
    chown -R appuser:appgroup /usr/run /usr/test

USER appuser

WORKDIR /usr/run
CMD ["clangd", "--background-index", "--compile-commands-dir=build", "--query-driver=/usr/local/bin/g++,/usr/local/bin/c++"]
