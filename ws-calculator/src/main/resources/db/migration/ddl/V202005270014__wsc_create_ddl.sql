DROP TABLE IF EXISTS eg_wsc_meterreading;
CREATE TABLE eg_wsc_meterreading
(
  id character varying(64),
  connectionNo character varying(64),
  billingPeriod character varying(64) NOT NULL,
  meterStatus character varying(64) NOT NULL,
  lastReading decimal NOT NULL,
  lastReadingDate bigint NOT NULL,
  currentReading decimal NOT NULL,
  currentReadingDate bigint NOT NULL,
  consumption decimal,
  CONSTRAINT uk_meterreading UNIQUE (id)
);