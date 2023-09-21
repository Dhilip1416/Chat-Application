package com.example.mqtt;


import static com.example.mqtt.MainActivity.broker;
import static com.example.mqtt.MainActivity.password;
import static com.example.mqtt.MainActivity.username;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.ByteArrayOutputStream;
public class Publish_fragment extends Fragment {
    private ImageView imageView;
    private MqttAndroidClient mqttClient;
    private String topic;

    private EditText topic_for_pub;
    private ActivityResultLauncher<PickVisualMediaRequest> launcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), o -> {
            if (o == null) {
                Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
            } else {
                Glide.with(getActivity().getApplicationContext()).load(o).into(imageView);
            }
        });

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
        View rootView = inflater.inflate(R.layout.fragment_publish_fragment, container, false);

        imageView = rootView.findViewById(R.id.imageView);
        topic_for_pub = rootView.findViewById(R.id.dataFrom2);
        ImageView pickImage = rootView.findViewById(R.id.imageView3);
        Button publishButton = rootView.findViewById(R.id.sendData1btn);

        pickImage.setOnClickListener(view -> launcher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        publishButton.setOnClickListener(view -> {
            // Get the image drawable from the ImageView
            Drawable imageDrawable = imageView.getDrawable();

            // Convert the drawable to a bitmap
            Bitmap bitmap = null;
            if (imageDrawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) imageDrawable).getBitmap();
            } else {
                Toast.makeText(getActivity(), "Invalid image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert the bitmap to a byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageBytes = stream.toByteArray();

            // Check if the MQTT client is connected
            if (mqttClient != null && mqttClient.isConnected()) {
                // Get the topic from the EditText
                topic = topic_for_pub.getText().toString().trim();

                // Check if the topic is not empty
                if (!TextUtils.isEmpty(topic)) {
                    // Publish the image as an MQTT message
                    try {
                        publishImage(mqttClient, bitmap, 1, topic);
                    } catch (MqttException e) {
                        Toast.makeText(getActivity(), "Failed to publish image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Topic cannot be empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "MQTT client is not connected", Toast.LENGTH_SHORT).show();
            }
        });



        return rootView;
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
            Toast.makeText(getActivity(), "Failed to initialize MQTT client: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void publishImage(@NonNull MqttAndroidClient client, @NonNull Bitmap imageBitmap, int qos, @NonNull String topic)
            throws MqttException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageData = stream.toByteArray();
        MqttMessage message = new MqttMessage(imageData);
        message.setId(320);
        message.setRetained(false);
        message.setQos(qos);
        client.publish(topic, message);
    }
}
