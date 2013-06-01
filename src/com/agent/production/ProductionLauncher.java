package com.agent.production;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.agent.AgentsEnvironment;
import com.agent.Food;
import com.agent.Informer;
import com.agent.MovingFood;
import com.agent.evolution.EatenFoodObserver;
import com.agent.evolution.NeuralNetworkDrivenAgent;
import com.agent.evolution.ParkEnvFitness;
import com.agent.evolution.ParkObserver;
import com.agent.evolution.PathFinderAgent;
import com.agent.evolution.TournamentEnvironmentFitness;
import com.agent.evolution.Visualizator;
import com.agent.production.NNAgent;
import com.agent.production.OptNN;
import com.agent.production.OptNNFitness;
import com.lagodiuk.ga.Fitness;
import com.lagodiuk.ga.GeneticAlgorithm;
import com.lagodiuk.ga.IterartionListener;
import com.lagodiuk.ga.Population;

import com.nn.NeuralNetwork;
import com.nn.genetic.OptimizableNeuralNetwork;

public class ProductionLauncher {
	private static final int gaPopulationSize = 5;
	private static final int parentalChromosomesSurviveCount = 1;
	private static final int ENV_WIDTH = 600;
	private static final int ENV_HEIGHT = 400;
	private static final int AGENTS_COUNT = 1;
	private static final int FOOD_COUNT = 1;

	private static final String PREFS_KEY_BRAINS_DIRECTORY = "BrainsDirectory";

	private static Random random = new Random();

	private static GeneticAlgorithm<OptimizableNeuralNetwork, Double> ga;

	private static AgentsEnvironment environment;

	private static int populationNumber = 0;

	private static volatile boolean play = true;

	private static volatile boolean staticFood = true;

	private static volatile boolean regenerateFood = true;

	// UI

	private static JFrame appFrame;

	private static JPanel environmentPanel;

	private static JPanel controlsPanel;

	private static JTextField evolveTextField;
	
	private static JTextField neuralTextField;

	private static JButton evolveButton;

	private static JButton playPauseButton;
	
	private static JButton printButton;

	private static JButton loadBrainButton;

	private static JButton saveBrainButton;

	private static JButton resetButton;

	private static JRadioButton staticFoodRadioButton;

	private static JRadioButton dynamicFoodRadioButton;

	private static ButtonGroup foodTypeButtonGroup;

	private static JCheckBox regenerateFoodCheckbox;

	private static JProgressBar progressBar;

	private static JLabel populationInfoLabel;

	private static BufferedImage displayEnvironmentBufferedImage;

	private static Graphics2D displayEnvironmentCanvas;

	private static JFileChooser fileChooser;

	private static Preferences prefs;

	public static void main(String[] args) throws Exception {
		initializeGeneticAlgorithm(gaPopulationSize, parentalChromosomesSurviveCount, null);

		initializeEnvironment(ENV_WIDTH, ENV_HEIGHT, AGENTS_COUNT, FOOD_COUNT);

		initializeCanvas(ENV_WIDTH, ENV_HEIGHT);

		initializeUI(ENV_WIDTH, ENV_HEIGHT);

		initializeEvolveButtonFunctionality();

		initializePlayPauseButtonFunctionality();

		initializeAddingFoodFunctionality();

		initializeLoadBrainButtonFunctionality();

		initializeSaveBrainButtonFunctionality();

		initializeChangingFoodTypeFunctionality();

		initializeRegenerateFoodCheckboxFunctionality();

		initializeResetButtonFunctionality();

		displayUI();

		mainEnvironmentLoop();
	}

