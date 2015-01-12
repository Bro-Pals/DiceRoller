/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import bropals.lib.simplegame.networking.ClientHandler;
import bropals.lib.simplegame.networking.Server;
import bropals.lib.simplegame.networking.ServerMessageHandler;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * @author Owner
 */
public class DiceRollerServer implements ServerMessageHandler {

    @Override
    public void handleMessage(Server server, ClientHandler ch, String string) {
        // turn the input into a dice hand
        String[] tokens = string.split("&");
        DiceHand playerHand = DiceHandParser.humanFormatToDiceHand(tokens[1], tokens[0]);
        
        // resend the DiceHand after being parsed back to a string and was rolled
        if (playerHand != null) {
            String sendOutString = DiceHandParser.translateToString(playerHand);
            server.broadcastMessage(sendOutString);
        }
    }
}
