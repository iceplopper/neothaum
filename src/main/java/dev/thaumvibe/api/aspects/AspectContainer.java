package dev.thaumvibe.api.aspects;

/** IAspectContainer: a block entity that holds aspects (jars, alembics, the infusion matrix's sources). */
public interface AspectContainer {
	AspectList getAspects();

	void setAspects(AspectList aspects);

	boolean doesContainerAccept(Aspect tag);

	/** Adds up to {@code amount}; returns what did not fit. */
	int addToContainer(Aspect tag, int amount);

	boolean takeFromContainer(Aspect tag, int amount);

	boolean takeFromContainer(AspectList list);

	boolean doesContainerContainAmount(Aspect tag, int amount);

	boolean doesContainerContain(AspectList list);

	int containerContains(Aspect tag);
}
