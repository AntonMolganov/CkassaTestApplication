package com.example.ckassatestapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditActivity extends AppCompatActivity implements DialogInterface.OnClickListener{

    final static String ID = "id";//-1 - add; 0... - edit
//    private final static String API_KEY = "trnsl.1.1.20180823T122605Z.0f872a1bd24f33e4.5428c7472248461895cd787c68ab0831ed272147";
    private final static String API_KEY = "dict.1.1.20180823T124933Z.89292fb8e2a0c4c5.ee814c8cd4c66bdd0beec2f9c20686aa6c5d76a4";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private AppCompatActivity mActivity;
    private EditText mSource;
    private EditText mTranslation;
    private ListView mSuggestions;
    private Button mButton;
    private ProgressBar mProgressBar;
    private Adapter mAdapter;
    private int mId;
    private YandexTask mYandexTask;
    private Gson mGson;
    private AlertDialog.Builder mADB;
    private ProgressDialog mProgressDialog;
    private LoadTask mLoadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_edit);
        mId = getIntent().getExtras().getInt(ID);

        if (mId == -1){
            getSupportActionBar().setTitle(getResources().getString(R.string.edit_title_add));
        }else{
            getSupportActionBar().setTitle(getResources().getString(R.string.edit_title_edit));
        }

        mSource = findViewById(R.id.source);
        mTranslation = findViewById(R.id.translation);
        mSuggestions = findViewById(R.id.suggestions);
        mButton = findViewById(R.id.commit);
        mProgressBar = findViewById(R.id.progressbar);
        if (mId == -1){
            mButton.setText(R.string.edit_button_add);
        }else{
            mButton.setText(R.string.edit_button_save);
        }
        mAdapter = new Adapter();
        mSuggestions.setAdapter(mAdapter);
        mSuggestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != mAdapter.getCount() - 1) {
                    mTranslation.setText(mAdapter.getData().get(position));
                }
            }
        });
        mSource.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mYandexTask != null) mYandexTask.cancel(true);
                mYandexTask = new YandexTask();
                mYandexTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mGson = new Gson();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new DictionaryEntry(mId, mSource.getText().toString(), mTranslation.getText().toString()));
            }
        });
        mADB = new AlertDialog.Builder(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mId != -1 && mLoadTask == null) {
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
        if (mId != -1){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu2, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                mADB.setMessage(R.string.delete_message)
                    .setPositiveButton(R.string.yes, this)
                    .setNegativeButton(R.string.no, this)
                    .create()
                    .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE){
            new DeleteTask(mActivity, mADB, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mId);
        }else{
            dialog.dismiss();
        }
    }

    private class Adapter extends BaseAdapter{
        private List<String> data = new LinkedList<>();
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = (TextView) getLayoutInflater().inflate(R.layout.suggestions_entry,null);
            tv.setText(data.get(position));
            return tv;
        }

        void setData(List<String> newData){
            data.clear();
            data.addAll(newData);
            notifyDataSetChanged();
        }

        List<String> getData(){
            return data;
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class YandexTask extends AsyncTask<Void,Void,Void>{

        List<String> mTranslations = new LinkedList<>();

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            mSuggestions.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String search_word = mSource.getText().toString();
            List<String> cachedTranslations = LRUCache.getInstance().get(search_word);
            if (cachedTranslations != null && cachedTranslations.size() > 0){
                mTranslations.addAll(cachedTranslations);
            }else{
                try {
                    String url_sb = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup" +
                            "?key=" + API_KEY +
                            "&text=" + URLEncoder.encode(search_word, "UTF-8") +
                            "&lang=ru-en";
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(JSON, "");
                    Request request = new Request.Builder()
                            .url(url_sb)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String response_string = response.body().string();
                    response.body().close();
                    response.close();
                    JsonElement e = mGson.fromJson(response_string, JsonElement.class);
                    JsonArray def_json_array = e.getAsJsonObject().get("def").getAsJsonArray();
                    for (int i = 0; i < def_json_array.size(); i++){
                        try{
                            JsonElement def_element = def_json_array.get(i);
                            JsonArray translations_json_array = def_element.getAsJsonObject().get("tr").getAsJsonArray();
                            for (int j = 0; j < translations_json_array.size(); j++){
                                try{
                                    JsonElement translation_json = translations_json_array.get(j);
                                    mTranslations.add(translation_json.getAsJsonObject().get("text").getAsString());
                                    if (translation_json.getAsJsonObject().has("syn")){
                                        JsonArray syn_json_array = translation_json.getAsJsonObject().get("syn").getAsJsonArray();
                                        for (int k = 0; k < syn_json_array.size(); k++){
                                            try{
                                                JsonElement syn_json = syn_json_array.get(k);
                                                mTranslations.add(syn_json.getAsJsonObject().get("text").getAsString());
                                            }catch (Exception ek){
                                                //do nothing, just catch to prevent loop failure and continue processing other elements
                                                ek.printStackTrace();
                                            }
                                        }
                                    }
                                }catch (Exception ej){
                                    //do nothing, just catch to prevent loop failure and continue processing other elements
                                    ej.printStackTrace();
                                }
                            }
                        }catch (Exception ei){
                            //do nothing, just catch to prevent loop failure and continue processing other elements
                            ei.printStackTrace();
                        }
                    }
                } catch (Exception e){
                    //do nothing, just catch to prevent app failure
                    e.printStackTrace();
                }
                LRUCache.getInstance().put(search_word, new LinkedList<>(mTranslations));
            }

            if (mTranslations.size() > 0) mTranslations.add(getString(R.string.yandex_compliance));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.setData(mTranslations);
            mProgressBar.setVisibility(View.INVISIBLE);
            mSuggestions.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadTask extends AsyncTask<Void,Void,DictionaryEntry>{

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.show();
        }

        @Override
        protected DictionaryEntry doInBackground(Void... voids) {
            List<DictionaryEntry> found_list = DictionaryDatabase.getInstance(getApplicationContext()).dao().findById(mId);
            if (found_list != null && found_list.size() >0) {
                return found_list.get(0);
            }else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(DictionaryEntry found) {
            if (found == null) {
                mADB.setMessage(R.string.loaderrormessage)
                    .setNeutralButton(R.string.Close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { finish();    }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {finish();
                        }
                    })
                    .create()
                    .show();
            }else {
                mSource.setText(found.getSource());
                mTranslation.setText(found.getTranslation());
            }
            mLoadTask = null;
            mProgressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            mLoadTask = null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateTask extends AsyncTask<DictionaryEntry,Void,Void>{

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(DictionaryEntry... dictionaryEntries) {
            for (DictionaryEntry entry : dictionaryEntries){
                if (entry.getId() == -1){
                    DictionaryDatabase.getInstance(mActivity.getApplicationContext()).dao().save(entry);
                }else{
                    DictionaryDatabase.getInstance(mActivity.getApplicationContext()).dao().update(entry);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mProgressDialog.dismiss();
            mADB.setMessage(R.string.donemessage)
                    .setNeutralButton(R.string.Close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { dialog.dismiss();    }
                    })
                    .create()
                    .show();
        }
    }
}
