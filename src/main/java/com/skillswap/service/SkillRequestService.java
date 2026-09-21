package com.skillswap.service;

import com.skillswap.dao.SkillRequestDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.RequestStatus;
import com.skillswap.model.SkillRequest;

import java.sql.SQLException;
import java.util.List;

/**
 * Enforces the rules a raw database UPDATE could never enforce by
 * itself: WHO is allowed to change a request's status, and WHICH
 * status changes are legal from wherever a request currently sits.
 * MySQL will happily let you set status to anything the ENUM allows —
 * it has no idea that "ACCEPTED -> PENDING" shouldn't be possible, or
 * that only the receiver may accept a request. This class is where
 * those rules actually live.
 */
public class SkillRequestService {

    private final SkillRequestDAO requestDAO;
    private final UserDAO userDAO;

    public SkillRequestService() {
        this.requestDAO = new SkillRequestDAO();
        this.userDAO = new UserDAO();
    }

    public SkillRequest sendRequest(int senderId, int receiverId) throws SkillSwapException {
        if (senderId == receiverId) {
            throw new SkillSwapException("You can't send a swap request to yourself.");
        }

        try {
            if (userDAO.findUserById(receiverId) == null) {
                throw new SkillSwapException("That user no longer exists.");
            }

            if (requestDAO.hasPendingRequestBetween(senderId, receiverId)) {
                throw new SkillSwapException("There's already a pending request between you two.");
            }

            return requestDAO.create(senderId, receiverId);

        } catch (SQLException e) {
            throw new SkillSwapException("Could not send that request. Please try again.", e);
        }
    }

    public void acceptRequest(int requestId, int currentUserId) throws SkillSwapException {
        SkillRequest request = loadForReceiver(requestId, currentUserId);
        requireStatus(request, RequestStatus.PENDING, "accept");
        updateStatus(requestId, RequestStatus.ACCEPTED);
    }

    public void rejectRequest(int requestId, int currentUserId) throws SkillSwapException {
        SkillRequest request = loadForReceiver(requestId, currentUserId);
        requireStatus(request, RequestStatus.PENDING, "reject");
        updateStatus(requestId, RequestStatus.REJECTED);
    }

    public void cancelRequest(int requestId, int currentUserId) throws SkillSwapException {
        SkillRequest request = loadRequest(requestId);

        // Notice this checks SENDER, not receiver — cancelling is the
        // sender's own decision to withdraw their request, which is a
        // completely different permission than accepting/rejecting
        // (the receiver's decision). Mixing these two checks up would
        // let anyone cancel a request that isn't theirs to cancel.
        if (request.getSenderId() != currentUserId) {
            throw new SkillSwapException("You can only cancel requests you sent.");
        }

        requireStatus(request, RequestStatus.PENDING, "cancel");
        updateStatus(requestId, RequestStatus.CANCELLED);
    }

    public List<SkillRequest> getPendingReceived(int userId) throws SkillSwapException {
        try {
            return requestDAO.findPendingReceived(userId);
        } catch (SQLException e) {
            throw new SkillSwapException("Could not load requests. Please try again.", e);
        }
    }

    public List<SkillRequest> getSent(int userId) throws SkillSwapException {
        try {
            return requestDAO.findSent(userId);
        } catch (SQLException e) {
            throw new SkillSwapException("Could not load requests. Please try again.", e);
        }
    }

    public List<SkillRequest> getAcceptedSwaps(int userId) throws SkillSwapException {
        try {
            return requestDAO.findAcceptedSwaps(userId);
        } catch (SQLException e) {
            throw new SkillSwapException("Could not load your swaps. Please try again.", e);
        }
    }

    // ---- Shared helpers ----

    private SkillRequest loadRequest(int requestId) throws SkillSwapException {
        try {
            SkillRequest request = requestDAO.findById(requestId);
            if (request == null) {
                throw new SkillSwapException("That request no longer exists.");
            }
            return request;
        } catch (SQLException e) {
            throw new SkillSwapException("Could not load that request. Please try again.", e);
        }
    }

    private SkillRequest loadForReceiver(int requestId, int currentUserId) throws SkillSwapException {
        SkillRequest request = loadRequest(requestId);
        if (request.getReceiverId() != currentUserId) {
            throw new SkillSwapException("You can only respond to requests sent to you.");
        }
        return request;
    }

    /**
     * THE STATE MACHINE CHECK. Every accept/reject/cancel routes through
     * here first. If someone double-clicks "Accept" (sending the same
     * request twice) or an old browser tab tries to cancel a request
     * that's already been accepted elsewhere, this is what stops the
     * second, now-invalid transition from silently succeeding.
     */
    private void requireStatus(SkillRequest request, RequestStatus expected, String action)
            throws SkillSwapException {
        if (request.getStatus() != expected) {
            throw new SkillSwapException(
                    "This request can no longer be " + action + "ed — its status is now " + request.getStatus() + ".");
        }
    }

    private void updateStatus(int requestId, RequestStatus status) throws SkillSwapException {
        try {
            requestDAO.updateStatus(requestId, status);
        } catch (SQLException e) {
            throw new SkillSwapException("Could not update that request. Please try again.", e);
        }
    }
}
