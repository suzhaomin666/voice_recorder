package com.suzhaomin.voice_recorder.Listeners;


public interface OnDatabaseChangedListener {
    void onNewDatabaseEntryAdded();
    void onDatabaseEntryRenamed();
}