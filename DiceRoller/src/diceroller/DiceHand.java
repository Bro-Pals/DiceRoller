/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.util.ArrayList;


/**
 * A package of information about a dice roll.
 * @author Owner
 */
public class DiceHand {
    
    private ArrayList<Die> dice;
    private int modifier;
    private String rollee;
    private int result;
    private String rawInput;
    
    public DiceHand(String rawInput, String rollee, int modifier, ArrayList<Die> dice) {
        this.dice = dice;
        this.modifier = modifier;
        this.rollee = rollee;
        this.rawInput = rawInput;
        
        this.result = modifier;
        
        for (int i=0; i<dice.size(); i++) {
            if (dice.get(i).getSign()) { // positive or negative?
                this.result += dice.get(i).getResult();
            } else {
                this.result -= dice.get(i).getResult();
            }
        }
    }

    public ArrayList<Die> getDice() {
        return dice;
    }

    public int getModifier() {
        return modifier;
    }

    public String getRollee() {
        return rollee;
    }

    public int getResult() {
        return result;
    }
}
