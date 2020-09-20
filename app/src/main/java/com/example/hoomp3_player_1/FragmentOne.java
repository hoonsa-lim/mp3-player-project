package com.example.hoomp3_player_1;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class FragmentOne extends Fragment {
    private Activity_Main activityMain;
    private Button btnPushedHartButtonList, btnNowPlayList;
    private ImageButton ibAddPlayList;
    private ListView frag1_listView;
    private LinearLayout linear_scrollView;
    private EditText editText_dialog;//dialog 에 입력한 플레이리스트 명
    private ListView dialog_listView, dialog_NowListView;//dialog

    private ArrayList<MusicData> arrayMusicData = new ArrayList<MusicData>();    //전체 리스트 중 재생목록으로 지정되지 않은 목록
    private ArrayList<MusicData> fragOne_playArrayList = new ArrayList<MusicData>();    //custom 재생목록을 로드할 때 사용함
    private int position;

    //어뎁터
    private ListViewAdapter listViewAdapter = null; // listview
    private ArrayAdapter adapter = null; //dialog listview

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activityMain = (Activity_Main) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.one_fragment, container, false);
        activityMain.linearBottomBar.setVisibility(View.VISIBLE);
        //ui 찾기
        findViewByIdFunction(viewGroup);

        //내 플레이 리스트 목록 로드 및 버튼 생성
        loadPlayListTitle();

        //첫 화면 현재재생목록 로드
        loadNowPlayList();

        //어뎁터 연결 - 첫화면 '현재재생목록'
        setAdapterFunction();

        //이벤트 등록
        ibAddPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogAddListFunction();
            }
        });
        btnPushedHartButtonList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadHeartPlayList();
            }
        });
        btnNowPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadNowPlayList();
            }
        });
        frag1_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mp3SettingFunction(position);

            }
        });
        return viewGroup;
    }

    //현재 재생목록 로드
    private void loadNowPlayList() {
        try {
            Mp3DBHelper mp3DBHelper = new Mp3DBHelper(activityMain);
            fragOne_playArrayList.clear();
            fragOne_playArrayList = mp3DBHelper.selectMyMusicTBL(Mp3DBHelper.SELECTED_NOW_PLAYLIST, null);
            if (fragOne_playArrayList.size() != 0) {
                Log.d("FragmentOne", "heart 목록 로드 성공 " + fragOne_playArrayList.size() + " 개");
            } else {
                showDialogAddPlayListFunction();
                Log.d("FragmentOne", "heart 목록 로드 실패");
            }
        } catch (Exception e) {
            Log.d("FragmentOne", e.getMessage() + " 예외 발생");
        }
        setAdapterFunction();
    }

    //선택한 값을 mainActivity의 view 에 올림
    private void mp3SettingFunction(int position) {
        MusicData musicData = fragOne_playArrayList.get(position);

        byte[] data = musicData.getImage();
        Bitmap imageBitmap = null;
        if (musicData.getImage() != null) {
            imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            activityMain.ibPicture_activity.setImageBitmap(imageBitmap);
        } else {
            activityMain.ibPicture_activity.setImageResource(R.drawable.disk);
        }
        activityMain.tvMusicName_main.setText(musicData.getMusicName());
        activityMain.tvArtistName_main.setText(musicData.getArtistName());
        activityMain.linearBottomBar.invalidate();

        //activity로 데이터 전달하는 interface
        this.position = position;
        ((DataDeliverToMainActivity_FromFrag1)activityMain).onSelectedMusic1(fragOne_playArrayList, position);
    }

    //내 플레이 리스트 목록 로드 및 버튼 생성
    private void loadPlayListTitle() {
        try {
            Mp3DBHelper mp3DBHelper = new Mp3DBHelper(activityMain);
            ArrayList<MusicData> playlistTitle = mp3DBHelper.selectMyMusicTBL(Mp3DBHelper.PLAYLIST_TITLE, null);
            if (playlistTitle != null) {
                for (MusicData md : playlistTitle) {
                    String str = md.getArtistName();//앨범 네임이긴 하지만 실제로는 제생목록명이 들어있음
                    makePlaylistButtonFunction(str);
                }
            } else {
                Log.d("FragmentOne", "재생 목록 제목 없거나 실패");
            }
        } catch (Exception e) {
            Log.d("FragmentOne", e.getMessage() + " 예외 발생");
        }
    }

    //heart 목록 로드
    private void loadHeartPlayList() {
        try {
            Mp3DBHelper mp3DBHelper = new Mp3DBHelper(activityMain);
            fragOne_playArrayList.clear();
            fragOne_playArrayList = mp3DBHelper.selectMyMusicTBL(Mp3DBHelper.ALIGN_HEART, null);
            if (fragOne_playArrayList.size() != 0) {

                Log.d("FragmentOne", "heart 목록 로드 성공 " + fragOne_playArrayList.size() + " 개");
            } else {
                Log.d("FragmentOne", "heart 목록 로드 실패");
            }
        } catch (Exception e) {
            Log.d("FragmentOne", e.getMessage() + " 예외 발생");
        }
        setAdapterFunction();
    }

    //ui 찾기
    private void findViewByIdFunction(ViewGroup viewGroup) {
        btnPushedHartButtonList = viewGroup.findViewById(R.id.btnPushedHartButtonList);
        btnNowPlayList = viewGroup.findViewById(R.id.btnNowPlayList);
        ibAddPlayList = viewGroup.findViewById(R.id.ibAddPlayList);
        frag1_listView = viewGroup.findViewById(R.id.frag1_listView);
        linear_scrollView = viewGroup.findViewById(R.id.linear_scrollView);
    }

    //main 반납 생명주기 마지막
    @Override
    public void onDetach() {
        super.onDetach();
        activityMain = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        //activity로 데이터 전달하는 interface
        ((DataDeliverToMainActivity_FromFrag1_resume)activityMain).onSelectedMusic1_resume(fragOne_playArrayList, position);
    }

    //재생목록 버튼 생성 함수
    private void makePlaylistButtonFunction(final String btnName) {
        Button button = new Button(activityMain);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = 10;
        params.rightMargin = 10;
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        button.setLayoutParams(params);

        button.setPadding(30, 1, 30, 1);
        button.setTextColor(Color.BLACK);
        button.setText(btnName);
        button.setBackgroundResource(R.drawable.btn_totallist2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mp3DBHelper mp3DBHelper1 = new Mp3DBHelper(activityMain);
                MusicData m = new MusicData(0, btnName);
                fragOne_playArrayList.clear();
                fragOne_playArrayList = mp3DBHelper1.selectMyMusicTBL(Mp3DBHelper.SELECTED_PLAYLIST, m);
                if (fragOne_playArrayList.size() != 0) {
                    Log.d("FragmentOne", "성공! : 선택한 플레이리스트의 곡 로드" + fragOne_playArrayList.size() + " 개");
                } else {
                    Log.d("FragmentOne", "실패! : 선택한 플레이리스트의 곡이 없거나 오류가 발생했습니다.");
                }
                setAdapterFunction();
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                linear_scrollView.removeView(view);
                return false;
            }
        });

        linear_scrollView.addView(button);
    }

    //custom 다이얼로그 기능
    private void showDialogAddListFunction() {
        View dialogView = View.inflate(activityMain, R.layout.dialog_make_playlist, null);
        editText_dialog = dialogView.findViewById(R.id.editText_dialog);
        dialog_listView = dialogView.findViewById(R.id.dialog_NowListView);

        //어뎁터 설정
        Mp3DBHelper mp3DBHelper = new Mp3DBHelper(activityMain);
        arrayMusicData = mp3DBHelper.selectMyMusicTBL(Mp3DBHelper.NON_PLAYLIST, null);
        ArrayList<String> strArrayList = new ArrayList<String>();
        strArrayList.clear();
        for (MusicData m : arrayMusicData) {
            String str = m.getMusicName() + " - ";
            strArrayList.add(str.concat(m.getArtistName()));
        }
        adapter = new ArrayAdapter(activityMain, android.R.layout.simple_list_item_multiple_choice, strArrayList);
        dialog_listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        dialog_listView.setAdapter(adapter);

        //특수문자 및 길이 10자 제한
        editText_dialog.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ \\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");
                if (charSequence.equals("") || ps.matcher(charSequence).matches()) {
                    return charSequence;
                }
                return "";
            }
        }, new InputFilter.LengthFilter(10)});

        androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(activityMain);
        dialog.setView(dialogView);
        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String s = editText_dialog.getText().toString();
                if (s.equals("")) {
                    Toast.makeText(activityMain, "재생목록 이름을 설정해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    SparseBooleanArray checkedItems = dialog_listView.getCheckedItemPositions();
                    int count = adapter.getCount();
                    if (count != 0 || checkedItems.size() != 0) {
                        for (int index = count - 1; index >= 0; index--) {
                            if (checkedItems.get(index)) {
                                MusicData md = null;
                                md = arrayMusicData.get(index);
                                Mp3DBHelper mp3DBHelper = new Mp3DBHelper(activityMain);
                                md.setPlayListName(s);
                                try {
                                    mp3DBHelper.updateMyMusicTBL(Mp3DBHelper.UPDATE_PLAYLIST, md);
                                    Log.d("FragmentOne", "성공! : 플레이리스트 명을 선택한 곡에 수정저장");
                                } catch (Exception e) {
                                    Log.d("FragmentOne", "실패! : updateMyMusicTBL 예외 발생");
                                }
                                fragOne_playArrayList = mp3DBHelper.selectMyMusicTBL(Mp3DBHelper.ALIGN_PLAYLIST, md);
                                if (fragOne_playArrayList != null) {
                                    Log.d("FragmentOne", "성공! : 플레이리스트 명을 기준으로 검색 및 로드 완료");
                                } else {
                                    Log.d("FragmentOne", "실패! : 플레이리스트 명을 기준으로 검색 하여 가져온 정보가 없음");
                                }

                            }
                        }
                        makePlaylistButtonFunction(s);
                    }
                }
            }
        });
        dialog.setNegativeButton("취소", null);
        dialog.show();
    }

    //현재 재생목록이 없을 때 다이얼로그 기능
    private void showDialogAddPlayListFunction() {
        View dialogView = View.inflate(activityMain, R.layout.dialog_make_now_playlist, null);
        dialog_NowListView = dialogView.findViewById(R.id.dialog_NowListView);

        //어뎁터 설정
        Mp3DBHelper mp3DBHelper = new Mp3DBHelper(activityMain);
        arrayMusicData = mp3DBHelper.selectMyMusicTBL(Mp3DBHelper.SELECTED_NON_NOW_PLAYLIST, null);
        ArrayList<String> strArrayList = new ArrayList<String>();
        strArrayList.clear();
        for (MusicData m : arrayMusicData) {
            String str = m.getArtistName() + " - ";
            strArrayList.add(str.concat(m.getMusicName()));
        }
        adapter = new ArrayAdapter(activityMain, android.R.layout.simple_list_item_multiple_choice, strArrayList);
        dialog_NowListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        dialog_NowListView.setAdapter(adapter);


        androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(activityMain);
        dialog.setView(dialogView);
        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SparseBooleanArray checkedItems = dialog_NowListView.getCheckedItemPositions();
                int count = adapter.getCount();
                if (count != 0 || checkedItems.size() != 0) {
                    for (int index = count - 1; index >= 0; index--) {
                        if (checkedItems.get(index)) {
                            MusicData md = null;
                            md = arrayMusicData.get(index);
                            Mp3DBHelper mp3DBHelper = new Mp3DBHelper(activityMain);
                            md.setCountPlay(1);
                            try {
                                mp3DBHelper.updateMyMusicTBL(Mp3DBHelper.UPDATE_SAVE_NOW_PLAYLIST, md);
                                Log.d("FragmentOne", "성공! : 현재 재생 목록으로 수정 저장");
                            } catch (Exception e) {
                                Log.d("FragmentOne", "실패! : updateMyMusicTBL 예외 발생");
                            }
                            fragOne_playArrayList = mp3DBHelper.selectMyMusicTBL(Mp3DBHelper.SELECTED_NOW_PLAYLIST, null);
                            if (fragOne_playArrayList != null) {
                                Log.d("FragmentOne", "성공! : 플레이리스트 명을 기준으로 검색 및 로드 완료");
                            } else {
                                Log.d("FragmentOne", "실패! : 플레이리스트 명을 기준으로 검색 하여 가져온 정보가 없음");
                            }
                        }
                    }
                }
                loadNowPlayList();
            }
        });
        dialog.setNegativeButton("취소", null);
        dialog.show();
    }

    //adapter 적용 모듈
    private void setAdapterFunction() {
        listViewAdapter = new ListViewAdapter(activityMain);
        listViewAdapter.setArrayList(fragOne_playArrayList);
        frag1_listView.setAdapter(listViewAdapter);
        frag1_listView.invalidate();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    //activity로 정보 전달을 위한 interface
    public interface DataDeliverToMainActivity_FromFrag1 {
        void onSelectedMusic1(ArrayList<MusicData> arrayList, int position);
    }
    //activity로 정보 전달을 위한 interface : onResume 용
    public interface DataDeliverToMainActivity_FromFrag1_resume {
        void onSelectedMusic1_resume(ArrayList<MusicData> arrayList, int position);
    }


}


