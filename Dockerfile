# Используем образ с Java 24
FROM eclipse-temurin:24-jdk

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл
COPY adventbot/build/libs/adventbot-0.0.1-SNAPSHOT.jar app.jar

# Параметры для запуска
ENTRYPOINT ["java", "-jar", "app.jar"]