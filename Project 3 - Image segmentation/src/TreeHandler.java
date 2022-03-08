import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TreeHandler{  // This function builds MST of pixels, generates genome from trees and trees from genome.

    ArrayList<ArrayList<ArrayList<Integer>>> rgbMatrix;
    Integer imageHeight, imageWidth;
    FitMinister fitMinister;
    Map<Integer, ArrayList<Integer>> neighMap;

    public TreeHandler(ArrayList<ArrayList<ArrayList<Integer>>> rgbMatrix, FitMinister fitMinister, Map<Integer, ArrayList<Integer>> neighMap){
        this.rgbMatrix = rgbMatrix;
        this.imageHeight = rgbMatrix.size();
        this.imageWidth = rgbMatrix.get(0).size();
        this.fitMinister = fitMinister;
        this.neighMap = neighMap;
    }

    public HashMap<ArrayList<Integer>, PixelNode> getInitialTree(){
        int nrOfTrees = (int)(Math.random()*4 + 4);
        return divideIntoTrees(nrOfTrees);
    }

    public ArrayList<Integer> extractGenome(HashMap<ArrayList<Integer>, PixelNode>  pixelNodes){
        ArrayList<Integer> genome = new ArrayList<>();
        for (int row = 0; row<imageHeight; row++){
            for (int place = 0; place<imageWidth; place++){
                genome.add(pixelNodes.get(new ArrayList<>(Arrays.asList(row, place))).neighbourDirection);
            }
        }
        return genome;
    }
    public HashMap<ArrayList<Integer>, PixelNode> getGenomeTree (ArrayList<Integer> genome){
        HashMap<ArrayList<Integer>, PixelNode> pixelNodes = new HashMap<>();
        ArrayList<PixelNode> roots = new ArrayList<>();
        for (int row = 0; row<imageHeight; row++) {
            for (int place = 0; place < imageWidth; place++) {
                int index = row*imageWidth + place;
                ArrayList<Integer> coords = new ArrayList<>(Arrays.asList(row, place));
                int neighbourDirection = genome.get(index);
                PixelNode neighbourNode = null;
                if (neighbourDirection != 0){
                    ArrayList<Integer> neighbourAddition = neighMap.get(neighbourDirection);
                    int neighRow = row + neighbourAddition.get(0);
                    int neighPlace = place + neighbourAddition.get(1);
                    ArrayList<Integer> neighbourCoords = new ArrayList<>(Arrays.asList(neighRow, neighPlace));
                    if(!pixelNodes.containsKey(neighbourCoords)){
                        neighbourNode = new PixelNode(neighRow, neighPlace, null, 0.0, 0, false);
                        pixelNodes.put(neighbourCoords, neighbourNode);
                    }
                    else{
                        neighbourNode = pixelNodes.get(neighbourCoords);
                    }
                }
                PixelNode currentNode;
                if(!pixelNodes.containsKey(coords)){
                    currentNode = new PixelNode(row, place, neighbourNode, fitMinister.distanceToNeighbour(row, place, neighbourDirection), neighbourDirection, false);
                    pixelNodes.put(coords, currentNode);
                }
                else{
                    currentNode = pixelNodes.get(coords);
                    currentNode.setParent(neighbourNode);
                    currentNode.setNeighbourDirection(neighbourDirection);
                    currentNode.setDistance(fitMinister.distanceToNeighbour(row, place, neighbourDirection));
                }
                if(neighbourNode != null){
                    neighbourNode.addChild(currentNode);
                }
                if(neighbourDirection == 0){
                    roots.add(currentNode);
                }
            }
        }
        for(PixelNode root : roots){
            root.setSegmentRoot(root);
            root.updateAllSegmentRoot(false);
        }
        for(int row = 0; row<imageHeight; row++) {
            for (int place = 0; place < imageWidth; place++) {
                ArrayList<Integer> coords = new ArrayList<>(Arrays.asList(row, place));
                if(pixelNodes.get(coords).segmentRoot == null){
                    PixelNode root = pixelNodes.get(coords).getAllFather();
                    root.setSegmentRoot(root);
                    root.updateAllSegmentRoot(false);
                }
            }
        }
        return pixelNodes;
    }

    public HashMap<PixelNode, ArrayList<PixelNode>> getBorderPixels(HashMap<ArrayList<Integer>, PixelNode> pixelNodes){
        HashMap<PixelNode, ArrayList<PixelNode>> borderPixels = new HashMap<>();
        for (int row = 0; row<imageHeight; row++){
            for (int place = 0; place<imageWidth; place++){
                ArrayList<Integer> coords = new ArrayList<>(Arrays.asList(row, place));
                PixelNode currentPixel = pixelNodes.get(coords);
                if(isBorderPixel(coords, pixelNodes)){
                    if(!borderPixels.keySet().contains(currentPixel.segmentRoot)){
                        borderPixels.put(currentPixel.segmentRoot, new ArrayList<>());
                    }
                    borderPixels.get(currentPixel.segmentRoot).add(currentPixel);
                }
            }
        }
        return borderPixels;
    }

    public HashMap<ArrayList<Integer>, PixelNode> divideIntoTrees(Integer nrOfTrees){
        HashMap<ArrayList<Integer>, PixelNode> pixelNodes = new HashMap<>();
        ArrayList<PixelNode> explorables = new ArrayList<>();
        ArrayList<PixelNode> startingNodes = new ArrayList<>();
        outerLoop:
        for (int i = 0; i<nrOfTrees; i++){
            ArrayList<Integer> startingCoords = new ArrayList<>(Arrays.asList(getRandomNumber(imageHeight), getRandomNumber(imageWidth)));
            if(pixelNodes.containsKey(startingCoords)){
                i-=1;
                continue outerLoop;
            }
            for(PixelNode explorable : explorables){
                if(startingCoords.get(0) == explorable.row && startingCoords.get(1) == explorable.place){
                    i-=1;
                    continue outerLoop;
                }
            }
            PixelNode currentNode = new PixelNode(startingCoords.get(0), startingCoords.get(1), null, 0, 0, true);
            ArrayList<PixelNode> neighbours = getUpdatedNeighbours(currentNode, pixelNodes);
            explorables.addAll(neighbours);
            startingNodes.add(currentNode);
            pixelNodes.put(startingCoords, currentNode);
        }
        while (explorables.size() > 0){
            PixelNode currentNode = null;
            double minDistance = Double.MAX_VALUE;
            for (PixelNode pixelNode : explorables){
                if(pixelNode.distanceFromStart < minDistance){
                    minDistance = pixelNode.distanceFromStart;
                    currentNode = pixelNode;
                }
            }
            currentNode.traversePixel();
            currentNode.parent.addChild(currentNode);
            explorables.remove(currentNode);
            ArrayList<PixelNode> neighbours = getUpdatedNeighbours(currentNode, pixelNodes);
            for (PixelNode pixelNode : neighbours){
                if(!explorables.contains(pixelNode) && !pixelNode.traversed){
                    explorables.add(pixelNode);
                }
            }
        }
        for (PixelNode root : startingNodes) {
            root.setSegmentRoot(root);
            root.updateAllSegmentRoot(false);
        }
        return pixelNodes;
    }

    public ArrayList<ArrayList<Integer>> getCrossoverIndexes(Integer nrOfSections){
        ArrayList<ArrayList<Integer>> selectionSets = new ArrayList<>();
        ArrayList<PixelNode> roots = new ArrayList<>();
        HashMap<ArrayList<Integer>, PixelNode> pixelNodes = divideIntoTrees(nrOfSections);
        for (ArrayList<Integer> coords : pixelNodes.keySet()){
            PixelNode node = pixelNodes.get(coords);
            if (!roots.contains(node.segmentRoot)){
                roots.add(node.segmentRoot);
                selectionSets.add(new ArrayList<>());
            }
            Integer index = roots.indexOf(node.segmentRoot);
            selectionSets.get(index).add(coords.get(0) * this.imageWidth + coords.get(1));
        }
        return selectionSets;
    }

    public ArrayList<PixelNode> getUpdatedNeighbours(PixelNode pixelNode, HashMap<ArrayList<Integer>, PixelNode> pixelNodes){
        ArrayList<PixelNode> neighbours = new ArrayList<>();
        for (int i = 1; i <= 4; i++){
            ArrayList<Integer> neighbourAddition = neighMap.get(i);
            int neighRow = pixelNode.row + neighbourAddition.get(0);
            int neighPlace = pixelNode.place + neighbourAddition.get(1);
            ArrayList<Integer> neighbourCoords = new ArrayList<>(Arrays.asList(neighRow, neighPlace));
            if(neighRow < 0 || neighPlace < 0 || neighRow >= this.imageHeight || neighPlace >= this.imageWidth){
                continue;
            }
            double edgeDistance = this.fitMinister.distanceToNeighbour(pixelNode.row, pixelNode.place, i);
            if(pixelNodes.containsKey(neighbourCoords)){
                PixelNode neighbour = pixelNodes.get(neighbourCoords);
                if(!neighbour.traversed){
                    neighbour.updateShortestDistance(pixelNode, edgeDistance, i);
                    neighbours.add(pixelNodes.get(neighbourCoords));
                }
            }
            else{
                PixelNode neighbour = new PixelNode(neighRow, neighPlace, pixelNode, edgeDistance, i, true);
                neighbours.add(neighbour);
                ArrayList<Integer> startingCoords = new ArrayList<>(Arrays.asList(neighRow, neighPlace));
                pixelNodes.put(startingCoords, neighbour);
            }
        }
        return neighbours;
    }

    public boolean isBorderPixel(ArrayList<Integer> coords, HashMap<ArrayList<Integer>, PixelNode> pixelNodes){
        PixelNode root = pixelNodes.get(coords).segmentRoot;
        for(int i =1; i<=4; i++){  // 8 nearest neighbors
            ArrayList<Integer> neighCoords = new ArrayList<>(Arrays.asList(
                    coords.get(0) + neighMap.get(i).get(0), coords.get(1) + neighMap.get(i).get(1)));
            if(pixelNodes.containsKey(neighCoords)){
                if(pixelNodes.get(neighCoords).segmentRoot != root){
                    return true;
                }
            }
        }
        return false;
    }

    public int getRandomNumber(int max) {
        return (int) ((Math.random() * (max)));
    }
}