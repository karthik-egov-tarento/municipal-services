DROP TABLE IF EXISTS eg_ws_plumberinfo;
DROP TABLE IF EXISTS eg_ws_applicationdocument;
DROP TABLE IF EXISTS water_service_connection;
DROP TABLE IF EXISTS connection;

CREATE TABLE connection
(
  id character varying(64) NOT NULL,
	tenantid character varying(250) NOT NULL,
  property_id character varying(64) NOT NULL,
  applicationno character varying(64),
  applicationstatus character varying(256),
  status character varying(64) NOT NULL,
  connectionno character varying(256),
  oldconnectionno character varying(64),
  roadCuttingArea FLOAT,
  action character varying(64),
  roadType character varying(32),
  adhocrebate numeric(12,2),
  adhocpenalty numeric(12,2),
  adhocpenaltyreason character varying(1024),
  adhocpenaltycomment character varying(1024),
  adhocrebatereason character varying(1024),
  adhocrebatecomment character varying(1024);
  CONSTRAINT connection_pkey PRIMARY KEY (id)
);

CREATE TABLE water_service_connection
(
  connection_id character varying(64) NOT NULL,
  connectioncategory character varying(32),
  rainwaterharvesting boolean,
  connectiontype character varying(32),
  watersource character varying(64),
  meterid character varying(64),
  meterinstallationdate bigint,
  pipeSize decimal,
  noOfTaps integer,
  connectionexecutiondate bigint,
  proposedpipesize decimal,
  proposedTaps integer,
  appCreatedDate bigint,
  initialmeterreading  numeric(12,2),
  detailsprovidedby character varying(256),
  CONSTRAINT water_service_connection_connection_id_fkey FOREIGN KEY (connection_id) REFERENCES connection (id) 
  ON UPDATE CASCADE
  ON DELETE CASCADE
);



CREATE TABLE public.eg_ws_plumberinfo
(
  id character varying(256) NOT NULL,
  name character varying(256),
  licenseno character varying(256),
  mobilenumber character varying(256),
  gender character varying(256),
  fatherorhusbandname character varying(256),
  correspondenceaddress character varying(1024),
  relationship character varying(256),
  wsid character varying(64),
  CONSTRAINT uk_eg_ws_plumberinfo PRIMARY KEY (id),
  CONSTRAINT fk_eg_ws_plumberinfo_connection_id FOREIGN KEY (wsid)
      REFERENCES public.connection (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE public.eg_ws_applicationdocument
(
  id character varying(64) NOT NULL,
  tenantid character varying(64),
  documenttype character varying(64),
  filestoreid character varying(64),
  wsid character varying(64),
  active character varying(64),
  documentUid character varying(64),
  createdby character varying(64),
  lastmodifiedby character varying(64),
  createdtime bigint,
  lastmodifiedtime bigint,
  CONSTRAINT uk_eg_ws_applicationdocument PRIMARY KEY (id),
  CONSTRAINT fk_eg_ws_applicationdocument_connection_id FOREIGN KEY (wsid)
      REFERENCES public.connection (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

