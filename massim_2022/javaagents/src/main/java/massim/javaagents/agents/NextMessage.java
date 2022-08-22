package massim.javaagents.agents;

import massim.javaagents.map.Vector2D;

public class NextMessage {

	private String message;
	private String senderAgent;
	private String targetAgent;
	private boolean hasMessage;
	private Vector2D position;
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public void newMessage(String message, String senderAgent, String receiverAgent)
	{
		this.message = message;
		this.senderAgent = senderAgent;
		this.targetAgent = receiverAgent;
		this.hasMessage = true;
	}
	
	public void newMessage(String message, String senderAgent, String receiverAgent, Vector2D position)
	{
		this.message = message;
		this.senderAgent = senderAgent;
		this.targetAgent = receiverAgent;
		this.hasMessage = true;
		this.position = position;
	}
	
	public void clearMessage() {
		this.message = "";
		this.senderAgent = "";
		this.targetAgent = "";
		this.hasMessage = false;
		this.position = new Vector2D(0,0);
	}

	public String getSenderAgent() {
		return senderAgent;
	}

	public void setSenderAgent(String senderAgent) {
		this.senderAgent = senderAgent;
	}

	public String getTargetAgent() {
		return targetAgent;
	}

	public void setTargetAgent(String targetAgent) {
		this.targetAgent = targetAgent;
	}

	public boolean hasMessage() {
		return hasMessage;
	}

	public void hasMessage(boolean hasMessage) {
		this.hasMessage = hasMessage;
	}

	public Vector2D getPosition() {
		return position;
	}

	public void setPosition(Vector2D position) {
		this.position = position;
	}
}
