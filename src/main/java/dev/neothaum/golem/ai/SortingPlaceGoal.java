package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;

/** AISortingPlace: put what it carries into the marked container beside it that already holds that item. */
public class SortingPlaceGoal extends EmptyPlaceGoal {
	public SortingPlaceGoal(GolemEntity golem) {
		super(golem, true);
	}
}
