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
public class Plane {

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
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTextureMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uTextureMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private static final String fragmentShaderCode =
//            "precision mediump float;" +
//                    "uniform vec4 vColor;" +
//                    "void main() {" +
//                    "  gl_FragColor = vColor;" +
//                    "}";
           "precision mediump float;\n" +
                   "varying vec2 vTextureCoord;\n" +
                   "uniform sampler2D sTexture;\n" +
                   "uniform vec4 vColor;\n" +
                   "\n" +
                   "void main() {\n" +
                   "    vec4 color = texture2D(sTexture, vec2(vTextureCoord.x,vTextureCoord.y));\n" +
                   "    vec4 color2 = vec4(vTextureCoord.x, vTextureCoord.y, 0.0,1.0);\n" +
                   "    gl_FragColor = color+color2;\n" +
                   "}\n";
    private static final String videoFragment = "#extension GL_OES_EGL_image_external : require\n" +
            "\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "\n" +
            "void main() {\n" +
            "    vec4 color = texture2D(sTexture, vTextureCoord);\n" +
            "    gl_FragColor = color;\n" +
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


    public Plane(boolean video) {
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
        int fragmentShader;
        checkGlError("initialize0");
        if (video){
            fragmentShader= loadShader(GLES20.GL_FRAGMENT_SHADER,
                    videoFragment);
        }else{
            fragmentShader= loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentShaderCode);
        }


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

        checkGlError("initialize6");

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

    public void draw(float[] mvpMatrix, int textureId, float[] mSTMatrix) {

        // get handles
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        texCoordHandle =  GLES20.glGetAttribLocation(program,"aTextureCoord");
        muSTMatrixHandle = GLES20.glGetUniformLocation(program, "uTextureMatrix");
        int i = GLES20.glGetUniformLocation(program, "sTexture");
        colorHandle = GLES20.glGetUniformLocation(program, "vColor");

        GLES20.glUseProgram(program);

        // prepare coordinates
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 5*4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        if(video && textureId!=-1) {
            GLES20.glBindTexture(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    textureId);
        } else{
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }
        GLES20.glUniform1i(i, 0);
//            Log.d("textura", ""+i + " "+textureId);

            vertexBuffer.position(3);
            GLES20.glVertexAttribPointer(texCoordHandle,
                    2, GLES20.GL_FLOAT, false, 5*4, vertexBuffer);
            GLES20.glEnableVertexAttribArray(texCoordHandle);

            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Set color for the plane
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // Draw the plane
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTICES_PER_PLANE);
//        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glFlush();
        checkGlError("draw");
    }


    public void setColor(float x, float y, float z){
        color[0] = x;
        color[1] = y;
        color[2] = z;
    }


    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("plane", op + ": glGetError: 0x" + Integer.toHexString(error));
            throw new RuntimeException("glGetError encountered (see log)");
        }
    }
}