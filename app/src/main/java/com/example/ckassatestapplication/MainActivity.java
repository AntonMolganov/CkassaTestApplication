package com.example.ckassatestapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppCompatActivity mActivity;
    private Adapter mAdapter;
    private ProgressDialog mProgressDialog;
    private LoadTask mLoadTask;
    private AlertDialog.Builder mADB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(getResources().getString(R.string.main_title));

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter = new Adapter();
        recyclerView.setAdapter(mAdapter);


        mADB = new AlertDialog.Builder(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLoadTask == null) {
            mLoadTask = new LoadTask();
            mLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    protected void onPause() {
        if (mLoadTask != null) mLoadTask.cancel(true);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:

                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra(EditActivity.ID, -1);
                startActivity(intent);


                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{
        List<DictionaryEntry> data;

        Adapter() {
            this.data = new LinkedList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            DictionaryEntryView dev = new DictionaryEntryView(viewGroup.getContext());
            dev.setLeftText(data.get(i).getSource());
            dev.setRightText(data.get(i).getTranslation());
            return new ViewHolder(dev);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.view.setLeftText(data.get(i).getSource());
            viewHolder.view.setRightText(data.get(i).getTranslation());
            viewHolder.position = i;
            viewHolder.view.setOnClickListener(viewHolder);
            viewHolder.view.setOnLongClickListener(viewHolder);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setData(List<DictionaryEntry> newData) {
            this.data = newData;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
            int position;
            DictionaryEntryView view;
            ViewHolder(DictionaryEntryView view) {
                super(view);
                this.view = view;
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, EditActivity.class);
                intent.putExtra(EditActivity.ID, data.get(position).getId());
                startActivity(intent);
            }

            @Override
            public boolean onLongClick(View v) {
                view.setBackgroundColor(Color.LTGRAY);
                mADB.setMessage(R.string.delete_message)
                    .setPositiveButton(R.string.yes,this)
                    .setNegativeButton(R.string.no, this)
                    .setOnDismissListener(this)
                    .create()
                    .show();
                return true;
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE){
                    new DeleteTask(mActivity, mADB, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.get(position).getId());
                    mLoadTask = new LoadTask();
                    mLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    dialog.dismiss();
                }else{
                    dialog.dismiss();
                }
                view.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onDismiss(DialogInterface dialog) {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }

    private class LoadTask extends AsyncTask<Void,Void,List>{

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.show();
        }

        @Override
        protected List doInBackground(Void... voids) {
            List<DictionaryEntry> newData = DictionaryDatabase.getInstance(getApplicationContext()).dao().findAll();
            Collections.sort(newData);
            return newData;
        }

        @Override
        protected void onPostExecute(List list) {
            mAdapter.setData(list);
            mLoadTask = null;
            mProgressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            mLoadTask = null;
        }
    }

}
