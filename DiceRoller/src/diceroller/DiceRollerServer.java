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
        NUMBER, D, PLUS, MINUS, ENDSYMBOL; // D is just the chracter 'd'
    }
    
    private enum PSymbol {
        E, V, X, K, // Nonterminals: Start symbol E, value V, X, and K
        /**Character d terminal*/
        d, 
        /**Modifier terminal*/
        m,
        /**Plus sign terminal*/
        p, 
        /**Subtraction sign / minus sign terminal*/
        s; // Terminals: dice roll, modifier, plus, and minus (subtraction)
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
            String testArea = "";
            do {
                if (p + size < input.length()) {
                    testArea = input.substring(p, p + size + 1);
                } else {
                    testArea = "";
                }
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
                if (on == 'd' || on == 'D') {
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
        //** Time to start parsing the tokens!
        
        // add on the end symbol so we know when the symbol ends
        tokens.add(new Token(TokenTag.ENDSYMBOL, "$")); 
        
        ArrayList<PSymbol> stack = new ArrayList<>();
        stack.add(PSymbol.E); // stack starts with the start symbol
        
        String errorMsg = null;
        // while there is no error or while the token on the top of the stack is
        // not the ending symbol.
        int tokenOn = 0;
        while (errorMsg == null) {
            switch (stack.get(0)) {
                case E: // start symbol and nonterminal
                    switch(tokens.get(tokenOn).tag) {
                        case NUMBER:
                            stack.remove(0);
                            stack.add(0, PSymbol.X);
                            stack.add(0, PSymbol.V); // replace E with VX
                            break;
                        case ENDSYMBOL:
                            stack.remove(0);
                            stack.add(0, PSymbol.X); // replace E with X
                            break;
                        default:
                            errorMsg = "Expected number by \"" + tokens.get(tokenOn).contents + "\"";
                            break;
                    }
                    break;
                case X: // nonterminal
                    switch(tokens.get(tokenOn).tag) {
                        case PLUS:
                            stack.remove(0);
                            stack.add(0, PSymbol.E);
                            stack.add(0, PSymbol.p); // add '+'E
                            break;
                        case MINUS:
                            stack.remove(0);
                            stack.add(0, PSymbol.E);
                            stack.add(0, PSymbol.s); // add '-'E
                            break;
                        case ENDSYMBOL:
                            errorMsg = "This is a valid string!";
                            break;
                        default:
                            errorMsg = "Did not expect another number by \"" + tokens.get(tokenOn).contents + "\"";
                            break;
                    }
                    break;
                case V:
                    switch(tokens.get(tokenOn).tag) {
                        case NUMBER:
                            stack.remove(0);
                            stack.add(0, PSymbol.K);
                            stack.add(0, PSymbol.m); // add 'number'K
                            break;
                        default:
                            errorMsg = "Expected a number token by \"" + tokens.get(tokenOn).contents + "\"";
                            break;
                    }
                    break;
                case K:
                    switch(tokens.get(tokenOn).tag) {
                        case D:
                            stack.remove(0);
                            stack.add(0, PSymbol.m);
                            stack.add(0, PSymbol.d); // add 'd''number'
                            break;
                        case PLUS:
                        case MINUS:
                            stack.remove(0); // epsilon
                            break;
                        case ENDSYMBOL:
                            errorMsg = "This is a valid string!";
                            break;
                        default:
                            errorMsg = "Unexpected number token by \"" + tokens.get(tokenOn).contents + "\"";
                            break;
                    }
                    break;
                case d:
                    switch(tokens.get(tokenOn).tag) {
                        case D:
                            stack.remove(0); // remove from stack
                            tokenOn++; // advance input
                            break;
                        default:
                            errorMsg = "Expect character D by \"" + tokens.get(tokenOn).contents + "\"";
                            break;
                    }
                    break;
                case m:
                    switch(tokens.get(tokenOn).tag) {
                        case NUMBER:
                            stack.remove(0);
                            tokenOn++;
                            break;
                        default:
                            errorMsg = "Expected number by \"" + tokens.get(tokenOn).contents + "\"";
                            break;
                    }
                    break;
                case p:
                    switch(tokens.get(tokenOn).tag) {
                        case PLUS:
                            stack.remove(0);
                            tokenOn++;
                            break;
                        default:
                            errorMsg = "Expected plus sign by \"" + tokens.get(tokenOn).contents + "\"";
                            break;
                    }
                    break;
                case s:
                    switch(tokens.get(tokenOn).tag) {
                        case MINUS:
                            stack.remove(0);
                            tokenOn++;
                            break;
                        default:
                            errorMsg = "Expected minus sign by \"" + tokens.get(tokenOn).contents + "\"";
                            break;
                    }
                    break;
            }
        }
        System.out.println("Stack: " + stack.toString());
        System.out.println(errorMsg);
        
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
