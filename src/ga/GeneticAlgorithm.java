package ga;

import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;

/**
 * used for evolving an optimal genome of type T
 */
public class GeneticAlgorithm<T extends Genome> {
    private Random rand;
    private int generation;
    private ArrayList<T> population;
    private ArrayList<Logger<T>> loggers;

    private SelectionMethod<T> SELECTION_METHOD;
    private int SURVIVING_CHILDREN;
    private int MAX_POPULATION_SIZE = -1;
    private int ELITE_COUNT = -1;
    private double MUTATION_RATE = -1.0;
    private double FITNESS_THRESHOLD = -1.0;

    /**
     * ensures: instantiates everything needed to run the genetic algorithm
     * 
     * @param configFileName
     * @param initialPopulation
     * @param rand
     * @param loggers
     */
    public GeneticAlgorithm(String configFileName, ArrayList<T> initialPopulation, Random rand,
            ArrayList<Logger<T>> loggers) {
        this.verifyConstants(configFileName);
        this.rand = rand;
        this.generation = 0;
        this.population = initialPopulation;
        this.SURVIVING_CHILDREN = this.MAX_POPULATION_SIZE - this.ELITE_COUNT;
        this.loggers = loggers;
    }

    /**
     * ensures: returns the current generation of the algorithm
     * 
     * @return generation
     */
    public int generation() {
        return this.generation;
    }

    /**
     * ensures: returns the current population of the algorithm
     * 
     * @return population
     */
    public ArrayList<T> population() {
        return this.population;
    }

    /**
     * ensures: returns the current fittest genome of the algorithm
     * 
     * @return fittest genome
     */
    public T fittestGenome() {
        return this.findFittestGenome(this.population);
    }

    /**
     * ensures: replaces the current population with the next generation using a
     * chosen selection method
     */
    public void nextGeneration() {
        ArrayList<T> nextGeneration = new ArrayList<T>(this.MAX_POPULATION_SIZE);
        this.partialSort(this.population, this.ELITE_COUNT);
        nextGeneration.addAll(this.population.subList(0, this.ELITE_COUNT));

        ArrayList<T> children = this.SELECTION_METHOD.produceNextGeneration(this.population);
        this.mutateAll(children);
        this.partialSort(children, this.SURVIVING_CHILDREN);
        nextGeneration.addAll(children.subList(0, this.SURVIVING_CHILDREN));

        this.generation++;
        this.population = nextGeneration;
        this.logData();
    }

    /**
     * ensures: runs the genetic algorithm for a designated number of generations or
     * until the fittest individual meets the given threshold
     * 
     * @param generations the number of generations to run
     * @return the fittest genome at the end of the simulation
     */
    public T run(int generations) {
        for (int i = 0; i < generations
                && this.findFittestGenome(this.population).fitness() < this.FITNESS_THRESHOLD; i++) {
            this.nextGeneration();
        }

        return this.findFittestGenome(this.population);
    }

    /**
     * ensures: validates and instantiates the constants from the config file
     * 
     * @param configFileName the path of the config file to use
     */
    private void verifyConstants(String configFileName) {
        PropParser.load(configFileName);

        this.MAX_POPULATION_SIZE = Integer.parseInt(PropParser.getProperty("population.size"));
        this.ELITE_COUNT = Integer.parseInt(PropParser.getProperty("elite.count"));
        this.MUTATION_RATE = Double.parseDouble(PropParser.getProperty("mutation.rate"));
        this.FITNESS_THRESHOLD = Double.parseDouble(PropParser.getProperty("fitness.threshold"));
        String selectionMethod = PropParser.getProperty("selection.method").trim();

        if (this.MAX_POPULATION_SIZE == -1) {
            System.out.println("Constant POPULATION_SIZE was not set. Default of 100 will be used");
            this.MAX_POPULATION_SIZE = 100;
        }
        if (this.ELITE_COUNT == -1) {
            System.out.println("Constant ELITE_COUNT was not set. Default of 1 will be used");
            this.ELITE_COUNT = 1;
        }
        if (this.MUTATION_RATE == -1.0) {
            System.out.println("Constant MUTATION_RATE was not set. Default of 0.1 will be used");
            this.MUTATION_RATE = 0.1;
        }
        if (this.FITNESS_THRESHOLD == -1) {
            System.out.println("Constant FITNESS_THRESHOLD was not set. Algorithm will not stop early");
            this.FITNESS_THRESHOLD = Double.MAX_VALUE;
        }

        switch (selectionMethod.toLowerCase()) {
            // case "PROPORTIONAL":
            // this.SELECTION_METHOD = this::selectProportional;
            // break;
            case "roulette":
                this.SELECTION_METHOD = this::selectRoulette;
                break;
            case "truncation":
                this.SELECTION_METHOD = this::selectTruncation;
                break;
            case "ranked":
                this.SELECTION_METHOD = this::selectRanked;
                break;
            // case "TOURNAMENT":
            // this.SELECTION_METHOD = this::selectTournament;
            // break;
            default: {
                this.SELECTION_METHOD = this::selectTruncation;
                System.out.println("Invalid (or blank) selection method chosen. Truncation will be used.");
                break;
            }
        }
    }

    // private ArrayList<T> selectProportional(ArrayList<T> genomes) {
    // return null;
    // }

