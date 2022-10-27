package ga;

import java.util.Random;
import java.util.ArrayList;

/**
 * a class to encapsulate a genome representation
 */
public abstract class Genome implements Comparable<Genome> {
    protected int size;
    protected double fitness;
    protected Random rand;

    /**
     * ensures: instantiates an instance of a genome
     * 
     * @param size
     * @param rand
     */
    public Genome(int size, Random rand) {
        this.rand = rand;
        this.size = size;
        this.fitness = 0;
    };

    /**
     * ensures: returns the fitness of the individual
     * requires: each genome is responsible for keeping its fitness up to date
     * 
     * @return the fitness of this genome
     */
    public double fitness() {
        return this.fitness;
    }

    /**
     * ensures: returns the size of this genome
     * 
     * @return the size of this genome
     */
    public int size() {
        return this.size;
    }

    @Override
    public int compareTo(Genome other) {
        if (this.fitness > other.fitness()) {
            return 1;
        } else if (this.fitness < other.fitness()) {
            return -1;
        }
        return 0;
    }

    // every genome needs the ability to crossover with another
    public abstract ArrayList<Genome> crossover(Genome other);

    // every genome needs the ability to mutate itself
    public abstract void mutate(double mutationRate);

    public abstract String toString();
}