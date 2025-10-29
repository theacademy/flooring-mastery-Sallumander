# Copilot instructions for Flooring Mastery (concise)

This project is a small Spring Boot console MVC application that stores domain data in CSV-like files under `FileData/` (no DB). Use these notes to be immediately productive when changing code.

- Big picture
  - Entry: `com.flooring.flooringmastery.FlooringMasteryApp` (implements CommandLineRunner). Spring Boot wires components.
  - Controller (`controller/Controller.java`) implements the interactive menu and delegates to the Service layer.
  - Service (`service/ServiceLayerImpl.java`) contains business logic and uses DAOs for persistence and Audit for simple logging.
  - DAOs (`dao/*FileImpl.java`) are file-based and read/write from `FileData/Data/*` and `FileData/Orders/Orders_MMDDYYYY.txt`.

- Important repo-specific behaviors and patterns
  - File format: comma-separated, header line present and intentionally removed when loading. Keep header when writing files.
  - Orders filenames use MMddyyyy (e.g. `Orders_08212025.txt`). The code uses DateTimeFormatter.ofPattern("MMddyyyy").
  - Product types are stored as lowercase keys in `ProductDaoFileImpl`.
  - State abbreviations are uppercased in `TaxDaoFileImpl` and the service expects uppercase for lookups.
  - User input parsing is performed by `view/UserIOConsoleImpl.java` — dates are parsed with `LocalDate.parse(...)` from the string format `YYYY-MM-DD`.

- Common developer workflows
  - Build: `mvn clean package` (Java 17). The project uses Spring Boot parent and `spring-boot-maven-plugin`.
  - Run locally: `mvn spring-boot:run` or `java -jar target/flooring-mastery-1.0-SNAPSHOT.jar` after packaging.
  - Tests: `mvn test` (JUnit 5 / spring-boot-starter-test). Add test fixtures under a temporary directory or mock DAOs — DAOs read from `FileData/` by default.

- Things to watch for when editing code (examples)
  - Persistence is file-based and stateful in-memory: DAOs cache data in maps and write files with `Files.write(...)`. When changing DAO behavior, preserve header + order of columns.
  - Edit flow in `Controller.editOrder()` replaces an existing order by calling `service.removeOrder(date, orderNum)` then `service.addOrder(date, newOrder)`. This is not transactional: ensure `removeOrder` returns the removed Order and that `AuditDao` is injected (otherwise you may delete the file entry and fail before re-adding the new one).
    - Example: `OrderDaoFileImpl.removeOrder` MUST return the removed Order object (not `null`) so the service can confirm deletion.
  - Product lookups use `product.getProductType().toLowerCase()` as the DAO key — keep case handling consistent.

- Integration points / extension notes
  - Audit: `AuditDao` exists and a simple `AuditDaoFileImpl` is included; annotate it as a Spring bean to be injected into `ServiceLayerImpl` if you need audit behavior.
  - Export functionality and some service methods are stubs (e.g. `exportData()`) — extend carefully and follow existing DAO file conventions.

- Quick fixes & diagnostics
  - If edits to orders disappear or old orders are deleted without replacement: inspect `OrderDaoFileImpl.removeOrder` and `ServiceLayerImpl.removeOrder` for return values and null/exception handling (this project expects the DAO to return the removed Order and a non-null `auditDao`).
  - If tests fail due to missing files, create `FileData/Data/Products.txt` and `Taxes.txt` and example `FileData/Orders/Orders_MMDDYYYY.txt` files used by the app/test harness.

If anything in these notes is unclear or you want me to add runnable examples (sample `FileData/` fixtures or a test that demonstrates the edit flow), tell me which part to expand and I will iterate.
