package com.example.testussd;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

public class MainActivity extends AppCompatActivity {

//    public Button checkNumber;
    public Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_indicator);
        progressDialog.setCancelable(false);
        LinearProgressIndicator progressIndicator = progressDialog.findViewById(R.id.dialog);
        progressIndicator.setIndeterminate(true);
//        checkNumber = findViewById(R.id.check_number);
//        checkNumber.setOnClickListener(view -> {
            dialUssd();
//        });
    }

    public void dialUssd() {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_alert_dialog);
        dialog.setCancelable(false);
        ImageView imageView = dialog.findViewById(R.id.alert_icon);
        imageView.setVisibility(View.VISIBLE);
        TextView message = dialog.findViewById(R.id.alert_message);
        message.setVisibility(View.VISIBLE);
        TextView title = dialog.findViewById(R.id.alert_title);
        dialog.findViewById(R.id.btn_neutral).setOnClickListener(view -> {
            dialog.dismiss();
            finish();
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 234);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

//            progressDialog.("Processing");
            progressDialog.show();
            TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            String carrier = manager.getSimOperator();

            if (carrier.equals("41001")) {
//                String ussdCode = "";
//
//                switch (carrier) {
//                    case "41001":
//                    case "41007":
//                        ussdCode = "*99#";
//                        break;
//                    case "41003":
//                        ussdCode = "*780*3#";
//                        break;
//                    case "41004":
//                        ussdCode = "*8#";
//                        break;
//                    case "41006":
//                        ussdCode = "*8888#";
//                        break;
//                }

                manager.sendUssdRequest("*99#", new TelephonyManager.UssdResponseCallback() {
                    @Override
                    public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                        super.onReceiveUssdResponse(telephonyManager, request, response);
                        Intent intent = new Intent(getApplicationContext(), Homepage.class);
                        intent.putExtra("msisdn", response.subSequence(response.length()-12, response.length()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
//                    imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24));
//                    message.setText("Hi, your number is " + response.subSequence(response.length()-12, response.length()));
                        progressDialog.dismiss();
                        finish();
//                    dialog.show();
                        Log.e("TAG", "onReceiveUssdResponse:  Ussd Response = " + response.toString().trim());
                    }

                    @Override
                    public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                        super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                        imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_cancel_24));
                        message.setText("An Error Occurred. Please try again");
                        title.setText("Error!");
                        progressDialog.dismiss();
                        dialog.show();
                        Log.e("TAG", "onReceiveUssdResponseFailed: " + "" + failureCode);
                    }
                }, new Handler());

            } else {
                imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_cancel_24));
                title.setText("Error!");
                message.setText("This app is for JAZZ only kindly select Jazz SIM for data to use this app");
                progressDialog.dismiss();
                dialog.show();
            }
        }
    }
}