package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import metaheuristics.ga.AbstractGA;
import problems.qbf.QBF;
import solutions.Solution;
import triple.Triple;

/**
 * Metaheuristic GA (Genetic Algorithm) for
 * obtaining an optimal solution to a QBF (Quadractive Binary Function --
 * {@link #QuadracticBinaryFunction}). 
 * 
 * @author ccavellucci, fusberti
 */
public class GA_QBFTP_DM extends AbstractGA<Integer, Integer> {

    private Triple[] triples;

	/**
	 * Constructor for the GA_QBF class. The QBF objective function is passed as
	 * argument for the superclass constructor.
	 * 
	 * @param generations
	 *            Maximum number of generations.
	 * @param popSize
	 *            Size of the population.
	 * @param mutationRate
	 *            The mutation rate.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public GA_QBFTP_DM(Integer generations, Integer popSize, Double mutationRate, String filename) throws IOException {
		super(new QBF(filename), generations, popSize, mutationRate);

        generateTriples();
	}
	
    /**
     * Linear congruent function l used to generate pseudo-random numbers.
     */
    public int l(int pi1, int pi2, int u, int n) {
        return 1 + ((pi1 * u + pi2) % n);
    }

    /**
     * Function g used to generate pseudo-random numbers
     */
    public int g(int u, int n) {
        int pi1 = 131;
        int pi2 = 1031;
        int lU = l(pi1, pi2, u, n);

        if (lU != u) {
            return lU;
        } else {
            return 1 + (lU % n);
        }
    }

    /**
     * Function h used to generate pseudo-random numbers
     */
    public int h(int u, int n) {
        int pi1 = 193;
        int pi2 = 1093;
        int lU = l(pi1, pi2, u, n);
        int gU = g(u, n);

        if (lU != u && lU != gU) {
            return lU;
        } else if ((1 + (lU % n)) != u && (1 + (lU % n)) != gU) {
            return 1 + (lU % n);
        } else {
            return 1 + ((lU + 1) % n);
        }
    }

    /**
     * That method generates a list of objects (Triple Elements) that represents
     * each binary variable that could be inserted into a prohibited triple
     */

    /**
     * Method that generates a list of n prohibited triples using l g and h
     * functions
     */
    private void generateTriples() {
        int n = ObjFunction.getDomainSize();
        this.triples = new Triple[ObjFunction.getDomainSize()];

        for (int u = 1; u <= n; u++) {
            Integer te1, te2, te3;
            Triple novaTripla;

            te1 = u - 1;
            te2 = g(u - 1, n) - 1;
            te3 = h(u - 1, n) - 1;
            novaTripla = new Triple(te1, te2, te3);

            Collections.sort(novaTripla.getElements());

            //novaTripla.printTriple();
            this.triples[u - 1] = novaTripla;
        }
        
    }


	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#decode(metaheuristics.ga.AbstractGA.
	 * Chromosome)
	 */
	@Override
	protected Solution<Integer> decode(Chromosome chromosome) {

		Solution<Integer> solution = createEmptySol();
		for (int locus = 0; locus < chromosome.size(); locus++) {
			if (chromosome.get(locus) == 1) {
				solution.add(new Integer(locus));
			}
		}

		ObjFunction.evaluate(solution);
		return solution;
	}
	
	private ArrayList<Triple> violate(Chromosome chromosome) {
		ArrayList<Triple> violatedTriples = new ArrayList<Triple>();
		for (Triple trip : triples) {
			if (chromosome.get(trip.getElements().get(0)) == 1 &&
				chromosome.get(trip.getElements().get(1)) == 1 &&
				chromosome.get(trip.getElements().get(2)) == 1						
					) {
				violatedTriples.add(trip);
			}
		}
		return violatedTriples;
	}
	
	private void fix(Chromosome chromosome) {
		ArrayList<Triple> violatedTriples = violate(chromosome);
		for (Triple trip : violatedTriples) {
			int value = rng.nextInt(3);
			chromosome.set(trip.getElements().get(value), 0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#generateRandomChromosome()
	 */
	@Override
	protected Chromosome generateRandomChromosome() {

		Chromosome chromosome = new Chromosome();
		for (int i = 0; i < chromosomeSize; i++) {
			chromosome.add(rng.nextInt(2));
		}
		
		fix(chromosome);

		return chromosome;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#fitness(metaheuristics.ga.AbstractGA.
	 * Chromosome)
	 */
	@Override
	protected Double fitness(Chromosome chromosome) {

		return decode(chromosome).cost;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * metaheuristics.ga.AbstractGA#mutateGene(metaheuristics.ga.AbstractGA.
	 * Chromosome, java.lang.Integer)
	 */
	@Override
	protected void mutateGene(Chromosome chromosome, Integer locus) {
		chromosome.set(locus, 1 - chromosome.get(locus));
	}
	
	@Override
	protected Population mutate(Population offsprings) {
		for (Chromosome c : offsprings) {
			for (int locus = 0; locus < chromosomeSize; locus++) {
				if (rng.nextDouble() < mutationRate) {
					mutateGene(c, locus);
				}
			}
			fix(c);			
		}
		return offsprings;
	}
	
	@Override
	protected AbstractGA<Integer, Integer>.Population crossover(AbstractGA<Integer, Integer>.Population parents) {
		
		Population offsprings = new Population();

		for (int i = 0; i < popSize; i = i + 2) {

			Chromosome parent1 = parents.get(i);
			Chromosome parent2 = parents.get(i + 1);
			int[] xor = new int[chromosomeSize];
			int count = 0;
			int cross_start = chromosomeSize + 1;
			int cross_end =  0;
			
			int crosspoint1;
			int crosspoint2;
			
			
			
			int k = 0;
			for (Integer g : parent1) {
				if (g != parent2.get(k)) {
					xor[k] = 1;
					count++;
					if (k < cross_start) cross_start = k;
					if (k > cross_end) cross_end = k;
				} else {
					xor[k] = 0;
				}
				
				k++;
			}
			// System.out.print("XOR = ");
			// for (int j = 0; j < chromosomeSize; j++) {
			// System.out.print(xor[j] + " ");
			// }
			// System.out.println();
			
			
			// System.out.println("count = " + count);
			// System.out.println("start = " + cross_start);
			// System.out.println("end = " + cross_end);
			
			if (count == 0) {
				crosspoint1 = 0;
				crosspoint2 = 0;	
			} else {
				crosspoint1 = cross_start + rng.nextInt(cross_end - cross_start + 1);
				crosspoint2 = crosspoint1 + rng.nextInt(cross_end - crosspoint1 + 1);
			}
			System.out.println("cross1 = " + crosspoint1);
			System.out.println("cross2 = " + crosspoint2);
			
			Chromosome offspring1 = new Chromosome();
			Chromosome offspring2 = new Chromosome();

			for (int j = 0; j < chromosomeSize; j++) {
				if (j >= crosspoint1 && j < crosspoint2) {
					offspring1.add(parent2.get(j));
					offspring2.add(parent1.get(j));
				} else {
					offspring1.add(parent1.get(j));
					offspring2.add(parent2.get(j));
				}
			}

			offsprings.add(offspring1);
			offsprings.add(offspring2);

		}

		return offsprings;

	}

	/**
	 * A main method used for testing the GA metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();
		GA_QBFTP ga = new GA_QBFTP(1000, 1000, 1.0 / 200.0, "../instances/qbf" + args[0]);
		Solution<Integer> bestSol = ga.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime = System.currentTimeMillis();	
		long totalTime = endTime - startTime;
		System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");

	}

}
