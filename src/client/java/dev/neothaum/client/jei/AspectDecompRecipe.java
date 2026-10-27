package dev.neothaum.client.jei;

import dev.neothaum.api.aspects.Aspect;

/** A compound aspect and its two direct parent component aspects for JEI. */
public record AspectDecompRecipe(Aspect aspect, Aspect component1, Aspect component2) {
}
