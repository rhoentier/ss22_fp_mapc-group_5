package massim.javaagents.agents;

import java.util.HashSet;

import massim.javaagents.map.Vector2D;

public final class NextMessageUtil {

	private static HashSet<NextMessage> messageStore = new HashSet<NextMessage>();

	public static HashSet<NextMessage> getMessageStore() {
		return messageStore;
	}

	public static void setMessageStore(HashSet<NextMessage> messageStore) {
		NextMessageUtil.messageStore = messageStore;
	}
	
	public static void addToMessageStore(NextMessage message)
	{
		messageStore.add(message);
	}
	
	public static void removeFromMessageStore(NextMessage message)
	{
		messageStore.remove(message);
	}
	
	public static void clearMessageStore()
	{
		messageStore.clear();
	}
	
	public static void addSpecificMessageToStore(String message, String senderAgent, String targetAgent)
	{
		NextMessage nextMessage = new NextMessage(message, senderAgent, targetAgent);
		if(!isMessageInStoreWithoutPosition(nextMessage))
		{
			messageStore.add(nextMessage);
		}
	}
	
	public static void addSpecificMessageToStore(String message, String senderAgent, String targetAgent, Vector2D position)
	{
		NextMessage nextMessage = new NextMessage(message, senderAgent, targetAgent, position);
		if(!isMessageInStoreWithoutPosition(nextMessage))
		{			
			messageStore.add(nextMessage);
		}
	}
	
	public static  NextMessage getMessageFromAgent(String agentName, String message)
	{
		for(NextMessage nextMessage : messageStore)
		{
			if(nextMessage.getTargetAgent().contains(agentName) && nextMessage.getMessage().contains(message))
			{
				return nextMessage;
			}
		}
		
		return null;
	}
	
	private static Boolean isMessageInStoreWithoutPosition(NextMessage message)
	{
		for(NextMessage nextMessage : messageStore)
		{
			if(nextMessage.equals(message)) return true;
		}
		return false;
	}
	
	private static Boolean isMessageInStoreWithPosition(NextMessage message)
	{
		for(NextMessage nextMessage : messageStore)
		{
			if(nextMessage.equalsWithPosition(message)) return true;
		}
		return false;
	}
}
