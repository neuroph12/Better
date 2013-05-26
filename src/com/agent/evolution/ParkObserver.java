package com.agent.evolution;

import com.agent.AgentsEnvironment;
import com.agent.AgentsEnvironmentObserver;
import com.agent.Informer;

public class ParkObserver implements AgentsEnvironmentObserver {

	protected static final double maxCommDistance = 5;
	
	@Override
	public void notify(AgentsEnvironment env) {
		for (Informer informer : env.filter(Informer.class)) {
			for (PathFinderAgent finder : env.filter(PathFinderAgent.class)) {
				double distanceToInfo = this.module(informer.getX() - finder.getX(), informer.getY() - finder.getY());
				if (distanceToInfo < maxCommDistance) {
					finder.setGoalX(informer.getGoalX());
					finder.setGoalY(informer.getGoalY());
				}
			}
		}
	}

	protected double module(double vx1, double vy1) {
		return Math.sqrt((vx1 * vx1) + (vy1 * vy1));
	}

}
