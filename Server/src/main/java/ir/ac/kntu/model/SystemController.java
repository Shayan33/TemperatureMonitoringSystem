package ir.ac.kntu.model;

import ir.ac.kntu.smarttemp.event.SmartTemp;
import ir.ac.kntu.smarttemp.event.TempReport;
import ir.ac.kntu.smarttemp.event.TempReportList;
import ir.ac.kntu.smarttemp.log.SmartTempLog;
import ir.ac.kntu.smarttemp.profile.SmartTempCP;
import org.kaaproject.kaa.client.*;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.logging.BucketInfo;
import org.kaaproject.kaa.client.logging.RecordInfo;
import org.kaaproject.kaa.client.logging.future.RecordFuture;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemController {

    private static final String KEY_DIR = "keys_for_java_evnet_demo";

    private static final Logger LOG = LoggerFactory.getLogger(SystemController.class);
    private KaaClient kaaClient;
    private SmartTemp smartTempEventFamily;
    private SmartTemp.Listener eventListener;
    private ExecutorService logSenderExecutor;

    public SystemController(SmartTemp.Listener eventListener) {
        this.eventListener = eventListener;
    }

    public void start() {
        try {
            KaaClientProperties endpointProperties = new KaaClientProperties();
            endpointProperties.setWorkingDirectory(KEY_DIR);

            DesktopKaaPlatformContext desktopKaaPlatformContext = new DesktopKaaPlatformContext(endpointProperties);

            final CountDownLatch startupLatch = new CountDownLatch(1);
            kaaClient = Kaa.newClient(desktopKaaPlatformContext, new SimpleKaaClientStateListener() {

                @Override
                public void onStarted() {
                    System.out.println("--= Kaa Client Started =--");
                    startupLatch.countDown();
                }

                @Override
                public void onStopped() {
                    System.out.println("--= Kaa Client Stopped");
                }
            }, true);

            kaaClient.setProfileContainer(() -> {
                SmartTempCP smartTempCP = new SmartTempCP();
                smartTempCP.setAppType("base");
                smartTempCP.setPlatform("raspberry pi");
                return smartTempCP;
            });

            kaaClient.start();
            startupLatch.await();

            final EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
            smartTempEventFamily = eventFamilyFactory.getSmartTemp();
            smartTempEventFamily.addListener(eventListener);
            logSenderExecutor = Executors.newCachedThreadPool();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void attachToUser(String userId, String userAccessToken) {
        try {
            final CountDownLatch attachLatch = new CountDownLatch(1);
            kaaClient.attachUser(userId, userAccessToken, new UserAttachCallback() {
                @Override
                public void onAttachResult(UserAttachResponse response) {
                    LOG.info("Attack to user result:{}", response.getResult());
                    if (response.getResult() == SyncResponseResultType.SUCCESS) {
                        LOG.info("Current endpoint have been successfully attached to user [ID={}]!", userId);
                    } else {
                        LOG.error("Attaching current endpoint to user [ID={}] FAILED.", userId);
                        LOG.error("Attach response: {}", response);
                        LOG.error("Fvents exchange will be NOT POSSIBLE.");
                    }

                    attachLatch.countDown();
                }

            });

            attachLatch.await();
        } catch (InterruptedException e) {
            LOG.warn("Thread interrupted when wait for attach current endpoint to user", e);
        }
    }

    public void stop() {
        kaaClient.stop();
    }

    public void notifyTemperatureChanged(TempReport tempReport) {
        smartTempEventFamily.sendEventToAll(tempReport);
    }

    public void sendTemperatureReportList(TempReportList tempReportList, String... receivers) {
        if(receivers.length==0)
            smartTempEventFamily.sendEventToAll(tempReportList);
        else {
            for (String receiver : receivers)
                smartTempEventFamily.sendEvent(tempReportList, receiver);
        }
    }

    public void sendLog(SmartTempLog smartTempLog) {
        logSenderExecutor.execute(() -> {
            // submit log record for sending to Kaa node
            RecordFuture future = kaaClient.addLogRecord(smartTempLog);
            try {
                RecordInfo recordInfo = future.get(); // wait for log record delivery error
                BucketInfo bucketInfo = recordInfo.getBucketInfo();
                System.out.println("Received log record delivery info. Log type " +
                        "[" + smartTempLog.getTempReportList() + "] ,Bucket Id [" + bucketInfo.getBucketId() + "], Delivery time " +
                        "[" + recordInfo.getRecordDeliveryTimeMs() + " ms].");
            } catch (Exception e) {
                System.out.println("Exception was caught while waiting for log's delivery report.");
            }
        });

    }
}

