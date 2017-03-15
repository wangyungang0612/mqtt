package app.android.mingjiang.com.mqttapi;

/**
 * 备注：Mqtt配置信息。
 * 作者：wangzs on 2016/1/4 17:52
 * 邮箱：wangzhaosen@shmingjiang.org.cn
 */
public class MqttConfig {

    public static final String USER_NAME = "admin";                //用户名
    public static final String PASS_WORD = "password";             //密码
    public static final String URL = "tcp://192.168.1.19:61613";  //访问URL
    public static final String TOPIC = "/inode/mychannel";                   //发布主题
    public static final int TIME_OUT = 50;                          //设置超时时间
    public static final int ALIVE_INTERVAL = 20;                    //设置会话心跳间隔时间
    public static final Boolean CLEAN_SESSION = Boolean.TRUE;      //清除缓存

}
