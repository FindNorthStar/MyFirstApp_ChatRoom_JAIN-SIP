package chatRoom;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TooManyListenersException;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Client  implements SipListener{
	private static SipProvider sipProvider;

	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;

	private static SipStack sipStack;
	
	String transport = "udp";
	String peerHostPort = "127.0.0.1:5070";//this is the server address
	
	//to save the local name and address
	String username;
	String localAddress;
	ConnectListener cl;

	// the parameter is the ip and port the user entered
	public Client(String username,String address){
		this.username=username;
		localAddress=address;
  		String IP = address.substring(0,address.indexOf(":"));
  		int port=Integer.parseInt(address.substring(address.indexOf(":")+1));
		init(IP,port);
	}
	private void init(String clientIP,int clientPort) {
		cl=LoginPanel.getCl();
		// TODO Auto-generated method stub
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		// If you want to try TCP transport change the following to
		
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
				+ transport);
		// If you want to use UDP then uncomment this.
		properties.setProperty("javax.sip.STACK_NAME", "client");
		properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
		"clientdebuglog.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
		"clientlog.txt");
		try {
			sipStack = sipFactory.createSipStack(properties);
		} catch (PeerUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("createSipStack " + sipStack);
		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			
			ListeningPoint tcp = sipStack.createListeningPoint(clientIP,clientPort, "tcp");//this is client address
			ListeningPoint udp = sipStack.createListeningPoint(clientIP,clientPort, "udp");
			
			sipProvider = sipStack.createSipProvider(tcp);
			sipProvider.addSipListener(this);
			sipProvider = sipStack.createSipProvider(udp);
			sipProvider.addSipListener(this);

		} catch (PeerUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ObjectInUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TooManyListenersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String toName,String to,String message) throws ParseException, InvalidArgumentException, SipException{

		// create >From Header
		SipURI fromAddress = addressFactory.createSipURI(username,
				localAddress);

		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(username);
		FromHeader fromHeader = headerFactory.createFromHeader(
				fromNameAddress, "12345");
		
		// create To Header
		SipURI toAddress = addressFactory
				.createSipURI(toName, to);
		Address toNameAddress = addressFactory.createAddress(toAddress);
		toNameAddress.setDisplayName(toName);
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
				null);
		
		// create Request URI
		SipURI requestURI = addressFactory.createSipURI(toName,
				peerHostPort);//连接到服务器
		
		//create viaheaders
		ArrayList viaHeaders = new ArrayList();
		ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
				sipProvider.getListeningPoint().getPort(), "udp", "branch1");
		viaHeaders.add(viaHeader);
		
		// Create ContentTypeHeader
		ContentTypeHeader contentTypeHeader = headerFactory
				.createContentTypeHeader("text", "message");
		
		// Create a new CallId header
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
				Request.MESSAGE);

		// Create a new MaxForwardsHeader
		MaxForwardsHeader maxForwards = headerFactory
				.createMaxForwardsHeader(70);
		
		// Create the request.
		Request request = messageFactory.createRequest(requestURI,
				Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);
		// Create contact headers
		String host = "127.0.0.1";

		// Create the contact name address.
		SipURI contactURI = addressFactory.createSipURI(username, host);
		contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

		Address contactAddress = addressFactory.createAddress(contactURI);

		// Add the contact address.
		contactAddress.setDisplayName(username);

		ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
		request.addHeader(contactHeader);
		
		//set content
		request.setContent(message, contentTypeHeader);

		sipProvider.sendRequest(request);//无连接发送
		System.out.println("-----------the message send is-------!!!!!!----\n"+request);
		System.out.println("----message sent");
	}
	
	/*INVITE to notify that the user is online*/
	public void sendInvite(String username,String address) throws ParseException, InvalidArgumentException, SipException{
		String toSipAddress = "127.0.0.1:5070";//the server
		String toUser = "server";
		String toDisplayName = "server";
		
		// create >From Header
		SipURI fromAddress = addressFactory.createSipURI(username,
				address);
		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(username);
		FromHeader fromHeader = headerFactory.createFromHeader(
				fromNameAddress, "12345");
		
		// create To Header
		SipURI toAddress = addressFactory
				.createSipURI(toUser, toSipAddress);
		Address toNameAddress = addressFactory.createAddress(toAddress);
		toNameAddress.setDisplayName(toDisplayName);
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
				null);
		
		// create Request URI
		SipURI requestURI = addressFactory.createSipURI(toUser,
				peerHostPort);
		
		//create viaheaders
		ArrayList viaHeaders = new ArrayList();
		ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
				sipProvider.getListeningPoint().getPort(), "udp", "branch1");
		viaHeaders.add(viaHeader);

		// Create a new CallId header
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
				Request.INVITE);

		// Create a new MaxForwardsHeader
		MaxForwardsHeader maxForwards = headerFactory
				.createMaxForwardsHeader(70);
		
		// Create the request.
		Request request = messageFactory.createRequest(requestURI,
				Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);
		// Create contact headers
		String host = "127.0.0.1";

		// Create the contact name address.
		SipURI contactURI = addressFactory.createSipURI(username, host);
		contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

		Address contactAddress = addressFactory.createAddress(contactURI);

		// Add the contact address.
		contactAddress.setDisplayName(username);

		ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
		request.addHeader(contactHeader);
		
		sipProvider.sendRequest(request);
		System.out.println("----INVITE sent------");
	}
	public void sendAction(String targetname,String targetaddress,String action){
		
		String toSipAddress = targetaddress;//the server
		String toUser = targetname;
		String toDisplayName = targetname;
		try {
		// create >From Header
		SipURI fromAddress = addressFactory.createSipURI(username,
				localAddress);
		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(username);
		FromHeader fromHeader = headerFactory.createFromHeader(
				fromNameAddress, "12345");
		
		// create To Header
		SipURI toAddress = addressFactory
				.createSipURI(toUser, toSipAddress);
		Address toNameAddress = addressFactory.createAddress(toAddress);
		toNameAddress.setDisplayName(toDisplayName);
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
				null);
		
		// create Request URI
		SipURI requestURI = addressFactory.createSipURI(toUser,
				peerHostPort);
		
		//create viaheaders
		ArrayList viaHeaders = new ArrayList();
		ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
				sipProvider.getListeningPoint().getPort(), "udp", "branch1");
		viaHeaders.add(viaHeader);

		// Create a new CallId header
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
				Request.INVITE);

		// Create a new MaxForwardsHeader
		MaxForwardsHeader maxForwards = headerFactory
				.createMaxForwardsHeader(70);
		
		// Create the request.
		Request request = messageFactory.createRequest(requestURI,
				Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);
		// Create contact headers
		String host = "127.0.0.1";

		// Create the contact name address.
		SipURI contactURI = addressFactory.createSipURI(username, host);
		contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

		Address contactAddress = addressFactory.createAddress(contactURI);

		// Add the contact address.
		contactAddress.setDisplayName(username);

		ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
		request.addHeader(contactHeader);
		// Create ContentTypeHeader
		ContentTypeHeader contentTypeHeader = headerFactory
				.createContentTypeHeader("action", action);
		request.setContent("", contentTypeHeader);// the action sends with a blank body
		sipProvider.sendRequest(request);
		
		System.out.println("----ACTION sent------");
		} catch (SipException | ParseException | InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void processRequest(RequestEvent requestEvent) {
		// TODO Auto-generated method stub
		Request request = requestEvent.getRequest();
		System.out.println("get a "+request.getMethod());
		if(request.getMethod().equals(Request.MESSAGE))//聊天信息
			presentMessage(request);
		else if(request.getMethod().equals(Request.INFO))//好友列表
			updateOnline(request);
		else if(request.getMethod().equals(Request.INVITE))//动作
			responseAction(request);
		
	}

	private void responseAction(Request request) {
		// TODO Auto-generated method stub
		//System.out.println("ssss");
		 ContentTypeHeader contentHeader= (ContentTypeHeader) request.getHeader("Content-Type");
		 FromHeader from=(FromHeader) request.getHeader("from");
		 String applyName=from.getAddress().getDisplayName();
		 if(contentHeader.getContentSubType().equals("add"))
		 {
			//show the ask information
				int choice=JOptionPane.showConfirmDialog(null, applyName+"想要添加你为好友","好友申请",JOptionPane.YES_NO_CANCEL_OPTION);
				if(choice==0)//agree
				{
					sendResponse(request,Response.OK);
					cl.c.appendInfo("你通过了"+applyName+"的好友申请，你们已经成为好友。\n");
					cl.fds.addElement(applyName);
					cl.p.getFriendsList().setModel(cl.fds);
				}
				else if(choice==1)//disagree
				{ 
					sendResponse(request,Response.NOT_ACCEPTABLE);
					cl.c.appendInfo("你拒绝了"+applyName+"的好友申请，对方将收到拒绝提示。\n");
				}
				else//cancel
					cl.c.appendInfo("你忽视了"+applyName+"的好友申请，对方收不到任何提示。\n");
		 }
		
	}
	private void presentMessage(Request request) {
		// TODO Auto-generated method stub
		
		ToHeader to=(ToHeader) request.getHeader("to");
		FromHeader from=(FromHeader) request.getHeader("from");
		System.out.println(to+"------------"+from);
		String toName=to.getAddress().toString().substring(4);
		String fromName=from.getAddress().toString().substring(4);
		System.out.println(toName+"------------"+fromName);
		//if all ,display in the chatPanel
		if(to.toString().contains("All")){
			try {
				ChatPanel.appendInfo("【"+fromName+"】 "+new String(request.getRawContent(),"UTF-8")+"\n");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//end if
		else{
			
			PrivateChatPanel.setHostName(toName);
			
			  PrivateChatPanel pc=LoginPanel.findPc(fromName);
	        if(pc==null)
	        {
	        	pc=LoginPanel.addPc(fromName);
	        	pc.setTarName(fromName);
	        	pc.setHostName(toName);
	        	pc.setTarAddress(from.getAddress().toString());
	        	 JFrame frame = new JFrame();
	      		frame.setSize(400, 340);
	      		
	      		frame.setTitle("你 "+to.getAddress().getDisplayName()+" 正在和 "+fromName+" 私聊");
	      		frame.add(pc);
	      		frame.setLocationRelativeTo(null);
	      		frame.setVisible(true);
	      		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        }
	        try {
				pc.getDisplay().append(new String(request.getRawContent(),"UTF-8")+"\n");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void updateOnline(Request request) {
		// TODO Auto-generated method stub
		try {
			String receive=new String(request.getRawContent(),"UTF-8");

			String readyToAdd[]=receive.split(";");
			
			//everytime clear all the old items and refresh
			cl.removeAllNames();
			//loop to add
			cl.c.getJcb().addItem("全体");
			for (String item : readyToAdd) {
				cl.addName(item.trim());
				System.out.println(item.trim());
			}
			
			//the last is the one who enters just now
			String info=cl.dlm.lastElement().toString()+" 登陆了.";
			cl.c.appendInfo(info+"\n");
			// set model
			cl.c.getAllList().setModel(cl.dlm);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void processResponse(ResponseEvent responseEvent) {
		// TODO Auto-generated method stub
		System.out.println("----response got-----");
		Response response=responseEvent.getResponse();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
		if(response.getStatusCode()==Response.OK){
			
			if(cseq.getMethod().equals(Request.INVITE)){
				 System.out.println("the content of response is:\n"+response);
				 ContentTypeHeader contentHeader= (ContentTypeHeader) response.getHeader("Content-Type");
				 System.out.println("the type of ok response is:"+contentHeader);
				 if(contentHeader!=null&&contentHeader.getContentSubType().equals("add")){
					 //通过了好友申请
					 System.out.println("the content of response is:\n"+response);
					 ToHeader to=(ToHeader) response.getHeader("to");
					 System.out.println(to);
					 String applyName=to.getAddress().getDisplayName();
					 cl.c.appendInfo(applyName+"通过了你的好友申请，你们现在是好友啦."+"\n");
					 cl.fds.addElement(applyName);
					 cl.p.getFriendsList().setModel(cl.fds);
				 }
				sendACK();
			}
			
		}
		
	}
	public void sendResponse(Request request,int status){
		try {
			Response response=messageFactory.createResponse(status, request);
			System.out.println("---action response---\n"+response);
			// Create ContentTypeHeader
			ContentTypeHeader contentHeader= (ContentTypeHeader) request.getHeader("Content-Type");
			response.setContent("", contentHeader);// the action sends with a blank body
			sipProvider.sendResponse(response);

		} catch (ParseException | SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void sendACK() {
		// TODO Auto-generated method stub
		try {
			
			Request ACKrequest=messageFactory.createRequest(Request.ACK);
			System.out.println(ACKrequest);
			sipProvider.sendRequest(ACKrequest);
			System.out.println(123);
			System.out.println("----the ACK sent-----");
			//it means login successfully. give a hint.
			JOptionPane.showMessageDialog(null,"连接成功!","消息提示",JOptionPane.INFORMATION_MESSAGE);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Override
	public void processTimeout(TimeoutEvent timeoutEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processIOException(IOExceptionEvent exceptionEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
		// TODO Auto-generated method stub
		
	}

}
