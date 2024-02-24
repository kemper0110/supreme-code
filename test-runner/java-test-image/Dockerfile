FROM eclipse-temurin:21-jdk-alpine
WORKDIR /usr/app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
RUN ./mvnw package

ENTRYPOINT ["./mvnw", "test", "--offline"]