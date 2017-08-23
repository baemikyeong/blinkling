package com.example.hyemin.blinkling;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hyemin.blinkling.event.NeutralFaceEvent;
import com.example.hyemin.blinkling.event.RightEyeClosedEvent;
import com.example.hyemin.blinkling.tracker.FaceTracker;
import com.example.hyemin.blinkling.util.PlayServicesUtil;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.google.android.gms.wearable.DataMap.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class TextViewFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERM = 69;      // 카메라 퍼미션을 위한 코드
    private TextView tv;                              // 텍스트 뷰를 띄워줄 뷰
    private HorizontalScrollView horizon_scrollView;
    private ScrollView scrollView;                          // 텍스트 뷰를 스크롤 뷰를 이용해 화면에 출력
    private ViewPager viewpager;                            // 텍스트 뷰를 뷰페이저를 이용해 화면에 출력
    private int[] location = new int[2];                    // 사용자가 현재 보고 있는 화면의 위치 저장
    private FaceDetector mFaceDetector;                     // 얼굴 인식
    private CameraSource mCameraSource;                     // 카메라 객체
    private FaceTracker face_tracker;                       // 눈 파악
    private double left_thres = 0;                          // 사용자의 초기값
    private double right_thres = 0;
    private SharedPreferences bookmarkPref;
    private SharedPreferences.Editor bookEdit;
    private int book_mark;
    private String bookName = "";
    private SharedPreferences intPref;
    private SharedPreferences.Editor editor1;
    private WindowManager.LayoutParams params;
    private float brightness; // 밝기값은 float형으로 저장되어 있습니다.
    View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            bookName = getArguments().getString("bookname");
        }
    }


    public TextViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_text_scrollview, container, false);

        Activity root = getActivity();
