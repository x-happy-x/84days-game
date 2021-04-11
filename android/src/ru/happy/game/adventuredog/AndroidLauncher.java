package ru.happy.game.adventuredog;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import ru.happy.game.adventuredog.Tools.ApplicationBundle;

public class AndroidLauncher extends AndroidApplication {
    VideoPlayerControl video;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loadPreferences();
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.r = config.g = config.b = config.a = 8;
        config.hideStatusBar = true;
        config.useImmersiveMode = true;
        config.numSamples = 2;
        //initialize(new MainGDX(new ApplicationBundle(view)), config);
        LinearLayout layout = findViewById(R.id.game);
        video = new VideoPlayerControl(this);
        layout.addView(initializeForView(new MainGDX(new ApplicationBundle(view, video)), config));
    }

    @Override
    protected void onPause() {
        super.onPause();
        video.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        video.play();
    }

    private View rootView;
    private AndroidView view;
    private int width, height;
    Rect rect;
    public void loadPreferences(){

        // Разрешение на доступ к памяти
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        // Определение типа дисплея
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            SharedPreferences preferences = getSharedPreferences("prefs",MODE_PRIVATE);
            SharedPreferences.Editor ed = preferences.edit();
            ed.putInt("cutoutMode",WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES);
            ed.apply();
            getWindow().getAttributes().layoutInDisplayCutoutMode = preferences.getInt("cutoutMode",1);
        }

        rect = new Rect();
        rootView = getWindow().getDecorView().getRootView();
        rootView.getWindowVisibleDisplayFrame(rect);
        height = rect.height();
        width = rect.width();
        view = new AndroidView(width,height);

        rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);
            if (!(width == rect.width() && height == rect.height())){
                width = rect.width();
                height = rect.height();
                view.onSizeChange(width, height); // Передаёт размеры в игру
            }
        });
    }
}
