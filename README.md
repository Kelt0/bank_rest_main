# Bank Cards API

## Быстрый старт

### Вариант A. Docker Compose (рекомендуется)
1) Запустите инфраструктуру и приложение:
   - docker compose up -d
2) Приложение будет доступно на:
   - http://localhost:8080

### Вариант B. Локальный запуск (без Docker)
1) Запустите PostgreSQL (локально или через Docker):
   - docker compose up -d db
2) Установите переменные окружения (пример):
   - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bank_db
   - SPRING_DATASOURCE_USERNAME=postgres
   - SPRING_DATASOURCE_PASSWORD=admin
   - security.jwt.secret=замените-на-сложный-секрет
   - security.jwt.expiration=900000
3) Запустите приложение:
   - mvn spring-boot:run
   - или сборка JAR и запуск:
     - mvn -DskipTests=true clean package
     - java -jar target/*.jar

## Конфигурация

Основные свойства (могут задаваться через переменные окружения):
- Datasource:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/bank_db
  - SPRING_DATASOURCE_USERNAME=postgres
  - SPRING_DATASOURCE_PASSWORD=admin
  - SPRING_JPA_HIBERNATE_DDL_AUTO=none
- JWT:
  - security.jwt.secret=секретная_строка_для_подписи_JWT
  - security.jwt.expiration=время_жизни_токена_в_миллисекундах
  - security.jwt.header=Authorization
  - security.jwt.prefix=Bearer

Liquibase применяет миграции автоматически при старте приложения.

## Документация API

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
