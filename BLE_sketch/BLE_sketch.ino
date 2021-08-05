/**BLE接続設定**/
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "b62c1ffa-bdd8-46ea-a378-d539cf405d93"

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};

//ピンを設定
int RED = 25;
int GREEN = 26;
int BLUE = 27;

const int BUTTON = 13;
bool state;

const int ax = 32;
const int ay = 35;
const int az = 34;

String aSensorX() {
  int x = analogRead(ax); // X軸
  String sx = (String)x;
  return sx;
}

String aSensorY() {
  int y = analogRead(ay);
  String sy = (String)y;
  return sy;
}

String aSensorZ() {
  int z = analogRead(az);
  String sz = (String)z;
  return sz;
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

void setup() {
  //シリアルモニターの初期化をする
  Serial.begin(115200);

  while ( !Serial ) {
  }

  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);
  pinMode(BUTTON, INPUT);

  pinMode(ax, INPUT);
  pinMode(ay, INPUT);
  pinMode(az, INPUT);

  //初期設定として青を出力
  digitalWrite(RED, LOW);
  digitalWrite(GREEN, LOW);
  digitalWrite(BLUE, HIGH);

  state = false;

  //割り込み処理
  attachInterrupt(BUTTON, mButton, RISING);

  //BLEセットアップ
  BLEDevice::init("ESP32");

  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );

  pCharacteristic->addDescriptor(new BLE2902());

  pService->start();

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();
}

void loop() {
  if (state) {
    // notify changed value
    if (deviceConnected) {
      //","これの位置で区切るため間に仕込む
      String strVal(aSensorX() + "," + aSensorY() + "," + aSensorZ());
      Serial.println(strVal);

      pCharacteristic->setValue(strVal.c_str());
      pCharacteristic->notify();
      delay(3); // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test i was able to go as low as 3ms
    }
    // disconnecting
    if (!deviceConnected && oldDeviceConnected) {
      delay(500); // give the bluetooth stack the chance to get things ready
      pServer->startAdvertising(); // restart advertising
      oldDeviceConnected = deviceConnected;
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
      // do stuff here on connecting
      oldDeviceConnected = deviceConnected;
    }
  } else {
    //シリアルプロッタ用
    Serial.print(analogRead(ax));
    Serial.print(",");
    Serial.print(analogRead(ay));
    Serial.print(",");
    Serial.print(analogRead(az));
    Serial.println("");
  }
  delay(100);
}
