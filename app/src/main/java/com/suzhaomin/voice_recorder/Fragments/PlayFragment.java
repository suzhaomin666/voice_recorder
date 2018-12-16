package com.suzhaomin.voice_recorder.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.suzhaomin.voice_recorder.PlaybackDialogFragment;
import com.suzhaomin.voice_recorder.R;
import com.suzhaomin.voice_recorder.RecordAudioDialogFragment;
import com.suzhaomin.voice_recorder.RecordingItem;


public class PlayFragment extends Fragment {

    private View view;

    public static PlayFragment newInstance() {
        Bundle args = new Bundle();
        PlayFragment fragment = new PlayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_play, container, false);
        Button b1 = view.findViewById(R.id.b1);
        b1.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      FragmentManager fm = getFragmentManager();
                                      final RecordAudioDialogFragment fragment = RecordAudioDialogFragment.newInstance();
                                      fragment.show(fm, RecordAudioDialogFragment.class.getSimpleName());
                                      fragment.setOnCancelListener(new RecordAudioDialogFragment.OnAudioCancelListener() {
                                          @Override
                                          public void onCancel() {
                                              fragment.dismiss();
                                          }
                                      });
                                  }
                              }
        );
        Button b2 = view.findViewById(R.id.b2);
        b2.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      RecordingItem recordingItem = new RecordingItem();
                                      FragmentManager fm = getFragmentManager();
                                      SharedPreferences sharePreferences = getContext().getSharedPreferences("sp_name_audio", getContext().MODE_PRIVATE);
                                      final String filePath = sharePreferences.getString("audio_path", "");
                                      long elpased = sharePreferences.getLong("elpased", 0);
                                      recordingItem.setFilePath(filePath);
                                      recordingItem.setLength((int) elpased);
                                      PlaybackDialogFragment fragmentPlay = PlaybackDialogFragment.newInstance(recordingItem);
                                      fragmentPlay.show(fm, PlaybackDialogFragment.class.getSimpleName());
                                  }
                              }
        );

        return view;

    }
}
