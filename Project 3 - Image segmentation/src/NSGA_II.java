import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NSGA_II {

    TreeHandler treeHandler;
    FitMinister fitMinister;
    Integer imageHeight, imageWidth, poolSize, maxGeneration, crossoverSections;
    Double mutationRate;
    String dataFile;
    ArrayList<ArrayList<ArrayList<Integer>>> rgbMatrix;

    ArrayList<Genome> currentPopulation, nextPopulation;

    ImageDrawer imageDrawer;

    Map<Integer, ArrayList<Integer>> neighMap = Stream.of(new Object[][] {
            {1, new ArrayList<>(Arrays.asList( 0, 1))},
            {2, new ArrayList<>(Arrays.asList( 0,-1))},
            {3, new ArrayList<>(Arrays.asList(-1, 0))},
            {4, new ArrayList<>(Arrays.asList( 1, 0))},
            {5, new ArrayList<>(Arrays.asList( 1,-1))},
            {6, new ArrayList<>(Arrays.asList( 1, 1))},
            {7, new ArrayList<>(Arrays.asList(-1, 1))},
            {8, new ArrayList<>(Arrays.asList(-1,-1))}
    }).collect(Collectors.toMap(data -> (int) data[0], data -> (ArrayList<Integer>) data[1]));

    public NSGA_II(String dataFile){
        this.dataFile = dataFile;
        ImageReader imageReader = new ImageReader(dataFile+".jpg");
        this.rgbMatrix = imageReader.getRGBMatrix();
        // Variables and helpers
        this.imageHeight = rgbMatrix.size();
        this.imageWidth = rgbMatrix.get(0).size();
        this.mutationRate = 0.001;
        this.poolSize = 25;  // 50
        this.maxGeneration = 10;  // 10
        this.crossoverSections = 2;

        //Classes
        this.fitMinister = new FitMinister(rgbMatrix, neighMap);
        this.treeHandler = new TreeHandler(rgbMatrix, this.fitMinister, this.neighMap);

        this.imageDrawer = new ImageDrawer(imageWidth, imageHeight);
    }

    public void run(){
        this.currentPopulation = new ArrayList<>();
        initializePopulation();
        nextPopulation = new ArrayList<>(currentPopulation);
        getRanks();
        for (int generation = 0; generation < maxGeneration; generation++) {
            System.out.println("Starting generation nr " + generation);
            while (nextPopulation.size() < poolSize*2){
                ArrayList<Genome> tournamentWinners = getTournamentWinners();
                crossover(tournamentWinners.get(0), tournamentWinners.get(1), crossoverSections);
            }
            currentPopulation = new ArrayList<>();
            ArrayList<ArrayList<Genome>> ranks = getRanks();
            System.out.println("ranks.size(): " + ranks.size());
            System.out.println("ranks.get(0).size(): " + ranks.get(0).size());
            for(int rank = 0; rank < ranks.size(); rank++){
                if (ranks.get(rank).size() + currentPopulation.size() <= poolSize){
                    currentPopulation.addAll(ranks.get(rank));
                }
                else if(currentPopulation.size() < poolSize){
                    int index = 0;
                    Collections.shuffle(ranks.get(rank));
                    while (currentPopulation.size() < poolSize){
                        currentPopulation.add(ranks.get(rank).get(index));
                        index += 1;
                    }
                }
                else{
                    break;
                }
            }
            nextPopulation = new ArrayList<>(currentPopulation);
        }
        // Draws image
        ArrayList<ArrayList<Genome>> ranks = getRanks();
        int index = 0;
        for(Genome genome : ranks.get(0)) {
            double random = Math.random();
            imageDrawer.drawOnToImage(treeHandler.getBorderPixels(genome.genomeTree),  dataFile + "_" + index + "_" + random, rgbMatrix);
            imageDrawer.createImage(treeHandler.getBorderPixels(genome.genomeTree), dataFile + "-" + index + "-" + random);
            index ++;
        }
    }

    public void weightedRun(){
        this.currentPopulation = new ArrayList<>();
        initializePopulation();
        nextPopulation = new ArrayList<>();
        for (int generation = 0; generation < maxGeneration; generation++) {
            System.out.println("Starting generation nr " + generation);
            while (nextPopulation.size() < poolSize){
                ArrayList<Genome> tournamentWinners = getWeightedTournamentWinners();
                crossover(tournamentWinners.get(0), tournamentWinners.get(1), crossoverSections);
            }
            currentPopulation = new ArrayList<>(nextPopulation);
            nextPopulation = new ArrayList<>();
        }
        // Draws image
        for(Genome genome : currentPopulation) {
            imageDrawer.drawOnToImage(treeHandler.getBorderPixels(genome.genomeTree),  dataFile + "_" + Math.random(), rgbMatrix);
            imageDrawer.createImage(treeHandler.getBorderPixels(genome.genomeTree), dataFile + "-" + Math.random());
        }
    }

    public void mutateGenome(Genome genome){
        genome.mutateGenes();
        alignTreeWithGenome(genome);
    }

    public void alignTreeWithGenome(Genome genome){
        HashMap<ArrayList<Integer>, PixelNode> genomeTree = treeHandler.getGenomeTree(genome.genome);
        ArrayList<Double> edgeAndConnect = fitMinister.getEdgeValueAndConnectivity(treeHandler.getBorderPixels(genomeTree), genomeTree);
        Double deviation = fitMinister.getOverallDeviation(genomeTree);
        genome.setFitness(genomeTree, edgeAndConnect.get(0), edgeAndConnect.get(1), deviation);
    }

    public void alignTreeWithGenome(Genome genome, HashMap<ArrayList<Integer>, PixelNode> genomeTree){
        ArrayList<Double> edgeAndConnect = fitMinister.getEdgeValueAndConnectivity(treeHandler.getBorderPixels(genomeTree), genomeTree);
        Double deviation = fitMinister.getOverallDeviation(genomeTree);
        genome.setFitness(genomeTree, edgeAndConnect.get(0), edgeAndConnect.get(1), deviation);
    }

    public ArrayList<Genome> getTournamentWinners(){  // currentPopulation. Ranks should be up to date.
        ArrayList<Genome> winners = new ArrayList<>();
        for(int tournaments = 0; tournaments < 2; tournaments++){
            Genome participant1 = currentPopulation.get((int)(Math.random()*currentPopulation.size()));
            Genome participant2 = currentPopulation.get((int)(Math.random()*currentPopulation.size()));
            if(participant1.rank < participant2.rank){
                winners.add(participant1);
            }
            else{
                winners.add(participant2);
            }
        }
        return winners;
    }

    public ArrayList<Genome> getWeightedTournamentWinners(){
        ArrayList<Genome> winners = new ArrayList<>();
        ArrayList<ArrayList<Double>> fitnesses = new ArrayList<>();
        for (int i = 0; i<3; i++){
            fitnesses.add(new ArrayList<>(Arrays.asList(0.0)));
        }
        for(Genome genome : currentPopulation){
            fitnesses.get(0).add(genome.edgeValue);
            fitnesses.get(1).add(genome.connectivity);
            fitnesses.get(2).add(genome.overallDeviation);
        }
        Collections.sort(fitnesses.get(0));
        Collections.sort(fitnesses.get(1));
        Collections.sort(fitnesses.get(2));
        Double totalFitness = 0.0;
        for(Genome genome : currentPopulation){
            totalFitness += (-genome.edgeValue) * (fitnesses.get(0).get(fitnesses.get(0).size()-1)-fitnesses.get(0).get(0));
            totalFitness += (- genome.connectivity + fitnesses.get(1).get(fitnesses.get(1).size()-1)) * (fitnesses.get(1).get(fitnesses.get(1).size()-1)-fitnesses.get(1).get(0));
            totalFitness += (- genome.overallDeviation + fitnesses.get(2).get(fitnesses.get(2).size()-1)) * (fitnesses.get(2).get(fitnesses.get(2).size()-1)-fitnesses.get(2).get(0));
        }
        for (int tournaments = 0; tournaments <2; tournaments++){
            Double genomeFit = Math.random()*totalFitness;
            Double runningFit = 0.0;
            for(Genome genome : currentPopulation){
                runningFit += (-genome.edgeValue) * (fitnesses.get(0).get(fitnesses.get(0).size()-1)-fitnesses.get(0).get(0));
                runningFit += (- genome.connectivity + fitnesses.get(1).get(fitnesses.get(1).size()-1)) * (fitnesses.get(1).get(fitnesses.get(1).size()-1)-fitnesses.get(1).get(0));
                runningFit += (- genome.overallDeviation + fitnesses.get(2).get(fitnesses.get(2).size()-1)) * (fitnesses.get(2).get(fitnesses.get(2).size()-1)-fitnesses.get(2).get(0));
                if(runningFit >= genomeFit){
                    winners.add(genome);
                    continue;
                }
            }
        }
        return winners;
    }

    public void crossover(Genome genome1, Genome genome2, Integer nrOfSections){
        ArrayList<ArrayList<Integer>> crossoverIndexes = treeHandler.getCrossoverIndexes(nrOfSections);
        ArrayList<Integer> rawGenome1 = (ArrayList<Integer>) genome1.genome.clone();
        ArrayList<Integer> rawGenome2 = (ArrayList<Integer>) genome2.genome.clone();
        for(int i = 0; i< nrOfSections; i+=2){
            ArrayList<Integer> indexes = crossoverIndexes.get(i);
            for(Integer index : indexes){
                rawGenome1.set(index, rawGenome2.get(index));
                rawGenome2.set(index, rawGenome1.get(index));
            }
        }
        Genome child1 = new Genome(rawGenome1, mutationRate, imageWidth, imageHeight);
        alignTreeWithGenome(child1);
        mutateGenome(child1);
        nextPopulation.add(child1);
        Genome child2 = new Genome(rawGenome2, mutationRate, imageWidth, imageHeight);
        alignTreeWithGenome(child2);
        mutateGenome(child1);
        nextPopulation.add(child2);
    }

    public void initializePopulation(){
        for(int individual = 0; individual<poolSize; individual++){
            HashMap<ArrayList<Integer>, PixelNode> genomeTree = treeHandler.getInitialTree();
            ArrayList<Integer> rawGenome = treeHandler.extractGenome(genomeTree);
            Genome genome = new Genome(rawGenome, this.mutationRate, imageWidth, imageHeight);
            alignTreeWithGenome(genome, genomeTree);
            currentPopulation.add(genome);
            System.out.println("Initialized genome " + individual);
        }
    }

    public ArrayList<Genome> getCrowdingList(ArrayList<Genome> rank){
        ArrayList<Genome> crowdingList = new ArrayList<>(rank);
        ArrayList<ArrayList<Double>> objectives = new ArrayList<>(Arrays.asList(new ArrayList<Double>(),
                new ArrayList<Double>(),
                new ArrayList<Double>()));
        for (Genome genome : rank){
            objectives.get(0).add(genome.edgeValue);
            objectives.get(1).add(genome.connectivity);
            objectives.get(2).add(genome.overallDeviation);
        }
        ArrayList<ArrayList<Double>> sortedObjectives = new ArrayList<>(objectives);
        Collections.sort(sortedObjectives.get(0));
        Collections.sort(sortedObjectives.get(1));
        Collections.sort(sortedObjectives.get(2));
        ArrayList<Genome> sortedGenomes = new ArrayList<>(rank);
        for (int index = 0; index<3; index++){
            ArrayList<Genome> newSortedGenomes = new ArrayList<>();
            for (Genome genome : sortedGenomes){
                if(genome.edgeValue == objectives.get(0).get(0)){
                    newSortedGenomes.add(genome);
                    objectives.get(0).remove(0);
                }
                sortedGenomes = new ArrayList<>(newSortedGenomes);
            }
        }
        return crowdingList;
    }

    public ArrayList<ArrayList<Genome>> getRanks(){
        for (Genome genome : nextPopulation){
            //System.out.println(genome.edgeValue + " | " + genome.connectivity + " | " + genome.overallDeviation);
            for (Genome domGenome : nextPopulation){
                if(genome != domGenome){
                    if(genome.edgeValue <= domGenome.edgeValue &&
                            genome.connectivity <= domGenome.connectivity &&
                            genome.overallDeviation <= domGenome.overallDeviation){
                        if(genome.edgeValue < domGenome.edgeValue ||
                                genome.connectivity < domGenome.connectivity ||
                                genome.overallDeviation < domGenome.overallDeviation){
                            genome.dominateGenome(domGenome);
                        }
                    }
                }
            }
        }
        ArrayList<ArrayList<Genome>> rankedNodes = new ArrayList<>();
        ArrayList<Genome> rank0 = new ArrayList<>();
        ArrayList<Genome> rank1 = new ArrayList<>();
        for (Genome genome : nextPopulation){
            if (genome.dominatedBy == 0){
                if(genome.connectivity < 100.0){
                    rank1.add(genome);
                }
                rank0.add(genome);
            }
        }
        if (rank0.size() > 0 && rank1.size() > 0){
            for (Genome genome : rank1){
                rank0.get(0).dominateGenome(genome);
                rank0.remove(genome);
            }
        }
        rankedNodes.add(rank0);
        int rank = 0;
        while (rank < rankedNodes.size()){
            ArrayList<Genome> rankN = new ArrayList();
            for (Genome genome : rankedNodes.get(rank)){
                ArrayList<Genome> nextRank = genome.getNextRank(rank);
                if (nextRank.size() > 0) {
                    rankN.addAll(nextRank);
                }
            }
            if (rankN.size() > 0){
                rankedNodes.add(rankN);
            }
            rank ++;
        }
        return rankedNodes;
    }
}
