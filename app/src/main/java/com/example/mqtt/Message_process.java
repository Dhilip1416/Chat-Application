package com.example.mqtt;
import static com.example.mqtt.MainActivity.broker;
import static com.example.mqtt.MainActivity.password;
import static com.example.mqtt.MainActivity.username;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
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

public class Message_process extends AppCompatActivity {

    EditText Pubtopic, PubMessage, Subtopic;
    TextView SubMessage;
    Button start_video, Pub_btn, Sub_btn;

    private MqttAndroidClient client;
    private String clientid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_process);

        start_video = findViewById(R.id.start_video);
        Pubtopic = findViewById(R.id.pubtopic);
        PubMessage = findViewById(R.id.pubmessage);
        Subtopic = findViewById(R.id.subtopic);
        SubMessage = findViewById(R.id.submessage);
        Pub_btn = findViewById(R.id.btnpub);
        Sub_btn = findViewById(R.id.btnsub);

        start_video.setOnClickListener(v -> {
            Intent intent = new Intent(Message_process.this, Image_processor.class);
            startActivity(intent);
        });

        ImageView Back_button  = findViewById(R.id.back_button);

        Back_button.setOnClickListener(v -> finish());

        Pub_btn.setOnClickListener(v -> {
            String topic = Pubtopic.getText().toString().trim();
            String message = PubMessage.getText().toString().trim();

            if (!topic.isEmpty() && !message.isEmpty()) {
                publishMessage(topic, message);
            } else {
                Toast.makeText(Message_process.this, "Topic and message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        Sub_btn.setOnClickListener(v -> {
            String topic = Subtopic.getText().toString().trim();

            if (!topic.isEmpty()) {
                subscribeToTopic(topic);
            } else {
                Toast.makeText(Message_process.this, "Subscription topic cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up MQTT client and connect
        String brokerUrl = "tcp://" + broker + ":1883"; // Replace with your MQTT broker address
        String mqttUsername = username; // Replace with your MQTT username (if required)
        String mqttPassword = password; // Replace with your MQTT password (if required)
        setupMqttClient(brokerUrl, mqttUsername, mqttPassword);
    }

    private void setupMqttClient(String brokerUrl, String username, String password) {
        try {
            clientid = "mqtt" + new Random().nextInt(5000 - 1) + 1;
            client = new MqttAndroidClient(this, brokerUrl, clientid);
            ImageView connection_image = findViewById(R.id.img1);
            TextView tvMessage  = (TextView) findViewById(R.id.connection_status);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());

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
                    Toast.makeText(Message_process.this, "Failed to connect to the MQTT broker", Toast.LENGTH_SHORT).show();
                }
            });

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                }

                @Override
                public void connectionLost(Throwable cause) {
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    Toast.makeText(Message_process.this, "Received message on topic: " + topic + ", Message: " + payload, Toast.LENGTH_SHORT).show();
                    SubMessage.setText("Received message: " + payload);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SubMessage.setText(""); // Reset the text after 5 seconds
                        }
                    }, 5000);

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }

            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishMessage(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getBytes());
            mqttMessage.setQos(1);
            mqttMessage.setRetained(false);

            client.publish(topic, mqttMessage, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(Message_process.this, "Message published successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Message_process.this, "Failed to publish the message", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopic(String topic) {
        try {
            client.subscribe(topic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(Message_process.this, "Subscribed to topic: " + topic, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Message_process.this, "Failed to subscribe to topic: " + topic, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
