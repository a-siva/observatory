// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.observatory;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.dartlang.service.Response;
import org.dartlang.service.ResponseCallback;
import org.dartlang.service.VM;


public class VMView extends Activity implements ResponseCallback {
  private ObservatoryApplication app;
  private VM vm;
  private TextView version;
  private TextView targetCPU;
  private TextView hostCPU;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_vm_view);
    version = (TextView)findViewById(R.id.version);
    targetCPU = (TextView)findViewById(R.id.target_cpu);
    hostCPU = (TextView)findViewById(R.id.host_cpu);
    app = (ObservatoryApplication)getApplication();
    Logger.info("VMView.onCreate");
    loadVM();
  }

  void loadVM() {
    vm = app.getVM(this);
    if (vm != null) {
      Logger.info("VMView.reload");
      vm.reload(this);
      return;
    }
  }

  public void onResponse(String id, Response response) {
    Logger.info("VMView.onResponse");
    assert id.equals("vm");
    vm = (VM)response;
    updateView();
  }

  private void updateView() {
    updateActionBar();
    if (vm == null) {
      version.setText("");
      targetCPU.setText("");
      hostCPU.setText("");
      return;
    }
    version.setText(vm.version);
    targetCPU.setText(vm.targetCPU);
    hostCPU.setText(vm.hostCPU);
  }

  private void updateActionBar() {
    assert app != null;
    ActionBar actionBar = getActionBar();
    if (vm == null) {
      actionBar.setTitle("No VM");
    } else {
      actionBar.setTitle(app.connection.uri);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.vm, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
