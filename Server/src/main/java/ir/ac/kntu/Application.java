package ir.ac.kntu;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import ir.ac.kntu.model.SystemController;
import ir.ac.kntu.smarttemp.event.*;
import ir.ac.kntu.smarttemp.log.SmartTempLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Application {

    private static final String USER_EXTERNAL_ID = "userid";
    private static final String USER_EXTERNAL_TOKEN = "token";

    private static SystemController controller;
    public static List<TempReport> temperatureList;
    public static List<TemperatureSensor> sensorList;

    public static void main(String[] args) {

        controller = new SystemController(new SmartTemp.Listener() {

            // Receive TempReport Event
            @Override
            public void onEvent(TempReport event, String source) {

            }

            // Receive TempReportQuery Event
            @Override
            public void onEvent(TempReportQuery query, String source)
            {
                TempReportList reportList=new TempReportList();
                List<TempReport> selectedReports = new ArrayList<>();
                for(Integer id : query.getSelectionList())
                {
                    selectedReports.add(temperatureList.get(id));
                }
                reportList.setList(selectedReports);
                controller.sendTemperatureReportList(reportList,source);
            }
        });

        controller.start();
        controller.attachToUser(USER_EXTERNAL_ID, USER_EXTERNAL_TOKEN);

        sensorList=new ArrayList<>();
        temperatureList=new ArrayList<>();

        W1Master master = new W1Master();
        List<W1Device> w1Devices = master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);
        for (int i=0;i<w1Devices.size();i++)
        {
            W1Device device= w1Devices.get(i);
            sensorList.add((TemperatureSensor) device);

            TempReport report=new TempReport();
            report.setID(i);
            report.setTemp(sensorList.get(i).getTemperature());
            temperatureList.add(report);
        }

        // Update temp value
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                for(int i=0;i<sensorList.size();i++)
                {
                    TempReport tempReport = temperatureList.get(i);
                    tempReport.setTemp(sensorList.get(i).getTemperature());
                    System.out.println("temp "+i+"__________"+tempReport.getTemp());
                }
                TempReportList tempReportList = new TempReportList();
                tempReportList.setList(temperatureList);
                controller.sendTemperatureReportList(tempReportList);
            }
        },0,5000);

        // Send log
        SmartTempLog smartTempLog = new SmartTempLog();
        smartTempLog.setTempReportList((TempReportList) temperatureList);
        controller.sendLog(smartTempLog);

    }



}
