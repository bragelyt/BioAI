import java.io.File;  // Import the File class
import java.io.FileWriter;
import java.io.IOException;  // Import the IOException class to handle errors
import java.util.ArrayList;


/**
 * Cost
 * depot nr, vehicle nr, duration of route, load of route, list of customers (start and end with 0)
 * 1, 1, routeDuration, routeLoad, customers
 * 1, 2, routeDuration, routeLoad, customers
 * 1, 3, routeDuration, routeLoad, customers
 * 2, 1, routeDuration, routeLoad, customers ...
 * **/

public class SolutionWriter {
    String fileName;
    Double totalCost;
    ArrayList<ArrayList<Double>> solutionFile;
    ArrayList<ArrayList<Integer>> routes;
    ArrayList<Integer> depots, routeLoad;
    ArrayList<Double> routeDuration;

    public SolutionWriter(String fileName){
        this.totalCost = 0.0;
        this.fileName = fileName;
        this.solutionFile = new ArrayList<>();
        this.routes = new ArrayList<>();
        this.depots = new ArrayList<>();
        this.routeDuration = new ArrayList<>();
        this.routeLoad = new ArrayList<>();
        createFile();
    }

    public void createFile(){
        try {
            File myObj = new File("src/outputFiles/"+fileName+".txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void writeToFile(){
        try {
            FileWriter myWriter = new FileWriter("src/outputFiles/"+fileName+".txt");
            myWriter.write(totalCost + "\n");
            Integer lastDepot = 0;
            Integer vehicle = 0;
            for(int i = 0; i<this.routes.size(); i++){
                String writeString = "";
                if(routes.get(i).size() > 0){
                    if(!depots.get(i).equals(lastDepot)){
                        lastDepot = depots.get(i);
                        vehicle = 1;
                    }
                    writeString += ((depots.get(i)-depots.get(0) +1 ) + "  " + vehicle + "  ");
                    writeString += (routeDuration.get(i) + "   ");
                    while(writeString.length() < 30){
                        writeString += " ";
                    }
                    writeString += routeLoad.get(i);
                    while(writeString.length() < 35){
                        writeString += " ";
                    }
                    writeString += "  0 ";
                    for(Integer custNr : routes.get(i)){
                        writeString += (custNr + " ");
                    }
                    vehicle ++;
                    writeString += ("0\n");
                    myWriter.write(writeString);
                }
            }
            myWriter.close();
            System.out.println("Solution on file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void addTotalCost(Double totalCost){
        this.totalCost = totalCost;
    }

    public void addRouteLoad (ArrayList<Integer> routeCosts){
        this.routeLoad = routeCosts;
    }

    public void addRouteDuration (ArrayList<Double> routeDuration){
        this.routeDuration = routeDuration;
    }

    public void addRoutes(ArrayList<ArrayList<Integer>> routes){
        this.routes = routes;
    }

    public void addDepots(ArrayList<Integer> depots){
        this.depots = depots;
    }
}
