/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.awt.Canvas;
import java.awt.Color;
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
    
    public DiceHandCanvas() {
        setBackground(Color.WHITE);
        side = BOTTOM;
    }
    
    public void paintHand(DiceHand hand) {
        displayHand = hand;
        repaint();
    }
    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform oldTransform = g2.getTransform();
        g2.scale((double)getWidth() / 400, (double)getHeight() / 270);
        
        
        
        g2.setTransform(oldTransform);
    }
    
}
