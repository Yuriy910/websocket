# Этап 1: Сборка проекта
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn clean package -DskipTests

# Этап 2: Финальный образ с правильным именем jar
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Обрати внимание: используем корректное имя jar
COPY --from=builder /app/target/websocket-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]


