```markdown
# waynboot-mall Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill teaches you the core development patterns and conventions used in the `waynboot-mall` TypeScript codebase. You'll learn about file naming, import/export styles, commit message conventions, and how to write and run tests. This guide ensures consistency and efficiency when contributing to the project.

## Coding Conventions

### File Naming
- **Pattern:** PascalCase  
  Example:  
  ```
  ProductList.ts
  UserProfile.ts
  ```

### Import Style
- **Pattern:** Relative imports  
  Example:  
  ```typescript
  import { Product } from './Product';
  import { getUser } from '../utils/user';
  ```

### Export Style
- **Pattern:** Named exports  
  Example:  
  ```typescript
  // In Product.ts
  export const Product = { ... };
  export function getProductById(id: string) { ... }
  ```

### Commit Messages
- **Pattern:** Conventional commits with `docs` prefix  
  Example:  
  ```
  docs: update README with new setup instructions
  ```

## Workflows

### Writing Documentation
**Trigger:** When updating or adding documentation  
**Command:** `/write-docs`

1. Make changes or additions to documentation files (e.g., `README.md`).
2. Use a conventional commit message with the `docs` prefix.
   ```
   docs: add installation section to README
   ```
3. Push your changes to the repository.

### Adding or Modifying Code
**Trigger:** When implementing new features or fixing bugs  
**Command:** `/update-code`

1. Create new files using PascalCase naming.
2. Use relative imports for dependencies.
3. Export functions, constants, or classes using named exports.
4. Write or update corresponding tests (see Testing Patterns).
5. Commit changes with a descriptive message.

### Writing Tests
**Trigger:** When adding new features or refactoring existing code  
**Command:** `/write-tests`

1. Create test files using the pattern `*.test.*` (e.g., `Product.test.ts`).
2. Write tests for each exported function or component.
3. Use the project's testing framework (unknown; check project docs or ask a maintainer).
4. Run tests to ensure correctness.

## Testing Patterns

- **Test File Pattern:** `*.test.*`  
  Example:  
  ```
  Product.test.ts
  UserProfile.test.ts
  ```
- **Framework:** Unknown (check project documentation or ask a maintainer).
- **Best Practice:** Write tests for each exported function or module. Place tests alongside or in a dedicated `tests` directory.

## Commands
| Command        | Purpose                                             |
|----------------|-----------------------------------------------------|
| /write-docs    | Start the workflow for updating documentation       |
| /update-code   | Workflow for adding or modifying code               |
| /write-tests   | Workflow for writing and running tests              |
```
