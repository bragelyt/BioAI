import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;

import static java.lang.Math.*;

public class Plott extends Component {

    ArrayList<Double> xCoords;
    ArrayList<Double> yCoords;

    public Plott(ArrayList<Double> xCoords, ArrayList<Double> yCoords){
        this.xCoords = xCoords;
        this.yCoords =yCoords;
    }

    public void paint(Graphics g)
    {
        g.drawLine(20, 200,640+20,200); // x-axis
        g.drawLine(20,150+200,20,-150+200); // y-axis

        g.setColor(Color.red);
        int X = 20;
        int Y = 200;
        for(double x=0; x<=128; x=x+0.5)
        {
            double y = -sin(x);
            g.drawLine(X, Y, (int)(x*5 + 20), (int)(y*100 + 200));
            X = (int)(x*5 + 20);
            Y = (int)(y*100 + 200);
        }

        g.setColor(Color.black);
        for (int i = 0; i<xCoords.size(); i++){
            g.drawRect((int)(xCoords.get(i)*5 + 20-3), (int)(-yCoords.get(i)*100 + 200-3), 6, 6);
        }
    }
}