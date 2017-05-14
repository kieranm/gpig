package domain.util;

import org.json.JSONObject;

/**
 * @author Oliver Lea
 */
public interface JSONable {
    JSONObject toJSON();
}
