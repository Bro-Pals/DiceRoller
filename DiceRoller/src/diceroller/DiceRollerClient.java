/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diceroller;

import bropals.lib.simplegame.networking.Client;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
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
        BufferedImage diceImages = null;
        try {
            diceImages = (BufferedImage) ImageIO.read(DiceRollerClient.class.getResourceAsStream("../img/dice.png"));
        } catch(Exception e) {
            System.out.println("Error when getting the dice images");
        }
        String ip = JOptionPane.showInputDialog(null, "Enter IP Address of server", "Need IP Address", JOptionPane.PLAIN_MESSAGE);
        try {
            InetAddress address = InetAddress.getByName(ip);
            
            String nn = "NO_USERNAME";
            boolean validValue = false;
            while(!validValue) {
                nn = JOptionPane.showInputDialog(null, "Enter a valid Username", 
                        "Need a valid Username", JOptionPane.PLAIN_MESSAGE);
                validValue = !(nn.contains("&") || nn.contains(" ") || nn.contains(":") 
                        || nn.length() == 0);
                if (!validValue) {
                    JOptionPane.showMessageDialog(null, "Usernames can't contain spaces, &, or :",
                        "Not a valid username", JOptionPane.ERROR_MESSAGE);
                }
            }
            final String username = nn;
            // save the last 10 messages
            final int historyLimit = 10;
            final CountWrapper historyPointer = new CountWrapper(-1); // point to which last message you're on
            final ArrayList<String> sentHistory = new ArrayList<>();
            
            JFrame frame = new JFrame();
            frame.setSize(550, 400);
            frame.setTitle("Dice Roller Client | " + username);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter(){
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                    try {
                        // stop the server?
                    } catch(Exception ex) {
                        System.err.println("Error when closing the window: " + ex.toString());
                    }
                }
                
            });
            
            final JTextField textField = new JTextField();
            textField.setPreferredSize(new Dimension(400, 30));
            frame.add(textField, BorderLayout.SOUTH);
            textField.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        //System.out.println("UP");
                        if (historyPointer.getValue() + 1 < sentHistory.size() &&
                                sentHistory.get(historyPointer.getValue() + 1) != null &&
                                historyPointer.getValue() < historyLimit - 2) {
                            historyPointer.increase();
                        }
                        if (sentHistory.get(historyPointer.getValue()) != null)
                            textField.setText(sentHistory.get(historyPointer.getValue()));
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                       // System.out.println("DOWN");
                        if (historyPointer.getValue() - 1 >= 0 &&
                                sentHistory.get(historyPointer.getValue() - 1) != null &&
                                historyPointer.getValue() > 0) {
                            historyPointer.decrease();
                        }
                        if (sentHistory.get(historyPointer.getValue()) != null)
                            textField.setText(sentHistory.get(historyPointer.getValue()));
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {}
                @Override
                public void keyTyped(KeyEvent e) {}
            });
            
            frame.setVisible(true);
            DiceHandCanvas canvas = new DiceHandCanvas();
            canvas.setSize(400, 270);
            frame.add(canvas);
            if (diceImages != null) {
                canvas.giveDiceImage(diceImages);
            }
            System.out.println("About to listen...");

            final Client rollerClient = new Client(address, DiceRoller.PORT, 
                 new DiceRollerClientHandle(canvas));
            System.out.println("Made a client object");
            rollerClient.listenToServer();
            System.out.println("connected to the server");
            
            textField.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (rollerClient != null) {
                        try {
                            // garuntee no |s will be there beforehand
                            String text = textField.getText().replace("&", "");
                            text = text.replace(" ", ""); // no spaces allowed
                            sentHistory.add(0, text);
                            if (sentHistory.size() > historyLimit) {
                                sentHistory.remove(historyLimit-1); // remove last message
                            }
                            // send the HUMAN READABLE format to the server
                            rollerClient.sendMessageToServer(username + "&" + text);
                            System.out.println("Sending a message...");
                            textField.setText("");
                            historyPointer.setValue(-1);
                        } catch(Exception ex) {
                            System.err.println("Exception when sending output to the server: " + ex.toString());
                        }
                    }
                }
                
            });
            
        } catch(UnknownHostException uhe) {
            JOptionPane.showMessageDialog(null, "Can't find host with IP \"" + ip + "\"", "Invalid IP", JOptionPane.ERROR_MESSAGE);
        } catch(Exception e) {
            System.err.println("Exception in the main class: " + e.toString());
        }
    }
}
