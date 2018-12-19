package com.suzhaomin.voice_recorder.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.suzhaomin.voice_recorder.DBHelper;
import com.suzhaomin.voice_recorder.Fragments.PlaybackDialogFragment;
import com.suzhaomin.voice_recorder.Listeners.OnDatabaseChangedListener;
import com.suzhaomin.voice_recorder.R;
import com.suzhaomin.voice_recorder.RecordingItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
        implements OnDatabaseChangedListener {

    private static final String LOG_TAG = "FileViewerAdapter";

    private DBHelper mDatabase;

    RecordingItem item;
    Context mContext;
    LinearLayoutManager llm;
    public static boolean showCheckBox=false;
    public static boolean Deletemodel=false;
    //这个是checkbox的Hashmap集合
    public static HashMap<Integer, Boolean> map ;


    public static boolean isShowCheckBox() {
        return showCheckBox;
    }
    public static void setShowCheckBox(boolean scb) {
        showCheckBox = scb;
    }

    public static boolean isDeletemodel() {
        return Deletemodel;
    }
    public static void setDeletemodel(boolean scb) {
        Deletemodel = scb;
    }
    public void singlesel(int postion) {
        Set<Map.Entry<Integer, Boolean>> entries = map.entrySet();
        for (Map.Entry<Integer, Boolean> entry : entries) {
            entry.setValue(false);
        }
        map.put(postion, true);
        notifyDataSetChanged();
    }

    public FileViewerAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        mContext = context;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        llm = linearLayoutManager;
        map = new HashMap<>();
        for (int i = 0; i < 1000; i++)
            map.put(i, false);

    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, final int position) {

        item = getItem(position);
        long itemDuration = item.getLength();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);

        holder.vName.setText(item.getName());
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));

        if (showCheckBox) {
            holder.mCbItem.setVisibility(View.VISIBLE);
        } else {
            holder.mCbItem.setVisibility(View.GONE);
            //取消掉Checkbox后不再保存当前选择的状态
            holder.mCbItem.setChecked(false);
        }

        holder.mCbItem.setChecked(map.get(position));
        holder.vDateAdded.setText(
                DateUtils.formatDateTime(
                        mContext,
                        item.getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
                )
        );

        //如果是批量删除模式
        if(isDeletemodel())
        {
            //不是复选框就设置复选框出现
            if(!showCheckBox) {
                Log.v("asfg","不是复选框");
                setShowCheckBox(true);
                holder.mCbItem.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                       notifyDataSetChanged();
                    }
                };
                handler.post(r);
            }
            holder.mCbItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(showCheckBox)
                        //当按了按钮，将按的逻辑放进map里面
                        map.put(position, !map.get(position));
                    Log.v("Agasfga","按了positon+把他丢进油锅"+position);
                    //刷新适配器
                    notifyDataSetChanged();
                    //单选,这个应该用在一个确定按钮里面，随后刷新控件
//                    singlesel(position);
                }
            });
        }else{
            //如果不是批量删除模式，就是正常的
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if(!showCheckBox)
                        {
                            PlaybackDialogFragment playbackFragment =
                                    new PlaybackDialogFragment().newInstance(getItem(holder.getPosition()));

                            FragmentTransaction transaction = ((FragmentActivity) mContext)
                                    .getSupportFragmentManager()
                                    .beginTransaction();

                            playbackFragment.show(transaction, "dialog_playback");

                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "exception", e);
                    }
                }
            });

            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(!showCheckBox) {
                        ArrayList<String> entrys = new ArrayList<String>();
                        entrys.add(mContext.getString(R.string.dialog_file_share));
                        entrys.add(mContext.getString(R.string.dialog_file_rename));
//                entrys.add(mContext.getString(R.string.dialog_file_delete));

                        final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);


                        // File delete confirm
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(mContext.getString(R.string.dialog_title_options));
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) {
                                    shareFileDialog(holder.getPosition());
                                } if (item == 1) {
                                    renameFileDialog(holder.getPosition());
                                }
//                        else if (item == 2) {
//                            deleteFileDialog(holder.getPosition());
//                        }
                            }
                        });
                        builder.setCancelable(true);
                        builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    return false;
                }
            });
        }

    }



    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.cardview, parent, false);

        mContext = parent.getContext();

        return new RecordingsViewHolder(itemView);
    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;
        protected View cardView;
        protected CheckBox mCbItem;
        public RecordingsViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.file_name_text);
            vLength = (TextView) v.findViewById(R.id.file_length_text);
            vDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
            cardView = v.findViewById(R.id.card_view);
            mCbItem=v.findViewById(R.id.checkbox);
        }
    }

    @Override
    public int getItemCount() {
        return mDatabase.getCount();
    }

    public RecordingItem getItem(int position) {
        return mDatabase.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        notifyItemInserted(getItemCount() - 1);
        llm.scrollToPosition(getItemCount() - 1);
    }

    @Override
    //TODO
    public void onDatabaseEntryRenamed() {

    }

    public void remove(int position) {
        //remove item from database, recyclerview and storage

        //delete file from storage
        Log.v("Agasfga",position+"在瑟瑟发抖");
        File file;
        if((getItem(position).getFilePath()!=null)){
            file= new File(getItem(position).getFilePath());
            file.delete();
        }else return;



        Toast.makeText(
                mContext,
                String.format(
                        mContext.getString(R.string.toast_file_delete),
                        getItem(position).getName()
                ),
                Toast.LENGTH_SHORT
        ).show();

        mDatabase.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
    }

    //TODO
    public void removeOutOfApp(String filePath) {
        //user deletes a saved recording out of the application through another application
    }

    public void rename(int position, String name) {
        //rename a file

        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file.
            Toast.makeText(mContext,
                    String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();

        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    public void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));
    }

    public void renameFileDialog (final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim() + ".mp4";
                            rename(position, value);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    public void deleteFileDialog (final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from database, recyclerview, and storage
                            remove(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

}
