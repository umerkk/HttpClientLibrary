package Debugging;

import MuHttpClientLibrary.*;
public class Debug {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			MuHttpClient client = new MuHttpClient("http://www.httpbin.org");
			client.sendRequest();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
