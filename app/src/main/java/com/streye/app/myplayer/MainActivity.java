package com.streye.app.myplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

public class MainActivity extends GvrActivity {

    private GvrView view;
    private String url = Environment.getExternalStorageDirectory()+"/vid3.mp4";
    private SceneRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);
        view = (GvrView) findViewById(R.id.gvrView);
        renderer = new SceneRenderer(url);
        view.setRenderer(renderer);
        renderer.trigger();
        view.setVRModeEnabled(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams params = view.getLayoutParams();

            view.setVRModeEnabled(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            view.setVRModeEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        renderer.stop();
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        renderer.restart();
    }
}
