FROM eclipse-temurin:21-jdk-alpine
WORKDIR /usr/app

COPY .mvn .mvn
COPY content-repository/pom.xml content-repository/pom.xml
COPY task-runner/pom.xml task-runner/pom.xml
COPY test-runner/pom.xml test-runner/pom.xml
COPY web/pom.xml web/pom.xml
COPY mvnw pom.xml ./
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -pl web -am test


COPY content-repository content-repository
COPY web web

RUN ./mvnw install -pl web -am -DskipTests

CMD ["tail", "-f", "/dev/null"]
