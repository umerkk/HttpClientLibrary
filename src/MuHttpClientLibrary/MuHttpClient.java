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

	public MuHttpResponse sendRequest() throws Exception {

		sock = new Socket(url.getHost(), this.port);
		writer = new PrintWriter(sock.getOutputStream());

		writer.println(method + " " + url.getFile() + " HTTP/1.0");
		header.addHeader("Host", url.getHost());

		if(this.method == MuMethod.POST) {
			header.addHeader("Content-Length", String.valueOf(this.postData.length()));
		}
		// header.addHeader("Content-Type", "application/json");
		// header.addHeader("Content-Length", "10");

		// writer.print("{\"Assignment\": 1}'");

		// Append all headers to the request.
		writer.print(header.toString());
		if(this.method == MuMethod.POST) {
			writer.print(postData);
		}
		writer.flush();

		BufferedReader bufRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		String outStr;

		// Output
		MuHttpResponse response = new MuHttpResponse();
		boolean isBodyStart = false;
		int i = 0;
		while ((outStr = bufRead.readLine()) != null) {
			if (i == 0) {
				String[] dumpRsp = outStr.split(" ");
				response.httpVersion = dumpRsp[0];
				response.responseCode = Integer.parseInt(dumpRsp[1]);
				for (int k = 2; k < dumpRsp.length; k++)
					response.responseMessage += dumpRsp[k] + " ";

			} else if (isBodyStart) {
				response.result += outStr + "\r\n";
			} else {
				if (!outStr.equalsIgnoreCase("")) {
					if (!isBodyStart) {
						response.getHeaders().parse(outStr);
					}
				} else {
					isBodyStart = true;
				}
			}
			i++;
		}

		bufRead.close();
		writer.close();
		sock.close();

		return response;
	}

}
