package MuHttpClientLibrary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

public class MuHttpClient {

	Socket sock;
	public MuMessageHeader header;
	private static int defaultPort = 80;
	URL url;
	int port;
	PrintWriter writer;

	public MuHttpClient(String URI, int port, MuMethod method) throws Exception {
		// Constructor
		try {

			url = new URL(URI);
			header = new MuMessageHeader();

			String[] urlDump = url.getAuthority().split(":");

			if (!(url.getProtocol().equals("http") || url.getProtocol().equals("https"))) {
				throw new Exception("Protocol not supported. Must be HTTP or HTTPS.");
			}

			if (urlDump.length > 1) {
				port = Integer.parseInt(urlDump[1]);
			} else {
				this.port = port;
			}

		} catch (Exception e) {
			throw e;
		}
	}

	public MuHttpClient(String URI) throws Exception {
		this(URI, defaultPort, MuMethod.GET);
	}

	public void sendRequest() throws Exception {

		sock = new Socket(url.getHost(), port);
		writer = new PrintWriter(sock.getOutputStream());

		writer.println("GET /status/418 HTTP/1.1");

		header.addHeader("Host", url.getHost());
		
		//Append all headers to the request.
		writer.print(header.toString());

		writer.flush();

		BufferedReader bufRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		String outStr;

		//Output
		while ((outStr = bufRead.readLine()) != null) {
			System.out.println(outStr);
		}

		bufRead.close();
		writer.close();
		sock.close();
	}

}
