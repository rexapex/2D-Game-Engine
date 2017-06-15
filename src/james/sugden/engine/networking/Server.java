package james.sugden.engine.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server extends Thread
{
	/**Client socket*/
	private DatagramSocket socket;
	
	/**Updates while running is true*/
	private volatile boolean running;
	
	public Server()
	{
		try {
			this.socket = new DatagramSocket(3079);
		} catch (SocketException e) {
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
			
			String message = new String(packet.getData());
			if(message.trim().equals("ping"))
			{
				sendData("pong".getBytes(), packet.getAddress(), packet.getPort());
			}
		}
		socket.close();
	}
	
	public void sendData(byte[] data, InetAddress ipAddress, int port)
	{
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
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
