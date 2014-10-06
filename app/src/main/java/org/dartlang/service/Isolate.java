package org.dartlang.service;

import org.json.JSONObject;

/**
 * Created by johnmccutchan on 10/4/14.
 */
public class Isolate extends ServiceObject implements Owner {
  Isolate(VM vm) {
    super(vm, null);
    assert vm != null;
    setIsolate(this);
  }

  public ServiceObject fromJSONObject(JSONObject obj) {
    return null;
  }

  public String relativeLink(String id) {
    return "/" + this.getId() + "/" + id;
  }

  public void get(String id, RequestCallback callback) {
    getVM().get(relativeLink(id), callback);
  }
}
