package Debugging;

import MuHttpClientLibrary.*;
public class Debug {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			MuHttpClient client = new MuHttpClient("http://httpbin.org/post",MuMethod.POST,"test=sdfdsf&LastName=Umer");
			client.header.addHeader("Content-Type", "application/x-www-form-urlencoded");
			MuHttpResponse resp = client.sendRequest();
			System.out.println(resp.getHttpVersion() + " " + resp.getResponseCode() + " "+resp.getResponseMessage());
			System.out.println(resp.getHeaders().toString());
			System.out.println(resp.getResult());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
