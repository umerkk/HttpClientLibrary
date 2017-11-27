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

import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class MuHttpClient {

	DatagramSocket sock;
	public MuMessageHeader header;
	private static int defaultPort = 80;
	URL url;
	int port;
	PrintWriter writer;
	MuMethod method;
	String postData;
	byte[][] dataChunks;
	String data = "";
	int currDataIndex = 0;
	int recvDataIndex = 0;

	/// ARQ Params
	long mySeqNum = 0l;
	long serverSeqNum = 0l;
	MyPacketWithTimer[] sendWindow = new MyPacketWithTimer[4];
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
		preProcessData();
	}

	private void preProcessData() throws Exception {
		this.data += method + " " + url.getFile() + " HTTP/1.0\r\n";
		this.header.addHeader("Host", url.getHost());

		if (this.method == MuMethod.POST) {
			header.addHeader("Content-Length", String.valueOf(this.postData.length()));
		}

		// Append all headers to the request.
		data += header.toString();
		if (this.method == MuMethod.POST) {
			//data += postData;
		}
		int payloadSize = 1013 - data.getBytes().length;
		this.dataChunks = splitBytes(postData.getBytes(), payloadSize);
		this.recvDataIndex = dataChunks.length;
		this.data = "";
	}

	public boolean doHandShake() throws Exception {
		InetAddress address = InetAddress.getByName("localhost");
		this.sock = new DatagramSocket();
		// First SYN Frame.
		Packet p = new Packet.Builder().setType(1) // 1=SYN,2=ACK,0=PayLoad
				.setSequenceNumber(mySeqNum).setPortNumber(this.port).setPeerAddress(url.getHost())
				.setPayload(new byte[0]).create();
		DatagramPacket packet = new DatagramPacket(p.toBytes(), p.toBytes().length, address, 3000);
		sock.send(packet);

		// Response from Server
		byte[] buf0 = new byte[1024];
		packet = new DatagramPacket(buf0, buf0.length);
		sock.receive(packet);
		Packet pr = Packet.fromBytes(packet.getData());
		if (pr.getType() == 1)
			this.serverSeqNum = pr.getSequenceNumber();
		else
			return false;

		// Send Last ACK
		Packet resp = pr.toBuilder().setPayload(new byte[0]).setType(2).create();

		packet = new DatagramPacket(resp.toBytes(), resp.toBytes().length, address, 3000);
		sock.send(packet);
		this.gPeerAddr = resp.getPeerAddress();
		this.gPeerPort = resp.getPeerPort();
		return true;
		// Everything done.
	}

	private long getNextSeqNumber() {
		if (this.mySeqNum >= 7l) {
			this.mySeqNum = 0l;
		} else {
			this.mySeqNum++;
		}
		return this.mySeqNum;
	}

	public byte[][] splitBytes(final byte[] data, final int chunkSize) {
		final int length = data.length;
		final byte[][] dest = new byte[(length + chunkSize - 1) / chunkSize][];
		int destIndex = 0;
		int stopIndex = 0;

		for (int startIndex = 0; startIndex + chunkSize <= length; startIndex += chunkSize) {
			stopIndex += chunkSize;
			dest[destIndex++] = Arrays.copyOfRange(data, startIndex, stopIndex);
		}

		if (stopIndex < length)
			dest[destIndex] = Arrays.copyOfRange(data, stopIndex, length);

		return dest;
	}

	private boolean isWindowAvailable() {
		if (sendWindow[0] == null || sendWindow[1] == null || sendWindow[2] == null || sendWindow[3] == null) {
			return true;
		} else
			return false;
	}

	private void addPacketToWindow(MyPacketWithTimer p) {
		for (int k = 0; k < sendWindow.length; k++) {
			if (sendWindow[k] == null) {
				sendWindow[k] = p;
				break;
			}
		}
	}
	
	private boolean removePacketFromWindow(Packet p) {
		boolean resp = false;
		for (int k = 0; k < sendWindow.length; k++) {
			if(sendWindow[k] != null) {
				long pSeq = sendWindow[k].myPacket.getSequenceNumber();
				long mSeq = p.getSequenceNumber();
				if (sendWindow[k].myPacket.getSequenceNumber() == p.getSequenceNumber()) {
					sendWindow[k].makrDelivered();
					sendWindow[k] = null;
					resp = true;
					break;
				}
			}
		}
		return resp;
	}
	
	
	private MuHttpResponse recieveRequest() throws Exception {
		// *****************************************************
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		
		this.sock.receive(packet);
		
		Packet p = Packet.fromBytes(packet.getData());
		if(removePacketFromWindow(p)) {
			String received = new String(p.getPayload(), 0, p.getPayload().length);
			this.recvDataIndex--;
			
			String[] parts = received.split("\r\n");
			// BufferedReader bufRead = new BufferedReader(new
			// InputStreamReader(sock.getInputStream()));

			// Output
			MuHttpResponse response = new MuHttpResponse();
			boolean isBodyStart = false;
			// while ((outStr = bufRead.readLine()) != null) {
			for (int k = 0; k < parts.length; k++) {
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
			}

			/*if (response.responseCode >= 300 && response.responseCode <= 399) {
				// Do the redirection
				if (response.getHeaders().getHeaderValue("Location") != null
						&& this.method == MuMethod.GET) {
					if (reqNumber - 1 > 0) {
						String newUrl = this.url.getProtocol() + "://" + this.url.getHost()
								+ response.getHeaders().getHeaderValue("Location").trim();
						MuHttpClient cl = new MuHttpClient(newUrl, this.port, this.method,
								new MuMessageHeader());
						response = cl.sendRequest(reqNumber - 1);
					}
				}
			}*/

			// bufRead.close();
			// writer.close();
			//this.sock.close();

			return response;
		} else {
			return null; //Discard Packet
		}
	}

	public ArrayList<MuHttpResponse> sendRequest(int reqNumber) throws Exception {
		ArrayList<MuHttpResponse> resp = new ArrayList<MuHttpResponse>();
		
		while (true) {
			if(recvDataIndex <= 0) {
				break;
			}
			if (currDataIndex >= dataChunks.length) {
				MuHttpResponse re = recieveRequest();
				if(re != null) {
					resp.add(re);
				}
			} else {
				if (isWindowAvailable()) {

					if (reqNumber < 1) {
						return null;
					} else {

						//InetAddress address = InetAddress.getByName("localhost");
						String data = "";

						data += method + " " + url.getFile() + " HTTP/1.0\r\n";

						/*
						 * header.addHeader("Host", url.getHost());
						 * 
						 * if (this.method == MuMethod.POST) {
						 * header.addHeader("Content-Length",
						 * String.valueOf(this.postData.length())); }
						 */

						// Append all headers to the request.
						data += header.toString();
						if (this.method == MuMethod.POST) {
							data += new String(dataChunks[currDataIndex]);
							currDataIndex++;
						}
						// writer.flush();
						byte[] buf = data.getBytes();

						Packet p = new Packet.Builder().setType(0).setSequenceNumber(getNextSeqNumber())
								.setPortNumber(this.gPeerPort).setPeerAddress(this.gPeerAddr.getHostAddress())
								.setPayload(buf).create();
						MyPacketWithTimer pkt = new MyPacketWithTimer(this.sock, p);
						pkt.transmitPacket(); // SEND the PACKET
						addPacketToWindow(pkt);

						// DatagramPacket packet = new
						// DatagramPacket(p.toBytes(), p.toBytes().length,
						// address, 3000);
						// this.sock.send(packet);
						
					}
				} else { // END ISWINDOWAVAILABLE
					MuHttpResponse re = recieveRequest();
					if(re != null)
						resp.add(re);
				}
			}
		}
		return resp;
	}
	

}
