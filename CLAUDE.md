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

## Структура проекта

```
src/main/java/com/cibit/p2p/selector/
├── P2pSelectorApplication.java                        ✅
├── config/
│   ├── OracleDataSourceConfig.java                    ✅ (@Primary, JdbcTemplate)
│   └── PostgresDataSourceConfig.java                  ✅ (JPA, @EnableJpaRepositories)
├── controller/
│   ├── P2pController.java                             ✅ (/sale, /cancel, /status, /health)
│   └── api/P2pApi.java                                ⏳ Swagger интерфейс
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

> `gateway-postgres` занимает порт `5432` на сервере. Контейнер `p2p-postgres` будет на `5433`.

## Что сейчас TODO

- ✅ Реализовать верификацию подписи HS256 входящих запросов
- ✅ PostgreSQL — своя таблица `payments` (статусы, логи операций)
- ⏳ Уточнить у Вити контракт хранимых процедур Oracle — заполнить `P2pRepository`
- ⏳ Swagger аннотации — `controller/api/P2pApi.java`
- ⏳ Docker Compose — `p2p-app` + `p2p-postgres` (порт 5433)
- ⏳ Добавить p2p-selector в `prometheus.yml` gateway (scrape на порт 8081)
- ⏳ Детали взаимодействия с Монетой/СБП — уточнить у руководства

## Важно

- Спека API: `P2P_Selector_API_Spec.md` — файл лежит в корне проекта
- Если платёж не будет произведён — **обязательно отменять** через `/cancel`, иначе упрёмся в лимиты
- `customer` — это ID пользователя в системе партнёра, не в нашей
- `invoice_id` — наш внутренний ID, возвращаем в ответе `/sale`, нужен для `/cancel` и `/status`
