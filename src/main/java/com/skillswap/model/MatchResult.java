package com.skillswap.model;

import java.util.List;

/**
 * Unlike User and Skill, a MatchResult has no table of its own — it's
 * COMPUTED on the fly by MatchService by comparing two users' skill
 * lists. It still deserves to be a proper class rather than, say, three
 * loose variables passed around, for the same reason User deserved to
 * be a class back in Phase 3: it's a single concept with several
 * related pieces of data that always travel together.
 *
 * implements Describable again here mostly to reinforce the Phase 3
 * idea: a THIRD, unrelated kind of object (not a database row at all)
 * can still promise the same describe() behavior as User and Skill.
 */
public class MatchResult implements Describable {

    private final User matchedUser;
    private final List<Skill> theyTeachThatIWant;
    private final List<Skill> theyWantThatITeach;

    public MatchResult(User matchedUser, List<Skill> theyTeachThatIWant, List<Skill> theyWantThatITeach) {
        this.matchedUser = matchedUser;
        this.theyTeachThatIWant = theyTeachThatIWant;
        this.theyWantThatITeach = theyWantThatITeach;
    }

    public User getMatchedUser() {
        return matchedUser;
    }

    public List<Skill> getTheyTeachThatIWant() {
        return theyTeachThatIWant;
    }

    public List<Skill> getTheyWantThatITeach() {
        return theyWantThatITeach;
    }

    @Override
    public String describe() {
        return matchedUser.getName() + " could teach you " + theyTeachThatIWant.size() +
                " skill(s), and wants to learn " + theyWantThatITeach.size() + " from you";
    }
}
