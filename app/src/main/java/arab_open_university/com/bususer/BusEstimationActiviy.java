package arab_open_university.com.bususer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BusEstimationActiviy extends AppCompatActivity {

    Spinner spinnerBusNum, spinnerBusStaion;
    TextView tvTimeChoosen;
    Button btnSubmit;
    TimePicker timePicker;
    List<BusStation> listOfBuses;
    List<String> busesNumbers, busesStations;
    String currentBusNum = "", currentBusStation;

    Calendar alarmCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_estimation_activiy);

        //Inflate layout views
        spinnerBusNum = (Spinner) findViewById(R.id.spinner_busNum);
        spinnerBusStaion = (Spinner) findViewById(R.id.spinner_busStation);
        tvTimeChoosen = (TextView) findViewById(R.id.tv_timeChoosen);
        btnSubmit = (Button) findViewById(R.id.btn_submit);
        timePicker = (TimePicker) findViewById(R.id.time_picker);

        listOfBuses = (List<BusStation>) getIntent().getExtras().getSerializable("buses");

        busesNumbers = getIntent().getExtras().getStringArrayList("buses_numbers");
        busesStations = new ArrayList<>();


        alarmCalendar = Calendar.getInstance();
        alarmCalendar.setTimeInMillis(System.currentTimeMillis());

        // add buses data to the spinner
        spinnerBusNum.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, busesNumbers));



        /* get user selection of buses and bus stations for that bus  and add them to the bus station spinner */
        spinnerBusNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentBusNum = busesNumbers.get(i);
                currentBusStation = "";
                busesStations.clear();
                for(BusStation busStation : listOfBuses){
                    if(busStation.getBusNumber().equals(busesNumbers.get(i))) {
                        busesStations = busStation.getBusStationsNames();
                    }
                }

                spinnerBusStaion.setAdapter(new ArrayAdapter<String>(BusEstimationActiviy.this, android.R.layout.simple_list_item_1, busesStations));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /* get user selection for specific bus station  */

        spinnerBusStaion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentBusStation = busesStations.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /* after user presses button submit check if user choose specific bus and specific location
         * to get notification how far the bus from station at specific time by firing alarm to start service at that time */

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentBusNum != ""){
                    if(currentBusStation != ""){
                        double busStationLat = 0, busStationLong = 0;
                        outerLoop:
                        for(BusStation busStation : listOfBuses){
                            if(busStation.getBusNumber().equals(currentBusNum)){
                                for(int i = 0; i < busStation.getBusStationsNames().size(); i++){
                                    if(busStation.getBusStationsNames().get(i).equals(currentBusStation)){
                                        busStationLat = busStation.getBusStationsLat().get(i);
                                        busStationLong = busStation.getBusStationsLong().get(i);
                                        break outerLoop;
                                    }
                                }
                            }
                        }

                        Intent intent = new Intent(BusEstimationActiviy.this, BusEstimationService.class);
                        Bundle extras = new Bundle();
                        extras.putDouble("lat", busStationLat);
                        extras.putDouble("long", busStationLong);
                        intent.putExtras(extras);
                        PendingIntent pendingIntent = PendingIntent.getService(BusEstimationActiviy.this, 0, intent, 0);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(),pendingIntent);
                        BusEstimationActiviy.this.finish();
                    }else {
                        Toast.makeText(BusEstimationActiviy.this, "Please, Choose Station First", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(BusEstimationActiviy.this, "Please, Choose Bus First", Toast.LENGTH_LONG).show();
                }
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                alarmCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                alarmCalendar.set(Calendar.MINUTE, minute);
                tvTimeChoosen.setText(hourOfDay + ":" + minute);
            }
        });
    }
}
