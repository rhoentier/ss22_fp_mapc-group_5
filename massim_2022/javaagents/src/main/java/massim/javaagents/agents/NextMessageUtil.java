package massim.javaagents.agents;

import java.util.HashSet;

import massim.javaagents.map.Vector2D;

public final class NextMessageUtil {

    private static HashSet<NextMessage> messageStore = new HashSet<NextMessage>();

    public static HashSet<NextMessage> GetMessageStore() {
        return messageStore;
    }

    public static void SetMessageStore(HashSet<NextMessage> messageStore) {
        NextMessageUtil.messageStore = messageStore;
    }

    public static void AddToMessageStore(NextMessage message) {
        messageStore.add(message);
    }

    public static void RemoveFromMessageStore(NextMessage message) {
        messageStore.remove(message);
    }

    public static void ClearMessageStore() {
        messageStore.clear();
    }

    public static void AddSpecificMessageToStore(String message, String senderAgent, String targetAgent) {
        NextMessage nextMessage = new NextMessage(message, senderAgent, targetAgent);
        if (!isMessageInStoreWithoutPosition(nextMessage)) {
            messageStore.add(nextMessage);
        }
    }

    public static void AddSpecificMessageToStore(String message, String senderAgent, String targetAgent, Vector2D position) {
        NextMessage nextMessage = new NextMessage(message, senderAgent, targetAgent, position);
        if (!isMessageInStoreWithoutPosition(nextMessage)) {
            messageStore.add(nextMessage);
        }
    }

    public static NextMessage GetMessageFromAgent(String agentName, String message) {
        for (NextMessage nextMessage : messageStore) {
            if (nextMessage.GetTargetAgent().contains(agentName) && nextMessage.GetMessage().contains(message)) {
                return nextMessage;
            }
        }

        return null;
    }

    private static Boolean isMessageInStoreWithoutPosition(NextMessage message) {
        for (NextMessage nextMessage : messageStore) {
            if (nextMessage.equals(message)) {
                return true;
            }
        }
        return false;
    }

    private static Boolean isMessageInStoreWithPosition(NextMessage message) {
        for (NextMessage nextMessage : messageStore) {
            if (nextMessage.EqualsWithPosition(message)) {
                return true;
            }
        }
        return false;
    }
}
