import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ImageReader {

    String origFolder = "src/EVALUATOR/optimal/original/";
    String imageName;

    public ImageReader(String imageName){
        this.imageName = imageName;
    }

    public ArrayList<ArrayList<ArrayList<Integer>>> getRGBMatrix(){ // boolean padding
        ArrayList<ArrayList<ArrayList<Integer>>> rgbMatrix = new ArrayList<>();
        File file= new File(origFolder + imageName);
        try{
            BufferedImage img = ImageIO.read(file);
            for (int y = 0; y < img.getHeight(); y++) {
                ArrayList<ArrayList<Integer>> pixelRow = new ArrayList<>();
                for (int x = 0; x < img.getWidth(); x++) {
                    //Retrieving contents of a pixel
                    int pixel = img.getRGB(x,y);
                    ArrayList<Integer> pixelValues = new ArrayList<>();
                    //Creating a Color object from pixel value
                    Color color = new Color(pixel, true);
                    //Retrieving the R G B values
                    pixelValues.add(color.getRed());
                    pixelValues.add(color.getGreen());
                    pixelValues.add(color.getBlue());
                    pixelRow.add(pixelValues);
                }
                rgbMatrix.add(pixelRow);
            }
        }
        catch (IOException e){
            System.out.println("File not found");
        }
        return rgbMatrix;
    }
}
