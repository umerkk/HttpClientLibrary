/******************************************
 * ______________COMP6461__________________
 * _Data Communication & Computer Networks_
 * 
 *			  Assignment # 1
 * 
 *____________Submitted By_________________
 *		  Muhammad Umer (40015021)
 * 	  Reza Morshed Behbahani (40039400)
 * 
 ******************************************/

package MuHttpClientLibrary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public class MuHttpClient {

	DatagramSocket sock;
	public MuMessageHeader header;
	private static int defaultPort = 80;
	URL url;
	int port;
	PrintWriter writer;
	MuMethod method;
	String postData;
	
	///ARQ Params
	long mySeqNum = 1l;
	long serverSeqNum = 0l;
	int[] sendWindow = new int[4];
	int[] recvWindow = new int[4];
	boolean isHandShake = false;
	InetAddress gPeerAddr = null;
	int gPeerPort = -1;
	
	
	public MuHttpClient(String URI, int port, MuMethod method, MuMessageHeader header) throws Exception {
		// Constructor
		try {

			this.url = new URL(URI);
			this.header = header;
			this.method = method;
			this.postData = "";

			String[] urlDump = url.getAuthority().split(":");

			if (!(url.getProtocol().equalsIgnoreCase("http") || url.getProtocol().equalsIgnoreCase("https"))) {
				throw new Exception("Protocol not supported. Must be HTTP or HTTPS.");
			}

			if (urlDump.length > 1) {
				this.port = Integer.parseInt(urlDump[1]);
			} else {
				this.port = port;
			}

		} catch (Exception e) {
			throw e;
		}
	}

	public MuHttpClient(String URI) throws Exception {
		this(URI, defaultPort, MuMethod.GET, new MuMessageHeader());
	}

	public MuHttpClient(String URI, MuMethod method) throws Exception {
		this(URI, defaultPort, method, new MuMessageHeader());
	}

	public MuHttpClient(String URI, MuMethod method, MuMessageHeader header) throws Exception {
		this(URI, defaultPort, method, header);
	}

	public MuHttpClient(String URI, MuMethod method, String data) throws Exception {
		this(URI, defaultPort, method, new MuMessageHeader());
		this.postData = data;
	}

	public MuHttpClient(String URI, MuMethod method, String data, MuMessageHeader header) throws Exception {
		this(URI, defaultPort, method, header);
		this.postData = data;
	}

	public boolean doHandShake() throws Exception {
		InetAddress address = InetAddress.getByName("localhost");
		this.sock = new DatagramSocket();
		//First SYN Frame.
		Packet p = new Packet.Builder()
                .setType(1) //1=SYN,2=ACK,0=PayLoad
                .setSequenceNumber(mySeqNum)
                .setPortNumber(this.port)
                .setPeerAddress(url.getHost())
                .setPayload(new byte[0])
                .create();
        DatagramPacket packet = new DatagramPacket(p.toBytes(), p.toBytes().length, address, 3000);
        sock.send(packet);
        
        //Response from Server
        byte[] buf0 = new byte[1024];
        packet = new DatagramPacket(buf0, buf0.length);
        sock.receive(packet);
        Packet pr = Packet.fromBytes(packet.getData());
        if(pr.getType() == 1)
        	this.serverSeqNum = pr.getSequenceNumber();
        else
        	return false;
        
        //Send Last ACK
        Packet resp = pr.toBuilder()
                .setPayload(new byte[0])
                .setType(2)
                .create();
        
        packet = new DatagramPacket(resp.toBytes(), resp.toBytes().length, address, 3000);
        sock.send(packet);
        this.gPeerAddr = resp.getPeerAddress();
        this.gPeerPort = resp.getPeerPort();
		return true;
		//Everything done.
	}
	
	public MuHttpResponse sendRequest(int reqNumber) throws Exception {

		if (reqNumber < 1) {
			return null;
		} else {
			String ksd  = url.getHost();
			InetAddress address = InetAddress.getByName("localhost");
			
			String data = "";
			//writer = new PrintWriter(sock.getOutputStream());

			data += method + " " + url.getFile() + " HTTP/1.0\r\n";
			header.addHeader("Host", url.getHost());

			if (this.method == MuMethod.POST) {
				header.addHeader("Content-Length", String.valueOf(this.postData.length()));
			}
			// header.addHeader("Content-Type", "application/json");
			// header.addHeader("Content-Length", "10");

			// writer.print("{\"Assignment\": 1}'");

			// Append all headers to the request.
			data += header.toString();
			if (this.method == MuMethod.POST) {
				data += postData;
			}
			//writer.flush();
			byte[] buf = data.getBytes();
			Packet p = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(1L)
                    .setPortNumber(this.gPeerPort)
                    .setPeerAddress(this.gPeerAddr.getHostAddress())
                    .setPayload(buf)
                    .create();
	        DatagramPacket packet = new DatagramPacket(p.toBytes(), p.toBytes().length, address, 3000);
	        this.sock.send(packet);
	        
	        buf = new byte[1024];
	        packet = new DatagramPacket(buf, buf.length);
	        this.sock.receive(packet);
	        String received = new String(packet.getData(), 0, packet.getLength());
	        String[] parts = received.split("\r\n");
			//BufferedReader bufRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String outStr;

			// Output
			MuHttpResponse response = new MuHttpResponse();
			boolean isBodyStart = false;
			int i = 0;
			//while ((outStr = bufRead.readLine()) != null) {
			for(int k=0;k<parts.length;k++) {
				if (k == 0) {
					String[] dumpRsp = parts[k].split(" ");
					response.httpVersion = dumpRsp[0];
					response.responseCode = Integer.parseInt(dumpRsp[1]);
					for (int j = 2; j < dumpRsp.length; j++)
						response.responseMessage += dumpRsp[j] + " ";

				} else if (isBodyStart) {
					response.result += parts[k] + "\r\n";
				} else {
					if (!parts[k].equalsIgnoreCase("")) {
						if (!isBodyStart) {
							response.getHeaders().parse(parts[k]);
						}
					} else {
						isBodyStart = true;
					}
				}
				i++;
			}

			if (response.responseCode >= 300 && response.responseCode <= 399) {
				// Do the redirection
				if (response.getHeaders().getHeaderValue("Location") != null && this.method == MuMethod.GET) {
					if (reqNumber - 1 > 0) {
						String newUrl = this.url.getProtocol() + "://" + this.url.getHost()
								+ response.getHeaders().getHeaderValue("Location").trim();
						MuHttpClient cl = new MuHttpClient(newUrl, this.port, this.method, new MuMessageHeader());
						response = cl.sendRequest(reqNumber - 1);
					}
				}
			}

			//bufRead.close();
			//writer.close();
			this.sock.close();

			return response;
		}
	}

}
