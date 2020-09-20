package com.example.hoomp3_player_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<MusicData> arrayList;

    public ListViewAdapter(Context context) {
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
            view = layoutInflater.inflate(R.layout.list_partition2, null);
        }
        ImageView imageView = view.findViewById(R.id.listPartition_imageView);
        TextView tvName = view.findViewById(R.id.dialog_listPartition_tvArtistName);
        TextView tvMusicName = view.findViewById(R.id.dialog_listPartition_MusicName);
        TextView tvDuration = view.findViewById(R.id.listPartition_Duration);

        MusicData musicData = arrayList.get(i);
        byte[] data = musicData.getImage();
        Bitmap imageBitmap = null;
        if (musicData.getImage() != null) {
            imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            imageView.setImageBitmap(imageBitmap);
        }else {
            imageView.setImageResource(R.drawable.disk);
        }
        tvName.setText(musicData.getArtistName());
        tvMusicName.setText(musicData.getMusicName());
        tvDuration.setText(musicData.getDuration());
        return view;
    }
}
