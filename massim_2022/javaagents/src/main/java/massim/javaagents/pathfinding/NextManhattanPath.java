package massim.javaagents.pathfinding;

import eis.iilang.Action;

import static java.lang.Math.abs;

import java.util.ArrayList;

import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.map.Vector2D;

/**
 * Basic path calculation based on manhattan distance. Ignores map values.
 *
 * @author Alexander Lorenz
 */
public class NextManhattanPath {

    /**
     * Calculate path based on local view.
     *
     * @param X Integer - X position in local view to walk to
     * @param Y Integer - Y position in local view to walk to
     * @return Action ArrayList describing the movement path
     */
    public ArrayList<Action> CalculatePath(int X, int Y) {
        ArrayList<Action> steps = new ArrayList<>();
        for (int i = 0; i < abs(X); i++) {
            if (X > 0) {
                steps.add(NextAgentUtil.GenerateEastMove());
            } else {
                steps.add(NextAgentUtil.GenerateWestMove());
            }
        }
        for (int i = 0; i < abs(Y); i++) {
            if (Y > 0) {
                steps.add(NextAgentUtil.GenerateSouthMove());
            } else {
                steps.add(NextAgentUtil.GenerateNorthMove());
            }
        }
        return steps;
    }

    /**
     * Calculate path based on global view, providing start and end point.
     *
     * @param startPosition Vector2D position where the path should start
     * @param endPosition Vector2D position of the target
     * @return Action ArrayList describing the movement path
     */
    public static ArrayList<Action> CalculatePath(Vector2D startPosition, Vector2D endPosition) {
        int X = endPosition.GetComponents()[0] - startPosition.GetComponents()[0];
        int Y = endPosition.GetComponents()[1] - startPosition.GetComponents()[1];
        ArrayList<Action> steps = new ArrayList<>();
        for (int i = 0; i < abs(X); i++) {
            if (X > 0) {
                steps.add(NextAgentUtil.GenerateEastMove());
            } else {
                steps.add(NextAgentUtil.GenerateWestMove());
            }
        }
        for (int i = 0; i < abs(Y); i++) {
            if (Y > 0) {
                steps.add(NextAgentUtil.GenerateSouthMove());
            } else {
                steps.add(NextAgentUtil.GenerateNorthMove());
            }
        }
        return steps;
    }

}
