package chatRoom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * extract the socket from the old TestPanel
 * 
 * @author lenovo
 *
 */
public class MySocket {

	public static Socket getSocket() {
		return socket;
	}
	public static void setSocket(Socket socket) {
		MySocket.socket = socket;
	}
	public static DataOutputStream getToServer() {
		return toServer;
	}
	public static void setToServer(DataOutputStream toServer) {
		MySocket.toServer = toServer;
	}
	public static DataInputStream getFromServer() {
		return fromServer;
	}
	public static void setFromServer(DataInputStream fromServer) {
		MySocket.fromServer = fromServer;
	}
	private static Socket socket;
	private static DataOutputStream toServer;
	private static DataInputStream fromServer;
}
