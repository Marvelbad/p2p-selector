# CLAUDE.md — p2p-selector

Сервис для обработки P2P-переводов. Заменяет Агрософт как посредника между внешними российскими банками (ВТБ, Сбер, Тинькофф) / локальными партнёрами и банком Кибит.

## Контекст проекта

### Бизнес-схема
```
ВТБ / Сбер / Тинькофф  ──→┐
Фирма 1 / 2 / 3        ←──┤  p2p-selector  ←──→  Кибит (Oracle АБС)
Агрософт               ←──┘
```

Все внешние участники стучатся в этот сервис по единому API (P2P Selector).
Сервис проверяет клиента, возвращает реквизиты для перевода, отслеживает статус.

> **Текущий партнёр:** ВТБ. Протокол P2P Selector разработан банком ВТБ.
> Именно ВТБ будет стучаться в наш сервис как клиент.

### Референс проект
**xfer-gateway** — `/Users/badribagateliya/IdeaProjects/gateway`
Аналогичная архитектура: Controller → Service → Repository → Oracle.
Те же конвенции кода, те же принципы. Смотреть туда за образцом.

### Роль Кибита
Кибит — расчётный банк, не процессинговый. Платёж процессирует внешняя сторона (Монета/СБП).
Кибит занимается расчётами: фиксирует операции, зачисляет средства клиентам.

## Команды

```bash
# Запуск в режиме разработки
./gradlew bootRun

# Сборка JAR
./gradlew build

# Запуск тестов
./gradlew test

# Очистка
./gradlew clean
```

## Стек

- Java 21, Spring Boot 3.5.13
- Gradle Kotlin DSL (`build.gradle.kts`)
- PostgreSQL — своя таблица статусов/логов операций
- Spring Data JPA (для PostgreSQL)
- Oracle (АБС Вити) — хранимые процедуры через JdbcTemplate (детали уточняются)
- Lombok, SpringDoc/Swagger
- Jakarta Validation
- `com.auth0:java-jwt:4.5.1` — верификация подписей HS256
- `io.micrometer:micrometer-registry-prometheus` — экспорт метрик для Prometheus/Grafana

## API эндпоинты (по спеке P2P Selector)

| Метод | URL | Описание |
|-------|-----|----------|
| POST | `/p2p-selector/sale` | Создание платежа (card / sbp / bank_transfer) |
| POST | `/p2p-selector/cancel` | Отмена платежа |
| GET | `/status` | Получение статуса платежа |
| GET | `/health` | Проверка работоспособности сервиса |

### Типы платежа (`payment_type`)
- `card` — перевод на карту, в ответе приходит `pan`
- `sbp` — СБП по номеру телефона, в ответе приходит `phone`
- `bank_transfer` — по номеру счёта, в ответе приходит `account_number`

### Поля запроса POST `/p2p-selector/sale`

| Поле | Тип | Описание |
|------|-----|----------|
| `payment_type` | string | `card` / `sbp` / `bank_transfer` |
| `amount` | string | Сумма платежа |
| `currency` | string | Валюта (например `RUB`) |
| `customer` | string | ID клиента в системе партнёра |
| `order` | string | Номер заказа в системе партнёра |
| `description` | string | Описание заказа |
| `language` | string | Язык (например `ru`) |
| `merchant_id` | string | ID мерчанта (выдаётся при регистрации) |
| `endpoint_id` | string | ID эндпоинта (выдаётся при регистрации) |
| `email` | string | Email плательщика |
| `notification_url` | string | URL для уведомлений об оплате |
| `signature` | string | Подпись HS256 |

### Структура ответа `/sale` — объект `beneficiary`

```json
{
  "status": "success",
  "invoice_id": "...",
  "beneficiary": {
    "pan": "4111 1111 1111 1111",  // заполнен для card, null для остальных
    "name": "SOME NAME",
    "bank_name": "SOME BANK",
    "phone": "+71111111111",         // заполнен для sbp, null для остальных
    "account_number": "0123...9",    // заполнен для bank_transfer, null для остальных
    "country_code": null,
    "country_name": null,
    "country_phone_code": null
  }
}
```

### Поля запроса POST `/p2p-selector/cancel`

| Поле | Описание |
|------|----------|
| `merchant_id` | ID мерчанта |
| `endpoint_id` | ID эндпоинта |
| `invoice_id` | Наш ID платежа (из ответа `/sale`) |
| `signature` | Подпись HS256 |

Ответ: `{"status": "success"}`

### Поля запроса GET `/status` (query string)

| Параметр | Описание |
|----------|----------|
| `merchant_id` | ID мерчанта |
| `endpoint_id` | ID эндпоинта |
| `order` | Номер заказа в системе партнёра |
| `signature` | Подпись HS256 |

Пример: `GET /status?merchant_id=...&endpoint_id=...&order=...&signature=...`

### Структура ответа `/status`

```json
{
  "status": "success",
  "payment_status": "complete",
  "id": "...",
  "order": "test-order01",
  "price": "100.0000",
  "amount_paid": "100.0000",
  "currency": "RUB"
}
```

