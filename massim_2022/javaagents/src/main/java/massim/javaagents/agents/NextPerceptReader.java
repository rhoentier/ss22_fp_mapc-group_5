package massim.javaagents.agents;

import massim.javaagents.map.NextMapTile;
import eis.iilang.*;
import massim.javaagents.percept.NextNorm;
import massim.javaagents.percept.NextNormRequirement;
import massim.javaagents.percept.NextRole;
import massim.javaagents.percept.NextTask;

import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EPercepts;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import massim.javaagents.percept.NextNorm;
import massim.javaagents.percept.NextNormRequirement;
import massim.javaagents.percept.NextRole;
import massim.javaagents.percept.NextSurveyedAgent;
import massim.javaagents.percept.NextSurveyedThing;
import massim.javaagents.percept.NextTask;

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
    private NextSimulationStatus simStatus;
    private NextAgentStatus agentStatus;

    private HashSet<List<Parameter>> tasks;
    private HashSet<List<Parameter>> roles;
    private HashSet<List<Parameter>> norms;
    private HashSet<List<Parameter>> attached;
    private HashSet<List<Parameter>> things;
    private HashSet<List<Parameter>> obstacles;
    private HashSet<List<Parameter>> hits;
    private HashSet<String> violations;
    private HashSet<List<Parameter>> surveyedAgents;
    private HashSet<List<Parameter>> surveyedThings;

    private HashSet<String> overhangNames = new HashSet<>();
    private HashSet<List<Parameter>> goalZones;
    private HashSet<List<Parameter>> roleZones;

    public NextPerceptReader(NextAgent agent) {
        this.agent = agent;
        this.simStatus = agent.getSimulationStatus();
        this.agentStatus = agent.getStatus();

        clearSets();
    }

    /**
     * evaluate the percepts
     * @param percepts
     * @param agent
     */
    public void evaluate(List<Percept> percepts, NextAgent agent) {

        clearSets(); //clearing of the containers before processing of perceipts

        //WARNING: ConcurrentModificationException workaround! based on FitBUT
        synchronized (percepts) {
            for (Percept percept : percepts) {
                try {

                    switch (NextConstants.EPercepts.valueOf(percept.getName())) {
                        // - SimulationStart Messages
                        case simStart:
                            simStatus.SetFlagSimulationIsStarted();
                            break;
                        case name:
                            agentStatus.SetName(percept.getParameters().get(0).toProlog());
                            break;
                        case team:
                            agentStatus.SetTeam(percept.getParameters().get(0).toProlog());
                            break;
                        case teamSize:
                            simStatus.SetTeamSize(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                            break;
                        case steps:
                            simStatus.SetTotalSteps(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                            break;
                        case role:
                            // List of roles in simulation
                            // role(name, vision, [action1, action2, ...], [speed1, speed2, ...], clearChance, clearMaxDistance)
                            if (percept.getParameters().size() > 1) {
                                roles.add(percept.getParameters());
                            } else {
                                // Actual role
                                agentStatus.SetRole(percept.getParameters().get(0).toProlog());
                            }
                            break;
                        // - SimulationEnd Messages
                        case simEnd:
                            simStatus.SetFlagSimulationIsFinished();
                            break;
                        case ranking:
                            simStatus.SetRanking(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                            break;
                        case score:
                            simStatus.SetScore(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                            break;
                        // - AllSimulationsAreFinished Message
                        case bye:
                            // is called, when last Simulation is finished.
                            agent.setFlagDisableAgent();
                            break;
                        // - Request Action Messages
                        case requestAction:
                            agent.setFlagActionRequest();
                            break;
                        case actionID:
                            simStatus.SetActionID(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                            break;
                        case timestamp:
                            simStatus.SetTimestamp(Long.parseLong(percept.getParameters().get(0).toProlog()));
                            break;
                        case deadline:
                            simStatus.SetDeadline(Long.parseLong(percept.getParameters().get(0).toProlog()));
                            break;
                        case step:
                            simStatus.SetActualStep(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                            break;
                        case lastAction:
                            agentStatus.SetLastAction(percept.getParameters().get(0).toProlog());
                            break;
                        case lastActionResult:
                            agentStatus.SetLastActionResult(percept.getParameters().get(0).toProlog());
                            break;
                        case lastActionParams:
                            // has to be adjusted to a List if used/needed
                            agentStatus.SetLastActionParams(percept.getParameters().get(0).toProlog());
                            break;
                        // The "Score" percept is handled together with @SimEnd messages above
                        case thing:
                            // Dividing in two sublists obstacles and things 
                            if (percept.getParameters().get(2).toProlog().equals("obstacle")) {
                                obstacles.add(percept.getParameters());
                                continue;
                            }
                            things.add(percept.getParameters());
                            break;
                        case task:
                            tasks.add(percept.getParameters());
                            break;
                        case attached:
                            attached.add(percept.getParameters());
                            break;
                        case energy:
                            agentStatus.SetEnergy(Integer.parseInt(percept.getParameters().get(0).toProlog()));
                            break;
                        case deactivated:
                            agentStatus.SetDeactivatedFlag(percept.getParameters().get(0).toProlog().equals("true"));
                            break;
                        case roleZone:
                            roleZones.add(percept.getParameters());
                            break;
                        case goalZone:
                            goalZones.add(percept.getParameters());
                            break;
                        case violation:
                            violations.add(percept.getParameters().get(0).toProlog());
                            break;
                        case norm:
                            norms.add(percept.getParameters());
                            break;
                        case hit:
                            hits.add(percept.getParameters());
                            break;
                        case surveyed:
                            // Dividing in two sublists handling Surveyed Agents and Surveyed Things 
                            // Surveyed Agent
                            if (percept.getParameters().size() == 4) {
                                surveyedAgents.add(percept.getParameters());
                            }
                            // Surveyed Thing
                            if (percept.getParameters().size() == 2) {
                                surveyedThings.add(percept.getParameters());
                            }
                            break;
                        default: //All not processed perceipts are moved to the Overhang List
                        {
                            overhangNames.add(percept.getName());
                        }
                        break;
                    }
                } catch (Exception e) {
                    agent.say("Error in NextPerceptReader - evaluate \n" + e.toString());
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
        //Process all Datasets and transfer to Storage - NextAgentStatus
        simStatus.SetTasksList(processTasksSet());
        simStatus.SetNormsList(processNormsSet());
        simStatus.SetRolesList(processRolesSet());
        simStatus.SetViolations(processViolationsSet());

        agentStatus.SetAttachedElements(processAttachedSet());

        agentStatus.SetVision(processThingsSet());
        agentStatus.SetObstacles(processObstaclesSet());
        agentStatus.SetGoalZones(processGoalZonesSet());
        agentStatus.SetRoleZones(processRoleZonesSet());
        agentStatus.SetHits(processHitsSet());

        agentStatus.SetSurveyedAgents(processSurveyedAgentSet());
        agentStatus.SetSurveyedThings(processSurveyedThingSet());
    }

    private HashSet<NextTask> processTasksSet() {
        // task(name, deadline, reward, [req(x,y,type),...])
        HashSet<NextTask> processedTasksSet = new HashSet<>();
        // Converts Percept Data to Task Elements.
        for (List<Parameter> task : tasks) {
            try {

                HashSet<List<Parameter>> collectionOfBlocks = new HashSet<>();
                for (Parameter block : ((ParameterList) task.get(3))) {
                    collectionOfBlocks.add(((Function) block).getParameters());
                }
                processedTasksSet.add(
                        new NextTask(
                                task.get(0).toProlog(),
                                Integer.parseInt(task.get(1).toProlog()),
                                Integer.parseInt(task.get(2).toProlog()),
                                convertRequirements(collectionOfBlocks))
                );
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processTasksSet: \n" + e.toString());
            }
        }
        /* Debug Helper - Place // before to activate
        if (!processedTasksSet.isEmpty()) {
            agent.say("\n" + "Tasks \n" + processedTasksSet.toString() + "\n");
        }
        //*/
        return processedTasksSet;

    }

    private HashSet<NextNorm> processNormsSet() {
        /*  
        
        norm(id, start, end, [requirement(type, name, quantity, details), ...], fine)

        id : Identifier - ID of the norm
        start : Numeral - first step the norm holds
        end : Numeral - last step the norm holds
            requirement:
                type : the subject of the norm
                name : the precise name the subject refers to, e.g., the role constructor
                quantity : the maximum quantity that can be carried/adopted
                details : possibly additional details
        fine : Numeral - the energy cost of violating the norm (per step)
        
         */

        HashSet<NextNorm> processedNormsSet = new HashSet<>();
        // Converts Percept Data to Norm Attributes and constructs Norms.
        for (List<Parameter> norm : norms) {
            try {

                HashSet<NextNormRequirement> collectionOfRequirements = new HashSet<>();

                HashSet<List<Parameter>> collectionOfRequirementElements = new HashSet<>();
                for (Parameter requirement : ((ParameterList) norm.get(3))) {
                    collectionOfRequirementElements.add(((Function) requirement).getParameters());
                }

                collectionOfRequirements.add(new NextNormRequirement("type", "name", 0, "details"));

                processedNormsSet.add(new NextNorm(
                        norm.get(0).toProlog(),
                        Integer.parseInt(norm.get(1).toProlog()),
                        Integer.parseInt(norm.get(2).toProlog()),
                        convertNormRequirements(collectionOfRequirementElements),
                        Integer.parseInt(norm.get(4).toProlog())
                ));

            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processNormsSet: \n" + e.toString());
            }
        }
        /* Debug Helper - Place // before to activate
        if (!processedNormsSet.isEmpty()) {
            agent.say("\n" + "Norms \n" + processedNormsSet.toString() + "\n");
        }
        //*/
        return processedNormsSet;
    }

    private HashSet<NextRole> processRolesSet() {
        // role(name, vision, [action1, action2, ...], [speed1, speed2, ...], clearChance, clearMaxDistance)

        HashSet<NextRole> processedRolesSet = new HashSet<>();
        // Converts Percept Data to Role Attributes and constructs Roles.
        for (List<Parameter> role : roles) {
            try {

                HashSet<String> collectionOfActions = new HashSet<>();
                for (Parameter action : ((ParameterList) role.get(2))) {
                    collectionOfActions.add(action.toProlog());
                }
                ArrayList<Integer> collectionOfSpeeds = new ArrayList<>();
                for (Parameter speed : ((ParameterList) role.get(3))) {
                    collectionOfSpeeds.add(Integer.parseInt(speed.toProlog()));
                }
                processedRolesSet.add(
                        new NextRole(
                                role.get(0).toProlog(),
                                Integer.parseInt(role.get(1).toProlog()),
                                collectionOfActions,
                                collectionOfSpeeds,
                                Float.parseFloat(role.get(4).toProlog()),
                                Integer.parseInt(role.get(5).toProlog())
                        )
                );
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processRolesSet: \n" + e.toString());
            }
        }
        /* Debug Helper - Place // before to activate
        if (!processedRolesSet.isEmpty()) {
            agent.say("\n" + "Roles \n" + processedRolesSet.toString() + "\n");
        }
        //*/
        return processedRolesSet;

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
        /* Debug Helper - Place // before to activate 
        if (!processedAttachedSet.isEmpty()) {
            agent.say("\n" + "Attached Elements\n" + processedAttachedSet.toString() + "\n");
        }
        //*/
        return processedAttachedSet;
    }

    private HashSet<NextMapTile> processThingsSet() {
        // thing(x, y, type, details) - Percept Data Format
        HashSet<NextMapTile> processedThingsSet = new HashSet<>();
        // Converts Percept Data to NextMapTile Elements
        for (List<Parameter> object : things) {
            try {
                switch (object.size()) {
                    case 3 -> {
                        processedThingsSet.add(new NextMapTile(
                                Integer.parseInt(object.get(0).toProlog()),
                                Integer.parseInt(object.get(1).toProlog()),
                                simStatus.GetActualStep(),
                                object.get(2).toString()
                        ));
                    }
                    case 4 -> {
                        processedThingsSet.add(new NextMapTile(
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

        /* Debug Helper - Place // before to activate 
        if (!processedThingsSet.isEmpty()) {
            agent.say("\n" + "Visible Things\n" + processedThingsSet.toString() + "\n");
        }
        //*/
        return processedThingsSet;
    }

    private HashSet<NextMapTile> processObstaclesSet() {
        // thing(x, y, type, details) - Percept Data Format
        HashSet<NextMapTile> processedObstacles = new HashSet<>();
        // Converts Percept Data to goalZone MapTiles
        for (List<Parameter> object : obstacles) {
            try {
                processedObstacles.add(new NextMapTile(
                        Integer.parseInt(object.get(0).toProlog()),
                        Integer.parseInt(object.get(1).toProlog()),
                        agent.getSimulationStatus().GetActualStep(),
                        "obstacle"));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processObstaclesSet: \n" + e.toString());
            }

        }

        /* Debug Helper - Place // before to activate 
        if (!processedObstacles.isEmpty()) {
            agent.say("\n" + "Obstacles\n" + processedObstacles.toString() + "\n");
        }
        //*/
        return processedObstacles;

    }

    private HashSet<String> processViolationsSet() {
        // violation(id) - Percept Data Format
        // Forwards Percept Data as a String

        /* Debug Helper - Place // before to activate 
        if (!violations.isEmpty()) {
            agent.say("\n" + "Violations \n" + violations.toString() + "\n");
        }
        //*/
        return violations;

    }

    private HashSet<NextMapTile> processGoalZonesSet() {
        // goalZone(x, y) - Percept Data Format
        HashSet<NextMapTile> processedGoalZones = new HashSet<>();
        // Converts Percept Data to goalZone MapTiles
        for (List<Parameter> zone : goalZones) {
            try {
                processedGoalZones.add(new NextMapTile(
                        Integer.parseInt(zone.get(0).toProlog()),
                        Integer.parseInt(zone.get(1).toProlog()),
                        agent.getSimulationStatus().GetActualStep(),
                        "goalZone"));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processGoalZonesSet: \n" + e.toString());
            }

        }

        /* Debug Helper - Place // before to activate 
        if (!processedGoalZones.isEmpty()) {
            agent.say("\n" + "Goal Zones\n" + processedGoalZones.toString() + "\n");
        }
        //*/
        return processedGoalZones;
    }

    private HashSet<NextMapTile> processRoleZonesSet() {
        // roleZone(x, y) - Percept Data Format
        HashSet<NextMapTile> processedRoleZones = new HashSet<>();
        // Converts Percept Data to roleZone MapTiles
        for (List<Parameter> zone : roleZones) {
            try {
                processedRoleZones.add(new NextMapTile(
                        Integer.parseInt(zone.get(0).toProlog()),
                        Integer.parseInt(zone.get(1).toProlog()),
                        agent.getSimulationStatus().GetActualStep(),
                        "roleZone"));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processRoleZonesSet: \n" + e.toString());
            }
        }

        /* Debug Helper - Place // before to activate 
        if (!processedRoleZones.isEmpty()) {
            agent.say("\n" + "Role Zones \n" + processedRoleZones.toString() + "\n");
        }
        //*/
        return processedRoleZones;
    }

    private HashSet<NextMapTile> processHitsSet() {
        // hit(x, y) - Percept Data Format
        HashSet<NextMapTile> processedHits = new HashSet<>();
        // Converts Percept Data to goalZone MapTiles
        for (List<Parameter> hit : hits) {
            try {
                processedHits.add(new NextMapTile(
                        Integer.parseInt(hit.get(0).toProlog()),
                        Integer.parseInt(hit.get(1).toProlog()),
                        agent.getSimulationStatus().GetActualStep(),
                        "Hit"));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processHitsSet: \n" + e.toString());
            }

        }

        /* Debug Helper - Place // before to activate 
        if (!processedHits.isEmpty()) {
            agent.say("\n" + "Hits \n" + processedHits.toString() + "\n");
        }
        //*/
        return processedHits;
    }

    private HashSet<NextSurveyedAgent> processSurveyedAgentSet() {
        // surveyed("agent", name, role, energy)
        // name : Identifier
        // role : Identifier
        // energy : Numeral

        HashSet<NextSurveyedAgent> processedSurveyedAgents = new HashSet<>();
        // Converts Percept Data to Target Data
        for (List<Parameter> SurveyedAgent : surveyedAgents) {
            try {
                processedSurveyedAgents.add(
                        new NextSurveyedAgent(
                            SurveyedAgent.get(0).toProlog(),
                            SurveyedAgent.get(1).toProlog(),
                            Integer.parseInt(SurveyedAgent.get(1).toProlog())
                        ));
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processSurveyedAgentSet: \n" + e.toString());
            }
        }
        /* Debug Helper - Place // before to activate 
        if (!processedSurveyedAgents.isEmpty()) {
            agent.say("\n" + "Surveyed Agents \n" + processedSurveyedAgents.toString() + "\n");
        }
        //*/
        return processedSurveyedAgents;

    }

    private HashSet<NextSurveyedThing> processSurveyedThingSet() {

        // surveyed("dispenser"/"goal"/"role", distance)
        HashSet<NextSurveyedThing> processedSurveyedThings = new HashSet<>();
        // Converts Percept Data to Target Data
        for (List<Parameter> SurveyedAgent : surveyedThings) {
            try {
                processedSurveyedThings.add(
                        new NextSurveyedThing(
                                SurveyedAgent.get(0).toProlog(),
                                Integer.parseInt(SurveyedAgent.get(1).toProlog())
                        )
                );
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processSurveyedThingSet: \n" + e.toString());
            }
        }
        /* Debug Helper - Place // before to activate 
        if (!processedSurveyedThings.isEmpty()) {
            agent.say("\n" + "Distance to Surveyed Things \n" + processedSurveyedThings.toString() + "\n");
        }
        //*/
        return processedSurveyedThings;
    }

    private HashSet<NextMapTile> convertRequirements(HashSet<List<Parameter>> requirementsList) {
        HashSet<NextMapTile> processedRequirements = new HashSet<>();
        for (List<Parameter> element : requirementsList) {
            processedRequirements.add(new NextMapTile(
                    Integer.parseInt(element.get(0).toProlog()),
                    Integer.parseInt(element.get(1).toProlog()),
                    -1,
                    element.get(2).toProlog())
            );
        }
        //agent.say(requirementsList.toString());

        return processedRequirements;
    }

    private HashSet<NextNormRequirement> convertNormRequirements(HashSet<List<Parameter>> collectionOfRequirementElements) {
        /*
        norm(id, start, end, [requirement(type, name, quantity, details), ...], fine)

        id : Identifier - ID of the norm
        start : Numeral - first step the norm holds
        end : Numeral - last step the norm holds
            requirement:
                type : the subject of the norm
                name : the precise name the subject refers to, e.g., the role constructor
                quantity : the maximum quantity that can be carried/adopted
                details : possibly additional details
        fine : Numeral - the energy cost of violating the norm (per step)

        */
        
        HashSet<NextNormRequirement> processedNormRequirements = new HashSet<>();
        for (List<Parameter> element : collectionOfRequirementElements) {
            processedNormRequirements.add(
                    new NextNormRequirement(
                            element.get(0).toProlog(),
                            element.get(1).toProlog(),
                            Integer.parseInt(element.get(2).toProlog()),
                            element.get(3).toProlog())
            );
        }

        return processedNormRequirements;
    }
}
