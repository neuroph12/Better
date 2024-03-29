package com.agent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.agent.evolution.NeuralNetworkDrivenAgent;

public class AgentsEnvironment {

	private int width;

	private int height;

	private List<AbstractAgent> agents = new ArrayList<AbstractAgent>();

	private List<AgentsEnvironmentObserver> listeners = new ArrayList<AgentsEnvironmentObserver>();

	public AgentsEnvironment(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void addListener(AgentsEnvironmentObserver listener) {
		this.listeners.add(listener);
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public synchronized void timeStep() {
		for (AbstractAgent agent : this.getAgents()) {
			agent.interact(this);
			this.avoidMovingOutsideOfBounds(agent);
		}
		for (AgentsEnvironmentObserver l : this.listeners) {
			l.notify(this);
		}
	}

	/**
	 * avoid moving outside of environment
	 */
	private void avoidMovingOutsideOfBounds(AbstractAgent agent) {
		double newX = agent.getX();
		double newY = agent.getY();
		if (newX < 0) {
			newX = this.width - 1;
		}
		if (newY < 0) {
			newY = this.height - 1;
		}
		if (newX > this.width) {
			newX = 1;
		}
		if (newY > this.height) {
			newY = 1;
		}

		agent.setX(newX);
		agent.setY(newY);
	}

	public List<AbstractAgent> getAgents() {
		// to avoid concurrent modification exception
		return new LinkedList<AbstractAgent>(this.agents);
	}

	public synchronized void addAgent(AbstractAgent agent) {
		this.agents.add(agent);
	}

	public synchronized void removeAgent(AbstractAgent agent) {
		this.agents.remove(agent);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractAgent> Iterable<T> filter(Class<T> clazz) {
		// Guava increases binary size from 70Kb to 2Mb :(
		// return Iterables.filter(this.getAgents(), clazz);
		List<T> filtered = new LinkedList<T>();
		for (AbstractAgent agent : this.getAgents()) {
			if (clazz.isInstance(agent)) {
				filtered.add((T) agent);
			}
		}
		return filtered;
	}
	
	public synchronized void clearAgents() {
		for (int i = agents.size() - 1; i >= 0; i--) {
			if (agents.get(i).getClass().equals(NeuralNetworkDrivenAgent.class))
				agents.remove(i);
		}
	}
}
