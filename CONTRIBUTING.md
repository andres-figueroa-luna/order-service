# Contributing Guidelines

Welcome to this project! To maintain a clean, professional, and high-quality codebase, we follow the **Conventional Commits** specification and standard software engineering practices.

## 📝 Commit Message Standard

We use the [Conventional Commits](https://www.conventionalcommits.org/) specification. This leads to more readable messages that are easy to follow when looking through the project history and helps with automated versioning.

The commit message should be structured as follows:

```text
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### 🏆 Commit Types

Use one of the following types to categorize your changes:

*   **`feat`**: A new feature for the user.
    *   *Example:* `feat: add cancel order endpoint`
*   **`fix`**: A bug fix.
    *   *Example:* `fix: resolve Kafka deserialization error`
*   **`docs`**: Documentation only changes.
    *   *Example:* `docs: update Swagger annotations`
*   **`test`**: Adding missing tests or correcting existing tests.
    *   *Example:* `test: add integration test for stock update`
*   **`refactor`**: A code change that neither fixes a bug nor adds a feature (improving structure).
    *   *Example:* `refactor: migrate to SKU as business identifier`
*   **`style`**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc).
    *   *Example:* `style: format OrderController`
*   **`chore`**: Other changes that don't modify src or test files (e.g., updating dependencies, Maven config).
    *   *Example:* `chore: update Spring Boot to version 4.1.2`
*   **`ci`**: Changes to our CI configuration files and scripts (GitHub Actions).
    *   *Example:* `ci: add maven dependency submission action`
*   **`build`**: Changes that affect the build system or external dependencies.
    *   *Example:* `build: add validation starter to pom.xml`
*   **`perf`**: A code change that improves performance.
    *   *Example:* `perf: add database indexes to orders table`

### 💎 Best Practices for Commits

1.  **Use the Imperative Mood**: Write the description as if you are giving a command.
    *   ✅ **Correct:** `feat: add user login`
    *   ❌ **Incorrect:** `feat: added user login` or `feat: adds user login`
2.  **Use Lowercase**: Start the description with a lowercase letter.
3.  **No Period**: Do not end the description with a period.
    *   ✅ **Correct:** `fix: handle null pointer`
    *   ❌ **Incorrect:** `fix: Handle null pointer.`
4.  **Use the Body**: If the change is complex, use the commit body (`-m` flag in Git) to explain *why* the change was made, not just *what* changed.
    *   *Example:*
        ```bash
        git commit -m "refactor: migrate to SKU as business identifier" -m "- Replaced productId with sku in all Kafka event DTOs.
        - Updated OrderRequest, OrderResponse, and Order entity.
        - Fixed integration tests to align with the new domain model."
        ```

## 🚀 Local Development Workflow

Before pushing any code, ensure that your local environment matches the CI/CD pipeline to prevent build failures.

1.  Ensure **Rancher Desktop** (or Docker) is running.
2.  Run the full verification command locally:
    ```bash
    ./mvnw clean verify
    ```
3.  If the build is successful (`BUILD SUCCESS`) and all tests pass (green), you are ready to push.

## 🧪 Testing Standards

*   **Unit Tests:** For pure business logic (using Mockito).
*   **Integration Tests:** For repository, database, and Kafka interactions (using Testcontainers). Every new feature or endpoint **must** include an integration test.
*   **No "Magic Strings":** Always use Enums or constants for states and event topics.

## 🛡️ Code Quality & Architecture

*   **DTOs over Entities:** Never expose JPA entities directly in REST controllers. Always map them to Request/Response DTOs.
*   **Custom Exceptions:** Use domain-specific exceptions (e.g., `ResourceNotFoundException`, `BusinessRuleException`) and handle them globally via `@RestControllerAdvice`.
*   **Idempotency:** All Kafka consumers must implement idempotency checks to prevent duplicate message processing.
*   **Resilience:** Implement Dead Letter Queues (DLQ) for message brokers.

---
*Thank you for contributing and maintaining high engineering standards!*