package com.example.esp32ble;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    private LineChart chart;
    private ArrayList<Entry> xLine;
    private ArrayList<Entry> yLine;
    private ArrayList<Entry> zLine;
    private float i;

    private boolean connectState = false;
    private Button changeButton;
    private TextView nameText;
    private final String NOW_CONNECT = "接続を終了";
    private final String NOW_DISCONNECT = "接続を開始";

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private final int duration = Toast.LENGTH_SHORT;
    private SubsidyBle subBle;
    public boolean mainView = false;

    private BtDialogFragment dialog;
    public final String KI_NAME = "pareState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar Toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(Toolbar);

        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        changeButton = findViewById(R.id.chart_change);
        changeButton.setOnClickListener(this::onClick);
        nameText = findViewById(R.id.device_name_text);

        xLine = new ArrayList<>();
        yLine = new ArrayList<>();
        zLine = new ArrayList<>();

        chart = findViewById(R.id.lineChert);
        lineChart();
        i = 0;

        subBle = new SubsidyBle(this);

        //BLEの設定
        if( !getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) ) {
            Log.d("BtActivity","Bluetooth is not supported");
            finish();    // アプリ終了宣言
            return;
        }
        // Bluetoothアダプタの取得
        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if( null == bluetoothAdapter ) {
            // Android端末がBluetoothをサポートしていない
            Log.d("BtActivity","Bluetooth is not supported");
            finish();    // アプリ終了宣言
            return;
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestBluetoothFeature();

        mainView = true;
    }

    //画面が遷移したとあとに実行される
    @Override
    public void onPause() {
        super.onPause();

        chart.clear();

        mainView = false;
    }

    //アプリがタスクを切られた時
    @Override
    protected void onStop() {
        super.onStop();

        subBle.disconnectS();
    }

    //空きメモリが足りなくなった場合に呼び出される
    public void onLowMemory(){ Log.i("SystemError","空きメモリが少なくなっています");}

    /*********
     Bluetooth
     *********/

    private void requestBluetoothFeature(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }
    }

    // 機能の有効化ダイアログの操作結果
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch( requestCode ) {
            case 1: // Bluetooth有効化要求
                if( Activity.RESULT_CANCELED == resultCode ) {
                    // 有効にされなかった
                    Toast.makeText(this,"Bluetooth is not working",duration);
                    finish();    // アプリ終了宣言
                    return;
                }
                break;
        }
        super.onActivityResult( requestCode, resultCode, data );
    }

    /**ボタン**/

    //グラフの表示非表示を切り替えるクリックメソッド
    private void onClick(View v){
        buttonChange();
    }

    public void buttonChange(){
        if (!connectState) {
            dialog = new BtDialogFragment(this);
            dialog.show(getSupportFragmentManager(), "my_dialog");
        }else{
            changeButton.setText(NOW_DISCONNECT);//Connectと表示
            connectState = false;
            nameText.setText("未接続");

            subBle.disconnectS();
            Toast.makeText(this,"接続を終了しました",Toast.LENGTH_SHORT).show();
        }
    }

    public void positiveCallback(){
            changeButton.setText(NOW_CONNECT);//Disconnectと表示
            connectState = true;

            int xf = 0;
            int yf = 0;
            int zf = 0;
            //上の3つの変数にArduinoからBluetoothで取得した加速度センサーの数値をいれる
            LatestData(xf, yf, zf);

            subBle.scanAndConnect(this, bluetoothAdapter);
    }

    //subClassで検出したデバイスの名前を受け取って表示
    public void setTextName(String deviceName){
        Log.d("kiteru?",deviceName);

        nameText.setText(deviceName);
    }

    /**********
     折れ線グラフ
     **********/

    //グラフの初期設定
    public void lineChart() {
        //グラフの設定
        chart.getDescription().setEnabled(true);         //グラフ説明テキストを表示するか
        chart.getDescription().setText("加速度センサー");   //グラフ説明テキスト
        chart.getDescription().setTextColor(Color.BLACK);//グラフ説明テキストの文字色指定
        chart.setBackgroundColor(Color.WHITE);           //グラフの背景色
        chart.getAxisRight().setEnabled(true);           //右側のメモリ
        chart.getAxisLeft().setEnabled(false);           //左側のメモリ

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);

        //グラフのX軸の設定
        XAxis xAxis = chart.getXAxis();                 //XAxisをインスタンス化
        xAxis.setAxisMinimum(0);                        //X軸最小値

        //グラフのY軸の設定
        YAxis yAxis = chart.getAxisLeft();              //YAxisをインスタンス化
        yAxis.setAxisMinimum(0);                        //Y軸最小値
        yAxis.setAxisMaximum(4095);                     //Y軸最大値
        yAxis.setDrawGridLines(true);                   //グリッドを表示

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);
        chart.setData(data);
    }

    public void LatestData( int xFlt, int yFlt, int zFlt ){

        //2.Entry型でListを作成し(x,y)=(i,data)で座標を格納
        xLine.add(new Entry(i, xFlt));
        yLine.add(new Entry(i, yFlt));
        zLine.add(new Entry(i, zFlt));

        //3,LineDataSet
        LineDataSet xSet = new LineDataSet(xLine,"X軸");
        xSet.setColor(Color.RED);
        xSet.setDrawCircles(false);
        xSet.setLineWidth(1.5f);              //線の太さ
        xSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);// 折れ線グラフの表示方法
        xSet.setDrawValues(false);            // 折れ線グラフの値を非表示
        
        LineDataSet ySet = new LineDataSet(yLine,"Y軸");
        ySet.setColor(Color.BLUE);
        ySet.setDrawCircles(false);
        ySet.setLineWidth(1.5f);
        ySet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ySet.setDrawValues(false);

        LineDataSet zSet = new LineDataSet(zLine,"Z軸");
        zSet.setColor(Color.GREEN);
        zSet.setDrawCircles(false);
        zSet.setLineWidth(1.5f);
        zSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        zSet.setDrawValues(false);

        //4.LineDataにLineDataSet格納
        LineData data = new LineData(xSet,ySet,zSet);

        //5.LineChartにLineData格納
        chart.setData(data);

        //6.LineChartを更新(リアルタイム更新のサイトを漁って左に流していくやつを付け加える)
        chart.notifyDataSetChanged();

        // X軸に表示する最大のEntryの数を指定
        chart.setVisibleXRangeMaximum(100);

        //7.最大数を超えたら動かす
        chart.moveViewToX(i);

        i++;//x軸を+1する
    }

    /***************
     Menuについて(左側)
     ***************/

    //toolbarのボタンのonClickに登録してあるメソッド
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(this::onMenuItemClick);    //下のメソッド
        inflater.inflate(R.menu.menuresorcefile, popup.getMenu());
        popup.show();
        if (connectState){
            buttonChange();
        }
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

    /***************
     Menuについて(右側)
     ***************/

    //menuの追加
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bt_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //clickの追加
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bt_setting:
                startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), 0);
                return true;

            case R.id.bt_scan:
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
                {
                    Toast.makeText(getApplicationContext(),"Bluetooth通信に対応していません",duration);
                }else {
                    buttonChange();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}