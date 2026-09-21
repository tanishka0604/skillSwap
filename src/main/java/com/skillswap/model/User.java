package com.skillswap.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one row in the "users" table.
 *
 * This is a plain Java object (often called a POJO / model / entity).
 * It does not talk to the database itself — it just holds data in memory,
 * the same way a form on paper holds someone's details. UserDAO is the
 * class that actually reads/writes rows and fills in a User object.
 *
 * "implements Describable" (see that interface's own comments) means
 * User promises to provide a describe() method — you'll find it near
 * the bottom, right next to Skill's own version of the same method.
 */
public class User implements Describable {

    // Fields are private -> this is ENCAPSULATION.
    // No other class can reach in and change a User's email directly;
    // they must go through the getter/setter methods below. That means
    // if we ever need to add a rule ("email must be lowercase" etc.),
    // there is exactly one place to add it.
    private int id;
    private String name;
    private String email;
    private String password; // stores the HASHED password, never plain text
    private LocalDateTime createdAt;

    // New in Phase 6. Both are nullable — a fresh registration has
    // neither, and that's a perfectly normal state for a User to be in,
    // not an error condition.
    private String bio;
    private String profilePictureUrl;

    // COLLECTIONS: a List<Skill> is a resizable, ordered group of Skill
    // objects — unlike a plain array, we don't have to decide the size
    // up front, and it grows as skills are added. We use ArrayList, the
    // most common List implementation, which stores items in the order
    // they were added and lets us fetch by position quickly.
    //
    // These two lists are IN-MEMORY ONLY for now — nothing here talks to
    // the database yet. Phase 4 introduces a "user_skills" table and a
    // SkillDAO that will actually persist these; for this phase, we're
    // only practicing how a User "has many" Skills in Java itself.
    private final List<Skill> skillsToTeach = new ArrayList<>();
    private final List<Skill> skillsToLearn = new ArrayList<>();

    // ---- Constructors ----

    // Empty constructor: lets us build a User step by step with setters.
    // Useful when UserDAO reads a row from a ResultSet one column at a time.
    public User() {
    }

    // Constructor used when we are about to INSERT a new user.
    // Notice there's no "id" or "createdAt" parameter — MySQL generates
    // those for us (AUTO_INCREMENT and DEFAULT CURRENT_TIMESTAMP).
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Full constructor: used when UserDAO builds a User from a database row.
    public User(int id, String name, String email, String password, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    // ---- Getters and setters ----
    // These are the "front door" to the private fields above.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    // ---- Skill collections ----

    // addSkillToTeach / addSkillToLearn are METHODS — named, reusable
    // blocks of behavior a class exposes. Notice they don't just call
    // list.add(skill); they check for duplicates first. That's the same
    // idea as encapsulation: the list itself is private, and the ONLY
    // way to put something into it is through a method that can enforce
    // a rule, instead of every caller having direct access to `.add()`
    // and being trusted to remember the rule themselves.
    public void addSkillToTeach(Skill skill) {
        if (!skillsToTeach.contains(skill)) {
            skillsToTeach.add(skill);
        }
    }

    public void addSkillToLearn(Skill skill) {
        if (!skillsToLearn.contains(skill)) {
            skillsToLearn.add(skill);
        }
    }

    // Collections.unmodifiableList wraps our list so whoever calls this
    // getter can READ it (loop over it, print it) but calling .add() or
    // .remove() on the result throws an exception. Without this, we'd be
    // handing out a direct reference to our private list — technically
    // still "private", but anyone with the reference could mutate it
    // from outside the class, quietly breaking encapsulation in practice.
    public List<Skill> getSkillsToTeach() {
        return Collections.unmodifiableList(skillsToTeach);
    }

    public List<Skill> getSkillsToLearn() {
        return Collections.unmodifiableList(skillsToLearn);
    }

    // User's own answer to the Describable promise — a completely
    // different sentence shape than Skill.describe(), which is exactly
    // the point of an interface: each implementer decides its own "how".
    @Override
    public String describe() {
        return name + " teaches " + skillsToTeach.size() +
                " skill(s) and wants to learn " + skillsToLearn.size() + " skill(s)";
    }

    @Override
    public String toString() {
        // Deliberately NOT including password here — printing a User
        // (e.g. in a log statement) should never leak the password hash.
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
