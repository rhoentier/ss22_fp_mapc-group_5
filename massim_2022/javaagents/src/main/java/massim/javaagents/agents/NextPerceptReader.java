package massim.javaagents.agents;

import eis.iilang.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The basic Interpreter of Server Communication Protocoll
 *
 * Handling of all documented Percepts, The conversion of sets into the target
 * format + saving in external Data Storage
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
        this.simStatus = agent.getSimulationStatus();
        this.agentStatus = agent.getStatus();

        clearSets();
    }

    //Frage an das Team: Sollen mögliche Fehler innerhalb der Switch abfrage abgefangen werden.
    // Nachteil: BoilerCode + Performance
    void evaluate(List<Percept> percepts, NextAgent agent) {

        clearSets(); //clearing of the containers before processing of perceipts
        
        //WARNING: ConcurrentModificationException workaround! based on FitBUT
        synchronized (percepts) {

            for (Percept percept : percepts) {

                switch (percept.getName()) {

                    // - SimulationStart Messages
                    case "simStart" -> {
                        simStatus.setFlagSimulationIsStarted();
                    }

                    case "name" -> {
                        try {
                            agentStatus.SetName(percept.getParameters().get(0).toProlog());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - name: \n" + e.toString());
                        }
                    }
                    case "team" -> {
                        try {
                            agentStatus.SetTeam(percept.getParameters().get(0).toProlog());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - team: \n" + e.toString());
                        }
                    }
                    case "teamSize" -> {
                        try {
                            simStatus.setTeamSize(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - teamSize: \n" + e.toString());
                        }
                    }
                    case "steps" -> {
                        try {
                            simStatus.setTotalSteps(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - steps: \n" + e.toString());
                        }
                    }
                    case "role" -> {
                        try {
                            // List of roles in simulation
                            // role(name, vision, [action1, action2, ...], [speed1, speed2, ...], clearChance, clearMaxDistance)
                            if (percept.getParameters().size() > 1) {
                                roles.add(percept.getParameters());
                            } else {
                                // Actual role
                                agentStatus.SetRole(percept.getParameters().get(0).toProlog());
                            }
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - role: \n" + e.toString());
                        }
                    }

                    // - SimulationEnd Messages
                    case "simEnd" -> {
                        simStatus.setFlagSimulationIsFinished();
                    }
                    case "ranking" -> {
                        try {
                            simStatus.setRanking(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - ranking: \n" + e.toString());
                        }
                    }
                    case "score" -> {
                        try {
                            simStatus.setScore(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - score: \n" + e.toString());
                        }
                    }

                    // - AllSimulationsAreFinished Message
                    case "bye" ->
                        // is called, when last Simulation is finished.
                        agent.setFlagDisableAgent();

                    // - Request Action Messages
                    case "requestAction" ->
                        agent.setFlagActionRequest();

                    case "actionID" -> {
                        try {
                            simStatus.setActionID(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - actionID: \n" + e.toString());
                        }
                    }
                    case "timestamp" -> {
                        try {
                            simStatus.setTimestamp(Long.parseLong(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - timestamp: \n" + e.toString());
                        }
                    }
                    case "deadline" -> {
                        try {
                            simStatus.setDeadline(Long.parseLong(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - deadline: \n" + e.toString());
                        }
                    }
                    case "step" -> {
                        try {
                            simStatus.setActualStep(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - step: \n" + e.toString());
                        }
                    }
                    case "lastAction" -> {
                        try {
                            agentStatus.SetLastAction(percept.getParameters().get(0).toProlog());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - lastAction: \n" + e.toString());
                        }
                    }
                    case "lastActionResult" -> {
                        try {
                            agentStatus.SetLastActionResult(percept.getParameters().get(0).toProlog());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - lastActionResult: \n" + e.toString());
                        }
                    }
                    case "lastActionParams" -> {
                        try {
                            // has to be adjusted to a List if used/needed
                            agentStatus.SetLastActionParams(percept.getParameters().get(0).toProlog());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - lastActionParams: \n" + e.toString());
                        }
                    }

                    // The "Score" percept is handled together with @SimEnd messages above
                    case "thing" -> {
                        try {
                            // Dividing in two sublists obstacles and things 
                            if (percept.getParameters().get(2).toProlog().equals("obstacle")) {
                                obstacles.add(percept.getParameters());
                                continue;
                            }
                            things.add(percept.getParameters());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - Thing: \n" + e.toString());
                        }

                    }
                    case "task" -> {
                        try {
                            tasks.add(percept.getParameters());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - task: \n" + e.toString());
                        }
                    }
                    case "attached" -> {
                        try {
                            attached.add(percept.getParameters());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - attached: \n" + e.toString());
                        }
                    }
                    case "energy" -> {
                        try {
                            agentStatus.SetEnergy(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - energy: \n" + e.toString());
                        }
                    }
                    case "deactivated" -> {
                        try {
                            agentStatus.SetDeactivatedFlag(percept.getParameters().get(0).toProlog().equals("true"));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - deactivated: \n" + e.toString());
                        }
                    }
                    case "roleZone" -> {
                        try {
                            roleZones.add(percept.getParameters());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - roleZone: \n" + e.toString());
                        }
                    }
                    case "goalZone" -> {
                        try {
                            goalZones.add(percept.getParameters());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - goalZone: \n" + e.toString());
                        }
                    }
                    case "violation" -> {
                        try {
                            violations.add(percept.getParameters().get(0).toProlog());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - violation: \n" + e.toString());
                        }
                    }
                    case "norm" -> {
                        try {
                            norms.add(percept.getParameters());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - norm: \n" + e.toString());
                        }
                    }
                    case "hit" -> {
                        try {
                            hits.add(percept.getParameters());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - hit: \n" + e.toString());
                        }
                    }
                    case "surveyed" -> {
                        // Dividing in two sublists handling Surveyed Agents and Surveyed Things 
                        try {
                            // Surveyed Agent
                            if (percept.getParameters().size() == 4) {
                                surveyedAgent.add(percept.getParameters());
                            }
                            // Surveyed Thing
                            if (percept.getParameters().size() == 2) {
                                surveyedThing.add(percept.getParameters());
                            }
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - lastActionParams: \n" + e.toString());
                        }
                    }

                    default -> //All not processed perceipts are moved to the Overhang List
                       {
                        try {
                            overhangNames.add(percept.getName());
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - Default Overhang Messages : \n" + e.toString());
                        }
                    }

                }
            }

            // handling of unusual perception entries
            if (!overhangNames.isEmpty()) {
                agent.say("------------------------------------------------");
                agent.say("WARNING! overhang \n" + overhangNames.toString() + "\n detected");
                agent.say("------------------------------------------------");
            }

            //Second Step of Processing of Sets
            convertGeneratedSets();

        }
    }

    private void clearSets() {
        //clearing of the containers before processing of perceipts
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

        //Process all Datasets and transfer to AgentStatus
        agentStatus.SetAttachedElements(processAttachedSet());
        // processTasksSet();
        // processNormsSet();
        // processRolesSet();

        // processObstaclesSet();
        agentStatus.SetVision(processThingsSet());
        // processViolationsSet();
        agentStatus.SetGoalZones(processGoalZonesSet());
        agentStatus.SetRoleZones(processRoleZonesSet());
        agentStatus.SetHits(processHitsSet());

        // processSurveyedAgentSet();
        // processSurveyedThingSet();
    }

    private HashSet<Point> processAttachedSet() {
        // attached(x, y) - Percept Data Format
        HashSet<Point> processedAttachedSet = new HashSet<>();
        // Converts percept data to attached points. All visible attached elements are processed.
        for (List<Parameter> zone : attached) {
            try {
                processedAttachedSet.add(new Point(
                        Integer.parseInt(zone.get(0).toProlog()),
                        Integer.parseInt(zone.get(1).toProlog())
                ));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processAttachedSet: \n" + e.toString());
            }
        }
        /* Debug Helper 
        if (!processedAttachedSet.isEmpty()) {
            agent.say("\n" + "Attached Elements\n" + processedAttachedSet.toString() + "\n");
        }
        //*/
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

    private HashSet<MapTile> processThingsSet() {
        // thing(x, y, type, details) - Percept Data Format
        HashSet<MapTile> processedThingsSet = new HashSet<>();
        // Converts Percept Data to MapTile Elements
        for (List<Parameter> zone : things) {
            try {
                processedThingsSet.add(
                        new MapTile(
                                Integer.parseInt(zone.get(0).toProlog()),
                                Integer.parseInt(zone.get(1).toProlog()),
                                simStatus.getActualStep(),
                                zone.get(2).toString()
                        ));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processThingsSet: \n" + e.toString());
            }
        }

        /* Debug Helper 
        if (!processedThingsSet.isEmpty()) {
            agent.say("\n" + "Visible Things\n" + processedThingsSet.toString() + "\n");
        }
        //*/
        return processedThingsSet;
    }

    private void processObstaclesSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void processViolationsSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private HashSet<MapTile> processGoalZonesSet() {
        // goalZone(x, y) - Percept Data Format
        HashSet<MapTile> processedGoalZones = new HashSet<>();
        // Converts Percept Data to goalZone MapTiles
        for (List<Parameter> zone : goalZones) {
            try {
                processedGoalZones.add(new MapTile(
                        Integer.parseInt(zone.get(0).toProlog()),
                        Integer.parseInt(zone.get(1).toProlog()),
                        agent.getSimulationStatus().getActualStep(),
                        "goalZone"));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processGoalZonesSet: \n" + e.toString());
            }

        }

        /* Debug Helper 
        if (!processedGoalZones.isEmpty()) {
            agent.say("\n" + "Goal Zones\n" + processedGoalZones.toString() + "\n");
        }
        //*/
        return processedGoalZones;
    }

    private HashSet<MapTile> processRoleZonesSet() {
        // roleZone(x, y) - Percept Data Format
        HashSet<MapTile> processedRoleZones = new HashSet<>();
        // Converts Percept Data to roleZone MapTiles
        for (List<Parameter> zone : roleZones) {
            try {
                processedRoleZones.add(new MapTile(
                        Integer.parseInt(zone.get(0).toProlog()),
                        Integer.parseInt(zone.get(1).toProlog()),
                        agent.getSimulationStatus().getActualStep(),
                        "roleZone"));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processRoleZonesSet: \n" + e.toString());
            }
        }

        /* Debug Helper 
        if (!processedRoleZones.isEmpty()) {
            agent.say("\n" + "Role Zones \n" + processedRoleZones.toString() + "\n");
        }
        //*/
        return processedRoleZones;
    }

    private HashSet<MapTile> processHitsSet() {
        // hit(x, y) - Percept Data Format
        HashSet<MapTile> processedHits = new HashSet<>();
        // Converts Percept Data to goalZone MapTiles
        for (List<Parameter> hit : hits) {
            try {
                processedHits.add(new MapTile(
                        Integer.parseInt(hit.get(0).toProlog()),
                        Integer.parseInt(hit.get(1).toProlog()),
                        agent.getSimulationStatus().getActualStep(),
                        "Hit"));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processHitsSet: \n" + e.toString());
            }

        }

        /* Debug Helper 
        if (!processedHits.isEmpty()) {
            agent.say("\n" + "Hits \n" + processedHits.toString() + "\n");
        }
        //*/
        return processedHits;
    }

    private void processSurveyedAgentSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void processSurveyedThingSet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
