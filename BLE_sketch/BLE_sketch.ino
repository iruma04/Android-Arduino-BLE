/**BLE接続設定**/
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b" //サーバーUUID
#define WRITE_UUID   "b62c1ffa-bdd8-46ea-a378-d539cf405d93" //書き込みUUID

//ピンを設定
const int RED = 25;
const int GREEN = 26;
const int BLUE = 27;

const int BUTTON = 2;

int i = 0;
bool state = false;
bool readState = false;

const int x = 32;
const int y = 35;
const int z = 34;

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      digitalWrite(RED, HIGH);
      digitalWrite(GREEN, HIGH);
      digitalWrite(BLUE, LOW);

      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};

int aSensorX() {
  int x;
  x = analogRead(x); // X軸
  return x;
}

int aSensorY() {
  int y;
  y = analogRead(y);
  return y;
}

int aSensorZ() {
  int z;
  z = analogRead(z);
  return z;
}

void mButton() {
  if (state) {
    //機器の状態をoffにするときの処理
    state = false;

    digitalWrite(RED, HIGH);
    digitalWrite(GREEN, LOW);
    digitalWrite(BLUE, LOW);
  } else {
    //onにするときの処理
    state = true;

    digitalWrite(RED, LOW);
    digitalWrite(GREEN, HIGH);
    digitalWrite(BLUE, LOW);
  }
}

void ledSetup() {
  while (i < 2) {
    digitalWrite(RED, HIGH);
    digitalWrite(GREEN, HIGH);
    digitalWrite(BLUE, HIGH);

    delay(100);

    digitalWrite(RED, LOW);
    digitalWrite(GREEN, LOW);
    digitalWrite(BLUE, LOW);

    delay(50);

    i++;
  }
}

void setup() {
  //シリアルモニターの初期化をする
  Serial.begin(115200);

  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);

  pinMode(BUTTON, INPUT);

  pinMode(x, INPUT);
  pinMode(y, INPUT);
  pinMode(z, INPUT);

  state = false;

  //BLEセットアップ
  BLEDevice::init("ESP32");

  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
                      WRITE_UUID,
                      BLECharacteristic::PROPERTY_READ  |
                      BLECharacteristic::PROPERTY_WRITE |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pCharacteristic->addDescriptor(new BLE2902());

  pService->start();

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();

  ledSetup();
}

void loop() {
  detachInterrupt(2);//割り込み禁止
  // notify changed value
  if (deviceConnected) {
    attachInterrupt(2, mButton, RISING);//割り込みピン指定
    //ボタンが押されたら
    if (state) {
      //センサの数値を取得する
      String sx = (String)aSensorX();
      String sy = (String)aSensorY();
      String sz = (String)aSensorZ();

      //","これの位置で区切るため間に仕込む
      String strVal(sx + "," + sy + "," + sz);
      Serial.println(strVal);

      pCharacteristic->setValue(strVal.c_str());
      pCharacteristic->notify();
      delay(3); // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test i was able to go as low as 3ms
    }
  }
  // disconnecting
  if (!deviceConnected && oldDeviceConnected) {
    digitalWrite(RED, LOW);
    digitalWrite(GREEN, LOW);
    digitalWrite(BLUE, LOW);

    state = false;

    delay(500); // give the bluetooth stack the chance to get things ready

    pServer->startAdvertising(); // restart advertising
    oldDeviceConnected = deviceConnected;
  }
  // connecting
  if (deviceConnected && !oldDeviceConnected) {
    // do stuff here on connecting
    oldDeviceConnected = deviceConnected;
  }
  delay(100);
}
