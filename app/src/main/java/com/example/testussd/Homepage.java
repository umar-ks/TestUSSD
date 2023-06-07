package com.example.testussd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class Homepage extends AppCompatActivity {

    TextView msisdn;
    Button balance;
    Intent intent;
    public Dialog progressDialog;
    public ScrollView alert_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        alert_view = findViewById(R.id.alert_scrollview);
        intent = getIntent();

        String msisdnText = intent.getStringExtra("msisdn");
        msisdn = findViewById(R.id.msisdn);
        msisdn.setText(msisdnText);
        balance = findViewById(R.id.btn_balance);
//        topup = findViewById(R.id.btn_topup);

        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_indicator);
        progressDialog.setCancelable(false);
        LinearProgressIndicator progressIndicator = progressDialog.findViewById(R.id.dialog);
        progressIndicator.setIndeterminate(true);

        balance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBalanceCLicked();
            }
        });

//        topup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onTopupClicked();
//            }
//        });
    }

    public void onBalanceCLicked() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_alert_dialog);
        dialog.setCancelable(true);
        ImageView imageView = dialog.findViewById(R.id.alert_icon);
        imageView.setVisibility(View.VISIBLE);
        TextView title = dialog.findViewById(R.id.alert_title);
        TextView message = dialog.findViewById(R.id.alert_message);
        MaterialButton facebookOffer = dialog.findViewById(R.id.facebook_offer);
        MaterialButton dailyBrowser = dialog.findViewById(R.id.daily_browser);
        message.setVisibility(View.VISIBLE);
        Button neutral = dialog.findViewById(R.id.btn_neutral);
        neutral.setOnClickListener(view -> {
            dialog.dismiss();
        });
        facebookOffer.setOnClickListener(view -> {
            dialog.dismiss();
            this.sendPackageUSSD("*220*3*1#");
        });
        dailyBrowser.setOnClickListener(view -> {
            dialog.dismiss();
            this.sendPackageUSSD("*117*11#");
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 234);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            progressDialog.show();

            TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            String carrier = manager.getSimOperator();
            if (carrier.equals("41001")) {
                manager.sendUssdRequest("*111#", new TelephonyManager.UssdResponseCallback() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                        super.onReceiveUssdResponse(telephonyManager, request, response);
                        if (response.toString().startsWith("Rs.")) {
                            double balance = Double.parseDouble(response.subSequence(3, 7).toString());
                            Log.e("BALANCE", String.valueOf(balance));
                            if (balance > 20) {
                                imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24));
                                title.setText("Subscribe");
                                facebookOffer.setVisibility(View.VISIBLE);
                                dailyBrowser.setVisibility(View.VISIBLE);
                                neutral.setText("Close");
                            } else if(balance > 10) {
                                imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24));
                                title.setText("Subscribe");
                                facebookOffer.setVisibility(View.VISIBLE);
                                neutral.setText("Close");
                            } else {
                                imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_cancel_24));
                                message.setText("Insufficient balance to subscribe an offer.");
                                title.setText("Error!");
                            }
                            progressDialog.dismiss();
                            dialog.show();
                            Log.e("TAG", "onReceiveUssdResponse:  Ussd Response = " + response.toString().trim());
                        } else {
                            imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_cancel_24));
                            message.setText("Insufficient balance to subscribe an offer.");
                            title.setText("Error!");
                            progressDialog.dismiss();
                            dialog.show();
                            Log.e("TAG", "onReceiveUssdResponse:  Ussd Response = " + response.toString().trim());
                        }
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

//        Random random = new Random();
//        double num = random.nextDouble()*100;
//        message.setText("Dear Customer, Your balance is: " + Math.round(num*100.0)/100.0);
//
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                progressDialog.dismiss();
//                dialog.show();
//            }
//        }, 3000);
    }


    public void sendPackageUSSD(String ussd) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_alert_dialog);
        dialog.setCancelable(true);
        ImageView imageView = dialog.findViewById(R.id.alert_icon);
        imageView.setVisibility(View.VISIBLE);
        TextView title = dialog.findViewById(R.id.alert_title);
        title.setVisibility(View.GONE);
        TextView message = dialog.findViewById(R.id.alert_message);
        message.setVisibility(View.VISIBLE);
        Button neutral = dialog.findViewById(R.id.btn_neutral);
        neutral.setOnClickListener(view -> {
            dialog.dismiss();
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 234);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            progressDialog.show();
            TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            String carrier = manager.getSimOperator();
            if (carrier.equals("41001")) {

                manager.sendUssdRequest(ussd, new TelephonyManager.UssdResponseCallback() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                        super.onReceiveUssdResponse(telephonyManager, request, response);
                        imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24));
                        message.setText(response.toString().trim());
                        progressDialog.dismiss();
                        dialog.show();
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

//    public void onTopupClicked() {
//        Dialog dialog = new Dialog(this);
//        dialog.setContentView(R.layout.custom_alert_dialog);
//        dialog.setCancelable(true);
//        EditText editText = dialog.findViewById(R.id.alert_textbox);
//        editText.setVisibility(View.VISIBLE);
//        TextView title = dialog.findViewById(R.id.alert_title);
//        title.setText("Top Up");
//        Button btn = dialog.findViewById(R.id.btn_neutral);
//        btn.setText("Topup");
//
//        dialog.show();
//        btn.setOnClickListener(view -> {
//            if (editText.getText().toString().equals("")) {
//                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
//            } else {
//                progressDialog.show();
//                Dialog dialog1 = new Dialog(this);
//                dialog1.setContentView(R.layout.custom_alert_dialog);
//                dialog1.setCancelable(true);
//                ImageView imageView = dialog1.findViewById(R.id.alert_icon);
//                imageView.setVisibility(View.VISIBLE);
//                imageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24));
//                TextView message = dialog1.findViewById(R.id.alert_message);
//                TextView title1 = dialog1.findViewById(R.id.alert_title);
//                title1.setText("Success!");
//                message.setText("Dear Customer, you have successfully top up balance of " + editText.getText().toString());
//                message.setVisibility(View.VISIBLE);
//                dialog1.findViewById(R.id.btn_neutral).setOnClickListener(view1 -> {
//                    dialog1.dismiss();
//                });
//
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    public void run() {
//                        progressDialog.dismiss();
//                        dialog.dismiss();
//                        dialog1.show();
//                    }
//                }, 3000);
//            }
//        });
//    }


    @Override
    public void onBackPressed() {
        finish();
    }
}