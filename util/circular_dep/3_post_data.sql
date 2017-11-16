CREATE INDEX ON grandparents (favorite_parent_id);
CREATE INDEX ON parents (grandparent_id);
CREATE INDEX ON children (parent_id);