FROM gcc:latest

RUN apt-get update && \
    apt-get install -y \
    cmake

WORKDIR /usr/app
COPY . .
RUN cmake -S . -B build && cmake --build build && cd build && ctest --output-junit junit.xml
RUN rm hello_test.cc

CMD cmake -S . -B build && cmake --build build && cd build && ctest --output-junit junit.xml