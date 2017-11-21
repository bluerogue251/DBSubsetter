ALTER TABLE self_referencing_table
  ADD FOREIGN KEY (parent_id) REFERENCES self_referencing_table (id);