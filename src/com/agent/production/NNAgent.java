package com.agent.production;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.learning.DataSet;
import org.neuroph.core.learning.DataSetRow;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.comp.neuron.ThresholdNeuron;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.TransferFunctionType;

import com.agent.Agent;
import com.agent.AgentsEnvironment;
import com.agent.Food;
import com.nn.ThresholdFunction;
import com.nn.genetic.OptimizableNeuralNetwork;

public class NNAgent extends Agent {

	private static final double maxDeltaAngle = 10;

	private volatile NeuralNetwork brain;

	public NNAgent(double x, double y, double angle) {
		super(x, y, angle);

	}

	@Override
	public synchronized void interact(AgentsEnvironment env) {
		DataSet trainingSet = new DataSet(2, 1);
		trainingSet.addRow(new DataSetRow(new double[] { 0, 0 },
				new double[] { 0 }));
		trainingSet.addRow(new DataSetRow(new double[] { 0, 1 },
				new double[] { 0 }));
		trainingSet.addRow(new DataSetRow(new double[] { 1, 0 },
				new double[] { 0 }));
		trainingSet.addRow(new DataSetRow(new double[] { 1, 1 },
				new double[] { 1 }));

		// create perceptron neural network
		Perceptron myPerceptron = new Perceptron(2, 1);
		// learn the training set
		myPerceptron.learn(trainingSet);
		// test perceptron
		System.out.println("Testing trained perceptron");

		Food nearestFood = null;
		double nearestFoodDist = Double.MAX_VALUE;

		for (Food currFood : env.filter(Food.class)) {
			// agent can see only ahead
			if (this.inSight(currFood)) {
				double currFoodDist = this.distanceTo(currFood);
				if ((nearestFood == null) || (currFoodDist <= nearestFoodDist)) {
					nearestFood = currFood;
					nearestFoodDist = currFoodDist;
				}
			}
		}

		if (nearestFood != null) {
			double rx = this.getRx();
			double ry = this.getRy();
			double x = this.getX();
			double y = this.getY();

			double foodDirectionVectorX = nearestFood.getX() - x;
			double foodDirectionVectorY = nearestFood.getY() - y;

			// left/right cos
			double foodDirectionCosTeta = Math.signum(this.pseudoScalarProduct(
					rx, ry, foodDirectionVectorX, foodDirectionVectorY))
					* this.cosTeta(rx, ry, foodDirectionVectorX,
							foodDirectionVectorY);

			double deltaAngle = foodDirectionCosTeta;
			// double deltaSpeed = 15;

			// deltaSpeed = avoidNaNAndInfinity(deltaSpeed);
			deltaAngle = avoidNaNAndInfinity(deltaAngle);

			// double newSpeed = normalizeSpeed(this.getSpeed() + deltaSpeed,
			// maxSpeed);
			double newAngle = this.getAngle()
					+ normalizeDeltaAngle(deltaAngle, maxDeltaAngle);

			this.setAngle(newAngle);
		}
		this.setSpeed(10);

		this.move();
	}

	public synchronized void setBrain(NeuralNetwork best) {
		brain = best;
	}

	public NeuralNetwork getBrain() {
		return brain;
	}
	
	public static OptNN randomNeuralNetworkBrain () {
		OptNN nn = new OptNN();

		nn.setNetworkType(NeuralNetworkType.KOHONEN);
		Neuron inputNeurons[];
		// TODO: change that
//		for (int i=0;i<6;i++)
//		{
//			inputNeurons[i] = new Neuron().setTransferFunction();
//		}
//		nn.setInputNeurons(inputNeurons);
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
