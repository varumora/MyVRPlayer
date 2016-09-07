package com.streye.app.myplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.widget.ImageView;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by alvaro on 5/7/16.
 */
public class SceneRenderer implements GvrView.StereoRenderer, SurfaceTexture.OnFrameAvailableListener {

    private Context context;
    private String url;
    private MediaPlayer player;

    private Cube cube;
    private SphericalSceneRenderer sphere;
    private SurfaceTexture videoSurfaceTexture;
    private int textureId = -1;
    private boolean updateTexture=false;
    private float[] videoTextureMatrix = GLHelpers.createIdentityMtx();
    private float[] modelMatrix =  GLHelpers.createIdentityMtx();
    private float[] pvMatrix =  GLHelpers.createIdentityMtx();
    private float[] perspective =  GLHelpers.createIdentityMtx();

    private float[] camera =  GLHelpers.createIdentityMtx();
    private float[] view =  GLHelpers.createIdentityMtx();
    private float[] mvpMatrix =  GLHelpers.createIdentityMtx();
    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 1000.0f;

    public float[] rotation= {0f,0f};
    public float fov;

    private boolean video;
    private Bitmap bitmap;


    public SceneRenderer(Context context,String url) {
        this.url=url;
        this.context=context;
        Matrix.setIdentityM(view, 0);
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye
                0.0f, 0.0f, 0.01f, // center
                0.0f, 1.0f, 0.0f); // up
    }


    private float clamp(float valor, float min, float max){
        return Math.max(min, Math.min(max, valor));
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        float [] actual = new float[16];
        float [] rot1 = new float [16];
        float [] rot2 = new float [16];
        float inercia = 0.1f;

        Log.d("rotation", ""+rotation[0]+" "+rotation[1]);
        if(rotation[0]>0){
            rotation[0]=clamp(rotation[0]-inercia,0f,5f);
        }else if(rotation[0]<0){
            rotation[0]=clamp(rotation[0]+inercia,-5f,0f);
        }
        if(rotation[1]>0){
            rotation[1]=clamp(rotation[1]-inercia,0f,5f);
        }else if(rotation[1]<0){
            rotation[1]=clamp(rotation[1]+inercia,-5f,0f);
        }

        //giro sobre x
        Matrix.multiplyMM(actual,0, headTransform.getHeadView(),0,camera,0);

        Matrix.setRotateM(rot1, 0, rotation[0], actual[1],actual[5],actual[9]);
        Matrix.setRotateM(rot2, 0, rotation[1], 0,1,0);

        Matrix.multiplyMM(rot1,0,rot1,0,rot2,0);
        Matrix.multiplyMM(camera,0,camera,0,rot1,0);

//        Matrix.rotateM(camera, 0, rotation[0], actual[1], 0, actual[9]);
//        //giro sobre y
//        Matrix.rotateM(camera, 0, rotation[1], 0, 1, 0);

    }

    public void changeFov(float f){
        fov = f;
    }

    @Override
    public void onDrawEye(Eye eye) {

        if(fov!=0){
            float[] actual = {eye.getFov().getLeft(),eye.getFov().getRight(),eye.getFov().getTop(),eye.getFov().getBottom()};

            float horizontal = actual[0];
            float vertical = actual[2];
            float ratio = vertical/horizontal;
            horizontal+=fov;
            if(horizontal<50 && horizontal>10 ) {
                eye.getFov().setLeft(horizontal);
                eye.getFov().setRight(horizontal);
                eye.getFov().setTop(horizontal * ratio);
                eye.getFov().setBottom(horizontal * ratio);
                eye.setProjectionChanged();
            }
            fov=0;
        }

        if(video) {
            if (updateTexture) {
                videoSurfaceTexture.updateTexImage();
                updateTexture = false;
            }
            videoSurfaceTexture.getTransformMatrix(videoTextureMatrix);
        }

        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        perspective  = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(pvMatrix, 0, perspective, 0, view, 0);
        Matrix.multiplyMM(mvpMatrix, 0, pvMatrix, 0, modelMatrix, 0);


        GLES20.glClearColor(1.0f,0.0f,1.0f,1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);


//        cube.draw(mvpMatrix,textureId,videoTextureMatrix);
        sphere.onDrawFrame(
                textureId,
                videoTextureMatrix,
                mvpMatrix);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {

        GLES20.glViewport(0, 0, width, height);


        camera=new float[]{-1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,-1.0f,0.0f,
                0.0f,0.0f,0.0f,1.0f};

//        aux(perspective, 90, (float) width
//                / (float) height, 1f, 10f);
    }
//    public void aux(float[] m, float yFovInDegrees, float aspect,
//               float n, float f){
//        final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0);
//        final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0));
//
//        m[0] = a / aspect;
//        m[1] = 0f;
//        m[2] = 0f;
//        m[3] = 0f;
//
//        m[4] = 0f;
//        m[5] = a;
//        m[6] = 0f;
//        m[7] = 0f;
//
//        m[8] = 0f;
//        m[9] = 0f;
//        m[10] = -((f + n) / (f - n));
//        m[11] = -1f;
//
//        m[12] = 0f;
//        m[13] = 0f;
//        m[14] = -((2f * f * n) / (f - n));
//        m[15] = 0f;
//    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        if(url.endsWith(".mp4")) {
            prepareVideo(url);
            video = true;
        } else{
            preparePhoto(url);
            video = false;
        }

        Matrix.setRotateM(modelMatrix, 0, -90.0f, 1, 0, 0);
//        cube = new Cube();
//        cube.initialize();
        sphere = new SphericalSceneRenderer(context, video);
    }

    @Override
    public void onRendererShutdown() {

    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        updateTexture=true;
    }

    private Surface getVideoDecodeSurface() {

        textureId = GLHelpers.generateExternalTexture();
        videoSurfaceTexture = new SurfaceTexture(textureId);

        videoSurfaceTexture.setOnFrameAvailableListener(this);
        return new Surface(videoSurfaceTexture);
    }

    private void prepareVideo(String videoPath) {

        if (TextUtils.isEmpty(videoPath)) {
            throw new RuntimeException("Cannot begin playback: video path is empty");
        }

        try {
            player = new MediaPlayer();
            player.setSurface(getVideoDecodeSurface());
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(videoPath);
            player.setLooping(true);

            player.setOnPreparedListener(
                    new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            player.start();
                        }
                    });
            player.prepareAsync();
        } catch (IOException e) {
            Log.e("Error en MediaPlayer", e.toString(), e);
        }
    }

    private void preparePhoto(String photoPath){

        if (TextUtils.isEmpty(photoPath)) {
            throw new RuntimeException("Cannot begin playback: video path is empty");
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling
        bitmap = BitmapFactory.decodeFile(photoPath,options);
        textureId = GLHelpers.glGeneratePhotoTexture(bitmap);

    }

    public String matToString(float[] f){
        NumberFormat format = NumberFormat.getCurrencyInstance().getNumberInstance();
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        if(f.length!=16)
            return "Not 4x4 matrix";
        String s = "[ ";
        for (int i=0;i<4;i++){
            for (int j=0;j<4;j++){
                s=s+format.format(f[4*i+j]) +" ";
            }
            if (i<3) s = s+ "\n";
        }
        s = s+ "]";
        return s;
    }

    public void stop(){
        if (video) player.stop();
    }
    public void pause(){
        if (video) player.pause();
    }
    public void restart(){
        if (video) player.start();
    }
}