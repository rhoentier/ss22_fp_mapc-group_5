package massim.javaagents.agents;

import eis.iilang.Percept;
import massim.javaagents.MailService;

import java.util.*;

/**
 * An abstract Java agent.
 */
public abstract class Agent {

    private final String name;
    private final MailService mailbox;
    private final Set<Percept> percepts = Collections.synchronizedSet(new HashSet<>());

    /**
     * Constructor
     * @param name the agent's name
     * @param mailbox the mail facility
     */
    Agent(String name, MailService mailbox){
        this.name = name;
        this.mailbox = mailbox;
    }

    /**
     * Handles a percept.
     * This method is used only if the EIS is configured to handle percepts as notifications.
     * @param percept the percept to process
     */
    public abstract void HandlePercept(Percept percept);

    /**
     * Called for each step.
     */
    public abstract eis.iilang.Action Step();

    /**
     * @return the name of the agent
     */
    public String GetName() {
        return name;
    }

    /**
     * Sends a percept as a message to the given agent.
     * The receiver agent may fetch the message the next time it is stepped.
     * @param message the message to deliver
     * @param receiver the receiving agent
     * @param sender the agent sending the message
     */
    protected void SendMessage(Percept message, String receiver, String sender){
        mailbox.SendMessage(message, receiver, sender);
    }

    /**
     * Broadcasts a message to the entire team.
     * @param message the message to broadcast
     * @param sender the agent sending the message
     */
    public void Broadcast(Percept message, String sender){
        mailbox.Broadcast(message, sender);
    }

    /**
     * Called if another agent sent a message to this agent; so technically this is part of another agent's step method.
     *
     * @param message the message that was sent
     * @param sender name of the agent who sent the message
     */
    public abstract void HandleMessage(Percept message, String sender);

    /**
     * Sets the percepts for this agent. Should only be called from the outside.
     * @param addList the new percepts for this agent.
     * @param delList the now invalid percepts for this agent.
     */
    public void SetPercepts(List<Percept> addList, List<Percept> delList) {
        this.percepts.removeAll(delList);
        this.percepts.addAll(addList);
    }

    /**
     * Prints a message to std out prefixed with the agent's name.
     * @param message the message to say
     */
    public void Say(String message){
        System.out.println("[ " + name + " ]  " + message);
    }

    /**
     * Returns a list of this agent's percepts. Percepts are set by the scheduler
     * each time before the step() method is called.
     * Percepts are cleared before each step, so relevant information needs to be stored somewhere else
     * by the agent.
     * @return a list of all new percepts for the current step
     */
    public List<Percept> GetPercepts(){
        return new ArrayList<>(percepts);
    }
}
