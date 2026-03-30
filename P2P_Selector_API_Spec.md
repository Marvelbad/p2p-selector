# P2P-Selector (sbp, card, account)

## Обзор

### Возможные типы платежа

- Плательщик осуществляет p2p-перевод **с карты на карту**, API отдаёт номер карты получателя
- Плательщик осуществляет **СБП перевод** по номеру телефона, который отдаёт API
- Плательщик осуществляет **перевод по номеру счёта**, который отдаёт API

### Процесс проведения платежа

1. Ваш backend вызывает метод создания платежа (`/p2p-selector/sale`)
2. В ответ получаете PAN карты, номер телефона для СБП или номер счёта, имя и банк, куда покупатель должен осуществить перевод
3. Если платёж не будет произведён, то крайне важно его отменить (`/p2p-selector/cancel`). Если платежи не будут отменяться, то вы быстро упрётесь в лимиты и трафик встанет.

> **Важно:** в поле `customer` необходимо передавать id пользователя в вашей системе.

### Тестовые статусы (зависят от значения поля `amount`)

| Диапазон `amount` | Статус | Описание |
|---|---|---|
| ≤ 100 | `failed` | Неуспех |
| 100 – 500 | `partial_complete` | Частичная оплата |
| 500 – 1000 | `pending` | В обработке |
| > 1000 | `complete` | Успех |

---

## Создание платежа

### POST /p2p-selector/sale

---

#### Перевод на карту (`payment_type: "card"`)

**Запрос:**

```json
{
  "payment_type": "card",
  "amount": "100",                                    // сумма платежа
  "currency": "RUB",                                  // валюта платежа
  "customer": "6eeef8d1-188e-47dc-b858-b4f6f6f658a7", // идентификатор клиента в вашей системе
  "order": "test-order01",                            // номер заказа в вашей системе
  "description": "Description",                      // описание заказа в вашей системе
  "language": "ru",
  "merchant_id": "82d7b202-632a-439c-bd32-ecae71282d5c",
  "endpoint_id": "a4c0bda0-9129-4ca5-9815-83bf9e6b995d",
  "email": "test@example.com",                        // email плательщика
  "notification_url": "https://example.com",          // адрес для уведомлений об оплате
  "signature": "<signature>"                          // подпись
}
```

**Ответ:**

```json
{
  "status": "success",
  "invoice_id": "ab0cee2f-59e0-40f6-afca-dbab4f2a5996", // id заказа в нашей системе, для получения статуса
  "beneficiary": {
    "pan": "4111 1111 1111 1111",  // номер карты, кому необходимо осуществить перевод
    "name": "SOME NAME",           // имя владельца карты
    "bank_name": "SOME BANK",      // банк владельца карты
    "phone": null,
    "account_number": null,
    "country_code": null,
    "country_name": null,
    "country_phone_code": null
  }
}
```

---

#### СБП перевод (`payment_type: "sbp"`)

**Запрос:**

```json
{
  "payment_type": "sbp",
  "amount": "100",                                    // сумма платежа
  "currency": "RUB",                                  // валюта платежа
  "customer": "6eeef8d1-188e-47dc-b858-b4f6f6f658a7", // идентификатор клиента в вашей системе
  "order": "test-order01",                            // номер заказа в вашей системе
  "description": "Description",                      // описание заказа в вашей системе
  "language": "ru",
  "merchant_id": "82d7b202-632a-439c-bd32-ecae71282d5c",
  "endpoint_id": "a4c0bda0-9129-4ca5-9815-83bf9e6b995d",
  "email": "test@example.com",                        // email плательщика
  "notification_url": "https://example.com",          // адрес для уведомлений об оплате
  "signature": "<signature>"                          // подпись
}
```

**Ответ:**

```json
{
  "status": "success",
  "invoice_id": "ab0cee2f-59e0-40f6-afca-dbab4f2a5996", // id заказа в нашей системе, для получения статуса
  "beneficiary": {
    "pan": null,
    "name": "SOME NAME",           // имя владельца счёта
    "bank_name": "SOME BANK",      // банк владельца счёта
    "phone": "+71111111111",        // телефон, через который необходимо отправить СБП
    "account_number": null,
    "country_code": null,
    "country_name": null,
    "country_phone_code": null
  }
}
```

---

#### Перевод по номеру счёта (`payment_type: "bank_transfer"`)

**Запрос:**

