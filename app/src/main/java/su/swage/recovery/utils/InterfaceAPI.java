package su.swage.recovery.utils;

import org.json.JSONObject;

public interface InterfaceAPI {
    Void onSuccessResponseAPI(JSONObject result);

    Void onFailureResponseAPI(Boolean isSystem, JSONObject result);
}
