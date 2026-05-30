```markdown
# waynboot-mall Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill covers the core development patterns and conventions used in the `waynboot-mall` Java codebase. It details file naming, import/export styles, commit conventions, and testing patterns, providing practical examples and command suggestions for efficient collaboration and code maintenance.

## Coding Conventions

### File Naming
- **Style:** PascalCase
- **Example:**  
  ```java
  public class OrderService { ... }
  ```

### Import Style
- **Style:** Relative imports
- **Example:**  
  ```java
  import com.waynboot.mall.service.OrderService;
  ```

### Export Style
- **Style:** Named exports (public classes/methods)
- **Example:**  
  ```java
  public class ProductController { ... }
  ```

### Commit Messages
- **Convention:** Conventional Commits
- **Prefix:** `feat`
- **Example:**  
  ```
  feat: Add order cancellation endpoint
  ```

## Workflows

### Feature Development
**Trigger:** When adding a new feature  
**Command:** `/feature-dev`

1. Create a new branch for the feature.
2. Implement the feature using PascalCase for files/classes.
3. Use relative imports for dependencies.
4. Export new classes/methods as `public`.
5. Write or update corresponding test files (`*.test.*`).
6. Commit changes with a message starting with `feat:`.
7. Open a pull request for review.

### Testing
**Trigger:** Before merging or releasing code  
**Command:** `/run-tests`

1. Identify all test files matching the `*.test.*` pattern.
2. Run tests using the project's preferred test runner.
3. Ensure all tests pass before proceeding.

## Testing Patterns

- **Test File Pattern:** `*.test.*` (e.g., `OrderService.test.java`)
- **Framework:** Unknown (use the project's standard Java test runner)
- **Example:**  
  ```java
  public class OrderServiceTest {
      @Test
      public void testOrderCreation() {
          // test logic here
      }
  }
  ```

## Commands
| Command         | Purpose                                 |
|-----------------|-----------------------------------------|
| /feature-dev    | Start a new feature development workflow |
| /run-tests      | Run all test files before merging        |
```
