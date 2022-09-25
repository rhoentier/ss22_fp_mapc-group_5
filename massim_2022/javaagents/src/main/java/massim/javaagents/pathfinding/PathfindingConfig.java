package massim.javaagents.pathfinding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.general.NextConstants.EPathFinding;

public final class PathfindingConfig {

    private static EPathFinding algorithm;
    private static int steps;
    private static ECardinals direction;

    /**
     * Create a new Pathfinding based on the given configuration file
     *
     * @param path path to a java path configuration file
     */
    public static void ParseConfig(String path) {
        try {
            var config = new JSONObject(new String(Files.readAllBytes(Paths.get(path, "pathfindingconfig.json"))));
            algorithm = EPathFinding.valueOf(config.getString("algorithm"));
            steps = config.getInt("steps");
            direction = ECardinals.valueOf(config.getString("direction"));
        } catch (IOException e) {
            e.printStackTrace();
            // Default
            algorithm = EPathFinding.random;
            steps = 0;
            direction = ECardinals.n;
        }
    }

    public static EPathFinding GetAlgorithm() {
        return algorithm;
    }

    public static ECardinals GetDirection() {
        return direction;
    }

    public static int GetSteps() {
        return steps;
    }
}
