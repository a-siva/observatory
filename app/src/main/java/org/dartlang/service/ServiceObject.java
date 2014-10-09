// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.json.JSONObject;

public class ServiceObject extends Response {
  protected Owner owner;
  protected String id;
  protected String type;
  protected String vmType;
  private boolean loaded = false;

  public boolean isLoaded() { return loaded; }

  protected ServiceObject(Owner owner) {
    setOwner(owner);
  }

  public VM getVM() {
    return owner.getVM();
  }

  public Isolate getIsolate() {
    return owner.getIsolate();
  }

  protected void setOwner(Owner owner) {
    this.owner = owner;
  }

  public interface LoadListener {
    public void onLoad(ServiceObject object);
  }

  protected static boolean isServiceMap(JSONObject object) {
    if (object == null) {
      return false;
    }
    String id = object.optString("id");
    String type = object.optString("type");
    return (id != null) && (type != null);
  }

  protected static boolean hasRef(String id) {
    return id.startsWith("@");
  }

  protected static String stripRef(String id) {
    if (!hasRef(id)) {
      return id;
    }
    return id.substring(1);
  }

  protected static ServiceObject fromMap(Owner owner, JSONObject object) {
    return null;
  }

  public void load(LoadListener loadListener) {
    if (isLoaded()) {
      loadListener.onLoad(this);
    }
    reload(loadListener);
  }

  public void reload(LoadListener loadListener) {
  }

  public Owner getOwner() {
    return owner;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getVMType() {
    return vmType;
  }
}
