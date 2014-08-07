/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author Owner
 */
public class DiceRollerClient {
    
    public static void main(String[] args) {
        String ip = JOptionPane.showInputDialog(null, "Enter IP Address of server", "Need IP Address", JOptionPane.PLAIN_MESSAGE);
        try {
            InetAddress address = InetAddress.getByName(ip);
            final Socket socket = new Socket(address, DiceRoller.PORT);
            final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            
            final String name = JOptionPane.showInputDialog(null, "Enter a Username", "Need a Username", JOptionPane.PLAIN_MESSAGE);
            
            JFrame frame = new JFrame();
            frame.setSize(400, 300);
            frame.setTitle("Dice Roller Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter(){
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch(Exception ex) {
                        System.err.println("Error when closing the window: " + ex.toString());
                    }
                }
                
            });
            
            final JTextField textField = new JTextField();
            textField.setPreferredSize(new Dimension(400, 30));
            frame.add(textField, BorderLayout.SOUTH);
            textField.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (out != null) {
                        try {
                            out.writeObject(new DiceHandRequest(name, textField.getText()));
                            out.flush();
                            textField.setText("");
                        } catch(Exception ex) {
                            System.err.println("Exception when sending output to the server: " + ex.toString());
                        }
                    }
                }
                
            });
            
            final String top = "Draw dice on top", bottom = "Draw dice on bottom", right = "Draw dice on right", left = "Draw dice on left", none = "Don't draw dice";
            
            final JComboBox box = new JComboBox(new String[]{top, bottom, left, right, none});
            box.setPreferredSize(new Dimension(125, 25));
            box.setMaximumSize(new Dimension(125, 25));
            frame.add(box, BorderLayout.NORTH);
            
            frame.setVisible(true);
            DiceHandCanvas canvas = new DiceHandCanvas();
            canvas.setSize(400, 270);
            frame.add(canvas);
            
            
            
            DiceHand nextHand = null;
            while ( (nextHand = (DiceHand) in.readObject()) != null) {
                canvas.paintHand(nextHand);
            }
            
        } catch(UnknownHostException uhe) {
            JOptionPane.showMessageDialog(null, "Can't find host with IP \"" + ip + "\"", "Invalid IP", JOptionPane.ERROR_MESSAGE);
        } catch(Exception e) {
            System.err.println("Exception in the main class: " + e.toString());
        }
    }
}
