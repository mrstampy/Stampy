package asia.stampy.client.message.subscribe;

import asia.stampy.client.message.AbstractClientMessageHeader;

public class SubscribeHeader extends AbstractClientMessageHeader {
	private static final long serialVersionUID = 2321658220170938363L;
	
	public static final String ID = "id";
	public static final String ACK = "ack";
	public static final String DESTINATION = "destination";
	
	public enum Ack {
		auto("auto"),
		client("client"),
		clientIndividual("client-individual");
		
		String ackValue;
		Ack(String ackValue) {
			this.ackValue = ackValue;
		}
		
		public String getAckValue() {
			return ackValue;
		}
		
		public static Ack fromString(String s) {
			for(Ack ack : Ack.values()) {
				if(ack.getAckValue().equals(s)) {
					return ack;
				}
			}
			
			return null;
		}
	}

	public void setDestination(String destination) {
		addHeader(DESTINATION, destination);
	}
	
	public String getDestination() {
		return getHeaderValue(DESTINATION);
	}
	
	public void setAck(Ack ack) {
		addHeader(ACK, ack.getAckValue());
	}
	
	public Ack getAck() {
		String s = getHeaderValue(ACK);
		if(s == null) return null;
		
		return Ack.fromString(s);
	}
	
	public void setId(String id) {
		addHeader(ID, id);
	}
	
	public String getId() {
		return getHeaderValue(ID);
	}

}
