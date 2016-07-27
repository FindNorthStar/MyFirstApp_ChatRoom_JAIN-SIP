package chatRoom;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.*;

public class LoginPanel extends JPanel {

	private static JFrame frame = new JFrame("CC");
	private JLabel nameTip = new JLabel("       用户名");
	private static JTextField nameInput = new JTextField(10);
	
	private JLabel addrTip = new JLabel("        你的地址");
	private static JTextField addrInput = new JTextField(20);
	
	private JButton login = new JButton("登录");
	
	private static PrivateChatPanel[] pc=new PrivateChatPanel[10];
	private static int number=0;
	private static ConnectListener cl=new ConnectListener();
	// add main frame to login panel, in order to change sub-panel
	public static void main(String[] args) {

		frame.setSize(300, 300);
		LoginPanel loginPanel = new LoginPanel();
		frame.add(loginPanel);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public static void disapear() {
		frame.dispose();
	}

	public LoginPanel() {
		setLayout(null);
		add(nameTip);
		add(nameInput);
		add(addrTip);
		add(addrInput);
		add(login);
		nameInput.setText("cat");
		addrInput.setText("127.0.0.1:5060");
		// set fixed position
		addrTip.setBounds(145, 20, 150, 100);
		nameTip.setBounds(20, 20, 150, 100);
		nameInput.setBounds(20, 100, 80, 30);
		addrInput.setBounds(120, 100, 150, 30);
		login.setBounds(100, 150, 80, 30);

		// add listener
		login.addActionListener(cl);
	}// end constructor

	public static JTextField getNameInput() {
		return nameInput;
	}

	public static ConnectListener getCl() {
		return cl;
	}

	public static void setCl(ConnectListener cl) {
		LoginPanel.cl = cl;
	}

	public void setNameInput(JTextField nameInput) {
		this.nameInput = nameInput;
	}

	public static JTextField getAddrInput() {
		return addrInput;
	}

	public static void setAddrInput(JTextField addrInput) {
		LoginPanel.addrInput = addrInput;
	}

	public static JFrame getFrame() {
		return frame;
	}

	public static void setFrame(JFrame frame) {
		LoginPanel.frame = frame;
	}

	public static PrivateChatPanel[] getPc() {
		return pc;
	}

	public static void setPc(PrivateChatPanel[] pc) {
		LoginPanel.pc = pc;
	}
	public static PrivateChatPanel addPc(String name) {
		int n=number;
		pc[n]=new PrivateChatPanel();
		pc[n].setTarName(name);
		number++;
		return pc[n];
	}
	public static PrivateChatPanel findPc(String name) {
		for(int i=0;i<number;i++){
			if(pc[i].getTarName().equals(name))
				return pc[i];
		}
		return null;
	}
}
