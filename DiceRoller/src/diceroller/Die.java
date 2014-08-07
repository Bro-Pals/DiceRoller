/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.io.Serializable;

/**
 * A single dice object for the DiceRoller program.
 * @author Owner
 */
public class Die implements Serializable {
    
    private int size, result;
    
    public Die(int size) {
        this.size = size;
        this.result = (int)(Math.random() * size) + 1;
    }
    
    public int getSize() {
        return size;
    }
    
    public int getResult() {
        return result;
    }
}
