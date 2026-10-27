package dev.thaumvibe.client.jei;

import dev.thaumvibe.api.aspects.Aspect;

/** A compound aspect and its two direct parent component aspects for JEI. */
public record AspectDecompRecipe(Aspect aspect, Aspect component1, Aspect component2) {
}
