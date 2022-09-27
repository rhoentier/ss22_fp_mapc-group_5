package massim.javaagents.percept;

/**
 * Container class for describing surveyed Things.
 *
 * @author Alexander Lorenz
 */
public class NextSurveyedThing {

    // surveyed("dispenser"/"goal"/"role", distance)
    private String type;
    private int distance;

    /**
     *
     * @param type
     * @param distance
     */
    public NextSurveyedThing(String type, int distance) {
        this.type = type;
        this.distance = distance;
    }

    /**
     *
     * @return
     */
    public String GetType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void SetType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public int GetDistance() {
        return distance;
    }

    /**
     *
     * @param distance
     */
    public void SetDistance(int distance) {
        this.distance = distance;
    }

}
