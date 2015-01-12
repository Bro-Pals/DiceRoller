/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author Owner
 */
public class DiceHandParser {
    
    private final static String VAL_SEP = ":"; // seperates values/signs/identifiers
    private final static String ELEMENT_SEP = "&"; // seperates elements
    
    private final static String DICE_IDENTIFIER = "D";
    private final static String MOD_IDENTIFIER = "M";
    private final static String ROLLEE_IDENTIFIER = "R";

    private final static String SIGN_IDENTIFIER = "!"; // ! means it's positive
    
    /**
     * Each hand will be expected to be broken up into many this format:
     *     IDENTIFIER (SIGN) VALUE(S)
     * If the identifier is a mod, only a value is expected (number). If it's 
     * for a dice, then 2 values and a sign are expected (Dice size and result). 
     * If the identifier is for a Rollee, the only thing expected is a single 
     * value for the name of the roller.
     * 
     * Each value in the returned string is separated by the different identifiers
     * @param hand
     * @return 
     */
    public synchronized static String translateToString(DiceHand hand) {
        String returnMsg = "";
        // add the rollee
        returnMsg += ROLLEE_IDENTIFIER + VAL_SEP + hand.getRollee() + ELEMENT_SEP;
        // place on the modifier if it's not zero
        if (hand.getModifier() != 0) {
            returnMsg += MOD_IDENTIFIER + VAL_SEP + hand.getModifier() + ELEMENT_SEP;
        }
        // place on every dice
        for (Die die : hand.getDice()) {
            String positive = die.getSign() ? "!" : "@";
            returnMsg += DICE_IDENTIFIER + VAL_SEP + positive + VAL_SEP +
                    die.getSize() + VAL_SEP + die.getResult() + ELEMENT_SEP;
        }
        
        return returnMsg;
    }
    
    public synchronized static DiceHand translateToDiceHand(String rawMsg) {
        int totalMod = 0;
        ArrayList<Die> dice = new ArrayList<>();
        String rollee = "NO_ROLLEE_FOUND";
        
        String[] elements = rawMsg.split(ELEMENT_SEP);
        for (int m=0; m<elements.length; m++) {
            // expected format: IDENTIFIER (SIGN) VALUE(S)
            String[] tokens = elements[m].split(VAL_SEP);
            if (tokens.length == 0) {
                continue; // don't worry if it's empty
            }
            // process it based on the starting identifier
            if (tokens[0].equals(DICE_IDENTIFIER)) {
                boolean positive = tokens[1].equals("!");
                if (!positive) System.out.println("NEGATIVE DICE");
                dice.add(new Die(Integer.parseInt(tokens[2]), // size first
                        Integer.parseInt(tokens[3]), // then result
                        positive));
            } else if (tokens[0].equals(MOD_IDENTIFIER)) {
                totalMod += Integer.parseInt(tokens[1]);
            } else if (tokens[0].equals(ROLLEE_IDENTIFIER)) {
                rollee = tokens[1];
            }
        }
        
        return new DiceHand(rawMsg, rollee, totalMod, dice);
    }
    
    /**
     * Turn something that was typed by the user into a freshly rolled 
     * DiceHand object.
     * @param rawMsg Human created message
     * @return The resulting DiceHand. All the dice will be rolled
     */
    public synchronized static DiceHand humanFormatToDiceHand(String rawMsg, String rollee) {
        /* Expected format for each request by a human
         * Starts with plus or minus, followed by the value, which is
         * either a single number or "number 'd' number", with the first
         * number being the number of dice and the second being the size. If
         * there is no first number, the number of dice is 1
         * //not sure if this is the right way to write a regular expression//
         * '[+]|-' 'number'|[number|''] 'd' 'number' ... '+|-' ...
         * Essentially, everything is seperated by + and - signs, but the 
         * first + sign is optional
         */
        // is not valid if it contains anything that isn't a -, d, +, or number
        Pattern findPattern = Pattern.compile("[^-d+0-9]");
        if (Pattern.matches(findPattern.pattern(), rawMsg)) {
            System.out.println("The input \"" + rawMsg + "\" is not valid input");
            return null;
        }
        
        if (rawMsg == null) {
            return null; // no input
        }
        
        System.out.println("Parsing the human input: " + rawMsg);
        int totalMod = 0;
        ArrayList<Die> dice = new ArrayList<>();
        
        int signUsing = 0; // keep track of the where the sign we're using is
                           // since String.split() will remove the signs
        String[] elements = rawMsg.split("[+-]"); // split the pluses or minuses
        try {
            for (int i=0; i<elements.length; i++) {
                if (elements[i] == null || elements[i].length() == 0) { // skip empty elements
                    continue; 
                }

                boolean negative = (rawMsg.substring(signUsing, signUsing + 1)).equals("-");
                if (elements[i].contains("d")) {
                    // this is a dice!   
                    String[] diceParts = elements[i].split("d");
                    if (diceParts.length >= 1 && diceParts[0] != null && 
                            diceParts[1] != null && diceParts[1].length() > 0) {
                        
                        int numDice = 1; // default if there is no first parameter
                        if (diceParts[0].length() > 0) {
                            numDice = Integer.parseInt(diceParts[0]);
                        }
                        int diceSize = Integer.parseInt(diceParts[1]);
                        for (int times=0; times<numDice; times++) {
                            dice.add(new Die(diceSize, !negative)); // make and roll a new dice
                        }
                    }
                } else {
                    // this is a modifier!
                    int theValue = Integer.parseInt(elements[i]);
                    if (negative) {
                        theValue *= -1; //make the number negative if it was subtracted
                    }
                    totalMod += theValue; // add to the total
                }
                if (i == 0 && !rawMsg.startsWith("-")) {
                    signUsing--; // take a step back so we don't lose our place
                }
                // where the next sign SHOULD be
                signUsing += elements[i].length() + 1;
            }  
        } catch(Exception e) {
            System.out.println("THERE WAS AN ERROR PARSING THE HUMAN MADE STUFF");
            return null; // no diceHand if there was an error
        }   
        
        // woo
        return new DiceHand(rawMsg, rollee, totalMod, dice);
    }
}
