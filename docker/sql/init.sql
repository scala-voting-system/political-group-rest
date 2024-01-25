CREATE DATABASE political_groups_db;
\c political_groups_db;

CREATE TABLE political_groups(
	    id uuid DEFAULT gen_random_uuid(),
      name varchar(250) NOT NULL,
      created_at TIMESTAMP,
      updated_at TIMESTAMP,
      is_active boolean DEFAULT true
);

ALTER TABLE political_groups
ADD CONSTRAINT pk_political_groups PRIMARY KEY (id);
