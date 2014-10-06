package org.dartlang.service;

/**
 * Created by johnmccutchan on 10/4/14.
 */
public class ServiceObject extends Response {
  private final VM vm;
  private String id;
  private String type;
  private String vmType;
  private Isolate isolate;
  private Owner owner;

  public ServiceObject(VM vm, Isolate isolate) {
    this.vm = vm;
    setIsolate(isolate);
  }

  public VM getVM() {
    return vm;
  }

  public Isolate getIsolate() {
    return isolate;
  }

  public void setIsolate(Isolate isolate) {
    this.isolate = isolate;
    if (this.isolate == null) {
      this.owner = vm;
    } else {
      this.owner = isolate;
    }
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
