package org.dartlang.observatory;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.dartlang.service.VM;


public class Connect extends Activity {
  private String address = "10.0.2.2:8181";
  private EditText addressEditText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_connect);
    addressEditText = (EditText)findViewById(R.id.vm_address);
    addressEditText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        addressEditText.setText(address);
        addressEditText.setSelection(address.length());
      }
    });

    ObservatoryApplication app = (ObservatoryApplication)getApplication();
    if (app.getVM() != null) {
      // Have a VM.
      return;
    }
    connectToVM(address);
  }


  private void connectToVM(String address) {
    if (!address.startsWith("ws://")) {
      address = "ws://" + address + "/ws";
    }
    ObservatoryApplication app = (ObservatoryApplication)getApplication();
    VM vm = app.getVM();
    if (vm != null) {
      vm.disconnect();
    }
    VM vm = new VM(app, address);
  }

  public void connect(View view) {
    connectToVM(address);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.connect, menu);
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
