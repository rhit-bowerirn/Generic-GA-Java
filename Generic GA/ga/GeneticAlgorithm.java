package ga;
import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;

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
    
    

    public GeneticAlgorithm(String configFileName, ArrayList<T> initialPopulation, Random rand, ArrayList<Logger<T>> loggers) {
        this.verifyConstants(configFileName);
        this.rand = rand;
        this.generation = 0;
        this.population = initialPopulation;
        this.SURVIVING_CHILDREN = this.MAX_POPULATION_SIZE - this.ELITE_COUNT;
        this.loggers = loggers;
    }

    public int generation() {
        return this.generation;
    }

    public ArrayList<T> population() {
        return this.population;
    }

    public T fittestGenome() {
        return this.findFittestGenome(this.population);
    }

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

    public T run(int generations) {
        for(int i = 0; i < generations && this.findFittestGenome(this.population).fitness() < this.FITNESS_THRESHOLD; i++) {
            this.nextGeneration();
        }

        return this.findFittestGenome(this.population);
    }









    private void verifyConstants(String configFileName) {
        PropParser.load(configFileName);

        this.MAX_POPULATION_SIZE = Integer.parseInt(PropParser.getProperty("population.size"));
        this.ELITE_COUNT = Integer.parseInt(PropParser.getProperty("elite.count"));
        this.MUTATION_RATE = Double.parseDouble(PropParser.getProperty("mutation.rate"));
        this.FITNESS_THRESHOLD = Double.parseDouble(PropParser.getProperty("fitness.threshold"));
        String selectionMethod = PropParser.getProperty("selection.method").trim();

        if(this.MAX_POPULATION_SIZE == -1) {
            System.out.println("Constant POPULATION_SIZE was not set. Default of 100 will be used");
            this.MAX_POPULATION_SIZE = 100;
        }
        if(this.ELITE_COUNT == -1) {
            System.out.println("Constant ELITE_COUNT was not set. Default of 1 will be used");
            this.ELITE_COUNT = 1;
        }
        if(this.MUTATION_RATE == -1.0) {
            System.out.println("Constant MUTATION_RATE was not set. Default of 0.1 will be used");
            this.MUTATION_RATE = 0.1;
        }
        if(this.FITNESS_THRESHOLD == -1) {
            System.out.println("Constant FITNESS_THRESHOLD was not set. Algorithm will not stop early");
            this.FITNESS_THRESHOLD = Double.MAX_VALUE;
        }

        switch (selectionMethod.toLowerCase()) {
            // case "PROPORTIONAL":
            //     this.SELECTION_METHOD = this::selectProportional;
            //     break;
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
            //     this.SELECTION_METHOD = this::selectTournament;
            //     break;
            default:
            {
                this.SELECTION_METHOD = this::selectTruncation; 
                System.out.println("Invalid (or blank) selection method chosen. Truncation will be used.");
                break;
            }
        }
    }

    // private ArrayList<T> selectProportional(ArrayList<T> genomes) {
    //     return null;
    // }

    private ArrayList<T> selectRoulette(ArrayList<T> genomes) {
        ArrayList<Genome> children = new ArrayList<Genome>();

        for (int i = 0; i < this.MAX_POPULATION_SIZE; i += 2) { // generate random pairs of children
            int firstParent = rand.nextInt(this.population.size());
            int secondParent = rand.nextInt(this.population.size() - 1);
            if (firstParent == secondParent) {
                secondParent++; // ensure we don't end up using the same parent twice
            }

            children.addAll(this.population.get(firstParent).crossover(this.population.get(secondParent)));
        }

        return (ArrayList<T>) children;
    }

    private ArrayList<T> selectRanked(ArrayList<T> genomes) {
        genomes.sort(Genome::compareTo);
        ArrayList<Genome> children = new ArrayList<Genome>();

        for (int i = 0; i < this.MAX_POPULATION_SIZE; i += 2) { // generate random pairs of children
            int firstParent = rand.nextInt((this.population.size() * (this.population.size() + 1)) >> 1); 
            firstParent = (int) (Math.sqrt((firstParent << 1) + 0.25) - 0.5);

            int secondParent = rand.nextInt((this.population.size() * (this.population.size() - 1)) >> 1); 
            secondParent = (int) (Math.sqrt((firstParent << 1) + 0.25) - 0.5);


            if (firstParent == secondParent) {
                secondParent++; // ensure we don't end up using the same parent twice
            }

            children.addAll(this.population.get(firstParent).crossover(this.population.get(secondParent)));
        }

        return (ArrayList<T>) children;
    }

    private ArrayList<T> selectTruncation(ArrayList<T> genomes) {
        int numParents = genomes.size() >> 1;
        this.partialSort(genomes, numParents);
        ArrayList<T> parents = new ArrayList<T>(numParents);
        parents.addAll(genomes.subList(0, numParents));

        return this.selectRoulette(parents);
    }

    // private ArrayList<T> selectTournament(ArrayList<T> genomes) {
    //     return null;
    // }


    private T findFittestGenome(ArrayList<T> genomes){
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

    private void mutateAll(ArrayList<T> children) {
        for(T child : children) {
            child.mutate(this.MUTATION_RATE);
        }
    }

    private void logData() {
        for (Logger<T> logger : this.loggers) {
            logger.log(this.population, this.generation);
        }
    }

    // partially quicksorting the population to get the best n individuals in front
    private void partialSort(ArrayList<T> genomes, int numToSelect) {
        //Ensures linear time for trivial case
        if(numToSelect == 1) {
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
