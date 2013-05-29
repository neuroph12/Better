package com.agent.evolution;

import java.util.LinkedList;
import java.util.List;

import com.agent.Agent;
import com.agent.AgentsEnvironment;
import com.agent.AgentsEnvironmentObserver;
import com.agent.Food;
import com.agent.Informer;

public class ParkObserver implements AgentsEnvironmentObserver {

	protected static final double maxCommDistance = 5;
	
	private double score = 0;
	
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
		
		this.score += getTouchGoal(env).size();
	}

	protected double module(double vx1, double vy1) {
		return Math.sqrt((vx1 * vx1) + (vy1 * vy1));
	}

	private List<Food> getTouchGoal(AgentsEnvironment env) {
		List<Food> eatenFood = new LinkedList<Food>();

		for (Food food : env.filter(Food.class)) {
			for (PathFinderAgent fish : env.filter(PathFinderAgent.class)) {
				double distanceToFood = this.module(food.getX() - fish.getX(), food.getY() - fish.getY());
				if (distanceToFood < maxCommDistance) {
					eatenFood.add(food);
				}
			}
		}
		return eatenFood;
	}

	public double getScore() {
		return score;
	}
}
