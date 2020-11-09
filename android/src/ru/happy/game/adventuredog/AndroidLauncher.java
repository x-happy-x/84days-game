package ru.happy.game.adventuredog;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Tools.ApplicationBundle;

public class AndroidLauncher extends AndroidApplication {
    private View rootView;
    private AndroidView view;
    private int width, height;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.r = config.g = config.b = config.a = 8;
        config.hideStatusBar = true;
        config.useImmersiveMode = true;
        config.numSamples = 2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            SharedPreferences preferences = getSharedPreferences("prefs",MODE_PRIVATE);
            SharedPreferences.Editor ed = preferences.edit();
            ed.putInt("cutoutMode",WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES);
            ed.apply();
            getWindow().getAttributes().layoutInDisplayCutoutMode = preferences.getInt("cutoutMode",1);
        }
        rootView = getWindow().getDecorView().getRootView();
        Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);
        width = rect.width();
        height = rect.height();
        view = new AndroidView(width,height);
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                if (!(width == rect.width() && height == rect.height())){
                    width = rect.width();
                    height = rect.height();
                    view.onSizeChange(width,height);
                }
            }
        });
        initialize(new MainGDX(new ApplicationBundle(view)), config);
    }
}
