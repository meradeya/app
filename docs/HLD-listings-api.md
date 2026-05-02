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

Three dedicated endpoints serve different discovery intents:

```
1. Feed (passive discovery)
   → GET /v1/feed
   → Always ACTIVE listings, ordered by created_at DESC
   → Auth optional — personalised ranking planned for a future iteration
   → Pagination via page/size query params

2. Search (intent-driven)
   → POST /v1/listings/search
   → Always ACTIVE listings only
   → Filters: keyword (FTS), category, condition, price range, sellerId
   → sellerId filter enables seller profile pages (shows their public listings)
   → Pagination via page/size/sort query params

3. Listing detail
   → GET /v1/listings/{id}
   → Full detail including photos, seller info, attributes
```

**Why separate feed and search?**  
Search is *intent-driven* (buyer knows what they want — filtering and ranking by relevance).
A feed is *passive discovery* (buyer is browsing �� ordered by recency or personalisation score).
These will diverge in implementation; separating them now avoids a breaking contract change when a
recommender is plugged in.

**Why POST for search?**  
Filter bodies can be complex (keyword + category + price range). POST avoids URL length limits and
keeps the filter contract versioned in the request body schema, consistent with industry practice
(Elasticsearch, Algolia).

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
  → POST /v1/listings/{id}/publish 

Seller marks item as sold / permanently removes:
  → POST /v1/listings/{id}/delete
```

> There is no `DELETE /v1/listings/{id}`. Removal is always expressed as an explicit action endpoint
> so the intent is unambiguous and future per-action authorisation rules are easy to add.

---

### 2.4 Seller Views Their Own Listings

```
Seller opens "My Listings" dashboard
  → GET /v1/listings/own                     (all statuses by default)
  → GET /v1/listings/own?status=DRAFT        (filter to a specific status)
```

> The caller's identity is the implicit sellerId filter — no need to pass it explicitly.
> All statuses (`DRAFT`, `ACTIVE`, `ARCHIVED`, `DELETED`) are accessible.

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

| Method   | Path                                        | Auth Required | Who Can Call | Description                        |
|----------|---------------------------------------------|:-------------:|--------------|------------------------------------|
| `GET`    | `/v1/feed`                                  |       ❌       | Anyone       | Recency feed (ACTIVE only)         |
| `POST`   | `/v1/listings/search`                       |       ❌       | Anyone       | Search ACTIVE listings             |
| `GET`    | `/v1/listings/own`                          |       ✅       | Owner        | Own listings, all statuses         |
| `POST`   | `/v1/listings`                              |       ✅       | Seller       | Create a listing in DRAFT          |
| `GET`    | `/v1/listings/{listingId}`                  |       ❌       | Anyone       | View listing detail                |
| `PATCH`  | `/v1/listings/{listingId}`                  |       ✅       | Owner        | Update listing fields              |
| `POST`   | `/v1/listings/{listingId}/publish`          |       ✅       | Owner        | DRAFT\|ARCHIVED → ACTIVE           |
| `POST`   | `/v1/listings/{listingId}/archive`          |       ✅       | Owner        | ACTIVE → ARCHIVED                  |
| `POST`   | `/v1/listings/{listingId}/delete`           |       ✅       | Owner        | ACTIVE\|ARCHIVED → DELETED         |
| `POST`   | `/v1/listings/{listingId}/photos`           |       ✅       | Owner        | Upload photo (multipart)           |
| `DELETE` | `/v1/listings/{listingId}/photos/{photoId}` |       ✅       | Owner        | Delete photo                       |
| `GET`    | `/v1/categories`                            |       ❌       | Anyone       | Fetch category tree                |

### 4.2 Visibility Rules

| Listing Status | Guest | Owner | Other authenticated user |
|----------------|:-----:|:-----:|:------------------------:|
| `DRAFT`        | ❌ 404 |   ✅   |          ❌ 404           |
| `ACTIVE`       |   ✅   |   ✅   |            ✅             |
| `ARCHIVED`     | ❌ 404 |   ✅   |          ❌ 404           |
| `DELETED`      | ❌ 404 | ❌ 404 |          ❌ 404           |


### 4.3 Search Behaviour

`POST /v1/listings/search` always returns `ACTIVE` listings. Filters:

| Filter              | Type    | Description                                                         |
|---------------------|---------|---------------------------------------------------------------------|
| `q`                 | string  | Full-text keyword search (PostgreSQL `tsvector`)                    |
| `categoryId`        | UUID    | Exact category match                                                |
| `condition`         | enum    | `NEW`, `LIKE_NEW`, `GOOD`, `FAIR`, `POOR`                           |
| `minPrice` / `maxPrice` | decimal | Price range                                                    |
| `sellerId`          | UUID    | Filter to a specific seller — used for seller profile pages         |

Pagination via query params: `?page=0&size=20&sort=createdAt,desc`.  
When `q` is provided, results are ranked by full-text relevance.

### 4.4 Own Listings

`GET /v1/listings/own` — requires authentication.

| Parameter  | Type          | Description                                              |
|------------|---------------|----------------------------------------------------------|
| `status`   | enum (optional) | Filter by status; omit to return all statuses          |
| `page`, `size`, `sort` | — | Standard pagination query params                  |

Returns all statuses (`DRAFT`, `ACTIVE`, `ARCHIVED`, `DELETED`) for the authenticated caller.

## 5. Photo Storage Strategy

Photos are uploaded as `multipart/form-data` and stored on the **local filesystem** in this phase.

```
Upload flow:
  POST /v1/listings/{id}/photos
    → reject with 422 if listing status is DELETED
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



