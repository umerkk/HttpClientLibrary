package HttpCLI;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import MuHttpClientLibrary.*;

public class httpc {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MuHttpClient client = null;
		MuMessageHeader header = new MuMessageHeader();
		MuMethod httpMethod = null;
		String url = "";
		String postData = "";
		boolean isVerbose = false;
		boolean isHeadersProvided = false;
		boolean isDataProvided = false;
		boolean isInputError = false;
		String errorMessage = "";

		try {
			url = args[args.length - 1];
		} catch (Exception e) {
			errorMessage += "URL is invalid. URL must start with http:// or https:// \r\n";
			isInputError = true;
		}

		for (int k = 0; k < args.length; k++) {
			if (args[k].equalsIgnoreCase("get") || args[k].equalsIgnoreCase("post")) {
				try {
					httpMethod = MuMethod.valueOf(args[k].toUpperCase());
				} catch (Exception e) {
					errorMessage += "Request method is unknown Request method can be GET or POST. \r\n";
					isInputError = true;
				}
			}

			if (args[k].equalsIgnoreCase("-v")) {
				isVerbose = true;
			}

			if (args[k].equalsIgnoreCase("-h")) {
				// Header is followed on next iteration.
				String[] headerDump = args[k + 1].split(":");
				if (headerDump.length < 2) {
					errorMessage += "--h must be followed by a header value in quotes. Example \"headerName : headerValue\". \r\n";
					isInputError = true;
				}

				try {
					header.addHeader(headerDump[0], headerDump[1]);
				} catch (Exception e) {
					errorMessage += e.getMessage() + " \r\n";
					isInputError = true;
				}
				k++;

			}

			if (args[k].equalsIgnoreCase("--d")) {
				if (isDataProvided) {
					isInputError = true;
					errorMessage += "Input data is already provieded, you cannot use --d and --f at the same time or multiple --d and --f switches.\r\n";
					isInputError = true;
				} else {
					postData = args[k + 1];
					isDataProvided = true;
					k++; // Skip the next one.
				}
			}

			if (args[k].equalsIgnoreCase("--f")) {
				if (isDataProvided) {
					isInputError = true;
					errorMessage += "Input data is already provieded, you cannot use --d and --f at the same time or multiple --d and --f switches.\r\n";
					isInputError = true;
				} else {
					postData = readFile(args[k + 1]);
					isDataProvided = true;
					k++; // Skip the next one.
				}
			}
		}

		if (httpMethod == null) {
			errorMessage += "Request method is unknown Request method can be GET or POST. \r\n";
			isInputError = true;
		}
		if (isInputError) {
			System.out.println("The following errors occured while executing your request:\r\n \r\n " + errorMessage);
		} else {
			try {
				if (isDataProvided) {
					client = new MuHttpClient(url, httpMethod, postData, header);
				} else {
					client = new MuHttpClient(url, httpMethod, header);
				}
				
				MuHttpResponse response = client.sendRequest();
				if(isVerbose) {
					System.out.println(response.getHttpVersion() + " " + response.getResponseCode() + " "+response.getResponseMessage());
					System.out.println(response.getHeaders().toString());
				}
				System.out.println(response.getResult());
			} catch (Exception e) {
				System.out.println(
						"The following errors occured while executing your request:\r\n \r\n " + e.getMessage());
			}
		}

	}

	private static String readFile(String filename) {
		String content = null;
		File file = new File(filename);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return content;
	}

}

// httpc GET 'http://www.yahoo.com/?id=34'
// httpc GET -v 'http://www.yahoo.com/?id=34'
// httpc GET -v -h 'Content-type: text/html' 'http://www.yahoo.com/?id=34'
// httpc GET -v -h 'Content-type: text/html' -h 'User-Agent: Chrome/11.0'
// 'http://www.yahoo.com/?id=34'
// httpc POST -v -h 'Content-type: text/html' -h 'User-Agent: Chrome/11.0' --d
// 'Username=Umer&Password=Umer' 'http://www.yahoo.com/?id=34'
// httpc POST -v -h 'Content-type: text/html' -h 'User-Agent: Chrome/11.0' --f
// 'c:/myinfo.txt' 'http://www.yahoo.com/?id=34'