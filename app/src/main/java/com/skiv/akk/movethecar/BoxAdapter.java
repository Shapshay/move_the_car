package com.skiv.akk.movethecar;

/**
 * Created by Skiv on 11.07.2016.
 */
import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BoxAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Msg> objects;
    final String LOG_TAG = "myLog";

    BoxAdapter(Context context, ArrayList<Msg> products) {
        ctx = context;
        objects = products;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item, parent, false);
        }

        Msg p = getProduct(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        Log.d(LOG_TAG, "p.title = "+p.title);
        Log.d(LOG_TAG, "p.date = "+p.date);
        ((TextView) view.findViewById(R.id.tvDescr)).setText(p.title);
        ((TextView) view.findViewById(R.id.tvPrice)).setText(p.date);
        //((TextView) view.findViewById(R.id.tvPush)).setText(p.push_id);
        ((TextView) view.findViewById(R.id.tvPush)).setText(""+p.push_id);
        if(p.view==0) {
            ((TextView) view.findViewById(R.id.tvDescr)).setTextColor(Color.parseColor("#1F6F58"));
        }
        else{
            ((TextView) view.findViewById(R.id.tvDescr)).setTextColor(Color.parseColor("#000000"));
        }

        //((ImageView) view.findViewById(R.id.ivImage)).setImageResource(p.image);

        CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
        // присваиваем чекбоксу обработчик
        cbBuy.setOnCheckedChangeListener(myCheckChangList);
        // пишем позицию
        cbBuy.setTag(position);
        // заполняем данными из товаров: в корзине или нет
        cbBuy.setChecked(p.box);

        LinearLayout llMsg = (LinearLayout) view.findViewById(R.id.llMsg);
        llMsg.setOnClickListener(myMsgOnClickListener);



        return view;
    }

    // товар по позиции
    Msg getProduct(int position) {
        return ((Msg) getItem(position));
    }

    // содержимое корзины
    ArrayList<Msg> getBox() {
        ArrayList<Msg> box = new ArrayList<Msg>();
        for (Msg p : objects) {
            // если в корзине
            if (p.box)
                box.add(p);
        }
        return box;
    }

    // обработчик для чекбоксов
    OnCheckedChangeListener myCheckChangList = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // меняем данные товара (в корзине или нет)
            getProduct((Integer) buttonView.getTag()).box = isChecked;
        }
    };

    View.OnClickListener myMsgOnClickListener = new View.OnClickListener() {
        public void onClick(View view){
            TextView tvPush = (TextView) view.findViewById(R.id.tvPush);
            String txtPush = tvPush.getText().toString();
            int push_id = Integer.parseInt(txtPush);
            Intent msgIntent = new Intent(ctx, MsgActivity.class);
            msgIntent.putExtra("push_id", push_id);
            ctx.startActivity(msgIntent);
        }
    };
}
