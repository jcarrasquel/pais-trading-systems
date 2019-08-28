package model;
import java.time.LocalDateTime;

public class Trade {

	protected String identifier;
	
	protected LocalDateTime timestamp;
	
	protected String order1Identifier; // buy order
	
	protected String order2Identifier; // sell order
	
	protected LocalDateTime order1ArrivalTime;
	
	protected LocalDateTime order2ArrivalTime;
	
	protected Integer quantity;
	
	public Trade(String identifier, LocalDateTime timestamp, String order1Identifier, String order2Identifier, Integer quantity, LocalDateTime order1ArrivalTime, LocalDateTime order2ArrivalTime){
		this.identifier = identifier;
		this.timestamp = timestamp;
		this.order1Identifier = order1Identifier;
		this.order2Identifier = order2Identifier;
		this.quantity = quantity;
		this.order1ArrivalTime = order1ArrivalTime;
		this.order2ArrivalTime = order2ArrivalTime;
	}
	
	public LocalDateTime getOrder1ArrivalTime(){
		return order1ArrivalTime;
	}
	
	public LocalDateTime getOrder2ArrivalTime(){
		return order2ArrivalTime;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getOrder1Identifier() {
		return order1Identifier;
	}

	public String getOrder2Identifier() {
		return order2Identifier;
	}

	public Integer getQuantity() {
		return quantity;
	}
	
}
