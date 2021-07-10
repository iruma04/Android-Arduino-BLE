# Android - ESP32間でのBLE通信
### 開発環境
Android Studio v4.2.1  
* 言語: java
* ライブラリ: MPAndroidChart:v3.1.0  

Arduino IDE  
* 追加ボードマネージャーのURL: https://dl.espressif.com/dl/package_esp32_index.json  
* ボードマネージャー: ESP32  

### 概要
central = ESP32（マイコン）  
peripheral = Android  

セントラルとなるマイコンで加速度センサの数値を取得して、ペリフェラルとなるAndroidでデータを取得するプログラムとなっています。  

以降で、小分けにし詳しく説明します。  

# Android
## 1.BLEデバイスを検出&接続
※ BLEScanCallbackは実装していません  

ボタンのクリックメソッドで、ダイアログを表示しその結果を評価して検出&接続処理を行っています  

## 2.データの受け取り
characteristicのnotifyでデータを取得しています  

"x値, y値, z値"という形でデータを受け取り、","（コンマ）で分けてStringの配列に格納します  
そのデータを一つずつ取り出し、lineChartに表示させています  

たまに、配列のエラーが起きますが原因は調査中です  

## 3.線グラフに表示
MainActivityでchartを初期化し、横軸の最大値を決めないことである程度データがたまったら左側に古いデータが流れていく仕様になっています  

表示の仕方などについては書いてある通りです  

# ESP32
## 1.BLEの初期化について
UUIDを作成し定数として宣言

void setup()の「//BLEセットアップ」としてコメントアウトしてある部分より下側が初期化処理です











