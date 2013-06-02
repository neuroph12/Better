package com.agent.production;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.DataSet;
import org.neuroph.core.learning.DataSetRow;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.comp.neuron.ThresholdNeuron;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.TransferFunctionType;

import com.agent.Agent;
import com.agent.AgentsEnvironment;
import com.agent.Food;
import com.agent.Informer;
import com.nn.ThresholdFunction;
import com.nn.genetic.OptimizableNeuralNetwork;

public class NNAgent extends Agent {

	private static final double maxSpeed = 4;
	
	private static final double maxDeltaAngle = 10;

	protected static final double AGENT = -10;

	protected static final double EMPTY = 0;

	protected static final double FOOD = 10;
	
	protected static final double maxAgentsDistance = 5;
	
	private volatile NeuralNetwork brain;

	public NNAgent(double x, double y, double angle) {
		super(x, y, angle);

	}

	@Override
	public synchronized void interact(AgentsEnvironment env) {
//		DataSet trainingSet = new DataSet(2, 1);
//		trainingSet.addRow(new DataSetRow(new double[] { 0, 0 },
//				new double[] { 0 }));
//		trainingSet.addRow(new DataSetRow(new double[] { 0, 1 },
//				new double[] { 0 }));
//		trainingSet.addRow(new DataSetRow(new double[] { 1, 0 },
//				new double[] { 0 }));
//		trainingSet.addRow(new DataSetRow(new double[] { 1, 1 },
//				new double[] { 1 }));
//
//		// create perceptron neural network
//		Perceptron myPerceptron = new Perceptron(2, 1);
//		// learn the training set
//		myPerceptron.learn(trainingSet);
		// test perceptron
		// System.out.println("Testing trained perceptron");

		List<Double> nnInputs = this.createNnInputs(env);

		this.brain.setInput(nnInputs.get(0), nnInputs.get(1), nnInputs.get(2),
				nnInputs.get(3), nnInputs.get(4), nnInputs.get(5));
		
		this.brain.calculate();
		double deltaAngle = this.brain.getOutput()[0];
		double deltaSpeed = this.brain.getOutput()[1];

		deltaSpeed = this.avoidNaNAndInfinity(deltaSpeed);
		deltaAngle = this.avoidNaNAndInfinity(deltaAngle);

		double newSpeed = normalizeSpeed(this.getSpeed() + deltaSpeed, maxSpeed);
		double newAngle = this.getAngle()
				+ normalizeDeltaAngle(deltaAngle, maxDeltaAngle);

		this.setAngle(newAngle);
		this.setSpeed(newSpeed);

		this.move();
	}

	public synchronized void setBrain(NeuralNetwork best) {
		brain = best;
	}

	public NeuralNetwork getBrain() {
		return brain;
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
	
	public static OptNN randomNeuralNetworkBrain () {
		OptNN nn = new OptNN();

		nn.setNetworkType(NeuralNetworkType.KOHONEN);
		Neuron inputNeurons[] = new Neuron[6];
		for (int i=0;i<6;i++)
			inputNeurons[i] = new Neuron();
		nn.setInputNeurons(inputNeurons);
		
		Neuron outputNeurons[] = new Neuron[2];
		for (int i=0;i<2;i++)
			outputNeurons[i] = new Neuron();
		nn.setOutputNeurons(outputNeurons);
		
		nn.randomizeWeights();
		
		return nn;
	}	
	
	public static OptNN genNeuralBrain (int inputCount, int neuronsCount, int linksCount) {
		OptNN nn = new OptNN();
//		for (int i = 0; i < neuronsCount; i++) {
//			ThresholdFunction f = ThresholdFunction.getRandomFunction();
//			nn.setNeuronFunction(i, f, f.getDefaultParams());
//		}
//		for (int i = 0; i < inputCount; i++) {
//			nn.setNeuronFunction(i, ThresholdFunction.LINEAR, ThresholdFunction.LINEAR.getDefaultParams());
//		}
//		for (int i = 0; i < inputCount; i++) {
//			for (int j = inputCount; j < neuronsCount; j+=linksCount) {
//				nn.addLink(i, j, Math.random());
//			}
//		}
//		for (int i = inputCount; i < neuronsCount; i++) {
//			for (int j = inputCount; j < neuronsCount; j+=linksCount) {
//				if (i < j) {
//					nn.addLink(i, j, Math.random());
//				}
//			}
//		}
		return nn;
	}
}
