package com.skiv.akk.movethecar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MenuCutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_cut);
    }

    public void onAboutBtnClick(View view) {
        AlertDialog dialog = DialogScreen.getDialog(MenuCutActivity.this, 1);
        if (dialog != null) {
            dialog.show();
        }
    }

    public void onExitBtnClick(View view) {
        this.finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
