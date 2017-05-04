package com.example.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Created by lijun on 2017/3/15.
 * //使用udp 组播，进行心跳监听
 * {code:30,content:[{ studentId:”S49588”, className:””,studentName:””,ip:”10.1.1.123” }]}
 *  地址及端口：236.0.0.1:3000
 *
 */
public class MultiCastActivity extends AppCompatActivity {
    private static final String TAG = "MultiCastActivity";
    private TextView tv_send;
    private TextView tv_receive;
    private static final String  MULTICAST_IP = "236.0.0.1";
    private static final int  MULTICAST_PORT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cilen_activity);
        tv_send = (TextView) findViewById(R.id.t_send);
        tv_receive = (TextView) findViewById(R.id.t_receive);
    }

    /*
    * 1.发送数据包的buffer2要改成buffer，然后删掉上面的buffer定义
      2.组播地址224.0.0.4和224.0.0.1要改成相同的
      3.发送端的socket端口不重要，但是接收端的socket端口和发送端的packet目标端口必须相同，也就是说要把4006和4000改成一样的
    * */

    public  void Received (View view){
        new Thread(){
            @Override
            public void run() {
                //Received
                InetAddress group = null; // 组播地址
                try {
                    group = InetAddress.getByName(MULTICAST_IP);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                int port = MULTICAST_PORT; // 端口
                MulticastSocket msr = null;
                try {
                    msr = new MulticastSocket(port); // 1.创建一个用于发送和接收的MulticastSocket组播套接字对象
                    msr.joinGroup(group); // 3.使用组播套接字joinGroup(),将其加入到一个组播
                    byte[] buffer = new byte[8192];
                    Log.d(TAG,"接收数据包启动！（启动时间：）" + new Date() + ")");
                    while (true) {
                        DatagramPacket dp = new DatagramPacket(buffer, buffer.length); // 2.创建一个指定缓冲区大小及组播地址和端口的DatagramPacket组播数据包对象
                        msr.receive(dp); // 4.使用组播套接字的receive（）方法，将组播数据包对象放入其中，接收组播数据包
                        String s = new String(dp.getData(), 0, dp.getLength()); // 5.解码组播数据包提取信息，并依据得到的信息作出响应
                        System.out.println(s);
                        final String finalIp = s;
                        MultiCastActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_receive.setText("接收Msg；"+finalIp);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                } finally {
                    if (msr != null) {
                        try {
                            msr.leaveGroup(group); // 7.使用组播套接字的leaveGroup()方法，离开组播组
                            msr.close(); // 关闭组播套接字
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }.start();
    }
    Thread t;
    public void Sender  (View view) {
     t =  new Thread(){
            @Override
            public void run() {
                InetAddress group = null; // 组播地址
                try {
                    group = InetAddress.getByName(MULTICAST_IP);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                MulticastSocket mss = null;
                int port = MULTICAST_PORT; // 端口
                try {
                    mss = new MulticastSocket(port); // 1.创建一个用于发送和接收的MulticastSocket组播套接字对象
                    mss.joinGroup(group); // 3.使用组播套接字joinGroup(),将其加入到一个组播
                    Log.d(TAG,"发送数据包启动！（启动时间：）" + new Date() + ")");
                    while (true) {
                        String message = "Hello" + new Date()+ "/"+t.getName();
                        final String finalIp = message;
                        MultiCastActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_send.setText(finalIp);
                            }
                        });
                        byte[] buffer = message.getBytes(); // 2.创建一个指定缓冲区大小及组播地址和端口的DatagramPacket组播数据包对象

                        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, group, port);
                        // msr.receive(dp); //接收组播数据包

                        mss.send(dp); // 4.使用组播套接字的send()方法，将组播数据包对象放入其中，发送组播数据包
                        // String s = new String(dp.getData(), 0, dp.getLength()); //5.解码组播数据包提取信息，并依据得到的信息作出响应
                        Log.d(TAG,"发送数据包给" + group + ":" + port);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (mss != null) {
                        try {
                            mss.leaveGroup(group); // 7.使用组播套接字的leaveGroup()方法，离开组播组
                            mss.close(); // 关闭组播套接字
                        } catch (IOException e) {
                        }
                    }
                }
            }
        };
        t.start();
    }



}
