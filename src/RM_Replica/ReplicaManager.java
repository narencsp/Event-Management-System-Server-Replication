package RM_Replica;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Timer; 
import java.util.TimerTask;


public class ReplicaManager {
	
	private static Queue<String> q = new LinkedList<String>();
	public Queue<String> holdBackQueue;
	public Queue<String> deliveryQueue;
	public Queue<String> backUpQueue;
	public static String FEip = null;
	public static boolean hasCrashed = false;
	
	public static void delay() {
		long startTime = System.currentTimeMillis();
		while(true) {
			if(System.currentTimeMillis() - startTime > 5000) {
				break;
			}
		}
	}
	
	public static void fromSequencer() throws IOException{
		
		
		int counter = 0;

		System.setProperty("java.net.preferIPv4Stack", "true");
		MulticastSocket mSocket = new MulticastSocket(Ports.RM1PortNumber);
		InetAddress group = InetAddress.getByName("238.255.255.255");
		mSocket.setBroadcast(true);
		mSocket.joinGroup(group);
		try {
			byte[] data = new byte[65335];
			while (true) {
				data = new byte[65535];
				DatagramPacket packet = new DatagramPacket(data, data.length);
				mSocket.receive(packet);

				String incomingMessage = new String(packet.getData(), 0, packet.getLength());
				
				System.out.print(incomingMessage);
				
		 		String[] messageBreak = incomingMessage.split("\\s");
				FEip = messageBreak[1];

				InetAddress aHost = InetAddress.getByName(FEip);
				q.add(incomingMessage);
				if(!hasCrashed) {
					
					hasCrashed = true;
				    Timer timer = new Timer();
				    TimerTask delayedThreadStartTask = new TimerTask() {
				        @Override
				        public void run() {
				            //moved to TimerTask
				            new Thread(new Runnable() {
				                @Override
				                public void run() {
				    				while(!q.isEmpty()) {
				    					String msg = q.poll();
				    					String resp = sendReceive(msg);
				    					if(!resp.equals(null)) {
				    						byte[] dta = new byte[1000];
				    						dta = resp.getBytes();
				    						DatagramPacket rep = new DatagramPacket(dta, dta.length, aHost, Ports.FrontEndPortNumber);
				    						try {
												mSocket.send(rep);
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
				    					}
				    				}
									System.out.println("RM Crashed");
				                }
				            }).start();
				        }
				    };
				    timer.schedule(delayedThreadStartTask, 6 * 1000); //1 minute
				}
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			mSocket.close();
		}

	}
	
	public static String sendReceive(String data) {		
		String resp = "";
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = null;
		
		try {
			final DatagramSocket aSocket = new DatagramSocket(Ports.RM1ListenerPortNumber);
			Callable<String> callable = new Callable<String>() {

				@Override
				public String call() {
					String rep = null;
					try {
						byte[] buffer = new byte[1000];
						DatagramPacket request = null;
						
						while (true) {
							request = new DatagramPacket(buffer, buffer.length);
							aSocket.receive(request);
							rep = new String(request.getData(), 0, request.getLength());
							
							if(!rep.equals(null)) {
								break;
							}
						}

					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						aSocket.close();
					}

					return rep;
				}

			};
			future = executor.submit(callable);
			byte[] seq = null;

			int serverPort = 0;
	 		String[] messageBreak = data.split("\\s");
			FEip = messageBreak[1];
			if (messageBreak[2].equalsIgnoreCase("MTL")) {
				serverPort = Ports.RM1MontrealPortNumber;
			} else if (messageBreak[2].equalsIgnoreCase("OTW")) {
				serverPort = Ports.RM1OttawaPortNumber;
			} else if (messageBreak[2].equalsIgnoreCase("TOR")) {
				serverPort = Ports.RM1TorontoPortNumber;
			}
			seq = data.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(seq, seq.length, aHost, serverPort);
			aSocket.send(request);

			try {
				resp = future.get();
				System.out.println("value received from the " + resp);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Socket: " + e.getMessage());
		}

		executor.shutdown();
		return resp;

	}

	public static void main(String[] args) {
		
		Runnable task1 = () -> {
			try {
				fromSequencer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		};

		new Thread(task1).start();
		
		System.out.println("RM is running");
	}

}