    /**
     * ensures: uses roulette selection to produce the next generation of genomes
     * 
     * @param genomes the parents to produce the next generation
     * @return the next generation in an ArrayList
     */
    private ArrayList<T> selectRoulette(ArrayList<T> genomes) {
        ArrayList<Genome> children = new ArrayList<Genome>();

        for (int i = 0; i < this.MAX_POPULATION_SIZE; i += 2) {
            // we just select random indexes in the list for reproduction
            int firstParent = rand.nextInt(this.population.size());
            int secondParent = rand.nextInt(this.population.size() - 1);
            if (firstParent == secondParent) {
                secondParent++; // ensure we don't end up using the same parent twice
            }

            children.addAll(this.population.get(firstParent).crossover(this.population.get(secondParent)));
        }

        return (ArrayList<T>) children;
    }

    /**
     * ensures: uses ranked selection to produce the next generation of genomes
     * 
     * @param genomes the parents to produce the next generation
     * @return the next generation in an ArrayList
     */
    private ArrayList<T> selectRanked(ArrayList<T> genomes) {
        // sort the genomes so we can use their rank
        genomes.sort(Genome::compareTo);
        ArrayList<Genome> children = new ArrayList<Genome>();

        for (int i = 0; i < this.MAX_POPULATION_SIZE; i += 2) {
            // we need to assign each index a probability out of the sum from 1 to the
            // number of genomes
            // let x be a random variable between 0 (inclusive) and n(n+1)/2 (exclusive)
            // the function index = sqrt(2x + 1/4) - 1/2 maps x to an index in the list with
            // the right probabilities
            int firstParent = rand.nextInt((this.population.size() * (this.population.size() + 1)) >> 1);
            firstParent = (int) (Math.sqrt((firstParent << 1) + 0.25) - 0.5);

            // use the population size - 1 as if we removed the first parent
            int secondParent = rand.nextInt((this.population.size() * (this.population.size() - 1)) >> 1);
            secondParent = (int) (Math.sqrt((firstParent << 1) + 0.25) - 0.5);

            if (firstParent == secondParent) {
                secondParent++; // ensure we don't end up using the same parent twice
            }

            children.addAll(this.population.get(firstParent).crossover(this.population.get(secondParent)));
        }

        return (ArrayList<T>) children;
    }

    /**
     * ensures: uses truncation selection to produce the next generation of genomes
     * 
     * @param genomes the parents to produce the next generation
     * @return the next generation in an ArrayList
     */
    private ArrayList<T> selectTruncation(ArrayList<T> genomes) {
        // select only the top 50% of genomes
        int numParents = genomes.size() >> 1;
        this.partialSort(genomes, numParents);
        ArrayList<T> parents = new ArrayList<T>(numParents);

        // add the fittest 50% of genomes
        parents.addAll(genomes.subList(0, numParents));

        // roulette selection on the selected parents
        return this.selectRoulette(parents);
    }

    // private ArrayList<T> selectTournament(ArrayList<T> genomes) {
    // return null;
    // }

    /**
     * ensures: finds the fittest genome in a given population
     * 
     * @param genomes the population to be searched
     * @return the fittest genome
     */
    private T findFittestGenome(ArrayList<T> genomes) {
        T fittest = genomes.get(0);
        int index = 0;
        for (int i = 0; i < genomes.size(); i++) {
            if (genomes.get(i).compareTo(fittest) > 0) {
                fittest = genomes.get(i);
                index = i;
            }
        }
        Collections.swap(genomes, 0, index);
        return fittest;
    }

    /**
     * ensures: mutates all genomes in the given collection
     * 
     * @param children the genomes to mutate
     */
    private void mutateAll(ArrayList<T> children) {
        for (T child : children) {
            child.mutate(this.MUTATION_RATE);
        }
    }

    /**
     * ensures: logs all requested data for the current generation
     */
    private void logData() {
        for (Logger<T> logger : this.loggers) {
            logger.log(this.population, this.generation);
        }
    }

    /**
     * ensures: moves the fittest n genomes in a list to the front of the list
     * 
     * @param genomes     the list of genomes
     * @param numToSelect the number of genomes to move to the front
     */
    private void partialSort(ArrayList<T> genomes, int numToSelect) {
        // Ensures linear time for trivial case
        if (numToSelect == 1) {
            this.findFittestGenome(genomes);
            return;
        }

        int left = 0, right = genomes.size() - 1; // initialize the bounds of the array

        while (left != right) { // condition won't ever be met but is there for safety
            int pivotIndex = left + rand.nextInt(right - left + 1); // randomized pivot to minimize worst case frequency
            T pivot = genomes.get(pivotIndex); // store the pivot individual for comparisons
            Collections.swap(genomes, pivotIndex, right); // move the pivot to the right of the array

            int nextSmaller = left; // running index to keep track of where we need to swap
            for (int i = nextSmaller; i < right; i++) { // stops before it reaches the pivot
                if (genomes.get(i).compareTo(pivot) >= 0) { // we want the fittest individuals at the front
                    Collections.swap(genomes, nextSmaller++, i); // swap the 2 elements and increment the pointer
                }
            }
            Collections.swap(genomes, nextSmaller, right); // put the pivot in its sorted position

            if (nextSmaller + 1 < numToSelect) { // iterate on the less fit partition if it has too few members
                left = nextSmaller + 1;
            } else if (nextSmaller > numToSelect) { // iterate on the fitter partition if it has too many members
                right = nextSmaller - 1;
            } else {
                return; // stop if the fitter partition is the right size
            }
        }
    }

    @FunctionalInterface
    private interface SelectionMethod<T> {
        ArrayList<T> produceNextGeneration(ArrayList<T> genomes);
    }
}