	private static void mainEnvironmentLoop() throws InterruptedException {
		for (;;) {
			Thread.sleep(50);
			if (play) {
				environment.timeStep();
			}
			Visualizator.paintEnvironment(displayEnvironmentCanvas, environment);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					environmentPanel.getGraphics().drawImage(displayEnvironmentBufferedImage, 0, 0, null);
				}
			});
		}
	}

	private static void initializeCanvas(int environmentWidth, int environmentHeight) {
		displayEnvironmentBufferedImage = new BufferedImage(environmentWidth, environmentHeight, BufferedImage.TYPE_INT_RGB);

		displayEnvironmentCanvas = (Graphics2D) displayEnvironmentBufferedImage.getGraphics();
		displayEnvironmentCanvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private static void initializeEnvironment(int environmentWidth, int environmentHeight, int agentsCount, int foodCount) {
		environment = new AgentsEnvironment(environmentWidth, environmentHeight);
		environment.addListener(new ParkObserver());
		environment.addListener(new EatenFoodObserver() {
			@Override
			protected void addRandomPieceOfFood(AgentsEnvironment env) {
				if (regenerateFood) {
					Food food = createRandomFood(env.getWidth(), env.getHeight());
					env.addAgent(food);
				}
			}
		});

		NeuralNetwork brain = NeuralNetworkDrivenAgent.randomNeuralNetworkBrain();
		initializeAgents(brain, agentsCount);
		initializeFood(foodCount);
	}

	private static Food createRandomFood(int width, int height) {
		int x = random.nextInt(width);
		int y = random.nextInt(height);

		Food food = null;
		if (staticFood) {
			food = new Food(x, y);
		} else {
			double speed = random.nextDouble() * 2;
			double direction = random.nextDouble() * 2 * Math.PI;

			food = new MovingFood(x, y, direction, speed);
		}
		return food;
	}

	private static void displayUI() {
		// put application frame to the center of screen
		appFrame.setLocationRelativeTo(null);

		appFrame.setVisible(true);
	}

	private static void initializeUI(int environmentWidth, int environmentHeight) {
		appFrame = new JFrame("Evolving neural network driven agents");
		appFrame.setSize(environmentWidth + 130, environmentHeight + 50);
		appFrame.setResizable(false);
		appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		appFrame.setLayout(new BorderLayout());

		environmentPanel = new JPanel();
		environmentPanel.setSize(environmentWidth, environmentHeight);
		appFrame.add(environmentPanel, BorderLayout.CENTER);

		controlsPanel = new JPanel();
		appFrame.add(controlsPanel, BorderLayout.EAST);
		controlsPanel.setLayout(new GridLayout(11, 1, 5, 5));

		evolveTextField = new JTextField("10");
		controlsPanel.add(evolveTextField);

		evolveButton = new JButton("evolve");
		controlsPanel.add(evolveButton);

		saveBrainButton = new JButton("save brain");
		controlsPanel.add(saveBrainButton);

		loadBrainButton = new JButton("load brain");
		controlsPanel.add(loadBrainButton);

		staticFoodRadioButton = new JRadioButton("usual agent");
		dynamicFoodRadioButton = new JRadioButton("park agent");
		foodTypeButtonGroup = new ButtonGroup();
		foodTypeButtonGroup.add(staticFoodRadioButton);
		foodTypeButtonGroup.add(dynamicFoodRadioButton);
		controlsPanel.add(staticFoodRadioButton);
		controlsPanel.add(dynamicFoodRadioButton);
		if (staticFood) {
			staticFoodRadioButton.setSelected(true);
		} else {
			dynamicFoodRadioButton.setSelected(true);
		}

		regenerateFoodCheckbox = new JCheckBox("regenerate food");
		regenerateFoodCheckbox.setSelected(regenerateFood);
		controlsPanel.add(regenerateFoodCheckbox);

		printButton = new JButton("print");
		controlsPanel.add(printButton);
		
		neuralTextField = new JTextField("6:15:1");
		controlsPanel.add(neuralTextField);
		
		playPauseButton = new JButton("pause");
		controlsPanel.add(playPauseButton);

		resetButton = new JButton("reset");
		controlsPanel.add(resetButton);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setVisible(false);
		appFrame.add(progressBar, BorderLayout.SOUTH);

		populationInfoLabel = new JLabel("Population: " + populationNumber, SwingConstants.CENTER);
		appFrame.add(populationInfoLabel, BorderLayout.NORTH);

		prefs = Preferences.userNodeForPackage(ProductionLauncher.class);
		String brainsDirPath = prefs.get(PREFS_KEY_BRAINS_DIRECTORY, "");
		fileChooser = new JFileChooser(new File(brainsDirPath));
	}

	protected static void initializeChangingFoodTypeFunctionality() {
		ItemListener changingFoodTypeListener = new ItemListener() {
			@Override
			// TODO: временное исправление
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					disableControls();
					
					int environmentWidth = environment.getWidth();
					int environmentHeight = environment.getHeight();

					environment.clearAgents();
					for (int i = 0; i < AGENTS_COUNT; i++) {
						int x = random.nextInt(environmentWidth);
						int y = random.nextInt(environmentHeight);
						double direction = random.nextDouble() * 2 * Math.PI;

						Informer informer = new Informer(x, y, direction);
						for (Food currFood : environment.filter(Food.class)) {
							informer.setGoalX(currFood.getX());
							informer.setGoalY(currFood.getY());
						}

						x = random.nextInt(environmentWidth);
						y = random.nextInt(environmentHeight);
						PathFinderAgent agent = new PathFinderAgent(x, y,
								direction);
						agent.setBrain(NeuralNetworkDrivenAgent.randomNeuralNetworkBrain());
						
						environment.addAgent(agent);
						environment.addAgent(informer);
					}
//					boolean wasPlaying = play;
//					play = false;
//					staticFood = !staticFood;
//					List<Food> food = new LinkedList<Food>();
//					for (Food f : environment.filter(Food.class)) {
//						food.add(f);
//					}
//					for (Food f : food) {
//						environment.removeAgent(f);
//						Food newFood = createRandomFood(1, 1);
//						newFood.setX(f.getX());
//						newFood.setY(f.getY());
//						environment.addAgent(newFood);
//					}
//					play = wasPlaying;
					enableControls();
				}
			}
		};
		staticFoodRadioButton.addItemListener(changingFoodTypeListener);
		dynamicFoodRadioButton.addItemListener(changingFoodTypeListener);
	}

	public static void initializeRegenerateFoodCheckboxFunctionality() {
		regenerateFoodCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				regenerateFood = !regenerateFood;
			}
		});
	}

	private static void initializeLoadBrainButtonFunctionality() {
		loadBrainButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableControls();

				int returnVal = fileChooser.showOpenDialog(appFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						File brainFile = fileChooser.getSelectedFile();
						prefs.put(PREFS_KEY_BRAINS_DIRECTORY, brainFile.getParent());

						FileInputStream in = new FileInputStream(brainFile);

						NeuralNetwork newBrain = NeuralNetwork.unmarsall(in);
						in.close();
						
						for (PathFinderAgent agent : environment.filter(PathFinderAgent.class)) {
							agent.setBrain(newBrain.clone());
						}
//						setAgentBrains(newBrain);

						OptimizableNeuralNetwork optimizableNewBrain = new OptimizableNeuralNetwork(newBrain);
						int populationSize = ga.getPopulation().getSize();
						int parentalChromosomesSurviveCount = ga.getParentChromosomesSurviveCount();
						// TODO:
//						initializeGA(populationSize, parentalChromosomesSurviveCount, optimizableNewBrain);
						initializeGeneticAlgorithm(populationSize, parentalChromosomesSurviveCount, optimizableNewBrain);

						// reset population number counter
						populationNumber = 0;
						populationInfoLabel.setText("Population: " + populationNumber);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				enableControls();
			}
		});
	}

	private static void initializeSaveBrainButtonFunctionality() {
		saveBrainButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableControls();

				int returnVal = fileChooser.showSaveDialog(appFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						File brainFile = fileChooser.getSelectedFile();
						prefs.put(PREFS_KEY_BRAINS_DIRECTORY, brainFile.getParent());

						FileOutputStream out = new FileOutputStream(brainFile);

						// current brain is the best evolved neural network
						// from genetic algorithm
						NeuralNetwork brain = NeuralNetworkDrivenAgent.randomNeuralNetworkBrain();
						NeuralNetwork.marsall(brain, out);
						
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				enableControls();
			}
		});
	}

	private static void initializeResetButtonFunctionality() {
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableControls();

				int populationSize = ga.getPopulation().getSize();
				int parentalChromosomesSurviveCount = ga.getParentChromosomesSurviveCount();
				int neuronsCount = Integer.parseInt(neuralTextField.getText().split(":")[1]);
				int linksCount = Integer.parseInt(neuralTextField.getText().split(":")[2]);
				int inputCount = Integer.parseInt(neuralTextField.getText().split(":")[0]);
				// TODO: remember
//				initializeGA(populationSize, parentalChromosomesSurviveCount, 
//						ProductionAgent.genNeuralBrain(inputCount, neuronsCount, linksCount));
//				NeuralNetwork newBrain = ga.getBest();
//				for (PathFinderAgent agent : environment.filter(PathFinderAgent.class)) {
//					agent.setBrain(newBrain.clone());
//				}

				// reset population number counter
				populationNumber = 0;
				populationInfoLabel.setText("Population: " + populationNumber);

				enableControls();
			}
		});
	}

	private static void initializeEvolveButtonFunctionality() {
		evolveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableControls();
				progressBar.setVisible(true);
				progressBar.setValue(0);
				environmentPanel.getGraphics().drawImage(displayEnvironmentBufferedImage, 0, 0, null);

				String iterCountStr = evolveTextField.getText();
				if (!iterCountStr.matches("\\d+")) {
					evolveButton.setEnabled(true);
					evolveTextField.setEnabled(true);
					progressBar.setVisible(false);
					environmentPanel.getGraphics().drawImage(displayEnvironmentBufferedImage, 0, 0, null);
					return;
				}

				final int iterCount = Integer.parseInt(iterCountStr);

				new Thread(new Runnable() {
					@Override
					public void run() {
						IterartionListener<OptimizableNeuralNetwork, Double> progressListener =
								new IterartionListener<OptimizableNeuralNetwork, Double>() {
									@Override
									public void update(GeneticAlgorithm<OptimizableNeuralNetwork, Double> environment) {
										final int iteration = environment.getIteration();
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												progressBar.setValue((iteration * 100) / iterCount);
											}
										});
									}
								};

						ga.addIterationListener(progressListener);
						ga.evolve(iterCount);
						ga.removeIterationListener(progressListener);
						populationNumber += iterCount;

						NeuralNetwork newBrain = NeuralNetworkDrivenAgent.randomNeuralNetworkBrain();
						setAgentBrains(newBrain);

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								progressBar.setVisible(false);
								populationInfoLabel.setText("Population: " + populationNumber);
								enableControls();
								evolveButton.requestFocusInWindow();
							}
						});
					}
				}).start();
			}
		});
	}

	private static void disableControls() {
		evolveButton.setEnabled(false);
		evolveTextField.setEnabled(false);
		loadBrainButton.setEnabled(false);
		saveBrainButton.setEnabled(false);
		staticFoodRadioButton.setEnabled(false);
		dynamicFoodRadioButton.setEnabled(false);
	}

	private static void enableControls() {
		evolveButton.setEnabled(true);
		evolveTextField.setEnabled(true);
		loadBrainButton.setEnabled(true);
		saveBrainButton.setEnabled(true);
		staticFoodRadioButton.setEnabled(true);
		dynamicFoodRadioButton.setEnabled(true);
	}

	private static void initializeAddingFoodFunctionality() {
		environmentPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent click) {
				double x = click.getX();
				double y = click.getY();

				if (SwingUtilities.isLeftMouseButton(click)) {
					Food food = createRandomFood(1, 1);
					food.setX(x);
					food.setY(y);
					environment.addAgent(food);
				} else {
					double angle = 2 * Math.PI * random.nextDouble();
					ProductionAgent agent = new ProductionAgent(x, y, angle);
					OptimizableNeuralNetwork brain = NeuralNetworkDrivenAgent.randomNeuralNetworkBrain();
					agent.setBrain(brain);
					environment.addAgent(agent);
				}
			}
		});
	}

	private static void initializePlayPauseButtonFunctionality() {
		printButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				for (ProductionAgent agent : environment.filter(ProductionAgent.class)) {
					agent.getBrain().print();
				}
			}
		});
		
		playPauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				play = !play;
				if (play) {
					playPauseButton.setText("pause");
				} else {
					playPauseButton.setText("play");
				}
			}
		});
	}

	private static void initializeAgents(NeuralNetwork brain, int agentsCount) {
		int environmentWidth = environment.getWidth();
		int environmentHeight = environment.getHeight();
		
		for (int i = 0; i < agentsCount; i++) {
			int x = random.nextInt(environmentWidth);
			int y = random.nextInt(environmentHeight);
			double direction = random.nextDouble() * 2 * Math.PI;

			ProductionAgent agent = new ProductionAgent(x, y, direction);
			agent.setBrain(brain);

			environment.addAgent(agent);
		}
	}

	private static void initializeFood(int foodCount) {
		int environmentWidth = environment.getWidth();
		int environmentHeight = environment.getHeight();

		for (int i = 0; i < foodCount; i++) {
			Food food = createRandomFood(environmentWidth, environmentHeight);
			environment.addAgent(food);
		}
	}

	private static void initializeGeneticAlgorithm(
			int populationSize,
			int parentalChromosomesSurviveCount,
			OptimizableNeuralNetwork baseNeuralNetwork) {
		Population<OptimizableNeuralNetwork> brains = new Population<OptimizableNeuralNetwork>();

		for (int i = 0; i < (populationSize - 1); i++) {
			if (baseNeuralNetwork == null) {
//				brains.addChromosome(ProductionAgent.randomNeuralNetworkBrain());
			} else {
				brains.addChromosome(baseNeuralNetwork.mutate());
			}
		}
		if (baseNeuralNetwork != null) {
			brains.addChromosome(baseNeuralNetwork);
		} else {
//			brains.addChromosome(ProductionAgent.randomNeuralNetworkBrain());
		}

		Fitness<OptimizableNeuralNetwork, Double> fit = new TournamentEnvironmentFitness();

		ga = new GeneticAlgorithm<OptimizableNeuralNetwork, Double>(brains, fit);

		addGASystemOutIterationListener();

		ga.setParentChromosomesSurviveCount(parentalChromosomesSurviveCount);
	}

	private static void addGASystemOutIterationListener() {
		ga.addIterationListener(new IterartionListener<OptimizableNeuralNetwork, Double>() {
			@Override
			public void update(GeneticAlgorithm<OptimizableNeuralNetwork, Double> ga) {
				OptimizableNeuralNetwork bestBrain = NeuralNetworkDrivenAgent.randomNeuralNetworkBrain();
				Double fit = ga.fitness(bestBrain);
				System.out.println(ga.getIteration() + "\t" + fit);

				ga.clearCache();
			}
		});
	}

	private static void setAgentBrains(NeuralNetwork newBrain) {
		for (ProductionAgent agent : environment.filter(ProductionAgent.class)) {
			agent.setBrain(newBrain.clone());
		}
	}
	