При `payment_status: "failed"` добавляются поля:
- `last_payment_error_code` — код ошибки
- `last_payment_error` — описание ошибки

### Статусы платежа (`payment_status`)
- `new` — платёж создан
- `pending` — в обработке
- `complete` — успех
- `partial_complete` — частичная оплата (при P2P)
- `failed` — неуспех (см. `last_payment_error_code` и `last_payment_error`)

### Тестовые статусы (зависят от `amount`)

| Диапазон `amount` | Статус |
|-------------------|--------|
| ≤ 100 | `failed` |
| 100 – 500 | `partial_complete` |
| 500 – 1000 | `pending` |
| > 1000 | `complete` |

## Подпись запросов

Алгоритм **HS256** (HMAC-SHA256, JWS).
Симметричный — один секретный ключ на обе стороны (в отличие от RSA в xfer-gateway).

Формула:
```
ASCII(BASE64URL(UTF8(JWS Protected Header)) || '.' || BASE64URL(JWS Payload))
```

JWS Header: `{"alg":"HS256"}`
JWS Payload:
- для POST: `{"PATH": "<path>", "POST": {<отсортированные поля>}}`
- для GET: `{"PATH": "<path>", "GET": {<отсортированные query-параметры>}}`

Правила:
- Ключи сортируются в алфавитном порядке
- Все значения — строки
- Кириллица в значениях — энкодить в UTF-8
- BASE64URL (не BASE64): убрать `=`, заменить `+`→`-`, `/`→`_`

Библиотека для Java: `com.auth0:java-jwt`

## Требования безопасности ВТБ

Помимо стандартной подписи HS256, ВТБ выдвинул дополнительное требование по безопасности.

### AES256 шифрование query-параметров (GET /status)

По требованию ИБ банка ВТБ — **нельзя передавать чувствительные данные в открытых query-параметрах**.
Решение: ВТБ шифрует значения query-параметров алгоритмом **AES256-CBC**.

**Как работает:**
1. ВТБ шифрует каждое значение query-параметра (AES256) → кодирует в base64
2. В каждом запросе передаёт header `INIT_VECTOR` со случайным вектором инициализации (IV)
3. Мы проверяем наличие header `INIT_VECTOR` → если есть: base64 decode → AES256 расшифровать → идти дальше по обычному пути (HS256 верификация и т.д.)

**Важно:** шифровать ответ **не нужно** — только расшифровывать входящий запрос.

**Технические параметры:**

| Параметр | Значение |
|----------|----------|
| Алгоритм | AES256-CBC |
| Ключ | 256 бит (32 байта) — ВТБ пришлёт отдельно |
| IV | случайный, в каждом запросе в header `INIT_VECTOR` |
| Шифруются | только значения query-параметров |

**Что нужно реализовать:**
- `AesDecryptionService.java` — расшифровка (javax.crypto, без новых зависимостей)
- Интеграция в обработку `/status` — проверка header и расшифровка до HS256 верификации
- Ключ хранится как переменная окружения `VTB_AES_KEY`

> Подробности: `vtb_aes256_encryption.md` в корне проекта.
> Сетевая часть (домен, SSL-сертификат, IP whitelist) — задача сетевого администратора, см. `vtb_network_setup.md`.

---

## Структура проекта

```
src/main/java/com/cibit/p2p/selector/
├── P2pSelectorApplication.java                        ✅
├── config/
│   ├── OracleDataSourceConfig.java                    ✅ (@Primary, JdbcTemplate)
│   └── PostgresDataSourceConfig.java                  ✅ (JPA, @EnableJpaRepositories)
├── controller/
│   ├── P2pController.java                             ✅ (/sale, /cancel, /status, /health)
│   └── api/P2pApi.java                                ✅ (Swagger интерфейс)
├── service/
│   └── P2pService.java                                ✅
├── repository/
│   ├── P2pRepository.java                             ⏳ заглушка (ждём контракт Oracle от Вити)
│   └── PaymentRepository.java                         ✅ (JPA)
├── entity/
│   └── Payment.java                                   ✅
├── enums/
│   └── PaymentStatus.java                             ✅
├── security/
│   └── SignatureService.java                          ✅ (верификация HS256)
├── dto/
│   ├── request/
│   │   ├── SaleRequest.java                           ✅
│   │   ├── CancelRequest.java                         ✅
│   │   └── StatusRequest.java                         ✅
│   └── response/
│       ├── SaleResponse.java                          ✅
│       ├── CancelResponse.java                        ✅
│       ├── StatusResponse.java                        ✅
│       └── Beneficiary.java                           ✅
└── exception/
    ├── GlobalExceptionHandler.java                    ✅
    ├── InvalidSignatureException.java                 ✅
    └── PaymentNotFoundException.java                  ✅

src/main/resources/
├── application.yaml                                   ✅
├── application-local.yaml                             ✅ (в .gitignore)
└── db/migration/migration/
    └── V1__create_payments_table.sql                  ✅ (выполнить вручную в PostgreSQL)

src/test/java/com/cibit/p2p/selector/
├── SignatureGeneratorTest.java                        ✅ (генератор подписей для Postman)
├── security/
│   └── SignatureServiceTest.java                      ✅ (6 тестов HS256)
└── service/
    └── P2pServiceTest.java                            ✅ (8 тестов resolveStatus)

/ (корень проекта)
├── Dockerfile                                         ✅ (eclipse-temurin:21-jre)
├── docker-compose.yml                                 ✅ (p2p-app + p2p-postgres)
├── vtb_network_setup.md                               ✅ (сетевые требования ВТБ — для сетевого админа)
└── vtb_aes256_encryption.md                           ✅ (требование AES256 от ИБ ВТБ — для разработчика)
```

