package com.skillswap.demo;

import com.skillswap.model.Describable;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillCategory;
import com.skillswap.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A plain class with a main() method — this is how you run ANY Java
 * program directly, no Tomcat involved. We're using it here purely to
 * see Phase 3's concepts actually execute, before they get wired into
 * servlets and a database in later phases.
 *
 * HOW TO RUN THIS (from a terminal, inside src/main/java):
 *   javac com/skillswap/model/*.java com/skillswap/demo/*.java
 *   java com.skillswap.demo.SkillSwapConceptsDemo
 *
 * (Or just click the "Run" arrow next to main() in your IDE.)
 */
public class SkillSwapConceptsDemo {

    public static void main(String[] args) {

        // ---- Classes and objects ----
        // "new User(...)" creates an OBJECT — a specific instance of the
        // User CLASS, with its own values in memory. The class is the
        // blueprint; each "new" call builds one house from that blueprint.
        User aisha = new User("Aisha", "aisha@example.com", "not-a-real-hash");
        User rohan = new User("Rohan", "rohan@example.com", "not-a-real-hash");

        // ---- Building some Skills ----
        Skill javascript = new Skill("JavaScript", SkillCategory.PROGRAMMING);
        Skill photoshop = new Skill("Photoshop", SkillCategory.DESIGN);

        // ---- Collections in action ----
        aisha.addSkillToTeach(javascript);
        aisha.addSkillToLearn(photoshop);

        rohan.addSkillToTeach(photoshop);
        rohan.addSkillToLearn(javascript);

        // Try adding the SAME skill again — thanks to Skill's equals(),
        // this is correctly ignored instead of creating a duplicate.
        aisha.addSkillToTeach(new Skill("JavaScript", SkillCategory.PROGRAMMING));

        System.out.println("Aisha teaches: " + aisha.getSkillsToTeach());
        System.out.println("(still just one JavaScript, duplicate was ignored)");
        System.out.println();

        // ---- Interfaces in action ----
        // Both User and Skill implement Describable, so we can put BOTH
        // kinds of object into one List<Describable> and call describe()
        // on each without caring which concrete class it actually is.
        List<Describable> items = new ArrayList<>();
        items.add(aisha);
        items.add(rohan);
        items.add(javascript);
        items.add(photoshop);

        System.out.println("Everything that can describe itself:");
        for (Describable item : items) {
            System.out.println(" - " + item.describe());
        }
    }
}
