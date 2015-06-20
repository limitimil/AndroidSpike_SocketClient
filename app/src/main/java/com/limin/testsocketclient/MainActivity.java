package com.limin.testsocketclient;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {
    SocketClient socketclient = null;
    Button send;
    Thread listeningThread;
    TextView text ;
    //Socket Server information
    final String ServerIP = "10.0.0.54";
    final int ServerPort = 7777;
    //layout information
    EditText myID;
    EditText mySpeedVector;
    EditText myShieldOrientationVector;
    Button periodSend;
    Button stopPeriodSend;
    //periodically send
    Timer SystemTimer = null;
    TimerTask sendTimer = null;
    final int periodToSend = 200;
    HandlerExtension resultHandler = new HandlerExtension(this);
    private static class HandlerExtension extends Handler {
        private final WeakReference<MainActivity> currentActivity;

        public HandlerExtension(MainActivity activity) {
            currentActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            MainActivity activity = currentActivity.get();
            if (activity != null) {
                //System.out.println("msg send form the thread"+message.getData().getString("result"));
                //activity.updateint(message.getData().getInt("result"));
                activity.updateText(message.getData().getString("result"));
            }
        }
    }
    public  void updateint(int i){
       System.out.print("update int : " + i);
    }
    public void updateText(String s){
        try{
           System.out.println("your string is : " + s);
            text.setText(s);
        }catch(NullPointerException e){
            System.out.println("set text exception: "+e.toString());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text=(TextView)findViewById(R.id.text);
        new Thread(new Runnable (){
            @Override
            public void run(){
                socketclient = new SocketClient(ServerIP,ServerPort);
            }

            });
        send = (Button) findViewById(R.id.button);
        send.setOnClickListener( new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String message = "";
                        //set the message
                        message = (myID.getText()+":");
                        message = (message+mySpeedVector.getText()+":");
                        message = (message+myShieldOrientationVector.getText());
                        String result = "";
                        if(message!=null || message.isEmpty()) {
                            result = socketclient.sendMessage(message);
                        }else {
                            System.out.println("start"+myID.getText()+":"+mySpeedVector.getText()+":"+myShieldOrientationVector.getText());
                            System.out.println(message);
                        }
                        Bundle msgBundle = new Bundle();
                        msgBundle.putString("result", result);
                        //msgBundle.putInt("result", 123);
                        Message msg = new Message();
                        msg.setData(msgBundle);
                        resultHandler.sendMessage(msg);

                        /*resultHandler.handleMessage();*/
                    }
                }).start();
        }

        });

        //initializing layout information
        myID = (EditText)findViewById(R.id.myID);
        mySpeedVector = (EditText)findViewById(R.id.mySpeedVector);
        myShieldOrientationVector = (EditText)findViewById(R.id.myShieldOrientationVector);
        //button to periodically send packege
        periodSend = (Button)findViewById(R.id.periodSend);
        periodSend.setOnClickListener( new Button.OnClickListener(){
            @Override
            public void onClick(View v){

                sendTimer = new TimerTask(){
                    @Override
                    public void run(){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                String message = "";
                                //set the message
                                message = (myID.getText()+":");
                                message = (message+mySpeedVector.getText()+":");
                                message = (message+myShieldOrientationVector.getText());
                                String result = "";
                                if(message!=null || message.isEmpty()) {
                                    result = socketclient.sendMessage(message);
                                }else {
                                    System.out.println("start"+myID.getText()+":"+mySpeedVector.getText()+":"+myShieldOrientationVector.getText());
                                    System.out.println(message);
                                }
                                Bundle msgBundle = new Bundle();
                                msgBundle.putString("result", result);
                                //msgBundle.putInt("result", 123);
                                Message msg = new Message();
                                msg.setData(msgBundle);
                                resultHandler.sendMessage(msg);

                        /*resultHandler.handleMessage();*/
                            }
                        }).start();
                    }
                };
                SystemTimer = new Timer();
                SystemTimer.schedule(sendTimer,0,periodToSend);
            }

        });
        stopPeriodSend = (Button)findViewById(R.id.stopPeriodSend);
        stopPeriodSend.setOnClickListener( new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                if(SystemTimer != null) {
                    SystemTimer.cancel();
                }
                if(sendTimer != null) {
                    sendTimer.cancel();
                }
            }

        });
    }

    @Override
    public void onPause(){
        super.onPause();
        if(socketclient!=null) {
            socketclient.disconnect();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        new Thread(new Runnable(){
            @Override
            public void run(){
                socketclient = new SocketClient(ServerIP,ServerPort);
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
