package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lj on 2017/3/13.
 * 通过service 来管理ScoketServer;
 * 该服务通过 bind() 的方式来启动,
 *  客户端连接时，必须保证和服务端在同意网络/IP地址为服务端在该网络下的IP
 */

public class SocketService extends Service {
    public static final int INT = 43;
    public static final int INT1 = 39;
    private final String TAG = "SocketService";
    private ServerSocket ss = null;
    //server 标记serverscoket 是否开启
    private boolean isStart = false;
    private int PORT = 3001;
    //使用list保存所有已连接的客户端
    private List<ClientConn> clients = new ArrayList<ClientConn>();

    @Override
    public void onCreate() {
        super.onCreate();
//        startServer();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ScokerBinder();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        isStart = false;
    }

    /**
     * 开启ServerSocket
     */
    public void startServer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ss = new ServerSocket(PORT);//创建服务器连接对象
                    isStart = true;//server 开启
                    Log.e(TAG, "startServer: 服务端开启成功--InetAddress:"+ ss.getInetAddress().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    isStart = false;
                    Log.e(TAG, "startServer: 服务端开启失败！" );
                }


                while (true){  //死循环。接受客服端的请求信息
                    if(isStart){
                        try {
                            // 实例化 socket 对象
                            Socket socket = ss.accept();

                            ////////////////方法一///////////////////
                            // 加入 客户端 集合
                            ClientConn cc = new ClientConn(socket);
                            clients.add(cc);

                            //开启线程，处理消息
                            Thread t = new Thread(cc);
                            t.start();


                            //////////////////////////////方法二///////////////////////////////////
                            //根据输入输出流和客户端连接
                          /*  InputStream inputStream=socket.getInputStream();//得到一个输入流，接收客户端传递的信息
                            InputStreamReader inputStreamReader=new InputStreamReader(inputStream);//提高效率，将自己字节流转为字符流
                            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);//加入缓冲区
                            String temp=null;
                            String info="";
                            while((temp=bufferedReader.readLine())!=null){
                                info+=temp;
                                Log.e(TAG, "startServer: 已接收到客户端连接！" );
                                Log.e(TAG, "startServer: 服务端接收到客户端信息："+info+",当前客户端ip为："+socket.getInetAddress().getHostAddress());
                            }

                            OutputStream outputStream=socket.getOutputStream();//获取一个输出流，向服务端发送信息
                            PrintWriter printWriter=new PrintWriter(outputStream);//将输出流包装成打印流
                            printWriter.print("你好，服务端已接收到您的信息");
                            printWriter.flush();
                            socket.shutdownOutput();//关闭输出流*/


                            //////////////////////////////////////////////////////////////

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "startServer: 客户端连接失败！" );
                        }

                    }
                }
            }
        }).start();
    }

    /**
     * 在服务端，创建Client 实例和其他连接到服务端的客户端进行通信
     * 实现Runnable 接口 通过 子线程开启去刷消息
     */
    class ClientConn implements Runnable{
        Socket socket = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        public ClientConn(Socket s){
            try {

                this.socket = s;
                dis = new DataInputStream(s.getInputStream()); //读取客户端信息
                dos = new DataOutputStream(s.getOutputStream()); //写

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ClientConn: 获取客户端流失败！");
            }

        }

        /**
         * 发送消息
         * @param msg
         * @throws IOException
         */
        public void sendMessage(String msg) throws IOException {
            dos.writeUTF(msg);
            dos.flush();
        }

        @Override
        public void run() {
            if (isStart) {
                while (true) {
                    try {
                        String message = dis.readUTF();
                        Date date = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        message = sdf.format(date) + "\r\n" + message;
                        Log.e(TAG, "startServer run: 服务端接收到客户端信息："+message+",当前客户端ip为："+socket.getInetAddress().getHostAddress());

                        //遍历客户端集合，将接收到的消息发送给所以连接者
                        for (ClientConn c : clients) {
                            c.sendMessage(message);
                        }

                    } catch (IOException e) {
//                    e.printStackTrace();
                        //异常关闭
                        try {
                            isStart = false;
                            dis.close();
                            dos.close();
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                    }
                }
            }
        }
    }


    public class ScokerBinder extends Binder{
        public SocketService getScoketService(){
            return  SocketService.this;
        }
    }


}
