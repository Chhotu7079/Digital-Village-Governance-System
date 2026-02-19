# Default reading scope (DVGS)

When reading/understanding this repository, **exclude build outputs and runtime noise** and focus on source/config/docs.

## Exclude (do not read)
These folders/files are generated artifacts or runtime noise and are not useful for understanding the maintainable code:

- `**/target/**` (Maven build output: compiled classes, surefire reports, generated sources)
- `**/build/**`
- `**/dist/**`
- Logs/dumps (e.g., `*.log`, `*.dump`, `*.dumpstream`, `logs/` folders)
- Other common generated folders (if present later):
  - `**/.next/**`, `**/node_modules/**`, `**/__pycache__/**`, `**/.venv/**`

## Include (read these)
These are the files that define the real system behavior and are the correct sources to study:

### Documentation
- `README.md`, `REQUIREMENTS.md`, `PROJECT_DESCRIPTION.md`

### Build / Dependencies
- `pom.xml` files

### Source code
- `src/main/java/**`
- `src/main/resources/**` (especially `application.yml`, templates, config)
- `src/test/java/**` (tests describe expected behavior)

### Database schema
- `src/main/resources/db/migration/**` (Flyway migrations)

### Infrastructure / DevOps
- `backend/infrastructure/docker-compose.yml`
- `backend/infrastructure/*.ps1`
- init SQL under `backend/infrastructure/postgres-init/**`

## Reasoning
Build outputs (`target/`, `dist/`, `build/`) are derived from source and can be regenerated. Reading them is slow and can be misleading.
The included files above are the authoritative, maintainable definitions of the system.
