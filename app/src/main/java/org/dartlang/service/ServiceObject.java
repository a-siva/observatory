// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.json.JSONObject;

import java.util.Date;

abstract public class ServiceObject extends Response {
  protected Owner owner;
  protected Date updateTime;
  protected String id;
  protected String type;
  protected String vmType;
  private boolean loaded = false;

  public boolean isLoaded() { return loaded; }

  protected ServiceObject(Owner owner) {
    setOwner(owner);
  }

  public VM getVM() {
    if (owner == null) {
      // Special case for VM.
      assert this instanceof VM;
      return (VM)this;
    }
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

  public String commonToString() {
    String common = "ID=" + getId();
    common += " TYPE=" + getType();
    common += " VM_TYPE=" + getVMType();
    common += " UPDATED AT " + updateTime.toString();
    return common;
  }


  protected static String stripRef(String id) {
    if (!hasRef(id)) {
      return id;
    }
    return id.substring(1);
  }

  protected void updateCommon(JSONObject object) {
    id = Response.getIdFromMap(object);
    type = Response.getTypeFromMap(object);
    vmType = Response.getTypeFromMap(object);
    updateTime = new Date();
  }

  abstract protected void update(JSONObject object);

  public void load(final ResponseCallback callback) {
    if (isLoaded()) {
      if (callback != null) {
        callback.onResponse(this);
      }
    }
    reload(callback);
  }

  public void reload(final ResponseCallback callback) {
    VM vm = getVM();
    // All ServiceObjects have a VM.
    assert vm != null;
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
