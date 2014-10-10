// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

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
import org.dartlang.service.ResponseCallback;
import org.dartlang.service.Connection;
import org.json.JSONObject;

/**
 * The Connect Activity is the bottom of the activity stack. The activity is
 * responsible for:
 * 1) Providing the UI for connecting to the service.
 * 2) Delegating all network activity onto the UI thread. This ensures all
 * modifications of ServiceObject and Activity instances are isolated to a
 * single thread.
 */
public class Connect extends Activity implements Connection.EventListener {
  private final String defaultAddress = "10.0.2.2:8181";
  private EditText addressEditText;
  private TextView statusTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
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
    if (app.getConnection() == null) {
      // Try and connect.
      connect(null);
    }
    updateConnectionLabel(app.getConnection());
  }

  private void connectToService(String address) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();

    Connection connection = app.getConnection();
    if (connection != null) {
      Logger.info("Forcing disconnect from " + connection.uri);
      connection.disconnect();
    }
    Logger.info("Connecting to " + address);
    connection = new Connection(this, address);
  }

  public void connect(View view) {
    String address = addressEditText.getText().toString();
    connectToService(address);
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
  public void onConnectFailed(final Connection connection, String reason) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast("Could not connect to " + connection.uri);
        app.setConnection(null);
        updateConnectionLabel(null);
      }
    });
  }

  public void onConnect(final Connection connection) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    final Context context = getApplicationContext();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast("Connected to " + connection.uri);
        app.setConnection(connection);
        updateConnectionLabel(connection);
        Intent launchVM = new Intent(context, VMView.class);
        startActivity(launchVM);
      }
    });
  }

  public void onDisconnect(final Connection connection) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    final Context context = getApplicationContext();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast("Disconnected from " + connection.uri);
        app.setConnection(null);
        updateConnectionLabel(null);
      }
    });
  }

  public void onConnectionLost(final Connection connection) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    final Context context = getApplicationContext();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast("Lost connection to " + connection.uri);
        app.setConnection(null);
        updateConnectionLabel(null);
      }
    });
  }

  public void onResponse(final Connection connection,
                         final Owner owner,
                         final ResponseCallback callback,
                         final String id,
                         final JSONObject responseMap) {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    final Context context = getApplicationContext();

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // Get response object. This must be done on the UI thread so that
        // all access to ServiceObjects (e.g. VM, Isolate, etc) is isolated
        // to a single thread.
        Response response = Response.makeResponse(connection,
                                                  owner,
                                                  id,
                                                  responseMap);
        // Call callback. This must be done on the UI thread because most
        // of these callbacks will update ServiceObjects or an Activity.
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

  private void updateConnectionLabel(Connection connection) {
    if (connection == null) {
      statusTextView.setText(R.string.connect_to_service);
    } else {
      statusTextView.setText(connection.uri.toString());
    }
  }

  private void navigateVMButton() {
    final ObservatoryApplication app = (ObservatoryApplication)getApplication();
    if (!app.hasConnection()) {
      // Do nothing.
      return;
    }
    final Context context = getApplicationContext();
    Intent launchVM = new Intent(context, VMView.class);
    startActivity(launchVM);
  }
}
