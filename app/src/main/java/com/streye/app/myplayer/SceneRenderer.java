package com.streye.app.myplayer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.Manifest;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by alvaro on 5/7/16.
 */
public class SceneRenderer implements GvrView.StereoRenderer {

    private Context context;

    public Cube cube;

    private float[] camera = new float[16];
    private float[] view = new float[16];
    private float[] mvpMatrix = new float[16];
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;


    public SceneRenderer(String url) {
        this.cube = new Cube(url);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye
                0.0f, 0.0f, 0.01f, // center
                0.0f, 1.0f, 0.0f); // up
    }

    @Override
    public void onDrawEye(Eye eye) {
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        float [] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mvpMatrix, 0, perspective, 0, view, 0);

        GLES20.glClearColor(1.0f,0.0f,1.0f,1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        cube.draw(mvpMatrix);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        Log.d("width",""+ i);
        Log.d("height",""+ i1);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        cube.initialize();

    }

    @Override
    public void onRendererShutdown() {

    }
    public void trigger() {
        this.cube.randomizeColors();
    }

    public void stop(){
        cube.stop();
    }
    public void restart(){
        cube.restart();
    }
}
