package com.streye.app.myplayer;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by alvaro on 5/7/16.
 */
public class Plane implements SurfaceTexture.OnFrameAvailableListener {

    private SurfaceTexture mSurface;
    private boolean updateTexture;
    private float[] mSTMatrix = new float[16];
    private int muSTMatrixHandle;
    private boolean video;


    private static final int COORDS_PER_VERTEX = 3;
    private static final int VERTICES_PER_PLANE = 4;


    private static final float[] VERTICES = {
            -0.5f, 0.0f, 0.5f,0f, 1f, // left front
            -0.5f, 0.0f, -0.5f, 0f, 0f,// left back
            0.5f, 0.0f, 0.5f, 1f, 1f,// right front
            0.5f, 0.0f, -0.5f, 1f, 0f// right back
    };

    private static final String vertexShaderCode =
//            "uniform mat4 uMVPMatrix;" +
//                    "attribute vec4 vPosition;" +
//                    "void main() {" +
//                    "  gl_Position = uMVPMatrix * vPosition;" +
//                    "}";
            "uniform mat4 uMVPMatrix;" +
                    "uniform mat4 uSTMatrix;\n" +
            "attribute vec4 vPosition;\n" +
            "attribute vec4 aTexCoord;\n" +
            "varying   vec2 vTexCoord;\n" +
            "void main() {\n" +
            "  gl_Position = uMVPMatrix * vPosition;\n" +
            "  vTexCoord   = (uSTMatrix * aTexCoord).xy;\n" +
            "}\n";

    private static final String fragmentShaderCode =
//            "precision mediump float;" +
//                    "uniform vec4 vColor;" +
//                    "void main() {" +
//                    "  gl_FragColor = vColor;" +
//                    "}";
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES uSampler;\n" +
            "uniform vec4 vColor;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
                    "if (vColor.x==1.0 && vColor.y ==1.0 && vColor.z == 1.0){\n"+
            "  gl_FragColor = texture2D(uSampler, vTexCoord);\n" +
                    "} else{\n" +
                    "  gl_FragColor = vColor;\n" +
                    "}" +
            "}\n";

    private FloatBuffer vertexBuffer;

    private int program;

    // Handles
    private int positionHandle;
    private int texCoordHandle;
    private int textureId=-1;
    private int mvpMatrixHandle;
    private int colorHandle;

    float color[] = new float[4];


    public Plane(MediaPlayer player,boolean video) {
        Matrix.setIdentityM(mSTMatrix, 0);
        randomizeColor();
        buildVertexBuffer();

        this.video=video;
    }

    public void randomizeColor() {
        color[0] = (float) Math.random();
        color[1] = (float) Math.random();
        color[2] = (float) Math.random();
//        color[0] = 1.0f;
//        color[1] = 0.0f;
//        color[2] = 1.0f;

        color[3] = 1.0f;
    }

    private void buildVertexBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(VERTICES_PER_PLANE * COORDS_PER_VERTEX * Float.SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(VERTICES);
        vertexBuffer.position(0);
    }

    public void initializeProgram() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        program = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(program, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(program, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(program);


        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }

        if(video) {

// generate one texture pointer and bind it as an external texture.
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            textureId = textures[0];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            checkGlError("bindtexture " + textureId);
// No mip-mapping with camera source.
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
// Clamp to edge is only option.
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        }

        colorHandle = GLES20.glGetUniformLocation(program, "vColor");
    }

    private static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void draw(float[] mvpMatrix) {

        synchronized(this) {
            if (updateTexture && video) {
                mSurface.updateTexImage();
                mSurface.getTransformMatrix(mSTMatrix);
                updateTexture = false;
            } else if(video){
            }
        }

        // get handles
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        texCoordHandle =  GLES20.glGetAttribLocation(program,"aTexCoord");
        muSTMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
        int i = GLES20.glGetUniformLocation(program, "uSampler");

        GLES20.glUseProgram(program);

        // prepare coordinates
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 5*4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        if(video && textureId!=-1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(i, 0);
//            Log.d("textura", ""+i + " "+textureId);

            vertexBuffer.position(3);
            GLES20.glVertexAttribPointer(texCoordHandle,
                    2, GLES20.GL_FLOAT, false, 5*4, vertexBuffer);
            GLES20.glEnableVertexAttribArray(texCoordHandle);

            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        }

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Set color for the plane
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // Draw the plane
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTICES_PER_PLANE);
//        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glFlush();
    }

    public Surface getSurface(){
        mSurface = new SurfaceTexture(textureId);
        mSurface.setOnFrameAvailableListener(this);
//        Log.d("textureID", ""+textureId);
        return new Surface(mSurface);
    }

    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if(video)
            updateTexture = true;
    }

    public void setColor(float x, float y, float z){
        color[0] = x;
        color[1] = y;
        color[2] = z;
    }


    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("VideoRender", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}