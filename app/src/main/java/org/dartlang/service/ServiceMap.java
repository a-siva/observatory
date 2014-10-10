// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;


import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServiceMap extends ServiceObject {
  private final Map<String, Object> map = new HashMap<>();

  protected ServiceMap(Owner owner) {
    super(owner);
  }

  public boolean isEmpty() { return map.isEmpty(); }

  public int size() { return map.size(); }

  public Object get(String key) {
    return map.get(key);
  }

  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public Object put(String key, Object value) {
    return map.put(key, value);
  }

  public void putAll(Map<String, Object> other) {
    map.putAll(other);
  }

  public Object remove(String key) {
    return map.remove(key);
  }

  public void clear() {
    map.clear();
  }

  public Set<String> keySet() {
    return map.keySet();
  }

  public Collection<Object> values() {
    return map.values();
  }

  public java.util.Set<java.util.Map.Entry<String,Object>> entrySet() {
    return map.entrySet();
  }

  protected void update(JSONObject object) {

  }
}
