package com.skillswap.model;

/**
 * An ENUM is a special Java type for a fixed, known set of values —
 * here, the 8 categories from the Phase 1 landing page. It's not one
 * of the concepts on your list, but it solves the exact problem
 * "Collections" and "Basic OOP" are there for: without it, a skill's
 * category would just be a raw String, and nothing would stop someone
 * from typing "Programing" (typo) or "programming" (wrong case) into
 * the database. With an enum, that category can ONLY ever be one of
 * these 8 named values — the compiler checks it for you.
 */
public enum SkillCategory {
    PROGRAMMING,
    DESIGN,
    MUSIC,
    PHOTOGRAPHY,
    LANGUAGES,
    MARKETING,
    FITNESS,
    ACADEMICS
}
