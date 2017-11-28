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
package HttpCLI;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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
		boolean isOutputToFile = false;
		String errorMessage = "";
		String outputFileName = "output.txt";
		final String CRLF = "\r\n";

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

			if (args[k].equalsIgnoreCase("-o")) {
				isOutputToFile = true;
				if (k + 1 < args.length) {
					outputFileName = args[k + 1];
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
				
				ArrayList<MuHttpResponse> response = null;
				if(client.doHandShake()) {
					response = client.sendRequest(6);
				} else {
					System.out.println("The handshake was unsuccessful. The request could not be sent.");
					return;
				}
				
				for(int k=0;k<response.size();k++) {
					if (isOutputToFile) {
						String output = "";
						if (isVerbose) {
							output += response.get(k).getHttpVersion() + " " + response.get(k).getResponseCode() + " "
									+ response.get(k).getResponseMessage() + CRLF;
							output += response.get(k).getHeaders().toString() + CRLF;
						}
						output += response.get(k).getResult();
						writeFile(outputFileName, output);
					} else {
						if (isVerbose) {
							System.out.println(response.get(k).getHttpVersion() + " " + response.get(k).getResponseCode() + " "
									+ response.get(k).getResponseMessage());
							System.out.println(response.get(k).getHeaders().toString());
						}
						System.out.println(response.get(k).getResult());
					}
				}
				

			} catch (Exception e) {
				System.out.println(
						"The following errors occured while executing your request:\r\n \r\n " + e.getMessage());
				e.printStackTrace();
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

	private static void writeFile(String filename, String content) {
		File file = new File(filename);
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
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