package ir.ac.kntu.finalclient;
import android.app.Application;
import android.content.Context;
import ir.ac.kntu.finalclient.model.SmartTempController;

public class SmartTempApplication extends Application {

    SmartTempController smartTempController;
    @Override
    public void onCreate()
    {
        super.onCreate();
        smartTempController = new SmartTempController(this);
        smartTempController.start(null);
    }

    public static SmartTempApplication app(Context ctx) {
        return ((SmartTempApplication) ctx.getApplicationContext());
    }

    public SmartTempController getSmartTempController()
    {
        return smartTempController;
    }
}
