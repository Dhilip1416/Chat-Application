package com.example.mqtt;

import static com.example.mqtt.MainActivity.broker;
import static com.example.mqtt.MainActivity.password;
import static com.example.mqtt.MainActivity.username;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Subscriber_fragment extends Fragment {

    private MqttAndroidClient mqttClient;
    private String topic;
    private ImageView imageView;
    private Button startSubscriberBtn;
    private EditText topic_for_sub;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up MQTT client and connect
        String brokerUrl = "tcp://" + broker + ":1883"; // Replace with your MQTT broker address
        String mqttUsername = username; // Replace with your MQTT username (if required)
        String mqttPassword = password; // Replace with your MQTT password (if required)
        setupMqttClient(brokerUrl, mqttUsername, mqttPassword);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_subscriber_fragment, container, false);
        imageView = rootView.findViewById(R.id.imageView);
        startSubscriberBtn = rootView.findViewById(R.id.sendData1btn);


        startSubscriberBtn.setOnClickListener(v -> startSubscriber());
        topic_for_sub = rootView.findViewById(R.id.dataFrom2);
        return rootView;
    }

    private void startSubscriber() {
        // Check if the MQTT client is connected before subscribing
        if (mqttClient != null && mqttClient.isConnected()) {
            // Get the topic from the EditText
            topic = topic_for_sub.getText().toString().trim();

            // Validate the topic
            if (topic.isEmpty()) {
                Toast.makeText(getActivity(), "Topic cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                // Subscribe to the topic
                try {
                    subscribeToTopic(mqttClient, topic, 1); // Replace '1' with the desired QoS level
                } catch (MqttException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Failed to subscribe to topic: " + topic, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getActivity(), "MQTT client is not connected", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unsubscribe from the topic and disconnect MQTT client when the fragment is destroyed
        if (mqttClient != null) {
            try {
                if (mqttClient.isConnected()) {
                    unsubscribeFromTopic(mqttClient, topic);
                    mqttClient.disconnect();
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupMqttClient(String brokerUrl, String username, String password) {
        try {
            String clientId = MqttAsyncClient.generateClientId();
            mqttClient = new MqttAndroidClient(getActivity(), brokerUrl, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getActivity(), "Connected to the MQTT broker", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getActivity(), "Failed to connect to the MQTT broker", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopic(@NonNull MqttAndroidClient client, @NonNull String topic, int qos)
            throws MqttException {
        client.subscribe(topic, qos, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // Subscription successful
                Toast.makeText(getActivity(), "Subscribed to topic: " + topic, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                // Subscription failed
                Toast.makeText(getActivity(), "Failed to subscribe to topic: " + topic, Toast.LENGTH_SHORT).show();
            }
        });

        // Set up message callback to handle received messages
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // Connection complete
            }

            @Override
            public void connectionLost(Throwable cause) {
                // Connection lost
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // Message received
                byte[] imageData = message.getPayload();
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                // Check if the imageView is not null before setting the image
                if (imageView != null) {
                    imageView.setImageBitmap(imageBitmap);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Delivery complete
            }
        });
    }

    private void unsubscribeFromTopic(@NonNull MqttAndroidClient client, @NonNull String topic)
            throws MqttException {
        client.unsubscribe(topic, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // Unsubscribe successful
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Unsubscribed from topic: " + topic, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                // Unsubscribe failed
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to unsubscribe from topic: " + topic, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}

