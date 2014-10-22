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


/**
* Client sends requests to DHT servers in the form of inserting, updating
* or deleting (via a "put" request) or retrieving (via a "get" request) key-
* value pairs on the server's hashmap.
* 
* usage: DhtClient myIp cfgFile operation [ key ] [ value ]
* 
* Takes 3-5 arguments:
* 
* myIp: the IP address associated with this client
* 
* cfgFile: a file containing the IP address and port number of the server to
* which this client plans to make a request
* 
* operation: either a "put" if inserting/updating/deleting information, or a 
* "get" if retrieving information.
*
* key: the key of the map entry
* 
* value: the value of the map entry
*/
public class DhtClient{
	
	private static int sendTag = 12344;
	
	//Indices for the run arguments
	public static final int IP_INDEX = 0;
	public static final int CONFIG_INDEX = 1;
	public static final int OPERATION_INDEX = 2;
	public static final int KEY_INDEX = 3;
	public static final int VALUE_INDEX = 4;
	
	//Valid numbers of run arguments
	public static final int MIN_ARGS = 3;
	public static final int MIN_ARGS_PLUS_KEY = 4;
	public static final int MAX_ARGS = 5;
	
	//The indices of the information read from the cfg file, 
	//once delimited into a string array
	public static final int CONFIG_IP = 0;
	public static final int CONFIG_PORT = 1;
	
	public static final int EPHEMERAL_PORT = 0;
	public static final String DEFAULT_CFG_NAME = "cfg";
	public static final int EXIT = 1;
	public static final int DEFAULT_TTL = 100;
	public static final boolean DEBUG = true;
	
	public static void main(String[] args){
		
		// process command-line arguments
		if (args.length < MIN_ARGS) { 
			System.err.println("usage: DhtClient myIp cfgFile " +
					   "operation [key] [ value ] ");
			System.exit(EXIT);
		}


		String op = args[OPERATION_INDEX];
		String key = null;
		String val = null;
		if (args.length > KEY_INDEX) {
			key = args[KEY_INDEX];
		}
		if (args.length > VALUE_INDEX) {
			val = args[VALUE_INDEX];
		}
		
		// open socket for sending/receiving packets
		// read ip and port from config file
		InetSocketAddress destAdr = null;
		DatagramSocket sock = null;		
		BufferedReader inBuf;
		
		try {
			InetAddress myIp = InetAddress.getByName(args[IP_INDEX]);
			sock = new DatagramSocket(0, myIp);

			inBuf = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[CONFIG_INDEX]),"US-ASCII"));
			
			String[] info = inBuf.readLine().split(" ");	
			InetAddress serverAdr = InetAddress.getByName(info[CONFIG_IP]);		
			int serverPort = Integer.parseInt(info[CONFIG_PORT]);	
			destAdr = new InetSocketAddress(serverAdr, serverPort);

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
				
		//create packet with request to send to server
		//Debug option is always on for sending and receiving
		Packet outPkt = new Packet();
		outPkt.type = op;
		outPkt.key = key;
		outPkt.val = val;
		outPkt.tag = ++sendTag;
		outPkt.send(sock, destAdr, DEBUG); 
			
		//create packet to receive response from server
		Packet inPkt = new Packet();
		inPkt.receive(sock,DEBUG);
		
	}
}

