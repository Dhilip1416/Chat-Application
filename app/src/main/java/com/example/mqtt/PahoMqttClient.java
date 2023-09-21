package com.example.mqtt;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class PahoMqttClient {
        private static final String TAG = "PahoMqttClient";
        public MqttAndroidClient mqttAndroidClient;

        public MqttAndroidClient getMqttClient(Context context, String brokerUrl, String clientId, String clientUn, String clientPw) {
            mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId);
            try {
                MqttConnectOptions myMqttcnxoptions = getMqttConnectionOption();
                if(clientUn.trim().length() > 0) {
                    myMqttcnxoptions.setUserName(clientUn);
                }
                if(clientPw.trim().length() > 0) {
                    myMqttcnxoptions.setPassword(clientPw.toCharArray());
                }
                IMqttToken token = mqttAndroidClient.connect(myMqttcnxoptions);

                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                        Log.d(TAG, "Success");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d(TAG, "Failure " + exception.toString());
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }

            return mqttAndroidClient;
        }


        @NonNull
        private DisconnectedBufferOptions getDisconnectedBufferOptions() {
            DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
            disconnectedBufferOptions.setBufferEnabled(true);
            disconnectedBufferOptions.setBufferSize(100);
            disconnectedBufferOptions.setPersistBuffer(false);
            disconnectedBufferOptions.setDeleteOldestMessages(false);
            return disconnectedBufferOptions;
        }

        @NonNull
        private MqttConnectOptions getMqttConnectionOption() {
            //private MqttConnectOptions getMqttConnectionOption(String clientUn, String clientPw) {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setAutomaticReconnect(true);
            //mqttConnectOptions.setWill(Constants.PUBLISH_TOPIC, "I am going offline".getBytes(), 1, true);
            //mqttConnectOptions.setUserName("dave_test1");
            //mqttConnectOptions.setPassword("dave_test123".toCharArray());
            //mqttConnectOptions.setUserName(un);
            //mqttConnectOptions.setPassword(clientPw.toCharArray());
            return mqttConnectOptions;
        }


}