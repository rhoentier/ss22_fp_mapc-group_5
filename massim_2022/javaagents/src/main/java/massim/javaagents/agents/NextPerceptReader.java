package massim.javaagents.agents;

import eis.iilang.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import massim.javaagents.map.NextMapTile;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.Vector2D;
import massim.javaagents.percept.NextNorm;
import massim.javaagents.percept.NextNormRequirement;
import massim.javaagents.percept.NextRole;
import massim.javaagents.percept.NextSurveyedAgent;
import massim.javaagents.percept.NextSurveyedThing;
import massim.javaagents.percept.NextTask;

/**
 * The basic interpreter of the MASSim server communication protocoll
 * 
 * Handling of all documented Percepts. 
 * The conversion of sets into the target format + saving in external Data Storage
 *
 * @author Alexander Lorenz
 */
public class NextPerceptReader {

    /*
     * ########## region fields
     */
    
    private final NextAgent             agent;              // the instance of the parent agent
    private final NextSimulationStatus  simStatus;          // target containter for simulation related status values
    private final NextAgentStatus       agentStatus;        // target containter for agent related status values

    // all perceptions are received in no particular order and have to be sorted in order to be processed
    
    private HashSet<List<Parameter>>    tasks;              
    private HashSet<List<Parameter>>    roles;              
    private HashSet<List<Parameter>>    norms;
    private HashSet<List<Parameter>>    attached;
    private HashSet<List<Parameter>>    things;
    private HashSet<List<Parameter>>    markers;
    private HashSet<List<Parameter>>    obstacles;
    private HashSet<List<Parameter>>    hits;
    private HashSet<String>             violations;
    private HashSet<List<Parameter>>    surveyedAgents;
    private HashSet<List<Parameter>>    surveyedThings;
    private HashSet<List<Parameter>>    goalZones;
    private HashSet<List<Parameter>>    roleZones;
    
    private HashSet<String>             overhangNames;      // percepts without known target

    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     *
     * @param agent NextAgent instance of the parent agent
     */
    public NextPerceptReader(NextAgent agent) {
        this.agent = agent;                                 // the instance of the parent agent                   
        this.simStatus = agent.GetSimulationStatus();       // target containter for simulation values
        this.agentStatus = agent.GetAgentStatus();          // target containter for agent values
        
        clearSets();                                        // initialise and clear all sets
    }

    /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */
    
    /**
     * Evaluate the percepts and sort by type
     *
     * @param percepts Percept List recieved from server 
     */
    
    public void Evaluate(List<Percept> percepts) {

        clearSets(); //clearing of the containers before processing of percepts

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
                        // no action in NextPerceptReader required. Should be handled in NextAgent
                        break;
                        
            // - Request Action Messages
                    case requestAction:
                        // no action, should be handled in NextAgent
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
                        simStatus.SetCurrentStep(Integer.parseInt(percept.getParameters().get(0).toProlog()));
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
                        // Dividing in sublists obstacles, markers and things 
                        if (percept.getParameters().get(2).toProlog().equals("obstacle")) {
                            obstacles.add(percept.getParameters());
                            continue;
                        }
                        if (percept.getParameters().get(2).toProlog().equals("marker")) {
                            markers.add(percept.getParameters());
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
                this.agent.say("Error in NextPerceptReader - evaluate \n" + e.toString());
            }
        }

        // handling of unusual percept entries
        if (!overhangNames.isEmpty()) {
            this.agent.say("------------------------------------------------");
            this.agent.say("WARNING! Unusual entry \n" + overhangNames.toString() + "\n detected");
            this.agent.say("------------------------------------------------");
        }

