package massim.javaagents.agents;

import massim.javaagents.map.Vector2D;

public final class NextMessage {

    private String message;
    private String senderAgent;
    private String targetAgent;
    private boolean hasMessage;
    private Vector2D position;

    public String GetMessage() {
        return message;
    }

    public void SetMessage(String message) {
        this.message = message;
    }

    public NextMessage(String message, String senderAgent, String targetAgent) {
        this.message = message;
        this.senderAgent = senderAgent;
        this.targetAgent = targetAgent;
        this.hasMessage = true;
    }

    public NextMessage(String message, String senderAgent, String targetAgent, Vector2D position) {
        this.message = message;
        this.senderAgent = senderAgent;
        this.targetAgent = targetAgent;
        this.hasMessage = true;
        this.position = position;
    }

//	public void clearMessage() {
//		this.message = "";
//		this.senderAgent = "";
//		this.targetAgent = "";
//		this.HasMessage = false;
//		this.position = new Vector2D(0,0);
//	}
    public String GetSenderAgent() {
        return senderAgent;
    }

    public void SetSenderAgent(String senderAgent) {
        this.senderAgent = senderAgent;
    }

    public String GetTargetAgent() {
        return targetAgent;
    }

    public void SetTargetAgent(String targetAgent) {
        this.targetAgent = targetAgent;
    }

    public boolean HasMessage() {
        return hasMessage;
    }

    public void HasMessage(boolean hasMessage) {
        this.hasMessage = hasMessage;
    }

    public Vector2D GetPosition() {
        return position;
    }

    public void SetPosition(Vector2D position) {
        this.position = position;
    }

    public boolean EqualsWithPosition(Object o) {
        NextMessage nextMessage = (NextMessage) o;
        return this.GetMessage().equals(nextMessage.GetMessage())
                && this.GetPosition() != null && nextMessage.GetPosition() != null
                && this.GetPosition().equals(nextMessage.GetPosition())
                && this.GetSenderAgent().equals(nextMessage.GetSenderAgent())
                && this.GetTargetAgent().equals(nextMessage.GetTargetAgent());
    }

    @Override
    public boolean equals(Object o) {
        NextMessage nextMessage = (NextMessage) o;
        return this.GetMessage().equals(nextMessage.GetMessage())
                && this.GetSenderAgent().equals(nextMessage.GetSenderAgent())
                && this.GetTargetAgent().equals(nextMessage.GetTargetAgent());
    }
}
