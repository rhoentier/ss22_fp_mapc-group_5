package massim.javaagents.general;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.general.NextConstants.EActions;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.map.Vector2D;

public class NextActionWrapper {

    /**
     * Create an Action with two parameters.
     *
     * @param action
     * @param parameter1
     * @param parameter2
     * @return An Action with parameter or null if a required parameter is missing
     */
    public static Action CreateAction(EActions action, Identifier parameter1, Identifier parameter2) {
        switch (action) {
            case connect: {
                if (parameter1 != null && parameter2 != null)
                    return new Action(EActions.connect.toString(), parameter1, parameter2);
            }
            case disconnect: {
                if (parameter1 != null && parameter2 != null)
                    return new Action(EActions.disconnect.toString(), parameter1, parameter2);
            }
            case clear: {
                if (parameter1 != null && parameter2 != null)
                    return new Action(EActions.clear.toString(), parameter1, parameter2);
            }
            default:
                return null;
        }
    }

    /**
     * Create an Action with one parameter.
     *
     * @param action
     * @param parameter
     * @return An Action with parameter or null if a required parameter is missing
     */
    public static Action CreateAction(EActions action, Identifier parameter) {
        switch (action) {
            case move: {
                if (parameter != null) {
                    return new Action(EActions.move.toString(), parameter);
                }
                return null;
            }
            case attach: {
                if (parameter != null) {
                    return new Action(EActions.attach.toString(), parameter);
                }
                return null;
            }
            case detach: {
                if (parameter != null) {
                    return new Action(EActions.detach.toString(), parameter);
                }
                return null;
            }
            case rotate: {
                if (parameter != null) {
                    if (parameter.getValue() == "ccw" || parameter.getValue() == "cw") {
                        return new Action(EActions.rotate.toString(), parameter);
                    }
                }
                return null;
            }
            case request: {
                if (parameter != null) {
                    return new Action(EActions.request.toString(), parameter);
                }
                return null;
            }
            case submit: {
                if (parameter != null) {
                    return new Action(EActions.submit.toString(), parameter);
                }
                return null;
            }
            case adopt: {
                if (parameter != null)
                    return new Action(EActions.adopt.toString(), parameter);

                return null;
            }
            case survey: {
                if (parameter != null)
                    return new Action(EActions.survey.toString(), parameter);

                return null;
            }
            default:
                if (parameter == null) return CreateAction(action);
                return null;
        }

    }

    /**
     * Create an Action with no Parameters.
     *
     * @param action
     * @return An Action without a parameter
     */
    public static Action CreateAction(EActions action) {
        switch (action) {
            case skip: {
                return new Action("skip");
            }
            default:
                return null;
        }

    }
}

