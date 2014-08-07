/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.io.Serializable;

/**
 * A request object sent to the DiceRollerServer by a DiceRollerClient to process
 * a roll.
 * @author Owner
 */
public class DiceHandRequest implements Serializable {
    
    private String name;
    private String input;

    public DiceHandRequest(String name, String input) {
        this.name=name;
        this.input=input;
    }
    
    public String getName() {
        return name;
    }

    public String getInput() {
        return input;
    }
}
