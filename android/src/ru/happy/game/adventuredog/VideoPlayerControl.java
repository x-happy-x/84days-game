package ru.happy.game.adventuredog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import ru.happy.game.adventuredog.Interfaces.VideoPlayer;

public class VideoPlayerControl implements VideoPlayer, Player.EventListener {

    private final PlayerView videoPlayer;
    private final SimpleExoPlayer player;
    private long duration;
    private final TextView title;
    private final TextView subtitle;
    private final TextView message;
    private final TextView min;
    private final TextView sec;
    private final Button skip;
    private final View parent;
    private boolean playing;
    private boolean videoShowing;
    private final Handler handler;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (playing) {
                update();
                handler.postDelayed(runnable, 1000);
            }
        }
    };
    private PlayerListener listener;
    Activity activity;

    public VideoPlayerControl(AndroidApplication app) {
        activity = app;
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        handler = new Handler();
        videoPlayer = app.findViewById(R.id.video);
        title = app.findViewById(R.id.pTitle);
        subtitle = app.findViewById(R.id.pSubtitle);
        message = app.findViewById(R.id.pMessage);
        min = app.findViewById(R.id.pMinute);
        sec = app.findViewById(R.id.pSecond);
        skip = app.findViewById(R.id.skip);
        skip.setOnClickListener(v -> stop());
        parent = (View) videoPlayer.getParent();
        stop();
        player = new SimpleExoPlayer.Builder(app).build();
        videoPlayer.setPlayer(player);
        player.addListener(this);
        videoPlayer.setUseController(false);
    }

    @Override
    public void setListener(PlayerListener listener){
        this.listener = listener;
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    @Override
    public void pause() {
        if (player != null && videoShowing){
            playing = false;
            player.pause();
        }
    }

    @Override
    public void play() {
        if (player != null && videoShowing){
            playing = true;
            handler.post(runnable);
            player.play();
        }
    }

    @Override
    public void setBounds(float x, float y, float w, float h) {
        parent.setX(x);
        parent.setY(y);
        parent.getLayoutParams().height = (int) h;
        parent.getLayoutParams().width = (int) w;
    }

    @Override
    public void loadVideo(String url, String title, String subtitle){
        activity.runOnUiThread(()->{
            this.title.setText(title);
            this.subtitle.setText(subtitle);
            this.message.setText("Загрузка...");
            this.title.setVisibility(View.VISIBLE);
            this.subtitle.setVisibility(View.VISIBLE);
            this.message.setVisibility(View.VISIBLE);
            MediaItem item = MediaItem.fromUri(url);
            player.setMediaItem(item);
            player.setPlayWhenReady(true);
            player.prepare();
        });
    }


    @Override
    public void stop() {
        playing = false;
        videoShowing = false;
        activity.runOnUiThread(()->{
            if (player != null) {
                player.clearMediaItems();
                duration = 0;
                //player.release();
            }
            min.setVisibility(View.GONE);
            sec.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
            skip.setVisibility(View.GONE);
            parent.setVisibility(View.GONE);
            message.setText("");
        });
        if (listener != null) listener.onStop();
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        activity.runOnUiThread(() -> OnStateChanged(state));
    }

    public void update(){
        long position = player == null? 0 : player.getCurrentPosition();
        int minute = (int) (((duration-position)/1000)/60);
        int second = (int) ((duration-minute*1000*60-position)/1000);
        activity.runOnUiThread(() -> {
            if (min.getVisibility() == View.GONE){
                min.setVisibility(View.VISIBLE);
                sec.setVisibility(View.VISIBLE);
            }
            if (skip.getVisibility() == View.GONE && position >= 15000)
                skip.setVisibility(View.VISIBLE);
            min.setText(String.format(Locale.getDefault(),"%02d",minute));
            sec.setText(String.format(Locale.getDefault(),"%02d",second));
        });
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        activity.runOnUiThread(() -> message.setText(error.getLocalizedMessage()));
        if (listener != null) listener.onError(error.getLocalizedMessage());
    }

    private void OnStateChanged(int state){
        switch (state) {
            case Player.STATE_BUFFERING:
                message.setText("Загрузка...");
                break;
            case ExoPlayer.STATE_READY:
                message.setText("");
                duration = player.getDuration();
                if (parent.getVisibility() == View.GONE)
                    parent.setVisibility(View.VISIBLE);
                if (!videoShowing){
                    playing = true;
                    videoShowing = true;
                    handler.post(runnable);
                    if (listener != null) listener.onStart();
                }
                break;
            case ExoPlayer.STATE_ENDED:
                stop();
                break;
        }
    }
}
