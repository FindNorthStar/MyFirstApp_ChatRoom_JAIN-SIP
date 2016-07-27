package chatRoom;



import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.sql.*;
import java.io.*;

/**
 * This class is a UAS template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 * 
 * @author M. Ranganathan
 */

public class Server implements SipListener {

	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;

	private static SipStack sipStack;

	private static final String myAddress = "192.168.137.89";

	private static final int myPort = 5070;

	private SipProvider sipProvider;
	
	private List<UserStatus> addrs= new ArrayList();
	
	private Map<SipURI,String> addrsMap  =new HashMap<SipURI,String>();
	
	private Connection connection;
	
	/**
     * this is to find the friends with the user
     * @param nameToGetFriend
     * @return
     */
    private synchronized List<String> getFriend(String nameToGetFriend){
        List<String> friendName = new ArrayList<String>();
        try {
            Statement statement = connection.createStatement();
            String sql = "select friendname from friend WHERE username = '" + nameToGetFriend +"'";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()){
                if (!friendName.contains(rs.getString("friendname"))) {
                    friendName.add(rs.getString("friendname"));
                }
            }
            sql = "select username from friend WHERE friendname = '" + nameToGetFriend +"'";
            rs = statement.executeQuery(sql);
            while (rs.next()){
                if (!friendName.contains(rs.getString("username"))) {
                    friendName.add(rs.getString("username"));
                }
            }
            statement.close();
            return friendName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * test if the two users are friends
     * @param userName1
     * @param userName2
     * @return
     */
    private synchronized boolean isFriend(String userName1, String userName2){
        try {
            Statement statement = connection.createStatement();
            String sql = "select friendname from friend WHERE username = '" + userName1 +"'";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()){
                if (userName2.equals(rs.getString("friendname"))){
                    return true;
                }
            }
            sql = "select username from friend WHERE friendname = '" + userName1 +"'";
            rs = statement.executeQuery(sql);
            while (rs.next()){
                if (userName2.equals(rs.getString("username"))){
                    return true;
                }
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * make two users become friends
     * @param userName1
     * @param userName2
     */
    private synchronized void createFriend(String userName1, String userName2){
        if (this.isFriend(userName1,userName2)){
            return;
        }
        else{
            try {
                Statement statement = connection.createStatement();
                String sql = "insert into friend values('" + userName1 + "','" + userName2 + "')";
                statement.executeUpdate(sql);
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    private void deleteFriend(String sourceName, String targetName) {
        try {
            Statement statement = connection.createStatement();
            String sql = "delete from friend WHERE username = '" + sourceName +"' and friendname = '" + targetName + "'";
            statement.executeUpdate(sql);
            sql = "delete from friend WHERE username = '" + targetName +"' and friendname = '" + sourceName + "'";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
	public void init() {
		SipFactory sipFactory=SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties=new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "chatServer");
		properties.setProperty("javax.sip.IP_ADDRESS", myAddress);
		
		//DEBUGGING: Information will go to files 
		//textclient.log and textclientdebug.log
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
			"textserver.txt");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
			"textclientserver.log");
		try {
			sipStack = sipFactory.createSipStack(properties);
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			
			ListeningPoint tcp = sipStack.createListeningPoint(myPort, "tcp");
			ListeningPoint udp = sipStack.createListeningPoint(myPort, "udp");
			
			sipProvider = sipStack.createSipProvider(tcp);
			sipProvider.addSipListener(this);
			sipProvider = sipStack.createSipProvider(udp);
			sipProvider.addSipListener(this);
			
			Class.forName("com.mysql.jdbc.Driver");
            //connection = DriverManager.getConnection("jdbc:mysql://10.125.103.139:3306/chat","qwe","123");
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3307/chat","root","112233");
		} catch (PeerUnavailableException | TransportNotSupportedException | InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ObjectInUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TooManyListenersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
		
	}

	public static void main(String args[]) {
		new Server().init();
	}

	@Override
	public void processRequest(RequestEvent requestEvent) {
		// TODO Auto-generated method stub
		Request request = requestEvent.getRequest();
		ToHeader to=(ToHeader) request.getHeader("to");
		System.out.println("*****the request is* " + request+"*********");
		if(request.getMethod().equals(Request.INVITE))
			processInvite(request);
		else if(request.getMethod().equals(Request.ACK))
			processACK(request);
		else if(request.getMethod().equals(Request.BYE))
			processBYE(request);
		else if(request.getMethod().equals(Request.MESSAGE)){
			try {
				ContentTypeHeader contentHeader= (ContentTypeHeader) request.getHeader("Content-Type");
				String temp=new String(request.getRawContent(),"UTF-8");
				if(temp.equals("logout"))
				{
					processBYE(request);
				}
				else if(contentHeader!=null&&contentHeader.getContentSubType().equals("online"))
				 {
					FromHeader from=(FromHeader) request.getHeader("from");
					String fromName=from.getAddress().toString();
					for(SipURI a:addrsMap.keySet()){
						try {
							sendRequest("online"+fromName,a,Request.OPTIONS);
						} catch (ParseException | InvalidArgumentException | SipException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				 }
				else if(contentHeader!=null&&contentHeader.getContentSubType().equals("busy"))
				 {
					FromHeader from=(FromHeader) request.getHeader("from");
					String fromName=from.getAddress().toString();
					for(SipURI a:addrsMap.keySet()){
						try {
							sendRequest("busy"+fromName,a,Request.OPTIONS);
						} catch (ParseException | InvalidArgumentException | SipException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				 }
				else if(contentHeader!=null&&contentHeader.getContentSubType().equals("afk"))
				 {
					FromHeader from=(FromHeader) request.getHeader("from");
					String fromName=from.getAddress().toString();
					for(SipURI a:addrsMap.keySet()){
						try {
							sendRequest("afk"+fromName,a,Request.OPTIONS);
						} catch (ParseException | InvalidArgumentException | SipException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				 }
				else if(contentHeader!=null&&contentHeader.getContentSubType().equals("add"))
				 {
					 //是动作直接转发
					 to=(ToHeader) request.getHeader("to");
					 System.out.println("the action redirect location is:"+(SipURI) to.getAddress().getURI());
					 sendMessage(request,(SipURI) to.getAddress().getURI());
					 System.out.println("the action redirected.");
				 }
				 else if(contentHeader!=null&&contentHeader.getContentSubType().equals("approve"))
				 {
					 to=(ToHeader) request.getHeader("to");
					 FromHeader from=(FromHeader) request.getHeader("from");
					 String fromName=from.getAddress().toString();
					 String toName=to.getAddress().toString();
					 //创建完朋友关系 通知对方。
					 createFriend(fromName,toName);
					 //把request（approve）转发给提出申请的那一方
					 sendMessage(request,(SipURI) to.getAddress().getURI());
					 
					 //更新好友列表
					 
				 }
				 else if(contentHeader!=null&&contentHeader.getContentSubType().equals("refuse"))
				 {
					 //把拒绝信息转发
					 to=(ToHeader) request.getHeader("to");
					 sendMessage(request,(SipURI) to.getAddress().getURI());
				 }
			//if all ,send to all
			else if(to.toString().contains("All")){
				for(SipURI a:addrsMap.keySet()){
					sendMessage(request,a);
				}
			}
			else{
				 to=(ToHeader) request.getHeader("to");
				 System.out.println("the private chat redirect location is:"+(SipURI) to.getAddress().getURI());
				 sendMessage(request,(SipURI) to.getAddress().getURI());
			}
/*			System.out.println(to);
			SipURI addr=(SipURI) to;
			System.out.println(addr.getUser());
			//extract the address
			//ContactHeader contact=(ContactHeader) request.getHeader("contact");
			//sendMessage(request,(SipURI) contact.getAddress().getURI());
*/		}
		 catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
	}
	private void processBYE(Request request) {
		// TODO Auto-generated method stub
		FromHeader from=(FromHeader) request.getHeader("from");
		addrs.remove((SipURI) from.getAddress().getURI());
		System.out.println("the host leaves:"+(SipURI) from.getAddress().getURI());
		/*update the online members*/
		updateOnLine(request);
		
	}

	private void processACK(Request request) {
		// TODO Auto-generated method stub
		System.out.println("----three times handshake completed.");
	}

	/*INVITE means a user log in,it must save*/
	 private void processInvite(Request request) {
		// TODO Auto-generated method stub
		 ContentTypeHeader contentHeader= (ContentTypeHeader) request.getHeader("Content-Type");
		 System.out.println(contentHeader);
		 if(contentHeader!=null&&contentHeader.getContentSubType().equals("list")){
			 //索要好友列表：回复一个好友列表
			//extract the address
			ContactHeader contact=(ContactHeader) request.getHeader("contact");
			SipURI target=(SipURI) contact.getAddress().getURI();
			String content="";
			/*for(SipURI a:addrs){
				content+=a.toString()+";";
			}*/
			for (Entry<SipURI, String> entry : addrsMap.entrySet()) {
				content+=entry.getKey().toString()+";";
			}
			try {
				sendRequest(content,target,Request.INFO);
			} catch (ParseException | InvalidArgumentException | SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		 }
		 else{
		//extract the address
		ContactHeader contact=(ContactHeader) request.getHeader("contact");
		System.out.println("contact:"+contact);
		//store it in the vector
		SipURI sipURI=(SipURI) contact.getAddress().getURI();
		addrsMap.put(sipURI, "online");
		/*if(!addrs.contains(sipURI))
			addrs.add(sipURI);*/
		System.out.println("map key set:"+addrsMap.keySet());
		
		/*update the online members*/
		updateOnLine(request);
		System.out.println("----INVITE got: comes from  "+contact.getAddress());
		//send response
			Response response;
			try {
				response = messageFactory.createResponse(Response.OK,
						request);
				sipProvider.sendResponse(response);
			} catch (ParseException | SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }//end if-else
	}

	 /*redirect the message to uac*/
	public void sendMessage(Request request,SipURI sipURI){
		
		
		request.setRequestURI(sipURI);
		try {
			sipProvider.sendRequest(request);
			System.out.println(request+"--redirect");
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	 }
	@Override
	public void processResponse(ResponseEvent responseEvent) {
		// TODO Auto-generated method stub
		
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
	public void updateOnLine(Request request){
		/*update the online members*/
		String content="";
		for(SipURI a:addrsMap.keySet()){
			content+=a.toString()+";";
		}
		System.out.println(content);
		for(SipURI a:addrsMap.keySet()){
			try {
				sendRequest(content,a,Request.INFO);
			} catch (ParseException | InvalidArgumentException | SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}//end update
	
	void sendRequest(String message,SipURI sipURI,String method) throws ParseException, InvalidArgumentException, SipException{
		
		// create >From Header
		SipURI fromAddress = addressFactory.createSipURI("server",
				myAddress+":"+myPort);

		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName("server");
		FromHeader fromHeader = headerFactory.createFromHeader(
				fromNameAddress, "12345");
		// create To Header
		Address toNameAddress = addressFactory.createAddress(sipURI);
		toNameAddress.setDisplayName("idontcare");
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
				null);
		
		String s=sipURI.toString();
		String requestaddr=s.substring(s.indexOf("@")+1);
		// create Request URI

		SipURI requestURI = addressFactory.createSipURI("idontcare",
				requestaddr);
		
		//create viaheaders
		ArrayList viaHeaders = new ArrayList();
		ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
				sipProvider.getListeningPoint().getPort(), "udp", "branch1");
		viaHeaders.add(viaHeader);
		
		// Create ContentTypeHeader
		ContentTypeHeader contentTypeHeader = headerFactory
				.createContentTypeHeader("text", "update");
		
		// Create a new CallId header
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
				method);

		// Create a new MaxForwardsHeader
		MaxForwardsHeader maxForwards = headerFactory
				.createMaxForwardsHeader(70);
		
		// Create the request.
		Request request = messageFactory.createRequest(requestURI,
				method, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);
		// Create contact headers
		String host = "127.0.0.1";

		//set content
		request.setContent(message, contentTypeHeader);
		
		sipProvider.sendRequest(request);
	}//end create request
	
}//end whole class