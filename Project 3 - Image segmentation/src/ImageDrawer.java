import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import static java.awt.Color.black;
import static java.awt.Color.green;

public class ImageDrawer {

    Integer imageWidth, imageHeight;

    public ImageDrawer(int imageWidth, int imageHeight){
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public void createImage(HashMap<PixelNode, ArrayList<PixelNode>> borderPixels, String fileName){
        BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        g2d.setColor(black);
        drawOnBorders(borderPixels, fileName, g2d, bufferedImage, black);
    }

    public void drawOnToImage(HashMap<PixelNode, ArrayList<PixelNode>> borderPixels, String fileName, ArrayList<ArrayList<ArrayList<Integer>>> rgbMatrix){
        BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        for(int row = 0; row<rgbMatrix.size(); row++){
            for(int place = 0; place<rgbMatrix.get(row).size(); place++){
                ArrayList<Integer> color = rgbMatrix.get(row).get(place);
                g2d.setColor(new Color(color.get(0), color.get(1), color.get(2)));
                g2d.fillRect(place, row, imageWidth, imageHeight);
            }
        }
        drawOnBorders(borderPixels, fileName, g2d, bufferedImage, green);
    }

    public void drawOnBorders(HashMap<PixelNode, ArrayList<PixelNode>> borderPixels, String fileName, Graphics2D g2d, BufferedImage bufferedImage, Color borderColor){
        g2d.setColor(borderColor);
        for (PixelNode root : borderPixels.keySet()){
            for (PixelNode border : borderPixels.get(root)){
                g2d.fillRect(border.place, border.row, 1, 1);
            }
        }
        for(int width = 0; width < imageWidth; width ++){
            for(int heigth = 0; heigth < imageWidth; heigth++){
                if(heigth == 0 || heigth == imageHeight-1){
                    g2d.fillRect(width, heigth, 1, 1);
                }
                else if(width == 0 || width == imageWidth-1){
                    g2d.fillRect(width, heigth, 1, 1);
                }
            }
        }
        g2d.dispose();
        try {
            File file = new File("src/generatedImages/" + fileName +".png");
            ImageIO.write(bufferedImage, "png", file);
        }
        catch (IOException e){
            System.out.println(e);
        }
    }
}