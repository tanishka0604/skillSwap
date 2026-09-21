package com.skillswap.dao;

import com.skillswap.model.RequestStatus;
import com.skillswap.model.SkillRequest;
import com.skillswap.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SkillRequestDAO {

    public SkillRequest create(int senderId, int receiverId) throws SQLException {
        String sql = "INSERT INTO skill_requests (sender_id, receiver_id) VALUES (?, ?)";
        // status isn't listed — the column's DEFAULT 'PENDING' from the
        // Phase 4 SQL fills it in automatically for a brand-new request.

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.executeUpdate();

            SkillRequest request = new SkillRequest(senderId, receiverId);
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    request.setId(keys.getInt(1));
                }
            }
            return request;
        }
    }

    /**
     * A bare lookup by id — no joins, no names attached. Used internally
     * by the Service layer when it needs to check WHO a request belongs
     * to before allowing an accept/reject/cancel, not to display it.
     */
    public SkillRequest findById(int id) throws SQLException {
        String sql = "SELECT id, sender_id, receiver_id, status, created_at FROM skill_requests WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapBasicRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Guards against the same person spamming the same target with
     * repeat requests: true if a PENDING request already exists between
     * these two users, in EITHER direction.
     */
    public boolean hasPendingRequestBetween(int userA, int userB) throws SQLException {
        String sql = "SELECT COUNT(*) FROM skill_requests " +
                "WHERE status = 'PENDING' " +
                "AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userA);
            stmt.setInt(2, userB);
            stmt.setInt(3, userB);
            stmt.setInt(4, userA);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Requests where `userId` is the RECEIVER and status is still PENDING. */
    public List<SkillRequest> findPendingReceived(int userId) throws SQLException {
        String sql = "SELECT r.id, r.sender_id, r.receiver_id, r.status, r.created_at, " +
                "u.name AS sender_name " +
                "FROM skill_requests r " +
                "JOIN users u ON u.id = r.sender_id " +
                "WHERE r.receiver_id = ? AND r.status = 'PENDING' " +
                "ORDER BY r.created_at DESC";
        return queryList(sql, userId);
    }

    /** Every request `userId` has SENT, any status — their own history/outbox. */
    public List<SkillRequest> findSent(int userId) throws SQLException {
        String sql = "SELECT r.id, r.sender_id, r.receiver_id, r.status, r.created_at, " +
                "u.name AS receiver_name " +
                "FROM skill_requests r " +
                "JOIN users u ON u.id = r.receiver_id " +
                "WHERE r.sender_id = ? " +
                "ORDER BY r.created_at DESC";
        return queryList(sql, userId);
    }

    /** ACCEPTED requests involving `userId`, on either side. */
    public List<SkillRequest> findAcceptedSwaps(int userId) throws SQLException {
        String sql = "SELECT r.id, r.sender_id, r.receiver_id, r.status, r.created_at, " +
                "su.name AS sender_name, ru.name AS receiver_name " +
                "FROM skill_requests r " +
                "JOIN users su ON su.id = r.sender_id " +
                "JOIN users ru ON ru.id = r.receiver_id " +
                "WHERE r.status = 'ACCEPTED' AND (r.sender_id = ? OR r.receiver_id = ?) " +
                "ORDER BY r.created_at DESC";

        List<SkillRequest> requests = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SkillRequest request = mapBasicRow(rs);
                    request.setSenderName(rs.getString("sender_name"));
                    request.setReceiverName(rs.getString("receiver_name"));
                    requests.add(request);
                }
            }
        }
        return requests;
    }

    public void updateStatus(int requestId, RequestStatus status) throws SQLException {
        String sql = "UPDATE skill_requests SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, requestId);
            stmt.executeUpdate();
        }
    }

    // Shared by findPendingReceived and findSent, which each join to
    // users under a DIFFERENT alias (sender_name vs receiver_name) —
    // so this takes the already-prepared SQL/parameter and lets each
    // caller's own column name (checked before this runs) do the rest.
    private List<SkillRequest> queryList(String sql, int userId) throws SQLException {
        List<SkillRequest> requests = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SkillRequest request = mapBasicRow(rs);

                    // These two columns only exist in SOME of the queries
                    // above — hasColumn() checks the actual ResultSet
                    // metadata so we don't blow up reading a column that
                    // isn't part of this particular SELECT.
                    if (hasColumn(rs, "sender_name")) {
                        request.setSenderName(rs.getString("sender_name"));
                    }
                    if (hasColumn(rs, "receiver_name")) {
                        request.setReceiverName(rs.getString("receiver_name"));
                    }

                    requests.add(request);
                }
            }
        }
        return requests;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        int columnCount = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (rs.getMetaData().getColumnLabel(i).equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }

    private SkillRequest mapBasicRow(ResultSet rs) throws SQLException {
        SkillRequest request = new SkillRequest();
        request.setId(rs.getInt("id"));
        request.setSenderId(rs.getInt("sender_id"));
        request.setReceiverId(rs.getInt("receiver_id"));
        request.setStatus(RequestStatus.valueOf(rs.getString("status")));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            request.setCreatedAt(ts.toLocalDateTime());
        }
        return request;
    }
}
