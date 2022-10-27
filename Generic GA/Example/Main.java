package Example;
import ga.GeneticAlgorithm;
import ga.CSVLogger;
import ga.PropParser;
import ga.Logger;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

public class Main {

    private static final String GA_CONFIG_FILE = "Example/properties/geneticAlgorithm.properties";
    private static final String MAIN_CONFIG_FILE = "Example/properties/bitstring.properties";

    //private static final String ALLELE_FREQ_FILENAME = "AlleleFrequency.csv";
    private static final String POPULATION_FITNESS_FILENAME = "PopulationFitness.csv";

    private static int GENOME_LENGTH;
    private static int POPULATION_SIZE;
    private static int GENERATIONS;
    private static int SEED;
    private static Random RAND;
    private static String CSV_LOGS_PATH;
    private static CSVLogger LOGGER;

    public static void main(String[] args) {
        PropParser.load(MAIN_CONFIG_FILE);
        GENOME_LENGTH = Integer.parseInt(PropParser.getProperty("genome.length"));
        POPULATION_SIZE = Integer.parseInt(PropParser.getProperty("population.size"));
        GENERATIONS = Integer.parseInt(PropParser.getProperty("generations"));
        SEED = Integer.parseInt(PropParser.getProperty("seed"));
        RAND = new Random(SEED);
        CSV_LOGS_PATH = PropParser.getProperty("logs.path").trim();
        LOGGER = new CSVLogger(CSV_LOGS_PATH);

        ArrayList<EvolvableBitstring> population = generateRandomPopulation(POPULATION_SIZE);

        ArrayList<Logger<EvolvableBitstring>> loggers = createLoggers();

        GeneticAlgorithm<EvolvableBitstring> ga = new GeneticAlgorithm<EvolvableBitstring>(GA_CONFIG_FILE, population,
                RAND, loggers);

        ga.run(GENERATIONS);
        System.out.println("Generation: " + ga.generation());
        System.out.println(ga.fittestGenome().toString());
        System.out.println(ga.fittestGenome().fitness());

        LOGGER.closeAll();

    }

    public static ArrayList<EvolvableBitstring> generateRandomPopulation(int populationSize) {
        ArrayList<EvolvableBitstring> population = new ArrayList<EvolvableBitstring>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            population.add(new EvolvableBitstring(GENOME_LENGTH, RAND));
        }

        return population;
    }

    public static ArrayList<Logger<EvolvableBitstring>> createLoggers() {
        String filePrefix = "Seed" + Integer.toString(SEED) + "_";

        // String alleleFreqFile = filePrefix + ALLELE_FREQ_FILENAME;
        // ArrayList<String> alleleFreqHeaders = new ArrayList<String>(GENOME_LENGTH + 1);
        // alleleFreqHeaders.add("Generation");
        // for(int i = 0; i < GENOME_LENGTH; i++) {
        //     alleleFreqHeaders.add("Allele" + i);
        // }
        // Logger<EvolvableBitstring> alleleFreq = new Logger<EvolvableBitstring>(alleleFreqFile, LOGGER, alleleFreqHeaders, Main::logAlleleFreq);

        String populationFitnessFile = filePrefix + POPULATION_FITNESS_FILENAME;
        ArrayList<String> populationFitnessHeaders = new ArrayList<String>(Arrays.asList("Generation", "Max Fitness", "Avg Fitness", "Min Fitness"));
        Logger<EvolvableBitstring> populationFitness = new Logger<EvolvableBitstring>(populationFitnessFile, LOGGER, populationFitnessHeaders, Main::logPopulationFitness);

        return new ArrayList<Logger<EvolvableBitstring>>(Arrays.asList(populationFitness));
    }

    // private static void logAlleleFreq(CSVLogger logger, String filename, ArrayList<EvolvableBitstring> population,
    //         int generation) {
    //     StringBuilder row = new StringBuilder();
    //     row.append(generation + ",");
    //     for (int allele = 0; allele < GENOME_LENGTH; allele++) {
    //         double total = 0;
    //         for (EvolvableBitstring genome : population) {
    //             total += genome.genome().get(allele);
    //         }
    //         row.append(total / GENOME_LENGTH + ",");
    //     }
    //     logger.appendRowToFile(filename, row.toString());
    // }

    public static void logPopulationFitness(CSVLogger logger, String filename,
            ArrayList<EvolvableBitstring> population,
            int generation) {

        StringBuilder row = new StringBuilder();
        row.append(generation + ",");
        double max = population.get(0).fitness();
        double min = population.get(0).fitness();
        double total = 0;

        for (EvolvableBitstring genome : population) {
            if (max < genome.fitness()) {
                max = genome.fitness();
            }
            if (min > genome.fitness()) {
                min = genome.fitness();
            }
            total += genome.fitness();
        }

        row.append(max + "," + total / population.size() + "," + min + ",");
        logger.appendRowToFile(filename, row.toString());
    }
}
