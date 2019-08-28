package model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Event {
	
	protected String identifier;
	
	protected String activity;
	
	protected LocalDateTime timestamp;
	
	protected Order o1;
	
	protected Order o2; // for activities involving two orders such as a trade
	
	protected String tradeIdentifier; // for activities related to a trade, i.e, trade or trade_cancel
	
	public Event(String identifier, String activity, String timestamp, Order o1){
		this.identifier = identifier;
		this.activity = activity;
		DateTimeFormatter myFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss.SSSSSS");
		this.timestamp = LocalDateTime.parse(timestamp, myFormatter);
		this.o1 = o1;
	}
	
	public Event(String identifier, String activity, String timestamp, Order o1, Order o2, String tradeIdentifier){
		this(identifier, activity, timestamp, o1);
		this.o2 = o2;
		this.tradeIdentifier = tradeIdentifier;
	}
	
	public Event(String identifier, String activity, LocalDateTime timestamp, Order o1){
		this.identifier = identifier;
		this.activity = activity;
		this.timestamp = timestamp;
		this.o1 = o1;
	}
	
	public Event(String identifier, String activity, LocalDateTime timestamp, Order o1, Order o2, String tradeIdentifier){
		this(identifier, activity, timestamp, o1);
		this.o2 = o2;
		this.tradeIdentifier = tradeIdentifier;
	}
	
	public String getTradeIdentifier(){
		return tradeIdentifier;
	}
	
	public String getIdentifier(){
		return identifier;
	}
	
	public Order getOrder1(){
		return o1;
	}
	
	public Order getOrder2(){
		return o2;
	}
	
	public String getActivity(){
		return activity;
	}
	
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	
	@Override
	public String toString(){
		DateTimeFormatter myFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss.SSSSSS");
		if(o2 == null){
			return identifier + "," + activity + "," + timestamp.format(myFormatter) + "," + o1.getIdentifier() + "," + o1.getCurrentState() +
					"," + o1.getCurrentSize() + "," + o1.getSide();
		}else{
			return identifier + "," + activity + "," + timestamp.format(myFormatter) + "," + o1.getIdentifier() + "," + o1.getCurrentState() +
					"," + o1.getCurrentSize() + "," + o1.getPrice() + "," + o1.getSide() + "," + o2.getIdentifier() + "," + o2.getCurrentState() +
					"," + o2.getCurrentSize() + "," + o1.getPrice() + "," + o2.getSide() + "," + tradeIdentifier;
		}
	}
}