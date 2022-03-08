import java.lang.Math;
import java.util.*;


public class Train {

    //dataFile, poolsize, mutationRate, numOfGenerations,    #Vars
    //depot trainer, crowdingFunction, visualizer, fitness function, genome, crossoverMethod    #Funcs
    //dict of depots mapping to customers on that depot, data file picker
    String dataFile;
    Integer nrOfVehicles, nrOfCustomers, nrOfDepots, depotPoolSize, maxGenerations;
    ArrayList<Integer> maxDuration, maxLoad;
    ArrayList<Node> customerList, depotList;
    ArrayList<TrainDepot> trainDepots;
    Double mutationRate, benchMark, tournamentFrac;
    DataReader dr;
    SolutionWriter solutionWriter;

    public Train(String dataFile, Integer depotPoolSize, Double mutationRate, Double benchMark, Double tournamentFrac, Integer maxGenerations){
        //what problem are we training at?
        //num of gen or until > 5%?
        //initialize vars
        this.tournamentFrac = tournamentFrac;
        this.dataFile = dataFile;
        this.solutionWriter = new SolutionWriter(dataFile);
        this.depotPoolSize = depotPoolSize;
        this.mutationRate = mutationRate;
        this.benchMark = benchMark;
        this.maxGenerations = maxGenerations;
        dr = new DataReader();
        trainDepots = new ArrayList<>();
    }

    public void run(){
        //cluster the customers based on clustering technique
        //call depot trainers on each depot. Train multiple at once or not?
        //visualize the solution
        if(this.initNodes()) {   // if node file found and read correctly
            groupNodesToClosestDepot();  // Groups all customers to closest depot.
            for (Node depot : depotList) {
                if (depotOverflowing(depot)) {
                    if (!fixDepotWeights(depot)) {
                        System.out.println("Some Depot is overflowing. Unsolvable?");
                    }
                }
                updateAllNodeNr();
                trainDepots.add(new TrainDepot(depot, depotPoolSize, nrOfVehicles, tournamentFrac));
            }
            Graph solution = new Graph(customerList, depotList, getDataSolutions());
            int nrOfGenerations = 0;
            initializeProblem();
            Graph start = new Graph(customerList, depotList, getSolution());
            Double fit = getTotalFit();
            while((fit > benchMark && nrOfGenerations < maxGenerations) || nrOfGenerations < 20){
                for (TrainDepot trainDepot : trainDepots) {
                    trainDepot.runOneGeneration();
                }
                nrOfGenerations += 1;
                if(nrOfGenerations % 50 == 0) {
                    System.out.println("        gen: " + nrOfGenerations);
                    writeSolutionToFile();
                }
                if(fit > getTotalFit()){
                    System.out.println("    Best fit: " + getTotalFit());
                    fit = getTotalFit();
                }
            }
            Graph solved = new Graph(customerList, depotList, getSolution());
            System.out.println("Final travel distance: " + getTotalFit());
            System.out.println("Number of generations: " + nrOfGenerations);
            writeSolutionToFile();
        }
    }

    public void writeSolutionToFile(){
        this.solutionWriter.addTotalCost(getTotalFit());
        ArrayList<Integer> depots = new ArrayList<>();
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
        ArrayList<Double> routeDuration = new ArrayList<>();
        ArrayList<Integer> routeLoad = new ArrayList<>();
        for (TrainDepot trainDepot : trainDepots){
            Genome bestDepotGenome = trainDepot.getBestGenome();
            for (ArrayList<Integer> route : bestDepotGenome.routes){
                depots.add(trainDepot.depot.number);
                routes.add(route);
                routeDuration.add(trainDepot.fitFunc.getDurationOfRoute(route));
                routeLoad.add(trainDepot.fitFunc.getDemandOfRoute(route));
            }
        }
        this.solutionWriter.addRoutes(routes);
        this.solutionWriter.addRouteLoad(routeLoad);
        this.solutionWriter.addRouteDuration(routeDuration);
        this.solutionWriter.addDepots(depots);
        this.solutionWriter.writeToFile();
    };

    public boolean initNodes(){
        ArrayList<ArrayList<Integer>> nodeList = dr.GetNodeList(this.dataFile);
        if (nodeList != null){
            customerList = new ArrayList<>();
            depotList  = new ArrayList<>();
            maxDuration = new ArrayList<>();
            maxLoad = new ArrayList<>();
            List<Integer> nodeData;
            for(int i = 0; i < nodeList.size(); i++){
                if(i == 0){  // game world specs
                    nrOfVehicles = nodeList.get(i).get(0);
                    nrOfCustomers = nodeList.get(i).get(1);
                    nrOfDepots = nodeList.get(i).get(2);
                }
                else if(i <= nrOfDepots){  // depot specs
                    maxDuration.add(nodeList.get(i).get(0));
                    maxLoad.add(nodeList.get(i).get(1));
                }
                else if (i <= nrOfDepots + nrOfCustomers){  // customer
                    if(nodeList.get(i).get(4) == 0){
                        System.out.println("Hva faen?");
                    }
                    nodeData = nodeList.get(i).subList(0, 5);
                    customerList.add(new Node(0, nodeData, null));
                }
                else{
                    if(nodeList.get(i).get(4) != 0){  // depot
                        System.out.println("Hva faen x2?");
                    }
                    nodeData = nodeList.get(i).subList(0, 5);
                    ArrayList<Integer> depotMaxes = new ArrayList<Integer>();
                    depotMaxes.add(maxDuration.get(i - nrOfDepots - nrOfCustomers-1));
                    depotMaxes.add(maxLoad.get(i - nrOfDepots - nrOfCustomers -1));
                    depotList.add(new Node(1, nodeData, depotMaxes));
                }
            }
            return true;
        }
        return false;
    }

