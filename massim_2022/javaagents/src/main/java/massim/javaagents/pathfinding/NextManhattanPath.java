package massim.javaagents.pathfinding;

import eis.iilang.Action;

import static java.lang.Math.abs;

import java.util.ArrayList;

import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.map.Vector2D;

/**
 * Basic Pathcalculation based on Manhattan distance
 * Ignores map values
 *
 * @author AVL
 */
public class NextManhattanPath {

    /**
     * @param X X position to Walk to
     * @param Y Y Position to Wals to
     * @return ArrayList of "walk" Actions
     */
    public ArrayList<Action> calculatePath(int X, int Y) {
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
     * @param startPosition position where the walk should start
     * @param endPosition   position where the walk should end
     * @return ArrayList of "walk" Actions
     */
    public static ArrayList<Action> CalculatePath(Vector2D startPosition, Vector2D endPosition) {
        int X = endPosition.getComponents()[0] - startPosition.getComponents()[0];
        int Y = endPosition.getComponents()[1] - startPosition.getComponents()[1];
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