//	private static void initializeGA(
//			int populationSize,
//			int parentalChromosomesSurviveCount,
//			OptimizableNeuralNetwork baseNeuralNetwork) {
//		Population<OptimizableNeuralNetwork> brains = new Population<OptimizableNeuralNetwork>();
//
//		for (int i = 0; i < (populationSize - 1); i++) {
//			if (baseNeuralNetwork == null) {
//				brains.addChromosome(ProductionAgent.randomNeuralNetworkBrain());
//			} else {
//				brains.addChromosome(baseNeuralNetwork.mutate());
//			}
//		}
//		if (baseNeuralNetwork != null) {
//			brains.addChromosome(baseNeuralNetwork);
//		} else {
//			brains.addChromosome(ProductionAgent.randomNeuralNetworkBrain());
//		}
//
//		Fitness<OptimizableNeuralNetwork, Double> fit = new ParkEnvFitness();
//
//		ga = new GeneticAlgorithm<OptimizableNeuralNetwork, Double>(brains, fit);
//		// Remember!!
////		Population<OptNN> brains2 = new Population<OptNN>();
////		Fitness<OptNN, Double> fit2 = new OptNNFitness();
////		GeneticAlgorithm<OptNN, Double> ga2 = new GeneticAlgorithm<OptNN, Double>(brains2, fit2);
////		ga2.getBest();
//		
//		addGASystemOutIterationListener();
//
//		ga.setParentChromosomesSurviveCount(parentalChromosomesSurviveCount);
//	}
}
