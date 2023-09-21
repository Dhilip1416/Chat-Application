package com.example.mqtt;


import static com.example.mqtt.MainActivity.broker;
import static com.example.mqtt.MainActivity.password;
import static com.example.mqtt.MainActivity.username;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;
public class Image_processor extends AppCompatActivity {

    Button  secondFragmentBtn;

    private MqttAndroidClient client;
    private String clientid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        secondFragmentBtn = findViewById(R.id.subscribeButton);


        secondFragmentBtn.setOnClickListener(v -> {
            replaceFragment(new Subscriber_fragment());
            secondFragmentBtn.setVisibility(View.GONE); // Hide the button after clicking it
        });

        // Set up MQTT client and connect
        String brokerUrl = "tcp://" + broker + ":1883"; // Replace with your MQTT broker address
        String mqttUsername = username; // Replace with your MQTT username (if required)
        String mqttPassword = password; // Replace with your MQTT password (if required)
        setupMqttClient(brokerUrl, mqttUsername, mqttPassword);

        ImageView Back_button  = findViewById(R.id.back_button);

        Back_button.setOnClickListener(v -> finish());

        replaceFragment(new Publish_fragment());


    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect MQTT client when the activity is destroyed
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupMqttClient(String brokerUrl, String username, String password) {
        try {
            clientid = "mqtt" + new Random().nextInt(5000 - 1) + 1;
            client = new MqttAndroidClient(this, brokerUrl, clientid);
            ImageView connection_image = findViewById(R.id.img);
            TextView tvMessage = findViewById(R.id.connection_status);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            // Set the keep-alive interval (in seconds)
            options.setKeepAliveInterval(60); // Adjust this value as needed

            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    tvMessage.setText("Connected");
                    connection_image.setImageResource(R.drawable.green);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    tvMessage.setText("Disconnected");
                    connection_image.setImageResource(R.drawable.red);
                    Toast.makeText(Image_processor.this, "Failed to connect to the MQTT broker: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Handle client reconnection
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        // Handle successful reconnection, if needed
                        Toast.makeText(Image_processor.this, "Reconnected to the MQTT broker", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    // Handle connection lost and attempt reconnection, if needed
                    Toast.makeText(Image_processor.this, "Connection lost. Attempting to reconnect...", Toast.LENGTH_SHORT).show();
                    try {
                        client.reconnect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Handle received messages, if needed
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Handle message delivery completion, if needed
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize MQTT client: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
