package james.sugden.engine.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client extends Thread
{
	/**IP address of the client*/
	private InetAddress ipAddress;
	
	/**Client socket*/
	private DatagramSocket socket;
	
	/**Updates while running is true*/
	private volatile boolean running;
	
	public Client(String ipAddress)
	{
		try {
			this.socket = new DatagramSocket();
			this.ipAddress = InetAddress.getByName(ipAddress);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void start()
	{
		running = true;
		super.start();
	}
	
	public void run()
	{
		while(running)
		{
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket.close();
	}
	
	public void sendData(byte[] data)
	{
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 3079);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public final void close()
	{
		running = false;
	}
}
