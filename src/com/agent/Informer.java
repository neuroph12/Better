package com.agent;

public class Informer extends Agent {
	private double goalX;
	private double goalY;
	
	public Informer(double x, double y, double angle) {
		super(x, y, angle);
	}
	
	public Informer(double x, double y, double gx, double gy) {
		super(x, y, 0);
		goalX = gx;
		goalY = gy;
	}
	
	@Override
	public synchronized void interact(AgentsEnvironment env) {
		
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