        //Second Step of Processing of Sets
        convertGeneratedSets();
    }

    /*
     * ##################### endregion public methods
     */

    /*
     * ########## region private methods
     */

    private void clearSets() {
        
        //initialising and clearing of the containers before processing of percepts
        
        attached        = new HashSet<>();
        tasks           = new HashSet<>();
        norms           = new HashSet<>();
        roles           = new HashSet<>();
        things          = new HashSet<>();
        markers         = new HashSet<>();
        obstacles       = new HashSet<>();
        violations      = new HashSet<>();
        goalZones       = new HashSet<>();
        roleZones       = new HashSet<>();
        overhangNames   = new HashSet<>();
        hits            = new HashSet<>();
        surveyedAgents  = new HashSet<>();
        surveyedThings  = new HashSet<>();
    }

    /**
     * Process all Datasets and transfer to NextAgentStatus and NextSimStatus
     */
    private void convertGeneratedSets() {

        //Process Things, Markers and Obstales and combine to fullLocalView
        //target: keep the flexibility to combine the thing elements later
        HashSet<NextMapTile> fullLocalView = new HashSet<>();
        HashSet<NextMapTile> processedObstacles;
        HashSet<NextMapTile> processedThings;
        HashSet<NextMapTile> processedMarkers;

        processedThings = processThingsSet(things);
        processedMarkers = processThingsSet(markers);
        processedObstacles = processObstaclesSet();
        fullLocalView.addAll(processedThings);
        //fullLocalView.addAll(processedMarkers);           
        fullLocalView.addAll(processedObstacles);

        agentStatus.SetVision(processedThings);
        agentStatus.SetMarkers(processedMarkers);
        agentStatus.SetObstacles(processedObstacles);
        agentStatus.SetFullLocalView(fullLocalView);
        
        //Process remaining Datasets
        simStatus.SetTasksList(processTasksSet());
        simStatus.SetNormsList(processNormsSet());
        simStatus.SetRolesList(processRolesSet());
        simStatus.SetViolations(processViolationsSet());
        
        agentStatus.SetVisibleAttachedElements(processAttachedSet());
        agentStatus.SetGoalZones(processGoalZonesSet());
        agentStatus.SetRoleZones(processRoleZonesSet());
        agentStatus.SetHits(processHitsSet());
        agentStatus.SetSurveyedAgents(processSurveyedAgentSet());
        agentStatus.SetSurveyedThings(processSurveyedThingSet());
        agentStatus.SetDispenser(convertDispenserFromVision());
        
    }

    /**
     * Select dispenser from vision
     * 
     * @return NextMapTile HashSet of type dispenser
     */
    private HashSet<NextMapTile> convertDispenserFromVision() {
        HashSet<NextMapTile> collectionOfDispenser = new HashSet<>();
        for (NextMapTile mapTile : agentStatus.GetVisibleThings()) {
            try {
                if (mapTile.GetThingType().contains("dispenser")) {
                    collectionOfDispenser.add(mapTile);
                }
            } catch (Exception e) {
                agent.say("Error in NextPerceptReader - processTasksSet: \n" + e.toString());
            }
        }
        return collectionOfDispenser;
    }

    /**
     * Convert the collected task data to NextTask elements
     * 
     * data example:
     * task (name, deadline, reward, [req(x,y,type),...])
     * 
     * @return NextTask HashSet with available tasks
     */
    private HashSet<NextTask> processTasksSet() {
        HashSet<NextTask> processedTasksSet = new HashSet<>();
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
    
    /**
     * Converts percept data to norm attributes and constructs norms.
     * 
     * Recieved data example:
     * norm(id, start, end, [requirement(type, name, quantity, details), ...], fine)
     * 
     * id : Identifier - ID of the norm
     * start : Numeral - first step the norm holds
     * end : Numeral - last step the norm holds
     * requirement:
     *          type : the subject of the norm
     *          name : the precise name the subject refers to, e.g., the role constructor
     *          quantity : the maximum quantity that can be carried/adopted
     *          details : possibly additional details
     * fine : Numeral - the energy cost of violating the norm (per step)
     *  
     * @return NextNorm HashSet with available norms
     */
    private HashSet<NextNorm> processNormsSet() {
        
        HashSet<NextNorm> processedNormsSet = new HashSet<>();
        
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

    /**
     * Converts percept data to roles attributes and constructs roles.
     * 
     * Recieved data example:
     * role(name, vision, [action1, action2, ...], [speed1, speed2, ...], clearChance, clearMaxDistance)
     * 
     * @return NextRoles HashSet with available roles 
     */
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

    /**
     * Converts percept data to attached points. All visible attached elements are processed.
     * 
     * Recieved data example:
     * attached(x, y)
     * 
     * @return Vector2D HashSet with positions of visible attached elements
     */
    private HashSet<Vector2D> processAttachedSet() {
        HashSet<Vector2D> processedAttachedSet = new HashSet<>();
        for (List<Parameter> zone : attached) {
            try {
                processedAttachedSet.add(new Vector2D(
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


    /**
     * Converts Percept Data to NextMapTile Elements
     * 
     * Recieved data example:
     * thing(x, y, type, details) - Percept Data Format
     *   
     * @param things HashSet of Parameter Lists containing the recieved data
     * @return NextMapTile HashSet with visible things
     */

    private HashSet<NextMapTile> processThingsSet(HashSet<List<Parameter>> things) {
        HashSet<NextMapTile> processedThingsSet = new HashSet<>();
               for (List<Parameter> object : things) {
            try {
                switch (object.size()) {
                    case 3 -> {
                        processedThingsSet.add(new NextMapTile(
                                Integer.parseInt(object.get(0).toProlog()),
                                Integer.parseInt(object.get(1).toProlog()),
                                simStatus.GetCurrentStep(),
                                object.get(2).toString()
                        ));
                    }
                    case 4 -> {
                        processedThingsSet.add(new NextMapTile(
                                Integer.parseInt(object.get(0).toProlog()),
                                Integer.parseInt(object.get(1).toProlog()),
                                simStatus.GetCurrentStep(),
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
            agent.say("\n" + "Visible Things \n" + processedThingsSet.toString() + "\n");
        }
        //*/
        return processedThingsSet;
    }
    
    /**
     * Converts Percept Data of obstacles to NextMapTile Elements
     * 
     * Recieved data example:
     * thing(x, y, type, details) - Percept Data Format
     *   
     * @return NextMapTile HashSet with visible obstacles
     */
    private HashSet<NextMapTile> processObstaclesSet() {
        HashSet<NextMapTile> processedObstacles = new HashSet<>();
        for (List<Parameter> object : obstacles) {
            try {
                processedObstacles.add(new NextMapTile(
                        Integer.parseInt(object.get(0).toProlog()),
                        Integer.parseInt(object.get(1).toProlog()),
                        agent.GetSimulationStatus().GetCurrentStep(),
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
    
    /**
     * Converts Percept Data to Violation Elements
     * no processing needed
     * 
     * Recieved data example:
     * violation(id) - Percept Data Format
     *   
     * @return String HashSet with violation IDs
     */

    private HashSet<String> processViolationsSet() {

        /* Debug Helper - Place // before to activate 
        if (!violations.isEmpty()) {
            agent.say("\n" + "Violations \n" + violations.toString() + "\n");
        }
        //*/
        return violations;

    }

    /**
     * Converts percept data of goal zones to NextMapTile elements
     * 
     * Recieved data example:
     * goalZone(x, y) - Percept Data Format
     *   
     * @return NextMapTile HashSet with visible goal zones
     */
    
    private HashSet<NextMapTile> processGoalZonesSet() {
        
        HashSet<NextMapTile> processedGoalZones = new HashSet<>();
        for (List<Parameter> zone : goalZones) {
            try {
                processedGoalZones.add(new NextMapTile(
                        Integer.parseInt(zone.get(0).toProlog()),
                        Integer.parseInt(zone.get(1).toProlog()),
                        agent.GetSimulationStatus().GetCurrentStep(),
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
    
    /**
     * Converts percept data of role zones to NextMapTile elements
     * 
     * Recieved data example:
     * roleZone(x, y) - Percept Data Format
     *   
     * @return NextMapTile HashSet with visible role zones
     */
    private HashSet<NextMapTile> processRoleZonesSet() {
        HashSet<NextMapTile> processedRoleZones = new HashSet<>();
        for (List<Parameter> zone : roleZones) {
            try {
                processedRoleZones.add(new NextMapTile(
                        Integer.parseInt(zone.get(0).toProlog()),
                        Integer.parseInt(zone.get(1).toProlog()),
                        agent.GetSimulationStatus().GetCurrentStep(),
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

    /**
     * Converts percept data of hits to NextMapTile elements
     * 
     * Recieved data example:
     * hit(x, y) - Percept Data Format
     *   
     * @return NextMapTile HashSet with source of damage
     */
    private HashSet<NextMapTile> processHitsSet() {
        HashSet<NextMapTile> processedHits = new HashSet<>();
        for (List<Parameter> hit : hits) {
            try {
                processedHits.add(new NextMapTile(
                        Integer.parseInt(hit.get(0).toProlog()),
                        Integer.parseInt(hit.get(1).toProlog()),
                        agent.GetSimulationStatus().GetCurrentStep(),
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

     /**
     * Converts percept data of surveyed agent
     * 
     * Recieved data example:
     * surveyed("agent", name, role, energy)
     *    name:     Identifier
     *    role:     Identifier
     *    energy:   Numeral
     * 
     * @return NextSurveyedAgent HashSet with information about the specific agent
     */
    private HashSet<NextSurveyedAgent> processSurveyedAgentSet() {
        
        HashSet<NextSurveyedAgent> processedSurveyedAgents = new HashSet<>();
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

    /**
     * Converts percept data of surveyed thing
     * 
     * Recieved data example:
     * surveyed("dispenser"/"goal"/"role", Distance)
     * 
     * @return NextSurveyedThing HashSet with distance th next object of type
     */

    private HashSet<NextSurveyedThing> processSurveyedThingSet() {

        HashSet<NextSurveyedThing> processedSurveyedThings = new HashSet<>();
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

    /**
     * Converts percept data to description of block orientation for a task
     * 
     * @param requirementsList HashSet of Parameter Lists with requirement description
     * @return NextMapTile HashSet with block position for task submission
     */
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

    /**
     * Converts percept data to description of norm specification
     * 
     * Recieved data example:
     * requirement(
     *          type : the subject of the norm
     *          name : the precise name the subject refers to, e.g., the role constructor
     *          quantity : the maximum quantity that can be carried/adopted
     *          details : possibly additional details)
     * 
     * @param requirementsList HashSet of Parameter Lists with requirement description
     * @return NextMapTile HashSet with block position for task submission
     */
    private HashSet<NextNormRequirement> convertNormRequirements(HashSet<List<Parameter>> collectionOfRequirementElements) {

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
    
    /*
     * ##################### endregion private methods
     */
}
