import java.lang.Math;

public class FitnessFunction {

    double scalingFactor;

    public FitnessFunction(int chromosomeLen){
        scalingFactor = 128/Math.pow(2,chromosomeLen);
    }

    public double getScaledValue(Individual individual){  // Returns value in [0,128]/phenotype
        if (individual.chromosome.toLongArray().length == 0){
            individual.makeNonZero();
            return this.getScaledValue(individual);
        }
        return (individual.chromosome.toLongArray()[0] * scalingFactor);
    }

    public double Fitness(Individual individual){
        if (individual.chromosome.toLongArray().length == 0){
            individual.makeNonZero();
            this.Fitness(individual);
        }
        double x = individual.chromosome.toLongArray()[0] * scalingFactor; //Calculates phenotype
        return (Math.sin(x));   //Returns fit
    }
}
