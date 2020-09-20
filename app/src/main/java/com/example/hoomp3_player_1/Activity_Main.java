package com.example.hoomp3_player_1;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTabHost;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//implements FragmentOne.OnApplySelectedListener

public class Activity_Main extends AppCompatActivity
        implements FragmentOne.DataDeliverToMainActivity_FromFrag1,
        FragmentTwo.DataDeliverToMainActivity_FromFrag2,
        FragmentOne.DataDeliverToMainActivity_FromFrag1_resume {

    //UI
    //tab
    private FragmentTabHost tabHost;
    private TabHost.TabSpec tabSpecOne, tabSpecTwo, tabSpecThree;
    private ImageView imageView1, imageView2, imageView3;
    //bottom ui
    public ImageButton ibPicture_activity, ibNext_main, ibPlay_main, ibBack_main;
    public TextView tvMusicName_main, tvArtistName_main;
    private LinearLayout linear_go_tab3, linear_media_progress;
    public LinearLayout linearBottomBar;//tab3 이동시 가리기 위함

    //fragment
    public FragmentOne fragmentOne;
    public FragmentTwo fragmentTwo;
    public FragmentThree fragmentThree;
    private FragmentLyrics fragmentLyrics = new FragmentLyrics();

    //
    private ArrayList<MusicData> outputArrayMusicData = new ArrayList<MusicData>();    //화면에 보여질 때 사용하는 list
    private ArrayList<MusicData> inputArrayMusicData = new ArrayList<MusicData>();    //db에 들어갈 때 사용하는 list
    private ArrayList<MusicData> selectPlayListMusic = new ArrayList<MusicData>();    //현재 진행되고 있는 플레이 리스트

    //음악재생
    private String sdCardPath;//sd 절대 경로
    public MediaPlayer mediaPlayer = new MediaPlayer();
    public MusicData playingMusicData; //현재 재생되고 있는 곡
    private MusicData holding_musicData;
    private final int NEW_PLAY = 0;
    private final int PAUSE_PLAY = 1;
    private final int SAME_PLAY = 2;
    private final int PAUSE_START = 3;
    private final int NEXT_PLAY = 1;
    private final int BACK_PLAY = -1;

    private int btnFlag = NEW_PLAY; //0이면 새곡, 1이면 일시정지, 2면 같은 곡 일시정지 해제
    private long time = 0; //앱종료를 위한 변수
    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ui 찾기
        findViewByIdFunction();

        //mp3 list load
        mp3ResourceGetFunction();

        //file load 한 것 db 저장
        startAppSaveDBFunction();

        //최종 db에서 로드
        getDBmp3FileListFunction();

        //tabHost와 fragment 3개 연결하기
        matchTabAndFragmentFunction();

        //이벤트 등록 - interface 화하여 관리안하는 이유: 새로 만들어질 버튼들의 이벤트 등록을 위해서.
        ibPicture_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragmentThreeFunction();
            }
        });
        linear_go_tab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragmentThreeFunction();
            }
        });
        ibPlay_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMusicFunction(btnFlag, null);
            }
        });
        ibNext_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMusicNextAndBackFunction(NEXT_PLAY);

            }
        });
        ibBack_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMusicNextAndBackFunction(BACK_PLAY);
            }
        });

        //자동으로 다음곡 넘어가는 기능
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                ibNext_main.callOnClick();
            }
        });
    }

    public void setImageArtistNameSingNameEtcFunction(MusicData musicData) {
        if (tvArtistName_main.getText().equals(new String("-"))) {
            Toast.makeText(getApplicationContext(), "재생할 곡을 선택해주세요." + tvArtistName_main.getText(), Toast.LENGTH_SHORT).show();
        } else {
            byte[] data = musicData.getImage();
            Bitmap imageBitmap;
            if (musicData.getImage() != null) {
                imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                ibPicture_activity.setImageBitmap(imageBitmap);
            } else {
                ibPicture_activity.setImageResource(R.drawable.disk);
            }
            tvMusicName_main.setText(musicData.getMusicName());
            tvArtistName_main.setText(musicData.getArtistName());
            linearBottomBar.invalidate();

        }
    }


    //mp3 재생
    private void playMusicFunction(int num, MusicData m) {
        try {
            switch (num) {
                case NEW_PLAY:
                    playingMusicData = m;
                    setImageArtistNameSingNameEtcFunction(playingMusicData);
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(sdCardPath + playingMusicData.getFileName());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    holding_musicData = m;//현재곡 새로들어오는 곡 같은지 여부 확인을 위해
                    linear_media_progress.setVisibility(View.VISIBLE);
                    ibPlay_main.setImageResource(R.drawable.pause);
                    btnFlag = PAUSE_PLAY;
                    break;
                case PAUSE_PLAY:
                    mediaPlayer.pause();
                    linear_media_progress.setVisibility(View.INVISIBLE);
                    ibPlay_main.setImageResource(R.drawable.play);
                    btnFlag = SAME_PLAY;
                    break;
                case SAME_PLAY:
                    mediaPlayer.start();
                    linear_media_progress.setVisibility(View.VISIBLE);
                    ibPlay_main.setImageResource(R.drawable.pause);
                    btnFlag = PAUSE_PLAY;
                    break;
                case PAUSE_START:
                    playingMusicData = m;
                    setImageArtistNameSingNameEtcFunction(playingMusicData);
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(sdCardPath + playingMusicData.getFileName());
                    mediaPlayer.prepare();
                    linear_media_progress.setVisibility(View.INVISIBLE);
                    ibPlay_main.setImageResource(R.drawable.play);
                    btnFlag = SAME_PLAY;
                    break;
                default:
                    break;
            }

            //activity의 버튼 상태
            Bundle bundle = new Bundle();
            bundle.putInt("playBtnState", btnFlag);
            fragmentThree.setArguments(bundle);
        } catch (IOException e) {
            Log.d("Activity_Main", "IOException : " + e.getMessage());
        } catch (NullPointerException e) {
            Log.d("Activity_Main", "NullPointerException : " + e.getMessage());
        }
    }

    private void playMusicNextAndBackFunction(int nextOrBack) {
        MusicData md = null;
        Bundle bundle = new Bundle();

        switch (nextOrBack) {
            case NEXT_PLAY:
                try {
                    if (selectPlayListMusic.size() > (position + 1)) {//index out of bound exception 방지
                        position += nextOrBack;
                        md = selectPlayListMusic.get(position);
                        if (mediaPlayer.isPlaying() == true) {
                            playMusicFunction(NEW_PLAY, md);
                        } else {
                            playMusicFunction(PAUSE_START, md);
                        }
                    } else {
                        position = 0;
                        md = selectPlayListMusic.get(position);
                        if (mediaPlayer.isPlaying() == true) {
                            playMusicFunction(NEW_PLAY, md);
                        } else {
                            playMusicFunction(PAUSE_START, md);
                        }
                        Toast.makeText(getApplicationContext(), "재생목록 첫곡", Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException e) {
                    Log.d("Activity_Main", "NullPointerException : " + e.getMessage());
                } catch (IndexOutOfBoundsException e) {
                    Log.d("Activity_Main", "IndexOutOfBoundsException : " + e.getMessage());
                }
                break;


            case BACK_PLAY:
                try {
                    if (position <= 0) {//index out of bound exception 방지
                        position = (selectPlayListMusic.size() - 1);
                        md = selectPlayListMusic.get(position);
                        if (mediaPlayer.isPlaying() == true) {
                            playMusicFunction(NEW_PLAY, md);
                        } else {
                            playMusicFunction(PAUSE_START, md);
                        }
                        Toast.makeText(getApplicationContext(), "재생목록 마지막곡", Toast.LENGTH_SHORT).show();
                    } else {
                        position += nextOrBack;
                        md = selectPlayListMusic.get(position);
                        if (mediaPlayer.isPlaying() == true) {
                            playMusicFunction(NEW_PLAY, md);
                        } else {
                            playMusicFunction(PAUSE_START, md);
                        }
                    }
                } catch (NullPointerException e) {
                    Log.d("Activity_Main", "NullPointerException : " + e.getMessage());
                } catch (IndexOutOfBoundsException e) {
                    Log.d("Activity_Main", "IndexOutOfBoundsException : " + e.getMessage());
                }
                break;
            default:
                break;
        }
        bundle.putParcelable("music", md);
        fragmentThree.setArguments(bundle);
    }

    //fragment 3로 이동
    private void changeFragmentThreeFunction() {
        if (tvArtistName_main.getText().equals(new String("-"))) {
            Toast.makeText(getApplicationContext(), "재생할 곡을 선택해주세요." + tvArtistName_main.getText(), Toast.LENGTH_SHORT).show();
        } else {
            tabHost.setCurrentTab(2);
        }
    }

    //db에 등록된 곡 load
    private void getDBmp3FileListFunction() {
        Mp3DBHelper mp3DBHelper = new Mp3DBHelper(getApplicationContext());
        outputArrayMusicData.clear();
        outputArrayMusicData = mp3DBHelper.selectMyMusicTBL(Mp3DBHelper.ALIGN_TOTAL, null);
        if (outputArrayMusicData != null) {
            Log.d("selectMyMusicTBL()", "DB 로드 성공 : db에 저장된 mp3 로드 성공");
        } else {
            Log.d("selectMyMusicTBL()", "DB 로드 실패 : db에 저장된 mp3가 없거나 로드 실패");
        }
    }

    //file load 한 것 db 저장
    private void startAppSaveDBFunction() {
        Mp3DBHelper mp3DBHelper = new Mp3DBHelper(getApplicationContext());
        mp3DBHelper.onUpgrade(mp3DBHelper.getWritableDatabase(), 0, 1);
        for (MusicData m : inputArrayMusicData) {
            boolean returnValue = mp3DBHelper.insertMyMusicTBL(m);
            if (returnValue == true) {
                Log.d("insertMyMusicTBL()", "DB 저장 성공 : 휴대폰에 내장된 mp3 DB저장 성공");
            } else {
                Log.d("insertMyMusicTBL()", "DB 저장 실패 : 휴대폰에 내장된 mp3 DB저장 실패");
            }
        }
    }

    //mp3 list load
    private void mp3ResourceGetFunction() {
        //db에 등록된 곡 load
        getDBmp3FileListFunction();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);//권한 요청
        sdCardPath = Environment.getExternalStorageDirectory().getPath() + "/"; //sdcard 절대 경로
        File[] listFile = new File(sdCardPath).listFiles(); //절대경로에 있는 file 전체 가져옴

        //mp3 file load (외장)
        getMp3FileListFunction(listFile);

        //mp3 file load (내장)
//        getMp3FileListFunction(listFile);

        if (inputArrayMusicData == null)
            Toast.makeText(getApplicationContext(), "재생가능한 mp3가 없습니다.", Toast.LENGTH_SHORT).show();
        Log.d("Activity_Main", "mp3 load 성공");
    }

    //
    private ArrayList<MusicData> getMp3FileListFunction(File[] listFile) {
        for (File f : listFile) {
            String fileName = f.getName();
            if (fileName.endsWith("mp3")) {//file 이름의 끝이 mp3로 끝나는가?

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(sdCardPath + fileName);//여기서 mp3 file 선택됨, 실질적으로

                //이미지
                byte[] data = null;
                data = mmr.getEmbeddedPicture();

                //가수명
                String metaName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if (metaName == null) metaName = "(unknown)";

                //제목
                String metaMusicName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if (metaMusicName == null) metaMusicName = "(unknown)";

                //영상길이
                String metaMusicDuration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (metaMusicDuration == null) {
                    metaMusicDuration = "(unknown)";
                } else {
                    long timemillisec = Long.parseLong(metaMusicDuration);
                    long duration = timemillisec / 1000;
                    long hours = duration / 3600;
                    long minutes = (duration - hours * 3600) / 60;
                    long seconds = duration - (hours * 3600 + minutes * 60);
                    String sHours = String.valueOf(hours);
                    String sMinutes = String.valueOf(minutes);
                    String sSeconds = String.valueOf(seconds);

                    if (sHours.length() <= 1) sHours = "0" + sHours;
                    if (sMinutes.length() <= 1) sMinutes = "0" + sMinutes;
                    if (sSeconds.length() <= 1) sSeconds = "0" + sSeconds;

                    metaMusicDuration = sHours + ":" + sMinutes + ":" + sSeconds;
                }

                //장르 명
                String metaGenre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                if (metaGenre == null) metaGenre = "(unknown)";

                //앨범 명
                String metaAlbumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                if (metaAlbumName == null) metaAlbumName = "(unknown)";

                //발매일
                String metaReleaseDate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
                if (metaReleaseDate == null) metaReleaseDate = "(unknown)";

                MusicData musicData = new MusicData(data, metaName, metaMusicName, metaMusicDuration, metaGenre, metaAlbumName, metaReleaseDate, -1, "null", 0, fileName);
                inputArrayMusicData.add(musicData);

                //자원 반납 -> FAILED BINDER TRANSACTION 발생해서 해봄
                data = null;
                mmr = null;
            }
        }
        return inputArrayMusicData;
    }

    //tabHost에 custom frameLayout을 지정
    private void matchTabAndFragmentFunction() {
        //번들객체 생성
        Bundle fragTwoBundle = new Bundle();
        Bundle fragOneBundle = new Bundle();
        fragOneBundle.putParcelableArrayList("totalListMP3", outputArrayMusicData);
        fragTwoBundle.putParcelableArrayList("totalListMP3", outputArrayMusicData);

        //tabhost와 custom framelayout 연결
        tabHost.setup(getApplicationContext(), getSupportFragmentManager(), R.id.tabRealContent);

        //객체를 만들어서 넣는 식으로 하지 않으면 bundle 사용, fragment 객체에 접근하기 어려움
        fragmentOne = new FragmentOne();
        fragmentTwo = new FragmentTwo();
        fragmentThree = new FragmentThree();

        //tab widget에 들어갈 icon 생성
        imageView1 = new ImageView(this);
        imageView2 = new ImageView(this);
        imageView3 = new ImageView(this);
        imageView1.setImageResource(R.drawable.tab_one);
        imageView2.setImageResource(R.drawable.tab_two);
        imageView3.setImageResource(R.drawable.tab_three);

        //tab spec 3개 생성
        tabSpecOne = tabHost.newTabSpec("ONE").setIndicator(imageView1);
        tabSpecTwo = tabHost.newTabSpec("TWO").setIndicator(imageView2);
        tabSpecThree = tabHost.newTabSpec("THREE").setIndicator(imageView3);

        //tab spec을 tabhost에 저장함
        tabHost.addTab(tabSpecOne, fragmentOne.getClass(), fragTwoBundle);
        tabHost.addTab(tabSpecTwo, fragmentTwo.getClass(), fragOneBundle);
        tabHost.addTab(tabSpecThree, fragmentThree.getClass(), null);

        //시작 화면을 0번째 tab으로 설정함
        tabHost.setCurrentTab(0);
    }

    //ui 찾기
    private void findViewByIdFunction() {
        tabHost = (FragmentTabHost) findViewById(R.id.tabHost);
        ibPicture_activity = findViewById(R.id.ibPicture_activity);
        tvMusicName_main = findViewById(R.id.textView_main);
        tvArtistName_main = findViewById(R.id.textView2_main);
        ibBack_main = findViewById(R.id.ibBack_main);
        ibPlay_main = findViewById(R.id.ibPlay_main);
        ibNext_main = findViewById(R.id.ibNext_main);
        linear_go_tab3 = findViewById(R.id.linear_intent);
        linear_media_progress = findViewById(R.id.linear_now);
        linearBottomBar = findViewById(R.id.linearBottomBar);

        //초기 설정
        linear_media_progress.setVisibility(View.INVISIBLE);
    }

    //tranjection exception 방지를 위해 콜백함수 선언
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    //fragment 3번째에서 가사 fragment를 띄우기 위한 함수
    public void lyricsFragmentShow() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameLayout_imageLyrics, fragmentLyrics).commit();
    }

    //이 이벤트 발생 시 값을 바로 받기 위한 인터페이스
    //fragment1에서 전달해옴
    @Override
    public void onSelectedMusic1(ArrayList<MusicData> arrayList, int position) {
        try {
            selectPlayListMusic = arrayList;
            this.position = position;

            MusicData musicData = arrayList.get(position);
            playMusicFunction(NEW_PLAY, musicData);

            //fragment 3로 전달
            Bundle bundle = new Bundle();
            bundle.putParcelable("holdingMusic1", musicData);
            fragmentThree.setArguments(bundle);
        } catch (IndexOutOfBoundsException e) {
            Log.d("Activity_main", e.getMessage());
        }
    }

    //fragment2에서 전달해옴
    @Override
    public void onSelectedMusic2(ArrayList<MusicData> arrayList, int position) {
        MusicData musicData = arrayList.get(position);

        playMusicFunction(NEW_PLAY, musicData);

        //fragment 3로 전달
        Bundle bundle = new Bundle();
        bundle.putParcelable("holdingMusic2", musicData);
        fragmentThree.setArguments(bundle);
    }

    //fragment1 에서 resume 상황에서 넘겨주는 값 - 재생 함수만 없음
    @Override
    public void onSelectedMusic1_resume(ArrayList<MusicData> arrayList, int position) {
        try {
            selectPlayListMusic = arrayList;
            this.position = position;
            MusicData musicData = arrayList.get(position);

            //fragment 3로 전달
            Bundle bundle = new Bundle();
            bundle.putParcelable("holdingMusic1", musicData);
            fragmentThree.setArguments(bundle);
        } catch (IndexOutOfBoundsException e) {
            Log.d("Activity_main", e.getMessage());
        }
    }


    //앱 종료
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "'뒤로가기' 버튼 2번 누룰 시 앱이 종료 됩니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {
            mediaPlayer.stop();
            finish();
        }
    }


}

