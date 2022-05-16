package massim.javaagents.general;

import eis.iilang.Action;
import eis.iilang.Identifier;

public class NextActionWrapper {


    /**
     * Create an Action with optional Parameters.
     *
     * @param action
     * @param parameter
     * @return An Action with parameter or null if a required parameter is missing
     */
    public static Action createAction(NextConstants.EActions action, String parameter) {
        switch (action) {
            case rotate: {
                if (parameter != null) {
                    if (parameter == "ccw" || parameter == "cw") {
                        return new Action("rotate", new Identifier(parameter));
                    }
                }
                return null;
            }
            case adopt: {
                if (parameter != null) {
                    return new Action("adopt", new Identifier(parameter));
                }
                return null;
            }
            default:
                return null;
        }

    }
}
