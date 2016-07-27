package chatRoom;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;


public class ConnectListener implements ActionListener {
	ChatPanel c = new ChatPanel();
	DefaultListModel dlm = new DefaultListModel();
	DefaultListModel fds = new DefaultListModel();
	PersonPanel p=new PersonPanel();
	String holdName="";
	Client sipClient;//the sipstack
	
	String finalName="";
	public void removeAllNames() {
		c.getJcb().removeAllItems();
		dlm.clear();
		fds.clear();
	}

	public void addName(String username) {
		c.getJcb().addItem(username);
		dlm.addElement(username);
	
	}
	/*
	 * create a new panel ,that is the main panel in order to start chanting
	 */
	public void mainFrame(ChatPanel c) {
		JFrame frame = new JFrame();
		frame.setSize(610, 340);
		c.setHostName(LoginPanel.getNameInput().getText());
		frame.setTitle("当前用户 "+c.getHostName());
		frame.add(c);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
    public void personFrame(PersonPanel p){
    	PersonFrame frame=new PersonFrame();
    	frame.add(p);
		//frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {

      //deactivate some components
        LoginPanel.disapear();
        
      //init the sip client
      		String username=LoginPanel.getNameInput().getText();
      		String address=LoginPanel.getAddrInput().getText();
      		String IP = address.substring(0,address.indexOf(":"));
      		finalName=username+"@"+address;
      		System.out.println("local address is "+IP+"  "+address.substring(address.indexOf(":")+1));
      		int port=Integer.parseInt(address.substring(address.indexOf(":")+1));
      		sipClient=new Client(username,address);
      		try {
      			//register into the server
      			sipClient.sendInvite(username, address);
      			personFrame(p);
          		mainFrame(c);
      		} catch (ParseException e1) {
      			// TODO Auto-generated catch block
      			e1.printStackTrace();
      		} catch (InvalidArgumentException e1) {
      			// TODO Auto-generated catch block
      			e1.printStackTrace();
      		} catch (SipException e1) {
      			// TODO Auto-generated catch block
      			e1.printStackTrace();
      		}    		
    }
    
}