//        Toast toast = Toast.makeText(root, bookName, Toast.LENGTH_SHORT);
//         toast.show();

        PlayServicesUtil.isPlayServicesAvailable(getActivity(), 69);

        intPref = this.getActivity().getSharedPreferences("mPred", Activity.MODE_PRIVATE);//이거
        editor1 = intPref.edit();

        int bgcolor = intPref.getInt("background", 0);
        int font = intPref.getInt("font_edit", 0);

        Typeface typeFace;
        tv = (TextView) rootView.findViewById(R.id.txtview);

        int value = intPref.getInt("brightness_gauge",5);
        float bright_value = (float)value/10;

        //원래 밝기 저장
        params = getActivity().getWindow().getAttributes();
        brightness = params.screenBrightness;
        // 최대 밝기로 설정
        params.screenBrightness = bright_value;
        // 밝기 설정 적용
        getActivity().getWindow().setAttributes(params);

        switch (bgcolor % 10) {
            case 0: // 연한 베이지
                tv.setBackgroundColor(Color.rgb(245, 241, 222));
                rootView.setBackgroundColor(Color.rgb(245, 241, 222));
                break;
            case 1: // 연한 그레이
                tv.setBackgroundColor(Color.rgb(204, 204, 204));
                rootView.setBackgroundColor(Color.rgb(204, 204, 204));
                break;
            case 2: // 흰색
                tv.setBackgroundColor(Color.rgb(255, 255, 255));
                rootView.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
            case 3: // 검정색
                tv.setBackgroundColor(Color.rgb(0, 0, 0));
                rootView.setBackgroundColor(Color.rgb(0, 0, 0));
                break;
        }


        switch (font % 10) {
            case 9: // 나눔바른고딕
                typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumBarunGothic.otf");
                tv.setTypeface(typeFace);
                break;
            case 0: // 나눔손글씨
                typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumPen.otf");
                tv.setTypeface(typeFace);
                break;
            case 1: // 나눔바른펜
                typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumBarunpenRegular.otf");
                tv.setTypeface(typeFace);
                break;
            case 2: // 나눔명조체
                typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumMyeongjoBold.otf");
                tv.setTypeface(typeFace);
                break;
            case 3: // 나눔스퀘어체
                typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumSquareOTFRegular.otf");
                tv.setTypeface(typeFace);
                break;
            case 4: // 나눔손글씨붓
                typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumBrush.otf");
                tv.setTypeface(typeFace);
                break;
        }

        if (isCameraPermissionGranted()) {
            // ...create the camera resource
            createCameraResources();
        } else {
            // ...else request the camera permission
            requestCameraPermission();
        }

        if (1 != 1) { // 뷰페이저와 스크롤 뷰 구분
            viewpager = (ViewPager) rootView.findViewById(R.id.viewpager_text);
            Toast.makeText(getActivity(), rootView.getHeight()+"임", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), tv.getHeight()+"킴", Toast.LENGTH_SHORT).show();
        } else {
            scrollView = (ScrollView) rootView.findViewById(R.id.scroll_text);
            // 사용자가 화면을 터치하여 스크롤 뷰의 위치 변경시, 체크
            scrollView.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    tv.getLocationOnScreen(location);
                    return false;
                }
            });
        }
        readTxt();

        return rootView;
    }

    private void readTxt() {

        File dir = Environment.getExternalStorageDirectory().getAbsoluteFile();
        //File yourFile = new File(dir, "path/to/the/file/inside/the/sdcard.ext");

        //Get the text file
        //Intent intent = getIntent();


        //  String bookName = getArguments().getString("bookname");
        //Toast.makeText(this, textName, Toast.LENGTH_SHORT).show();
        File file = new File(dir, bookName);
        // i have kept text.txt in the sd-card

        if (file.exists())   // check if file exist
        {
            //Read text from file
            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }
            //Set the text
            tv.setText(text);
        } else {
            tv.setText("Sorry file doesn't exist!!");
        }

    }

    //눈깜박임에 따른 페이지 down 함수
    public void change_down_location() {
        // 절대값을 통해 text뷰의 스크롤뷰에서의 위치 파악
        if (location[1] < 0)
            location[1] = (-1) * location[1];

        // 위치 변경
        scrollView.scrollTo(0, location[1] + 60);
        location[1] += 60;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    // 눈깜박임에 따른 페이지 up 함수
    public void change_up_location() {
        // 절대값을 통해 text뷰의 스크롤뷰에서의 위치 파악
        if (location[1] < 0)
            location[1] = (-1) * location[1];
        // 기존의 위치에서 60 이동
        scrollView.scrollTo(0, location[1] - 60);
        location[1] -= 60;
    }

    /**
     * Check camera permission
     *
     * @return <code>true</code> if granted
     */
    private boolean isCameraPermissionGranted() {
        return ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request the camera permission
     */
    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_CAMERA_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createCameraResources();
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getActivity().finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity().getApplicationContext());
        builder.setTitle("EyeControl")
                .setMessage("No camera permission")
                .setPositiveButton("Ok", listener)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (MainActivity.bottomNavigation.getSelectedItemId() != R.id.navigation_home)
            MainActivity.bottomNavigation.getMenu().findItem(R.id.navigation_home).setChecked(true);

        // register the event bus
        EventBus.getDefault().register(this);

        // start the camera feed
        if (mCameraSource != null && isCameraPermissionGranted()) {
            try {
                //noinspection MissingPermission
                mCameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //   Log.e(TAG, "onResume: Camera.start() error");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //밝기 다시 원래 값으로 변경
        params.screenBrightness = brightness;
        getActivity().getWindow().setAttributes(params);

        // unregister from the event bus
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        // stop the camera source
        if (mCameraSource != null) {
            mCameraSource.stop();
        } else {
            //    Log.e(TAG, "onPause: Camera.stop() error");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // release them all...
        if (mFaceDetector != null) {
            mFaceDetector.release();
        } else {
            // Log.e(TAG, "onDestroy: FaceDetector.release() error");
        }
        if (mCameraSource != null) {
            mCameraSource.release();
        } else {
            // Log.e(TAG, "onDestroy: Camera.release() error");
        }
    }

    /*   @Subscribe(threadMode = ThreadMode.MAIN)
       public void onLeftEyeClosed(LeftEyeClosedEvent e) {
           change_down_location();
       }
   */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRightEyeClosed(RightEyeClosedEvent e) {
        // change_up_location();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNeutralFace(NeutralFaceEvent e) {

        change_down_location();

    }

    private void createCameraResources() {
        Context context = getActivity().getApplicationContext();

        // create and setup the face detector
        mFaceDetector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
                .build();

        // now that we've got a detector, create a processor pipeline to receive the detection
        // results
        mFaceDetector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
                .build();

        // now that we've got a detector, create a processor pipeline to receive the detection
        // results

        mFaceDetector.setProcessor(new LargestFaceFocusingProcessor(mFaceDetector, face_tracker = new FaceTracker()));

        // operational...?
        if (!mFaceDetector.isOperational()) {
            //  Log.w(TAG, "createCameraResources: detector NOT operational");
        } else {
            //   Log.d(TAG, "createCameraResources: detector operational");
        }

        // Create camera source that will capture video frames
        // Use the front camera
        mCameraSource = new CameraSource.Builder(getActivity().getApplicationContext(), mFaceDetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30f)
                .build();
    }

}
