package com.agent.production;

import java.util.Random;

import com.agent.AgentsEnvironment;
import com.agent.Food;
import com.agent.evolution.EatenFoodObserver;
import com.agent.evolution.NeuralNetworkDrivenAgent;
import com.agent.evolution.TournamentEnvironmentFitness;
import com.lagodiuk.ga.Fitness;

public class OptNNFitness implements Fitness<OptNN, Double> {

	private static Random random = new Random();

	@Override
	public Double calculate(OptNN chromosome) {
		// TODO maybe, its better to initialize these parameters in constructor
		final int width = 200;
		final int height = 200;
		int agentsCount = 10;
		int foodCount = 5;
		int environmentIterations = 50;

		AgentsEnvironment env = new AgentsEnvironment(width, height);

		for (int i = 0; i < agentsCount; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			double direction = 2 * Math.PI * random.nextDouble();

			NNAgent agent = new NNAgent(x, y, direction);
			agent.setBrain(chromosome.clone());

			env.addAgent(agent);
		}

		for (int i = 0; i < foodCount; i++) {
			Food food = this.newPieceOfFood(width, height);
			env.addAgent(food);
		}

		EatenFoodObserver tournamentListener = new EatenFoodObserver() {
			@Override
			protected void addRandomPieceOfFood(AgentsEnvironment env) {
				Food newFood = OptNNFitness.this.newPieceOfFood(width, height);
				env.addAgent(newFood);
			}
		};
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
