package com.example.hoomp3_player_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;

public class FragmentThree extends Fragment implements View.OnClickListener {
    private Activity_Main activityMain;

    //activity 에서 넘어오는 값
    private final int NEW_PLAY = 0;
    private final int PAUSE_PLAY = 1;
    private int main_btnFlag = 2; //2일 때 일시정지 버튼으로 되지 않음

    //UI
    private LinearLayout linear_imageLyrics, linear_big;
    public SeekBar seekBar;
    public ImageView imageViewBigImage;
    public TextView textViewStartTime, textViewEndTime, textViewMusic, textViewSinger;
    public ImageButton imageButtonList, imageButtonHeart, imageButtonBack, imageButtonPlay, imageButtonNext;

    //여러가지
    private MusicData musicData;
    private int btnFlag = 0;
    private int PLAY = 0;
    private int PAUSE = 1;
    private Mp3DBHelper mp3DBHelper = new Mp3DBHelper(activityMain);


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
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.activity_play_music, container, false);
        activityMain.linearBottomBar.setVisibility(View.GONE);
        findViewByIdFunction(viewGroup);

        linear_imageLyrics.setOnClickListener(this);
        imageButtonPlay.setOnClickListener(this);
        imageButtonNext.setOnClickListener(this);
        imageButtonBack.setOnClickListener(this);
        imageButtonHeart.setOnClickListener(this);
        imageButtonList.setOnClickListener(this);

        //seekbar 재생 위치 변경
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    activityMain.mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return viewGroup;
    }

    private void findViewByIdFunction(ViewGroup viewGroup) {
        linear_imageLyrics = viewGroup.findViewById(R.id.linear_imageLyrics);
        linear_big = viewGroup.findViewById(R.id.linear_big);
        imageViewBigImage = viewGroup.findViewById(R.id.imageViewBigImage);
        seekBar = viewGroup.findViewById(R.id.seekBar);
        textViewStartTime = viewGroup.findViewById(R.id.textViewStartTime);
        textViewEndTime = viewGroup.findViewById(R.id.textViewEndTime);
        imageButtonList = viewGroup.findViewById(R.id.imageButtonList);
        textViewMusic = viewGroup.findViewById(R.id.textViewMusic);
        textViewSinger = viewGroup.findViewById(R.id.textViewSinger);
        imageButtonHeart = viewGroup.findViewById(R.id.imageButtonHeart);
        imageButtonBack = viewGroup.findViewById(R.id.imageButtonBack);
        imageButtonPlay = viewGroup.findViewById(R.id.imageButtonPlay);
        imageButtonNext = viewGroup.findViewById(R.id.imageButtonNext);
    }

    @Override
    public void onResume() {
        super.onResume();

        //번들 받아오는 함수
        getBundleFromActivity();

        //음악정보 세팅
        setMusicDataFunction(musicData);

        Log.d("FragmentThree", musicData.getFileName()+"====================================================");
        //버튼 상태 세팅
        btnStateSettingFunction();

        //thread 사용하여 ui 변경 설정
        threadUIChangeFunction();
    }

    //번들 받아오는 함수
    private void getBundleFromActivity() {
        //처음에 값을 전달 받고 받은 값을 저장해놓기 위한 작업
        try {
            Bundle bundle = activityMain.fragmentThree.getArguments();
            int btnFlag_activity = bundle.getInt("playBtnState");
            main_btnFlag = btnFlag_activity;

            MusicData m1 = bundle.getParcelable("holdingMusic1");
            MusicData m2 = bundle.getParcelable("holdingMusic2");
            MusicData m3 = bundle.getParcelable("music");
            if (m1 != null) {
                musicData = m1;
            } else if(m2 != null){
                musicData = m2;
            }else{
                musicData = m3;
            }
        } catch (NullPointerException e) {
            Log.d("FragmentThree", e.getMessage());
        }
        activityMain.fragmentThree.setArguments(null);
    }

    //음악 정보 세팅
    public void setMusicDataFunction(MusicData musicData) {
        //music 정보 세팅
        if (musicData != null) {
            textViewMusic.setText(musicData.getMusicName());
            textViewSinger.setText(musicData.getArtistName());
            textViewEndTime.setText(musicData.getDuration());
            textViewStartTime.setText("00:00");

            byte[] data = musicData.getImage();
            Bitmap imageBitmap;
            if (musicData.getImage() != null) {
                imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                imageViewBigImage.setImageBitmap(imageBitmap);
            } else {
                imageViewBigImage.setImageResource(R.drawable.disk);
            }
        } else {

        }
        linear_big.invalidate();

        //thread seekbar 변경
        threadUIChangeFunction();
    }

    //버튼 상태 세팅
    public void btnStateSettingFunction() {
        //버튼 상태 조절
        if (main_btnFlag == NEW_PLAY || main_btnFlag == PAUSE_PLAY) {
            imageButtonPlay.setImageResource(R.drawable.pause);
        } else {
            imageButtonPlay.setImageResource(R.drawable.play);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.linear_imageLyrics:
                activityMain.lyricsFragmentShow();
                break;

            case R.id.imageButtonPlay:
                activityMain.ibPlay_main.callOnClick();

                if (btnFlag == 1) {
                    imageButtonPlay.setImageResource(R.drawable.pause);
                    btnFlag = PLAY;
                } else {
                    imageButtonPlay.setImageResource(R.drawable.play);
                    btnFlag = PAUSE;
                }
                threadUIChangeFunction();
                break;

            case R.id.imageButtonNext:
                activityMain.ibNext_main.callOnClick();
                setMusicDataFunction(activityMain.playingMusicData);
                break;

            case R.id.imageButtonBack:
                activityMain.ibBack_main.callOnClick();
                setMusicDataFunction(activityMain.playingMusicData);
                break;

            case R.id.imageButtonHeart:
                if(activityMain.playingMusicData.getHeart() == 0){
                    mp3DBHelper.updateMyMusicTBL(Mp3DBHelper.UPDATE_HEART, activityMain.playingMusicData);
                    imageButtonHeart.setImageResource(R.drawable.heart_red);
                }else{
                    mp3DBHelper.updateMyMusicTBL(Mp3DBHelper.UPDATE_NO_HEART, activityMain.playingMusicData);
                    imageButtonHeart.setImageResource(R.drawable.heart);
                }

                break;

            case R.id.imageButtonList :
                break;

            default:
                break;
        }
    }

    //thread 사용하여 ui 변경
    public void threadUIChangeFunction() {
        Thread thread = new Thread() {
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

            @Override
            public void run() {
                if (activityMain.playingMusicData == null) {
                    return;
                }
                activityMain.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setMax(activityMain.mediaPlayer.getDuration());
                    }
                });
                while (activityMain.mediaPlayer.isPlaying()) {
                    activityMain.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(activityMain.mediaPlayer.getCurrentPosition());
                            textViewStartTime.setText(sdf.format(activityMain.mediaPlayer.getCurrentPosition()));
                        }
                    });

                    SystemClock.sleep(200);
                }
            }
        };
        thread.start();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }
}
