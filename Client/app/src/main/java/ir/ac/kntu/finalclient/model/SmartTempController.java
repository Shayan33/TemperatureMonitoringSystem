package ir.ac.kntu.finalclient.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;

import ir.ac.kntu.smarttemp.event.SmartTemp;

public class SmartTempController
{
    private static final String KEYS_DIR = "keys_for_java_event_demo";

    private KaaClient kaaClient;

    private SmartTemp smartTemp;

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    private Context context;

    public SmartTempController(Context context)
    {
        this.context = context;
    }

    public void start(@Nullable final Runnable successCallback)
    {
        try
        {
            // Setup working directory for endpoint
            KaaClientProperties endpointProperties = new KaaClientProperties();
            endpointProperties.setWorkingDirectory(KEYS_DIR);

            // Create the Kaa desktop context for the application
            AndroidKaaPlatformContext androidKaaPlatformContext = new AndroidKaaPlatformContext(context, endpointProperties);

            kaaClient = Kaa.newClient(androidKaaPlatformContext, new SimpleKaaClientStateListener()
            {
                @Override
                public void onStarted()
                {
                    toast("kaa -> start");
                    kaaClient.getConfiguration();
                    final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
                    smartTemp = eventFamilyFactory.getSmartTemp();

                    if (successCallback != null)
                    {
                        successCallback.run();
                    }
                }
                @Override
                public void onStopped()
                {
                }
            }, true);

            //set client profile
            /*kaaClient.setProfileContainer(new ProfileContainer() {
                @Override
                public SmartHomeCP getProfile()
                {
                    SmartHomeCP smartHomeCP =new SmartHomeCP();
                    smartHomeCP.setAppType("controller");
                    smartHomeCP.setPlatform("android");
                    return smartHomeCP;
                }
            });*/

            //Start the Kaa client and connect it to the Kaa server.
            kaaClient.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void attachToUser(final String userId,String userAccessToken, final UserAttachCallback callback)
    {
        // Attach the endpoint to the user
        // This demo application uses a trustful verifier, as a result
        // any user credentials sent by the endpoint are accepted as valid.
        kaaClient.attachUser(userId, userAccessToken, new UserAttachCallback()
        {
            @Override
            public void onAttachResult(UserAttachResponse response)
            {
                if (response.getResult() == SyncResponseResultType.SUCCESS)
                {
                    toast("success attach user -> "+userId);
                }
                else
                {
                    toast("failed attach user");
                }
                System.out.println(callback+"-------------------");
                if (callback != null)
                {
                    callback.onAttachResult(response);
                }
            }
        });
    }

    private void toast(final String toast)
    {
        mMainThreadHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public SmartTemp getSmartHomeEventFamily()
    {
        return smartTemp;
    }



    public void stop()
    {
        kaaClient.stop();
    }

    public void bindListener(SmartTemp.Listener listener)
    {
        boolean result = smartTemp.addListener(listener);
        System.out.println("bind result----->"+result);
    }

    public void removeListener(SmartTemp.Listener listener)
    {
        smartTemp.removeListener(listener);
    }
}
