/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A packaged way for the DiceRollerServer to keep track of clients.
 * @author Owner
 */
public class DiceRollerClientHandle implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DiceRollerServer server;
    private Thread threadReference;
    
    public DiceRollerClientHandle(DiceRollerServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.out = null;
        this.in = null;
        try {
            this.out = new PrintWriter(socket.getOutputStream());
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(Exception e) {
            DiceRoller.printErr("Exception in a DiceRollerClientHandle object constructor: " 
                                + e.toString());
        }
    }
    
    public void startOnThread() {
        threadReference = new Thread(this);
        threadReference.start();
    }
    
    public void disconnect() {
        threadReference.interrupt();
        try {
            socket.close();
            in.close();
            out.close();
            socket = null;
            in = null;
            out = null;
        } catch(Exception e) {
            DiceRoller.printErr("Error closing this ClientHandle: " + e.toString());
        }
    }
    
    public void sendDiceHandOutput(DiceHand hand) {
        if (out != null && !socket.isClosed()) {
            try {
                 // send the clients the parser readable dice hand
                 out.println(DiceHandParser.translateToString(hand));
                 out.flush();
            } catch(Exception ioe) {
                DiceRoller.printErr("Error while sending dice output: " + ioe.toString());
            }
        } else {
            DiceRoller.printErr("This object's output object is null");
        }
    }
    
    @Override
    public void run() {
        try {
            String request = null;
            while ( !socket.isClosed() && (request = in.readLine()) != null) {
                if (request != null) {
                    if (request instanceof String) {
                        System.out.println("Got input: " + request);
                        // dice server initially gets human readable formats
                        String[] parts = request.split("&");
                        DiceHand receivedHand = DiceHandParser.humanFormatToDiceHand(parts[1], parts[0]);
                        if (receivedHand != null) {
                            server.broadcastDiceHandOutput(receivedHand);
                        }
                    } else {
                        server.disconnectClient(this);
                    }
                }
            }
        } catch (Exception e) {
            server.disconnectClient(this);
        }
    }
    
    public Socket getSocket() {
        return socket;
    }
}
