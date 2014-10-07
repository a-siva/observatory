package org.dartlang.observatory;

import android.app.Application;

import org.dartlang.service.VM;

/**
 * Created by johnmccutchan on 10/4/14.
 */
public class ObservatoryApplication extends Application {
  protected VM vm;

  VM getVM() {
    return vm;
  }

  void setVM(VM vm) {
    if (vm == null) {
      Logger.info("No VM");
    } else {
      Logger.info("New VM(" + vm.uri + ")");
    }
    this.vm = vm;
  }

  @Override
  public void onTerminate() {
    if (vm != null) {
      vm.disconnect();
    }
    super.onTerminate();
  }
}
