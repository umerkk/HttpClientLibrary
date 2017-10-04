package MuHttpClientLibrary;

import java.util.HashMap;
import java.util.Map;

public class MuMessageHeader {

	private final static String CRLF = "\r\n";
	HashMap<String, String> map;

	public MuMessageHeader() {
		map = new HashMap<String, String>();
	}

	public void addHeader(String fieldName, String value) throws Exception {
		if (map.containsKey(fieldName)) {
			throw new Exception("The specified header \"" + fieldName + "\" already exists.");
		} else {
			map.put(fieldName, value);
		}
	}

	public void removeHeader(String fieldName) throws Exception {
		if (map.containsKey(fieldName)) {
			map.remove(fieldName);
		} else {
			throw new Exception("Specified header \"" + fieldName + "\" does not exists.");
		}
	}

	public String toString() {
		String val = "";
		for (Map.Entry<String, String> entry : map.entrySet()) {
			val += entry.getKey() + ": " + entry.getValue();
			val += CRLF;
		}
		val+=CRLF;
		return val;
	}
	
	public void parse(String string) throws Exception {
		String[] dump = string.split(":");
		if(dump.length == 2) {
			this.addHeader(dump[0], dump[1]);
			
		}
	}

}
