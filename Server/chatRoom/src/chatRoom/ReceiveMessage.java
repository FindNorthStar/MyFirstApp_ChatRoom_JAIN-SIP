package chatRoom;

import java.io.IOException;
import java.text.ParseException;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * show receiving messages Created by FindNS@outlook.com on 2015/12/3.
 * 
 * @author Yue Zhao
 */
public class ReceiveMessage implements Runnable {
	ChatPanel c = new ChatPanel();
	DefaultListModel dlm = new DefaultListModel();
	DefaultListModel fds = new DefaultListModel();
	PersonPanel p=new PersonPanel();
	String holdName="";
	Client sipClient;//the sipstack
	public void removeComboBoxItem(String username) {
	
	}

	public void removeAllNames() {
		c.getJcb().removeAllItems();
		dlm.clear();
		fds.clear();
	}

	public void addName(String username) {
		c.getJcb().addItem(username);
		dlm.addElement(username);
		dlm.
	
	}

	/*
	 * create a new panel ,that is the main panel in order to start chanting
	 */
	void mainFrame(ChatPanel c) {
		JFrame frame = new JFrame();
		frame.setSize(500, 340);
		c.setHostName(LoginPanel.getNameInput().getText());
		frame.setTitle("当前用户 "+c.getHostName());
		frame.add(c);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
    void personFrame(PersonPanel p){
    	PersonFrame frame=new PersonFrame();
    	frame.add(p);
		//frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
    }
	@Override
	public void run() {
		//init the sip client
		String username=LoginPanel.getNameInput().getText();
		String address=LoginPanel.getAddrInput().getText();
		String IP = address.substring(0,address.indexOf(":"));
		System.out.println(IP+"ddd"+address.substring(address.indexOf(":")+1));
		int port=Integer.parseInt(address.substring(address.indexOf(":")+1));
		sipClient=new Client(username,IP,port);
		try {
			sipClient.sendInvite(username, address);
			JOptionPane.showMessageDialog(null,"连接成功!","消息提示",JOptionPane.INFORMATION_MESSAGE);
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
		
		personFrame(p);
		mainFrame(c);
		
		while (true) {
			String receiveInfo = null;
			try {
				// listening server message
				receiveInfo = MySocket.getFromServer().readUTF();
				System.out.println(receiveInfo);
				if (receiveInfo.contains(" 上线了。")) {// if someone enters the room fetch user lists
					MySocket.getToServer().writeUTF(
							LoginPanel.getNameInput().getText() + ";74661f669fc89cfeef4d48f7ba61ad55c777b9b4");
					c.appendInfo(receiveInfo);
				} 
				else if (receiveInfo.contains(" af8fb5d8d7a247312c1262c6b46a59f51050e12d")) 
				{//get list
					String receive = receiveInfo.replace(" af8fb5d8d7a247312c1262c6b46a59f51050e12d", "");
					String readyToAdd[]=receive.split("#@");
					readyToAdd[0]=readyToAdd[0].trim();
					readyToAdd[1]=readyToAdd[1].trim();
					String fs[] = readyToAdd[0].split(";");
					String items[] = readyToAdd[1].split(";");
					//everytime clear all the old items and refresh
					removeAllNames();

					if(fs.length>0&&fs[0]!=""){
						for (String f : fs) {
							int flag=0;
							for(String item:items){
								if(f.equals(item))
								{
									fds.addElement(f+"(在线)");
									flag=1;
									break;
								}
							}
							if(flag==0)
								fds.addElement(f+"(离线)");
						}

					}
					
					//loop to add
					c.getJcb().addItem("全体");
					for (String item : items) {
						addName(item.trim());
					}
					// set model
					p.getFriendsList().setModel(fds);
					c.getAllList().setModel(dlm);
					// addComboBoxItem("全体成员");
				} else if (receiveInfo.contains(" b9dded77d303219e5a92260272748e0e8e96d0e2")) {
					// ask for being a friend
					String readyToAdd = receiveInfo.replace(" b9dded77d303219e5a92260272748e0e8e96d0e2", "");
					String items[] = readyToAdd.split(";");
					items[0]=items[0].trim();
					items[1]=items[1].trim();
					if(items[0].equals("ask")){
						//show the ask information
						int choice=JOptionPane.showConfirmDialog(null, items[1]+"想要添加你为好友","好友申请",JOptionPane.YES_NO_CANCEL_OPTION);
						if(choice==0)//agree
						{
							MySocket.getToServer().writeUTF(
									"agree;" +c.getHostName()+";"+items[1]+ ";74661f669fc89cfeef4d48f7ba61ad55c777b9b4");
			
						}
						else if(choice==1)//disagree
						{
							MySocket.getToServer().writeUTF(
								"disagree;" +c.getHostName()+";"+items[1]+ ";74661f669fc89cfeef4d48f7ba61ad55c777b9b4");
							c.appendInfo("你拒绝了"+items[1]+"的好友申请，对方将收到拒绝提示。\n");
						}
						else//cancel
							c.appendInfo("你忽视了"+items[1]+"的好友申请，对方收不到任何提示。\n");
						
					}//end if ask
					else if(items[0].equals("nck")){//being rejected
						JOptionPane.showMessageDialog(null, 
								"很遗憾，你的好友申请被"+items[1]+"拒绝了！", "消息提示", JOptionPane.INFORMATION_MESSAGE);
					}
					else if(items[0].equals("ack")){
						//add firends
						fds.addElement(items[1]);
						p.getFriendsList().setModel(fds);
					}
								
				} else if (receiveInfo.contains(" 706c9530b099afb9277fbc936b82e0f4910d7f41")) {
					//退出的时候
					removeComboBoxItem(receiveInfo.replace(" 706c9530b099afb9277fbc936b82e0f4910d7f41", ""));
				} 
				else if(receiveInfo.contains("dgfgsd6rgfdt3re4gfgfdgdgdsgddd"))
				{
					String readyToAdd=receiveInfo.replace(" dgfgsd6rgfdt3re4gfgfdgdgdsgddd", "");
					String items[]=readyToAdd.split(";");
					if(items[0].trim().equals("del"))
					{
						String name1=items[1].trim()+"(离线)";
						String name2=items[1].trim()+"(在线)";
						fds.removeElement(name1);
						fds.removeElement(name2);
						p.getFriendsList().setModel(fds);
					}
				}
				//if gets a private chat message
				else if(receiveInfo.contains("qwertyuiodsbnm77664f"))
				{
					String readyToAdd=receiveInfo.replace(" qwertyuiodsbnm77664f","");
					PrivateChatPanel.setHostName(c.getHostName());
			        String items[]=readyToAdd.split(":");
			        PrivateChatPanel pc=LoginPanel.findPc(items[0].trim());
			        if(pc==null)
			        {
			        	pc=LoginPanel.addPc(items[0].trim());
			        	
			        	 JFrame frame = new JFrame();
			      		frame.setSize(400, 340);
			      		
			      		frame.setTitle("你 "+c.getHostName()+" 正在和 "+items[0].trim()+" 私聊");
			      		frame.add(pc);
			      		frame.setLocationRelativeTo(null);
			      		frame.setVisible(true);
			      		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			           
			        }

			        	
			        pc.getDisplay().append(readyToAdd+"\n");
					
				}
				else {
					// append receive string in display area
					ChatPanel.appendInfo(receiveInfo);
				}

			} catch (IOException e) {
				break;
			}
		}
	}
}
