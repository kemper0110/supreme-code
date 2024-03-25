FROM eclipse-temurin:21-jdk-alpine
WORKDIR /usr/app

COPY .mvn .mvn
COPY mvnw mvnw.cmd pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline test
COPY src src
RUN ./mvnw package

CMD ["./mvnw", "test", "--offline"]