```json
{
  "payment_type": "bank_transfer",
  "amount": "100",                                    // сумма платежа
  "currency": "RUB",                                  // валюта платежа
  "customer": "6eeef8d1-188e-47dc-b858-b4f6f6f658a7", // идентификатор клиента в вашей системе
  "order": "test-order01",                            // номер заказа в вашей системе
  "description": "Description",                      // описание заказа в вашей системе
  "language": "ru",
  "merchant_id": "82d7b202-632a-439c-bd32-ecae71282d5c",
  "endpoint_id": "a4c0bda0-9129-4ca5-9815-83bf9e6b995d",
  "email": "test@example.com",                        // email плательщика
  "notification_url": "https://example.com",          // адрес для уведомлений об оплате
  "signature": "<signature>"                          // подпись
}
```

**Ответ:**

```json
{
  "status": "success",
  "invoice_id": "ab0cee2f-59e0-40f6-afca-dbab4f2a5996", // id заказа в нашей системе, для получения статуса
  "beneficiary": {
    "pan": null,
    "name": "SOME NAME",                // имя владельца счёта
    "bank_name": "SOME BANK",           // банк владельца счёта
    "phone": null,
    "account_number": "0123...9",       // номер счёта, на который необходимо осуществить перевод
    "country_code": null,
    "country_name": null,
    "country_phone_code": null
  }
}
```

---

## Отмена платежа

### POST /p2p-selector/cancel

**Запрос:**

```json
{
  "merchant_id": "82d7b202-632a-439c-bd32-ecae71282d5c",
  "endpoint_id": "a4c0bda0-9129-4ca5-9815-83bf9e6b995d",
  "invoice_id": "ab0cee2f-59e0-40f6-afca-dbab4f2a5996",
  "signature": "<signature>"  // подпись
}
```

**Ответ:**

```json
{
  "status": "success"
}
```

---

## Информация о платеже

### Получение статуса платежа

Получить статус можно на любом этапе проведения платежа.

**Адрес:** `/status`
**Метод:** `GET`

#### Параметры запроса

| Параметр | Описание | Пример | Применение |
|---|---|---|---|
| `merchant_id` | Ваш ID мерчанта в системе, выдаётся при регистрации | `82d7b202-...` | Обязательный |
| `endpoint_id` | Ваш ID эндпоинта в системе, выдаётся при регистрации | `a4c0bda0-...` | Обязательный |
| `order` | Номер заказа/платежа в системе мерчанта | `order-001` | Обязательный |
| `signature` | Подпись HS256 | `6ye84vg…` | Обязательный |

#### Пример вызова

```
GET https://{base_url}/status?merchant_id=82d7b202-632a-439c-bd32-ecae71282d5c&endpoint_id=a4c0bda0-9129-4ca5-9815-83bf9e6b995d&order=test-order01&signature=6ye84vg
```

#### Пример ответа — успешный ордер

```json
{
  "status": "success",
  "payment_status": "complete",
  "id": "8eee08ec-3ae9-462d-b2e5-dc22624827f6",
  "order": "test-order01",
  "price": "100.0000",
  "amount_paid": "100.0000",
  "currency": "RUB"
}
```

#### Пример ответа — неуспешный ордер

При неуспешном статусе `failed` дополнительно передаются параметры `last_payment_error_code` и `last_payment_error`:

```json
{
  "status": "success",
  "payment_status": "failed",
  "id": "8eee08ec-3ae9-462d-b2e5-dc22624827f6",
  "order": "test-order01",
  "price": "100.0000",
  "amount_paid": "0.0000",
  "currency": "RUB",
  "last_payment_error_code": "error_code",
  "last_payment_error": "error"
}
```

### Возможные статусы платежа

| Статус | Описание |
|---|---|
| `new` | Платёж создан |
| `pending` | Платёж находится в обработке |
| `complete` | Операция успешна |
| `partial_complete` | Операция оплачена частично (при P2P) |
| `failed` | Операция неуспешна (см. `payment_error_code` и `payment_error`) |

---

## Расчёт подписи HS256

Для проверки запросов используется подпись, которая считается по алгоритму **JSON Web Signature (JWS)**.
Подробнее об алгоритме: https://tools.ietf.org/html/rfc7515

### Алгоритм формирования

```
ASCII(BASE64URL(UTF8(JWS Protected Header)) || '.' || BASE64URL(JWS Payload))
```

