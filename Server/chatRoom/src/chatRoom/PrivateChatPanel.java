package chatRoom;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import chatRoom.ChatPanel.SendListener;
import chatRoom.ChatPanel.SendPanel;

public class PrivateChatPanel extends JPanel {
	private JTextArea display = new JTextArea();

	private JButton send = new JButton("发送");
	private JTextField input = new JTextField(20);

	// combine panel
	private JPanel diaPanel = new JPanel();
	private JScrollPane dialScrollPane;
	private JPanel sidePanel=new JPanel();
	private static String hostName=null;
	private String tarName=null;
	String tarAddress=null;
	public String getTarAddress() {
		return tarAddress;
	}


	public void setTarAddress(String tarAddress) {
		this.tarAddress = tarAddress;
	}


	public PrivateChatPanel(){
		// set the display
		display.setLineWrap(true);// auto-line wrap
		display.setWrapStyleWord(true);
		display.setEditable(false);
		dialScrollPane = new JScrollPane(display);
		dialScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		dialScrollPane.setPreferredSize(new Dimension(340, 215));
		 diaPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.
				 createTitledBorder("聊天信息框"), BorderFactory.createEmptyBorder(5, 5, 5,
				 5)));
		JPanel subPanel = new JPanel();
		diaPanel.setLayout(new BorderLayout());
		diaPanel.add(dialScrollPane, BorderLayout.CENTER);
		diaPanel.add(new SendPanel(), BorderLayout.SOUTH);
		add(diaPanel,BorderLayout.CENTER);
		
		
		send.addActionListener(new SendListener());
	}


	/* combine jcb, send and input to sendPanel */
	public class SendPanel extends JPanel {
		public SendPanel() {
			setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
			add(input);
			add(send);
		}
	}// end class
	
	
	public class SendListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
				 // i don't know why it may be null??? but it works
			
				 if(hostName!=null&&tarName!=null){ 
					 try {
						LoginPanel.getCl().sipClient.sendMessage(tarName, 
								 tarAddress,input.getText());
					} catch (ParseException | InvalidArgumentException | SipException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		              display.append("你说："+input.getText()+"\n");
		              input.setText("");
				 }
		}

	}//end class


	public JTextArea getDisplay() {
		return display;
	}


	public static String getHostName() {
		return hostName;
	}


	public String getTarName() {
		return tarName;
	}


	public void setDisplay(JTextArea display) {
		this.display = display;
	}


	public static void setHostName(String hostName) {
		PrivateChatPanel.hostName = hostName;
	}


	public void setTarName(String tarName) {
		this.tarName = tarName;
	}




}
