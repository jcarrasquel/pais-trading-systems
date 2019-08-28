/** ===================================================================
 * Laboratory of Process-Aware Information Systems (PAIS Lab)
 * National Research University Higher School of Economics. Moscow, Russia.
 * Author: Julio Cesar Carrasquel. Research Asssistant | PhD Candidate
 * Contact: jcarrasquel@hse.ru 
 * 
 * Program: Interface Prototype for Replay and Visualization of Simulations.
 * Description: This prototype interface works on two modes. It allows to replay an event
 * log of order books from a file. Applications like simulation tools also can send events 
 * on stream to this interface via sockets to support better visualization of the order book state.
 * ==================================================================== **/

package main;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import model.Event;
import model.Order;
import model.OrderBook;
import model.Trade;
import thread.SocketThread;
import ui.OrderBookUserInterface;
import util.Utils;

public class Main {
	
	protected static OrderBook orderBook = new OrderBook();
	
	protected static OrderBookUserInterface orderBookUserInterface;
	
	protected static List<Trade> trades = new LinkedList<Trade>();
	
	protected static PriorityQueue<Order> arrivingBuyOrders = new PriorityQueue<>(Utils.createComparator("buy"));
	
	protected static PriorityQueue<Order> arrivingSellOrders = new PriorityQueue<>(Utils.createComparator("sell"));
	
	protected static int applicationState = Utils.APP_STATE_INITIAL;
	
	protected static Character applicationMode;
	
	public static String inputFile;
	
	public static Thread eventExecutionThread;
	
	public static Thread userInterfaceThread;
	
	public static SocketThread socketThread;
	
	public static String host;
	
	public static int port;
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException{
		
		System.out.println("Laboratory of Process-Aware Information Systems (PAIS Lab). Moscow, Russia.");		
		System.out.println("Order Book Interface for Replay and Simulation Support");
		System.out.println("******************************************************");
		
		applicationMode = args[0].charAt(1);	
		
		if(applicationMode == Utils.MODE_FILE){
			inputFile = args[1];
			System.out.println("Executing order book interface in mode = READ");
			System.out.println("Order book event log will be read from file = " + inputFile);
			
		}
	
		if(applicationMode == Utils.MODE_SOCKET){
			host = args[1];
			port = Integer.parseInt(args[2]);
			System.out.println("Executing order book interface in file = SOCKET");
			socketThread = new SocketThread(port, host);
			System.out.println("Events will be received in stream from host = " + host + " and port = " + port);
		}
		
		setUIFont(new FontUIResource(new Font("SansSerif", 0, 11)));
		
		orderBookUserInterface = new OrderBookUserInterface(){
	
			private static final long serialVersionUID = 1L;

			// override actions based on button events that will affect the event execution thread
			
			@Override 
			public void play() throws FileNotFoundException, IOException, InterruptedException{
				
				if(applicationState == Utils.APP_STATE_INITIAL){
					
					eventExecutionThread = initializeEventExecutionThread();
					applicationState = Utils.APP_STATE_RUNNING;
					eventExecutionThread.start();
					
				}else if(applicationState == Utils.APP_STATE_PAUSED){
					synchronized(eventExecutionThread){
						applicationState = Utils.APP_STATE_RUNNING;
						eventExecutionThread.notifyAll();
					}
				}
			}
			
			@Override 
			public void pause() throws InterruptedException{
				applicationState = Utils.APP_STATE_PAUSED;
			}
			
			@Override
			public void stop(){
				int previousApplicationState = applicationState;
				applicationState = Utils.APP_STATE_STOP;
				synchronized(eventExecutionThread){
					if(previousApplicationState == Utils.APP_STATE_PAUSED){
						eventExecutionThread.notifyAll();
					}
				}
			}
			
			@Override
			public void nextStep(){
				if(applicationState == Utils.APP_STATE_INITIAL){
					eventExecutionThread = initializeEventExecutionThread();
					applicationState = Utils.APP_STATE_ONESTEP;
					eventExecutionThread.start();
				}else if(applicationState == Utils.APP_STATE_PAUSED){
					synchronized(eventExecutionThread){
						applicationState = Utils.APP_STATE_ONESTEP;
						eventExecutionThread.notifyAll();
					}
				}
			}
		};
		
		userInterfaceThread = new Thread(){
			@Override
			public void run(){
				orderBookUserInterface.display();
			}
		};
		userInterfaceThread.start();
		
		if(applicationMode == Utils.MODE_SOCKET){
			socketThread.start();
			eventExecutionThread = initializeEventExecutionThread();
			applicationState = Utils.APP_STATE_RUNNING;
			eventExecutionThread.start();
		}
	}
	
