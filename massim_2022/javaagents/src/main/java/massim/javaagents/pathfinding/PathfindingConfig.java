package massim.javaagents.pathfinding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.general.NextConstants.EPathFinding;

public class PathfindingConfig {

	private EPathFinding algorithm;
	private int steps;
	private ECardinals direction;
  
    /**
     * Create a new Pathfinding based on the given configuration file
     *
     * @param path path to a java path configuration file
     */
    public PathfindingConfig(String path) {
        parseConfig(path);
    }
    
    private void parseConfig(String path) {
        try {
            var config = new JSONObject(new String(Files.readAllBytes(Paths.get(path, "pathfindingconfig.json"))));
            this.algorithm = EPathFinding.valueOf(config.getString("algorithm"));
            this.steps = config.getInt("steps");
            this.direction = ECardinals.valueOf(config.getString("direction"));
        } catch (IOException e) {
            e.printStackTrace();
            // Default
            this.algorithm = EPathFinding.random;
            this.steps = 0;
        	this.direction = ECardinals.n;
        }
    }
    
    public EPathFinding GetAlgorithm() {
    	return this.algorithm;
    }
    
    public ECardinals GetDirection()
    {
    	return this.direction;
    }
    
    public int GetSteps()
    {
    	return this.steps;
    }
}
