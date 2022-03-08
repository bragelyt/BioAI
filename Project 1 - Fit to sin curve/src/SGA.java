import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SGA {

    int poolSize;
    int chromosomeLen;
    double mutationRate;
    int numOfGenerations;
    boolean crowding;
    boolean probCrowding;
    boolean nitching;
    ArrayList<Individual> newIndividuals;
    ArrayList<Double> individualWeights;
    FitnessFunction f;
    Random rand;
    double avgFit;

    public SGA(int poolSize, int chromosomeLen, double mutationRate, int numOfGenerations, boolean crowding, boolean probCrowding, boolean nitching){
        this.poolSize = poolSize;
        this.chromosomeLen = chromosomeLen;
        this.mutationRate = mutationRate;
        this.numOfGenerations = numOfGenerations;
        this.crowding = crowding;
        this.probCrowding = probCrowding;
        this.nitching = nitching;
        this.f = new FitnessFunction(chromosomeLen);
        this.newIndividuals = new ArrayList<Individual>();
        for (int i=0; i<poolSize; i++){
            this.newIndividuals.add(new Individual(chromosomeLen, null));
        }
        this.rand = new Random();
        run();
    }

    public void run(){
        updateRoulette();
        ArrayList<Integer> genPlots = new ArrayList<>(Arrays.asList(1, 2, 3, 5, 8, 13, 21, 34));
        plotFitness(0);
        for (int i = 1; i< this.numOfGenerations; i++){
            pickParents();
            updateRoulette();
            System.out.println(i);
            if(genPlots.contains(i)){
                plotFitness(i);
            }
        }
        plotFitness(this.numOfGenerations);
    }

    public void updateRoulette(){
        double totalFit = 0;
        for (Individual individual : this.newIndividuals){
            double fitness = f.Fitness(individual) + 1;
            if(this.nitching){
                fitness = fitness/sharingRate(individual);
            }
            totalFit += fitness;
        }
        if(this.nitching){  //For printing avg fit
            double actualTotal = 0.0;
            for(Individual individual : this.newIndividuals){
                actualTotal += this.f.Fitness(individual);
            }
            avgFit = actualTotal/this.poolSize;
        }
        else{
            avgFit = (totalFit/this.poolSize -1);
        }
        System.out.println("avg fit: " + avgFit);
        this.individualWeights = new ArrayList<>();
        double accumulativeProb = 0;
        for (Individual individual : this.newIndividuals){
            double fitness = f.Fitness(individual) + 1;
            if(this.nitching){
                fitness = fitness/sharingRate(individual);
            }
            double prob = (fitness)/totalFit;
            accumulativeProb += prob;
            this.individualWeights.add(accumulativeProb);
        }
    }

    public void pickParents(){
        ArrayList<Individual> parents = new ArrayList<>();
        for (int i = 0; i < this.poolSize; i++){
            parents.add(pickFromRoulette());
        }
        mateParents(parents);
    }

    public void mateParents(ArrayList<Individual> parents){
        this.newIndividuals = new ArrayList<Individual>();
        for (int i = 0; i < parents.size(); i += 2) { //Pairs parrents
            if(i != parents.size() -1){ //For each pair
                int splitPlace = ThreadLocalRandom.current().nextInt(1, this.chromosomeLen-1);
                BitSet c_1BitSet = new BitSet(this.chromosomeLen);
                BitSet c_2BitSet = new BitSet(this.chromosomeLen);
                for (int j = 0; j < splitPlace; j++){   //Copies up to splitPlace
                    if (parents.get(i).chromosome.get(j)){
                        c_1BitSet.set(j);
                    }
                    if (parents.get(i+1).chromosome.get(j)){
                        c_2BitSet.set(j);
                    }
                }
                for (int k = splitPlace; k< this.chromosomeLen; k++){   //Copies after the splitPlace
                    if (parents.get(i).chromosome.get(k)){
                        c_2BitSet.set(k);
                    }
                    if (parents.get(i+1).chromosome.get(k)){
                        c_1BitSet.set(k);
                    }
                }
                Individual c_1 = new Individual(this.chromosomeLen, c_1BitSet);
                Individual c_2 = new Individual(this.chromosomeLen, c_2BitSet);
                mutateIndividual(c_1);
                mutateIndividual(c_2);
                if(this.crowding){  //Need some extra logic for crowding
                    HashMap<Double, Individual> familyFit = new HashMap<>();
                    ArrayList<Individual> family = new ArrayList<>(Arrays.asList(parents.get(i), parents.get(i+1), c_1, c_2));
                    ArrayList<Double> fitnesses = new ArrayList<>();
                    double fitness;
                    for (Individual individual : family){
                        fitness = f.Fitness(individual);
                        familyFit.put(fitness, individual);
                        fitnesses.add(fitness);
                    }
                    if (this.probCrowding) {  //The chance for survival are calculated by size of fitness roulette style.
                        probCrowding(family, fitnesses);
                    }
                    else {  //Strict crowding default. Best two lives on 100%.
                        strictCrowding(family, familyFit);
                    }
                }
                else {  //If not crowding, add entire family
                    this.newIndividuals.add(c_1);
                    this.newIndividuals.add(c_2);
                }
            }
        }
    }

    public void probCrowding(ArrayList<Individual> family, ArrayList<Double> fitnesses){
        Double totalFit = fitnesses.stream().mapToDouble(a -> a).sum();
        ArrayList<Double> familyWeights = new ArrayList<>();
        double accumulativeProb = 0;
        for (Individual individual : family){
            double prob = (f.Fitness(individual)+1)/totalFit;
            familyWeights.add(prob+accumulativeProb);
            accumulativeProb += prob;
        }
        Double spin = this.rand.nextDouble();
        for (int j = 0; j <2; j++){
            for (int k = 0; k <familyWeights.size(); k++){
                if(spin <= familyWeights.get(k)){
                    this.newIndividuals.add(family.get(k));
                    break;
                }
            }
        }
    }

    public void strictCrowding(ArrayList<Individual> family, HashMap<Double, Individual> familyFit){
        List<Double> sortedFitness = familyFit.keySet().stream().sorted().collect(Collectors.toList());
        Collections.reverse(sortedFitness);
        int bestNr = 0;
        for (Individual individual : family) {  //Check to get around using hashMaps as equal fitness of
            if (this.f.Fitness(individual) == sortedFitness.get(0)) { //two individuals register only once
                bestNr++;
            }
        }
        this.newIndividuals.add(familyFit.get(sortedFitness.get(0)));
        if (bestNr > 1) {   //If the two or more the same individual are best and equal, add both.
            this.newIndividuals.add(familyFit.get(sortedFitness.get(0)));
        } else {
            this.newIndividuals.add(familyFit.get(sortedFitness.get(1)));
        }
    }

    public Double sharingRate(Individual individual){
        double shareDistance = 1.57;
        double nc = 0.0;
        double alpha = 1;
        double pos = f.getScaledValue(individual);
        for (Individual neighbour : this.newIndividuals){
            double distance = Math.abs(f.getScaledValue(neighbour) - pos);
            if (distance < shareDistance){
                nc += Math.pow((1 - distance/shareDistance),alpha);
            }
        }
        return nc;
    }

    public Individual pickFromRoulette(){   //Hmm tror dette skal være riktig
        Double spin = this.rand.nextDouble();
        for (int i = 0; i<this.individualWeights.size(); i++){
            if(spin <= this.individualWeights.get(i)){
                return (this.newIndividuals.get(i));
            }
        }
        System.out.println("Overflow roulette spin: " + spin);
        return null;
    }

    public void mutateIndividual(Individual individual){
        double mutationRate = this.mutationRate;
        while (mutationRate > 1) {
            mutateGene(individual);
            mutationRate -= 1;
        }
        if (mutationRate > 0){
            double restMutation = ThreadLocalRandom.current().nextDouble();
            if (restMutation <= mutationRate){
                mutateGene(individual);
            }
        }
    }

    public void mutateGene(Individual individual){
        int geneIndex = ThreadLocalRandom.current().nextInt(0, this.chromosomeLen);
        if(individual.chromosome.get(geneIndex)){
            individual.chromosome.clear(geneIndex);
        }
        else{
            individual.chromosome.set(geneIndex);
        }
    }

    public void plotFitness(int generation){
        ArrayList<Double> xCoords = new ArrayList<>();
        ArrayList<Double> yCoords = new ArrayList<>();
        for(Individual ind : this.newIndividuals){
            xCoords.add(this.f.getScaledValue(ind));
            yCoords.add(this.f.Fitness(ind));
        }
        new Graph(xCoords, yCoords, generation, this.avgFit);
    }
}