	public static int getApplicationState(){
		return applicationState;
	}
	
	public static Character getApplicationMode(){
		return applicationMode;
	}
	
    public static void setUIFont(FontUIResource f) {
        Enumeration<?> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                FontUIResource orig = (FontUIResource) value;
                Font font = new Font(f.getFontName(), orig.getStyle(), f.getSize());
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }
    
	public static void reInitiateApplication(){
		orderBook = new OrderBook();
		trades = new LinkedList<Trade>();
		arrivingBuyOrders = new PriorityQueue<>(Utils.createComparator("buy"));
		arrivingSellOrders = new PriorityQueue<>(Utils.createComparator("sell"));
		applicationState = Utils.APP_STATE_INITIAL;
		orderBookUserInterface.displayCurrentEvent(null);
		orderBookUserInterface.refresh(orderBook, arrivingBuyOrders, arrivingSellOrders, null, 0);
	}
	
	public static Thread initializeEventExecutionThread(){
		return new Thread(){
			
			@Override
			public void run(){
				try {
					if(applicationMode == Utils.MODE_FILE){
						Main.readEventsFromLog();
					}else if(applicationMode == Utils.MODE_SOCKET){
						Main.readEventsFromSocket();
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	public static void moveFromBufferToOrderBook(Order o1){
        Iterator<Order> value = o1.getSide().equals("buy") ? (Iterator<Order>) arrivingBuyOrders.iterator() : arrivingSellOrders.iterator();
        
        while (value.hasNext()) { 
        	Order o2 = (Order) value.next();
        	if(o1.equals(o2)){
        		o1.setArrivalTime(o2.getArrivalTime());
        		break;
        	}
        }
        if(o1.getSide().equals("buy")){
        	arrivingBuyOrders.remove(o1);
        	orderBook.getBuySide().add(o1);
        }else{
        	arrivingSellOrders.remove(o1);
        	orderBook.getSellSide().add(o1);
        }
	}
	
	public static void matchOrders(String tradeIdentifier, LocalDateTime tradeTimestamp, PriorityQueue<Order> buyQueue, PriorityQueue<Order> sellQueue, Order buyOrderUpdated, Order sellOrderUpdated){
		
		Iterator<Order> buyQueueIterator = buyQueue.iterator();
		Iterator<Order> sellQueueIterator = sellQueue.iterator();
		
		Integer buyOrderPreviousSize = 0, sellOrderPreviousSize = 0, tradeQuantity;
		
		LocalDateTime buyOrderArrivalTime = null, sellOrderArrivalTime = null;
		
		String order1Id = buyOrderUpdated.getIdentifier();
		String order2Id = sellOrderUpdated.getIdentifier();
		
        while (buyQueueIterator.hasNext()) { 
        	Order orderInQueue = (Order) buyQueueIterator.next();
        	if(buyOrderUpdated.equals(orderInQueue)){
        		orderInQueue.setCurrentState(buyOrderUpdated.getCurrentState());
        		buyOrderPreviousSize = orderInQueue.getCurrentSize();
        		buyOrderArrivalTime = orderInQueue.getArrivalTime();
        		orderInQueue.setCurrentSize(buyOrderUpdated.getCurrentSize());
        		if(orderInQueue.getCurrentSize() == 0){
        			buyQueue.remove(orderInQueue);
        		}
        		break;
        	}
        }
        	
    	while (sellQueueIterator.hasNext()) { 
         	Order orderInQueue = (Order) sellQueueIterator.next();
         	if(sellOrderUpdated.equals(orderInQueue)){
         		orderInQueue.setCurrentState(sellOrderUpdated.getCurrentState());
         		sellOrderPreviousSize = orderInQueue.getCurrentSize();
         		sellOrderArrivalTime = orderInQueue.getArrivalTime();
         		orderInQueue.setCurrentSize(sellOrderUpdated.getCurrentSize());
         		if(orderInQueue.getCurrentSize() == 0){
         			sellQueue.remove(orderInQueue);
         		}
         		break;
         	}
    	}
    	
    	tradeQuantity = Math.abs(buyOrderPreviousSize - sellOrderPreviousSize);
    	
    	trades.add(new Trade(tradeIdentifier, tradeTimestamp, order1Id, order2Id, tradeQuantity, buyOrderArrivalTime, sellOrderArrivalTime));
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static void cancelTrade(String tradeIdentifier, PriorityQueue<Order> buyQueue, PriorityQueue<Order> sellQueue, Order buyOrderUpdated, Order sellOrderUpdated){
		
		LocalDateTime buyOrderArrivalTime = null, sellOrderArrivalTime = null;
		
		for(int i = 0; i < trades.size(); i++){ 
			// retrieve previous arrival times of the orders from the trade
			// needed if the orders were already deleted from the order books because of a complete fill
			if(trades.get(i).getIdentifier().equals(tradeIdentifier)){
				buyOrderArrivalTime = trades.get(i).getOrder1ArrivalTime();
				sellOrderArrivalTime = trades.get(i).getOrder2ArrivalTime();
				break;
			}
		}
		
		trades.remove(tradeIdentifier);
		
		if(buyQueue != null){ // the order is in the order book
		
			Iterator<Order> buyQueueIterator = buyQueue.iterator();
			
	        while (buyQueueIterator.hasNext()) { 
	        	Order orderInQueue = (Order) buyQueueIterator.next();
	        	if(buyOrderUpdated.equals(orderInQueue)){
	        		orderInQueue.setCurrentState(buyOrderUpdated.getCurrentState());
	        		orderInQueue.setCurrentSize(buyOrderUpdated.getCurrentSize());
	        		if(orderInQueue.getCurrentSize() == 0){
	        			buyQueue.remove(orderInQueue);
	        		}
	        		break;
	        	}
	        }
		}else{ // the queue passed as null means, that the buy order does not exist in any queue (it was previously filled), so place it again
			buyOrderUpdated.setArrivalTime(buyOrderArrivalTime);
			if(buyOrderUpdated.getCurrentState().equalsIgnoreCase("new") || buyOrderUpdated.getCurrentState().equalsIgnoreCase("partially filled")){
				// if the updated state indicates this place it on the order book
				orderBook.getBuySide().add(buyOrderUpdated);
			}else{ // otherwise, on the arriving orders
				arrivingBuyOrders.add(buyOrderUpdated);
			}
		}
		
		if(sellQueue != null){
			
			Iterator<Order> sellQueueIterator = sellQueue.iterator();
	        
	    	while (sellQueueIterator.hasNext()) { 
	         	Order orderInQueue = (Order) sellQueueIterator.next();
	         	if(sellOrderUpdated.equals(orderInQueue)){
	         		orderInQueue.setCurrentState(sellOrderUpdated.getCurrentState());
	         		orderInQueue.setCurrentSize(sellOrderUpdated.getCurrentSize());
	         		if(orderInQueue.getCurrentSize() == 0){
	         			sellQueue.remove(orderInQueue);
	         		}
	         		break;
	         	}
	    	}
		}else{
			sellOrderUpdated.setArrivalTime(sellOrderArrivalTime);
			if(sellOrderUpdated.getCurrentState().equalsIgnoreCase("new") || sellOrderUpdated.getCurrentState().equalsIgnoreCase("partially filled")){
				// if the updated state indicates this place it on the order book
				orderBook.getSellSide().add(sellOrderUpdated);
			}else{ // otherwise, on the arriving orders
				arrivingSellOrders.add(sellOrderUpdated);
			}
		}
	}
	
	public static void replaceOrder(PriorityQueue<Order> queue, Order updatedOrder){
		
		Iterator<Order> queueIterator = queue.iterator();
		
        while (queueIterator.hasNext()) { 
        	Order orderInQueue = (Order) queueIterator.next();
        	if(updatedOrder.equals(orderInQueue)){
        		orderInQueue.setCurrentState(updatedOrder.getCurrentState());
        		orderInQueue.setCurrentSize(updatedOrder.getCurrentSize());
        		orderInQueue.setPrice(updatedOrder.getPrice());
        		break;
        	}
        }
	}
	
	public static void executeEvent(Event event) throws InterruptedException{
		
		int x = 0; // in which queues the event is being held
		
		orderBookUserInterface.displayCurrentEvent(event);
		
		String activity = event.getActivity();
		
		Order o1 = event.getOrder1();
		String side1 = o1.getSide();
		
		if(activity.equalsIgnoreCase("replace")){
			if(side1.equals("buy")){
				x = arrivingBuyOrders.contains(o1) ? 1 : 2;
				replaceOrder(arrivingBuyOrders.contains(o1) ? arrivingBuyOrders : orderBook.getBuySide(), o1);
			}else{
				x = arrivingSellOrders.contains(o1) ? 4 : 3;
				replaceOrder(arrivingSellOrders.contains(o1) ? arrivingSellOrders : orderBook.getSellSide(), o1);
			}
		}
		
		if(activity.equalsIgnoreCase("trade_cancel")){
			
			Order o2 = event.getOrder2();
			
			String tradeIdentifier = event.getTradeIdentifier();
			
			if(side1.equals("buy")){
				cancelTrade(tradeIdentifier, arrivingBuyOrders.contains(o1) ? arrivingBuyOrders : (orderBook.getBuySide().contains(o1) ? orderBook.getBuySide() : null),
					arrivingSellOrders.contains(o2) ? arrivingSellOrders : (orderBook.getSellSide().contains(o2) ? orderBook.getSellSide() : null), o1, o2);
			}else{
				cancelTrade(tradeIdentifier, arrivingBuyOrders.contains(o2) ? arrivingBuyOrders : (orderBook.getBuySide().contains(o2) ? orderBook.getSellSide() : null),
					arrivingSellOrders.contains(o1) ? arrivingSellOrders : (orderBook.getSellSide().contains(o1) ? orderBook.getSellSide() : null), o2, o1);
			}	
			
			if(side1.equals("buy")){
				if(arrivingBuyOrders.contains(o1) && orderBook.getSellSide().contains(o2)){
					x = 5;
				}else if (orderBook.getBuySide().contains(o1) && orderBook.getSellSide().contains(o2)){
					x = 8;
				}else if (orderBook.getBuySide().contains(o1) && arrivingSellOrders.contains(o2)){
					x = 6;
				}
			}else{
				if(arrivingBuyOrders.contains(o2) && orderBook.getSellSide().contains(o1)){
					x = 5;
				}else if (orderBook.getBuySide().contains(o2) && orderBook.getSellSide().contains(o1)){
					x = 8;
				}else if (orderBook.getBuySide().contains(o2) && arrivingSellOrders.contains(o1)){
					x = 6;
				}
			}
		}
		
		if(activity.equalsIgnoreCase("trade")){
			
			Order o2 = event.getOrder2();
			
			String tradeIdentifier = event.getTradeIdentifier();

			LocalDateTime tradeTimestamp = event.getTimestamp();
			
			if(side1.equals("buy")){
				if(arrivingBuyOrders.contains(o1) && orderBook.getSellSide().contains(o2)){
					x = 5;
				}else if (orderBook.getBuySide().contains(o1) && orderBook.getSellSide().contains(o2)){
					x = 8;
				}else if (orderBook.getBuySide().contains(o1) && arrivingSellOrders.contains(o2)){
					x = 6;
				}
			}else{
				if(arrivingBuyOrders.contains(o2) && orderBook.getSellSide().contains(o1)){
					x = 5;
				}else if (orderBook.getBuySide().contains(o2) && orderBook.getSellSide().contains(o1)){
					x = 8;
				}else if (orderBook.getBuySide().contains(o2) && arrivingSellOrders.contains(o1)){
					x = 6;
				}
			}
			
			orderBookUserInterface.refresh(orderBook, arrivingBuyOrders, arrivingSellOrders, event, x);	
			Thread.sleep(1900);
			
			if(side1.equals("buy")){ // o2 is assumed them to be a sell order
				matchOrders(tradeIdentifier, tradeTimestamp, arrivingBuyOrders.contains(o1) ? arrivingBuyOrders : orderBook.getBuySide(),
					arrivingSellOrders.contains(o2) ? arrivingSellOrders : orderBook.getSellSide(), o1, o2);
			}else{
				matchOrders(tradeIdentifier, tradeTimestamp, arrivingBuyOrders.contains(o2) ? arrivingBuyOrders : orderBook.getBuySide(),
					arrivingSellOrders.contains(o1) ? arrivingSellOrders : orderBook.getSellSide(), o2, o1);
			}			
		}
		
		if(activity.equalsIgnoreCase("submit")){
			// add order to one of the arriving buffers
			o1.setArrivalTime(event.getTimestamp());
			if(side1.equals("buy")){
				arrivingBuyOrders.add(o1);
			}else{
				arrivingSellOrders.add(o1);
			}
		}
		
		if(activity.equalsIgnoreCase("new")){
			// the order will be removed from the arrival buffer, and added to the order book
			moveFromBufferToOrderBook(o1);
		}
		
		if(activity.equalsIgnoreCase("reject")){
			// discard from the arrival buffer
			orderBookUserInterface.refresh(orderBook, arrivingBuyOrders, arrivingSellOrders, event, side1.equals("buy") ? 1 : 4);	
			Thread.sleep(1900);
			if(side1.equals("buy")){
				arrivingBuyOrders.remove(o1);
			}else{
				arrivingSellOrders.remove(o1);
			}
		}
		
		if(activity.equalsIgnoreCase("cancel") || activity.equalsIgnoreCase("expire")){
			if(side1.equals("buy")){
				x = arrivingBuyOrders.contains(o1) ? 1 : 2;
				orderBookUserInterface.refresh(orderBook, arrivingBuyOrders, arrivingSellOrders, event, x);
				Thread.sleep(1900);
				if(arrivingBuyOrders.contains(o1)){
					arrivingBuyOrders.remove(o1);
				}else if(orderBook.getBuySide().contains(o1)){
					orderBook.getBuySide().remove(o1);
				}
			}else{
				x = arrivingSellOrders.contains(o1) ? 4 : 3;
				orderBookUserInterface.refresh(orderBook, arrivingBuyOrders, arrivingSellOrders, event, x);
				Thread.sleep(1900);
				if(arrivingSellOrders.contains(o1)){
					arrivingSellOrders.remove(o1);
				}else if(orderBook.getSellSide().contains(o1)){
					orderBook.getSellSide().remove(o1);
				}
			}
		}
		
		orderBookUserInterface.refresh(orderBook, arrivingBuyOrders, arrivingSellOrders, event, x);	
		
		if(activity.equalsIgnoreCase("cancel") || activity.equalsIgnoreCase("expire") || activity.equalsIgnoreCase("reject")
			|| activity.equalsIgnoreCase("trade")){
			Thread.sleep(800);
		}else{
			Thread.sleep(1600);
		}
	}
	
	public static void setApplicationState(int newState){
		 applicationState = newState;
	}
	
	public static void readEventsFromSocket() throws InterruptedException, IOException{
		
		//PrintWriter eventLogWriter = new PrintWriter("output.csv", "UTF-8");
		//eventLogWriter.println("CASE, EVENT_ID, ACTIVITY, TIMESTAMP, O1_ID, O1_STATE, O1_SIZE, O1_PRICE, O1_SIDE,  [O2_ID], [O2_STATE], [O2_SIZE], [O2_PRICE], [O2_SIDE], [TRADE_ID]");
		
		while(true){
			Event nextEvent;
			synchronized(eventExecutionThread){
				while(socketThread.eventBufferEmpty()){
					System.out.println("Waiting for new event...");
					applicationState = Utils.APP_STATE_PAUSED;
					eventExecutionThread.wait();
				}
				nextEvent = socketThread.getNextEvent();
				executeEvent(nextEvent); // why if to put it out 
				System.out.println("1" + "," + nextEvent.toString());
			}
		}
	}
	
	public static void readEventsFromLog() throws FileNotFoundException, IOException, InterruptedException{
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)))) {
			
			@SuppressWarnings("unused")
			String eventLine, caseId, eventId, activity, timestamp;
			Order o1, o2;
			
			reader.readLine(); // read the first event log line which is the header

			while ((eventLine = reader.readLine()) != null) {
				
				synchronized(eventExecutionThread){
					
					while(! (applicationState == Utils.APP_STATE_RUNNING || applicationState == Utils.APP_STATE_ONESTEP || applicationState == Utils.APP_STATE_STOP)){
						eventExecutionThread.wait();
					}
					
					if(applicationState == Utils.APP_STATE_STOP){
						break;
					}
					
					String[] tmp = eventLine.split(",");
					caseId = tmp[0];
					eventId = tmp[2];
					activity = tmp[3];
					timestamp = tmp[4];
					Boolean isTradeActivity = activity.equalsIgnoreCase("trade") || activity.equalsIgnoreCase("trade_cancel");
					o1 = new Order(tmp[5], tmp[6], tmp[7], tmp[8], tmp[9]);
					o2 = isTradeActivity ? new Order(tmp[10], tmp[11], tmp[12], tmp[13], tmp[14]) : null;
					
					Event event = isTradeActivity? new Event(eventId, activity, timestamp, o1, o2, tmp[15]) : new Event(eventId, activity, timestamp, o1) ;
					executeEvent(event);
					
					if(applicationState == Utils.APP_STATE_ONESTEP){
						applicationState = Utils.APP_STATE_PAUSED;
					}	
				}
			}
		}
		if(applicationState == Utils.APP_STATE_STOP){
			reInitiateApplication();
		}
	}
}