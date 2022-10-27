package Example;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import ga.Genome;


public class EvolvableBitstring extends Genome{
    private ArrayList<Integer> genome;

    public EvolvableBitstring(int genomeLength, Random rand) {
        super(genomeLength, rand);
        this.genome = this.createRandomGenome(genomeLength); 
        this.calculateFitness();
    }

    public EvolvableBitstring(ArrayList<Integer> genome, Random rand) {
        super(genome.size(), rand);
        this.genome = genome;
    }

    public EvolvableBitstring(EvolvableBitstring mother, EvolvableBitstring father, int crossoverIndex) {
        super(mother.size(), mother.rand);
        this.genome = new ArrayList<Integer>(this.size);
        
        this.genome.addAll(mother.genome().subList(0, crossoverIndex));
        this.genome.addAll(father.genome().subList(crossoverIndex, this.size));

        this.calculateFitness();
    }

    private ArrayList<Integer> createRandomGenome(int genomeLength) {
        ArrayList<Integer> genome = new ArrayList<Integer>(genomeLength);
        for(int gene = 0; gene < genomeLength; gene++) {
            genome.add(rand.nextInt(2));
        }
        return genome;
    }

    public ArrayList<Integer> genome() {
        return this.genome;
    }

    public void calculateFitness() {
        double fitness = 0;
        for (int gene : this.genome) {
            fitness += gene;
        }

        this.fitness = fitness / this.size;
    }

    @Override
    public ArrayList<Genome> crossover(Genome other) {
        int crossoverIndex = this.rand.nextInt(this.size);
        EvolvableBitstring child1 = new EvolvableBitstring(this, (EvolvableBitstring) other, crossoverIndex);
        EvolvableBitstring child2 = new EvolvableBitstring((EvolvableBitstring) other, this, crossoverIndex);

        return new ArrayList<Genome>(Arrays.asList(child1, child2));
    }

    @Override
    public void mutate(double mutationRate) {
        for (int gene = 0; gene < this.size; gene++) {
            if (this.rand.nextDouble() < mutationRate) {
                this.genome.set(gene, this.genome.get(gene) ^ 1);
            }
        }

        this.calculateFitness();
        
    }

    @Override
    public String toString() {
        return this.genome.toString();
    }
    
}
