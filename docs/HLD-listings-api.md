# HLD — Listings API

> **Scope:** High-level design of the Listings domain — business flows, API surface, security model,
> data ownership, and storage strategy.

---

## 1. Business Context

### 1.1 Actors

| Actor      | Description                                                    |
|------------|----------------------------------------------------------------|
| **Guest**  | Unauthenticated visitor. Can browse and view listings only.    |
| **Buyer**  | Authenticated user browsing and saving listings.               |
| **Seller** | Authenticated user who creates and manages their own listings. |
| **Admin**  | Platform operator. Out of scope for this API version.          |

## 2. Core Business Flows

### 2.1 Seller Creates and Publishes a Listing

```
Seller fills in listing form
  → POST /v1/listings          (creates listing in DRAFT status)
  → Seller uploads photos      (POST /v1/listings/{id}/photos, up to 10)
  → Seller reviews draft
  → POST /v1/listings/{id}/publish   ← publishes it
  → Listing is now discoverable by Buyers
```

**Why DRAFT first?**  
Sellers should not accidentally expose an incomplete listing. The two-step flow (create → publish)
gives them a review window.

---

### 2.2 Buyer Discovers Listings

```
Buyer opens the app home screen
  → GET /v1/feed               (personalised feed — ACTIVE listings, recency + category bias)
  → Paginated via cursor (not page/offset — feed is append-driven)
  → Buyer performs an explicit search
  → POST /v1/listings/search   (keyword, category, price, condition filters)
  → Always returns ACTIVE listings only for public callers
  → Buyer clicks a card
  → GET /v1/listings/{id}      (full detail — photos, description, seller info)
```

**Why POST for search?**  
A search request can carry complex filter bodies (nested categories, price range, keyword). Using
POST avoids URL length limits and is consistent with industry practice for rich search APIs (
Elasticsearch, Algolia). It also keeps the filter contract versioned in the request body schema.

**Why a separate feed endpoint?**  
Search is *intent-driven* (the user knows what they want). A feed is *passive discovery* (the user
is browsing). These are different UX modes that will diverge in implementation — the feed will
eventually be backed by a recommendation engine, while search stays query-driven. Separating them
now avoids a breaking contract change later.

**v1 feed implementation:** Returns newest `ACTIVE` listings, optionally biased toward the
authenticated user's most recently browsed categories. Auth is optional — guests receive a generic
recency feed. The endpoint contract is designed to be stable when a real recommender is plugged in.

**Why cursor pagination on the feed?**  
Page/offset pagination is unstable on a live feed: new listings inserted between requests shift
pages, causing duplicates or gaps. A cursor (`lastSeenId` / `lastSeenCreatedAt`) gives a consistent
window.

---

### 2.3 Seller Manages an Active Listing

```
Seller edits listing details:
  → PATCH /v1/listings/{id}    (update title, price, description, etc.)

Seller adds/removes photos:
  → POST   /v1/listings/{id}/photos           (upload new photo)
  → DELETE /v1/listings/{id}/photos/{photoId} (remove photo)

Seller takes listing off-market temporarily:
  → POST /v1/listings/{id}/archive

Seller re-activates an archived listing:
  → POST /v1/listings/{id}/republish

Seller marks item as sold / permanently removes:
  → POST /v1/listings/{id}/delete
```

> There is no `DELETE /v1/listings/{id}`. Removal is always expressed as an explicit action endpoint
> so the intent is unambiguous and future per-action authorisation rules are easy to add.

---

### 2.4 Seller Views Their Own Listings

```
Seller opens "My Listings" dashboard
  → POST /v1/listings/search  { sellerId: <own id>, status: <optional filter> }
  → Authenticated caller filtering own sellerId may request any status
  → Returns listings filtered by requested status (or all statuses if omitted)
```

> The `status` filter in search is **access-controlled** (see §4.3). Only the authenticated owner
> of the filtered `sellerId` may request non-ACTIVE statuses. Public callers always see ACTIVE only.

---

### 2.5 Category Browsing

