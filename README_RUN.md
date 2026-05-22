# Run instructions (local development)

1) Copy `.env.example` to `.env` and fill in values you need (SMS_API_TOKEN, SMS_FROM_NUMBER, etc.).

2) Build the Java services (optional, compose will build images from Dockerfiles):

```bash
mvn -DskipTests clean package
```

3) Start Docker Compose (from project root):

```bash
docker compose up -d --build
```

4) Smoke tests (example curl commands):

- Create a scheduled SMS (replace host/port if needed):

```bash
curl -s -X POST http://localhost:8080/api/v1/scheduled-notifications \
  -H "Content-Type: application/json" \
  -d '{"type":"SMS","recipient":"+79991234567","text":"Тест scheduled","scheduledAt":"2026-04-22T12:00:00+03:00"}' | jq .
```

- List pending scheduled messages:

```bash
curl -s http://localhost:8080/api/v1/scheduled-notifications/pending | jq .
```

- Cancel a scheduled message (replace <ID> with id from create response):

```bash
curl -s -X PUT http://localhost:8080/api/v1/scheduled-notifications/cancel/<ID> | jq .
```

- Get status of scheduled message:

```bash
curl -s http://localhost:8080/api/v1/scheduled-notifications/<ID> | jq .
```

5) Logs:

```bash
docker compose logs -f notification-gateway
docker compose logs -f whatsapp-worker  # this container runs SMS worker logic
```

Notes:
- WhatsApp/Twilio credential placeholders have been replaced with SMS provider variables (`SMS_API_TOKEN`, `SMS_FROM_NUMBER`).
- If you want real SMS sending, fill `SMS_API_TOKEN` and `SMS_FROM_NUMBER` in `.env` (provider-specific format).
- The worker will publish status events containing `providerMessageId`. Gateway updates `scheduled_notifications.provider_message_id` automatically.
