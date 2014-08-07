/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * A packaged way for the DiceRollerServer to keep track of clients.
 * @author Owner
 */
public class DiceRollerClientHandle implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private DiceRollerServer server;
    private Thread threadReference;
    
    public DiceRollerClientHandle(DiceRollerServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.out = null;
        this.in = null;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
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
    
    public void sendDiceHandOutput(DiceHand hand) throws IOException {
        if (out != null && !socket.isClosed()) {
            try {
                 out.writeObject((Object)hand);
                 out.flush();
            } catch(IOException ioe) {
                DiceRoller.printErr("Error while sending dice output: " + ioe.toString());
            }
        } else {
            DiceRoller.printErr("This object's output object is null");
        }
    }
    
    @Override
    public void run() {
        try {
            Object request = null;
            while ( !socket.isClosed() && (request = in.readObject()) != null) {
                if (request != null) {
                    if (request instanceof DiceHandRequest) {
                        server.handleRequest((DiceHandRequest)request);
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
