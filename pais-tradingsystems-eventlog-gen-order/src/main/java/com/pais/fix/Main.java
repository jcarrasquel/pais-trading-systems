/** ===================================================================
 * Laboratory of Process-Aware Information Systems (PAIS Lab)
 * National Research University Higher School of Economics. Moscow, Russia.
 * Author: Julio Cesar Carrasquel. Research Asssistant | PhD Candidate
 * Contact: jcarrasquel@hse.ru 
 * 
 * Program: PAIS Event Log Generation from FIX Messages order-based
 * Description: It generates an event log such that each case represents the trace of a single order.
 * All orders are trading the same security. Thus, we may assume they belong to the same trading session 
 * in an order book.
 * ==================================================================== **/

package com.pais.fix;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Main {
	
	protected static PrintWriter writer;

	protected static List<FIXMessage> fixMessages;
	
	public static void generateOrderEventLog(String instrumentId) throws ParseException, FileNotFoundException, UnsupportedEncodingException{
		
		EventLog eventLog = new EventLog();
		
		String messageType = null, sender = null, orderId = null, activity = null, timestamp = null, instrument = null, orderType = null;
		Integer caseCounter = 0, eventCounter = 0;
		
		String price = null, side = null, qty = null, qty2 = null, receiver = null;
		String state = null;
		String ciOrder = null;
		String tradeId = null;
		
		Map<String, Integer> activityFrequencies = new HashMap<String, Integer>();
		
		ArrayList<OrderEvent> newOrders = new ArrayList<OrderEvent>();
		
		for(FIXMessage m : fixMessages){
			instrument = m.getField(48);
			messageType = m.getField(35);
			sender = m.getField(49);
			orderId = m.getField(37);			// system-side order identifier. This will be our case identifier!
			activity = m.getField(150);
			timestamp = m.getField(60);
			orderType = m.getField(40);
			price = m.getField(44);
			side = m.getField(54);
			qty = m.getField(151);
			qty2 = m.getField(38);
			receiver = m.getField(56);
			state = m.getField(39);
			ciOrder = m.getField(11);			// client-side order identifier
			tradeId = m.getField(880);
			
			// *** extracting NEW order messages (FIX message type <D>) ***
			if(messageType != null && messageType.equals("D") && sender != null &&
					ciOrder != null && timestamp != null && instrument != null
						&& instrument.equalsIgnoreCase(instrumentId) && (price != null || orderType.equalsIgnoreCase("1")) && side != null && qty2 != null && receiver != null){
				OrderEvent e = new OrderEvent(activity, state, timestamp, price, side, qty2, sender, ciOrder, orderType);
				newOrders.add(e);
			}
			
			// *** extracting EXECUTION REPORT messages (FIX message type <8>) ***
			if(messageType != null && messageType.equalsIgnoreCase("8") && sender != null && sender.equalsIgnoreCase("FGW") &&
				orderId != null && activity != null && timestamp != null && instrument != null
					&& instrument.equalsIgnoreCase(instrumentId) && orderType != null 
					&& (price != null || orderType.equalsIgnoreCase("1")) && side != null && qty != null && receiver != null){

				if( ((LinkedHashMap<String, Case>) eventLog.getCases()).containsKey(orderId) == false){
					Case newCase = new Case();
					((LinkedHashMap<String, Case>) eventLog.getCases()).put(orderId, newCase);
					caseCounter++;
				}
				
				OrderEvent e = new OrderEvent(activity, state, timestamp, price, side, qty, receiver, ciOrder, orderType);
				if(e.getActivity().equalsIgnoreCase("trade") || e.getActivity().equalsIgnoreCase("trade_cancel")){
					e.setTradeId(tradeId);
				}
				((Case) ( (LinkedHashMap<String, Case>) eventLog.getCases()).get(orderId)).addEvent(e); // new event e to the case identified by order id
				eventCounter++;
			}
		}
		
		LinkedHashMap<String,Case> cases = eventLog.getCases();
		
		// *** add new orders messages to the cases ***
		int n = 0;
		for(int i = 0; i < newOrders.size(); i++){
			for(Entry<String, Case> c : cases.entrySet()){
				ArrayList<Event> events = ( (Case) c.getValue()).getEvents();
				OrderEvent e = (OrderEvent) events.get(0);
				if(e.getCiOrderId().equalsIgnoreCase(newOrders.get(i).getCiOrderId())){
					events.add(0, newOrders.get(i));
					break;
				}
			}
		}
		
		PrintWriter writer = new PrintWriter("order-event-log.csv", "UTF-8");
		
		writer.println("CASE, ORDER, EVENT_ID, ACTIVITY, STATE, TIMESTAMP, CLIENT, SIZE, PRICE, SIDE, [TRADE_ID]");
		
		n = 0;
		int k = 1;
		for(Entry<String, Case> c : cases.entrySet()){
			n++;
			ArrayList<Event> events = ( (Case) c.getValue()).getEvents();
			for(int i = 0; i < events.size(); i++){
				OrderEvent e = (OrderEvent) events.get(i);
				writer.println(n + "," + c.getKey() + "," + k + "," + e.getActivity() + "," + e.getState() + "," + e.getTimestamp() + "," + e.getReceiver() + "," + e.getQty() + "," + (e.getOrderType().equalsIgnoreCase("market") ? "market" : e.getPrice()) + "," + e.getSide() + "," + (e.getTradeId() == null ? "" : e.getTradeId()));
				k++;
			}
		}
		writer.close();
		
		System.out.println("Execution Report Messages <8> =  " + eventCounter);
		for (Map.Entry<String, Integer> activityEntry : activityFrequencies.entrySet()){ 
			System.out.println("activity = " + activityEntry.getKey() + " frequency = " + activityEntry.getValue()); 
		}
		
		System.out.println("Number of Orders (Cases) = " + caseCounter);
		System.out.println("Number of Events = " + (eventCounter + newOrders.size()));
	}
	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, ParseException{
		
		System.out.println("Laboratory of Process-Aware Information Systems (PAIS Lab). Moscow, Russia.");		
		System.out.println("Order-based Event Log Generator");
		System.out.println("*******************************");
		
		String fixMessagesFilename = args[0];
		
		String securityId = args[1];
		
		fixMessages = new LinkedList<FIXMessage>();
		
		System.out.println("Retrieving FIX messages from TCP segment payloads in capture file = " + fixMessagesFilename);
		
		FIXParser.getFIXMessages(fixMessagesFilename, fixMessages); // extract FIX messages from TCP segments payload
		
		System.out.println("Generating Event Log of orders trading financial security = " + securityId);
		
		generateOrderEventLog(securityId);
		
		System.out.println("Event log generation terminated.");
	}
	
}