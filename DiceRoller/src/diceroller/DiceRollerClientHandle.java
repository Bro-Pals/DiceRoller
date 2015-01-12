/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import bropals.lib.simplegame.networking.Client;
import bropals.lib.simplegame.networking.ClientMessageHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 
 */
public class DiceRollerClientHandle implements ClientMessageHandler {
    
    private DiceHandCanvas canvas;
    
    public DiceRollerClientHandle(DiceHandCanvas c) {
        canvas = c;
    }
    
    @Override
    public void handleMessage(Client client, String string) {
        System.out.println("We got a message from the server: " + string);
        // display hands the client gets
        DiceHand hand = DiceHandParser.translateToDiceHand(string);
        canvas.paintHand(hand);
    }

}
