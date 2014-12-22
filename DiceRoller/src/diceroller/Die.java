/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;


/**
 * A single dice object for the DiceRoller program.
 * @author Owner
 */
public class Die {
    
    private int size, result;
    private boolean sign;
    
    public Die(int size) {
        this.size = size;
        this.result = (int)(Math.random() * size) + 1;
        this.sign = true; // positive by default
    }
    
     public Die(int size, boolean sign) {
        this(size);
        this.sign = sign;
    }
    
    public Die(int size, int rolledValue, boolean sign) {
        this.size = size;
        this.result = rolledValue;
        this.sign = sign;
    }
    
    /**
     * If the dice counts as a positive or negative value
     * @return Returns true if positive, or returns false if negative
     */
    public boolean getSign() {
        return sign;
    }
    
    public int getSize() {
        return size;
    }
    
    public int getResult() {
        return result;
    }
}
