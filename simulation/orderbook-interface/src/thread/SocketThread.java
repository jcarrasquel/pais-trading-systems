package thread;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.LinkedList;

import cpn.EncodeDecode;
import cpn.JavaCPN;
import main.Main;
import model.Event;
import model.Order;
import util.Utils;

public class SocketThread extends Thread{
    
	private int port;
    
	private String host;
    
	private JavaCPN javaCPN;
    
	private LinkedList<Event> eventBuffer;
	
	private int eventCounter;
	
    public SocketThread(int port, String host){

    	super();

    	this.port = port;
    	
    	this.host = host;
    	
    	eventBuffer = new LinkedList<Event>();
    	
    	eventCounter = 1;
   }
    
   public void enqueueEvent(String messageFromCPN){
	   
	   // messageFromCPN format 1 = (5,placed,7,1980,buy), submit
	   // messageFromCPN format 2 = (4,filled,0,2010,buy), (9,pfilled,1998,3,sell), tid, trade   
	   String[] tmp = messageFromCPN.replaceAll("[(]","").replaceAll("[)]","").split(","); // delete parentheses
	   Order o1 = new Order(tmp[0], tmp[1].equals("placed") ? "-" : tmp[1], tmp[2], tmp[3], tmp[4]);
	   Order o2 = tmp.length > 6 ? new Order(tmp[5], tmp[6].equals("placed") ? "-" : tmp[6], tmp[7], tmp[8], tmp[9]) : null;
	   Event e = tmp.length > 6 ? new Event(String.valueOf(eventCounter), tmp[tmp.length - 1], LocalDateTime.now(), o1, o2, tmp[10]) :  new Event(String.valueOf(eventCounter), tmp[tmp.length - 1], LocalDateTime.now(), o1);
	   
	   synchronized(eventBuffer){
		   eventBuffer.add(e);
	   }
	   
	   if(Main.getApplicationState() == Utils.APP_STATE_PAUSED){
		   synchronized(Main.eventExecutionThread){
			   Main.setApplicationState(Utils.APP_STATE_RUNNING);
			   Main.eventExecutionThread.notify();
		   }
	   }
	   
	   eventCounter++;
   }
   
   public boolean eventBufferEmpty(){
	   Boolean b;
	   synchronized(eventBuffer){
		  b = eventBuffer.isEmpty();
	   }
	   return b;
   }
   
   public Event getNextEvent(){
	   Event e;
	   synchronized(eventBuffer){
		   e =  eventBuffer.removeFirst();
	   }
	   return e;
   }
 
   @Override
   public void run(){

    	try{
    		javaCPN = new JavaCPN();
    		System.out.println("Connecting to host=" + host + " port=" + port);
            javaCPN.connect(host, port);
            System.out.println("Connected to the Petri net model");
    	}catch (UnknownHostException e){
            System.out.println("Don't know about host " + host);
            e.printStackTrace();
    	}catch (IOException e){
    		System.out.println("Couldn't get I/O for the connection to "+host);
    		e.printStackTrace();
    	}
    	
    	String messageFromCPN;
    	
    	while(true){
    		 try {
				messageFromCPN = EncodeDecode.decodeString(javaCPN.receive());
				enqueueEvent(messageFromCPN);
			} catch (SocketException e) {
				e.printStackTrace();
			}
    	}
    	
    }
}