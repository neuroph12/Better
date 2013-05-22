package com.agent;

public class Agent implements AbstractAgent {

	private double x;

	private double y;

	private double angle;

	private double speed;

	public Agent(double x, double y, double angle) {
		this.x = x;
		this.y = y;
		this.speed = 0;
		this.angle = angle;
	}

	public void move() {
		double rx = -Math.sin(this.angle);
		double ry = Math.cos(this.angle);
		this.x += rx * this.speed;
		this.y += ry * this.speed;
	}

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}

	@Override
	public void setX(double x) {
		this.x = x;
	}

	@Override
	public void setY(double y) {
		this.y = y;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(double v) {
		this.speed = v;
	}

	public double getAngle() {
		return this.angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getRx() {
		double rx = -Math.sin(this.angle);
		return rx;
	}

	public double getRy() {
		double ry = Math.cos(this.angle);
		return ry;
	}

	@Override
	public void interact(AgentsEnvironment env) {
		// Stub
	}
	
	protected boolean inSight(AbstractAgent agent) {
		double crossProduct = this.cosTeta(this.getRx(), this.getRy(), agent.getX() - this.getX(), agent.getY() - this.getY());
		return (crossProduct > 0);
	}

	protected double distanceTo(AbstractAgent agent) {
		return this.module(agent.getX() - this.getX(), agent.getY() - this.getY());
	}

	protected double cosTeta(double vx1, double vy1, double vx2, double vy2) {
		double v1 = this.module(vx1, vy1);
		double v2 = this.module(vx2, vy2);
		if (v1 == 0) {
			v1 = 1E-5;
		}
		if (v2 == 0) {
			v2 = 1E-5;
		}
		double ret = ((vx1 * vx2) + (vy1 * vy2)) / (v1 * v2);
		return ret;
	}

	protected double module(double vx1, double vy1) {
		return Math.sqrt((vx1 * vx1) + (vy1 * vy1));
	}

	protected double pseudoScalarProduct(double vx1, double vy1, double vx2, double vy2) {
		return (vx1 * vy2) - (vy1 * vx2);
	}
	
	protected double normalizeSpeed(double speed,double maxSpeed) {
		double abs = Math.abs(speed);
		if (abs > maxSpeed) {
			double sign = Math.signum(speed);
			speed = sign * (abs - (Math.floor(abs / maxSpeed) * maxSpeed));
		}
		return speed;
	}
	
	protected double normalizeDeltaAngle(double angle, double maxDeltaAngle) {
		double abs = Math.abs(angle);
		if (abs > maxDeltaAngle) {
			double sign = Math.signum(angle);
			angle = sign * (abs - (Math.floor(abs / maxDeltaAngle) * maxDeltaAngle));
		}
		return angle;
	}
	
	protected double avoidNaNAndInfinity(double x) {
		if ((Double.isNaN(x)) || Double.isInfinite(x)) {
			x = 0;
		}
		return x;
	}
}
