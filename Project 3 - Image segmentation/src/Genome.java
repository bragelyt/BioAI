import java.util.ArrayList;
import java.util.HashMap;

public class Genome {
    ArrayList<Integer> genome;
    Double mutationRate, edgeValue, connectivity, overallDeviation;
    HashMap<ArrayList<Integer>, PixelNode> genomeTree;
    ArrayList<Genome> dominatedGenomes;
    Integer rank, dominatedBy, imageWidth, imageHeight;

    public Genome(ArrayList<Integer> genome, double mutationRate, int imageWidth, int imageHeight){
        this.genome = genome;
        this.mutationRate = mutationRate;
        // Node tree for fitness
        this.genomeTree = null;
        // Fitness
        this.edgeValue = null;
        this.connectivity = null;
        this.overallDeviation = null;
        // Ranking variable
        this.dominatedGenomes = new ArrayList<>();
        this.dominatedBy = 0;
        this.rank = 0;
        //
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
    }

    public void mutateGenes(){  // x-Men assemble!  - Magneto man
        double mutationRate = this.mutationRate;
        while(Math.random() < mutationRate){
            int gene = (int)(Math.random()*genome.size());
            int neighDir = (int)(Math.random()*5);
            if(gene % imageWidth == 0 && neighDir == 2){
                genome.set(gene, 0);
            }
            else if(gene % imageWidth == imageWidth-1 && neighDir == 1){
                genome.set(gene, 0);
            }
            else if(gene < imageWidth && neighDir == 3){
                genome.set(gene, 0);
            }
            else if(gene >= imageWidth*imageHeight-imageWidth && neighDir == 4){
                genome.set(gene, 0);
            }
            else{
                genome.set(gene, neighDir);
            }
            mutationRate -= 1;
        }
    }

    public void setFitness(HashMap<ArrayList<Integer>, PixelNode> genomeTree, Double edgeValue, Double connectivity, Double overallDeviation){
        this.genomeTree = genomeTree;
        this.edgeValue = edgeValue;
        this.connectivity = connectivity;
        this.overallDeviation = overallDeviation;
    }

    public void dominateGenome(Genome genome){
        dominatedGenomes.add(genome);
        genome.dominatedBy += 1;
    }

    public ArrayList<Genome> getNextRank(Integer rank){
        if(this.dominatedBy > 0){
            System.out.println("Is dominated by " + this.dominatedBy);
            return null;
        }
        ArrayList<Genome> nextRank = new ArrayList<>();
        this.rank = rank;
        for (Genome genome : dominatedGenomes){
            genome.dominatedBy -= 1;
            if(genome.dominatedBy == 0){
                nextRank.add(genome);
            }
        }
        dominatedGenomes = new ArrayList<>();
        return nextRank;
    }
}
