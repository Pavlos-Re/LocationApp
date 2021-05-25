package com.example.locationapp;

        import android.app.AlarmManager;
        import android.app.AlertDialog;
        import android.app.Notification;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.BitmapFactory;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.Build;

        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.app.NotificationCompat;
        import androidx.core.app.TaskStackBuilder;
        import androidx.core.content.ContextCompat;
        import androidx.fragment.app.FragmentActivity;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.localbroadcastmanager.content.LocalBroadcastManager;

        import android.app.Application;
        import android.app.DownloadManager;
        import android.content.BroadcastReceiver;
        import android.content.ContentResolver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.database.ContentObserver;
        import android.database.Cursor;
        import android.database.Observable;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Handler;
        import android.provider.Telephony;
        import android.telephony.SmsManager;
        import android.util.Log;
        import android.widget.TextView;
        import android.widget.Toast;

        import android.os.Bundle;

//import com.example.parent_control.R;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.FusedLocationProviderClient;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.android.gms.location.LocationServices;

        import android.location.Location;
        import android.Manifest;
        import android.content.pm.PackageManager;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.location.LocationListener;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.android.gms.tasks.Task;
        import android.hardware.Sensor;
        import android.telephony.SmsManager;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.util.Calendar;


public class MapsActivity extends AppCompatActivity {
    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;

    final int MY_PERMISSIONS_REQUEST_SEND_SMS =1;
    private Button alertButton;
    private TextView alertTextView;

    String phoneNo;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        //Assign variable
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Initialize fused location
        client = LocationServices.getFusedLocationProviderClient(this);

        ContentResolver contentResolver = getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, new smsObserver(new Handler()));

        //Check permission
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            //call method
            getCurrentLocation();

        } else {
            //When permission denied
            //Request permission
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    class smsObserver extends ContentObserver {

        private String lastSmsId;

        public smsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Uri uriSMSURI = Uri.parse("content://sms/sent");
            Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
            cur.moveToNext();
            String id = cur.getString(cur.getColumnIndex("_id"));
            String message = null;
            String address = null;
            String test = null;

            if (smsChecker(id)) {
                //if (cur.getString(cur.getColumnIndex("address")) != "1234565789") {

                address = cur.getString(cur.getColumnIndex("address"));
                message = cur.getString(cur.getColumnIndex("body"));
                System.out.println("Message: " + message + " To :" + address);

               // if (!address.equals("1234565789")) {
                //    String line = message + address;
               //     String p = "6948309344";
                //    SmsManager sms = SmsManager.getDefault();
                 //   sms.sendTextMessage(p, "1234565789", line, null, null);

               // }

            }
        }


        public boolean smsChecker(String smsId) {
            boolean flagSMS = true;

            if (smsId.equals(lastSmsId)) {
                flagSMS = false;
            } else {
                lastSmsId = smsId;
            }

            return flagSMS;
        }
    }

    protected void onStart() {

        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(MyReceiver, new IntentFilter("Fantom-Message"));
        System.out.println("*** On Start");

    }

    protected void onStop() {

        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(MyReceiver);
        // TvSMS.setText("SMS Info here.....");
        // TvCall.setText("Phone Call Info Here...");       //Or we will never see the speaking to message
        System.out.println("*** On Stop...");

    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    private void getCurrentLocation() {

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>(){
            @Override
            public void onSuccess(Location location) {
                //When success
                if(location !=null){
                    //Sync map
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mMap = googleMap;
                            mMap.setMyLocationEnabled(true);


                            //Initialize lat lng
                            LatLng latLng = new LatLng(location.getLatitude()
                                    ,location.getLongitude());




                            alertTextView = (TextView) findViewById(R.id.AlertTextView);


                            System.out.println(Double.parseDouble(String.valueOf(location.getLongitude())));

                                    if(Double.parseDouble(String.valueOf(location.getLongitude()))>23.7169700) {

                                       // AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                      //  String pinpoint = location.getLatitude() + "\n" + location.getLongitude();
                                     //   builder.setCancelable(false);
                                      //  builder.setTitle("Current Location");
                                       // builder.setMessage(pinpoint);



                                       // builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                         //   @Override
                                         //   public void onClick(DialogInterface dialogInterface, int i) {
                                        sendSMSMessage();

                                           //    // alertTextView.setVisibility(View.VISIBLE);
                                         //   }
                                       // });
                                       // builder.show();
                                    }

                            //Create marker options
                            MarkerOptions options = new MarkerOptions().position(latLng)
                                    .title("I am there")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                            //Zoom map
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                            //Add marker on map
                            googleMap.addMarker(options);

                        }
                        protected synchronized void buildGoogleApiClient() {

                            mGoogleApiClient.connect();
                        }


                    });
                }
            }
        });

    }


    protected void sendSMSMessage() {

        phoneNo = "6948309344";
        message = "Message from ParentControl app:"+"\n"+"\n"+"Warning! Maximum allowed distance exceeded!";

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);

        } else {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

            }

        }
    }

    BroadcastReceiver MyReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent BroadInt) {

            System.out.println("Action: " + BroadInt.getAction());
            System.out.println("*** On Receive...");
            String Type = BroadInt.getStringExtra("To:");
            String Mess = BroadInt.getStringExtra("Message:");
            //  if (Type.equals("SMS"))
            // TvSMS.setText(Mess);
            //  if (Type.equals("PHONE"))
            //  TvCall.setText(Mess);

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    //Toast.makeText(getApplicationContext(), "SMS sent.",
                      //Toast.LENGTH_LONG).show();
                    //when permission is granted
                    //call method

                }

            }

            }
        if (requestCode == 44){
            if(grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //when permission is granted
                //call method
                getCurrentLocation();

            }
        }


        }

    }
