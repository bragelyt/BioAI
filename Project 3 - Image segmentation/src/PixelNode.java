import java.util.ArrayList;

public class PixelNode {

    int row, place, neighbourDirection;
    PixelNode parent, segmentRoot;
    double distanceFromStart = 0.0;
    boolean traversed = false;
    ArrayList<PixelNode> children;

    public PixelNode(int row, int place, PixelNode parent, double edgeDistance, int neighbourDirection, boolean reverseDirection){
        this.row = row;  // pixel placement
        this.place = place;  // evt col
        this.parent = parent;
        this.children = new ArrayList<>();
        this.segmentRoot = null;  // The root that this tree points to. Is the key of the segment
        if(parent != null) {
            this.distanceFromStart = parent.distanceFromStart + edgeDistance;
            if(reverseDirection) {
                this.neighbourDirection = (int) (neighbourDirection + Math.pow(-1, neighbourDirection % 2 + 1));
            }
            else{
                this.neighbourDirection = neighbourDirection;
            }
        }
        else{
            traversed = true;
            this.neighbourDirection = neighbourDirection;
            this.segmentRoot = this;
        }
    }

    public void updateShortestDistance(PixelNode parent, double edgeDistance, int neighbourDirection){
        if (distanceFromStart > edgeDistance){  // parent.distanceFromStart + edgeDistance check if this is better?
            this.parent = parent;
            this.distanceFromStart = edgeDistance; // parent.distanceFromStart + edgeDistance;
            if(neighbourDirection != 0){
                this.neighbourDirection = (int) (neighbourDirection + Math.pow(-1, neighbourDirection%2 + 1));
            }
            else{
                this.neighbourDirection = 0;
            }
        }
    }

    public void updateAllSegmentRoot(boolean changeRoot){  // Sets the segment of all children to the root of this or parent.
        ArrayList<PixelNode> orphans = new ArrayList<>();
        PixelNode root = this.segmentRoot;
        if(changeRoot) {
            if (this.parent == null) {
                root = this;
            } else {
                root = this.parent.segmentRoot;
            }
        }
        for (PixelNode child : children){
            orphans.add(child);
        }
        while (orphans.size() > 0){
            PixelNode currentOrphan = orphans.remove(0);
            currentOrphan.updateSegmentRoot(root);
            for (PixelNode child : currentOrphan.children){
                if(child.segmentRoot != root) {
                    orphans.add(child);
                }
            }
            orphans.remove(currentOrphan);
        }
    }

    public PixelNode getAllFather(){
        PixelNode padre = this;
        ArrayList<PixelNode> family = new ArrayList<>();
        family.add(padre);
        while(padre.parent != null && !family.contains(padre.parent)){
            padre = padre.parent;
            family.add(padre);
        }
        return padre;
    }

    public void updateSegmentRoot(PixelNode root){
        this.segmentRoot = root;
    }

    public void traversePixel(){
        this.traversed = true;
    }

    public void addChild(PixelNode child){
        if (!children.contains(child)){
            children.add(child);
        }
    }

    public void removeChild(PixelNode child){
        children.remove(child);
    }

    public void setParent(PixelNode parent){
        this.parent = parent;
    }

    public void setDistance(Double distance){
        this.distanceFromStart = distance;
    }

    public void setSegmentRoot(PixelNode root){
        this.segmentRoot = root;
    }

    public void setNeighbourDirection(Integer neighbourDirection){
        this.neighbourDirection = neighbourDirection;
    }
}

/*
public void rerouteNode(PixelNode newParent, double edgeDistance, int neighbourDirection){  // for mutation. crossover will be a bitch + 1/2
    this.distanceFromStart = edgeDistance;
    this.removeChild(this);
    if(neighbourDirection != 0) {
        newParent.addChild(this);
        this.neighbourDirection = (int) (neighbourDirection + Math.pow(-1, neighbourDirection % 2 + 1));
        this.parent = newParent;
        this.segmentRoot = parent.segmentRoot;
    }
    else{
        this.neighbourDirection = 0;
        this.segmentRoot = this;
        this.parent = null;
    }
    this.updateAllSegmentRoot();
}
*/