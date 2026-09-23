package com.skillswap.dao;

import com.skillswap.model.Message;
import com.skillswap.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public Message send(int requestId, int senderId, String content) throws SQLException {
        String sql = "INSERT INTO messages (request_id, sender_id, content) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, requestId);
            stmt.setInt(2, senderId);
            stmt.setString(3, content);
            stmt.executeUpdate();

            Message message = new Message(requestId, senderId, content);
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    message.setId(keys.getInt(1));
                }
            }
            return message;
        }
    }

    /**
     * Every message for one conversation, oldest first — a chat reads
     * top-to-bottom in the order things were actually said, which is
     * exactly what ORDER BY sent_at ASC gives us (Phase 8's request
     * lists all used DESC, newest first, because those are more like
     * an inbox than a conversation).
     */
    public List<Message> findByRequestId(int requestId) throws SQLException {
        String sql = "SELECT m.id, m.request_id, m.sender_id, m.content, m.sent_at, " +
                "u.name AS sender_name " +
                "FROM messages m " +
                "JOIN users u ON u.id = m.sender_id " +
                "WHERE m.request_id = ? " +
                "ORDER BY m.sent_at ASC";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requestId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setRequestId(rs.getInt("request_id"));
                    message.setSenderId(rs.getInt("sender_id"));
                    message.setContent(rs.getString("content"));
                    message.setSenderName(rs.getString("sender_name"));

                    Timestamp ts = rs.getTimestamp("sent_at");
                    if (ts != null) {
                        message.setSentAt(ts.toLocalDateTime());
                    }

                    messages.add(message);
                }
            }
        }
        return messages;
    }

    /**
     * The most recent messages across EVERY accepted conversation this
     * user is part of — not just one requestId like findByRequestId().
     * This is genuinely new: every earlier query filtered by a single
     * request_id; this one joins to skill_requests specifically so it
     * can filter by "any conversation involving this user" instead.
     * Used only by the dashboard, to show "recent activity" at a glance.
     */
    public List<Message> findRecentForUser(int userId, int limit) throws SQLException {
        String sql = "SELECT m.id, m.request_id, m.sender_id, m.content, m.sent_at, " +
                "u.name AS sender_name " +
                "FROM messages m " +
                "JOIN skill_requests r ON r.id = m.request_id " +
                "JOIN users u ON u.id = m.sender_id " +
                "WHERE r.sender_id = ? OR r.receiver_id = ? " +
                "ORDER BY m.sent_at DESC " +
                "LIMIT ?";

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setRequestId(rs.getInt("request_id"));
                    message.setSenderId(rs.getInt("sender_id"));
                    message.setContent(rs.getString("content"));
                    message.setSenderName(rs.getString("sender_name"));

                    Timestamp ts = rs.getTimestamp("sent_at");
                    if (ts != null) {
                        message.setSentAt(ts.toLocalDateTime());
                    }

                    messages.add(message);
                }
            }
        }
        return messages;
    }
}
