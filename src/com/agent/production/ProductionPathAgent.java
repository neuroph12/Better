package com.agent.production;

import com.agent.Agent;
import com.agent.AgentsEnvironment;
import com.agent.Informer;

public class ProductionPathAgent extends ProductionAgent {

	private double goalX = 0;
	private double goalY = 0;

	private static final double maxSpeed = 4;

	public ProductionPathAgent(double x, double y, double angle) {
		super(x, y, angle);
	}

	// TODO: realize own implementation
	@Override
	public synchronized void interact(AgentsEnvironment env) {
		// Find nearest informer
		Agent nearestInformer = null;
		double nearestAgentDist = Double.MAX_VALUE;

		for (Agent currAgent : env.filter(Informer.class)) {
			// agent can see only ahead
			if ((this != currAgent)) {
				double currAgentDist = this.distanceTo(currAgent);
				if (currAgentDist <= nearestAgentDist) {
					nearestInformer = currAgent;
					nearestAgentDist = currAgentDist;
				}
			}
		}

		double rx = this.getRx();
		double ry = this.getRy();
		double x = this.getX();
		double y = this.getY();

		double goalDirectionVectorX = 0; // = nearestFood.getX() - x;
		double goalDirectionVectorY = 0;// = nearestFood.getY() - y;

		if (goalX != 0) {
			goalDirectionVectorX = goalX - x;
			goalDirectionVectorY = goalY - y;
		} else if (nearestInformer != null) {
			goalDirectionVectorX = nearestInformer.getX() - x;
			goalDirectionVectorY = nearestInformer.getY() - y;
		}

		// left/right cos
		double foodDirectionCosTeta = Math.signum(this.pseudoScalarProduct(rx, ry, goalDirectionVectorX,
				goalDirectionVectorY)) * this.cosTeta(rx, ry, goalDirectionVectorX, goalDirectionVectorY);

		double deltaAngle = foodDirectionCosTeta;
		deltaAngle = avoidNaNAndInfinity(deltaAngle);
		double newAngle = this.getAngle() + normalizeDeltaAngle(deltaAngle, maxDeltaAngle);

		this.setAngle(newAngle);
		this.setSpeed(maxSpeed);

		this.move();
	}

	public double getGoalX() {
		return goalX;
	}

	public void setGoalX(double goalX) {
		this.goalX = goalX;
	}

	public double getGoalY() {
		return goalY;
	}

	public void setGoalY(double goalY) {
		this.goalY = goalY;
	}
}
