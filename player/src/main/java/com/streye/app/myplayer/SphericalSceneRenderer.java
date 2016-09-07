package com.streye.app.myplayer;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by alvaro on 8/7/16.
 */
public class SphericalSceneRenderer {
    public static final int SPHERE_SLICES = 180;
    private static final int SPHERE_INDICES_PER_VERTEX = 1;
    private static final float SPHERE_RADIUS = 100f;

    private ShaderProgram shaderProgram;

    private int aPositionLocation;
    private int uMVPMatrixLocation;
    private int uTextureMatrixLocation;
    private int aTextureCoordLocation;
    private int texHandle=-1;

    private Sphere sphere;

    private boolean video;

    public SphericalSceneRenderer(Context context, boolean video) {
        this.video=video;

        if (video) {
            shaderProgram = new ShaderProgram(
                    readRawTextFile(context, R.raw.video_vertex_shader),
                    readRawTextFile(context, R.raw.video_fragment_shader));
        } else {
            shaderProgram = new ShaderProgram(
                    readRawTextFile(context, R.raw.video_vertex_shader),
                    readRawTextFile(context, R.raw.photo_fragment_shader));
        }
        aPositionLocation = shaderProgram.getAttribute("aPosition");
        uMVPMatrixLocation = shaderProgram.getUniform("uMVPMatrix");
        if(video) {
            uTextureMatrixLocation = shaderProgram.getUniform("uTextureMatrix");
        } else{
            uTextureMatrixLocation = shaderProgram.getUniform("uTextureMatrix");
            texHandle = shaderProgram.getUniform("sTexture");
        }
        aTextureCoordLocation = shaderProgram.getAttribute("aTextureCoord");

//        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        sphere = new Sphere(SPHERE_SLICES, 0.f, 0.f, 0.f, SPHERE_RADIUS, SPHERE_INDICES_PER_VERTEX);

//        GLES20.glCullFace(GLES20.GL_BACK);

    }

    public void onDrawFrame(
            int textureId,
            float[] textureMatrix,
            float[] mvpMatrix) {


        GLES20.glUseProgram(shaderProgram.getShaderHandle());

        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLHelpers.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aPositionLocation, 3,
                GLES20.GL_FLOAT, false, sphere.getVerticesStride(), sphere.getVertices());

        GLHelpers.checkGlError("glVertexAttribPointer");

        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
        GLHelpers.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aTextureCoordLocation, 2,
                GLES20.GL_FLOAT, false, sphere.getVerticesStride(),
                sphere.getVertices().duplicate().position(3));

        GLHelpers.checkGlError("glVertexAttribPointer");


        if(video) {
            Matrix.translateM(textureMatrix, 0, 0, 1, 0);
            GLES20.glBindTexture(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    textureId);

        } else{
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(
                    GLES20.GL_TEXTURE_2D,
                    textureId);

            GLES20.glUniform1i(texHandle,0);
        }
        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, textureMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

//        Log.d("texHandle",""+texHandle);
//        Log.d("textureId",""+textureId);
//        Log.d("uTextureMatrixLocation",""+uTextureMatrixLocation);
//        Log.d("uMVPMatrixLocation",""+uMVPMatrixLocation);

        for (int j = 0; j < sphere.getNumIndices().length; ++j) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    sphere.getNumIndices()[j], GLES20.GL_UNSIGNED_SHORT,
                    sphere.getIndices()[j]);
        }
    }

    public void release() {
        shaderProgram.release();
    }


    public static String readRawTextFile(Context context, int resId) {
        InputStream is = context.getResources().openRawResource(resId);
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader buf = new BufferedReader(reader);
        StringBuilder text = new StringBuilder();
        try {
            String line;
            while ((line = buf.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

}
