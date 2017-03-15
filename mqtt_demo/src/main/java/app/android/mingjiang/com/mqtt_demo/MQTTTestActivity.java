package app.android.mingjiang.com.mqtt_demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttClient;

import app.android.mingjiang.com.mqttapi.MqttConfig;
import app.android.mingjiang.com.mqttapi.MqttSubscribeListener;

/**
 * Mqtt订阅与发布数据测试。
 */
public class MQTTTestActivity extends Activity implements MqttSubscribeListener {

    @Override
    public void onNewPushMessage(String message) {
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        Message messageObj = new Message();
        messageObj.setData(bundle);
        handler.sendMessage(messageObj);
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message) {
            String messageStr =  message.getData().getString("message");
            subscribeText.setText(messageStr);
        }
    };

    Button publishBtn;
    EditText publishEdit;
    TextView subscribeText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtttest);

        publishBtn = (Button)findViewById(R.id.publishBtn);
        publishEdit = (EditText)findViewById(R.id.publish);
        subscribeText = (TextView)findViewById(R.id.subscribe);

        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发布消息
//                MqttPublishService.sendMessage(publishClient, publishEdit.getText().toString(), "");
            }
        });
        initpublish();
        register();
        startService();
    }

    //初始化订阅
    private void initsubscribe(){

    }
    //初始化发布
    private void initpublish(){
    }

    private void register()
    {
        MqttService.addPushMesageListeners(this);
    }

    private void startService(){
        MqttService.actionStart(this);
    }

    private void endService(){
        MqttService.actionStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endService();
    }
}
