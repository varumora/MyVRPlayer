package com.streye.app.myplayer;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by alvaro on 8/7/16.
 */
public class GLHelpers {
    private static final String TAG = GLHelpers.class.getSimpleName();

    public static int generateExternalTexture() {
        int externalTextureId = -1;
        int[] textures = new int[1];
        try {
            GLES20.glGenTextures(1, textures, 0);
            externalTextureId = textures[0];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    externalTextureId);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString(), e);
            if (externalTextureId != -1) {
                GLES20.glDeleteTextures(1, textures, 0);
            }
            return -1;
        }
        return externalTextureId;
    }

    public static int glGeneratePhotoTexture(Bitmap bitmap){
        checkGlError("texture1");

        int externalTextureId = -1;
        int[] textures = new int[1];
        try {
            Log.d("tamaño",""+bitmap.getWidth()+"x"+bitmap.getHeight());
            GLES20.glGenTextures(1, textures, 0);
            externalTextureId = textures[0];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(
                    GLES20.GL_TEXTURE_2D,
                    externalTextureId);
            GLES20.glTexParameterf(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            checkGlError("texture2");

            if(bitmap!=null) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                bitmap.recycle();
            }else{
                Log.d("errorBitmap","bitmap null");
            }

        } catch (RuntimeException e) {
            Log.e(TAG, e.toString(), e);
            if (externalTextureId != -1) {
                GLES20.glDeleteTextures(1, textures, 0);
                Log.d("errorBitmap","textures deleted");
            }
            return -1;
        }
        return externalTextureId;

    }

    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error == GLES20.GL_NO_ERROR) {
            return;
        }
        String msg = op + ": glError 0x" + Integer.toHexString(error);
        Log.e(TAG, msg);
        throw new RuntimeException(msg);
    }

    public static float[] createIdentityMtx() {
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        return m;
    }
}
