/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The main class.
 * @author Owner
 */
public class DiceRoller {

    static final int PORT = 24232;
    public static final JTextArea area = new JTextArea();
    
    public static void print(String text) {
        String str = text + System.getProperty("line.separator");
        area.append(str);
        System.out.print(str);
    }
    
    public static void printErr(String text) {
        String str = "ERROR: " + text + System.getProperty("line.separator");
        area.append(str);
        System.err.print(str);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final JFrame serverFrame = new JFrame("Dice Roller Server Window");
        try {
            final DiceRollerServer server = new DiceRollerServer(PORT);
            ///Making swing gui
            area.setEditable(false);
            area.setLineWrap(true);
            JScrollPane pane = new JScrollPane(area);
            pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            pane.createVerticalScrollBar();
            serverFrame.add(pane);
            serverFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    DiceRoller.print("Stopping the server");
                    server.stopListening();
                }   
            });
            serverFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            serverFrame.setSize(400, 300);
            serverFrame.setLocationRelativeTo(null);
            serverFrame.setVisible(true);
            ///Starting the server
            
            DiceRoller.print("Created server");
            server.printInfo();
            DiceRoller.print("Port forward to this computer on port " + PORT);
            server.listenForClients();
            DiceRoller.print("Stopped listening for clients");
        } catch(Exception e) {
            DiceRoller.printErr("Exception in the main class; " + e.toString());
        }
        serverFrame.dispose();
    }
}
