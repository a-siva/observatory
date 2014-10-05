package org.dartlang.service;

import org.json.JSONObject;

/**
 * Created by johnmccutchan on 10/4/14.
 */
public interface Owner {
    public ServiceObject fromJSONObject(JSONObject object);
    public String relativeLink(String id);

    public static interface RequestCallback {
        public void onResponse(Response response);
    }

    public void get(String id, RequestCallback callback);
}
