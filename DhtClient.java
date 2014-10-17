import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;



public class DhtClient{
	
	private static int sendTag;
	
	public static final int IP_INDEX = 0;
	public static final int CONFIG_INDEX = 1;
	public static final int OPERATION_INDEX = 2;
	public static final int KEY_INDEX = 3;
	public static final int VALUE_INDEX = 4;
	
	public static final int MIN_ARGS = 3;
	public static final int MIN_ARGS_PLUS_KEY = 4;
	public static final int MAX_ARGS = 5;
	
	public static final int CONFIG_IP = 0;
	public static final int CONFIG_PORT = 1;
	
	public static final int EPHEMERAL_PORT = 0;
	public static final String DEFAULT_CFG_NAME = "cfg";
	public static final int EXIT = 1;
	public static final int DEFAULT_TTL = 100;
	public static final boolean DEBUG = true;
	
	public static void main(String[] args){
		if (args.length < MIN_ARGS) {
			System.err.println("usage: DhtClient myIp cfgFile " +
					   "operation [key] [ value ] ");
			System.exit(EXIT);
		}

		//Note: Don't need else statement because it will exit in the if statement
		
		//Process Server info from second argument
		InetSocketAddress destAdr;
		DatagramSocket sock = null;
		
		BufferedReader inBuf;
		try {
			//Process IP Address from first argument
			//InetSocketAddress myIp = new InetSocketAddress(args[IP_INDEX], EPHEMERAL_PORT);
			InetAddress myIp = InetAddress.getByName(args[IP_INDEX]);

			inBuf = new BufferedReader(new InputStreamReader(new FileInputStream("cfg"),"US-ASCII"));
			String[] info = inBuf.readLine().split(" ");
			InetAddress serverAdr = InetAddress.getByName(info[CONFIG_IP]);
			int serverPort = Integer.parseInt(info[CONFIG_PORT]);
			System.out.println("server port: " + serverPort);
			destAdr = new InetSocketAddress(serverAdr, serverPort);
			
			//Process Packet info from third, fourth, fifth arguments, and send them along a socket
			Packet outPkt = new Packet();
			outPkt.type = args[OPERATION_INDEX];
			if(args.length >= MIN_ARGS_PLUS_KEY){
				outPkt.key = args[KEY_INDEX];
			}
			if(args.length >= MAX_ARGS){
				outPkt.val = args[VALUE_INDEX];
			}
			outPkt.tag = ++sendTag;
			sock = new DatagramSocket(0, myIp);
			outPkt.send(sock, destAdr, DEBUG);
			
			//Receive packet from server //FIXME No need to use check() method here, right?
			Packet inPkt = new Packet();
			InetSocketAddress srcAdr = inPkt.receive(sock, DEBUG);
			String reply = inPkt.toString();
		} catch (NumberFormatException e) {
			System.err.println("Config file contains invalid port");
			e.printStackTrace();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			System.err.println("Config file not found");
			e.printStackTrace();
		} catch (UnknownHostException e){
			System.err.println("Invalid or unknown host");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Config file is misformatted");
			e.printStackTrace();
		}
			
			
			
		
	}
}