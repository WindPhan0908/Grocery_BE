# Sử dụng JDK 17 làm base image
FROM eclipse-temurin:21-jdk

# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Copy toàn bộ project vào container
COPY . .

# Build project với Gradle
RUN ./gradlew build -x test

# Chạy ứng dụng Spring Boot
CMD ["java", "-jar", "build/libs/demo-0.0.1-SNAPSHOT.jar"]