```
App loads category picker / browse page
  → GET /v1/categories         (returns full tree: root categories + children)
  → Displayed as hierarchical menu
  → Selected category passed as filter to POST /v1/listings/search
```

Categories are read-only from the API perspective. Seeded via database migration. Managed by
platform admins via direct DB access or a future admin API.

---

## 3. Listing Lifecycle (Status State Machine)


| From       | To         | Who can trigger | Notes                            |
|------------|------------|-----------------|----------------------------------|
| `DRAFT`    | `ACTIVE`   | Seller (owner)  | Listing becomes publicly visible |
| `ACTIVE`   | `ARCHIVED` | Seller (owner)  | Listing hidden from search       |
| `ARCHIVED` | `ACTIVE`   | Seller (owner)  | Re-lists without re-creating     |
| `ACTIVE`   | `DELETED`  | Seller (owner)  | Item sold or withdrawn           |
| `ARCHIVED` | `DELETED`  | Seller (owner)  | Permanent removal                |

Any transition not listed above is **rejected with HTTP 422**.

---

## 4. API Surface

### 4.1 Endpoint Reference

| Method   | Path                                        | Auth Required | Who Can Call | Description                 |
|----------|---------------------------------------------|:-------------:|--------------|-----------------------------|
| `GET`    | `/v1/feed`                                  |       ❌       | Anyone       | Personalised / recency feed |
| `POST`   | `/v1/listings`                              |       ✅       | Seller       | Create a listing in DRAFT   |
| `POST`   | `/v1/listings/search`                       |       ❌       | Anyone       | Search/browse listings      |
| `GET`    | `/v1/listings/{listingId}`                  |       ❌       | Anyone       | View listing detail         |
| `PATCH`  | `/v1/listings/{listingId}`                  |       ✅       | Owner        | Update listing fields       |
| `POST`   | `/v1/listings/{listingId}/publish`          |       ✅       | Owner        | DRAFT → ACTIVE              |
| `POST`   | `/v1/listings/{listingId}/archive`          |       ✅       | Owner        | ACTIVE → ARCHIVED           |
| `POST`   | `/v1/listings/{listingId}/republish`        |       ✅       | Owner        | ARCHIVED → ACTIVE           |
| `POST`   | `/v1/listings/{listingId}/delete`           |       ✅       | Owner        | ACTIVE\|ARCHIVED → DELETED  |
| `POST`   | `/v1/listings/{listingId}/photos`           |       ✅       | Owner        | Upload photo (multipart)    |
| `DELETE` | `/v1/listings/{listingId}/photos/{photoId}` |       ✅       | Owner        | Delete photo                |
| `GET`    | `/v1/categories`                            |       ❌       | Anyone       | Fetch category tree         |

### 4.2 Visibility Rules

| Listing Status | Guest | Owner | Other authenticated user |
|----------------|:-----:|:-----:|:------------------------:|
| `DRAFT`        | ❌ 404 |   ✅   |          ❌ 404           |
| `ACTIVE`       |   ✅   |   ✅   |            ✅             |
| `ARCHIVED`     | ❌ 404 |   ✅   |          ❌ 404           |
| `DELETED`      | ❌ 404 | ❌ 404 |          ❌ 404           |


### 4.3 Search Behaviour

`POST /v1/listings/search` accepts:

| Filter                       | Type    | Description                                                         |
|------------------------------|---------|---------------------------------------------------------------------|
| `q`                          | string  | Full-text keyword search (PostgreSQL `tsvector`)                    |
| `categoryId`                 | UUID    | Exact category match                                                |
| `condition`                  | enum    | `NEW`, `LIKE_NEW`, `GOOD`, `FAIR`, `POOR`                           |
| `minPrice` / `maxPrice`      | decimal | Price range                                                         |
| `sellerId`                   | UUID    | Filter to a specific seller (used for "My Listings" dashboard)      |
| `status`                     | enum    | Status filter — see access rules below                              |
| `page` / `pageSize` / `sort` | —       | Standard pagination (`newest`, `oldest`, `price_asc`, `price_desc`) |

**Status visibility enforcement in search:**

