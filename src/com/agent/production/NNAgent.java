package com.agent.production;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.DataSet;
import org.neuroph.core.learning.DataSetRow;
import org.neuroph.nnet.Perceptron;

import com.agent.Agent;
import com.agent.AgentsEnvironment;
import com.agent.Food;

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

	public void setBrain(NeuralNetwork best) {
		brain = best;
	}
}
