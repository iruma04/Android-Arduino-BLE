package com.example.esp32ble;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

public class GamePage extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_page);

        Toolbar toolbar = findViewById(R.id.sub_toolbar);
        setSupportActionBar(toolbar);

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
