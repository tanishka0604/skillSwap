-- =========================================================
-- SkillSwap — Phase 6 database change
-- Run AFTER 01_create_users_table.sql and 02_create_skill_tables.sql
--   mysql -u root -p skillswap < 03_add_profile_fields.sql
-- =========================================================

USE skillswap;

-- ALTER TABLE changes a table that already exists and already has data
-- in it, instead of creating a new one. Both new columns are NULLable
-- (no NOT NULL) because every user who registered before this phase
-- existed has neither a bio nor a picture yet — if we'd required a
-- value, this migration would fail on their existing rows.
ALTER TABLE users
    ADD COLUMN bio TEXT NULL,
    ADD COLUMN profile_picture_url VARCHAR(500) NULL;
