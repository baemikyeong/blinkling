package com.example.hyemin.blinkling;
/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hyemin.blinkling.camera.CameraSourcePreview;
import com.example.hyemin.blinkling.camera.GraphicOverlay;
import com.example.hyemin.blinkling.event.EyeClosedEvent;
import com.example.hyemin.blinkling.event.EyeOpenEvent;
import com.example.hyemin.blinkling.event.NeutralFaceEvent;
import com.example.hyemin.blinkling.util.PlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import static java.lang.Thread.sleep;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class Face_Activity extends Activity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private GraphicFaceTracker face_check;


    // 눈 감았을때의 오른쪽, 왼쪽 눈의 크기 저장
    public static float right_thred1 = 0.7f;
    public static float left_thred1 = 0.7f;

    // 초기화 여부 판단
    public static boolean initial_check;

    // 크기 저장을 하는 함수 호출을 위한 변수

    private int check = 0;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    public boolean eye_blink = false;


    private SharedPreferences intPref;
    private SharedPreferences.Editor editor1;
    private TextView tx1;

    long indivisual_blink_time;
    boolean startChecked = false;
    int starttimecheck = 100;
    long startTime = 0, endTime = 0;
    static int time = 1000;
    public int auto_start=1;
    Handler handler;
    Handler handler2;
    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);


        // tx1 = (TextView)findViewById(R.id.num);

        PlayServicesUtil.isPlayServicesAvailable(this, 69);

        // register the event bus
        EventBus.getDefault().register(this);

        initial_check = false;
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        indivisual_blink_time = 0;
        intPref = getSharedPreferences("mPred", Activity.MODE_PRIVATE);
        editor1 = intPref.edit();

       // checking = 0;
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
        auto_start = 2;

        handler = new Handler();

    /*    if(checking == 0) {
            Intent intent = new Intent(Face_Activity.this, Popup_Information_Activity.class);
            startActivity(intent);
            checking = 1;
        }*/
        Toast.makeText(this, "눈 크기 측정을 시작합니다. 눈을 감아주세요", Toast.LENGTH_SHORT).show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start_init();
            }
        },3000);


    }

    public void start_init(){
        try {
            onClickInit(getCurrentFocus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "눈 크기 측정을 완료했습니다. 3초 후 눈 깜빡임 시간을 측정합니다", Toast.LENGTH_SHORT).show();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    onClickInit_time(getCurrentFocus());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },3000);
      //  Toast.makeText(this, "눈 크기 측정이 완료 되었습니다. 의식적으로 눈을 깜박여 보세요.", Toast.LENGTH_SHORT).show();


    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEyeClosed(EyeClosedEvent e) {
        // change_up_location();
        if (starttimecheck == 1) {
            startTime = System.currentTimeMillis(); // 시간재기
            startChecked = true;
            starttimecheck++;
        }

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEyeOpened(EyeOpenEvent e) {

        if (starttimecheck == 2 && startChecked == true) {
            // 시간 멈추기
            endTime = System.currentTimeMillis();
            indivisual_blink_time = endTime - startTime;
            Toast.makeText(this, "시간" + (double)indivisual_blink_time/1000+"초", Toast.LENGTH_SHORT).show();
            editor1.putLong("time_blink", indivisual_blink_time);
            editor1.commit();
            starttimecheck++;
        }
        startChecked = false;
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
//                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(40.0f)
                .build();
    }

    public void onClickBack(View v) {

        Intent intent = new Intent(this, MainActivity.class);

        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }

        left_thred1 = 0.8f;
        right_thred1 = 0.8f;
        startActivity(intent);

    }

    public void onClickInit(View v) throws InterruptedException {
        check = 1;
        initial_check = true;
        face_check = new GraphicFaceTracker(mGraphicOverlay);
        Toast.makeText(this, "성공" + right_thred1 + "와" + left_thred1, Toast.LENGTH_SHORT).show();
        editor1.putFloat("LValue", left_thred1);
        editor1.putFloat("RValue", right_thred1);
        editor1.commit();

    }

    public void onClickInit_time(View v) throws InterruptedException {

        face_check.onDone();
        face_check = new GraphicFaceTracker(mGraphicOverlay);
        face_check.mFaceGraphic.set_closed_size((double) left_thred1, (double) right_thred1);
        starttimecheck = 1;
    }


    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
            startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {


        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            face_check = new GraphicFaceTracker(mGraphicOverlay);
            return face_check;
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);

            if (check == 1) {
                return_check();
            }
        }

        public void return_check() {


            double r, l;

            r = mFaceGraphic.return_right();
            l = mFaceGraphic.return_left();

            // 정확도를 위해 보다 작은 값으로 눈의 크기 저장
            if (right_thred1 != 0 && (float) r <= right_thred1 && r > (-1))
                right_thred1 = (float) r;
            if (left_thred1 != 0 && (float) l <= left_thred1 && l > (-1))
                left_thred1 = (float) l;

            if(right_thred1 <0.3f)
                right_thred1 = 0.3f;
            if(left_thred1<0.3f)
                left_thred1 = 0.3f;

            // 눈의크기가 저장이 되어있지 않은 경우, 비교 없이 값 자체 저장
            if (right_thred1 == 0)
                right_thred1 = (float) r;
            if (left_thred1 == 0)
                left_thred1 = (float) l;


        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {

            mFaceGraphic.setId(faceId);

        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {

            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }


    }
}
