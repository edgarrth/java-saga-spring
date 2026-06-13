CREATE TABLE payments (
  payment_id UUID PRIMARY KEY,
  amount NUMERIC(18,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  mode VARCHAR(30) NOT NULL,
  status VARCHAR(40) NOT NULL,
  funds_reserved BOOLEAN NOT NULL DEFAULT FALSE,
  fraud_approved BOOLEAN,
  settlement_captured BOOLEAN NOT NULL DEFAULT FALSE,
  failure_reason VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE saga_instances (
  saga_id UUID PRIMARY KEY,
  payment_id UUID NOT NULL,
  saga_type VARCHAR(40) NOT NULL,
  status VARCHAR(40) NOT NULL,
  current_step VARCHAR(80) NOT NULL,
  compensation_executed BOOLEAN NOT NULL DEFAULT FALSE,
  failure_reason VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_store (
  event_id UUID PRIMARY KEY,
  aggregate_id UUID NOT NULL,
  aggregate_type VARCHAR(80) NOT NULL,
  event_type VARCHAR(120) NOT NULL,
  payload TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE outbox_events (
  outbox_id UUID PRIMARY KEY,
  aggregate_id UUID NOT NULL,
  event_type VARCHAR(120) NOT NULL,
  topic VARCHAR(120) NOT NULL,
  payload TEXT NOT NULL,
  published BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_at TIMESTAMP
);

CREATE INDEX idx_outbox_unpublished ON outbox_events(published, created_at);
CREATE INDEX idx_events_aggregate ON event_store(aggregate_id, created_at);
