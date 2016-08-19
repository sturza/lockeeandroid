package com.adipopa.lockee;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import pl.droidsonroids.gif.GifImageView;

public class ControlActivity extends AppCompatActivity {

    GifImageView unlockView, lockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_screen);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        unlockView = (GifImageView)findViewById(R.id.unlockView);
        lockView = (GifImageView)findViewById(R.id.lockView);

        TextView lockNicknameText = (TextView)findViewById(R.id.lockNickname);
        TextView lockIDText = (TextView)findViewById(R.id.lockID);
        TextView shareIDText = (TextView)findViewById(R.id.shareID);

        Bundle itemData = getIntent().getExtras();
        if(itemData == null){
            return;
        }
        String nickname = itemData.getString("nickname");
        lockNicknameText.setText(nickname);
        String lockID = itemData.getString("lockID");
        lockIDText.setText(lockID);
        String shareID = itemData.getString("shareID");
        shareIDText.setText(shareID);
        String status = itemData.getString("status");
        if(status.equals("unlocked")){
            unlockView.setAlpha(0f);
            lockView.setAlpha(1f);
        } else if (status.equals("locked")){
            unlockView.setAlpha(1f);
            lockView.setAlpha(0f);
        }
    }

    public void onControl(View view){
        TextView lockID = (TextView) findViewById(R.id.lockID);
        String lock_inner_id = lockID.getText().toString();
        new startControl().execute(lock_inner_id);
    }

    private class startControl extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String lock_inner_id = params[0];
            String switchLock_url = "https://lockee-cloned-andrei-b.c9users.io/android/lock_mechanic/";
            // This is the login request
            try {
                URL url = new URL(switchLock_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String postData = URLEncoder.encode("lock_inner_id", "UTF-8") + "=" + URLEncoder.encode(lock_inner_id, "UTF-8");
                bufferedWriter.write(postData);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result != null) {
                if(result.equals("unlocked")){
                    unlockView.setAlpha(0f);
                    lockView.setAlpha(1f);
                } else {
                    unlockView.setAlpha(1f);
                    lockView.setAlpha(0f);
                }
            } else {
                Log.e("LockControlHandler", "There was an error handling the control, please check connection");
            }
        }
    }

    public void onGenerateShareID(View view){
        Button shareIDButton = (Button) findViewById(R.id.shareIDButton);
        final ObjectAnimator rotate = ObjectAnimator.ofFloat(shareIDButton, "rotation", 0f, 360f);
        rotate.setDuration(750);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(rotate);
        mAnimationSet.start();
        Toast toast = Toast.makeText(ControlActivity.this, "ShareID generated \nTap the ID to copy it to your clipboard", Toast.LENGTH_LONG);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setGravity(Gravity.CENTER);
        toast.show();
    }

    public void onCopyShareID(View view){
        TextView shareID = (TextView) findViewById(R.id.shareID);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("shareID", shareID.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(ControlActivity.this, "ShareID copied to your clipboard", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}