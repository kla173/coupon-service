FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/target/coupon-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]