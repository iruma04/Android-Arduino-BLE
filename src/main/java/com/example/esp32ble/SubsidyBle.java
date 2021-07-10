package com.example.esp32ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabItem;

import java.util.Set;
import java.util.UUID;

public class SubsidyBle {

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic readCharacteristic;
    private Set<BluetoothDevice> pairedDevices;
    private final String DEVICE_SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    private final String READ_UUID =           "b62c1ffa-bdd8-46ea-a378-d539cf405d93";
    private final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private final String TAG = "BLE";
    private MainActivity m;

    public SubsidyBle(MainActivity main) {
        m = main;
    }

    /******************
     Bluetoothの検出&接続
     ******************/

    public BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        final String TAG = "btActivity";
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //接続状況が変化したら実行
            if (newState == BluetoothProfile.STATE_CONNECTED){
                //接続に成功したらサービスを検索する
                gatt.discoverServices();
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                //接続が切れたらGATTを空にする
                if (bluetoothGatt != null){
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //Serviceが見つかったら実行
            if (status == bluetoothGatt.GATT_SUCCESS){
                //UUIDが同じかどうか確認する
                BluetoothGattService service = gatt.getService(UUID.fromString(DEVICE_SERVICE_UUID));
                if (service != null){
                    //指定したUUIDを持つCharacteristicを確認する
                    readCharacteristic = service.getCharacteristic(UUID.fromString(READ_UUID));
                    if (readCharacteristic != null){
                        // Service, CharacteristicのUUIDが同じならBluetoothGattを更新する
                        bluetoothGatt = gatt;

                        // キャラクタリスティックが見つかったら、Notificationをリクエスト
                        bluetoothGatt.setCharacteristicNotification(readCharacteristic, true);

                        // Characteristic の Notificationを有効化する
                        BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt.writeDescriptor(descriptor);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if( BluetoothGatt.GATT_SUCCESS != status ) {
                return;
            }
            Log.i(TAG,"BLEデバイスに書き込みしました");
        }

        @Override
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic ){
            // Characteristic が変化したら呼ばれる(データを取得するたびに呼ばれる)
            //arduinoで{String s = x + "," + y + "," + z}この形で送る
            // キャラクタリスティックのUUIDをチェック
            if(READ_UUID.equals(characteristic.getUuid().toString())){
                String data = characteristic.getStringValue(0);//データ取得
                if (data != null && data.length() > 0){
                    String[] listData = data.split(",");           //取得したデータを","の位置で分割
                    int xData = Integer.parseInt(listData[0]);
                    int yData = Integer.parseInt(listData[1]);
                    int zData = Integer.parseInt(listData[2]);
                    Log.i(TAG,"x:"+xData+"y:"+yData+"z:"+zData);
                    m.LatestData(xData,yData,zData);
                }
            }
        }
    };

    //ペアデバイスを取得してサーバーに接続する
    public void scanAndConnect(Context context,BluetoothAdapter bluetoothAdapter){
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG,"Name:"+device.getName()+"Address:"+device.getAddress());
                Toast.makeText(context,"接続を開始します",Toast.LENGTH_SHORT).show();
                device = bluetoothAdapter.getRemoteDevice(device.getAddress());
                bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
                m.setTextName(device.getName());
            }
        }else{
            Toast.makeText(context, "ペアリングされたデバイスが存在しません", Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnectS() {
        if( null == bluetoothGatt ) {
            return;
        }
        // 切断
        //   mBluetoothGatt.disconnect()ではなく、mBluetoothGatt.close()しオブジェクトを解放する。
        //   理由：「ユーザーの意思による切断」と「接続範囲から外れた切断」を区別するため。
        //   ①「ユーザーの意思による切断」は、mBluetoothGattオブジェクトを解放する。再接続は、オブジェクト構築から。
        //   ②「接続可能範囲から外れた切断」は、内部処理でmBluetoothGatt.disconnect()処理が実施される。
        //     切断時のコールバックでmBluetoothGatt.connect()を呼んでおくと、接続可能範囲に入ったら自動接続する。
        bluetoothGatt.close();
        bluetoothGatt = null;
    }
}