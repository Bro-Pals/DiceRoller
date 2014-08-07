/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.io.Serializable;

/**
 * A package of information about a dice roll.
 * @author Owner
 */
public class DiceHand implements Serializable {
    
    private Die[] dice;
    private int modifier;
    private String rollee;
    private int result;
    private String rawInput;
    
    public DiceHand(String rawInput, String rollee, int modifier, Die[] dice) {
        this.dice = dice;
        this.modifier = modifier;
        this.rollee = rollee;
        this.rawInput = rawInput;
        
        this.result = modifier;
        
        for (int i=0; i<dice.length; i++) {
            this.result += dice[i].getResult();
        }
    }

    public Die[] getDice() {
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
