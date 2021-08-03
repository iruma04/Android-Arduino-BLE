package com.example.esp32ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import java.util.ArrayList;

public class GamePage extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    private Button game1;
    private Button game2;
    private Button game3;

    private TextView title;
    private TextView iscText;
    private Button next;
    private int nPush;

    private TextView connectState;
    private TextView mcuState;
    private TextView notifyState;
    private TextView logState;
    private TextView allState;

    private SubsidyBle subBle;
    public boolean gameView = false;
    public boolean testGame1 = false; //test
    private int nData = 0;
    private int xVal = 0;
    private int yVal = 0;
    private int zVal = 0;
    private int zeroX;
    private int zeroY;
    private int zeroZ;
    private BluetoothManager mBtManager;
    private BluetoothAdapter mBtAdapter;

    boolean justOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_page);

        Toolbar toolbar = findViewById(R.id.sub_toolbar);
        setSupportActionBar(toolbar);

        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Bluetoothアダプタの取得
        mBtManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBtManager.getAdapter();
        if( null == mBtAdapter ) {
            // Android端末がBluetoothをサポートしていない
            Log.d("BtActivity","Bluetooth is not supported");
            //finish();    // アプリ終了宣言
            return;
        }
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        subBle = new SubsidyBle(this);

        game1 = findViewById(R.id.game_1);
        game2 = findViewById(R.id.game_2);
        game3 = findViewById(R.id.game_3);
        game1.setOnClickListener(this::gameButton);
        game2.setOnClickListener(this::gameButton);
        game3.setOnClickListener(this::gameButton);

        title = findViewById(R.id.gameTitle);
        iscText = findViewById(R.id.instructionsText);
        next = findViewById(R.id.nextButton);
        findViewById(R.id.nextButton).setOnClickListener(this::next);
        //クリックメソッドを定義

        connectState = findViewById(R.id.connect_state);
        mcuState = findViewById(R.id.mcu_state);
        notifyState = findViewById(R.id.notify_state);
        logState = findViewById(R.id.state_log);
        allState = findViewById(R.id.state);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //使う変数を初期化
        justOne = false;
        next.setEnabled(false);
        nPush=1;
    }

    @Override
    protected void onPause() {
        super.onPause();

        gameView = false;
    }

    private void gameButton(View v){
        //処理実行
        switch (v.getId()){
            case R.id.game_1:
                title.setText("ゲーム1");
                gBleConnect();
                break;
            case R.id.game_2:
                title.setText("ゲーム2");
                gBleConnect();
                break;
            case R.id.game_3:
                title.setText("ゲーム3");
                gBleConnect();
                break;
        }
    }

    private void gBleConnect(){
        //このページに来て一回目は実行する
        if(!justOne){
            subBle.scanAndConnect(this, mBtAdapter);
        }
    }

    public void setGpText(int number, String message){
        switch (number){
            case 1:
                connectState.setText("・"+message);
                break;
            case 2:
                mcuState.setText("・"+message);
                break;
            case 3:
                new Handler(Looper.getMainLooper()).post(new Runnable(){
                    @Override
                    public void run() {
                        notifyState.setText("・"+message);
                        logState.setText("・接続が確立しました");
                        allState.setText("・ゲームを開始できます");
                        iscText.setText("胸の前の位置で端末を固定してください");
                        next.setEnabled(true);
                        justOne = true;  //接続が完了したときだけscanできなくするため
                    }
                });
                break;
        }
    }

    public void next(View v){
        //同じボタンを使いまわすためif文を使う
        if (nPush==1) {
            next.setEnabled(false);
            iscText.setText("情報を取得しています");
            nPush++;
            gameView = true; //notifyで判断するため trueにすることによりdataSetが呼び出される
        }else if (nPush==2){
            next.setEnabled(false);
            iscText.setText("手を上にあげてください");
            testGame1 = true;
        }
    }

    public void dataSet(int x, int y, int z){
        if (nData < 30) {
            xVal += x;
            yVal += y;
            zVal += z;
        }else{
            //ここにデータが来るのをやめる
            gameView = false;
            //ゼロ点を設定
            zeroX = xVal / 30;
            zeroY = yVal / 30;
            zeroZ = zVal / 30;
            iscText.setText("ゼロ点の取得が完了しました"+"\n"+zeroX+" "+zeroY+" "+zeroZ);
            next.setEnabled(true);
        }
    }

    public void tempSave(int x, int y, int z){
        //一つのゲームのため
        //複数のゲーム用にするために分岐処理を追加する必要がある
        //まずはテスト
        //手を上にあげてという指示 = z
        if (z - zeroZ > 1000){  //初期値との差が1000より大きかったら
            testGame1 = false;
            iscText.setText("合格です\n手を下ろしてください");
        }
    }

    //toolbarのボタンのonClickに登録してあるメソッド
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(this::onMenuItemClick);    //下のメソッド
        inflater.inflate(R.menu.menuresorcefile, popup.getMenu());
        popup.show();
    }

    //interfaceをimplementsしたものだからOverrideしてどのmenuが押されたか判断する
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            //ホームが押された場合
            case R.id.home_menu:
                //Intentの作成 + 遷移
                Intent homeIntent = new Intent(this,MainActivity.class);
                startActivity(homeIntent);
                return true;
            //ゲームが押された場合
            case R.id.game_menu:
                Intent gmIntent = new Intent(this,GamePage.class);
                startActivity(gmIntent);
                return true;
            default:
                return false;
        }
    }
}