package com.link.cloud.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.link.cloud.R;
import com.link.cloud.base.BaseActivity;
import com.link.cloud.fragment.Group_Lesson_Fragment;
import com.link.cloud.fragment.LessonChoose_Fragment;
import com.link.cloud.utils.DialogUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.member)
    TextView member;
    @BindView(R.id.manager)
    TextView manager;
    @BindView(R.id.choose_lesson)
    TextView chooseLesson;
    @BindView(R.id.lesson_in)
    TextView lessonIn;
    @BindView(R.id.choose_lesson_container)
    LinearLayout chooseLessonContainer;
    @BindView(R.id.fg_container)
    FrameLayout fgContainer;

    private LessonChoose_Fragment lessonChoose_fragment;
    private Group_Lesson_Fragment group_lesson_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    private  void initViews() {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        lessonChoose_fragment = new LessonChoose_Fragment();
        fragmentTransaction.replace(R.id.fg_container, lessonChoose_fragment);
        fragmentTransaction.commit();
    }


    @OnClick({R.id.member, R.id.manager, R.id.lesson_in, R.id.choose_lesson, R.id.buy, R.id.lesson_consume, R.id.register})
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.member:
                member.setBackground(getResources().getDrawable(R.drawable.border_red));
                manager.setBackground(null);
                member.setTextColor(getResources().getColor(R.color.almost_white));
                manager.setTextColor(getResources().getColor(R.color.text_gray));
                break;
            case R.id.manager:
                View view = View.inflate(MainActivity.this, R.layout.veune_dialog, null);
                DialogUtils.showManagerDialog(view, this);
                manager.setBackground(getResources().getDrawable(R.drawable.border_red));
                member.setBackground(null);
                member.setTextColor(getResources().getColor(R.color.text_gray));
                manager.setTextColor(getResources().getColor(R.color.almost_white));
                break;
            case R.id.lesson_in:
                lessonIn.setBackground(getResources().getDrawable(R.drawable.border_red_half_right));
                chooseLesson.setBackground(null);
                lessonIn.setTextColor(getResources().getColor(R.color.almost_white));
                chooseLesson.setTextColor(getResources().getColor(R.color.red));
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                if (group_lesson_fragment == null) {
                    group_lesson_fragment = new Group_Lesson_Fragment();
                }
                fragmentTransaction.replace(R.id.fg_container, group_lesson_fragment);
                fragmentTransaction.commit();
                break;
            case R.id.choose_lesson:
                chooseLesson.setBackground(getResources().getDrawable(R.drawable.border_red_half_left));
                lessonIn.setBackground(null);
                lessonIn.setTextColor(getResources().getColor(R.color.red));
                chooseLesson.setTextColor(getResources().getColor(R.color.almost_white));
                FragmentManager manager1 = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction1 = manager1.beginTransaction();
                fragmentTransaction1.replace(R.id.fg_container, lessonChoose_fragment);
                fragmentTransaction1.commit();
                break;

            case R.id.buy:
                showActivity(DemoActivity.class);
                break;
            case R.id.register:
                startActivity(new Intent(this,RegisterActivity.class));
                break;
            case R.id.lesson_consume:

                break;
        }
    }
}
