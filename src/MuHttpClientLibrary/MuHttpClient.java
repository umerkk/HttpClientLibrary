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

	public MuHttpResponse sendRequest(int reqNumber) throws Exception {

		if (reqNumber < 1) {
			return null;
		} else {
			InetAddress address = InetAddress.getByName(url.getHost());
			
			sock = new DatagramSocket();
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
	        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, this.port);
	        sock.send(packet);
	        
	        buf = new byte[2048];
	        packet = new DatagramPacket(buf, buf.length);
	        sock.receive(packet);
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
			sock.close();

			return response;
		}
	}

}
