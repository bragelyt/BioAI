import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Genome {
    ArrayList<ArrayList<Integer>> routes;
    Fitness fitFunc;

    public Genome(ArrayList<ArrayList<Integer>> routes, Fitness fitFunc){
        this.routes = new ArrayList<>();
        for(ArrayList<Integer> route : routes){
            this.routes.add((ArrayList<Integer>) route.clone());
        }
        while (this.routes.size() < fitFunc.nrOfVehicles){
            this.routes.add(new ArrayList<>());
        }
        this.fitFunc = fitFunc;
    }

    public void mutate(Double mutationRate){  // Single customer re-route;
        if(mutationRate < ThreadLocalRandom.current().nextDouble()) {
            int customerLocation = ThreadLocalRandom.current().nextInt(fitFunc.depot.customerList.size());  // If going to do inter-mutate, remove old ones
            int customer = removeNthCustomer(customerLocation);
            ArrayList<Integer> custPlacement = fitFunc.getBestCustPlacement((ArrayList<ArrayList<Integer>>) this.routes.clone(), customer);
            this.routes.get(custPlacement.get(0)).add(custPlacement.get(1), customer);
        }
    }

    public void updateFitFunc(Fitness fitFunc){
        this.fitFunc = fitFunc;
    }

    public void setRoutes(ArrayList<ArrayList<Integer>> routes){
        this.routes = (ArrayList<ArrayList<Integer>>) routes.clone();
    }

    public void removeRoute(ArrayList<Integer> customers){
        ArrayList<Integer> toBeRemoved = new ArrayList<>();
        for (int i = 0; i<routes.size(); i++){
            for (int j = 0; j<routes.get(i).size(); j++){
                if (customers.contains(routes.get(i).get(j))){
                    routes.get(i).remove(routes.get(i).get(j));
                    j--;
                }
            }
        }
    }

    public ArrayList<Double> getRouteDurations(){
        ArrayList<Double> durations = new ArrayList<>();
        for (ArrayList<Integer> route: routes){
            durations.add(fitFunc.getDurationOfRoute(route));
        }
        return durations;
    }

    public int removeNthCustomer(int n){
        for (int i = 0; i<routes.size(); i++){
            for (int j = 0; j<routes.get(i).size(); j++){
                if(n >= routes.get(i).size()){
                    n -= routes.get(i).size();
                }
                else{
                    return routes.get(i).remove(j);
                }
            }
        }
        System.out.println(":(");
        return 0;
    }

    public boolean addCustomers(ArrayList<Integer> customers){
        for (int customer : customers){
            if (!addCustomer(customer)){
                return false;
            }
        }
        return true;
    }

    public boolean addCustomerToSpot(Integer customer, ArrayList<ArrayList<Integer>> legalSpot){
        return true;
    }

    public boolean addCustomer(Integer customer){
        ArrayList<ArrayList<Integer>> legalMoves = fitFunc.getLegalCustPlacement((ArrayList<ArrayList<Integer>>) this.routes.clone(), customer);
        int nrOfLegalMoves = 0;
        for (ArrayList<Integer> legalRoute : legalMoves){
            nrOfLegalMoves += legalRoute.size();
        }
        if (nrOfLegalMoves == 0){
            return false;
        }
        int insertionPlace = ThreadLocalRandom.current().nextInt(nrOfLegalMoves);
        for (int i = 0; i<legalMoves.size(); i++){
            if (insertionPlace >= legalMoves.get(i).size()){
                insertionPlace -= legalMoves.get(i).size();
            }
            else{
                if(legalMoves.get(i).get(insertionPlace) == routes.get(i).size()){
                    routes.get(i).add(customer);
                }
                else {
                    routes.get(i).add(legalMoves.get(i).get(insertionPlace), customer);
                }
                return true;
            }
        }
        System.out.println("Hø?");
        return true;
    }

    public ArrayList<Integer> getRandomRoute(){
        return (ArrayList<Integer>) routes.get(ThreadLocalRandom.current().nextInt(routes.size())).clone();
    }
}
