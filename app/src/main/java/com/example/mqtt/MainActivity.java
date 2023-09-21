    package com.example.mqtt;

    import androidx.appcompat.app.AppCompatActivity;

    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import org.eclipse.paho.android.service.MqttAndroidClient;
    import org.eclipse.paho.client.mqttv3.IMqttActionListener;
    import org.eclipse.paho.client.mqttv3.IMqttToken;
    import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
    import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
    import org.eclipse.paho.client.mqttv3.MqttException;

    import java.util.Random;
    public class MainActivity extends AppCompatActivity {

       Button Lets_Start;
        private MqttAndroidClient client;
        private String clientid = "";

        private EditText ETpassword, ETbrokername, ETusername;

        public static String broker, username, password;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            ETbrokername = findViewById(R.id.etbrokername);
            ETusername = findViewById(R.id.etusername);
            ETpassword = findViewById(R.id.etpassword);

            broker =ETbrokername.getText().toString().trim();
            username= ETusername.getText().toString().trim();
            password = ETpassword.getText().toString().trim();

            Lets_Start = findViewById(R.id.letstart);

            ImageView Back_button  = findViewById(R.id.back_button);

            Back_button.setOnClickListener(v -> finish());

            Lets_Start.setOnClickListener(v -> {
                String  brokerUrl = "tcp://" + ETbrokername.getText().toString().trim() + ":1883";
                String mqttUsername = ETusername.getText().toString().trim();
                String mqttPassword = ETpassword.getText().toString().trim();

                Random r = new Random();
                int i1 = r.nextInt(5000 - 1) + 1;
                clientid = "mqtt" + i1;

                connectToMqttBroker(brokerUrl, mqttUsername, mqttPassword);
            });
        }

        private void connectToMqttBroker(String brokerUrl, String username, String password) {
            try {
                String clientId = MqttAsyncClient.generateClientId();
                client = new MqttAndroidClient(this, brokerUrl, clientId);
                ImageView connection_image = findViewById(R.id.img);
                TextView tvMessage  = (TextView) findViewById(R.id.connection_status);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(username);
                options.setPassword(password.toCharArray());

                client.connect(options, null, new IMqttActionListener() {
                    @Override
                    public  void onSuccess(IMqttToken asyncActionToken) {

                        tvMessage.setText("Connected");
                        connection_image.setImageResource(R.drawable.green);
                        // The connection was successful

                        Toast.makeText(MainActivity.this, "Connected to the broker", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this,Message_process.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                        tvMessage.setText("Disconnected");
                        connection_image.setImageResource(R.drawable.red);
                        // The connection failed
                        Toast.makeText(MainActivity.this, "Failed to connect to the broker", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }