package massim.javaagents.agents;

import eis.iilang.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The basic Interpreter of Server Communication Protocoll
 *
 * Done: Handling of all documented Percepts is implemented ToDo: The conversion
 * of sets into the target format + external saving
 *
 * @author Alexander Lorenz
 */
public class NextPerceptReader {

    private NextAgent agent;
    private SimulationStatus simStatus;
    private AgentStatus agentStatus;

    private HashSet<List<Parameter>> tasks;
    private HashSet<List<Parameter>> roles;
    private HashSet<List<Parameter>> norms;
    private HashSet<List<Parameter>> attached;  // TODO: Convert to List<Point>
    private HashSet<List<Parameter>> things;
    private HashSet<List<Parameter>> obstacles;
    private HashSet<List<Parameter>> hits;
    private HashSet<String> violations;
    private HashSet<List<Parameter>> surveyedAgent;
    private HashSet<List<Parameter>> surveyedThing;

    private HashSet<String> overhangNames = new HashSet<>(); // Noch nicht bearbeitete Attribute
    private HashSet<List<Parameter>> goalZones; // TODO: Convert to List<Position>    
    private HashSet<List<Parameter>> roleZones; // TODO: Convert to List<Position>

    public NextPerceptReader(NextAgent agent) {
        this.agent = agent;
        simStatus = agent.getSimulationStatus();
        agentStatus = agent.getStatus();

        clearSets();

    }

