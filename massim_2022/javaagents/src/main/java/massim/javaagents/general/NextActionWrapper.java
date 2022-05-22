package massim.javaagents.general;

import eis.iilang.Action;
import eis.iilang.Identifier;

public class NextActionWrapper {

    /**
     * Create an Action with two parameters.
     *
     * @param action
     * @param parameter1
     * @param parameter2
     * @return An Action with parameter or null if a required parameter is missing
     */
    public static Action CreateAction(NextConstants.EActions action, Identifier parameter1, Identifier parameter2) {
        switch (action) {
            case CONNECT: {
                if (parameter1 != null && parameter2 != null)
                    return new Action("connect", parameter1, parameter2);
            }
            case DISCONNECT: {
                if (parameter1 != null && parameter2 != null)
                    return new Action(NextConstants.EActions.DISCONNECT.toString(), parameter1, parameter2);
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
    public static Action CreateAction(NextConstants.EActions action, Identifier parameter) {
        switch (action) {
            case MOVE: {
                if (parameter != null) {
                    return new Action(NextConstants.EActions.MOVE.toString(), parameter);
                }
                return null;
            }
            case ATTACH: {
                if (parameter != null) {
                    return new Action(NextConstants.EActions.ATTACH.toString(), parameter);
                }
                return null;
            }
            case DETACH: {
                if (parameter != null) {
                    return new Action(NextConstants.EActions.DETACH.toString(), parameter);
                }
                return null;
            }
            case ROTATE: {
                if (parameter != null) {
                    if (parameter.getValue() == "ccw" || parameter.getValue() == "cw") {
                        return new Action(NextConstants.EActions.ROTATE.toString(), parameter);
                    }
                }
                return null;
            }
            case REQUEST: {
                if (parameter != null) {
                    return new Action(NextConstants.EActions.REQUEST.toString(), parameter);
                }
                return null;
            }
            case SUBMIT: {
                if (parameter != null) {
                    return new Action(NextConstants.EActions.SUBMIT.toString(), parameter);
                }
                return null;
            }
            case CLEAR: {
                if (parameter != null)
                    return new Action(NextConstants.EActions.CLEAR.toString(), parameter);

                return null;
            }
            case ADOPT: {
                if (parameter != null)
                    return new Action(NextConstants.EActions.ADOPT.toString(), parameter);

                return null;
            }
            case SURVEY: {
                if (parameter != null)
                    return new Action(NextConstants.EActions.SURVEY.toString(), parameter);

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
    public static Action CreateAction(NextConstants.EActions action) {
        switch (action) {
            case SKIP: {
                return new Action("skip");
            }
            default:
                return null;
        }

    }

}

