# Use OpenAPI Generator Plugin for Contract-First Development

- Status: Rejected

## Context and Problem Statement

The project needed a consistent contract between the API specification and the Java implementation.
The `openapi-generator-gradle-plugin` was evaluated as a contract-first approach: define the API in
an OpenAPI YAML file and generate Java controller interfaces and DTOs from it automatically.

## Decision Outcome

Chosen option: "Abandon the OpenAPI generator plugin", because the generated code introduced more
friction than value for a project of this scale, and compatibility with Spring Boot 4 was
unreliable.

## Alternatives Considered

* **Keep the plugin** — rejected because generated controller interfaces and DTOs were overly
  verbose, difficult to customize, and conflicted with Lombok annotations, requiring constant
  workarounds.

## Consequences

**Positive:**

- Codebase is cleaner with no generated source files to maintain or exclude from version control.
- Full control over DTO structure, validation annotations, and naming conventions.

**Negative:**

- The OpenAPI spec is now derived from code rather than driving it; drift between spec and
  implementation becomes the developer's responsibility.
- More boilerplate to write manually.
