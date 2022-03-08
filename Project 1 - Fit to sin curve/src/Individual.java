import java.util.BitSet;
import java.util.concurrent.ThreadLocalRandom;

public class Individual {

    public BitSet chromosome;
    int chromosomeLen;

    public Individual(int chromosomeLen, BitSet chromosome){
        this.chromosomeLen = chromosomeLen;
        if (chromosome == null){
            this.chromosome = initializeBitset();
        }
        else{
            this.chromosome = chromosome;
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(int i = this.chromosome.length() -1 ; i >= 0;  i--)
        {
            s.append(this.chromosome.get(i) == true ? 1: 0);
        }
        String outString = s.toString();
        while(outString.length() < this.chromosomeLen){
            outString = "0"+outString;
        }
        return(outString);
    }

    public BitSet initializeBitset(){
        BitSet bits = new BitSet(chromosomeLen);
        for (int i = 0; i < chromosomeLen; i++){
            if(ThreadLocalRandom.current().nextBoolean()) {
                bits.set(i);
            }
        }
        return bits;
    }

    public void makeNonZero(){
        if(this.chromosome.length() == 0){
            for(int i = 0; i < this.chromosomeLen; i++)
            this.chromosome.set(i);
        }
    }
}