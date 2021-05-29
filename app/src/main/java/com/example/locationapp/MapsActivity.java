package com.example.locationapp;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import android.widget.Button;
import android.os.AsyncTask;

public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    Mail sender;

    final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private Button alertButton;
    private TextView alertTextView;

    String phoneNo;
    String message;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        //     PackageManager p = getPackageManager();
        //      ComponentName componentName = new ComponentName(this, MapsActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        //     p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        Context context = getApplicationContext();
        client = LocationServices.getFusedLocationProviderClient(this);


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

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

        } else {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //startService(new Intent(MapsActivity.this,MyService.class));

        //Assign variable
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Initialize fused location

        ContentResolver contentResolver = getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, new smsObserver(new Handler()));

    }

    private static final String COLUMN_TYPE = "type";
    private static final int MESSAGE_TYPE_SENT = 2;

    class smsObserver extends ContentObserver {
        Context context = getApplicationContext();

        public smsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {

            Uri uriSMSURI = Uri.parse("content://sms//sent");
            Cursor cur = null;

            int id = 0;
            String message = null;
            String address = null;
            String test = null;

            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);

            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                cur = getContentResolver().query(uriSMSURI, null, null, null, null);
                cur.moveToNext();

                id = cur.getInt(cur.getColumnIndex("type"));
                if (id == 2) {

                    address = cur.getString(cur.getColumnIndex("address"));
                    message = cur.getString(cur.getColumnIndex("body"));

                    if (!address.equals("123456789")) {
                        if (ContextCompat.checkSelfPermission(context,
                                Manifest.permission.SEND_SMS)
                                == PackageManager.PERMISSION_GRANTED) {

                            String line = "Message: " + message + " to: " + address;
                            sender = new Mail("", "");
                            StringMake.setString(line);
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.
                                    Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);

                            try {
                                new MapsActivity.MyAsyncClass_sms().execute();
                            } catch (Exception ex) {
                            }

                        }
                    }
                }
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                        Manifest.permission.READ_SMS)) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.READ_SMS}, 2);
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.READ_SMS}, 2);
                }

            }

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
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, MapsActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        // TvSMS.setText("SMS Info here.....");
        // TvCall.setText("Phone Call Info Here...");       //Or we will never see the speaking to message
        System.out.println("*** On Stop...");

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        Toast.makeText(getApplicationContext(), "You Are Not Allowed to Exit the App", Toast.LENGTH_SHORT).show();

    }


//@Override
//public void onBackPressed(){
    //  Toast.makeText(getApplicationContext(),"You Are Not Allowed to Exit the App", Toast.LENGTH_SHORT).show();
//}


    private void getCurrentLocation() {

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //When success
                if (location != null) {
                    //Sync map
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mMap = googleMap;
                            mMap.setMyLocationEnabled(true);

                            //Initialize lat lng
                            LatLng latLng = new LatLng(location.getLatitude()
                                    , location.getLongitude());

                            alertTextView = (TextView) findViewById(R.id.AlertTextView);

                            System.out.println(Double.parseDouble(String.valueOf(location.getLongitude())));
                            if (Double.parseDouble(String.valueOf(location.getLongitude())) > 1) {

                                double lat = location.getLatitude();
                                double lng = location.getLongitude();

                                // AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                //  String pinpoint = location.getLatitude() + "\n" + location.getLongitude();
                                //   builder.setCancelable(false);
                                //  builder.setTitle("Current Location");
                                // builder.setMessage(pinpoint);


                                // builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                //   @Override
                                //   public void onClick(DialogInterface dialogInterface, int i) {

                                sendSMSMessage(lat, lng);

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


    protected void sendSMSMessage(double lat, double lng) {

        phoneNo = "123456789";
        message = "Message from ParentControl app:" + "\n" + "\n" + "Target has strayed further from the maximum allowed distance\n" + "Latitude: " + lat + "\n" + "Longitude: " + lng;
//System.out.println("Message from ParentControl app:" + "\n" + "\n" + "Target has strayed further from the maximum allowed distance\n"+"Latitude: "+ lat+"\n"+"Longitude: "+lng);
        sender = new Mail("", "");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.
                Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            new MyAsyncClass().execute();
        } catch (Exception ex)
//
        {
            //    Toast.makeText(getApplicationContext(), ex.toString(), 100).show();
        }

      /*  if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager2 = SmsManager.getDefault();
            smsManager2.sendTextMessage(phoneNo, null, message, null, null);
            //     System.out.println("all good");
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
       */
    }

    static class StringMake {
        static String b;

        static void setString(String a) {
            b = a;
        }

        static String getString() {
            String c = b;
            return c;
        }
    }

    class MyAsyncClass extends AsyncTask<Void, Void, Void> {

        //   ProgressDialog pDialog;

        @Override

        protected void onPreExecute() {

            super.onPreExecute();

            //  pDialog = new ProgressDialog(MapsActivity.this);

            //  pDialog.setMessage("Please wait...");

            //  pDialog.show();

        }


        @Override

        protected Void doInBackground(Void... mApi) {

            try {

                // Add subject, Body, your mail Id, and receiver mail Id.
                sender.sendMail("Warning from parent control", "ooooooooooof", "", "cse242017051@uniwa.gr");

            } catch (Exception ex) {

            }
            return null;
        }

        @Override

        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

            //   pDialog.cancel();

            // Toast.makeText(getApplicationContext(), "Email send", 100).show();

        }

    }

    class MyAsyncClass_sms extends AsyncTask<Void, Void, Void> {

        //   ProgressDialog pDialog;

        @Override

        protected void onPreExecute() {

            super.onPreExecute();

            //  pDialog = new ProgressDialog(MapsActivity.this);

            //  pDialog.setMessage("Please wait...");

            //  pDialog.show();

        }


        @Override

        protected Void doInBackground(Void... mApi) {

            try {

                // Add subject, Body, your mail Id, and receiver mail Id.
                String line = null;
                line = StringMake.getString();
                sender.sendMail("Warning from parent control", line, "", "cse242017051@uniwa.gr");

            } catch (Exception ex) {

            }
            return null;
        }

        @Override

        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

            //   pDialog.cancel();

            // Toast.makeText(getApplicationContext(), "Email send", 100).show();

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("NUMBER IS " + phoneNo + " and message is " + message);

                    SmsManager smsManager2 = SmsManager.getDefault();
                    smsManager2.sendTextMessage(phoneNo, null, message, null, null);

                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                    //when permission is granted
                    //call method

                }

            }

        }
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //when permission is granted
                //call method
                getCurrentLocation();

            }
        }
        if (requestCode == 2) {
            Context context = getApplicationContext();
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

                }
            } else {
                Toast.makeText(context, "No Permission granted", Toast.LENGTH_SHORT).show();
            }


        }

    }
}