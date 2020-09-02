package RM_Replica;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;

import DEMSInterfaceApp.*;


public class FrontEnd extends DEMSInterfacePOA{
	
	private ORB orb;

	public void setORB(ORB orb_val) {
		orb = orb_val;

	}
	
	public String bookEvent(String customerID, String eventID, String eventType) {
		String data = customerID.substring(0, 3) + " " + "1" + " " + customerID + " " + eventID + " " + eventType;
		return sendToSequencer(data);
	}

	
	public String getBookingSchedule(String customerID) {
		String resp = "";

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = null;

		try {
			final DatagramSocket aSocket = new DatagramSocket(Ports.FrontEndPortNumber);
			Callable<String> callable = new Callable<String>() {

				@Override
				public String call() throws IOException {
					// TODO Auto-generated method stub

					HashMap<Integer, String> data = new HashMap<Integer, String>();
					try {
						byte[] buffer = new byte[1000];
						DatagramPacket request = null;
						long startTime = System.currentTimeMillis();

						while (true) {
							request = new DatagramPacket(buffer, buffer.length);
							aSocket.receive(request);

							data.put(request.getPort(), new String(request.getData(), 0, request.getLength()));

//							if (Ports.CRASH == true) {
//								if (data.size() == 3) {
//									break;
//								}
//							} else {
//								if (data.size() == 4) {
//									break;
//								}
//							}
							
							if(data.size() == 4) {
								break;
							}
							
							if(System.currentTimeMillis() - startTime > 2500) {
								Ports.CRASH = true;
								break;
							}

						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						aSocket.close();
					}

					Collection<String> list = data.values();
				    for(Iterator<String> itr = list.iterator(); itr.hasNext();)
				    {
				        if(Collections.frequency(list, itr.next()) == 1)
				        {
				        	for(HashMap.Entry<Integer, String> e : data.entrySet()) {
				        		if(e.getValue().equalsIgnoreCase(itr.toString())) {
				        			
				        			int serverPort = e.getKey();
				        			String dta = "Issue";
				        			
				        			byte[] m = new byte[1000];
				        			m = dta.getBytes();
				        			
				        			DatagramSocket socket = new DatagramSocket();
				        			DatagramPacket req = new DatagramPacket(m, m.length, InetAddress.getByName("238.255.255.255"), serverPort);
				        			socket.send(req);
				        			
				        		}
				        	}
				        }
				    }
				    
				    String[] a = new String[data.size()];
				    a = (String[]) data.values().toArray();
				    
				    if(Ports.CRASH == true) {
				    	if(data.size() == 2) {
				    		return a[0];
				    	} else {
				    		return "ERROR!!!";
				    	}
				    } else {
				    	if(data.size() == 3) {
				    		return a[0];
				    	} else {
				    		return "ERROR!!";
				    	}
				    }

				}

			};
			future = executor.submit(callable);
			byte[] seq = null;

			String data = customerID.substring(0, 3) + " " + "2" + " " + customerID;
			seq = data.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");
			int seq_port = Ports.SequencerPortNumber;
			DatagramPacket request = new DatagramPacket(seq, data.length(), aHost, seq_port);

			aSocket.send(request);

			try {
				resp = future.get();
				System.out.println("value received from the " + resp);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
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

	
	public String cancelEvent(String customerID, String eventID) {
		String data = customerID.substring(0, 3) + " " + "3" + " " + customerID + " " + eventID + " ";
		return sendToSequencer(data);
	}

	
	public String addEvent(String eventID, String eventType, String bookingCapacity){
		String data = eventID.substring(0, 3) + " " + "4" + " " + eventID + " " + eventType + " " + bookingCapacity;
		return sendToSequencer(data);
	}
	
	
	public String removeEvent(String eventID, String eventType) {
		String data = eventID.substring(0, 3) + " " + "5" + " " + eventID + " " + eventType;
		return sendToSequencer(data);
	}
	
	public String swapEvent(String customerID, String oldEventType, String oldEventID, String newEventType,
			String newEventID) {
		String data = customerID.substring(0, 3) + " " + "7" + " " + oldEventType + " " + oldEventID + " "+ newEventType + " " + newEventID;
		return sendToSequencer(data);
	}
	
	public String listEventAvailability(String eventType) {
		String data = "MTL" + " " + "6" + " " + eventType;
		return sendToSequencer(data);
	}
	
	public static String sendToSequencer(String data) {
		String resp = "";
		System.out.println(data);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = null;

		try {
			final DatagramSocket aSocket = new DatagramSocket(Ports.FrontEndPortNumber);
			Callable<String> callable = new Callable<String>() {

				@Override
				public String call() {
					// TODO Auto-generated method stub

					HashMap<Integer, String> data = new HashMap<Integer, String>();
					try {
						byte[] buffer = new byte[1000];
						DatagramPacket request = null;

						while (true) {
							request = new DatagramPacket(buffer, buffer.length);
							aSocket.receive(request);
							
							data.put(request.getPort(), new String(request.getData(), 0, request.getLength()));
							
						//	System.out.println(data.toString());
							
							if (Ports.CRASH == true) {
								if (data.size() == 1) {
									break;
								}
							} else {
								if (data.size() == 1) {
									break;
								}
							}

						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						aSocket.close();
					}

					Collection<String> list = data.values();
				    for(Iterator<String> itr = list.iterator(); itr.hasNext();)
				    {
				        if(Collections.frequency(list, itr.next()) == 1)
				        {
//				            itr.remove();
				        }
				    }
				    
				    String[] a = new String[data.size()];
				    a =  data.values().toArray(new String[data.size()]);
				    if(Ports.CRASH == true) {
				    	if(data.size() == 2) {
				    		return a[0];
				    	} else {
				    		return "ERROR!!!";
				    	}
				    } else {
				    	if(data.size() == 3) {
				    		return a[0];
				    	} else {
				    		return "ERROR!!";
				    	}
				    }

				}

			};
			future = executor.submit(callable);
			byte[] seq = null;

			seq = data.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");
			int seq_port = Ports.SequencerPortNumber;
			DatagramPacket request = new DatagramPacket(seq, data.length(), aHost, seq_port);

			aSocket.send(request);

			try {
				resp = future.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
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

	
	public static void main(String[] args) throws Exception {
		System.out.println("FE running... ");
		FrontEnd stub1 = new FrontEnd();
		ORB orb = ORB.init(args, null);

		POA rootpoa = (POA) orb.resolve_initial_references("RootPOA");
		rootpoa.the_POAManager().activate();
		stub1.setORB(orb);

		org.omg.CORBA.Object ref = rootpoa.servant_to_reference(stub1);
		DEMSInterface href = DEMSInterfaceHelper.narrow(ref);
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

		String mtl = "montreal";
		String otw = "ottawa";
		String tor = "toronto";

		NameComponent path1[] = ncRef.to_name(mtl);
		NameComponent path2[] = ncRef.to_name(otw);
		NameComponent path3[] = ncRef.to_name(tor);

		ncRef.rebind(path1, href);
		ncRef.rebind(path2, href);
		ncRef.rebind(path3, href);

		orb.run();

	}
}
