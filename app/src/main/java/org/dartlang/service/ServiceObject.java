// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.json.JSONObject;

abstract public class ServiceObject extends Response {
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

  abstract protected void update(JSONObject object);

  public void load(final ResponseCallback callback) {
    if (isLoaded()) {
      if (callback != null) {
        callback.onResponse(getId(), this);
      }
    }
    reload(callback);
  }

  public void reload(final ResponseCallback callback) {
    VM vm = owner.getVM();
    if (vm == null) {
      if (callback != null) {
        // TODO(johnmccutchan): Indicate load failed somehow.
        callback.onResponse(getId(), this);
      }
      return;
    }
    Connection connection = vm.connection;
    connection.get(getLink(), owner, callback);
  }

  public void reload() {
    reload(null);
  }

  public Owner getOwner() {
    return owner;
  }

  public String getId() {
    return id;
  }

  public String getLink() {
    if (owner == null) {
      return getId();
    }
    return owner.relativeLink(getId());
  }

  public String getType() {
    return type;
  }

  public String getVMType() {
    return vmType;
  }
}
