package com.agent.evolution;

import java.util.LinkedList;
import java.util.List;

import com.agent.Agent;
import com.agent.AgentsEnvironment;
import com.agent.Informer;

public class PathFinderAgent extends NeuralNetworkDrivenAgent{

	private double goalX = 0;
	private double goalY = 0;
	
	public PathFinderAgent(double x, double y, double angle) {
		super(x, y, angle);
	}
	
	@Override
	protected List<Double> createNnInputs(AgentsEnvironment environment) {
		// Find nearest informer
		Agent nearestAgent = null;
		double nearestAgentDist = Double.MAX_VALUE;

		for (Agent currAgent : environment.filter(Informer.class)) {
			// agent can see only ahead
			if ((this != currAgent)) {
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

		if (nearestAgent!=null && goalX == 0) {
			double agentDirectionVectorX = nearestAgent.getX() - x;
			double agentDirectionVectorY = nearestAgent.getY() - y;

			// left/right cos
			double agentDirectionCosTeta =
					Math.signum(this.pseudoScalarProduct(rx, ry, agentDirectionVectorX, agentDirectionVectorY))
							* this.cosTeta(rx, ry, agentDirectionVectorX, agentDirectionVectorY);

			nnInputs.add(FOOD);
			nnInputs.add(nearestAgentDist);
			nnInputs.add(agentDirectionCosTeta);

		} else {
//			nnInputs.add(EMPTY);
//			nnInputs.add(0.0);
//			nnInputs.add(0.0);
		}
		
		if (goalX != 0) {
			double foodDirectionVectorX = goalX - x;
			double foodDirectionVectorY = goalY - y;

			// left/right cos
			double foodDirectionCosTeta =
					Math.signum(this.pseudoScalarProduct(rx, ry, foodDirectionVectorX, foodDirectionVectorY))
							* this.cosTeta(rx, ry, foodDirectionVectorX, foodDirectionVectorY);

			nnInputs.add(FOOD);
			nnInputs.add(this.module(foodDirectionVectorX, foodDirectionVectorY));
			nnInputs.add(foodDirectionCosTeta);

		} else {
//			nnInputs.add(EMPTY);
//			nnInputs.add(0.0);
//			nnInputs.add(0.0);
		}

		
		return nnInputs;
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
