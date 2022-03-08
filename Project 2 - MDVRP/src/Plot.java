import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.*;

public class Plot extends Component {

    ArrayList<Node> customers;
    ArrayList<Node> depots;
    ArrayList<ArrayList<Node>> nodes;
    ArrayList<ArrayList<Integer>> routes;

    public Plot(ArrayList<Node> customers, ArrayList<Node> depots, ArrayList<ArrayList<Integer>> routes){
        this.customers = customers;
        this.depots = depots;
        this.routes = routes;
        this.nodes = new ArrayList<>();
        this.nodes.add(customers);
        this.nodes.add(depots);
    }

    public void paint(Graphics g)
    {
        int scalar = 1;
        int spacer = 5;
        int minX = (int) Double.POSITIVE_INFINITY;
        int minY = (int) Double.POSITIVE_INFINITY;
        int maxX = (int) Double.NEGATIVE_INFINITY;
        int maxY = (int) Double.NEGATIVE_INFINITY;

        for (Node customer : this.customers){
            int x = customer.xCoord;
            int y = customer.yCoord;
            if (x < minX){
                minX = x;
            }
            if (x > maxX){
                maxX = x;
            }
            if (y < minY){
                minY = y;
            }
            if (y > maxY){
                maxY = y;
            }
        }
        for (Node depot : this.depots){
            int x = depot.xCoord;
            int y = depot.yCoord;
            if (x < minX){
                minX = x;
            }
            if (x > maxX){
                maxX = x;
            }
            if (y < minY){
                minY = y;
            }
            if (y > maxY){
                maxY = y;
            }
        }
        spacer = max(-minX + 10, -minY + 10);
        scalar = min((800)/(maxX-minX), (800)/(maxY-minY));

        HashMap<Integer, ArrayList<Integer>> nodeNr= new HashMap<>();
        g.setColor(Color.black);
        ArrayList<Integer> coords;
        for (ArrayList<Node> nodeList : this.nodes){
            for (Node node : nodeList) {  // Plott all customers and save nr and pos pairs.
                if (node.nodeType == 1){
                    g.setColor(Color.red);
                }
                coords = new ArrayList<>();
                coords.add((node.xCoord + spacer) * scalar);
                coords.add((node.yCoord + spacer) * scalar);
                g.drawOval(coords.get(0), coords.get(1), 6, 6);
                nodeNr.put(node.number, coords);
                g.fillOval(coords.get(0), coords.get(1), 6, 6);
            }
        }
        g.setColor(Color.blue);
        for (ArrayList<Integer> route : this.routes) {
            float red = ThreadLocalRandom.current().nextFloat();
            float green = ThreadLocalRandom.current().nextFloat();
            float blue = ThreadLocalRandom.current().nextFloat();
            Color randomColor = new Color(red, green, blue);
            g.setColor(randomColor);  // Sets each rout to random color.
            if (route.size() != 0) {
                Integer lastNode = route.get(0);
                for (int i = 1; i < route.size(); i++) {
                    g.drawLine(nodeNr.get(lastNode).get(0) + 3, nodeNr.get(lastNode).get(1) + 3, nodeNr.get(route.get(i)).get(0) + 3, nodeNr.get(route.get(i)).get(1) + 3);
                    lastNode = route.get(i);
                }
            }
        }
    }
}