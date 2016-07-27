package chatRoom;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.*;

import chatRoom.ChatPanel.AddListener;
import chatRoom.ChatPanel.JListListener;

public class PersonPanel extends JPanel {
	private static JButton disConn = new JButton("退出登录");
	private static JButton del=new JButton("删除好友");
	private static JList friendsList;
	private static Vector<String> friends = new Vector();
	private static JPanel userPanel=new JPanel();
	private static JPanel listPanel=new JPanel();
	private static JLabel name;
	
	private static JPopupMenu pop = new JPopupMenu();
	private static JMenuItem delFriend = new JMenuItem("删除好友");
	private static JMenuItem privateChat = new JMenuItem("进行私聊");
	
	public PersonPanel(){
		this.setSize(300, 400);
		initList();
		pop.add(privateChat);
		pop.add(delFriend);
		name=new JLabel(LoginPanel.getNameInput().getText()+"@"+LoginPanel.getAddrInput().getText());
		userPanel.add(name);
		userPanel.setPreferredSize(new Dimension(180,70));
		add(userPanel,BorderLayout.NORTH);

		JScrollPane a=new JScrollPane(friendsList);
		a.setPreferredSize(new Dimension(180,220));
		add(a,BorderLayout.CENTER);
		friendsList.addMouseListener(new JListListener());
		delFriend.addActionListener(new DelListener());
		privateChat.addActionListener(new ChatListener());
		
	}
	void initList(){
		
		friendsList = new JList(friends);
		friendsList.setFixedCellWidth(80);
		friendsList.setBorder(BorderFactory.createTitledBorder("我的好友"));
		friendsList.add(pop);
		// for test
		friends.addElement("bird");
	}
	public static JList getFriendsList() {
		return friendsList;
	}
	public static Vector<String> getFriends() {
		return friends;
	}
	public static void setFriendsList(JList friendsList) {
		PersonPanel.friendsList = friendsList;
	}
	public static void setFriends(Vector<String> friends) {
		PersonPanel.friends = friends;
	}
	
	
	public class JListListener extends MouseAdapter{
		public void mouseClicked(MouseEvent e) {
            if(e.getButton() == 3 && friendsList.getSelectedIndex() !=-1)
                    pop.show(e.getComponent(),e.getX(),e.getY());
      }
	}//end class
	
	public class DelListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			String rowName=friendsList.getSelectedValue().toString();
			String targetName;
			//revert to the origin name
			if(rowName.contains("(在线)"))
				targetName=rowName.replace("(在线)", "");
			else
				targetName=rowName.replace("(离线)", "");
			
			int choice=JOptionPane.showConfirmDialog(null, "确定要删除好友"+targetName+"吗？","断绝关系确认",JOptionPane.YES_NO_OPTION);
			if(choice==0){
				 try {
					MySocket.getToServer().writeUTF(
							 name.getText() + ";" + targetName + ";"
					 + "dgfgsd6rgfdt3re4gfgfdgdgdsgddd");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}
		
	}//end class
	
	public class ChatListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
            PrivateChatPanel.setHostName(name.getText());
            String rowName=friendsList.getSelectedValue().toString();
			String targetName;
			//revert to the origin name
			if(rowName.contains("(在线)"))
				targetName=rowName.replace("(在线)", "");
			else
				targetName=rowName.replace("(离线)", "");
			
            PrivateChatPanel p=LoginPanel.addPc(targetName);
            JFrame frame = new JFrame();
     		frame.setSize(400, 340);
     		p.setTarAddress(rowName.substring(rowName.indexOf("@")));
     		frame.setTitle("你 "+name.getText()+" 正在和 "+targetName+" 私聊");
     		frame.add(p);
     		frame.setLocationRelativeTo(null);
     		frame.setVisible(true);
     		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          
      
		}
		
	}//end class

}
