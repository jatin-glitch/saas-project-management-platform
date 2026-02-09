# Backend Architecture Overview

This SaaS platform backend is organized as a Maven multi-module project. The parent aggregator (`backend/pom.xml`) manages common versions, dependency management, and shared plugins. Each bounded context is implemented as an independent Spring Boot service with its own module.

## Module Outline

| Module | Responsibility |
| --- | --- |
| `common-lib` | Shared domain primitives, DTOs, and utility classes reused by services. Contains no Spring Boot application class. |
| `auth-service` | Authentication, authorization, user management, and OAuth flows. |
| `project-service` | Core project/workspace entities, billing linkage, and tenant metadata. |
| `notification-service` | Email/SMS/in-app notification orchestration and delivery. |
| `gateway` | Edge service acting as API gateway + global filters, rate limiting, and routing. |

## Standard Package Structure

Every Spring Boot service module (e.g., `auth-service`, `project-service`, `notification-service`, `gateway`) follows the same base package convention: `com.saas.<service>`.

```
com.saas.<service>
├── application    # Application services, orchestrators, use-cases
├── config         # Spring @Configuration classes, security, OpenAPI
├── domain         # Aggregate roots, entities, value objects, domain events
├── infrastructure # Adapters to external systems (DB, messaging, HTTP)
├── interfaces
│   ├── rest       # REST controllers, request/response dto mapping
│   └── messaging  # Message consumers/producers (Kafka/SQS/etc)
└── Application.java  # Spring Boot entry point
```

`common-lib` opts into `com.saas.common` and mirrors domain layers relevant to shared models. It exposes:

```
com.saas.common
├── dto           # Cross-service DTO contracts
├── error         # Error codes, exceptions
├── security      # Security-related helpers (e.g., JwtPrincipal)
└── util          # Reusable utilities (clock, id generator contracts)
```

## Testing Layout

```
src
├── main
│   ├── java
│   └── resources
└── test
    ├── java
    └── resources
```

Unit tests mirror the main package tree. Integration and slice tests are grouped under `...interfaces.rest`, `...infrastructure`, etc.

## Build & Dependency Guidelines

1. **Parent BOM**: `backend/pom.xml` imports Spring Boot + Spring Cloud BOMs to guarantee aligned versions.
2. **Lombok & MapStruct**: Add to parent dependency management when used (not yet included).
3. **Configuration**: Each service keeps its own `application.yml` scoped to module; secrets handled via profiles.
4. **Inter-service contracts**: Only `common-lib` should be used for shared contracts. No service should depend directly on another service module to preserve loose coupling.

## Next Steps

1. Scaffold Maven sub-module `pom.xml` files.
2. Add skeletal Spring Boot applications + package directories per module.
3. Configure build tooling (test containers, docker) as the platform evolves.
