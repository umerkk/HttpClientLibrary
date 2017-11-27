package MuHttpClientLibrary;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class MyPacketWithTimer {

	DatagramSocket sock;
	Packet myPacket;
	Timer t;

	MyPacketWithTimer(DatagramSocket sock, Packet p) {
		this.sock = sock;
		this.myPacket = p;
		t = new Timer();
	}

	public void transmitPacket() throws Exception {
		InetAddress address = InetAddress.getByName("localhost");
		byte[] ss = myPacket.toBytes();
		
		DatagramPacket packet = new DatagramPacket(myPacket.toBytes(), myPacket.toBytes().length, address, 3000);
		this.sock.send(packet);

		t.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				try {
					retransmitPacket();
					//this.cancel();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}, 500, 500);
	}

	private void retransmitPacket() throws Exception {
		t.cancel();
		transmitPacket();
	}
	
	public void makrDelivered() {
		t.cancel();
	}

}
