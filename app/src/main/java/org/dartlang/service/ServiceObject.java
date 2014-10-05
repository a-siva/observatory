package org.dartlang.service;

/**
 * Created by johnmccutchan on 10/4/14.
 */
public class ServiceObject extends Response {
    private String id;
    private String type;
    private String vmType;

    private final VM vm;
    private Isolate isolate;
    private Owner owner;

    public VM getVM() { return vm; }
    public Isolate getIsolate() { return isolate; }
    public Owner getOwner() { return owner; }

    public ServiceObject(VM vm, Isolate isolate) {
        this.vm = vm;
        setIsolate(isolate);
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getVMType() { return vmType; }

    public void setIsolate(Isolate isolate) {
       this.isolate = isolate;
       if (this.isolate == null) {
           this.owner = vm;
       } else {
           this.owner = isolate;
       }
    }
}
