-- Ejemplo de consulta para inspeccionar pagos y sagas
SELECT * FROM payments ORDER BY created_at DESC;
SELECT * FROM saga_instances ORDER BY created_at DESC;
SELECT event_type, payload, created_at FROM event_store ORDER BY created_at DESC;
SELECT event_type, published, created_at, published_at FROM outbox_events ORDER BY created_at DESC;
