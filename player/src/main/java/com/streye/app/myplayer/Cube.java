package com.streye.app.myplayer;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alvaro on 5/7/16.
 */

public class Cube {


    private boolean isInitialized;

    // PLANES
    public static final int FACES_PER_CUBE = 6;
    private Plane[] planes = new Plane[FACES_PER_CUBE];

    // TRANSFORMS
    private List<float[]> transforms = new ArrayList<>();

    // SCALE
    private static final int SIDE_LENGTH = 10;
    private static final float HALF_SIDE_LENGTH = SIDE_LENGTH / 2.0f;

    List<float[]> scales = new ArrayList<>();

    // TRANSLATIONS
    private static final float[] POSITION_AHEAD = {0, 0, HALF_SIDE_LENGTH};
    private static final float[] POSITION_RIGHT = {-HALF_SIDE_LENGTH, 0, 0};
    private static final float[] POSITION_BEHIND = {0, 0, -HALF_SIDE_LENGTH};
    private static final float[] POSITION_LEFT = {HALF_SIDE_LENGTH, 0, 0};
    private static final float[] POSITION_TOP = {0, HALF_SIDE_LENGTH, 0};
    private static final float[] POSITION_BOTTOM = {0, -HALF_SIDE_LENGTH, 0};

    List<float[]> translations = new ArrayList<>();

    // ROTATIONS
    private static final float[] ROTATION_AHEAD_FIRST = {90, 1, 0, 0};
    private static final float[] ROTATION_AHEAD_SECOND = {180, 0, 1, 0};

    private static final float[] ROTATION_RIGHT_FIRST = {-90, 0, 0, 1};
    private static final float[] ROTATION_RIGHT_SECOND = {90, 1, 0, 0};

    private static final float[] ROTATION_BEHIND_FIRST = {90, 1, 0, 0};

    private static final float[] ROTATION_LEFT_FIRST = {90, 0, 0, 1};
    private static final float[] ROTATION_LEFT_SECOND = {90, 1, 0, 0};

    private static final float[] ROTATION_TOP_FIRST = {-90, 0, 1, 0};
    private static final float[] ROTATION_TOP_SECOND = {180, 0, 0, 1};

    private static final float[] ROTATION_BOTTOM_FIRST = {-90, 0, 1, 0};

    List<float[]> rotations = new ArrayList<>();

    // FACE CONSTANTS
    public static final int FACE_AHEAD  = 0;
    public static final int FACE_RIGHT  = 1;
    public static final int FACE_BEHIND = 2;
    public static final int FACE_LEFT   = 3;
    public static final int FACE_TOP    = 4;
    public static final int FACE_BOTTOM = 5;

    public static final int[] FACES = {
            FACE_AHEAD,
            FACE_RIGHT,
            FACE_BEHIND,
            FACE_LEFT,
            FACE_TOP,
            FACE_BOTTOM
    };

    public Cube() {
        initializeTransforms();
        initializePlanes();
        isInitialized = false;
    }

    private void initializeTranslations() {
        translations.add(buildTranslationMatrix(POSITION_AHEAD));
        translations.add(buildTranslationMatrix(POSITION_RIGHT));
        translations.add(buildTranslationMatrix(POSITION_BEHIND));
        translations.add(buildTranslationMatrix(POSITION_LEFT));
        translations.add(buildTranslationMatrix(POSITION_TOP));
        translations.add(buildTranslationMatrix(POSITION_BOTTOM));
    }

    private void initializeRotations() {
        rotations.add(buildRotationMatrix(ROTATION_AHEAD_SECOND, ROTATION_AHEAD_FIRST));
        rotations.add(buildRotationMatrix(ROTATION_RIGHT_SECOND, ROTATION_RIGHT_FIRST));
        rotations.add(buildRotationMatrix(ROTATION_BEHIND_FIRST));
        rotations.add(buildRotationMatrix(ROTATION_LEFT_SECOND, ROTATION_LEFT_FIRST));
        rotations.add(buildRotationMatrix(ROTATION_TOP_SECOND, ROTATION_TOP_FIRST));
        rotations.add(buildRotationMatrix(ROTATION_BOTTOM_FIRST));
    }

    private void initializeScales() {
        scales.add(buildScaleMatrix(SIDE_LENGTH));
        scales.add(buildScaleMatrix(SIDE_LENGTH));
        scales.add(buildScaleMatrix(SIDE_LENGTH));
        scales.add(buildScaleMatrix(SIDE_LENGTH));
        scales.add(buildScaleMatrix(SIDE_LENGTH));
        scales.add(buildScaleMatrix(SIDE_LENGTH));
    }

    private void initializeTransforms() {
        initializeTranslations();
        initializeRotations();
        initializeScales();

        for (int i = 0; i < FACES_PER_CUBE; ++i) {
            transforms.add(computeTransform(translations.get(i), rotations.get(i), scales.get(i)));
        }
    }

    private void initializePlanes() {
        for (int i = 0; i < FACES_PER_CUBE; ++i) {
            if(i == 1)
                planes[i] = new Plane(false);
            else
                planes[i] = new Plane(false);
        }

    }

    public void initialize() {
        for (Plane plane : planes) {
            plane.initializeProgram();
        }
        planes[1].setColor(1.0f,1.0f,1.0f);

        isInitialized = true;
    };

    public void draw(float[] mvpMatrix, int textureId, float[] mSTMatrix) {
        if (!isInitialized)
            throw new RuntimeException("Cube not initialized!");
        float[] modelView = new float[16];
        for (int i = 0; i < FACES_PER_CUBE; ++i) {
            // transform mvpMatrix with the transform specific to this plane
            Matrix.multiplyMM(modelView, 0, mvpMatrix, 0, transforms.get(i), 0);
            planes[i].draw(modelView, textureId, mSTMatrix);
        }
    }

    private float[] computeTransform(float[] translationMatrix, float[] rotationMatrix, float[] scaleMatrix) {
        // R*S
        float[] rsMatrix = new float[16];
        Matrix.multiplyMM(rsMatrix, 0, rotationMatrix, 0, scaleMatrix, 0);

        // T*R*S
        float[] trsMatrix = new float[16];
        Matrix.multiplyMM(trsMatrix, 0, translationMatrix, 0, rsMatrix, 0);

        return trsMatrix;
    }

    private float[] buildTranslationMatrix(float[] translation) {
        float[] translationMatrix = new float[16];
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.translateM(translationMatrix, 0, translation[0], translation[1], translation[2]);
        return translationMatrix;
    }

    private float[] buildRotationMatrix(float[] rotation) {
        float[] rotationMatrix = new float[16];
        Matrix.setRotateM(rotationMatrix, 0, rotation[0], rotation[1], rotation[2], rotation[3]);
        return rotationMatrix;
    }

    private float[] buildRotationMatrix(float[] rotation_l, float[] rotation_r) {
        float[] L = buildRotationMatrix(rotation_l);
        float[] R = buildRotationMatrix(rotation_r);
        float[] rotationMatrix = new float[16];
        Matrix.multiplyMM(rotationMatrix, 0, L, 0, R, 0);
        return rotationMatrix;
    }

    private float[] buildScaleMatrix(float scale) {
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, scale, scale, scale);
        return scaleMatrix;
    }

    public void randomizeColors() {
        for (int i = 0; i < FACES_PER_CUBE; ++i) {
            planes[i].randomizeColor();
        }
    }


}