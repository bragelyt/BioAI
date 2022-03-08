import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultTester {

    String dataFile;
    Integer nrOfVehicles, nrOfCustomers, nrOfDepots, depotPoolSize, maxGenerations;
    ArrayList<Integer> maxDuration, maxLoad;
    ArrayList<Node> customerList, depotList;
    ArrayList<TrainDepot> trainDepots;
    Double mutationRate, benchMark, tournamentFrac;
    DataReader dr;
    SolutionWriter solutionWriter;


    ArrayList<ArrayList<String>> resultList;
    ArrayList<ArrayList<Integer>> nodeList;
    ArrayList<ArrayList<ArrayList<Integer>>>  routeses;
    String fileName;


    public ResultTester(String fileName){
        this.fileName = fileName;
        dr = new DataReader();
        routeses = new ArrayList<>();
        resultList = dr.getFileList(fileName);
        nodeList = dr.GetNodeList(fileName);
        checkIfValidSolution();
    }

    public void getRoutes(){
        routeses = new ArrayList<>();
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
        int lastDepot = 1;
        for(int i = 1; i<resultList.size(); i++){
            if (Integer.parseInt(resultList.get(i).get(0)) != lastDepot){
                lastDepot = Integer.parseInt(resultList.get(i).get(0));
                routeses.add(routes);
                routes = new ArrayList<>();
            }
            ArrayList<Integer> route = new ArrayList<>();
            for (int j = 5; j < resultList.get(i).size(); j++){  // Minus 1 hvis siste er 0
                route.add(Integer.parseInt(resultList.get(i).get(j)));
            }
            routes.add(route);
        }
        routeses.add(routes);
    }

    public boolean checkIfValidSolution(){
        if(initNodes()){
            getRoutes();
            createBestSolutions();
            System.out.println(fileName + " "  + getTotalFit());
        }
        return false;
    }

    public void addCustomersToNode(Node depot, ArrayList<Integer> customers){
        for(Node node : customerList){
            if(customers.contains(node.number)){
                depot.addCustomerToDepot(node);
            }
        }
    }

    public void createBestSolutions(){
        trainDepots = new ArrayList<>();
        for(Node depot : depotList){
            trainDepots.add(new TrainDepot(depot, 10, nrOfVehicles, 0.2));
        }
        for (int i = 0; i<trainDepots.size(); i++){
            trainDepots.get(i).genomes.add(new Genome(routeses.get(i), trainDepots.get(i).fitFunc));
            for(ArrayList<Integer> route : routeses.get(i)){
                addCustomersToNode(trainDepots.get(i).depot, route);
            }
            trainDepots.get(i).updateFitFuncNodeNr();
            trainDepots.get(i).resetFitness();
        }
    }

    public Double getTotalFit(){
        double totalFit = 0.0;
        for(TrainDepot depotTrainer : trainDepots){
            totalFit += depotTrainer.getBestFit();
        }
        return totalFit;
    }

    public boolean initNodes(){
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

}
