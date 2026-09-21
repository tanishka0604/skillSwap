package com.skillswap.service;

import com.skillswap.dao.MessageDAO;
import com.skillswap.dao.SkillRequestDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.Message;
import com.skillswap.model.RequestStatus;
import com.skillswap.model.SkillRequest;
import com.skillswap.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Chat doesn't have its own permission system — it BORROWS one from
 * SkillRequestService's state machine (Phase 8). A conversation is
 * only usable when the underlying SkillRequest is ACCEPTED, and only
 * its two participants (sender_id / receiver_id) may read or post to
 * it. That single check, loadAcceptedRequestForParticipant() below,
 * is what both sendMessage() and getMessages() lean on before doing
 * anything else — same "one shared gate" idea as
 * SkillRequestService.requireStatus() was for request transitions.
 */
public class MessageService {

    private final MessageDAO messageDAO;
    private final SkillRequestDAO requestDAO;
    private final UserDAO userDAO;

    public MessageService() {
        this.messageDAO = new MessageDAO();
        this.requestDAO = new SkillRequestDAO();
        this.userDAO = new UserDAO();
    }

    public Message sendMessage(int requestId, int senderId, String content) throws SkillSwapException {
        if (content == null || content.trim().isEmpty()) {
            throw new SkillSwapException("Message cannot be empty.");
        }
        if (content.trim().length() > 2000) {
            throw new SkillSwapException("Message is too long (2000 characters max).");
        }

        loadAcceptedRequestForParticipant(requestId, senderId);

        try {
            return messageDAO.send(requestId, senderId, content.trim());
        } catch (SQLException e) {
            throw new SkillSwapException("Could not send your message. Please try again.", e);
        }
    }

    public List<Message> getMessages(int requestId, int currentUserId) throws SkillSwapException {
        loadAcceptedRequestForParticipant(requestId, currentUserId);

        try {
            return messageDAO.findByRequestId(requestId);
        } catch (SQLException e) {
            throw new SkillSwapException("Could not load messages. Please try again.", e);
        }
    }

    /**
     * Returns the OTHER person in this conversation — whoever currentUserId
     * isn't. Used for the chat header ("Chatting with Rohan").
     */
    public User getOtherParticipant(int requestId, int currentUserId) throws SkillSwapException {
        SkillRequest request = loadAcceptedRequestForParticipant(requestId, currentUserId);
        int otherUserId = (request.getSenderId() == currentUserId)
                ? request.getReceiverId()
                : request.getSenderId();

        try {
            User other = userDAO.findUserById(otherUserId);
            if (other == null) {
                throw new SkillSwapException("The other participant no longer exists.");
            }
            return other;
        } catch (SQLException e) {
            throw new SkillSwapException("Could not load conversation details. Please try again.", e);
        }
    }

    /**
     * Powers the dashboard's "recent messages" section — unlike
     * getMessages(), this isn't scoped to one conversation, so there's
     * no single request to check ACCEPTED/participant status against.
     * The DAO query itself only ever returns messages from
     * conversations userId is already a real participant in, so no
     * extra permission check is needed here.
     */
    public List<Message> getRecentMessagesForUser(int userId, int limit) throws SkillSwapException {
        try {
            return messageDAO.findRecentForUser(userId, limit);
        } catch (SQLException e) {
            throw new SkillSwapException("Could not load recent messages. Please try again.", e);
        }
    }

    // The shared gate: request must exist, must be ACCEPTED, and
    // currentUserId must be one of its two participants. Every public
    // method above calls this FIRST, before touching a single message.
    private SkillRequest loadAcceptedRequestForParticipant(int requestId, int currentUserId)
            throws SkillSwapException {

        try {
            SkillRequest request = requestDAO.findById(requestId);
            if (request == null) {
                throw new SkillSwapException("Conversation not found.");
            }
            if (request.getStatus() != RequestStatus.ACCEPTED) {
                throw new SkillSwapException("Chat is only available for accepted swap requests.");
            }
            if (request.getSenderId() != currentUserId && request.getReceiverId() != currentUserId) {
                throw new SkillSwapException("You are not part of this conversation.");
            }
            return request;

        } catch (SQLException e) {
            throw new SkillSwapException("Could not load that conversation. Please try again.", e);
        }
    }
}
