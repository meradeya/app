# ADR 002: Redis Caching Strategy for Listings API

- Status: Accepted

## Context and Problem Statement

The Listings API is highly read-intensive, with endpoints like fetching an individual listing by ID and loading the static category tree being invoked frequently. To ensure the application scales under load and provides low-latency responses, a robust caching mechanism is necessary.

## Decision Outcome

We decided to use Redis as a centralized cache and implement the Cache-Aside pattern using Spring's `@Cacheable` abstraction.

To maintain clean architecture, we adopted the Decorator Pattern. We created a `CachedListingServiceImpl` that wraps the `DefaultListingServiceImpl`. This separates the caching infrastructure logic from the core business and repository logic. We also introduced a configuration toggle (`app.cache.listings.enabled`) to easily switch the `@Primary` service bean, allowing caching to be turned on or off via properties.

We also added per-cache TTL configuration (`app.cache.listings.ttl` and `app.cache.categories.ttl`) and a top-level `app.cache.enabled` switch to disable the entire caching layer when needed. A configurable key prefix (`app.cache.key-prefix`) keeps Redis keys namespaced across environments. Cache operations are configured to fail open (log + continue) if Redis is unavailable, so read/write requests are not blocked by transient cache outages. The listing detail cache uses `sync=true` to reduce stampede under high contention.

## Alternatives Considered

* Modifying the existing implementation directly with `@Cacheable` annotations - Rejected because it tightly couples caching concerns with core business logic. It also makes it difficult to disable caching entirely for testing or operational reasons without recompiling.
* Using an in-memory local cache (e.g., Caffeine) - Rejected because it does not scale horizontally. As we deploy multiple instances of the application, a local cache would lead to inconsistent states and lower cache hit rates compared to a distributed cache.

## Consequences

**Positive:**

- Read latency for listings and the category tree is significantly improved.
- Clean separation of concerns is maintained via the Decorator Pattern.
- Caching can be toggled without changing the code, providing operational flexibility.

**Negative:**

- Added infrastructure complexity, as we now depend on a Redis instance for optimal performance.
- Potential cache consistency edge cases requiring explicit `@CacheEvict` rules on all mutating operations.
