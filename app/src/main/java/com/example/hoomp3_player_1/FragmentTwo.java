package com.example.hoomp3_player_1;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class FragmentTwo extends Fragment implements View.OnClickListener {
    private Activity_Main activityMain;

    //UI
    private ListView frag2_listView;
    private Button ibAllList, ibSinger, ibGenre;

    //arraylist
    private ArrayList<MusicData> arrayMusic_total = new ArrayList<MusicData>();    //total list
    private ArrayList<MusicData> arrayGenreArtist_title = new ArrayList<MusicData>();    //장르 및 가수로 묶을 때
    private ArrayList<MusicData> arrayGenreArtist_selected = new ArrayList<MusicData>(); //장르 및 가수에서 선택을 했을 때

    //adapter 변경을 위한 상수
    private final int ADAPTER_TOTAL = 0;
    private final int ADAPTER_GENRE_ARTIST_TITLE = 1;
    private final int ADAPTER_SELECT_GENRE_ARTIST = 2;

    //여러가지
    private ListViewAdapter listViewAdapter;//이미지, 곡명, 가수, 재생시간을 나타내는 adapter
    private MusicData musicData;//선택한 음악

    //현재 보여지는 list 구분 상수
    private final int LIST_TOTAL = 0;
    private final int LIST_GENRE = 1;
    private final int LIST_ARTIST = 2;

    //현재 보여지고 있는 리스트를 확인하는 flag, 0 전체리스트, 1 가수별, 2장르별
    private int flag_selectedListBtn = LIST_TOTAL; // 첫화면 전체리스트를 위한
    private boolean nowPlayList;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activityMain = (Activity_Main) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activityMain = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.two_fragment, container, false);
        activityMain.linearBottomBar.setVisibility(View.VISIBLE);

        //ui 찾기
        findViewByIdFunction(viewGroup);

        //첫화면 전체 리스트 출력 - mainActivity에서 로드해온 mp3
        setTotalListFunction();

        //버튼 이벤트 등록
        btnEventFunction();

        return viewGroup;
    }

    //버튼 이벤트 등록
    private void btnEventFunction() {
        ibAllList.setOnClickListener(this);
        ibSinger.setOnClickListener(this);
        ibGenre.setOnClickListener(this);
        frag2_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                setDeliverToMainActivity(adapterView, position);
            }
        });
    }

    //adapter 적용 모듈 - adapter는 계속 새로 만들어주지 않으면 화면에 나오지 않음 원인 모름
    private void setAdapterFunction(int num) {
        switch (num) {
            case ADAPTER_TOTAL:
                listViewAdapter = new ListViewAdapter(activityMain);
                listViewAdapter.setArrayList(arrayMusic_total);
                frag2_listView.setAdapter(listViewAdapter);
                break;
            case ADAPTER_GENRE_ARTIST_TITLE:
                ListViewAdapter_align_artist_genre artist_genre = new ListViewAdapter_align_artist_genre(activityMain);
                artist_genre.setArrayList(arrayGenreArtist_title);
                frag2_listView.setAdapter(artist_genre);
                break;
            case ADAPTER_SELECT_GENRE_ARTIST:
                listViewAdapter = new ListViewAdapter(activityMain);
                listViewAdapter.setArrayList(arrayGenreArtist_selected);
                frag2_listView.setAdapter(listViewAdapter);
                break;
            default:
                break;
        }
        frag2_listView.invalidate();
    }

    //ui 찾기
    private void findViewByIdFunction(ViewGroup viewGroup) {
        frag2_listView = viewGroup.findViewById(R.id.frag2_listView);
        ibAllList = viewGroup.findViewById(R.id.ibAllList);
        ibSinger = viewGroup.findViewById(R.id.ibSinger);
        ibGenre = viewGroup.findViewById(R.id.ibGenre);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibAllList:
                setTotalListFunction();
                setAdapterFunction(ADAPTER_TOTAL);
                flag_selectedListBtn = LIST_TOTAL;
                break;
            case R.id.ibSinger:
                Mp3DBHelper mp3DBHelper1 = new Mp3DBHelper(activityMain);
                arrayGenreArtist_title.clear();
                arrayGenreArtist_title = mp3DBHelper1.selectMyMusicTBL(Mp3DBHelper.ALIGN_SINGER, null);
                setAdapterFunction(ADAPTER_GENRE_ARTIST_TITLE);
                mp3DBHelper1 = null;
                flag_selectedListBtn = LIST_ARTIST;
                break;
            case R.id.ibGenre:
                Mp3DBHelper mp3DBHelper2 = new Mp3DBHelper(activityMain);
                arrayGenreArtist_title.clear();
                arrayGenreArtist_title = mp3DBHelper2.selectMyMusicTBL(Mp3DBHelper.ALIGN_GENRE, null);
                setAdapterFunction(ADAPTER_GENRE_ARTIST_TITLE);
                mp3DBHelper2 = null;
                flag_selectedListBtn = LIST_GENRE;
                break;
            default:
                break;
        }
    }

    //tab 이동했을 때 adapter 변경까지 이루어 지기 위해 선언
    @Override
    public void onResume() {
        super.onResume();
        ibAllList.callOnClick();
    }

    //첫화면 전체 리스트 출력 - mainActivity에서 로드해온 mp3
    public void setTotalListFunction() {
        Bundle bundle = getArguments();
        arrayMusic_total = bundle.getParcelableArrayList("totalListMP3");
        setAdapterFunction(ADAPTER_TOTAL);
    }

    //mp3 setting
    private void setDeliverToMainActivity(AdapterView<?> adapterView, int position) {
        //현재 adapter가 이미지도 출력하는 것일 때 : mainActivity ui에 적용함
        if (adapterView.getAdapter().equals(listViewAdapter)) {
            //현재 보여지는 List 종류가 - 전체,가수별,장르별
            if (flag_selectedListBtn == 1 || flag_selectedListBtn == 2) {
                setMainBottomUIFunction(arrayGenreArtist_selected, position);

            } else {//선택한 것이 listview adapter 중에서 전체 리스트 일 때
                setMainBottomUIFunction(arrayMusic_total, position);
            }

        } else {//현재 adapter가 글자만 출력할 때 : 해당하는 list db에서 가져옴
            MusicData m = arrayGenreArtist_title.get(position);
            Mp3DBHelper dbHelper = new Mp3DBHelper(activityMain);
            if (flag_selectedListBtn == LIST_ARTIST) {
                arrayGenreArtist_selected = dbHelper.selectMyMusicTBL(Mp3DBHelper.ALIGN_SINGER_SELECT, m);
            } else if (flag_selectedListBtn == LIST_GENRE) {
                arrayGenreArtist_selected = dbHelper.selectMyMusicTBL(Mp3DBHelper.ALIGN_GENRE_SELECT, m);
            }
            setAdapterFunction(ADAPTER_SELECT_GENRE_ARTIST);
        }
        activityMain.linearBottomBar.invalidate();
    }

    //main ui에 값 적용하기
    private void setMainBottomUIFunction(ArrayList<MusicData> arrayList, int position) {
        if(activityMain.mediaPlayer.isPlaying() != true){
            MusicData md = arrayList.get(position);
            byte[] data = md.getImage();
            Bitmap imageBitmap;
            if (md.getImage() != null) {
                imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                activityMain.ibPicture_activity.setImageBitmap(imageBitmap);
            } else {
                activityMain.ibPicture_activity.setImageResource(R.drawable.disk);
            }
            activityMain.tvMusicName_main.setText(md.getMusicName());
            activityMain.tvArtistName_main.setText(md.getArtistName());

            //activity로 데이터 전달하는 interface
            ((DataDeliverToMainActivity_FromFrag2) activityMain).onSelectedMusic2(arrayList, position);
        }else{
            musicData = arrayList.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(activityMain);
            View dialogView = View.inflate(activityMain, R.layout.dialog_add_playlist, null);
            TextView textView1 = dialogView.findViewById(R.id.textView4Artist);
            TextView textView2 = dialogView.findViewById(R.id.textView5Music);
            textView1.setText(musicData.getArtistName());
            textView2.setText(musicData.getMusicName());
            builder.setView(dialogView);
            builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(musicData.getCountPlay() == 1){
                        Toast.makeText(activityMain, "이미 저장되어 있습니다.", Toast.LENGTH_SHORT).show();
                    }else{
                        Mp3DBHelper mp3DBHelper = new Mp3DBHelper(activityMain);
                        musicData.setCountPlay(1);
                        mp3DBHelper.updateMyMusicTBL(Mp3DBHelper.UPDATE_SAVE_NOW_PLAYLIST, musicData);
                    }
                }
            });
            builder.setNegativeButton("취소", null);
            builder.show();
        }

    }

    //tranjection exception 방지를 위한 콜백함수
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    //activity로 정보 전달을 위한 interface
    public interface DataDeliverToMainActivity_FromFrag2 {
        void onSelectedMusic2(ArrayList<MusicData> arrayList, int position);
    }
}
