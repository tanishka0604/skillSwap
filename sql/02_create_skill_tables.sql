-- =========================================================
-- SkillSwap — Phase 4 database setup
-- Run AFTER 01_create_users_table.sql
--   mysql -u root -p skillswap < 02_create_skill_tables.sql
-- =========================================================

USE skillswap;

-- One row per distinct skill that exists anywhere on the platform.
-- category is stored as a plain string here (MySQL has no native
-- "Java enum" type) — it will always be one of the 8 names from
-- Java's SkillCategory enum; the Java layer is what enforces that.
CREATE TABLE IF NOT EXISTS skills (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    category    VARCHAR(50)  NOT NULL,
    UNIQUE KEY uniq_skill (name, category)
);

-- The JOIN TABLE between users and skills. A single "skills" table and
-- a single "users" table can't by themselves say "Aisha teaches
-- JavaScript" — you need a third table that links a user_id to a
-- skill_id. The extra `type` column is what makes this table do double
-- duty for BOTH "skills I can teach" and "skills I want to learn": the
-- same user/skill pair can exist twice, once with each type.
CREATE TABLE IF NOT EXISTS user_skills (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    skill_id    INT NOT NULL,
    type        ENUM('TEACH', 'LEARN') NOT NULL,
    FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE KEY uniq_user_skill_type (user_id, skill_id, type)
);

-- Created now so the schema is complete, per the original plan — the
-- DAO and Servlet logic that USES this table comes in the
-- Skill-Swap-Requests phase, not this one.
CREATE TABLE IF NOT EXISTS skill_requests (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    sender_id   INT NOT NULL,
    receiver_id INT NOT NULL,
    status      ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id)   REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Also created now, used starting in the Chat phase.
CREATE TABLE IF NOT EXISTS messages (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    request_id  INT NOT NULL,
    sender_id   INT NOT NULL,
    content     TEXT NOT NULL,
    sent_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES skill_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id)  REFERENCES users(id)          ON DELETE CASCADE
);
