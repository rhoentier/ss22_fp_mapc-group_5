package massim.javaagents;

import eis.iilang.Percept;
import massim.javaagents.agents.Agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * A simple register for agents that forwards messages.
 */
public class MailService {

    private Map<String, Agent> register = new HashMap<>();
    private Map<String, List<Agent>> agentsByTeam = new HashMap<>();
    private Map<String, String> teamForAgent = new HashMap<>();
    private Logger logger = Logger.getLogger("agents");

    /**
     * Registers an agent with this mail service. The agent will now receive messages.
     * @param agent the agent to register
     * @param team the agent's team (needed for broadcasts)
     */
    public void RegisterAgent(Agent agent, String team){
        register.put(agent.GetName(), agent);
        agentsByTeam.putIfAbsent(team, new Vector<>());
        agentsByTeam.get(team).add(agent);
        teamForAgent.put(agent.GetName(), team);
    }

    /**
     * Adds a message to this mailbox.
     * @param message the message to add
     * @param to the receiving agent
     * @param from the agent sending the message
     */
    public void SendMessage(Percept message, String to, String from){

        Agent recipient = register.get(to);

        if(recipient == null) {
            logger.warning("Cannot deliver message to " + to + "; unknown target,");
        }
        else{
            recipient.HandleMessage(message, from);
        }
    }

    /**
     * Sends a message to all agents of the sender's team (except the sender).
     * @param message the message to broadcast
     * @param sender the sending agent
     */
    public void Broadcast(Percept message, String sender) {
        agentsByTeam.get(teamForAgent.get(sender)).stream()
                .map(Agent::GetName)
                .filter(ag -> !ag.equals(sender))
                .forEach(ag -> SendMessage(message, ag, sender));
    }
}