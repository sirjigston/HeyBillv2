package com.jigwtf.heybill.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.jigwtf.heybill.R;
import com.jigwtf.heybill.utils.AppController;

import static com.jigwtf.heybill.activities.MainActivity.login_user_id;


public class NativeVideoPlayerActivity extends AppCompatActivity {

    String userId, contentId, contentTitle, contentImage, contentUrl, contentTypeId;
    Context context = this;
    Handler handler;
    Runnable myRunnable;
    VideoView videoView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fullscreen
        getSupportActionBar().hide();
        View decorView = this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        setContentView(R.layout.activity_native_video_player);

        loadInterstitialAd();

        //Update user coin
        if(login_user_id.equals("Not Login")) {
            //No Action
        }else{
            //Update user coin from watching video after 30 sec
            handler = new Handler();
            myRunnable = new Runnable() {
                public void run() {
                    final String theUserId = login_user_id;
                    final String theUserCoin = ((AppController) NativeVideoPlayerActivity.this.getApplication()).getReward_coin_watching_video();
                    final String updateCoinType = "playVideo";
                    final String expirationTime = ((AppController) NativeVideoPlayerActivity.this.getApplication()).getReward_coin_watching_video_exp();
                }
            };
            handler.postDelayed(myRunnable,30000);
        }

        //Get Intent Data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("contentId")) {
                userId = extras.getString("userId");
                contentId = extras.getString("contentId");
                contentTitle = extras.getString("contentTitle");
                contentImage = extras.getString("contentImage");
                contentUrl = extras.getString("contentUrl");
                contentTypeId = extras.getString("contentTypeId");
            }
        }

        //Native Video Player
        videoView =(VideoView)findViewById(R.id.native_video_player);
        //Set MediaController  to enable play, pause, forward, etc options.
        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);
        //Starting VideView By Setting MediaController and URI
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(contentUrl));
        videoView.requestFocus();
        videoView.start();
    }


    //============================================================================//
    private void loadInterstitialAd(){
        // Interstitial Ad
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, ((AppController) this.getApplication()).getAdmobSettingAppId());
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(((AppController) this.getApplication()).getAdmobSettingInterstitialUnitId());
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                if(login_user_id.equals("Not Login")) {
                    //No Action
                }else{
                    final String theUserId = login_user_id;
                    final String theUserCoin = ((AppController) NativeVideoPlayerActivity.this.getApplication()).getReward_coin_interstitial_ad_click();
                    final String updateCoinType = "interstitialAd";
                    final String expirationTime = ((AppController) NativeVideoPlayerActivity.this.getApplication()).getReward_coin_interstitial_ad_exp();
                }
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
            }
        });
    }



    //============================================================================//
    public void onBackPressed() {
        if(login_user_id.equals("Not Login")) {
            //No Action
        }else{
            handler.removeCallbacks(myRunnable);
        }
        videoView.stopPlayback();

        // InterstitialAd
        if (mInterstitialAd.isLoaded()) {
            if (((AppController) this.getApplication()).getAdmobSettingInterstitialStatus().equals("1"))
            {
                if(login_user_id.equals("Not Login")) {
                    mInterstitialAd.show();

                }else{
                    if (((AppController) this.getApplication()).getUserHideInterstitialAd().equals("0")) //Check user hide ads or not
                    {
                        mInterstitialAd.show();
                    }
                }
            }
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }

        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        //videoView.pause();
        pausePlayer();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        //videoView.resume();
        startPlayer();
    }

    private void pausePlayer(){
        videoView.start();
    }

    private void startPlayer(){
        videoView.start();
    }
}
