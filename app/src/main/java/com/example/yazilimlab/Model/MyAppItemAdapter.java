package com.example.yazilimlab.Model;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yazilimlab.R;

import java.util.ArrayList;

public class MyAppItemAdapter extends RecyclerView.Adapter<MyAppItemAdapter.ItemInfoHolder> {

    ArrayList<MyAppItemInfo> myAppItemInfoArrayList;
    private Context context;

    public MyAppItemAdapter(ArrayList<MyAppItemInfo> myAppItemInfoArrayList, Context context) {
        this.myAppItemInfoArrayList = myAppItemInfoArrayList;
        this.context = context;
    }

    // gorunumler burda ayarlanir
    @NonNull
    @Override
    public ItemInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.activity_my_app_item, parent, false);
        return new ItemInfoHolder(v);
    }

    // icerigini ayarlama
    @Override
    public void onBindViewHolder(@NonNull ItemInfoHolder holder, int position) {

        MyAppItemInfo myAppItemInfo = myAppItemInfoArrayList.get(position);
        holder.myApplicationItem_typeText.setText(myAppItemInfo.type + " \nBaşvurusu");
        holder.myApplicationItem_dateText.setText(myAppItemInfo.date);
        holder.stateText(myAppItemInfo.state);
    }

    // liste boyutu
    @Override
    public int getItemCount() {
        return myAppItemInfoArrayList.size();
    }

    class ItemInfoHolder extends RecyclerView.ViewHolder {

        TextView myApplicationItem_typeText, myApplicationItem_stateText, myApplicationItem_dateText;

        public ItemInfoHolder(@NonNull View itemView) {
            super(itemView);

            myApplicationItem_typeText = (TextView) itemView.findViewById(R.id.myApplicationItem_typeText);
            myApplicationItem_stateText = (TextView) itemView.findViewById(R.id.myApplicationItem_stateText);
            myApplicationItem_dateText = (TextView) itemView.findViewById(R.id.myApplicationItem_dateText);
        }

        public void stateText(String strState) {
            switch (strState) {
                case "0":
                    this.myApplicationItem_stateText.setText("İmza Bekleniyor");
                    break;
                case "1":
                    this.myApplicationItem_stateText.setText("Onay Bekleniyor");
                    break;
                case "2":
                    this.myApplicationItem_stateText.setText("Başvuru Onaylandı");
                    break;
                case "3":
                    this.myApplicationItem_stateText.setText("Başvuru Reddedildi");
                    break;
            }
        }


        public void setData(MyAppItemInfo myAppItemInfo) {
            this.myApplicationItem_typeText.setText(myAppItemInfo.getType());
            this.myApplicationItem_stateText.setText(myAppItemInfo.getState());
            this.myApplicationItem_dateText.setText(myAppItemInfo.getDate());

        }
    }
}
