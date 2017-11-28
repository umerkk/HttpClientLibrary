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
		
		DatagramPacket packet = new DatagramPacket(myPacket.toBytes(), myPacket.toBytes().length, address, 3000);
		this.sock.send(packet);

		if(myPacket.getType() == 0) {
			t.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					try {
						retransmitPacket();
						//this.cancel();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}, 5000, 5000);
		}
	}

	private void retransmitPacket() throws Exception {
		if(myPacket.getType() == 0) {
			t.cancel();
		}
		transmitPacket();
	}
	
	public void makrDelivered() {
		if(myPacket.getType() == 0) {
			t.cancel();
		}
	}

}
