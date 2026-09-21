package com.skillswap.model;

/**
 * Maps directly onto the ENUM('PENDING','ACCEPTED','REJECTED','CANCELLED')
 * column created back in Phase 4's SQL.
 *
 * This is our first STATE MACHINE: a request doesn't just have a status,
 * it has RULES about which statuses can follow which. A PENDING request
 * can become ACCEPTED, REJECTED, or CANCELLED — but an ACCEPTED request
 * can never be "un-accepted" back to PENDING, and a CANCELLED one can't
 * suddenly become ACCEPTED. The enum itself doesn't enforce any of
 * that (Java enums don't have a built-in idea of "allowed transitions")
 * — SkillRequestService is where those rules actually get checked,
 * every time, before any status is written to the database.
 */
public enum RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}
