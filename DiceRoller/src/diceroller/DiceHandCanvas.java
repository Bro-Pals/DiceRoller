/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Owner
 */
public class DiceHandCanvas extends Canvas {
    
    static final byte TOP = 0, BOTTOM = 1, RIGHT = 2, LEFT = 3, NONE = 4;
    
    private DiceHand displayHand;
    private int side;
    
    private final int requestedDiceSize = 50, maxDiceRows = 3, maxDiceColumns = 3;
    private int roomForMod;
    
    public DiceHandCanvas() {
        setBackground(Color.WHITE);
        side = BOTTOM;
        displayHand = null;
    }
    
    public void paintHand(DiceHand hand) {
        displayHand = hand;
        repaint();
    }
    
    @Override
    public void paint(Graphics g) {
        if (displayHand!=null) {
            Graphics2D g2 = (Graphics2D)g;
            AffineTransform oldTransform = g2.getTransform();
            g2.scale((double)getWidth() / 400, (double)getHeight() / 270);
            if (displayHand.getModifier()==0) {
                roomForMod = 0;
            } else {
                roomForMod = requestedDiceSize;
            }
            int diceRows, diceColumns, diceSize;
            int diceCount = displayHand.getDice().length;
            if (diceCount>maxDiceRows) {
                int cols = diceCount/maxDiceRows;
                if (diceCount/maxDiceRows > maxDiceColumns) {
                    //Make a relatively even number of rows and columns
                    int rowColsSqrt = (int)(Math.sqrt(diceCount));
                    diceColumns = rowColsSqrt;
                    diceRows = rowColsSqrt;
                    //The leftover will be an extra row
                    if (diceCount-rowColsSqrt>0) {
                        diceRows++;
                    }
                }
                diceColumns = cols;
                diceRows = maxDiceRows;
            } else {
                diceRows = maxDiceRows;
                diceColumns = maxDiceColumns;
            }
            if (diceRows>maxDiceRows) {
                diceSize = (requestedDiceSize*maxDiceRows)/diceRows;
            } else {
                diceSize = requestedDiceSize;
            }
            int startX = 200-((diceSize*diceRows)/2);
            int startY = 265-requestedDiceSize;
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, diceSize/2);
            g2.setFont(font);
            FontMetrics metrics = g2.getFontMetrics();
            for (int r=0; r<diceRows; r++) {
                for (int c=0; c<diceColumns; c++) {
                    Die die = null;
                    try {
                        die = displayHand.getDice()[(r*diceColumns)+c];
                    } catch(ArrayIndexOutOfBoundsException arioobe) { }
                    if (die!=null) {
                        paintDice(
                                g2, 
                                metrics,
                                startX + (c*diceSize),
                                startY + (r*diceSize),
                                diceSize, 
                                die
                                );
                    }
                }
            }
            g2.setTransform(oldTransform);
        }
    }
    
    private void paintDice(Graphics2D g, FontMetrics metrics, int x, int y, int diceSize, Die die) {
        g.drawRect(x, y, diceSize, diceSize);
    }
}
