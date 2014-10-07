package org.dartlang.observatory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.dartlang.service.Owner;
import org.dartlang.service.Response;
import org.dartlang.service.VM;

/**
 * The Connect Activity is the bottom of the activity stack. The activity is responsible for
 * delegating all network activity onto the UI thread.
 */
public class Connect extends Activity implements VM.EventListener {
  private final String defaultAddress = "10.0.2.2:8181";
  private EditText addressEditText;
  private TextView statusTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Logger.info("Connect.onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_connect);
    addressEditText = (EditText)findViewById(R.id.vm_address);
    statusTextView = (TextView)findViewById(R.id.status_text);
    statusTextView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        navigateVMButton();
      }
    });
    addressEditText.setText(defaultAddress);
    addressEditText.setSelection(defaultAddress.length());
    addressEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        final boolean isEnterEvent = (event != null) &&
                                     (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
        final boolean isEnterUpEvent = isEnterEvent &&
                                       (event.getAction() == KeyEvent.ACTION_UP);
        final boolean isEnterDownEvent = isEnterEvent &&
                                         (event.getAction() == KeyEvent.ACTION_DOWN);

        if (actionId == EditorInfo.IME_ACTION_DONE || isEnterUpEvent) {
          // Same as button press.
          connect(textView);
          return true;
        } else if (isEnterDownEvent) {
          // Consume to receive ACTION_EVENT.
          return true;
        }
        return false;
      }
    });

    ObservatoryApplication app = (ObservatoryApplication)getApplication();
    if (app.getVM() == null) {
      // Try and connect.
      connect(null);
    }
    updateVMButton(app.getVM());
  }

  private void connectToVM(String address) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();

    VM vm = app.getVM();
    if (vm != null) {
      Logger.info("Forcing disconnect from " + vm.uri);
      vm.disconnect();
    }
    Logger.info("Creating new VM for " + address);
    vm = new VM(this, address);
  }

  public void connect(View view) {
    String address = addressEditText.getText().toString();
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

  /* VM.EventListener interface */
  public void onConnectionFailed(final VM vm) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast("Could not connect to " + vm.uri);
        app.setVM(null);
        updateVMButton(null);
      }
    });
  }

  public void onConnection(final VM vm) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    final Context context = getApplicationContext();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast("Connected to " + vm.uri);
        app.setVM(vm);
        updateVMButton(vm);
        Intent launchVM = new Intent(context, VMView.class);
        startActivity(launchVM);
      }
    });
  }

  public void onConnectionLost(final VM vm) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    final Context context = getApplicationContext();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast("Connection to " + vm.uri + " lost");
        app.setVM(null);
        updateVMButton(null);
      }
    });
  }

  public void onResponse(final VM vm, final Owner.RequestCallback callback, final Response response) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    final Context context = getApplicationContext();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        callback.onResponse(response);
      }
    });
  }

  private void toast(String message) {
    Context context = getApplicationContext();
    int duration = Toast.LENGTH_SHORT;
    Toast toast = Toast.makeText(context, message, duration);
    toast.show();
  }

  private void updateVMButton(VM vm) {
    if (vm == null) {
      statusTextView.setText(R.string.connect_to_vm);
    } else {
      statusTextView.setText(vm.uri.toString());
    }
  }

  private void navigateVMButton() {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    if (app.getVM() == null) {
      // Do nothing.
      return;
    }
    final Context context = getApplicationContext();
    Intent launchVM = new Intent(context, VMView.class);
    startActivity(launchVM);
  }

}
