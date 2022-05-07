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
    private HashSet<List<Parameter>> surveyedAgents;
    private HashSet<List<Parameter>> surveyedThings;

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
                        simStatus.SetFlagSimulationIsStarted();
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
                            simStatus.SetTeamSize(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - teamSize: \n" + e.toString());
                        }
                    }
                    case "steps" -> {
                        try {
                            simStatus.SetTotalSteps(Integer.parseInt(percept.getParameters().get(0).toProlog()));
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
                        simStatus.SetFlagSimulationIsFinished();
                    }
                    case "ranking" -> {
                        try {
                            simStatus.SetRanking(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - ranking: \n" + e.toString());
                        }
                    }
                    case "score" -> {
                        try {
                            simStatus.SetScore(Integer.parseInt(percept.getParameters().get(0).toProlog()));
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
                            simStatus.SetActionID(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - actionID: \n" + e.toString());
                        }
                    }
                    case "timestamp" -> {
                        try {
                            simStatus.SetTimestamp(Long.parseLong(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - timestamp: \n" + e.toString());
                        }
                    }
                    case "deadline" -> {
                        try {
                            simStatus.SetDeadline(Long.parseLong(percept.getParameters().get(0).toProlog()));
                        } catch (Exception e) {
                            agent.say("Error in NextPerceptReader - evaluate - deadline: \n" + e.toString());
                        }
                    }
                    case "step" -> {
                        try {
                            simStatus.SetActualStep(Integer.parseInt(percept.getParameters().get(0).toProlog()));
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
                                surveyedAgents.add(percept.getParameters());
                            }
                            // Surveyed Thing
                            if (percept.getParameters().size() == 2) {
                                surveyedThings.add(percept.getParameters());
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
        //clearing of the containers before processing of percepts
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
        surveyedAgents = new HashSet<>();
        surveyedThings = new HashSet<>();

    }

    private void convertGeneratedSets() {

        //Process all Datasets and transfer to Storage - AgentStatus
        
        // processTasksSet();
        // processNormsSet();
        // processRolesSet();
        simStatus.setViolations(processViolationsSet());

        
        agentStatus.SetAttachedElements(processAttachedSet());
        
        agentStatus.SetVision(processThingsSet());
        agentStatus.SetObstacles(processObstaclesSet());
        agentStatus.SetGoalZones(processGoalZonesSet());
        agentStatus.SetRoleZones(processRoleZonesSet());
        agentStatus.SetHits(processHitsSet());

        processSurveyedAgentSet(); // Need a Target to store the Data
        processSurveyedThingSet(); // Need a Target to store the Data
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
        for (List<Parameter> object : things) {
            try {
                switch (object.size()) {
                    case 3 -> {
                        processedThingsSet.add(new MapTile(
                                Integer.parseInt(object.get(0).toProlog()),
                                Integer.parseInt(object.get(1).toProlog()),
                                simStatus.GetActualStep(),
                                object.get(2).toString()
                        ));
                    }
                    case 4 -> {
                        processedThingsSet.add(new MapTile(
                                Integer.parseInt(object.get(0).toProlog()),
                                Integer.parseInt(object.get(1).toProlog()),
                                simStatus.GetActualStep(),
                                (object.get(2).toString() + "-" + object.get(3).toString())
                        ));
                    }
                    default ->
                        agent.say("Error in NextPerceptReader - processThingsSet: Inadequate number of attributes");

                }
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

    private HashSet<MapTile> processObstaclesSet() {
        // thing(x, y, type, details) - Percept Data Format
        HashSet<MapTile> processedObstacles = new HashSet<>();
        // Converts Percept Data to goalZone MapTiles
        for (List<Parameter> object : obstacles) {
            try {
                processedObstacles.add(new MapTile(
                        Integer.parseInt(object.get(0).toProlog()),
                        Integer.parseInt(object.get(1).toProlog()),
                        agent.getSimulationStatus().GetActualStep(),
                        "obstacle"));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processObstaclesSet: \n" + e.toString());
            }

        }

        /* Debug Helper 
        if (!processedObstacles.isEmpty()) {
            agent.say("\n" + "Obstacles\n" + processedObstacles.toString() + "\n");
        }
        //*/
        return processedObstacles;

    }

    private HashSet<String> processViolationsSet() {
        // violation(id) - Percept Data Format
        // Forwards Percept Data as String
       
        /* Debug Helper 
        if (!violations.isEmpty()) {
            agent.say("\n" + "Violations \n" + violations.toString() + "\n");
        }
        //*/
        return violations;
        
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
                        agent.getSimulationStatus().GetActualStep(),
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
                        agent.getSimulationStatus().GetActualStep(),
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
                        agent.getSimulationStatus().GetActualStep(),
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

    
    private HashSet<String[]> processSurveyedAgentSet() {
        //surveyed("agent", name, role, energy)
        // name : Identifier
        // role : Identifier
        // energy : Numeral

        HashSet<String[]> processedSurveyedAgents = new HashSet<>();
        // Converts Percept Data to Target Data
        for (List<Parameter> SurveyedAgent : surveyedAgents) {
            try {
                processedSurveyedAgents.add(
                        new String[]{
                            SurveyedAgent.get(0).toProlog(),
                            SurveyedAgent.get(1).toProlog(),
                            SurveyedAgent.get(1).toProlog()}
                );
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processSurveyedAgentSet: \n" + e.toString());
            }
        }
        /* Debug Helper 
        if (!processedSurveyedAgents.isEmpty()) {
            agent.say("\n" + "Surveyed Agents \n" + processedSurveyedAgents.toString() + "\n");
        }
        //*/
        return processedSurveyedAgents;

    }

    private HashSet<String[]> processSurveyedThingSet() {
        
        // surveyed("dispenser"/"goal"/"role", distance)
        HashSet<String[]> processedSurveyedThings = new HashSet<>();
        // Converts Percept Data to Target Data
        for (List<Parameter> SurveyedAgent : surveyedThings) {
            try {
                processedSurveyedThings.add(
                        new String[]{
                            SurveyedAgent.get(0).toProlog(),
                            SurveyedAgent.get(1).toProlog()}
                );
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processSurveyedThingSet: \n" + e.toString());
            }
        }
        /* Debug Helper 
        if (!processedSurveyedThings.isEmpty()) {
            agent.say("\n" + "Distance to Surveyed Things \n" + processedSurveyedThings.toString() + "\n");
        }
        //*/
        return processedSurveyedThings;
    }
}
