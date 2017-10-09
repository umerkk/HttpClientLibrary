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

public class MuHttpResponse {

	protected String httpVersion = "";
	protected int responseCode = -1;
	protected String responseMessage = "";
	protected String result = "";
	protected MuMessageHeader headers;

	public String getHttpVersion() {
		return httpVersion;
	}

	protected MuHttpResponse() {
		//Constructor
		headers = new MuMessageHeader();
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public String getResult() {
		return result;
	}

	public MuMessageHeader getHeaders() {
		return headers;
	}
	

}
