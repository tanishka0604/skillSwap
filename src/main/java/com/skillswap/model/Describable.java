package com.skillswap.model;

/**
 * INTERFACES, explained with SkillSwap:
 *
 * An interface is a PROMISE, not an implementation. It says "any class
 * that implements Describable must have a describe() method" — but it
 * doesn't say HOW that method works. That's left to each class.
 *
 * Why is this useful here? Later, our dashboard will want to show a
 * mixed list of things — some Users, some Skills — and print a one-line
 * description of each, without caring which one it's looking at:
 *
 *     List<Describable> items = ...;
 *     for (Describable item : items) {
 *         System.out.println(item.describe());
 *     }
 *
 * User.describe() and Skill.describe() will each format their own
 * sentence completely differently, but the loop above doesn't need an
 * if/else to check "is this a User or a Skill?" — it just trusts that
 * whatever's in the list knows how to describe itself. That's the core
 * idea of interfaces: code that depends on WHAT something can do, not
 * on WHICH class it happens to be.
 */
public interface Describable {
    String describe();
}
