package chatRoom;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.swing.*;



/**
 *it contains  group chat
 * @author lenovo
 *
 */
public class ChatPanel extends JPanel {
	private static JTextArea display = new JTextArea();

	private static JButton send = new JButton("发送");
	private static JTextField input = new JTextField(20);
	private static JList allList;
	private static JComboBox jcb;
	private static JPopupMenu pop = new JPopupMenu();
	private static JMenuItem makeFriend = new JMenuItem("加为好友");
	// combine panel
	private JPanel diaPanel = new JPanel();
	private JScrollPane dialScrollPane;
	private JPanel sidePanel=new JPanel();
	
	private String hostName;

	public ChatPanel() {
		// set the display
		display.setLineWrap(true);// auto-line wrap
		display.setWrapStyleWord(true);
		display.setEditable(false);
		pop.add(makeFriend);
		// initiate
		initList();
		initScrollPane();
		initDiatPanel();
		add(diaPanel,BorderLayout.CENTER);
		JScrollPane a=new JScrollPane(allList);
		a.setPreferredSize(new Dimension(80,260));
		add(a,BorderLayout.EAST);
		
		//add listener
		send.addActionListener(new SendListener());
		allList.addMouseListener(new JListListener());
		makeFriend.addActionListener(new AddListener());
	}

	private void initList() {
		allList = new JList();
		allList.add(pop);
		jcb = new JComboBox();
		allList.setFixedCellWidth(110);
		allList.setBorder(BorderFactory.createTitledBorder("所有在线"));
	}

	/*
	 * initiate the scrollPane(display), set size(fixed) then put it in
	 * splitPane
	 */
	void initScrollPane() {
		dialScrollPane = new JScrollPane(display);
		dialScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		dialScrollPane.setPreferredSize(new Dimension(340, 215));
	}

	/*
	 * this is the main panel
	 */
	void initDiatPanel() {
		
		 diaPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.
		 createTitledBorder("聊天信息框"), BorderFactory.createEmptyBorder(5, 5, 5,
		 5)));
		JPanel subPanel = new JPanel();
		diaPanel.setLayout(new BorderLayout());
		diaPanel.add(dialScrollPane, BorderLayout.CENTER);
		diaPanel.add(new SendPanel(), BorderLayout.SOUTH);
	}
	
	//use to append and repaint
	public static void appendInfo(String s){
		display.append(s);
		display.paintImmediately(display.getBounds());
	}
	/* combine jcb, send and input to sendPanel */
	public class SendPanel extends JPanel {
		public SendPanel() {
			setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
			add(jcb);
			add(input);
			add(send);
		}
	}// end class

	public static JTextArea getDisplay() {
		return display;
	}

	public static JMenuItem getMakeFriend() {
		return makeFriend;
	}

	public static JButton getSend() {
		return send;
	}

	public static JTextField getInput() {
		return input;
	}

	public static JList getAllList() {
		return allList;
	}

	public static JComboBox getJcb() {
		return jcb;
	}

	public static void setDisplay(JTextArea display) {
		ChatPanel.display = display;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public static void setMakeFriend(JMenuItem makeFriend) {
		ChatPanel.makeFriend = makeFriend;
	}

	public static void setSend(JButton send) {
		ChatPanel.send = send;
	}

	public static void setInput(JTextField input) {
		ChatPanel.input = input;
	}

	public static void setAllList(JList allList) {
		ChatPanel.allList = allList;
	}

	public static void setJcb(JComboBox jcb) {
		ChatPanel.jcb = jcb;
	}

	// auto getters and setters
	
	/*put it in there in order to use the member
	 * hand out the send action
	 * */
	public class SendListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			String targetName=jcb.getSelectedItem().toString();
	                //send username and message
				 // i don't know why it may be null??? but it works
				 if(hostName==null||input.getText()=="")
					 ;
				  else if(targetName.equals("全体"))
					try {
						LoginPanel.getCl().sipClient.sendMessage("All", 
								  LoginPanel.getAddrInput().getText(), input.getText());
					} catch (ParseException | InvalidArgumentException | SipException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				else
					try {
						LoginPanel.getCl().sipClient.sendMessage("All", 
								  LoginPanel.getAddrInput().getText(), 
								  "@"+targetName+" "+input.getText());
					} catch (ParseException | InvalidArgumentException | SipException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				 input.setText("");
	            
		}

	}//end class
	
	public class JListListener extends MouseAdapter{
		public void mouseClicked(MouseEvent e) {
            if(hostName!=null&&e.getButton() == 3 && allList.getSelectedIndex() !=-1)
                    pop.show(e.getComponent(),e.getX(),e.getY());
      }
	}//end class
	
	public class AddListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if(hostName!=null)
			{
				
			 String targetClient = allList.getSelectedValue().toString();
			 //System.out.println("the target is:"+targetClient);	
			 String targetLocation=targetClient.substring(targetClient.indexOf("@"));
			 System.out.println("the action location is:"+targetLocation);
			 String currentClient="sip:"+hostName+"@"+LoginPanel.getAddrInput().getText();
			 System.out.println("name:"+currentClient);
			 if(targetClient.equals(currentClient))
				 JOptionPane.showMessageDialog(null, 
						 "你不能和自己建立好友关系", "消息提示", JOptionPane.ERROR_MESSAGE);
			 else{
				 LoginPanel.getCl().sipClient.sendAction(targetClient, 
						 targetLocation, "add");
			 }//end else

				
			}
				
		}
	}//end class


}
