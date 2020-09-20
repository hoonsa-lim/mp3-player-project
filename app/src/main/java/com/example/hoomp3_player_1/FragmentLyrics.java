package com.example.hoomp3_player_1;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class FragmentLyrics extends Fragment {
    private Activity_Main activity_main;
    private TextView tvLyrics;
    private float startY;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity_main = (Activity_Main) getActivity();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //화면1 one_fragment 메모리로 로드
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.lyrics_fragment, container, false);
        tvLyrics = viewGroup.findViewById(R.id.tvLyrics);

        tvLyrics.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = motionEvent.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        float differenceValue = motionEvent.getY() - startY;//눌렀을 때와 뗐을 때의 차이값 저장
                        if(Math.abs(differenceValue) <= 40){//절대값이 40이상 차이가 났을 때만 이벤트 발생
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            fragmentManager.beginTransaction().remove(FragmentLyrics.this).commit();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        return viewGroup;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity_main = null;
    }
}
