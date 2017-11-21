CREATE TABLE self_referencing_table (
  id        SERIAL PRIMARY KEY,
  parent_id INTEGER -- Foreign key added in post_data step
);