import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class TrainDepot {
    Node depot;
    ArrayList<Integer> baseGene;
    ArrayList<Genome> genomes, newGenomes;
    ArrayList<Double> fitness, newFitness;
    Integer poolSize;
    Fitness fitFunc;
    Double tournamentFrac;
    int nrOfVehicles;
    //poolsize, mutationRate, numOfGenerations, fitness variables (dist, weights, e.g.)
    //fitness function, genome, crossoverMethod
    //List of customers, list of genomes, list of fitness

    public TrainDepot(Node depot, int poolSize, int nrOfVehicles, double tournamentFrac){
        this.genomes = new ArrayList<>();
        this.depot = depot;
        this.baseGene = new ArrayList<>();
        for (Node customer : this.depot.customerList){
            baseGene.add(customer.number);
        }
        this.poolSize = poolSize;
        this.fitFunc = new Fitness(depot, nrOfVehicles);
        this.nrOfVehicles = nrOfVehicles;
        this.tournamentFrac = tournamentFrac;
    }

    public void updateFitFuncNodeNr(){
        fitFunc.updateNodeNr();
    }

    public void runOneGeneration(){  //returns solution of some format
        mateAllParents();
        genomes = newGenomes;
        newGenomes = new ArrayList<>();
        fitness = newFitness;
        newFitness = new ArrayList<>();
        mutateAll();
    }

    public boolean initializeGenomes(){  // Creates random bit arrays with all customers neighbouring this depot
        genomes = new ArrayList<>();
        fitness = new ArrayList<>();
        int failedAttempts = 0;
        int successes = poolSize;
        while(successes > 0 && failedAttempts < 100){
            if (ClarkeWrightInitialize()){
                successes -= 1;
                failedAttempts = 0;
            }
            else{
                failedAttempts ++;
            }
        }
        System.out.println("Depot "+ depot.number + " initialized " + genomes.size() + " routes. Nr of customers: " + depot.customerList.size());
        return (successes == 0);
    }

    public boolean ClarkeWrightInitialize(){
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
        for (Node customer : depot.customerList){
            routes.add(new ArrayList<>());
            routes.get(routes.size()-1).add(customer.number);
        }
        ArrayList<Double> savings = new ArrayList<>();
        ArrayList<ArrayList<Integer>> newRoutes = new ArrayList<>();
        ArrayList<ArrayList<Integer>> removeRoutes = new ArrayList<>();
        ArrayList<Integer> testRoute;
        for (int k = 0; k < depot.customerList.size() - nrOfVehicles; k++) {
            for (int i = 0; i < routes.size(); i++) {
                for (int j = 0; j < routes.size(); j++) {
                    if (i != j) {
                        testRoute = new ArrayList<Integer>();
                        for (Integer customer : routes.get(i)) {
                            testRoute.add(customer);
                        }
                        for (Integer customer : routes.get(j)) {
                            testRoute.add(customer);
                        }
                        if(fitFunc.getCostOfRoute(testRoute) != Double.POSITIVE_INFINITY){
                            Double saving = fitFunc.getCostOfRoute(routes.get(i)) + fitFunc.getCostOfRoute(routes.get(j)) - fitFunc.getCostOfRoute(testRoute);
                            if (saving != Double.NEGATIVE_INFINITY) {
                                savings.add(saving);
                                newRoutes.add((ArrayList<Integer>) testRoute.clone());
                                ArrayList<Integer> removeRoute = new ArrayList<>();
                                removeRoute.add(i);
                                removeRoute.add(j);
                                Collections.sort(removeRoute);
                                removeRoutes.add((ArrayList<Integer>) removeRoute.clone());
                            }
                        }
                    }
                }
            }
            if(savings.size() == 0){
                return false;
            }
            else {
                ArrayList<Double> savingsCopy = (ArrayList<Double>) savings.clone();
                Collections.sort(savingsCopy);
                Collections.reverse(savingsCopy);
                Double initChance = 0.5;
                while(savings.size() > 1){
                    if (initChance < ThreadLocalRandom.current().nextDouble()){
                        int removeIndex = savings.indexOf(savingsCopy.get(0));
                        savings.remove(removeIndex);
                        newRoutes.remove(removeIndex);
                        removeRoutes.remove(removeIndex);
                        savingsCopy.remove(0);
                    }
                    else {
                        break;
                    }
                }
                int chosenIndex = savings.indexOf(savingsCopy.get(0));
                routes.remove((int) removeRoutes.get(chosenIndex).get(1));
                routes.remove((int) removeRoutes.get(chosenIndex).get(0));
                routes.add(newRoutes.get(chosenIndex));
                removeRoutes = new ArrayList<>();
                newRoutes = new ArrayList<>();
                savings = new ArrayList<>();
            }
        }
        Genome g = new Genome(routes, fitFunc);
        genomes.add(g);
        fitness.add(this.fitFunc.fit(g));
        return true;
    }

    public void randomInitialize(Integer nrOfGenomes) {
        for (int i=0; i < nrOfGenomes; i++){
            ArrayList<Integer> genome = (ArrayList<Integer>) baseGene.clone();
            Collections.shuffle(genome);
            ArrayList<ArrayList<Integer>> routes = this.fitFunc.getRoutes(genome);
            if(!fitFunc.isLegalGenome(routes)){
                i-=1;
            }
            else {
                Genome g = new Genome(routes, fitFunc);
                genomes.add(g);
                fitness.add(this.fitFunc.fit(g));
            }
        }
    }
    public void mateAllParents(){
        newGenomes = new ArrayList<>();
        newFitness = new ArrayList<>();
        int nrOfElites = 2 * (int) (poolSize*0.05); // Takes a round number of elites and passes on to next gen.
        passOnElites(nrOfElites);
        crossover(poolSize-nrOfElites);;
    }

    public void mutateAll(){
        for (Genome genome : genomes){
            genome.mutate(0.05);
        }
    }

    public void crossover(Integer nrOfCrossovers){
        for (int i = 0; i < nrOfCrossovers; i+= 2){
            ArrayList<Genome> parentPair = simpleTournament((int)(poolSize*this.tournamentFrac));
            crossoverBCR(parentPair.get(0), parentPair.get(1));
        }
    }

    public ArrayList<Genome> simpleTournament(Integer tournamentSize){
        if(tournamentSize < 2){
            tournamentSize = 2;
        }
        ArrayList<Genome> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++){
            tournament.add(genomes.get(ThreadLocalRandom.current().nextInt(genomes.size())));
        }
        return getBestPair(tournament);
    }

    public void passOnElites(Integer nrOfElites){
        ArrayList<Double> bestFitsSorted = (ArrayList<Double>) fitness.clone();
        ArrayList<Double> bestFits = (ArrayList<Double>) fitness.clone();
        ArrayList<Genome> genomeCopy = (ArrayList<Genome>) genomes.clone();
        Collections.sort(bestFitsSorted);
        for(int i = 0; i<nrOfElites;i++){
            newFitness.add(bestFitsSorted.get(i));
            newGenomes.add(genomeCopy.remove(bestFits.indexOf(bestFitsSorted.get(i))));
            bestFits.remove(bestFits.indexOf(bestFitsSorted.get(i)));
        }
    }

    public ArrayList<Genome> getBestPair(ArrayList<Genome> tournament){
        ArrayList<Genome> bestPair = new ArrayList<>();
        Genome bestGenome = null;
        double bestFit = Double.POSITIVE_INFINITY;
        Genome secondBestGenome = null;
        double secondBestFit = Double.POSITIVE_INFINITY;
        for(Genome currentGenome : tournament){
            Double currentFit = fitFunc.fit(currentGenome);
            if (currentFit < bestFit){
                secondBestFit = bestFit;
                bestFit = currentFit;
                secondBestGenome = bestGenome;
                bestGenome = currentGenome;
            }
            else if (currentFit < secondBestFit){
                secondBestFit = currentFit;
                secondBestGenome = currentGenome;
            }
        }
        bestPair.add(bestGenome);
        bestPair.add(secondBestGenome);
        return bestPair;
    }

    public void crossoverBCR(Genome parent1, Genome parent2){  // Paper : Best Cost Route Crossover
        Genome child1 = new Genome(parent1.routes, fitFunc);
        Genome child2 = new Genome(parent2.routes, fitFunc);
        ArrayList<Integer> route1 = child1.getRandomRoute();
        ArrayList<Integer> route2 = child2.getRandomRoute();
        child1.removeRoute(route2);
        child2.removeRoute(route1);
        if (!child1.addCustomers(route2) || fitFunc.fit(child1) == Double.POSITIVE_INFINITY){
            newGenomes.add(parent1);
            newFitness.add(fitFunc.fit(parent1));
        }
        else{
            newGenomes.add(child1);
            newFitness.add(fitFunc.fit(child1));
        }
        if (!child2.addCustomers(route1) || fitFunc.fit(child2) == Double.POSITIVE_INFINITY){
            newGenomes.add(parent2);
            newFitness.add(fitFunc.fit(parent2));
        }
        else{
            newGenomes.add(child2);
            newFitness.add(fitFunc.fit(child2));
        }
    }

    public ArrayList<ArrayList<Integer>> copyGenome(ArrayList<ArrayList<Integer>> originalGenome){
        ArrayList<ArrayList<Integer>>  newGenome = new ArrayList<>();
        ArrayList<Integer> newRoute;
        for(ArrayList<Integer> route : originalGenome){
            newRoute = new ArrayList<>();
            for(Integer customer : route){
                newRoute.add(customer);
            }
            newGenome.add((ArrayList<Integer>) newRoute.clone());
        }
        return newGenome;
    }

    public void crossoverPMX(ArrayList<Integer> parent1, ArrayList<Integer> parent2){  // Partly mapped crossover
    }

    public ArrayList<Double> insertIntoAscList(ArrayList<Double> list, Double value, Integer maxLen){
        if(list.size() == 0){
            list.add(value);
        }
        else if (value >= list.get(list.size()-1) && list.size() < maxLen){
            list.add(value);
        }
        else if (value < list.get(list.size()-1)){
            for (int i = 0; i < list.size(); i++){
                if (value < list.get(i)){
                    list.add(i, value);
                    break;
                }
            }
        }
        if (list.size() > maxLen){
            ArrayList<Double> shortList = new ArrayList<>();
            for (int i = 0; i<maxLen; i++){
                shortList.add(list.get(i));
            }
            return shortList;
        }
        return list;
    }

    public Genome getBestGenome(){
        double bestFit = Double.POSITIVE_INFINITY;
        int bestIndex = 0;
        for (Double fit : fitness){
            if (fit < bestFit){
                bestFit = fit;
                bestIndex = fitness.indexOf(bestFit);
            }
        }
        return (genomes.get(bestIndex));
    }

    public void resetFitness(){
        fitness = new ArrayList<>();
        for(Genome genome : genomes){
            fitness.add(fitFunc.fit(genome));
        }
    }

    public Double getBestFit(){
        return fitFunc.fit(getBestGenome());
    }
}
