package com.agent.production;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import com.lagodiuk.ga.Chromosome;
import org.neuroph.core.Neuron;
import org.neuroph.nnet.Perceptron;

import com.nn.genetic.OptimizableNeuralNetwork;

@XmlRootElement
public class OptNN extends NeuralNetwork implements Chromosome<OptNN>,
		Cloneable {

	private static double weightsMutationInterval = 1;

	private static double neuronParamsMutationInterval = 1;

	@XmlTransient
	private Random random = new Random();

	public OptNN() {
		// Required by JAXB
	}

	@Override
	public List<OptNN> crossover(OptNN anotherChromosome) {
		OptNN anotherClone = anotherChromosome.clone();
		OptNN thisClone = this.clone();
		
		List<OptNN> ret = new ArrayList<OptNN>();
		ret.add(anotherClone);
		ret.add(thisClone);
		ret.add(anotherClone.mutate());
		ret.add(thisClone.mutate());
		return ret;
	}

	@Override
	public OptNN mutate() {
		OptNN mutated = this.clone();
		mutated.randomizeWeights();
		return mutated;
	}

	private void mutateWeights(List<Double> weights) {
		int weightsSize = weights.size();
		int itersCount = this.random.nextInt(weightsSize);
		if (itersCount == 0) {
			itersCount = 1;
		}
		Set<Integer> used = new HashSet<Integer>();
		for (int iter = 0; iter < itersCount; iter++) {
			int i = this.random.nextInt(weightsSize);
			if (weightsSize > 1) {
				while (used.contains(i)) {
					i = this.random.nextInt(weightsSize);
				}
			}
			double w = weights.get(i);
			w += (this.random.nextGaussian() - this.random.nextGaussian())
					* weightsMutationInterval;
			// w += (this.random.nextDouble() - this.random.nextDouble()) *
			// weightsMutationInterval;
			weights.set(i, w);
			used.add(i);
		}
	}

	@Override
	public OptNN clone() {
		OptNN clone = new OptNN();
		clone.setInputNeurons(this.getInputNeurons().clone());
		clone.setOutputNeurons(this.getOutputNeurons().clone());
		clone.setNetworkType(this.getNetworkType());
//		clone.setLearningRule(this.getLearningRule());
		for (Layer layer : this.getLayers()) {
			Layer newLayer = new Layer();
			for (Neuron neuron : layer.getNeurons()) {
				Neuron newNewron = new Neuron();
				newNewron.setError(neuron.getError());
				newNewron.setInput(neuron.getNetInput());
				newNewron.setOutput(neuron.getOutput());
				newNewron.setInputFunction(neuron.getInputFunction());
				newNewron.setTransferFunction(neuron.getTransferFunction());
				newLayer.addNeuron(newNewron);
			}
			clone.addLayer(newLayer);
		}

		// clone.activationIterations = this.activationIterations;
		// clone.neurons = new ArrayList<Neuron>(this.neurons.size());
		// for (Neuron neuron : this.neurons) {
		// clone.neurons.add(neuron.clone());
		// }
		return clone;
	}
}