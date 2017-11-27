package TestCases;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import MuHttpClientLibrary.MuHttpClient;
import MuHttpClientLibrary.MuHttpResponse;
import MuHttpClientLibrary.MuMessageHeader;
import MuHttpClientLibrary.MuMethod;

public class ServerConcurrent {

	MuHttpClient c1;
	MuHttpClient c2;

	@Before
	public void setUp() throws Exception {
		c1 = new MuHttpClient("http://localhost:8080/", MuMethod.GET, new MuMessageHeader());
		c2 = new MuHttpClient("http://localhost:8080/t1.txt", MuMethod.GET, new MuMessageHeader());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					MuHttpClient c1 = new MuHttpClient("http://localhost:8080/", MuMethod.GET, new MuMessageHeader());
					MuHttpResponse response = c1.sendRequest(6);

					System.out.println(response.getHttpVersion() + " " + response.getResponseCode() + " "
							+ response.getResponseMessage());
					System.out.println(response.getHeaders().toString());

					System.out.println(response.getResult());

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.start();
		
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				try {
					//MuHttpClient c1 = new MuHttpClient("http://localhost:8080/t1.txt", MuMethod.GET, new MuMessageHeader());
					MuMessageHeader header = new MuMessageHeader();
					header.addHeader("Overwrite", "false");
					MuHttpClient c1 = new MuHttpClient("http://localhost:8080/t1.txt", MuMethod.POST, "Yada Yada", header);
					MuHttpResponse response = c1.sendRequest(6);

					System.out.println(response.getHttpVersion() + " " + response.getResponseCode() + " "
							+ response.getResponseMessage());
					System.out.println(response.getHeaders().toString());

					System.out.println(response.getResult());

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t2.start();
		
		Thread t3 = new Thread(new Runnable() {
			public void run() {
				try {
					MuMessageHeader header = new MuMessageHeader();
					header.addHeader("Overwrite", "false");
					MuHttpClient c1 = new MuHttpClient("http://localhost:8080/t1.txt", MuMethod.POST, "Hayaaaaaaa",header);
					MuHttpResponse response = c1.sendRequest(6);

					System.out.println(response.getHttpVersion() + " " + response.getResponseCode() + " "
							+ response.getResponseMessage());
					System.out.println(response.getHeaders().toString());

					System.out.println(response.getResult());

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t3.start();
		try

		{
			t.join();
			t2.join();
			t3.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
