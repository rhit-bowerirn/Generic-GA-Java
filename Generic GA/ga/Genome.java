package ga;
import java.util.Random;
import java.util.ArrayList;

public abstract class Genome implements Comparable<Genome>{
    protected int size;
    protected double fitness;
    protected Random rand;

    public Genome(int size, Random rand) {
        this.rand = rand;
        this.size = size;
        this.fitness = 0;
    };

    public double fitness() {
        return this.fitness;
    }

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

    public abstract ArrayList<Genome> crossover(Genome other);

    public abstract void mutate(double mutationRate);

    public abstract String toString();
}