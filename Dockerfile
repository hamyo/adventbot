# Используем образ с Java 24
FROM eclipse-temurin:24-jdk

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл
COPY ./build/libs/adventbot-0.0.1-SNAPSHOT.jar adventbot.jar

# Параметры для запуска
ENTRYPOINT ["java", "-jar", "adventbot.jar"]