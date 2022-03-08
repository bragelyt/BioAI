import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Fitness {
    Node depot;
    HashMap<Integer, Node> nrNode;
    int nrOfVehicles;

    public Fitness(Node depot, int nrOfVehicles){
        this.nrOfVehicles = nrOfVehicles;
        this.depot = depot;
        updateNodeNr();
    }

    // Might be useful if shuffling what depots a customer node is serviced from
    public void addNodePosition(Node node){
        nrNode.put(node.number, node);  // Genome is mapped to node numbers. This is lookup for use in dist
    }

    public Double getFitOfBestCustPlacement(ArrayList<ArrayList<Integer>> routes, Integer customer){
        ArrayList<Integer> bestPlacement = getBestCustPlacement(routes, customer);
        if(bestPlacement.size() == 0){
            return Double.POSITIVE_INFINITY;
        }
        ArrayList<Integer> route = (ArrayList<Integer>) routes.get(bestPlacement.get(0)).clone();
        route.add(bestPlacement.get(1), customer);
        return(getCostOfRoute(route));
    }

    public Double getFitWithNthRemoved(ArrayList<Integer> route, int n){
        ArrayList<Integer> testRoute = (ArrayList<Integer>) route.clone();
        testRoute.remove(n);
        return(getCostOfRoute(testRoute));
    }

    public ArrayList<Integer> getBestCustPlacement(ArrayList<ArrayList<Integer>> routes, Integer customer){
        double bestFit = Double.POSITIVE_INFINITY;
        ArrayList<Integer> placement = new ArrayList<>();
        double currFit;
        for (int i =0; i < routes.size(); i++){
            for (int j =0; j< routes.get(i).size() +1; j++){
                routes.get(i).add(j, customer);
                if(isLegalGenome(routes)){
                    currFit = fit(routes);
                    if(currFit < bestFit){
                        bestFit = currFit;
                        placement = new ArrayList<>();
                        placement.add(i);
                        placement.add(j);
                    }
                }
                routes.get(i).remove(j);
            }
        }
        return placement;
    }

    public void updateNodeNr(){
        nrNode = new HashMap<>();
        addNodePosition(depot);
        for (Node customer : depot.customerList) {
            addNodePosition(customer);
        }
    }

    public ArrayList<ArrayList<Integer>> getLegalCustPlacement(ArrayList<ArrayList<Integer>> routes, Integer customer){
        ArrayList<ArrayList<Integer>> legalPlacements = new ArrayList<>();
        for (int i =0; i < routes.size(); i++){
            legalPlacements.add(new ArrayList<>());
            for (int j =0; j< routes.get(i).size() +1; j++){
                routes.get(i).add(j, customer);
                if(isLegalGenome(routes)){
                    legalPlacements.get(i).add(j);
                }
                routes.get(i).remove(j);
            }
        }
        return legalPlacements;
    }

    public Double fit(Genome genome){
        int nrOfCust = 0;
        for(ArrayList<Integer> route : genome.routes){
            nrOfCust += route.size();
        }
        if (nrOfCust != depot.customerList.size()){
            System.out.println("Fuck");
            return Double.POSITIVE_INFINITY;
        }
        if (genome.routes == null){
            return Double.POSITIVE_INFINITY;
        }
        if(genome.routes.size() > this.nrOfVehicles){  // Should obey by nr of vehicles. If not is inf long route
            return Double.POSITIVE_INFINITY;
        }
        double totalDistance = 0.0;
        for (ArrayList<Integer> vehicleRoute : genome.routes){
            totalDistance += getCostOfRoute((vehicleRoute));
        }
        return totalDistance;
    }

    public Double getDurationOfRoute(ArrayList<Integer> route){
        Double duration = getCostOfRoute(route);
        for(Integer customer : route){
            duration += nrNode.get(customer).duration;
        }
        return duration;
    }

    public Integer getDemandOfRoute(ArrayList<Integer> route){
        Integer totalDemand = 0;
        for (Integer nodeNr : route){
            totalDemand += this.nrNode.get(nodeNr).demand;
        }
        return(totalDemand);
    }

    public Double getCostOfRoute(ArrayList<Integer> route){
        if(route.size() == 0){
            return 0.0;
        }
        Double totalDistance = 0.0;
        Node prevNode = depot;
        Node currNode;
        double routeDuration = 0;
        double routeDemand = 0;
        for (Integer nodeNr : route){
            currNode = nrNode.get(nodeNr);
            totalDistance += this.nodeDistances(prevNode, currNode);
            routeDuration += currNode.duration;
            routeDemand += currNode.demand;
            prevNode = currNode;
        }
        totalDistance += this.nodeDistances(prevNode, depot);
        if(depot.maxLoad > 0 && routeDemand > depot.maxLoad){
            return Double.POSITIVE_INFINITY;
        }
        else if (depot.maxDuration > 0 && routeDuration + totalDistance > depot.maxDuration){
            return Double.POSITIVE_INFINITY;
        }
        return totalDistance;
    }

    public Double fit(ArrayList<ArrayList<Integer>> routes){
        if (routes == null){
            System.out.println("Routes = 0");
            return Double.POSITIVE_INFINITY;
        }
        if(routes.size() > this.nrOfVehicles){  // Should obey by nr of vehicles. If not is inf long route
            System.out.println("Too many routes");
            return Double.POSITIVE_INFINITY;
        }
        double totalDistance = 0.0;
        for (ArrayList<Integer> vehicleRoute : routes){
            totalDistance += getCostOfRoute(vehicleRoute);
        }
        return totalDistance;
    }

    public Boolean isLegalGenome(ArrayList<ArrayList<Integer>> routes){
        if (routes == null){
            return false;
        }
        if(routes.size() > this.nrOfVehicles){  // Should obey by nr of vehicles. If not is inf long route
            return false;
        }
        for (ArrayList<Integer> vehicleRoute : routes){
            Node prevNode = depot;
            Node currNode;
            double routeDuration = 0;
            double routeDemand = 0;
            for (Integer nodeNr : vehicleRoute){
                currNode = nrNode.get(nodeNr);
                routeDuration += this.nodeDistances(prevNode, currNode) + currNode.duration;
                routeDemand += currNode.demand;
                prevNode = currNode;
            }
            if(depot.maxLoad > 0 && routeDemand > depot.maxLoad){
                return false;
            }
            else if (depot.maxDuration > 0 && routeDuration > depot.maxDuration){
                return false;
            }
        }
        return true;
    }

    // Returns arrays of all routes from depot. Depot implicit, so that is not returned
    public ArrayList<ArrayList<Integer>> getRoutes (ArrayList<Integer> genome){
        ArrayList<ArrayList<Integer>> depotRoute = new ArrayList<>();
        ArrayList<Integer> vehicleRoute = new ArrayList<>();
        double tripDur = 0.0;
        int tripLoad = 0;
        Node currNode;
        for (Integer nodeNr : genome){
            currNode = nrNode.get(nodeNr);
            if(depot.maxLoad > 0 && (tripLoad + currNode.demand > depot.maxLoad)){
                depotRoute.add(vehicleRoute);
                vehicleRoute = new ArrayList<>();
                tripDur = nodeDistances(depot, currNode);
                tripLoad = 0;
                vehicleRoute.add(nodeNr);
            }
            else if(depot.maxDuration > 0 && (tripDur + currNode.duration > depot.maxDuration)){
                depotRoute.add(vehicleRoute);
                vehicleRoute = new ArrayList<>();
                tripDur = nodeDistances(depot, currNode);
                tripLoad = 0;
                vehicleRoute.add(nodeNr);
            }
            else{
                tripDur += currNode.duration + nodeDistances(depot, currNode);
                tripLoad += currNode.demand;
                vehicleRoute.add(nodeNr);
            }
        }
        depotRoute.add(vehicleRoute);
        return depotRoute;
    }

    // Distance to travel between two nodes.
    public Double nodeDistances(Node node1, Node node2){
        if(node1 == null || node2==null){
            return 0.0;
        }
        return(Math.sqrt(Math.pow(node1.xCoord-node2.xCoord, 2) + Math.pow(node1.yCoord-node2.yCoord, 2)));
    }
}
