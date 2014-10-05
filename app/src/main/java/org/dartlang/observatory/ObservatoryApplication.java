package org.dartlang.observatory;

import android.app.Application;
import android.content.res.Configuration;

import org.dartlang.service.Network;
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
        Network.initialize();
        vm = new VM(this, "http://10.0.2.2:8181/ws");
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
        this.vm = null;
    }

    public void connected(VM vm) {
        Logger.info("Observatory has new VM: " + vm.uri);
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
        this.vm = null;
    }


}
