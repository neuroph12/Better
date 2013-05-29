package com.agent.evolution;

import java.util.Random;

import com.agent.AgentsEnvironment;
import com.agent.Food;
import com.agent.Informer;
import com.lagodiuk.ga.Fitness;
import com.nn.genetic.OptimizableNeuralNetwork;

public class ParkEnvFitness implements
		Fitness<OptimizableNeuralNetwork, Double> {

	private static Random random = new Random();

	@Override
	public Double calculate(OptimizableNeuralNetwork chromosome) {
		// TODO maybe, its better to initialize these parameters in constructor
		final int width = 200;
		final int height = 200;
		int informerCount = 1;
		int agentsCount = 5;
		int environmentIterations = 50;

		AgentsEnvironment env = new AgentsEnvironment(width, height);

		Food food = this.newPieceOfFood(width, height);
		env.addAgent(food);
		
		for (int i = 0; i < informerCount; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			double direction = 2 * Math.PI * random.nextDouble();

			Informer agent = new Informer(x, y, direction);
			agent.setGoalX(food.getX());
			agent.setGoalX(food.getY());
			env.addAgent(agent);
		}

		for (int i = 0; i < agentsCount; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			double direction = 2 * Math.PI * random.nextDouble();

			PathFinderAgent agent = new PathFinderAgent(x, y, direction);
			agent.setBrain(chromosome.clone());
			env.addAgent(agent);
		}
		
//		EatenFoodObserver tournamentListener = new EatenFoodObserver() {
//			@Override
//			protected void addRandomPieceOfFood(AgentsEnvironment env) {
////				Food newFood = ParkEnvFitness.this
////						.newPieceOfFood(width, height);
////				env.addAgent(newFood);
//			}
//		};
//		env.addListener(tournamentListener);
		ParkObserver tournamentListener = new ParkObserver();
		env.addListener(tournamentListener);

		for (int i = 0; i < environmentIterations; i++) {
			env.timeStep();
		}

		double score = tournamentListener.getScore();
		return 1.0 / score;
	}

	protected Food newPieceOfFood(int width, int height) {
		Food food = new Food(random.nextInt(width), random.nextInt(height));
		return food;
	}
}
