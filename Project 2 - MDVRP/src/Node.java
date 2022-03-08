import java.util.ArrayList;
import java.util.List;

public class Node {
    public int nodeType;  // 0 = cust, 1 = depot. Unødvendig?
    public int number;
    public int xCoord;
    public int yCoord;
    public int duration;    //Time spent at customer
    public int demand;  //Cost of shipment in capacity? Load.
    public int maxDuration, maxLoad;
    public ArrayList<Node> customerList;

    public Node(int nodeType, List<Integer> nodeList, List<Integer> maxes){
        this.nodeType = nodeType;
        this.number = nodeList.get(0);
        this.xCoord = nodeList.get(1);
        this.yCoord = nodeList.get(2);
        this.duration = nodeList.get(3);
        this.demand = nodeList.get(4);
        if(nodeType == 1){
            this.maxDuration = maxes.get(0);
            this.maxLoad = maxes.get(1);
            customerList = new ArrayList<>();
        }
    }

    public void addCustomerToDepot(Node customer){
        if(nodeType == 1){
            customerList.add(customer);

        }
    }

    public void removeCustomerFromDepot(Node customer){
        if (customerList.contains(customer)){
            customerList.remove(customer);
        }
    }

    public void printCustomerNr(){
        if (nodeType == 1) {
            for (Node customer : customerList) {
                System.out.println(customer.number);
            }
        }
    }
}
