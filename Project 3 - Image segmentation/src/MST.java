import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MST {

    ArrayList<ArrayList<ArrayList<Integer>>> rgbMatrix;
    FitMinister fitMinister;
    Integer imageHeight, imageWidth;
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

    public MST(String dataFile){
        ImageReader imageReader = new ImageReader(dataFile);
        this.rgbMatrix = imageReader.getRGBMatrix();
        this.fitMinister = new FitMinister(rgbMatrix, neighMap);
        this.imageHeight = rgbMatrix.size();
        this.imageWidth = rgbMatrix.get(0).size();
    }

    public void initializeGenome(){
        ArrayList<ArrayList<Boolean>> visited = new ArrayList<>();
        ArrayList<ArrayList<Integer>> mst = new ArrayList<>();
        ArrayList<ArrayList<Integer>> explorable = new ArrayList<>();
        for (int y = 0; y < this.imageHeight; y++){
            ArrayList<Boolean> visitedRow = new ArrayList<>();
            ArrayList<Integer> mstRow = new ArrayList<>();
            for (int x = 0; x < this.imageWidth; x++){
                visitedRow.add(false);
                mstRow.add(-1);
                explorable.add(new ArrayList<>(Arrays.asList(y, x)));
            }
            visited.add(visitedRow);
            mst.add(mstRow);
        }
        while (explorable.size() > 0){
            boolean hasNeighbour = true;  // Check to se if new random should be picked
            ArrayList<Integer> currentNode = explorable.get(getRandomNumber(0, explorable.size()));  // Pick random pixel
            explorable.remove(currentNode);
            visited.get(currentNode.get(0)).set(currentNode.get(1), true);
            while (hasNeighbour) {  // If previous pixel has unvisited neighbours
                ArrayList<Integer> neighbours = unvisitedNeighbours(currentNode.get(0), currentNode.get(1), visited);
                if (neighbours.size() > 0){  // If current pixel has unvisited neighbours
                    // System.out.println("CN: " + currentNode);
                    // System.out.println("NE: " + neighbours);
                    ArrayList<Double> neighbourDistances = fitMinister.getDistanceToNeighbours(currentNode.get(0), currentNode.get(1), neighbours);
                    // System.out.println(neighbours + " " + neighbourDistances);
                    // System.out.println("dD: " + neighbourDistances);
                    double minDistance = Double.MAX_VALUE;
                    int neighbour = 0;
                    for(int i = 0; i < neighbourDistances.size(); i++){  // find min value
                        if (neighbourDistances.get(i) < minDistance){
                            neighbour = neighbours.get(i);
                            minDistance = neighbourDistances.get(i);
                        }
                    }
                    //System.out.println("Setting: " + currentNode + neighbour);
                    mst.get(currentNode.get(0)).set(currentNode.get(1), neighbour);

                    //System.out.println(neighbour);
                    //System.out.println("Old" + currentNode);
                    currentNode.set(0, currentNode.get(0) + neighMap.get(neighbour).get(0));
                    currentNode.set(1, currentNode.get(1) + neighMap.get(neighbour).get(1));
                    explorable.remove(currentNode);
                    visited.get(currentNode.get(0)).set(currentNode.get(1), true);
                    /*System.out.println("new" + currentNode);
                    for (ArrayList<Integer> row : mst){
                        System.out.println(row);
                    }
                    for (ArrayList<Boolean> row : visited){
                        System.out.println(row);
                    }*/
                } else {
                    visited.get(currentNode.get(0)).set(currentNode.get(1), true);
                    mst.get(currentNode.get(0)).set(currentNode.get(1), 0);
                    //System.out.println("Stopped" + currentNode);
                    explorable.remove(currentNode);
                    hasNeighbour = false;
                }
            }
        }
        for (ArrayList<Integer> row : mst){
            System.out.println(row);
        }
    }
/*
    public void prims(){
        ArrayList<ArrayList<Integer>> mst = new ArrayList<>();
        for (int y = 0; y < this.imageHeight; y++){
            ArrayList<Boolean> visitedRow = new ArrayList<>();
            ArrayList<Integer> mstRow = new ArrayList<>();
            for (int x = 0; x < this.imageWidth; x++){
                mstRow.add(-1);
            }
            mst.add(mstRow);
        }
        ArrayList<ArrayList<Integer>> explorable = new ArrayList<>();
        explorable.add(rgbMatrix.get(getRandomNumber(0, imageHeight)).get(getRandomNumber(0, imageWidth)));  // Pick random pixel

        while (explorable.size() > 0){
            ArrayList<Integer> currentNode = explorable.remove(0);
            explorable.remove(currentNode);
            visited.get(currentNode.get(0)).set(currentNode.get(1), true);
            ArrayList<Integer> neighbours = unvisitedNeighbours(currentNode.get(0), currentNode.get(1), visited);
            if (neighbours.size() > 0){  // If current pixel has unvisited neighbours
                // System.out.println("CN: " + currentNode);
                // System.out.println("NE: " + neighbours);
                ArrayList<Double> neighbourDistances = fitMinister.getDistanceToNeighbours(currentNode.get(0), currentNode.get(1), neighbours);
                // System.out.println(neighbours + " " + neighbourDistances);
                // System.out.println("dD: " + neighbourDistances);
                double minDistance = Double.MAX_VALUE;
                int neighbour = 0;
                for(int i = 0; i < neighbourDistances.size(); i++){  // find min value
                    if (neighbourDistances.get(i) < minDistance){
                        neighbour = neighbours.get(i);
                        minDistance = neighbourDistances.get(i);
                    }
                }
                //System.out.println("Setting: " + currentNode + neighbour);
                mst.get(currentNode.get(0)).set(currentNode.get(1), neighbour);

                //System.out.println(neighbour);
                //System.out.println("Old" + currentNode);
                currentNode.set(0, currentNode.get(0) + neighMap.get(neighbour).get(0));
                currentNode.set(1, currentNode.get(1) + neighMap.get(neighbour).get(1));
                explorable.remove(currentNode);
                visited.get(currentNode.get(0)).set(currentNode.get(1), true);
            }
        }
        for (ArrayList<Integer> row : mst){
            System.out.println(row);
        }
    }*/

    public ArrayList<Integer> unvisitedNeighbours(int row, int place, ArrayList<ArrayList<Boolean>> visited){
        ArrayList<Integer> unvisiteds = new ArrayList<>();
        for (int i = 1; i <= 4; i++){
            ArrayList<Integer> neighbourAddition = neighMap.get(i);
            int neighRow = row + neighbourAddition.get(0);
            int neighPlace = place + neighbourAddition.get(1);
            if(neighRow < 0 || neighPlace < 0 || neighRow >= this.imageHeight || neighPlace >= this.imageWidth){
                continue;
            }
            else if(visited.get(neighRow).get(neighPlace)){
                continue;
            }
            else{
                unvisiteds.add(i);
            }
        }
        return unvisiteds;
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
