# Code Style

Concise reference of conventions used in this codebase. 

---

## Naming

- **Entity classes** — singular: `Listing`, `Category`, `User`
- **Packages** — singular: `controller`, `service`, `dto`, `domain`, `exception`
- **Database tables** — plural snake_case: `listings`, `categories`, `listing_photos`

---

## Database Migrations

- Flyway migrations must be idempotent.

---

## Logging

- Entry and exit of every public service method must be logged at `INFO` level.