    //Frage an das Team: Sollen mögliche Fehler innerhalb der Switch abfrage abgefangen werden.
    // Nachteil: BoilerCode + Performance
    void evaluate(List<Percept> percepts) {

        clearSets();

        //WARNING: ConcurrentModificationException workaround! based on FitBUT
        synchronized (percepts) {

            for (Percept percept : percepts) {

                switch (percept.getName()) {

                    // - SimulationStart Message
                    case "simStart" -> {
                        simStatus.setFlagSimulationIsStarted();
                    }

                    case "name" ->
                        agentStatus.setName(percept.getParameters().get(0).toProlog());
                    case "team" ->
                        agentStatus.setTeam(percept.getParameters().get(0).toProlog());
                    case "teamSize" ->
                        simStatus.setTeamSize(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                    case "steps" ->
                        simStatus.setTotalSteps(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                    case "role" -> {
                        // List of roles in simulation
                        // role(name, vision, [action1, action2, ...], [speed1, speed2, ...], clearChance, clearMaxDistance)
                        if (percept.getParameters().size() > 1) {
                            roles.add(percept.getParameters());
                        } else {
                            // Actual role
                            agentStatus.setRole(percept.getParameters().get(0).toProlog());
                        }
                    }

                    // - SimulationEnd Message
                    case "simEnd" -> {
                        simStatus.setFlagSimulationIsFinished();
                    }
                    case "ranking" ->
                        simStatus.setRanking(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                    case "score" ->
                        simStatus.setScore(Integer.parseInt(percept.getParameters().get(0).toProlog()));

                    // - SimulationIsFinished Message
                    case "bye" ->
                        // is called, when Simulation is finished.
                        agent.setFlagDeactivateAgent();

                    // - Request Action Perceipts
                    case "requestAction" ->
                        agent.setFlagActionRequest();

                    case "actionID" ->
                        simStatus.setActionID(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                    case "timestamp" ->
                        simStatus.setTimestamp(Long.parseLong(percept.getParameters().get(0).toProlog()));
                    case "deadline" ->
                        simStatus.setDeadline(Long.parseLong(percept.getParameters().get(0).toProlog()));
                    case "step" ->
                        simStatus.setActualStep(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                    case "lastAction" ->
                        agentStatus.setlastAction(percept.getParameters().get(0).toProlog());
                    case "lastActionResult" ->
                        agentStatus.setLastActionResult(percept.getParameters().get(0).toProlog());
                    case "lastActionParams" -> // has to be adjusted to a List if used/needed
                        agentStatus.setLastActionParams(percept.getParameters().get(0).toProlog());

                    // find Score @SimEnd messages  
                    case "thing" -> {
                        // Dividing in a sublists obstacles and things 
                        if (percept.getParameters().get(2).toProlog().equals("obstacle")) {
                            obstacles.add(percept.getParameters());
                            continue;
                        }
                        things.add(percept.getParameters());
                    }
                    case "task" ->
                        tasks.add(percept.getParameters());
                    case "attached" ->
                        attached.add(percept.getParameters());
                    case "energy" ->
                        agentStatus.setEnergy(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                    case "deactivated" ->
                        agentStatus.setDeactivatedFlag(percept.getParameters().get(0).toProlog().equals("true"));
                    case "roleZone" ->
                        roleZones.add(percept.getParameters());
                    case "goalZone" ->
                        goalZones.add(percept.getParameters());
                    case "violation" ->
                        violations.add(percept.getParameters().get(0).toProlog());
                    case "norm" ->
                        norms.add(percept.getParameters());
                    case "hit" ->
                        hits.add(percept.getParameters());
                    case "surveyed" -> {
                        // Surveyed Agent
                        if (percept.getParameters().size() == 4) {
                            surveyedAgent.add(percept.getParameters());
                        }
                        // Surveyed Thing
                        if (percept.getParameters().size() == 2) {
                            surveyedThing.add(percept.getParameters());
                        }
                        //agent.say("Surveyed reading: \n" + percept.getName().toString() + ": \n" + percept.getParameters());
                    }
                    default -> //overhang.add(percept.getParameters());
                    {
                        overhangNames.add(percept.getName());
                        agent.say("Reading: \n" + percept.getName().toString() + ": \n" + percept.getParameters());
                    }

                }
                /*
                agent.say("attached \n" + attached.toString());
                agent.say("------------------------------------------------");
                agent.say("goals \n" + tasks.toString());
                agent.say("------------------------------------------------");
                agent.say("things \n" + things.toString());
                agent.say("------------------------------------------------");
                agent.say("obstacles \n" + obstacles.toString());
                agent.say("------------------------------------------------");
                agent.say("norms \n" + norms.toString());
                agent.say("------------------------------------------------");
                agent.say("roles \n" + roles.toString());
                agent.say("------------------------------------------------");
                agent.say("roleZones \n" + roleZones.toString());
                agent.say("------------------------------------------------");
                agent.say("GoalZones \n" + goalZones.toString());
                agent.say("------------------------------------------------");
                agent.say("------------------------------------------------");
                agent.say("------------------------------------------------");
                agent.say("hits \n" + hits.toString());
                agent.say("------------------------------------------------");
                 */
                // handling of unusual perception entries

                if (!tasks.isEmpty()) {
                    //  agent.say("Tasks \n" + tasks.toString());
                }

                if (!overhangNames.isEmpty()) {
                    agent.say("------------------------------------------------");
                    agent.say("overhang: \n" + overhangNames.toString());
                    agent.say("------------------------------------------------");
                }

                convertGeneratedSets();

                // Clearing of perception list after processing of the Data.
                // has to be moved here. At the moment it is handled in Next Agent to keep Action generation active.  
                // this.agent.setPercepts(new ArrayList<>(), percepts);
            }
        }
    }

    private void clearSets() {
        
        attached = new HashSet<>();
        tasks = new HashSet<>();
        norms = new HashSet<>();
        roles = new HashSet<>();
        things = new HashSet<>();
        obstacles = new HashSet<>();
        violations = new HashSet<>();
        goalZones = new HashSet<>();
        roleZones = new HashSet<>();
        overhangNames = new HashSet<>();
        hits = new HashSet<>();
        surveyedAgent = new HashSet<>();
        surveyedThing = new HashSet<>();

    }

    private void convertGeneratedSets() {
        
        //if (!overhangNames.isEmpty()) {
        
        //}

        agent.getStatus().setAttachedElements(processAttachedSet()); // TODO: Buggy ?
        // processTasksSet();
        // processNormsSet();
        // processRolesSet();
        // processThingsSet();
        // processObstaclesSet();
        // processViolationsSet();
        processGoalZonesSet();
        processRoleZonesSet();
        processHitsSet();

        // processSurveyedAgentSet();
        // processSurveyedThingSet();
    }

    private HashSet<Point> processAttachedSet() {
        // implement the Definition of the entity, by aquiring Data from Things set.
        HashSet<Point> processedAttachedSet = new HashSet<>();
        for (List<Parameter> zone : attached) {
            processedAttachedSet.add(new Point(
                    Integer.parseInt(zone.get(0).toProlog()),
                    Integer.parseInt(zone.get(1).toProlog())
            ));
        }
        if (!processedAttachedSet.isEmpty()) {
            agent.say("\n" + "Attached \n" + processedAttachedSet.toString() + "\n");
        }
        return processedAttachedSet;
    }

    private void processTasksSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void processNormsSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void processRolesSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void processThingsSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void processObstaclesSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void processViolationsSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void processGoalZonesSet() {
        // ToDo: Transfer into Main Data Warehouse
        HashSet<MapTile> processedGoalZones = new HashSet<>();
        for (List<Parameter> zone : goalZones) {
            processedGoalZones.add(new MapTile(
                    Integer.parseInt(zone.get(0).toProlog()),
                    Integer.parseInt(zone.get(1).toProlog()),
                    agent.getSimulationStatus().getActualStep(),
                    "goalZone"));
        }
        if (!processedGoalZones.isEmpty()) {
            //  agent.say("\n"+"GoalZones \n" + processedGoalZones.toString() + "\n");
        }
    }

    private void processRoleZonesSet() {
        // ToDo: Transfer into Main Data Warehouse
        HashSet<MapTile> processedRoleZones = new HashSet<>();
        for (List<Parameter> zone : roleZones) {
            processedRoleZones.add(new MapTile(
                    Integer.parseInt(zone.get(0).toProlog()),
                    Integer.parseInt(zone.get(1).toProlog()),
                    agent.getSimulationStatus().getActualStep(),
                    "roleZone"));
        }
        if (!processedRoleZones.isEmpty()) {
            //  agent.say("\n"+"RoleZones \n" + processedRoleZones.toString() + "\n");
        }
    }

    private void processHitsSet() {
        // ToDo: Transfer into Main Data Warehouse
        HashSet<MapTile> processedHits = new HashSet<>();
        for (List<Parameter> zone : roleZones) {
            processedHits.add(new MapTile(
                    Integer.parseInt(zone.get(0).toProlog()),
                    Integer.parseInt(zone.get(1).toProlog()),
                    agent.getSimulationStatus().getActualStep(),
                    "Hit"));
        }
        if (!processedHits.isEmpty()) {
            // agent.say("\n" + "Hits \n" + processedHits.toString() + "\n");
        }
    }

    private void processSurveyedAgentSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void processSurveyedThingSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
