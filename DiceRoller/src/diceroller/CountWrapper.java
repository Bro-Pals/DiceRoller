/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

/**
 *
 * @author Owner
 */
public class CountWrapper {
    
    private int value;
    
    public CountWrapper(int initialVal) {
        value = initialVal;
    }
    
    public void setValue(int val) {
        value = val;
    }
    
    public void increase() {
        value++;
    }
    
    public void decrease() {
        value--;
    }
    
    public int getValue() {
        return value;
    }
    
}
