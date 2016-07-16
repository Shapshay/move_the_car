package com.skiv.akk.movethecar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.util.ArrayMap;
import android.os.UserHandle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class McPushService extends Service {
    String uid;
    Timer mTimer;
    MyTimerTask mMyTimerTask;
    String message;
    ArrayList<Integer> msgsId;
    DBHelper dbHelper;

    public void AddToLog(String logtxt){
        String TAG="ServiceLog";
        Log.v(TAG, logtxt);
    };


    public void SendMessage(CharSequence title,CharSequence mess, int id) {
        Context context = getApplicationContext(); //инициатор - текущая активность
        int NOTIFY_ID = id;

        Intent notificationIntent = new Intent(context, MsgActivity.class);
        notificationIntent.putExtra("push_id", id);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                uniqueInt, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                //.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setTicker(mess)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(mess)// Текст уведомления
                .setContentIntent(contentIntent);

        Notification n = builder.getNotification();
        n.sound = Uri.parse("android.resource://com.skiv.akk.movethecar/" + R.raw.fafa);
        n.defaults = Notification.DEFAULT_VIBRATE;

        nm.notify(NOTIFY_ID, n);

    };
    void ParseTask() {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        Context gcontext;

        String LOG_TAG = "log_timer";
        AddToLog("--парсим json");
        // получаем данные с внешнего ресурса
        try {
            String u_id;
            // создаем объект для создания и управления версиями БД
            dbHelper = new DBHelper(this);
            // создаем объект для данных
            ContentValues cv = new ContentValues();

            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Log.d(LOG_TAG, "--- Rows in mytable: ---");
            // делаем запрос всех данных из таблицы mytable, получаем Cursor
            Cursor c = db.query("mc_table", null, null, null, null, null, null);

            // ставим позицию курсора на первую строку выборки
            // если в выборке нет строк, вернется false
            if (c.moveToFirst()) {
                int u_idColIndex = c.getColumnIndex("u_id");
                u_id = c.getString(u_idColIndex);
            } else {
                u_id = "0";
            }
            //uid=""+1;
            URL url = new URL("http://java.coap.kz/mc/push.php?u_id="+u_id);
            Log.d(LOG_TAG, "url: " + url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            resultJson = buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "--не смогли прочитать JSON");
            AddToLog("--не смогли прочитать JSON");
        }

        String strJson;
        strJson=resultJson;
        // выводим целиком полученную json-строку
        Log.d(LOG_TAG, strJson);
        AddToLog(strJson);

        JSONObject dataJsonObj = null;
        String secondName = "";

        try {
            dataJsonObj = new JSONObject(strJson);
            JSONArray messages = dataJsonObj.getJSONArray("messages");
            AddToLog("messages = "+messages);
            // 1. достаем инфо о втором друге - индекс 1
            JSONObject frec = messages.getJSONObject(0);
            AddToLog("frec = "+frec);
            String titl = frec.getString("title");
            AddToLog("titl = "+titl);

            //смотрим, а может тожэесамое сообщение?
            String mm;
            mm=frec.getString("message");
            AddToLog("id = "+frec.getInt("id"));
            AddToLog(msgsId.toString());
            AddToLog("--сравниваю:"+message+" и "+mm);
            if (msgsId.contains(frec.getInt("id"))){
                AddToLog("Сообщение с таким id уже было");
            } else {
                message = frec.getString("message");
                Log.d(LOG_TAG, "title: " + titl);
                Log.d(LOG_TAG, "message: " + message);
                // ну и раз уж такое щасье, то выводим сообщение юзеру..
                AddToLog("--Отправялем сообщение:"+message);
                msgsId.add(frec.getInt("id"));

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                // создаем объект для данных
                ContentValues cv = new ContentValues();
                // подготовим данные для вставки в виде пар: наименование столбца - значение
                cv.put("date", frec.getString("date"));
                cv.put("push_id", frec.getInt("id"));
                cv.put("from_id", frec.getInt("from_id"));
                cv.put("title", titl);
                cv.put("msg", message);
                cv.put("view", 0);
                // вставляем запись и получаем ее ID
                long rowID = db.insert("mc_msg", null, cv);
                Log.d(LOG_TAG, "row inserted, ID = " + rowID);

                SendMessage(titl, message, frec.getInt("id"));
            };

        } catch (JSONException e) {
            e.printStackTrace();
            AddToLog("e = "+e);
        }
    }


    public McPushService() {
        msgsId = new ArrayList<Integer>();
        msgsId.add(0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            AddToLog("--выполнили по таймеру...");
            ParseTask();
            //new MyService.ParseTask().execute();
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AddToLog("--это я, твой сервис, я стартовал! ");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        uid = sharedPreferences.getString("cooki", "");
        AddToLog("--сервис: load uid2:" + uid);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        message="";
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 1000, 5000);

        return Service.START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        AddToLog("--остановили сервис");
    }
}
