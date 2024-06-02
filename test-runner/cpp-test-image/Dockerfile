FROM gcc:13.2.0
RUN apt-get update && \
    apt-get install -y cmake

RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

WORKDIR /usr/app
COPY CMakeLists.txt sc_test.cc solution.hpp ./
RUN cmake -S . -B build && cmake --build build
CMD cmake -S . -B build && cmake --build build && cd build && ctest --output-junit junit.xml