package com.example.ckassatestapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class DeleteTask extends AsyncTask<Integer,Void,Boolean> {

    @SuppressLint("StaticFieldLeak")
    private Activity mActivity;
    private ProgressDialog mProgressDialog;
    private AlertDialog.Builder mADB;
    private boolean mFinish;

    DeleteTask(Activity activity, AlertDialog.Builder builder, boolean finish) {
        this.mActivity = activity;
        this.mADB = builder;
        this.mFinish = finish;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        try {
            DictionaryDatabase db = DictionaryDatabase.getInstance(mActivity.getApplicationContext());
            if (integers.length > 0) db.dao().delete(integers[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            mProgressDialog.dismiss();
            if (mFinish) mActivity.finish();
        } else {
            mADB.setMessage(R.string.deleteerrormessage)
                .setNeutralButton(R.string.Close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
        }
    }
}