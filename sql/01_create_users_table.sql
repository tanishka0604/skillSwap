-- =========================================================
-- SkillSwap — Phase 2 database setup
-- Run this in MySQL Workbench, or via the mysql command line:
--   mysql -u root -p < 01_create_users_table.sql
-- =========================================================

CREATE DATABASE IF NOT EXISTS skillswap;

USE skillswap;

CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