## Конвенции кода

- **Lombok:** `@Getter`, `@Setter`, `@NoArgsConstructor`, `@Slf4j`, `@RequiredArgsConstructor`
- **Swagger:** `@Schema(description = "...", example = "...")` на всех полях DTO
- **Валидация:** `@NotNull`, `@NotBlank` на обязательных полях
- **Комментарии и коммиты:** на русском языке
- **Деньги:** `BigDecimal`, не `double`
- **Дата/время:** `LocalDateTime` (java.time)
- **Ответы:** `@JsonInclude(NON_NULL)` — null-поля не попадают в JSON
- **Спека — источник истины:** названия полей в DTO, запросах и ответах должны **строго соответствовать** `P2P_Selector_API_Spec.md`. Никаких переименований, camelCase-адаптаций или "улучшений" без явного указания.

## Принципы разработки

- **KISS** — простое решение лучше умного
- **DRY** — не дублировать логику
- **YAGNI** — только то что нужно сейчас по спеке
- **Single Responsibility** — Controller принимает, Service логика, Repository БД

## Конфигурация БД

```bash
# PostgreSQL (своя таблица операций)
# Порт 5433 на сервере — чтобы не конфликтовать с gateway-postgres (5432)
POSTGRES_URL=jdbc:postgresql://host:5433/p2p
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=...

# Oracle (АБС Вити) — детали уточняются
DB_URL=...
DB_USERNAME=...
DB_PASSWORD=...
```

> `gateway-postgres` занимает порт `5432` на сервере. Контейнер `p2p-postgres` запущен на `5433`.

## Что сейчас TODO

- ✅ Реализовать верификацию подписи HS256 входящих запросов
- ✅ PostgreSQL — своя таблица `payments` (статусы, логи операций)
- ✅ Swagger аннотации — `controller/api/P2pApi.java`
- ✅ Docker Compose — `p2p-app` + `p2p-postgres` (порт 5433)
- ✅ Добавить p2p-selector в `prometheus.yml` gateway (scrape на порт 8081)
- ⏳ Уточнить у Вити контракт хранимых процедур Oracle — заполнить `P2pRepository`
- ⏳ Grafana dashboard для p2p-selector + алерты
- ⏳ Детали взаимодействия с Монетой/СБП — уточнить у руководства
- ⏳ Реализовать AES256 расшифровку query-параметров для GET `/status` (ждём ключ от ВТБ)

## Деплой

Сервис задеплоен на сервере `10.222.179.27`.

### Расположение файлов на сервере
- Приложение: `/opt/p2p/`
- JAR: `/opt/p2p/p2p-selector-0.0.1-SNAPSHOT.jar`
- Docker Compose: `/opt/p2p/docker-compose.yml`
- Переменные окружения: `/opt/p2p/.env`

### Процесс деплоя
```bash
# 1. Собрать JAR локально
./gradlew build -x test

# 2. Скопировать JAR на сервер
scp build/libs/p2p-selector-0.0.1-SNAPSHOT.jar user@10.222.179.27:/opt/p2p/

# 3. На сервере — пересобрать и перезапустить
cd /opt/p2p
sudo docker-compose up -d --build
```

### Контейнеры
| Контейнер | Порт | Описание |
|-----------|------|----------|
| p2p-app | 8081 | Spring Boot приложение |
| p2p-postgres | 5433 | PostgreSQL база данных |

Мониторинг: Prometheus scrape на `p2p-app:8081/actuator/prometheus` (через `gateway_network`).

## Тестирование подписей (Postman)

Для генерации HS256 подписей при ручном тестировании используй:
`src/test/java/com/cibit/p2p/selector/SignatureGeneratorTest.java`

Запусти нужный метод в IDEA, скопируй подпись из консоли:
- `generateSaleSignature()` — для POST `/p2p-selector/sale`
- `generateCancelSignature()` — для POST `/p2p-selector/cancel`
- `generateStatusSignature()` — для GET `/status`

Перед запуском обнови поля в методе (`order`, `invoice_id` и т.д.) под конкретный запрос.

## Важно

- Спека API: `P2P_Selector_API_Spec.md` — файл лежит в корне проекта
- Если платёж не будет произведён — **обязательно отменять** через `/cancel`, иначе упрёмся в лимиты
- `customer` — это ID пользователя в системе партнёра, не в нашей
- `invoice_id` — наш внутренний ID, возвращаем в ответе `/sale`, нужен для `/cancel` и `/status`