**JWS Protected Header:**

```json
{"alg": "HS256"}
```

**JWS Payload:**

```json
{"PATH": "<путь-из-URL>", "GET": {<словарь-query-string>}, "POST": {<словарь-POST>}}
```

### Правила формирования словарей в GET и POST

- Ключи сортируются в алфавитном порядке
- Значения всегда являются строками
- Значения, указанные с использованием кириллицы, нужно энкодить в UTF-8
- **BASE64URL ≠ BASE64:**
  - Все `=` удаляются из конца строки
  - `+` заменяется на `-`
  - `/` заменяется на `_`

### Готовые библиотеки для расчёта подписи

| Язык программирования | Ссылка |
|---|---|
| Python | https://pypi.org/project/jws/ |
| PHP | https://github.com/web-token/jwt-signature |
| JavaScript | https://www.npmjs.com/package/jws |
| Java | https://github.com/auth0/java-jwt |

---

## Примеры кода для расчёта подписи

### Python

```python
import json
import jws
from unittest.mock import patch

def _signing_input(head, payload, is_json=False):
    from jws import utils
    enc = utils.to_base64 if is_json else utils.encode
    head_input, payload_input = map(enc, [head, payload])
    result = b"%s.%s" % (head_input, payload_input)
    return result

def create_signature(api_secret, path, payload):
    payload = {
        "PATH": path,
        "POST": dict(sorted([(key, str(value)) for key, value in payload.items()]))
        # Измените на 'GET', если нужно
    }

    result = {
        "head": json.dumps({'alg': 'HS256'}, separators=(',', ':')),
        "payload": json.dumps(payload, separators=(',', ':')),
        "key": api_secret
    }

    with patch('jws._signing_input', wraps=_signing_input):
        signature = jws.sign(**result, is_json=True).decode("utf-8")

    return signature

api_secret = "test"
path = "/init"
payload = {
    "amount": "100",
    "merchant_id": "1",
    "endpoint_id": "1",
    "order": "order-0001"
}

print(create_signature(api_secret, path, payload))
```

### PHP

```php
<?php

$b64url = function ($string) {
    return rtrim(strtr(base64_encode($string), '+/', '-_'), '=');
};

$prepare_payload = function ($path, $data, $method) {
    return [
        'PATH' => $path,
        strtoupper($method) => array_map('strval', $data)
    ];
};

$apiSecret = 'test';
$path = '/init';
$data = [
    "amount"      => "100",
    "merchant_id" => "1",
    "endpoint_id" => "1",
    "order"       => "order-0001"
];

$method = 'POST'; // Измените на 'GET', если нужно
$payload = $prepare_payload($path, $data, $method);
ksort($payload[$method]);

$head = ['alg' => 'HS256'];
$headJson    = json_encode($head,    JSON_UNESCAPED_SLASHES);
$payloadJson = json_encode($payload, JSON_UNESCAPED_SLASHES);

$trueSign = $b64url(hash_hmac('sha256', $b64url($headJson) . '.' . $b64url($payloadJson), $apiSecret, true));

echo $trueSign;
?>
```

### Ruby

```ruby
require 'json'
require 'base64'
require 'openssl'

def base64_url_encode(str)
  Base64.urlsafe_encode64(str).gsub(/=+$/, '')
end

def signing_input(header, payload)
  head_input    = base64_url_encode(header)
  payload_input = base64_url_encode(payload)
  "#{head_input}.#{payload_input}"
end

def create_signature(api_secret, path, payload)
  payload = {
    "PATH" => path,
    "POST" => payload.sort.to_h.transform_values(&:to_s)
    # Замените на 'GET', если нужно
  }

  result = {
    "head"    => JSON.generate({ alg: 'HS256' }, ascii_only: true),
    "payload" => JSON.generate(payload, ascii_only: true),
    "key"     => api_secret
  }

  signing_input_str = signing_input(result["head"], result["payload"])
  signature = OpenSSL::HMAC.digest(OpenSSL::Digest.new('sha256'), result["key"], signing_input_str)
  base64_url_encode(signature)
end

api_secret = "api_secret"
path       = "/init"
payload    = {
  "amount"      => "100.0000",
  "merchant_id" => "0.0000",
  "endpoint_id" => "0.0000",
  "currency"    => "RUB",
  "order"       => "1234"
}

puts create_signature(api_secret, path, payload)
```
