# Datasets

Esta carpeta contiene scripts de apoyo para la PoC.

## Archivos

- `preload-data.sql`: script montado por Docker en PostgreSQL. No inserta datos porque las tablas las crea Flyway al iniciar la aplicación.
- `sample-events.sql`: consultas para inspeccionar pagos, sagas, event store y outbox.

## Uso

1. Levanta infraestructura:

```bash
cd infraestructura/docker
docker compose up -d
```

2. Ejecuta la aplicación:

```bash
mvn spring-boot:run
```

3. Crea pagos usando los requests de `infraestructura/request-response`.

4. Ejecuta consultas:

```bash
docker exec -it saga-postgres psql -U payments -d payments_db -f /docker-entrypoint-initdb.d/20-preload-data.sql
```

Para consultas manuales:

```bash
docker exec -it saga-postgres psql -U payments -d payments_db
```

Luego copia las consultas de `sample-events.sql`.
