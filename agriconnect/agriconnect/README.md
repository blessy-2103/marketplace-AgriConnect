# 🌱 AgriConnect

A farm-to-customer marketplace platform. Farmers list their produce directly;
customers browse, order, and track delivery — no middlemen.

**Tech stack:** Java 17 · Spring Boot 3 · Spring Data JPA · Spring Security ·
MySQL · HTML/CSS/JavaScript (vanilla, no framework/build step)

---

## 1. Project structure

```
agriconnect/
├── pom.xml
├── database/
│   └── schema.sql              # reference SQL schema + seed data (manual setup)
└── src/main/
    ├── java/com/agriconnect/
    │   ├── AgriConnectApplication.java
    │   ├── config/             # SecurityConfig (CORS, auth rules)
    │   ├── security/            # Bearer-token auth filter
    │   ├── model/                # JPA entities: User, Product, Category, Order, OrderItem, AuthToken
    │   ├── repository/           # Spring Data JPA repositories
    │   ├── service/              # Business logic: UserService, ProductService, OrderService
    │   ├── controller/           # REST controllers
    │   ├── dto/                  # Request/response payloads
    │   └── exception/            # Custom exceptions + global handler
    └── resources/
        ├── application.properties
        ├── data.sql              # seeds product categories on startup
        └── static/                # the entire frontend (served by Spring Boot)
            ├── index.html          (landing page)
            ├── login.html
            ├── register.html
            ├── marketplace.html    (browse/search/filter/add-to-basket)
            ├── cart.html           (basket + checkout)
            ├── orders.html         (customer order history)
            ├── farmer-dashboard.html (manage listings + incoming orders)
            ├── css/style.css
            └── js/api.js, app.js
```

Since the frontend lives in `src/main/resources/static`, Spring Boot serves it
directly — there is only **one server to run**, no separate frontend dev server.

---

## 2. Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+ running locally (or Docker)

## 3. Database setup

Create a MySQL user/database, or just let Hibernate do it — the default
connection string in `application.properties` already includes
`createDatabaseIfNotExist=true`. You only need MySQL running and reachable;
update credentials in `application.properties` to match your setup:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/agriconnect_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

`spring.jpa.hibernate.ddl-auto=update` creates/updates all tables
automatically on first run. `database/schema.sql` is provided if you'd rather
create the schema by hand (e.g. in a shared/production database where you set
`ddl-auto=validate` instead).

**No MySQL available right now?** Open `application.properties` and swap the
active block: comment out the MySQL lines, uncomment the H2 in-memory block.
The app will run with zero external setup (data resets on every restart).

## 4. Run it

```bash
cd agriconnect
mvn spring-boot:run
```

Then open **http://localhost:8080** in your browser. That's it — frontend and
backend are the same app on the same port.

## 5. Try it out

1. Go to `/register.html`, create a **farmer** account (toggle "I'm a farmer").
2. Log in, go to **My Farm**, click **+ Add produce** to list a few items.
3. Open an incognito window (or log out), register a **customer** account.
4. Browse `/marketplace.html`, add items to your basket, and check out.
5. Back on the farmer account, go to **Incoming Orders** to update order
   status (Confirmed → Packed → Shipped → Delivered).

---

## 6. Authentication model

This project uses a lightweight custom **Bearer token** scheme rather than
sessions or JWT, to keep things simple and transparent:

- `POST /api/auth/register` / `POST /api/auth/login` return a random opaque
  token, stored server-side in the `auth_tokens` table with a 7-day expiry.
- The frontend stores the token in `localStorage` and sends it as
  `Authorization: Bearer <token>` on every request.
- `TokenAuthenticationFilter` validates the token on each request and
  populates Spring Security's context, so `@AuthenticationPrincipal User`
  works in controllers just like normal Spring Security.
- Passwords are hashed with BCrypt (`spring-boot-starter-security`).

## 7. REST API reference

| Method | Endpoint                       | Auth           | Description                              |
|--------|---------------------------------|----------------|-------------------------------------------|
| POST   | `/api/auth/register`            | public         | Create a customer or farmer account       |
| POST   | `/api/auth/login`               | public         | Log in, returns token                     |
| POST   | `/api/auth/logout`               | token          | Invalidate the current token              |
| GET    | `/api/auth/me`                   | token          | Current user profile                      |
| GET    | `/api/categories`                 | public         | List product categories                  |
| GET    | `/api/products?q=&categoryId=`    | public         | Browse/search active products             |
| GET    | `/api/products/{id}`              | public         | Product detail                             |
| GET    | `/api/farmer/products`            | farmer         | List my own products (active + removed)    |
| POST   | `/api/farmer/products`            | farmer         | Create a product listing                   |
| PUT    | `/api/farmer/products/{id}`       | farmer (owner) | Update a listing                           |
| DELETE | `/api/farmer/products/{id}`       | farmer (owner) | Soft-delete (deactivate) a listing         |
| GET    | `/api/farmer/orders`              | farmer         | Order line items for my products           |
| POST   | `/api/orders`                     | customer       | Place an order (checkout)                  |
| GET    | `/api/orders`                     | customer       | My order history                           |
| GET    | `/api/orders/{id}`                | owner/farmer   | Order detail                               |
| PATCH  | `/api/orders/{id}/status`         | owner/farmer   | Update order status                        |

## 8. Notable design decisions

- **Stock deduction happens at checkout**, inside the same transaction as
  order creation, with a hard stock check to prevent overselling.
- **Price is snapshotted** on `OrderItem.priceAtOrder` at the time of
  purchase, so a farmer changing a price later doesn't rewrite order history.
- **Soft delete for products** (`active=false`) instead of hard delete, so
  historical orders still reference a valid product row.
- Farmers can only edit/delete their **own** listings — enforced server-side
  in `ProductService`, not just hidden in the UI.

## 9. Extending this further

Ideas if you want to keep building:
- Image upload (S3/local disk) instead of image URLs
- Ratings & reviews per farmer/product
- Payment gateway integration (Razorpay/Stripe) at checkout
- Admin panel for moderating listings and users
- Email/SMS notifications on order status changes
