/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

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
    
    public void handleRequest(DiceHandRequest request) {
        DiceHand parsedHand = null;
        DiceRoller.print("Got a dice role request");
        parsedHand = new DiceHand(request.getInput(), request.getName(), 0, new Die[]{new Die(20)});
        
        broadcastDiceHandOutput(parsedHand);
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
