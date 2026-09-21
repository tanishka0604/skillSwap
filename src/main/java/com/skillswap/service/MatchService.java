package com.skillswap.service;

import com.skillswap.dao.UserDAO;
import com.skillswap.dao.UserSkillDAO;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.MatchResult;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillType;
import com.skillswap.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds RECIPROCAL matches for a user — exactly the Aisha/Rohan example
 * from the very first phase:
 *   Aisha teaches JavaScript, wants Photoshop.
 *   Rohan teaches Photoshop, wants JavaScript.
 *   -> each satisfies what the other is looking for, so they match.
 *
 * THE ALGORITHM, IN PLAIN STEPS
 * ------------------------------
 * 1. Look at every skill I want to LEARN. For each one, find every user
 *    who TEACHES it. Group the results into a Map<userId, list of
 *    matching skills> — "this candidate teaches these of my wanted skills".
 * 2. Do the mirror image: for every skill I TEACH, find every user who
 *    wants to LEARN it. Same kind of map, the other direction.
 * 3. A user only counts as a real match if they show up in BOTH maps —
 *    that's the "reciprocal" part. A user who only teaches what I want
 *    (but doesn't want anything I teach) is a one-way fit, not a swap.
 *
 * A Map<Integer, List<Skill>> here is the same List<Skill> from Phase 3,
 * just wrapped in a Map so we can look things up BY userId instead of
 * scanning a list every time — this is the first time the project
 * reaches for a Map instead of a List, because for once we need
 * "give me this candidate's skills" to be a direct lookup, not a search.
 */
public class MatchService {

    private final UserSkillDAO userSkillDAO;
    private final UserDAO userDAO;
    private final SkillService skillService;

    public MatchService() {
        this.userSkillDAO = new UserSkillDAO();
        this.userDAO = new UserDAO();
        this.skillService = new SkillService();
    }

    public List<MatchResult> findMatchesForUser(int userId) throws SkillSwapException {
        try {
            List<Skill> myTeach = skillService.getSkillsToTeach(userId);
            List<Skill> myLearn = skillService.getSkillsToLearn(userId);

            // Step 1: who teaches something I want to learn?
            Map<Integer, List<Skill>> theyTeachWhatIWant =
                    buildCandidateMap(myLearn, SkillType.TEACH, userId);

            // Step 2: who wants to learn something I teach?
            Map<Integer, List<Skill>> theyWantWhatITeach =
                    buildCandidateMap(myTeach, SkillType.LEARN, userId);

            // Step 3: keep only candidates appearing in BOTH maps.
            List<MatchResult> matches = new ArrayList<>();

            for (Integer candidateId : theyTeachWhatIWant.keySet()) {
                if (theyWantWhatITeach.containsKey(candidateId)) {
                    User candidate = userDAO.findUserById(candidateId);
                    if (candidate == null) {
                        continue; // shouldn't happen, but never trust a stale id
                    }

                    matches.add(new MatchResult(
                            candidate,
                            theyTeachWhatIWant.get(candidateId),
                            theyWantWhatITeach.get(candidateId)
                    ));
                }
            }

            return matches;

        } catch (SQLException e) {
            throw new SkillSwapException("Could not compute matches right now. Please try again.", e);
        }
    }

    /**
     * For every skill in `mySkills`, finds every user linked to that
     * skill with `type`, and groups them: candidateUserId -> the subset
     * of `mySkills` that candidate is linked to. Shared by both
     * directions of the algorithm above — step 1 calls it with
     * (myLearn, TEACH) and step 2 calls it with (myTeach, LEARN).
     */
    private Map<Integer, List<Skill>> buildCandidateMap(List<Skill> mySkills, SkillType type, int excludeUserId)
            throws SQLException {

        Map<Integer, List<Skill>> candidates = new HashMap<>();

        for (Skill skill : mySkills) {
            List<Integer> userIds = userSkillDAO.findUserIdsBySkillAndType(skill.getId(), type);

            for (Integer candidateId : userIds) {
                if (candidateId == excludeUserId) {
                    continue; // never match a user against themselves
                }

                // computeIfAbsent: if this is the first skill we've found
                // for this candidate, create a fresh empty list for them
                // first. Without it we'd have to write an explicit
                // "if the map doesn't have this key yet, put one in"
                // check before every single add() below.
                candidates.computeIfAbsent(candidateId, id -> new ArrayList<>()).add(skill);
            }
        }

        return candidates;
    }
}