| Caller                                     | Allowed `status` values                               |
|--------------------------------------------|-------------------------------------------------------|
| Unauthenticated (guest)                    | `ACTIVE` only — any other value is ignored            |
| Authenticated, `sellerId` = own id         | Any status (`DRAFT`, `ACTIVE`, `ARCHIVED`, `DELETED`) |
| Authenticated, `sellerId` = other / absent | `ACTIVE` only — any other value is ignored            |

> This prevents accidental exposure of hidden listings through the public search endpoint. The
> enforcement is silent (ignore, not reject) to avoid leaking whether hidden listings exist.

When `q` is provided, results are ranked by full-text relevance. Otherwise sorted by the `sort`
parameter.

### 4.4 Feed Behaviour

`GET /v1/feed` accepts:

| Parameter  | Type   | Description                                                   |
|------------|--------|---------------------------------------------------------------|
| `cursor`   | string | Opaque cursor from previous response (`lastSeenId:createdAt`) |
| `pageSize` | int    | Number of items to return (default 20)                        |

- Always returns `ACTIVE` listings only.
- Auth optional: authenticated users receive a feed biased toward their recently browsed categories.
- Response includes a `nextCursor` field; `null` means end of feed.


## 5. Photo Storage Strategy

Photos are uploaded as `multipart/form-data` and stored on the **local filesystem** in this phase.

```
Upload flow:
  POST /v1/listings/{id}/photos
    → validate MIME type (jpeg / png / webp) + file size (≤ 10 MB)
    → enforce 10-photo limit
    → save to:  {uploadDir}/listings/{listingId}/{uuid}.{ext}
    → store URL: /media/listings/{listingId}/{uuid}.{ext}
    → served via Spring MVC static resource handler at /media/**

Delete flow:
  DELETE /v1/listings/{id}/photos/{photoId}
    → remove DB record (orphan removal)
    → delete physical file from disk
    → re-normalise display_order of remaining photos
```

**Migration path to cloud storage (S3 / GCS):**  
Only the `PhotoStorageService` implementation needs to change. The stored URL changes from a
server-relative path to an absolute CDN URL. No schema or API contract changes required.

---

## 6. Security Model

### 6.1 Authentication

All write operations require a valid **JWT Bearer token** in the `Authorization` header.

### 6.2 Authorisation

- `sellerId` extracted from `@AuthenticationPrincipal`
- Ownership check on mutating operations: `listing.getSeller().getId().equals(callerId)` → **403**
  if not owner
- Status visibility enforced per the table in §4.2

## 7. Error Handling

| Scenario                              | HTTP | Notes                                         |
|---------------------------------------|------|-----------------------------------------------|
| Resource not found                    | 404  | Listing / Category / Photo                    |
| Action endpoint called in wrong state | 422  | e.g. `POST /archive` on a DRAFT listing       |
| Bean Validation failure               | 400  | Field-level error details in response body    |
| Optimistic lock conflict              | 409  | Concurrent edit; client must re-fetch + retry |
| Photo limit exceeded                  | 422  | Returns configured max (default 10)           |
| Unsupported MIME / oversized file     | 400  | Validated before touching the filesystem      |
| File storage failure                  | 500  | Logged server-side; generic message to client |

---

## 8. Non-Functional Considerations

| Concern                | Approach                                                                               |
|------------------------|----------------------------------------------------------------------------------------|
| **Pagination**         | All list/search responses are paginated (default 20)                          |
| **Optimistic locking** | `@Version` on `Listing`; version echoed in API responses for safe PATCH                |
| **Full-text search**   | PostgreSQL `tsvector` column + `plainto_tsquery`; updated by DB trigger on write       |
| **Photo ordering**     | `display_order` (0-indexed) maintained; re-normalised on delete                        |
| **Currency**           | Stored as ISO-4217 string; default `MDL`. No conversion in this phase.                 |
| **Soft deletes**       | No physical row deletion via API. Status transitions preserve referential integrity.   |
| **Category tree**      | Expected < 500 nodes; loaded fully and assembled in Java (no recursive SQL CTE needed) |



