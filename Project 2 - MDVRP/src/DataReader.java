import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class DataReader {

    public ArrayList<ArrayList<String>> getFileList(String fileName){
        try {
            fileName = "src/outputFiles/" + fileName + ".txt";
            Scanner scanner = new Scanner(new File(fileName));
            ArrayList<ArrayList<String>> NodeList = new ArrayList<>();
            while (scanner.hasNext()) {
                ArrayList<String> rowList = new ArrayList<>();
                String[] row = scanner.nextLine().split(" ");
                for (String item : row) {
                    if (item != "") {
                        try {
                            rowList.add(item);
                        }
                        catch (Exception e){
                            rowList.add("0");
                        }
                    }
                }
                NodeList.add(rowList);
            }
            return (NodeList);
        }
        catch (FileNotFoundException e){
            System.out.println("File of name " + fileName + " not found");
            return null;
        }
    }

    public ArrayList<ArrayList<Integer>> GetNodeList(String fileName) {
        try {
            fileName = "src/DataFiles/" + fileName + ".txt";
            Scanner scanner = new Scanner(new File(fileName));
            ArrayList<ArrayList<Integer>> NodeList = new ArrayList<>();
            while (scanner.hasNext()) {
                ArrayList<Integer> rowList = new ArrayList<>();
                String[] row = scanner.nextLine().split(" ");
                for (String item : row) {
                    if (item != "") {
                        try {
                            rowList.add(Integer.parseInt(item));
                        }
                        catch (Exception e){
                            rowList.add(0);
                        }
                    }
                }
                NodeList.add(rowList);
            }
            return (NodeList);
        }
        catch (FileNotFoundException e){
            System.out.println("File of name " + fileName + " not found");
            return null;
        }
    }
}
