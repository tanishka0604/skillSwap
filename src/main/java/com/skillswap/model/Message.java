package com.skillswap.model;

import java.time.LocalDateTime;

/**
 * One row in "messages" — always attached to a specific skill_requests
 * row (requestId), never a general inbox. That's a deliberate design
 * choice matching your original spec: chat only exists BETWEEN two
 * people who already agreed to a swap, not as an open messaging system.
 *
 * senderName, like SkillRequest's senderName/receiverName in Phase 8,
 * is a read-only convenience field filled in only by queries that JOIN
 * to users — it's not a real column in "messages".
 */
public class Message {

    private int id;
    private int requestId;
    private int senderId;
    private String content;
    private LocalDateTime sentAt;

    private String senderName;

    public Message() {
    }

    public Message(int requestId, int senderId, String content) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
