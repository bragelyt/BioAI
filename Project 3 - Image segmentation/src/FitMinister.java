import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

public class FitMinister {

    ArrayList<ArrayList<ArrayList<Integer>>> rgbMatrix;
    Map<Integer, ArrayList<Integer>> neighMap;
    Integer imageHeight, imageWidth;

    public FitMinister(ArrayList<ArrayList<ArrayList<Integer>>> rgbMatrix, Map<Integer, ArrayList<Integer>> neighMap){
        this.rgbMatrix = rgbMatrix;
        this.neighMap = neighMap;
        this.imageHeight = rgbMatrix.size();
        this.imageWidth = rgbMatrix.get(0).size();
    }

    public double distanceToNeighbour(int row, int place, int neighbourNr){
        if(neighbourNr == 0){
            return 0.0;
        }
        ArrayList<Integer> RGBValues = rgbMatrix.get(row).get(place);
        Integer neighRow = row + neighMap.get(neighbourNr).get(0);
        Integer neighPlace = place + neighMap.get(neighbourNr).get(1);
        if(neighRow >= 0 && neighRow < imageHeight && neighPlace >= 0 && neighPlace < imageWidth) {
            ArrayList<Integer> neighRGBValues = rgbMatrix.get(neighRow).get(neighPlace);
            return (Math.sqrt(
                    Math.pow(RGBValues.get(0) - neighRGBValues.get(0), 2)
                            + Math.pow(RGBValues.get(1) - neighRGBValues.get(1), 2)
                            + Math.pow(RGBValues.get(2) - neighRGBValues.get(2), 2)
            ));
        }
        else return 0;
    }

    public ArrayList<Double> getDistanceToNeighbours(int row, int place, ArrayList<Integer> neighbourList){
        ArrayList<Double> distances = new ArrayList<>();
        for (Integer neighbour : neighbourList){
            distances.add(this.distanceToNeighbour(row, place, neighbour));
        }
        return distances;
    }

    public Double getDistanceToColor(int row, int place, ArrayList<Double> color){
        ArrayList<Integer> RGBValues = rgbMatrix.get(row).get(place);
        return(Math.sqrt(
                Math.pow(RGBValues.get(0)-color.get(0), 2)
                        + Math.pow(RGBValues.get(1)-color.get(1), 2)
                        + Math.pow(RGBValues.get(2)-color.get(2), 2)
        ));
    }


    public ArrayList<Double> getEdgeValueAndConnectivity(HashMap<PixelNode, ArrayList<PixelNode>> borderPixels, HashMap<ArrayList<Integer>, PixelNode> pixelNodes){
        double edgeValue = 0.0;
        double connectivity = 0.0;
        for (PixelNode root : borderPixels.keySet()) {
            for (PixelNode node : borderPixels.get(root)) {
                for (int i = 1; i <= 8; i++) {
                    ArrayList<Integer> neighCoords = new ArrayList<>(Arrays.asList(
                            node.row + neighMap.get(i).get(0), node.place + neighMap.get(i).get(1)));
                    if (pixelNodes.containsKey(neighCoords)) {
                        if (pixelNodes.get(neighCoords).segmentRoot != root) {
                            edgeValue -= distanceToNeighbour(node.row, node.place, i);
                            connectivity += 0.125;
                        }
                    }
                }
            }
        }
        return new ArrayList<>(Arrays.asList(edgeValue, connectivity));
    }

    public Double getOverallDeviation(HashMap<ArrayList<Integer>, PixelNode> pixelNodes){
        double overallDeviation = 0.0;
        // Get average segment value:
        HashMap<PixelNode, ArrayList<Double>> segmentValues = new HashMap<>();
        for(ArrayList<Integer> coords : pixelNodes.keySet()){
            PixelNode node = pixelNodes.get(coords);
            PixelNode root = node.segmentRoot;
            if (!segmentValues.containsKey(root)) {
                segmentValues.put(root, new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0)));
            }
            segmentValues.get(root).set(0, segmentValues.get(root).get(0) + (double)(this.rgbMatrix.get(coords.get(0)).get(coords.get(1)).get(0)));
            segmentValues.get(root).set(1, segmentValues.get(root).get(1) + (double)(this.rgbMatrix.get(coords.get(0)).get(coords.get(1)).get(1)));
            segmentValues.get(root).set(2, segmentValues.get(root).get(2) + (double)(this.rgbMatrix.get(coords.get(0)).get(coords.get(1)).get(2)));
            segmentValues.get(root).set(3, segmentValues.get(root).get(3) + 1.0);
        }
        HashMap<PixelNode, ArrayList<Double>> segmentAverage = new HashMap<>();
        for (PixelNode root : segmentValues.keySet()){
            segmentAverage.put(root, new ArrayList<>(Arrays.asList(
                    segmentValues.get(root).get(0) / segmentValues.get(root).get(3),
                    segmentValues.get(root).get(1) / segmentValues.get(root).get(3),
                    segmentValues.get(root).get(2) / segmentValues.get(root).get(3)
            )));
        }
        for (ArrayList<Integer> coords : pixelNodes.keySet()){
            PixelNode node = pixelNodes.get(coords);
            PixelNode root = node.segmentRoot;
            ArrayList<Double> color = new ArrayList<>(Arrays.asList(
                    segmentAverage.get(root).get(0),
                    segmentAverage.get(root).get(1),
                    segmentAverage.get(root).get(2)));
            overallDeviation += this.getDistanceToColor(node.row, node.place, color);
        }
        return overallDeviation; // /(imageHeight*imageWidth);
    }
}
