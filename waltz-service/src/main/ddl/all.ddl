create TABLE app_capability (
  application_id bigint not null,
  capability_id bigint not null,
  is_primary boolean not null default false,
  PRIMARY KEY (application_id, capability_id)
);
CREATE UNIQUE INDEX app_capability_pkey ON app_capability (application_id, capability_id)create TABLE application (
  id bigint not null default nextval('application_id_seq'::regclass),
name varchar(255),
  description varchar,
  aliases varchar[],
  asset_code varchar(255),
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now(),
  organisational_unit_id bigint not null,
  kind varchar(128) not null default 'IN_HOUSE'::character varying,
  lifecycle_phase varchar(128) not null default 'PRODUCTION'::character varying,
  parent_asset_code varchar(255),
PRIMARY KEY (id),
FOREIGN KEY (organisational_unit_id) REFERENCES organisational_unit (id)
);
CREATE UNIQUE INDEX application_pkey ON application (id);
CREATE UNIQUE INDEX unique_name ON application (name)create TABLE authoritative_source (
  id bigint not null default nextval('authoritative_source_id_seq'::regclass),
  parent_kind varchar(128) not null,
  data_type varchar(128) not null,
  parent_id bigint not null,
  application_id bigint not null,
  rating varchar(128) not null,
PRIMARY KEY (id)
);
CREATE UNIQUE INDEX authoritative_source_pkey ON authoritative_source (id);
CREATE INDEX fki_authoritative_source_application_id_fkey ON authoritative_source (application_id)create TABLE bookmark (
  id bigint not null default nextval('bookmark_id_seq'::regclass),
  title varchar(255),
  description varchar(255),
  kind varchar(255),
  url varchar(255),
  parent_kind varchar(255),
  parent_id bigint,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  is_primary boolean not null default false,
PRIMARY KEY (id)
);
CREATE UNIQUE INDEX bookmark_pkey ON bookmark (id)create TABLE capability (
  id bigint not null,
  parent_id bigint,
name varchar(255) not null,
  description varchar not null,
PRIMARY KEY (id)
);
CREATE UNIQUE INDEX capability_pkey ON capability (id);
CREATE UNIQUE INDEX unique_id ON capability (id)create TABLE change_log (
  id integer not null default nextval('change_log_id_seq1'::regclass),
  parent_kind varchar(128) not null,
  parent_id bigint not null,
message varchar not null,
  user_id varchar(128) not null,
  severity varchar(32) not null,
  created_at timestamp not null default now(),
PRIMARY KEY (id)
);
CREATE UNIQUE INDEX change_log_pkey ON change_log (id)create TABLE data_flow (
  source_entity_kind varchar(128) not null,
  source_entity_id bigint not null,
  target_entity_kind varchar(128) not null,
  target_entity_id bigint not null,
  data_type varchar(128) not null,
PRIMARY KEY (source_entity_kind, source_entity_id, target_entity_id, target_entity_kind, data_type)
);
CREATE UNIQUE INDEX data_flow_pkey ON data_flow (source_entity_kind, source_entity_id, target_entity_id, target_entity_kind, data_type)create TABLE data_type (
  code varchar(128) not null,
name varchar(256) not null,
  description varchar not null,
PRIMARY KEY (code)
);
CREATE UNIQUE INDEX data_type_pkey ON data_type (code)create TABLE involvement (
  entity_kind varchar(128) not null,
  entity_id bigint not null,
  kind varchar(128) not null,
  employee_id varchar(128) not null
)create TABLE organisational_unit (
  id bigint not null,
name varchar(255),
  description varchar,
  kind varchar(255),
  parent_id bigint,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now(),
PRIMARY KEY (id)
);
CREATE UNIQUE INDEX organisational_unit_pkey ON organisational_unit (id)create TABLE person (
  id bigint not null default nextval('person_id_seq'::regclass),
  employee_id varchar(128),
  display_name varchar(255),
  email varchar(255),
  user_principal_name varchar(255),
  department_name varchar(255),
  kind varchar(255),
  manager_employee_id varchar(128),
  title varchar(255),
PRIMARY KEY (id)
);
CREATE UNIQUE INDEX person_pkey ON person (id);
CREATE UNIQUE INDEX unique_employee_id ON person (employee_id)create TABLE person_hierarchy (
  manager_id varchar(128) not null,
  employee_id varchar(128) not null,
level integer not null default 99,
PRIMARY KEY (manager_id, employee_id)
);
CREATE UNIQUE INDEX reportee_pkey ON person_hierarchy (manager_id, employee_id)create TABLE perspective (
  code varchar(32) not null default 'BUSINESS'::character varying,
name varchar(128) not null,
  description text not null,
PRIMARY KEY (code)
);
CREATE UNIQUE INDEX perspective_pkey ON perspective (code);
CREATE UNIQUE INDEX unique_code ON perspective (code)create TABLE perspective_measurable (
  code varchar(32) not null default ''::character varying,
  perspective_code varchar(32) not null,
name varchar(128) not null,
  description text not null,
PRIMARY KEY (code)
);
CREATE UNIQUE INDEX perspective_measure_pkey ON perspective_measurable (code)create TABLE perspective_rating (
  measure_code varchar(32) not null,
  rating char not null default 'Z'::bpchar,
  parent_kind varchar(128) not null default 'APPLICATION'::character varying,
  parent_id bigint not null,
  capability_id bigint,
  perspective_code varchar(32) not null default 'BUSINESS'::character varying
)create TABLE server_information (
  id integer not null default nextval('server_information_id_seq'::regclass),
  hostname varchar(256) not null,
  operating_system varchar(128) not null default 'UNKNOWN'::character varying,
  environment varchar(128) not null default 'DEV'::character varying,
location varchar(128),
  asset_code varchar(128) not null,
PRIMARY KEY (id)
);
CREATE UNIQUE INDEX server_information_pkey ON server_information (id);