    public Double getTotalFit(){
        double totalFit = 0.0;
        for(TrainDepot depotTrainer : trainDepots){
            totalFit += depotTrainer.getBestFit();
        }
        return totalFit;
    }

    public ArrayList<Double> getAllDurations(){
        ArrayList<Double> durations = new ArrayList<>();
        for(TrainDepot depotTrainer : trainDepots){
            Genome depotGenome = depotTrainer.getBestGenome();
            ArrayList<Double> depotDurations = depotGenome.getRouteDurations();
            for(Double routeDuration : depotDurations){
                durations.add(routeDuration);
            }
        }
        return durations;
    }

    public void initializeProblem(){
        for (TrainDepot trainDepot : trainDepots) {
            if (!trainDepot.initializeGenomes()){
                removeWeightFromDepot(trainDepot.depot);
                updateAllNodeNr();
                initializeProblem();
                break;
            }
        }
    }

    public void groupNodesToClosestDepot(){
        for(Node customer : customerList){
            Double minDist = Double.POSITIVE_INFINITY;
            Node closestDepot = null;
            for(Node depot : depotList){
                Double dist = nodeDistances(customer, depot);
                if(dist < minDist){
                    minDist = dist;
                    closestDepot = depot;
                }
            }
            closestDepot.addCustomerToDepot(customer);
        }
    }

    public void intraDepotMutation(){
        Node bestCustomer = null;
        Node fromDepot = null;
        Node toDepot = null;
        Double fitGain = Double.NEGATIVE_INFINITY;
        for (Node fDepot : depotList){
            for (Node customer : customerList){
                for (Node tDepot : depotList){
                    if (fDepot != tDepot){
                        ArrayList<Integer> testRouteFrom = null;
                    }
                }
            }
        }
    }

    public boolean fixDepotWeights(Node depot){
        if(depotOverflowing(depot)){
            if(removeWeightFromDepot(depot)){
                return fixDepotWeights(depot);
            }
        }
        return true;
    }

    public boolean depotOverflowing(Node depot){
        if(depot.maxLoad == 0){
            return false;
        }
        int load = 0;
        for (Node customer : depot.customerList){
            load += customer.demand;
        }
        if (load > depot.maxLoad * nrOfVehicles){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean removeWeightFromDepot(Node depot){
        Node targetDepot = null;
        Node targetCustomer = null;
        Double bestDeltaDistance = Double.POSITIVE_INFINITY;
        Double deltaDistance;
        for (Node customer : depot.customerList){
            for (Node otherDepot : depotList){
                if (otherDepot != depot) {
                    //for(Node otherCustomer : otherDepot.customerList) {
                        deltaDistance = nodeDistances(otherDepot, customer) - nodeDistances(depot, customer);
                        if (deltaDistance < bestDeltaDistance) {
                            otherDepot.addCustomerToDepot(customer);
                            if (!depotOverflowing(otherDepot)) {
                                targetDepot = otherDepot;
                                targetCustomer = customer;
                            }
                            otherDepot.removeCustomerFromDepot(customer);
                        }
                    //}
                }
            }
        }
        if(targetDepot == null || targetCustomer == null){
            System.out.println("Fakk");
            return false;
        }
        depot.removeCustomerFromDepot(targetCustomer);
        targetDepot.addCustomerToDepot(targetCustomer);
        return true;
    }

    public Double nodeDistances(Node node1, Node node2){
        return(Math.sqrt(Math.pow(node1.xCoord-node2.xCoord, 2) + Math.pow(node1.yCoord-node2.yCoord, 2)));
    }

    public ArrayList<ArrayList<Integer>> getSolution(){
        ArrayList<ArrayList<Integer>> solution = new ArrayList<>();
        for (TrainDepot trainDepot : trainDepots){
            for (ArrayList<Integer> route : trainDepot.getBestGenome().routes) {
                ArrayList<Integer> routeCopy = (ArrayList<Integer>) route.clone();
                routeCopy.add(0, trainDepot.depot.number);
                routeCopy.add(trainDepot.depot.number);
                solution.add(routeCopy);
            }
        }
        return solution;
    }

    public ArrayList<ArrayList<Integer>> getDataSolutions(){
        ArrayList<ArrayList<Integer>> routesRaw = dr.GetNodeList(this.dataFile+"res");
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
        for (int i = 1; i< routesRaw.size(); i++){
            ArrayList<Integer> route  = new ArrayList<>();
            route.add(depotList.get(routesRaw.get(i).get(0)-1).number);
            for (int j = 5; j < routesRaw.get(i).size(); j++){
                route.add(routesRaw.get(i).get(j));
            }
            route.add(depotList.get(routesRaw.get(i).get(0)-1).number);
            routes.add(route);
        }
        return routes;
    }

    public void updateAllNodeNr(){
        for (TrainDepot trainDepot : trainDepots){
            trainDepot.updateFitFuncNodeNr();
        }
    }
}
