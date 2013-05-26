package com.agent.evolution;

import java.util.LinkedList;
import java.util.List;

import com.agent.AbstractAgent;
import com.agent.Agent;
import com.agent.AgentsEnvironment;
import com.agent.Food;
import com.nn.NeuralNetwork;
import com.nn.ThresholdFunction;
import com.nn.genetic.OptimizableNeuralNetwork;

public class NeuralNetworkDrivenAgent extends Agent {

	private static final double maxSpeed = 4;

	private static final double maxDeltaAngle = 1;

	protected static final double maxAgentsDistance = 5;

	private static final double AGENT = -10;

	private static final double EMPTY = 0;

	private static final double FOOD = 10;
	
	private static final int RAND_NEURONS = 15;

	private volatile NeuralNetwork brain;

	public NeuralNetworkDrivenAgent(double x, double y, double angle) {
		super(x, y, angle);
	}

	public NeuralNetwork getBrain() {
		return brain;
	}
	
	/**
	 * Animating of agents and evolving best brain - might be in different
	 * threads <br/>
	 * Synchronization prevents from race condition when trying to set new
	 * brain, while method "interact" runs <br/>
	 * <br/>
	 * TODO Maybe consider to use non-blocking technique. But at the moment this
	 * simplest solution doesn't cause any overheads
	 */
	public synchronized void setBrain(NeuralNetwork brain) {
		this.brain = brain;
	}

	/**
	 * Synchronization prevents from race condition when trying to set new
	 * brain, while method "interact" runs <br/>
	 * <br/>
	 * TODO Maybe consider to use non-blocking technique. But at the moment this
	 * simplest solution doesn't cause any overheads
	 */
	@Override
	public synchronized void interact(AgentsEnvironment env) {
		List<Double> nnInputs = this.createNnInputs(env);

		this.activateNeuralNetwork(nnInputs);

		int neuronsCount = this.brain.getNeuronsCount();
		double deltaAngle = this.brain.getAfterActivationSignal(neuronsCount - 2);
		double deltaSpeed = this.brain.getAfterActivationSignal(neuronsCount - 1);

		deltaSpeed = this.avoidNaNAndInfinity(deltaSpeed);
		deltaAngle = this.avoidNaNAndInfinity(deltaAngle);

		double newSpeed = normalizeSpeed(this.getSpeed() + deltaSpeed, maxSpeed);
		double newAngle = this.getAngle() + normalizeDeltaAngle(deltaAngle, maxDeltaAngle);

		this.setAngle(newAngle);
		this.setSpeed(newSpeed);

		this.move();
	}

	private void activateNeuralNetwork(List<Double> nnInputs) {
		for (int i = 0; i < nnInputs.size(); i++) {
			this.brain.putSignalToNeuron(i, nnInputs.get(i));
		}
		this.brain.activate();
	}

	protected List<Double> createNnInputs(AgentsEnvironment environment) {
		// Find nearest food
		Food nearestFood = null;
		double nearestFoodDist = Double.MAX_VALUE;

		for (Food currFood : environment.filter(Food.class)) {
			// agent can see only ahead
			if (this.inSight(currFood)) {
				double currFoodDist = this.distanceTo(currFood);
				if ((nearestFood == null) || (currFoodDist <= nearestFoodDist)) {
					nearestFood = currFood;
					nearestFoodDist = currFoodDist;
				}
			}
		}

		// Find nearest agent
		Agent nearestAgent = null;
		double nearestAgentDist = maxAgentsDistance;

		for (Agent currAgent : environment.filter(Agent.class)) {
			// agent can see only ahead
			if ((this != currAgent) && (this.inSight(currAgent))) {
				double currAgentDist = this.distanceTo(currAgent);
				if (currAgentDist <= nearestAgentDist) {
					nearestAgent = currAgent;
					nearestAgentDist = currAgentDist;
				}
			}
		}

		List<Double> nnInputs = new LinkedList<Double>();

		double rx = this.getRx();
		double ry = this.getRy();

		double x = this.getX();
		double y = this.getY();

		if (nearestFood != null) {
			double foodDirectionVectorX = nearestFood.getX() - x;
			double foodDirectionVectorY = nearestFood.getY() - y;

			// left/right cos
			double foodDirectionCosTeta =
					Math.signum(this.pseudoScalarProduct(rx, ry, foodDirectionVectorX, foodDirectionVectorY))
							* this.cosTeta(rx, ry, foodDirectionVectorX, foodDirectionVectorY);

			nnInputs.add(FOOD);
			nnInputs.add(nearestFoodDist);
			nnInputs.add(foodDirectionCosTeta);

		} else {
			nnInputs.add(EMPTY);
			nnInputs.add(0.0);
			nnInputs.add(0.0);
		}

		if (nearestAgent != null) {
			double agentDirectionVectorX = nearestAgent.getX() - x;
			double agentDirectionVectorY = nearestAgent.getY() - y;

			// left/right cos
			double agentDirectionCosTeta =
					Math.signum(this.pseudoScalarProduct(rx, ry, agentDirectionVectorX, agentDirectionVectorY))
							* this.cosTeta(rx, ry, agentDirectionVectorX, agentDirectionVectorY);

			nnInputs.add(AGENT);
			nnInputs.add(nearestAgentDist);
			nnInputs.add(agentDirectionCosTeta);

		} else {
			nnInputs.add(EMPTY);
			nnInputs.add(0.0);
			nnInputs.add(0.0);
		}
		return nnInputs;
	}
	
	public static OptimizableNeuralNetwork randomNeuralNetworkBrain () {
		OptimizableNeuralNetwork nn = new OptimizableNeuralNetwork(RAND_NEURONS);
		for (int i = 0; i < RAND_NEURONS; i++) {
			ThresholdFunction f = ThresholdFunction.getRandomFunction();
			nn.setNeuronFunction(i, f, f.getDefaultParams());
		}
		for (int i = 0; i < 6; i++) {
			nn.setNeuronFunction(i, ThresholdFunction.LINEAR, ThresholdFunction.LINEAR.getDefaultParams());
		}
		for (int i = 0; i < 6; i++) {
			for (int j = 6; j < RAND_NEURONS; j++) {
				nn.addLink(i, j, Math.random());
			}
		}
		for (int i = 6; i < RAND_NEURONS; i++) {
			for (int j = 6; j < RAND_NEURONS; j++) {
				if (i < j) {
					nn.addLink(i, j, Math.random());
				}
			}
		}
		return nn;
	}
	
	public static OptimizableNeuralNetwork genNeuralBrain (int neuronsCount, int linksCount) {
		OptimizableNeuralNetwork nn = new OptimizableNeuralNetwork(neuronsCount);
		for (int i = 0; i < neuronsCount; i++) {
			ThresholdFunction f = ThresholdFunction.getRandomFunction();
			nn.setNeuronFunction(i, f, f.getDefaultParams());
		}
		for (int i = 0; i < 6; i++) {
			nn.setNeuronFunction(i, ThresholdFunction.LINEAR, ThresholdFunction.LINEAR.getDefaultParams());
		}
		for (int i = 0; i < 6; i++) {
			for (int j = 6; j < neuronsCount; j+=linksCount) {
				nn.addLink(i, j, Math.random());
			}
		}
		for (int i = 6; i < neuronsCount; i++) {
			for (int j = 6; j < neuronsCount; j+=linksCount) {
				if (i < j) {
					nn.addLink(i, j, Math.random());
				}
			}
		}
		return nn;
	}
}
