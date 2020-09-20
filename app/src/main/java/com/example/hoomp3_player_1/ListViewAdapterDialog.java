package com.example.hoomp3_player_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapterDialog extends BaseAdapter {
    private Context context;
    private ArrayList<MusicData> arrayList;

    public ListViewAdapterDialog(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<MusicData> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<MusicData> arrayList) {
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view == null) {
            view = layoutInflater.inflate(R.layout.dialog_list_partition, null);
        }

        TextView mName = view.findViewById(R.id.dialog_listPartition_MusicName);
        TextView aName = view.findViewById(R.id.dialog_listPartition_tvArtistName);

        MusicData musicData = arrayList.get(i);

        mName.setText(musicData.getMusicName());
        aName.setText(musicData.getArtistName());
        return view;
    }
}
