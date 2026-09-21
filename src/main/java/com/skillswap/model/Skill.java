package com.skillswap.model;

/**
 * Skill is a simple class, same shape as User: private fields,
 * constructors, getters/setters. It represents ONE row that will
 * eventually live in a "skills" table (Phase 4) — e.g. "JavaScript",
 * category PROGRAMMING.
 *
 * "implements Describable" is how a class makes good on an interface's
 * promise. It means: "the compiler should force me to provide a real
 * describe() method, or refuse to compile this class." Try deleting
 * the describe() method below and recompiling — you'll get an error
 * telling you Skill is not abstract and does not override the
 * abstract method. That enforcement is the whole point of interfaces.
 */
public class Skill implements Describable {

    private int id;
    private String name;
    private SkillCategory category;

    public Skill() {
    }

    public Skill(String name, SkillCategory category) {
        this.name = name;
        this.category = category;
    }

    public Skill(int id, String name, SkillCategory category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

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

    public SkillCategory getCategory() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }

    // This is Skill's own answer to the Describable promise.
    @Override
    public String describe() {
        return name + " (" + category + ")";
    }

    // Why override equals()/hashCode() at all?
    // By default, Java compares objects by REFERENCE — two Skill objects
    // are only "equal" if they're literally the same object in memory.
    // That breaks User.addSkillToTeach()'s duplicate check: if you build
    // a fresh "new Skill("JavaScript", PROGRAMMING)" twice, Java's default
    // equals() sees two different objects and lets both in, even though
    // to a person they're clearly the same skill. Overriding equals() to
    // compare by name+category (and hashCode() to match — Java requires
    // equal objects to report the same hashCode) fixes that.
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Skill)) return false;
        Skill that = (Skill) other;
        return name != null && name.equalsIgnoreCase(that.name) && category == that.category;
    }

    @Override
    public int hashCode() {
        int result = (name != null) ? name.toLowerCase().hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Skill{id=" + id + ", name='" + name + "', category=" + category + "}";
    }
}
