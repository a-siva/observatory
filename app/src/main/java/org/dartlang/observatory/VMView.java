package org.dartlang.observatory;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.dartlang.service.VM;


public class VMView extends Activity {
  private ObservatoryApplication app;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_vm_view);
    updateActionBar();
  }

  private void updateActionBar() {
    app = (ObservatoryApplication) getApplication();
    VM vm = app.getVM();
    ActionBar actionBar = getActionBar();
    if (vm == null) {
      actionBar.setTitle("No VM");
    } else {
      actionBar.setTitle(app.vm.uri);
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
