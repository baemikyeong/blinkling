package com.example.hyemin.blinkling.Setting;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hyemin.blinkling.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetFontFragment extends DialogFragment {

    private SharedPreferences intPref;
    private SharedPreferences.Editor editor1;

    public SetFontFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        intPref = this.getActivity().getSharedPreferences("mPred", Activity.MODE_PRIVATE);
        editor1 = intPref.edit();

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("글꼴 조정");
        builder.setMessage("문서를 볼 때의 글꼴을 지정합니다.");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_set_font, null);
        final RadioGroup rg = (RadioGroup)view.findViewById(R.id.radiofont);

        final int[] nCurrent = {intPref.getInt("font_edit", 1)};
        switch (nCurrent[0]){
            case 1:
                rg.check(R.id.radio_font1);
                break;
            case 2:
                rg.check(R.id.radio_font2);
                break;
            case 3:
                rg.check(R.id.radio_font3);
                break;
            case 4:
                rg.check(R.id.radio_font4);
                break;
            case 5:
                rg.check(R.id.radio_font4);
                break;
            case 6:
                rg.check(R.id.radio_font4);
                break;
        }

        builder.setView(view)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if(rg.getCheckedRadioButtonId() == R.id.radio_font1)
                            nCurrent[0] = 1;
                        else if(rg.getCheckedRadioButtonId() == R.id.radio_font2)
                            nCurrent[0] = 2;
                        else if(rg.getCheckedRadioButtonId() == R.id.radio_font3)
                            nCurrent[0] = 3;
                        else if(rg.getCheckedRadioButtonId() == R.id.radio_font4)
                            nCurrent[0] = 4;
                        else if(rg.getCheckedRadioButtonId() == R.id.radio_font5)
                            nCurrent[0] = 5;
                        else
                            nCurrent[0] = 6;

                        editor1.putInt("font_edit", nCurrent[0]);
                        editor1.commit();

                    }

                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked Cancel so do some stuff */
                    }
                });

        return builder.create();
    }
}
