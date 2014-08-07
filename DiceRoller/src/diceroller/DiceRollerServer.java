/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * The server for the DiceRoller program.
 * @author Owner
 */
public class DiceRollerServer extends ServerSocket {
    
    private ArrayList<DiceRollerClientHandle> clients;
    private volatile boolean listening;
    
    public DiceRollerServer(int port) throws IOException {
        super(port);
        clients = new ArrayList<>();
        listening = false;
    }
    
    public void printInfo() {
        try {
            DiceRoller.print("Host IP address is \"" + InetAddress.getLocalHost().getHostAddress() + "\"");
        } catch(UnknownHostException uhe) {
            DiceRoller.printErr("Unable to get host IP address: " + uhe.toString());
        }
    }
    
    public void disconnectClient(DiceRollerClientHandle handle) {
        clients.remove(handle);
        handle.disconnect();
        DiceRoller.print("Disconnected a client");
    }
    
    public void disconnectAll() {
        for (DiceRollerClientHandle client : clients) {
            disconnectClient(client);
        }
        clients.clear();
    }
    
    public void stopListening() {
        listening = false;
    }
    
    public void listenForClients() {
        Socket nextSocket = null;
        listening = true;
        try {
            this.setSoTimeout(1000);
            while ( listening ) {
                nextSocket = null;
                try {
                    nextSocket = accept();
                } catch (SocketTimeoutException ste) {
                    //Gonna try again in a tick
                }
                if (nextSocket!=null) {
                    DiceRollerClientHandle handle = new DiceRollerClientHandle(this, nextSocket);
                    handle.startOnThread();
                    clients.add(handle);
                    DiceRoller.print("Added a new client!");
                }
            }
        } catch(Exception e) {
            DiceRoller.printErr("Exception while listening for clients: " 
                    + e.toString());
        }
        DiceRoller.print("Closing server");
        disconnectAll();
        try {
            close();
        } catch(Exception e) {
            DiceRoller.printErr("Error closing server");
        }
    }
    
    private enum TokenTag {
        NUMBER, D, PLUS, MINUS;
    }
    
    private class Token {
        
        TokenTag tag;
        String contents;
        
        public Token(TokenTag tag, String contents) {
            this.tag = tag;
            this.contents = contents;
        }
        
        @Override
        public String toString() {
            return this.contents;
        }
    }
    
    public void handleRequest(DiceHandRequest request) {
        DiceHand parsedHand = null;
        DiceRoller.print("Got a dice role request");
        String input = request.getInput();
        
        ArrayList<Token> tokens = new ArrayList<>();
        System.out.println("Stared processing print stuff. \nInput:\n"+input);
        for (int p=0; p<input.length(); p++) {
            boolean gettingNumbers = true;
            int size = 0;
            String testArea = null;
            do {
                testArea = input.substring(p, p + size + 1);
                try {
                    Integer.valueOf(testArea);
                } catch(NumberFormatException nfe) {
                    System.out.println(nfe.toString());
                    gettingNumbers = false;
                }
                if (gettingNumbers) {
                    size++;
                } else {
                    if (size > 0) {
                        tokens.add(new Token(TokenTag.NUMBER, input.substring(p, p + size)));
                        p = p + size;
                    }
                }
            } while (gettingNumbers);
            
            if (p < input.length()) {
                char on = input.charAt(p);
                if (on == 'd') {
                    tokens.add(new DiceRollerServer.Token(DiceRollerServer.TokenTag.D, "d"));
                } else if (on == '+') {
                    tokens.add(new DiceRollerServer.Token(DiceRollerServer.TokenTag.PLUS, "+"));
                } else if (on == '-') {
                    tokens.add(new DiceRollerServer.Token(DiceRollerServer.TokenTag.MINUS, "-"));
                } else {
                    System.err.println("Unexpected character at index "+p);
                }
            }
        }
        
        System.out.println("Tokens found: ");
        for (int i=0; i<tokens.size(); i++) {
            System.out.println(i + " : " + tokens.get(i).toString());
        }
        
        parsedHand = new DiceHand(input, request.getName(), 0, new Die[]{new Die(20)});
        broadcastDiceHandOutput(parsedHand);
        System.out.println("Done handling input");
    }
    
    private void broadcastDiceHandOutput(DiceHand hand) {
        for (int i=0; i<clients.size(); i++) {
            try {
                 if (!clients.get(i).getSocket().isClosed()) {
                    clients.get(i).sendDiceHandOutput(hand);
                 } else {
                     disconnectClient(clients.get(i));
                 }
              } catch(Exception e) {
                DiceRoller.printErr("Error with broadcasting output to a client: " + e.toString());
            }
        }
        DiceRoller.print("Broadcasted dice roll results to " + clients.size() + " clients");
    }
}
