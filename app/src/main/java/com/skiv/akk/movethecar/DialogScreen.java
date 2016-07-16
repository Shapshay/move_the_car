package com.skiv.akk.movethecar;

/**
 * Created by Skiv on 07.07.2016.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Created by Михаил on 09.07.2015.
 */
public class DialogScreen {
    public static final int IDD_ABOUT = 1; // Идентификаторы диалоговых окон
    public static final int IDD_OK = 2;
    public static final int IDD_ERR = 3;

    public static AlertDialog getDialog(Activity activity, int ID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        switch(ID) {
            case IDD_ABOUT: // Диалоговое окно About
                builder.setTitle(R.string.dialog_about_title);
                builder.setMessage(R.string.dialog_about_message);
                builder.setCancelable(true);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // Кнопка ОК
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Отпускает диалоговое окно
                    }
                });
                return builder.create();

            case IDD_OK: // Диалог успех
                builder.setTitle("Отлично");
                builder.setMessage("");
                builder.setCancelable(true);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // Кнопка ОК
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Отпускает диалоговое окно
                    }
                });
                return builder.create();

            case IDD_ERR: // Диалоговое окно Error
                builder.setTitle(R.string.dialog_about_title);
                builder.setMessage(R.string.common_error_message);
                builder.setIcon(R.drawable.warning);
                builder.setCancelable(true);
                builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() { // Кнопка ОК
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Отпускает диалоговое окно
                    }
                });
                return builder.create();
            default:
                return null;
        }
    }
}
