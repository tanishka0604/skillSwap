package com.skillswap.model;

import java.time.LocalDateTime;

/**
 * Represents one row in "skill_requests" — one user (sender) asking
 * another (receiver) to start a skill swap.
 *
 * senderName and receiverName are DELIBERATELY not columns in the
 * database — the table only stores sender_id and receiver_id. These
 * two fields exist purely so SkillRequestDAO can hand back a
 * ready-to-display object after a query that JOINs to the users table,
 * instead of forcing every caller to do a second lookup just to show
 * a name on screen. They stay null on a SkillRequest built any other
 * way (e.g. right after creating one) — nothing here writes them back
 * to the database, they're read-only convenience.
 */
public class SkillRequest {

    private int id;
    private int senderId;
    private int receiverId;
    private RequestStatus status;
    private LocalDateTime createdAt;

    private String senderName;
    private String receiverName;

    public SkillRequest() {
    }

    public SkillRequest(int senderId, int receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = RequestStatus.PENDING;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}
