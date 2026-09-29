# Event-Driven Order Notification System

A Spring Boot application demonstrating asynchronous event-driven architecture using Apache Kafka for decoupling order creation from email notification.

## Technologies

- Java 21
- Spring Boot 3.3
- Spring Data JPA + Hibernate
- PostgreSQL
- Apache Kafka + Spring Kafka
- Spring Mail
- Jakarta Bean Validation
- Docker Compose

## Architecture

```
POST /api/orders
       ↓
Save Order (PostgreSQL)
       ↓
Publish OrderCreatedEvent (Kafka)
       ↓
Kafka Consumer (async)
       ↓
Save Notification (PENDING)
       ↓
Send Email
       ↓
Update Notification (SENT)
```

The REST API returns immediately after publishing to Kafka. Email processing happens asynchronously.

## Setup & Run

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL on port 5432 and Kafka on port 9092.

### 2. Configure Environment

Copy `.env.example` to `.env` and fill in values (or set environment variables directly).

By default `MAIL_MOCK=true`, which logs emails to console instead of sending them — no SMTP setup needed for local demo.

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or run `OrderNotificationApplication` from IntelliJ.

The application starts on `http://localhost:8080`.

## API Usage

### Users

```
POST   /api/users          — Create user
GET    /api/users/{id}     — Get user by ID
GET    /api/users          — List all users
```

**Create user:**
```json
POST /api/users
{
  "name": "Atharva",
  "email": "atharva@example.com"
}
```

### Orders

```
POST   /api/orders              — Create order (triggers Kafka event)
GET    /api/orders/{id}         — Get order by ID
GET    /api/orders              — List all orders
PUT    /api/orders/{id}/status  — Update order status
```

**Create order:**
```json
POST /api/orders
{
  "userId": 1,
  "totalAmount": 1499.99
}
```

**Update status:**
```json
PUT /api/orders/1/status
{
  "status": "CONFIRMED"
}
```

Valid transitions: `CREATED → CONFIRMED`, `CREATED → CANCELLED`, `CONFIRMED → SHIPPED`, `CONFIRMED → CANCELLED`, `SHIPPED → DELIVERED`

### Notifications

```
GET    /api/notifications              — List all notifications
GET    /api/notifications/{id}         — Get notification by ID
GET    /api/notifications/order/{orderId} — Notifications for an order
```

## Kafka Flow

When `POST /api/orders` is called:

1. Order saved to PostgreSQL with status `CREATED`
2. `OrderCreatedEvent` published to `order-events` topic (keyed by `orderId`)
3. API returns `201 Created` immediately
4. `OrderEventConsumer` receives event asynchronously
5. Idempotency check: skip if `eventId` already in `processed_events`
6. Creates `Notification` with status `PENDING`
7. Sends email (or logs if `MAIL_MOCK=true`)
8. Updates notification to `SENT`
9. Saves `eventId` to `ProcessedEvent`

Failed events are retried up to 4 times, then sent to `notification-dlt` topic.

## Demo (Quick Test)

```bash
# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Atharva","email":"atharva@example.com"}'

# Create an order (watch logs for Kafka consumer output)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"totalAmount":1499.99}'

# Check notification status
curl http://localhost:8080/api/notifications/order/1
```
