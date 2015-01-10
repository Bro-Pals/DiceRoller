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
import java.awt.image.BufferedImage;

/**
 * Drawing things on the client program
 * @author Owner
 */
public class DiceHandCanvas extends Canvas {

    private DiceHand displayHand;
    private BufferedImage[] diceImages;
    private final int PADDING = 10;
    
    public DiceHandCanvas() {
        setBackground(Color.WHITE);
        displayHand = null;
        diceImages = null;
    }
    
    public void paintHand(DiceHand hand) {
        displayHand = hand;
        repaint();
    }
    
    @Override
    public void paint(Graphics g) {
        if (displayHand!=null) {
            int width = getWidth();
            int height = getHeight(); // in case it changes while drawing
            Graphics2D g2 = (Graphics2D)g;
            
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height); // background
            // display rollee
            g2.setFont(new Font("Arial Black", Font.PLAIN, 18));
            FontMetrics fontMetric1 = g2.getFontMetrics();
            
            // draw the dice
            if (!displayHand.getDice().isEmpty()) {
                int boxForDiceWidth = width - (3*PADDING) - 120;
                int boxForDiceHeight = height - (2*PADDING) - 110;
                int columnsOfDice = 1, rowsOfDice = 1;
                int maxPerRC = 3;
                if (boxForDiceWidth > boxForDiceHeight) {
                    columnsOfDice = displayHand.getDice().size() < maxPerRC ? 
                            displayHand.getDice().size() : maxPerRC;
                    rowsOfDice = 1 + (displayHand.getDice().size() / maxPerRC);
                } else {
                    rowsOfDice = displayHand.getDice().size() < maxPerRC ? 
                            displayHand.getDice().size() : maxPerRC;
                    columnsOfDice = 1 + (displayHand.getDice().size() / maxPerRC);
                }
                
                int diceWidth = (boxForDiceWidth-(PADDING * (columnsOfDice-1))) / columnsOfDice;
                int diceHeight = (boxForDiceHeight-(PADDING * (rowsOfDice-1))) / rowsOfDice;
                int diceSize = diceWidth;
                // make them square 
                if (diceWidth > diceHeight) {
                    diceSize = diceHeight;
                }
                // only draw if they're big enough
                if (diceSize > 0) {
                    //recalculate columns with new size
                    columnsOfDice = boxForDiceWidth / diceSize;

                    int rowOn = 0;
                    int columnOn = 0;
                    for (int i=0; i<displayHand.getDice().size(); i++) {
                        g2.setColor(Color.BLACK);
                        int xPos = PADDING + columnOn * (diceSize + PADDING);
                        int yPos = 60 + PADDING + rowOn * (diceSize + PADDING);
                        Die die = displayHand.getDice().get(i);
                        
                        BufferedImage diceImage = getDiceImage(die.getSize());
                        if (diceImage == null) {
                             g2.drawRect(xPos, yPos, diceSize, diceSize);
                        } else {
                            g2.drawImage(diceImage, xPos, yPos, diceSize, diceSize, null);
                        }
                        if (die.getResult() == die.getSize()) { // max roll!
                            g2.setColor(Color.BLUE);
                        }else if (die.getResult() == 1) { // critical fail! D:
                            g2.setColor(Color.RED);
                        }
                        String dieString = "" + die.getResult();
                        if (!die.getSign()) { // if it's a subtracting dice
                            dieString = "-" + dieString;
                        }
                        g2.drawString(dieString, xPos + (diceSize/2)-(fontMetric1.stringWidth(dieString)/2),
                                yPos + (diceSize/2)-(fontMetric1.getHeight()/2)+3);

                        columnOn++;
                        if (columnOn >= columnsOfDice) {
                            columnOn = 0;
                            rowOn++;
                        }
                    }
                }
            }
            g2.setFont(new Font("Arial Black", Font.PLAIN, 36));
            FontMetrics fontMetrics2 = g2.getFontMetrics();
            g2.setColor(Color.BLACK);
            // draw the mod
            if (displayHand.getModifier() != 0) {
                String modText = displayHand.getModifier() > 0 ? "+" : "";
                modText = modText + displayHand.getModifier();
                g2.drawString(modText,  width - PADDING - fontMetrics2.stringWidth(modText), 
                        (height / 2) - (fontMetrics2.getHeight()/2));
            }
            
            g2.drawString("Roll from " + displayHand.getRollee(), 5, 40);
            
            String resultString = "= " + displayHand.getResult();
            g2.drawString(resultString, width - PADDING - (fontMetrics2.stringWidth(resultString)), 
                    height - 15);
        }
    }
    
    private void paintDice(Graphics2D g, FontMetrics metrics, int x, int y, int diceSize, Die die) {
        g.drawRect(x, y, diceSize, diceSize);
    }
    
    private BufferedImage getDiceImage(int size) {
        if (diceImages == null)
            return null;
        
        switch(size) {
            case 4: return diceImages[0];
            case 6: return diceImages[1];
            case 8: return diceImages[2];
            case 10: return diceImages[3];
            case 12: return diceImages[4];
            case 20: return diceImages[5];
        }
        return null; // no image for that size
    }
    
    public void giveDiceImage(BufferedImage rawImg) {
        diceImages = new BufferedImage[]{
            rawImg.getSubimage(0, 0, 250, 250), // 0 d4
            rawImg.getSubimage(250, 0, 250, 250), // 1 d6
            rawImg.getSubimage(0, 250, 250, 250), // 2 d8
            rawImg.getSubimage(250, 250, 250, 250), // 3 d10
            rawImg.getSubimage(0, 500, 250, 250), // 4 d12
            rawImg.getSubimage(250, 500, 250, 250) // 5 d20
        };
    }
}
