package com.agent.evolution;

import java.awt.Color;
import java.awt.Graphics2D;

import com.agent.Agent;
import com.agent.AgentsEnvironment;
import com.agent.Food;
import com.agent.Informer;

public class Visualizator {

	private static int agentRadius = 5;

	public static void paintEnvironment(Graphics2D canvas, AgentsEnvironment environment) {
		canvas.clearRect(0, 0, environment.getWidth(), environment.getHeight());

		canvas.setColor(Color.GREEN);
		for (Food food : environment.filter(Food.class)) {
			int x = (int) food.getX();
			int y = (int) food.getY();

			canvas.fillOval(x - agentRadius, y - agentRadius, agentRadius * 2, agentRadius * 2);
		}

		canvas.setColor(Color.RED);
		for (Agent agent : environment.filter(Agent.class)) {
			int x = (int) agent.getX();
			int y = (int) agent.getY();

			canvas.fillOval(x - agentRadius, y - agentRadius, agentRadius * 2, agentRadius * 2);
		}
		
		canvas.setColor(Color.MAGENTA);
		for (Agent agent : environment.filter(Informer.class)) {
			int x = (int) agent.getX();
			int y = (int) agent.getY();

			canvas.fillOval(x - agentRadius, y - agentRadius, agentRadius * 2, agentRadius * 2);
		}
		
		canvas.setColor(Color.WHITE);
		for (Agent agent : environment.filter(Agent.class)) {
			int x = (int) agent.getX();
			int y = (int) agent.getY();

			int rx = (int) ((agent.getRx() * (agentRadius + 4)) + x);
			int ry = (int) ((agent.getRy() * (agentRadius + 4)) + y);

			canvas.drawOval(x - agentRadius, y - agentRadius, agentRadius * 2, agentRadius * 2);
			canvas.drawLine(x, y, rx, ry);
		}
	}
}
