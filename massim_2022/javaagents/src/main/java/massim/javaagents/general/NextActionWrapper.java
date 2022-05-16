package massim.javaagents.general;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class NextActionWrapper {


    /**
     * Create an Action with optional Parameters.
     *
     * @param action
     * @param parameter
     * @return An Action with parameter or null if a required parameter is missing
     */
    public static Action createAction(NextConstants.EActions action, Identifier parameter) {
        switch (action) {
            case attach: {
                if (parameter != null) {
                    return new Action("attach", parameter);
                }
                return null;
            }
            case rotate: {
                if (parameter != null) {
                    if (parameter.getValue() == "ccw" || parameter.getValue() == "cw") {
                        return new Action("rotate", parameter);
                    }
                }
                return null;
            }
            case request: {
                if (parameter != null) {
                    return new Action("request", parameter);
                }
                return null;
            }
            case adopt: {
                if (parameter != null)
                    return new Action("adopt", parameter);

                return null;
            }
            default:
                if (parameter == null) return createAction(action);
                return null;
        }

    }

    /**
     * Create an Action with no Parameters.
     *
     * @param action
     * @return An Action without a parameter
     */
    public static Action createAction(NextConstants.EActions action) {
        switch (action) {
            case skip: {
                return new Action("skip");
            }
            default:
                return null;
        }

    }

}

