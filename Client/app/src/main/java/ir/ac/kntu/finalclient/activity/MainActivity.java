package ir.ac.kntu.finalclient.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ir.ac.kntu.finalclient.R;
import ir.ac.kntu.finalclient.SmartTempApplication;
import ir.ac.kntu.finalclient.model.SmartTempController;
import ir.ac.kntu.smarttemp.event.SmartTemp;
import ir.ac.kntu.smarttemp.event.TempReport;
import ir.ac.kntu.smarttemp.event.TempReportList;


public class MainActivity extends AppCompatActivity {

    /*private static final String USER_EXTERNAL_ID = "userid";
    private static final String USER_ACCESS_TOKEN = "token";*/
    private SmartTempController smartTempController;

    TextView sensorInfo;
    TextView updateField;
    TextView sensorOne;
    TextView sensorTwo;
    TextView currentTemperatureField1;
    TextView currentTemperatureField2;

    public static String username;
    public static String password;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        showStartFragment();


        System.out.println(username+"----------------------"+password);

        smartTempController = SmartTempApplication.app(this).getSmartTempController();
        smartTempController.attachToUser(username, password, new UserAttachCallback()
        {
            @Override
            public void onAttachResult(UserAttachResponse userAttachResponse)
            {
                if (userAttachResponse.getResult() == SyncResponseResultType.SUCCESS)
                {
                    Toast.makeText(getApplicationContext(),"ورود موفق",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"مشخصات کاربری معتبر نمی باشد",Toast.LENGTH_SHORT).show();
                }
            }
        });

        SmartTemp.Listener eventListener = new SmartTemp.Listener()
        {

            @Override
            public void onEvent(TempReportList tempReportList, String s) {
                System.out.println("Receive Event____________#####");

                for(int  i=0;i<tempReportList.getList().size();i++)
                {
                    final TempReport report = tempReportList.getList().get(i);
                    if(i==0)
                    {
                        System.out.println("temp data 1 : "+report.getTemp());
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                sensorInfo.setText("Sensors' Data");
                                Date date = new Date();
                                String strDateFormat = "hh:mm:ss a";
                                DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
                                String formattedDate = dateFormat.format(date);
                                updateField.setText( "Last Update: " + formattedDate);
                                sensorOne.setText("Sensor ID: 01");
                                currentTemperatureField1.setText(String.format(Double.valueOf(report.getTemp()) + " ℃"));
                            }
                        });

                    }
                    else if(i==1)
                    {
                        System.out.println("temp data 2 : "+report.getTemp());
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                sensorTwo.setText("Sensor ID: 02");
                                currentTemperatureField2.setText(String.format(Double.valueOf(report.getTemp()) + " ℃"));


                            }
                        });


                    }
                }
            }

            @Override
            public void onEvent(TempReport tempReport, String s) {

            }
        };
        smartTempController.bindListener(eventListener);

    }

    public void showStartFragment() {

        sensorInfo = findViewById(R.id.sensor_info);
        updateField = findViewById(R.id.updated_field);
        sensorOne = findViewById(R.id.sensor_one);
        currentTemperatureField1 = findViewById(R.id.current_temperature_field1);

        sensorTwo = findViewById(R.id.sensor_two);
        currentTemperatureField2 = findViewById(R.id.current_temperature_field2);
        }
}
