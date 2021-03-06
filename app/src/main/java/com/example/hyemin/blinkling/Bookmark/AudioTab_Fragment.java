package com.example.hyemin.blinkling.Bookmark;


import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.hyemin.blinkling.MainActivity;
import com.example.hyemin.blinkling.R;

import java.util.ArrayList;

import static com.example.hyemin.blinkling.Service.AudioService.mContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class AudioTab_Fragment extends Fragment {
    public static final String TAG = "BK";
    MainActivity mHostActivity;
    private Cursor mCursor;
    private CustomAdapter_audio mAdapter;
    private ListView mListView;
    private ExamDbFacade_audio mFacade;
    EditText editSearch;
    // Long click된 item의 index(position)을 기록한다.
    int selectedPos = -1;
    ArrayList<InfoClass_audio> insertResult;
    ArrayList<InfoClass_audio> selectResult;
    Spinner s;
    String book_title;
    String book_pos;
    int book_position;
    String audio_path;


    public AudioTab_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_audio_tab_, container, false);

        s = (Spinner) view.findViewById(R.id.spinner3);//스피너 설정

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0: {
                        mCursor = mFacade.getAll();
                        mAdapter.swapCursor(mCursor);
                        mListView.setAdapter(mAdapter);
                        break;
                    }
                    case 1: {
                        mCursor = mFacade.order_desc();
                        mAdapter.swapCursor(mCursor);
                        mListView.setAdapter(mAdapter);
                        break;
                    }
                    case 2: {
                        mCursor = mFacade.order_alp_asc();
                        mAdapter.swapCursor(mCursor);
                        mListView.setAdapter(mAdapter);
                        break;
                    }
                    case 3: {
                        mCursor = mFacade.order_doc_asc();
                        mAdapter.swapCursor(mCursor);
                        mListView.setAdapter(mAdapter);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //데이터베이스 생성 및 오픈
        mFacade = new ExamDbFacade_audio(getActivity());
        mAdapter = new CustomAdapter_audio(getActivity(), mFacade.getCursor(), false);
        mCursor = mFacade.getCursor();
        mListView = (ListView) view.findViewById(android.R.id.list);

        mListView.setAdapter(mAdapter);

        editSearch = (EditText) view.findViewById(R.id.search_audio);//검색창 설정
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //스와이핑해서 아이템 삭제하기
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(mListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    final int pos = position;

                                    mCursor.moveToPosition(pos);
                                    mFacade.delete(mCursor.getInt(mCursor.getColumnIndex(ExamDbContract_audio.ExamDbEntry.ID)));

                                }
                                mCursor = mFacade.getAll();
                                mAdapter.swapCursor(mCursor);
                                mListView.setAdapter(mAdapter);
                            }
                        });

        mListView.setOnTouchListener(touchListener);
        mListView.setOnScrollListener(touchListener.makeScrollListener());


        //리스트뷰로 db에 저장된 북마크들을 내보냄
     //   selectResult = mFacade.select();
        mAdapter.changeCursor(mFacade.getCursor());

        //롱클릭했을때 웹마크 이름 편집
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View v, int position, long arg3) {
                final int pos = position;

                mCursor.moveToPosition(pos);

                final EditText editText = new EditText(getActivity());
                AlertDialog.Builder alertDlg = new AlertDialog.Builder(v.getContext());
                alertDlg.setTitle(R.string.alert_title_question)
                        .setView(editText)
                        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newTitle = editText.getText().toString();//새롭게 설정할 북마크의 이름

                                if (newTitle != null) {
                              mFacade.editTitle(mCursor.getInt(mCursor.getColumnIndex(ExamDbContract_audio.ExamDbEntry.ID)), newTitle);

                                    mCursor = mFacade.getAll();

                                    mAdapter.swapCursor(mCursor);
                                    mListView.setAdapter(mAdapter);

                                    dialog.dismiss();
                                }
                            }
                        });

                // '아니오' 버튼이 클릭되면
                alertDlg.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();  // AlertDialog를 닫는다.
                    }
                });

                alertDlg.create().show();
                return true;

            }
        });

        // Inflate the layout for this fragment
        return view;
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //리스트의 아이템을 눌렀을 때 해당 웹페이지로 이동한다
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final int pos = position;

                mCursor.moveToPosition(pos);
                book_title = mCursor.getString(mCursor.getColumnIndex(ExamDbContract_audio.ExamDbEntry.DOCUMENT));
                // String book_pos = mCursor.getString(mCursor.getColumnIndex(ExamDbContract.ExamDbEntry.POS));
                book_pos = mCursor.getString(mCursor.getColumnIndexOrThrow(ExamDbContract_audio.ExamDbEntry.POS));

                audio_path = mCursor.getString(mCursor.getColumnIndexOrThrow(ExamDbContract_audio.ExamDbEntry.COLUMN_NAME_RECORDING_FILE_PATH));

                book_position = Integer.parseInt(book_pos);

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("음성메모 재생")
                        .setMessage("해당 페이지로 이동하여 음성메모를 재생하시겠습니까?")
                        .setPositiveButton("예",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        ((MainActivity) getActivity()).floatingBtnShow(audio_path);
                                        ((MainActivity) getActivity()).changeToText(book_title,book_position+88);


                                    }
                                }).setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                dialog.create().show();



            }
        });
    }
}