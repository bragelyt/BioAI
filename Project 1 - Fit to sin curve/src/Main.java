public class Main {

    public static void main(String[] args) {
        SGA sga = new SGA(50,4,0.0025,50, false , false, false);
        sga.updateRoulette();
    }
}

/*
 Non nitching, non crowding:
 mr = 0.0025

 Crowding:
 mr = 5

 Higher chromosome length => higher pool size
 */