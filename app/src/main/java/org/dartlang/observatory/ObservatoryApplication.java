package org.dartlang.observatory;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.Toast;

import org.dartlang.service.Owner;
import org.dartlang.service.Response;
import org.dartlang.service.VM;

/**
 * Created by johnmccutchan on 10/4/14.
 */
public class ObservatoryApplication extends Application {
  private VM vm;

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Logger.info("ObservatoryApplication.onConfigurationChanged");
    super.onConfigurationChanged(newConfig);
  }

  @Override
  public void onCreate() {
    Logger.info("ObservatoryApplication.onCreate");
    vm = new VM(this, "ws://10.0.2.2:8181/ws");
    super.onCreate();
  }

  @Override
  public void onLowMemory() {
    Logger.info("ObservatoryApplication.onLowMemory");
    super.onLowMemory();
  }

  @Override
  public void onTerminate() {
    Logger.info("ObservatoryApplication.onTerminate");
    if (vm != null) {
      vm.disconnect();
    }
    super.onTerminate();
  }

  public void connectionFailed(VM vm) {
    Logger.info("Observatory has no VM");
    toast("Could not connect to " + vm.uri);
    this.vm = null;
  }

  public void connected(VM vm) {
    Logger.info("Observatory has new VM: " + vm.uri);
    toast("Connected to " + vm.uri);
    this.vm = vm;
    this.vm.get("vm", new Owner.RequestCallback() {
      @Override
      public void onResponse(Response response) {
        Logger.info("Response callback fired");
      }
    });
  }

  public void connectionClosed(VM vm) {
    Logger.info("Observatory has lost VM: " + vm.uri);
    toast("Connection to " + vm.uri + "lost");
    this.vm = null;
  }

  private void toast(String message) {
    Context context = getApplicationContext();
    int duration = Toast.LENGTH_SHORT;

    Toast toast = Toast.makeText(context, message, duration);
    toast.show();
  }
}
