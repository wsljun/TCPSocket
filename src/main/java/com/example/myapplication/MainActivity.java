package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.myapplication.BaseActivity;
import com.example.myapplication.R;
import com.example.myapplication.SocketService;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 */
public class MainActivity extends BaseActivity implements View.OnClickListener{
    SocketService socketService;
    private final String TAG = "MainActivity";
    private int port = 3001;
    private String  serverAddress = "10.1.1.202";//豆包ip
    private boolean isServer = false;
    private boolean isClient = false;
    private SocketServiceConn socketServiceConn;
    private Socket clientsocket;
    private EditText edMsg;
    private DataInputStream dis = null; //从服务端读取数据
    private DataOutputStream dos = null; //向服务端写入数据
    private boolean isConnection = false; //标记客户端连接成功
    private Button btnStartServer;
    private Button btnStartClient,btnSend;
    private TextView tv_msg;
    private String localIp ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socketServiceConn = new SocketServiceConn();
        localIp = getLocalIpAddress();
        initView();
        bindService();
    }

    private void initView() {
        edMsg = (EditText) findViewById(R.id.edit_msg);
        btnStartClient =   (Button)findViewById(R.id.btnClient);
        btnStartServer =   (Button)findViewById(R.id.btnServer);
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        btnSend = (Button) findViewById(R.id.btnSend);
        tv_msg.setText(localIp);
    }

    private void bindService(){
        Intent intent = new Intent(this,SocketService.class);
        bindService(intent,socketServiceConn,BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btnServer:// start server
                isServer = true;
                btnStartClient.setEnabled(false);
                btnSend.setEnabled(false);
                socketService.startServer();
                break;
            case R.id.btnClient: //start　client
                isClient = true;
                startClient();
                break;
            case R.id.btnSend:  //send msg
                if(isClient){
                    String s  = edMsg.getText().toString();
                    sendMsgToServer(s);
                }else{
                    Toast.makeText(this,"请先开启客户端连接",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    /**
     * 创建客户端
     */
    private void startClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientsocket = new Socket(serverAddress,port);

                    //写入流中 //方法一
                    DataOutputStream dos = new DataOutputStream(clientsocket.getOutputStream());
                    dos.writeUTF(localIp+":上线");
                    dos.flush();

                    //////////////////////////////方法二///////////////////////////////////
                    //根据输入输出流和服务端连接
                    /*OutputStream outputStream=clientsocket.getOutputStream();//获取一个输出流，向服务端发送信息
                    PrintWriter printWriter=new PrintWriter(outputStream);//将输出流包装成打印流
                    printWriter.print("服务端你好，clientsocket");
                    printWriter.flush();
                    clientsocket.shutdownOutput();//关闭输出流

                    InputStream inputStream=clientsocket.getInputStream();//获取一个输入流，接收服务端的信息
                    InputStreamReader inputStreamReader=new InputStreamReader(inputStream);//包装成字符流，提高效率
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);//缓冲区
                    String info="";
                    String temp=null;//临时变量
                    while((temp=bufferedReader.readLine())!=null){
                        info+=temp;
                        Log.d(TAG, "startClient run: 客户端接收服务端发送信息："+info);
                    }

                    //关闭相对应的资源
                    bufferedReader.close();
                    inputStream.close();
                    printWriter.close();
                    outputStream.close();
                    clientsocket.close();*/

                    //////////////////////////////////////////////////////////////
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "startClient run: 客户端连接失败："+e.toString());
                }
//
//                Message msg = new Message();
//                msg.what = 1;
//                handler.sendMessage(msg);
            }
        }).start();

    }

    private void receiveMsg() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //刷新消息
                while (true){
                    if(isConnection){
                        try {
                            if(dis==null){
                                //获取输入流实例
                                dis = new DataInputStream(clientsocket.getInputStream());
                            }
                            String message = dis.readUTF();
                            Log.e(TAG, "receiveMsg: "+message);

                            Message msg = new Message();
                            msg.what = 2;
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", message);
                            msg.setData(bundle);
                            handler.sendMessage(msg);

                        } catch (IOException e) {
                            try {
                                Log.e(TAG, "receiveMsg: 客户端刷新数据异常 ");
                                isConnection = false;
                                //异常关闭
                                dis.close();
                                dis = null;
//                                dos.close();
//                                clientsocket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }

            }
        });
        t.start();

    }

    private void sendMsgToServer(String msg) {
        if(msg!=null&&!"".equals(msg)){

            try {
                dos = new DataOutputStream(clientsocket.getOutputStream());
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                Log.e(TAG, "sendMsgToServer: 客户端发送数据异常");
                e.printStackTrace();
            }
        }
    }

    boolean isOk = false;
    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                // 连接成功
                tv_msg.setText(localIp+"连接服务器成功!\n");
                isConnection = true;
                receiveMsg();
            }else if(msg.what == 2){
                String s = msg.getData().getString("msg");
                tv_msg.append("server:"+s + "\n");
                String cmd = s.substring(s.length()-2,s.length());
                if("11".equals(cmd)){
                    removeWindowBlackView();
                }else if("10".equals(cmd)){
                    addWindowBlackView();
                }

            }
        };
    };


    @Override
    public void onDestroy() {
        if(isServer){
            unbindService(socketServiceConn);
        }
        //client.closeSocket();//关闭socket 连接
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        super.onDestroy();
    }

    /**
     * 实现ServiceConnection 接口
     */
    class SocketServiceConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // TODO: 2017/3/15 绑定成功时回调
            SocketService.ScokerBinder b = (SocketService.ScokerBinder) service;
            socketService = b.getScoketService(); //获取socketService 实例，可以操作socketService方法/
            Log.d(TAG, "onServiceConnected: service开启成功"+localIp);
//            socketService.startServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    /**
     *
     * 获取WIFI下ip地址
     */
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();

        //返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "ljonKeyDown: ");
        return super.onKeyDown(keyCode, event);//super.onKeyDown(keyCode, event); //交给父类处理，返回false or true，父类都不会处理该事件。
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menuSysUITest){
            startActivity(new Intent(this,SystemUIStateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }




//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        // 返回true，不响应其他key
//        Log.d(TAG, "dispatchKeyEvent: ");
//        return true;
//    }











}
