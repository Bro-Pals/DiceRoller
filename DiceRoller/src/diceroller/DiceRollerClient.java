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
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
            final Socket socket = new Socket(address, DiceRoller.PORT);
            final PrintWriter out = new PrintWriter(socket.getOutputStream());
            final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
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
            
            JFrame frame = new JFrame();
            frame.setSize(550, 400);
            frame.setTitle("Dice Roller Client | " + username);
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
                            // garuntee no |s will be there beforehand
                            String text = textField.getText().replace("&", "");
                            text = text.replace(" ", ""); // no spaces allowed
                            // send the HUMAN READABLE format to the server
                            out.println(username + "&" + text);
                            out.flush();
                            textField.setText("");
                        } catch(Exception ex) {
                            System.err.println("Exception when sending output to the server: " + ex.toString());
                        }
                    }
                }
                
            });
            
            frame.setVisible(true);
            DiceHandCanvas canvas = new DiceHandCanvas();
            canvas.setSize(400, 270);
            frame.add(canvas);
            if (diceImages != null) {
                canvas.giveDiceImage(diceImages);
            }

            String msg = null;
            while ((msg = in.readLine()) != null) {
                System.out.println("Got something back : " + msg);
                // clients get the PARSER READ format
                DiceHand hand = DiceHandParser.translateToDiceHand(msg);
                canvas.paintHand(hand);
            }
            socket.close();  
        } catch(UnknownHostException uhe) {
            JOptionPane.showMessageDialog(null, "Can't find host with IP \"" + ip + "\"", "Invalid IP", JOptionPane.ERROR_MESSAGE);
        } catch(Exception e) {
            System.err.println("Exception in the main class: " + e.toString());
        }
    }
}
