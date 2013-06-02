package com.agent.production;

import com.agent.Agent;
import com.agent.AgentsEnvironment;
import com.agent.Food;
import com.nn.NeuralNetwork;

public class ProductionAgent extends Agent {

	private static final double maxSpeed = 4;

	protected static final double maxDeltaAngle = 1;

	protected static final double maxAgentsDistance = 5;

	private volatile NeuralNetwork brain;

	public ProductionAgent(double x, double y, double angle) {
		super(x, y, angle);
	}

	// TODO: realize own implementation
	@Override
	public synchronized void interact(AgentsEnvironment env) {
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
			double foodDirectionCosTeta = Math.signum(this.pseudoScalarProduct(rx, ry, foodDirectionVectorX,
					foodDirectionVectorY)) * this.cosTeta(rx, ry, foodDirectionVectorX, foodDirectionVectorY);

			double deltaAngle = foodDirectionCosTeta;
			deltaAngle = avoidNaNAndInfinity(deltaAngle);
			double newAngle = this.getAngle() + normalizeDeltaAngle(deltaAngle, maxDeltaAngle);

			this.setAngle(newAngle);
		}
		this.setSpeed(maxSpeed);

		this.move();
	}

	public NeuralNetwork getBrain() {
		return brain;
	}

	public void setBrain(NeuralNetwork brain) {
		this.brain = brain;
	}
}
