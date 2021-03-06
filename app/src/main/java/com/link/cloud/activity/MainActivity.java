package com.link.cloud.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Power;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.link.cloud.Constants;
import com.link.cloud.MacApplication;
import com.link.cloud.R;
import com.link.cloud.User;
import com.link.cloud.adapter.GroupLesson_Adapter;
import com.link.cloud.adapter.IndicatorViewAdapter;
import com.link.cloud.api.ApiFactory;
import com.link.cloud.api.BaseProgressSubscriber;
import com.link.cloud.api.bean.LessonBean;
import com.link.cloud.api.bean.SingleUser;
import com.link.cloud.api.dataSourse.GroupLessonInResource;
import com.link.cloud.api.request.LessonPred;
import com.link.cloud.base.AppBarActivity;
import com.link.cloud.base.BaseActivity;
import com.link.cloud.bean.AllUser;
import com.link.cloud.bean.DeviceInfo;
import com.link.cloud.bean.GroupLessonUser;
import com.link.cloud.bean.MainFragment;
import com.link.cloud.gpiotest.Gpio;
import com.link.cloud.listener.DialogCancelListener;
import com.link.cloud.listener.FragmentListener;
import com.link.cloud.utils.DialogUtils;
import com.link.cloud.utils.NettyClientBootstrap;
import com.link.cloud.utils.TTSUtils;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.RecyclerIndicatorView;
import com.shizhefei.view.indicator.slidebar.SpringBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;
import com.zitech.framework.data.network.response.ApiResponse;
import com.zitech.framework.data.network.subscribe.NoProgressSubscriber;
import com.zitech.framework.data.network.subscribe.ProgressSubscriber;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import okhttp3.RequestBody;


public class MainActivity extends AppBarActivity implements DialogCancelListener, FragmentListener {

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
    @BindView(R.id.group_lesson_fragment)
    RelativeLayout group_lesson_fragment;
    @BindView(R.id.choose_lesson_fragment)
    RelativeLayout choose_lesson_fragment;
    @BindView(R.id.lesson_name)
    TextView lessonName;
    @BindView(R.id.coach_name)
    TextView coachName;
    @BindView(R.id.code_number)
    EditText code_mumber;
    private DialogUtils dialogUtils;
    ValueAnimator animator;
    private ViewPager viewPager;
    private RecyclerIndicatorView scrollIndicatorView;
    private IndicatorViewPager indicatorViewPager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private GroupLesson_Adapter loadMoreAdapter;
    private ArrayList<GroupLessonInResource> groupInList = new ArrayList<>();
    private ArrayList<GroupLessonUser> groupUsers = new ArrayList<>();
    private ArrayList<com.link.cloud.api.dataSourse.GroupLessonUser.UserInfosBean> groupInUserList = new ArrayList<>();
    RealmResults<GroupLessonUser> userBeans;
    private String courseReleasePkcode;
    NettyClientBootstrap nettyClientBootstrap;
    String gpiotext = "1067";
    public ArrayList<MainFragment> fragmentList = new ArrayList<>();
    private String deviceType;
    private RealmResults<AllUser> allCoachOrWorks;
    private ArrayList<AllUser> allCoachOrWorksList = new ArrayList<>();
    private LessonRecieve lesson;
    int postion ;
    private void getListDate(final int pos) {
        ApiFactory.courseList().subscribe(new BaseProgressSubscriber<ApiResponse<List<LessonBean>>>(MainActivity.this) {
            @Override
            public void onStart() {
                super.onStart();
                dismissProgressDialog();
            }

            @Override
            public void onNext(ApiResponse<List<LessonBean>> response) {
                if (!fragmentList.isEmpty()) {
                    fragmentList.clear();
                }

                for (LessonBean lessonBean : response.getData()) {
                    MainFragment mainFragment = new MainFragment();
                    mainFragment.setTip("    " + lessonBean.getDate().substring(5) + "    ");
                    mainFragment.setCourses(lessonBean.getCourses());
                    fragmentList.add(mainFragment);
                    indicatorViewPager.setAdapter(new IndicatorViewAdapter(MainActivity.this, MainActivity.this, fragmentList));
                    if (pos < response.getData().size()) {
                        indicatorViewPager.setCurrentItem(pos, false);
                    } else {
                        indicatorViewPager.setCurrentItem(0, false);
                    }
                }

            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
            }
        });

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void initData() {
        code_mumber.setFocusable(true);
        code_mumber.setCursorVisible(true);
        code_mumber.setFocusableInTouchMode(true);
        code_mumber.requestFocus();
        code_mumber.setShowSoftInputOnFocus(false);
        /**
         * EditText编辑框内容发生变化时的监听回调
         */
        code_mumber.addTextChangedListener(new EditTextChangeListener());
    }
    public class EditTextChangeListener implements TextWatcher {
        long lastTime;
        /**
         * 编辑框的内容发生改变之前的回调方法
         */
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        /**
         * 编辑框的内容正在发生改变时的回调方法 >>用户正在输入
         * 我们可以在这里实时地 通过搜索匹配用户的输入
         */
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        /**
         * 编辑框的内容改变以后,用户没有继续输入时 的回调方法
         */
        @Override
        public void afterTextChanged(Editable editable) {

            String str=code_mumber.getText().toString();
            if (str.contains("\n")) {
                if(!isLessonin){
                    code_mumber.setText("");
                    return;
                }
                if(System.currentTimeMillis()-lastTime<1500){
                    code_mumber.setText("");
                    return;
                }
                lastTime=System.currentTimeMillis();
                Log.e( "afterTextChanged: ", str);
                //  entranceContronller.openDoorQr(code_mumber.getText().toString());
                JSONObject object =null;
                try {
                    object= new JSONObject(code_mumber.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (object==null){
                    return;
                }
                RequestBody requestBody=RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),object.toString());
                ApiFactory.CourseInByQrcode(requestBody).subscribe(new BaseProgressSubscriber<ApiResponse>(MainActivity.this) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        onVeuenMsg(null,e.getMessage());
                    }

                    @Override
                    public void onNext(ApiResponse apiResponse) {
                        super.onNext(apiResponse);
                        handler.removeMessages(0);
                        onVeuenMsg(apiResponse.getCode(), apiResponse.getMessage());
                        getGroupData();
                    }

                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                    }
                });
                code_mumber.setText("");
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void initViews() {
        hideToolbar();
        dialogUtils = DialogUtils.getDialogUtils(this, this);
        userBeans = realm.where(GroupLessonUser.class).findAll();
        DeviceInfo deviceInfo = realm.where(DeviceInfo.class).findFirst();
        deviceType = deviceInfo.getDeviceType();
        userBeans.addChangeListener(new RealmChangeListener<RealmResults<GroupLessonUser>>() {
            @Override
            public void onChange(RealmResults<GroupLessonUser> fingerprintsBeans) {
                groupUsers.clear();
                groupUsers.addAll(realm.copyFromRealm(fingerprintsBeans));
            }
        });
        allCoachOrWorks = realm.where(AllUser.class).equalTo("userType", 2).or().equalTo("userType", 3).findAll();
        allCoachOrWorks.addChangeListener(new RealmChangeListener<RealmResults<AllUser>>() {
            @Override
            public void onChange(RealmResults<AllUser> allUsers) {
                allCoachOrWorksList.clear();
                allCoachOrWorksList.addAll(realm.copyFromRealm(allUsers));
            }
        });
        allCoachOrWorksList.addAll(realm.copyFromRealm(allCoachOrWorks));
        animator = ValueAnimator.ofInt(0, 80);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!isLessonin) {
                    return;
                }
                int state = MacApplication.getVenueUtils().getState();
                if (state == 3) {
                    final String uid = MacApplication.getVenueUtils().identifyNewImgUser(groupUsers);
                    if (uid != null) {
                        animator.end();
                        ApiFactory.admissionCourse(uid, courseReleasePkcode).subscribe(new BaseProgressSubscriber<ApiResponse>(MainActivity.this) {
                            @Override
                            public void onStart() {
                                super.onStart();
                                dismissProgressDialog();
                            }

                            @Override
                            public void onNext(ApiResponse apiResponse) {
                                super.onNext(apiResponse);
                                handler.removeMessages(0);
                                onVeuenMsg(uid, "");
                                getGroupData();
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                handler.removeMessages(0);
                                onVeuenMsg(null, e.getMessage());
                            }
                        });
                    } else {
                        final String workOrCoach = MacApplication.getVenueUtils().identifyNewImg(allCoachOrWorksList);
                        if(workOrCoach!=null){
                            openDoor();
                            handler.sendEmptyMessageDelayed(0,3000);
                        }else {
                            handler.sendEmptyMessageDelayed(0,3000);
                            View view = View.inflate(MainActivity.this, R.layout.verify_fail, null);
                            dialogUtils.showVeuneFailDialog(view, getResources().getString(R.string.please_confirm_bind));
                        }
                    }

                }
                if ((int) (animation.getAnimatedValue()) >= 79) {
                    animator.setCurrentPlayTime(0);
                }
            }
        });
        animator.setDuration(4000);
        ExecutorService service = Executors.newFixedThreadPool(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                nettyClientBootstrap = new NettyClientBootstrap(MainActivity.this, Constants.TCP_PORT, Constants.TCP_URL, "{\"data\":{},\"msgType\":\"HEART_BEAT\",\"token\":\"" + User.get().getToken() + "\"}");
                nettyClientBootstrap.start();
            }
        };
        service.execute(runnable);
        viewPager = (ViewPager) findViewById(R.id.viewPageView);
        scrollIndicatorView = (RecyclerIndicatorView) findViewById(R.id.titleIndicator);
        int selectColorId = getResources().getColor(R.color.almost_white);
        int unSelectColorId = getResources().getColor(R.color.dark_black);
        scrollIndicatorView.setOnTransitionListener(new OnTransitionTextListener().setColor(selectColorId, unSelectColorId));
        scrollIndicatorView.setScrollBar(new SpringBar(this, getResources().getColor(R.color.red)));
        indicatorViewPager = new IndicatorViewPager(scrollIndicatorView, viewPager);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getListDate(0);
        RegisteReciver();
        Gpio.gpioInt(gpiotext);
        if ("rk3399-mid".equals(deviceType)) {
            Gpio.set(gpiotext, 48);
        } else if ("rk3288".equals(deviceType)) {
            Power.set_zysj_gpio_value(4,0);
            //Power.set_zysj_gpio_value(4,1);
        }
        lesson = new LessonRecieve();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.LESSON);
        registerReceiver(lesson, intentFilter);
        initData();
    }
    public class LessonRecieve extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("msg");
            String type  =null;
            JSONObject object=null;
            Log.e( "onReceive: ",msg );
            Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
            try {
                object = new JSONObject(msg);
                type = object.getString("msgType");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if("REFRESH_COURSE_LIST".equals(type)){
             getListDate(postion);
            }else if("REFRESH_COURSE_USER".equals(type)){
                getGroupData();
            }else if("ENTRANCE_GUARD".equals(type)){
                try {
                    JSONObject data = object.getJSONObject("data");
                    String uuid = data.getString("uuid");
                    final RealmResults<AllUser> personIn = realm.where(AllUser.class).equalTo("uuid", uuid).findAll();
                    for(int x=0;x<personIn.size();x++){
                        final int finalX = x;
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                AllUser person = personIn.get(finalX);
                                person.setIsIn(1);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void initGroup() {
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));
        groupInList.clear();
        getGroupData();
        loadMoreAdapter = new GroupLesson_Adapter(groupInUserList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(loadMoreAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getGroupData();
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });
    }

    private void getGroupData() {
        ApiFactory.getRecentLesson().subscribe(new BaseProgressSubscriber<ApiResponse<ArrayList<GroupLessonInResource>>>(this) {
            @Override
            public void onStart() {
                super.onStart();
                dismissProgressDialog();
            }

            @Override
            public void onNext(ApiResponse<ArrayList<GroupLessonInResource>> arrayListApiResponse) {
                super.onNext(arrayListApiResponse);
                groupInList.addAll(arrayListApiResponse.getData());
                if (arrayListApiResponse.getData().size() > 0) {
                    coachName.setText(getResources().getString(R.string.now_coach) + arrayListApiResponse.getData().get(0).getStoreCoachName());
                    lessonName.setText(getResources().getString(R.string.now_lesson) + arrayListApiResponse.getData().get(0).getFitnessCourseName());
                    courseReleasePkcode = arrayListApiResponse.getData().get(0).getCourseReleasePkcode();
                    ApiFactory.getGroupUsers(courseReleasePkcode).subscribe(new BaseProgressSubscriber<ApiResponse<com.link.cloud.api.dataSourse.GroupLessonUser>>(MainActivity.this) {
                        @Override
                        public void onStart() {
                            super.onStart();
                            dismissProgressDialog();
                        }

                        @Override
                        public void onNext(final ApiResponse<com.link.cloud.api.dataSourse.GroupLessonUser> groupLessonUserApiResponse) {
                            super.onNext(groupLessonUserApiResponse);
                            groupInUserList.clear();
                            animator.start();
                            if (groupLessonUserApiResponse.getData().getUserInfos() != null) {
                                groupInUserList.addAll(groupLessonUserApiResponse.getData().getUserInfos());
                                loadMoreAdapter.notifyDataSetChanged();
                                final RealmResults<GroupLessonUser> groupLessonUsers = realm.where(GroupLessonUser.class).findAll();
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        groupLessonUsers.deleteAllFromRealm();
                                    }
                                });
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.copyToRealm(groupLessonUserApiResponse.getData().getFingerprints());
                                    }
                                });
                            }
                        }
                    });
                }

            }
        });
    }

    public void onVeuenMsg(String uid, String msg) {
        if (TextUtils.isEmpty(uid)) {
            View view = View.inflate(this, R.layout.verify_fail, null);
            dialogUtils.showVeuneFailDialog(view, msg);
        } else {
            View view = View.inflate(this, R.layout.verify_success, null);
            dialogUtils.showVeuneInOkDialog(view);
            openDoor();
        }
        handler.sendEmptyMessageDelayed(0, 3000);
    }

    private void openDoor() {
        if ("rk3399-mid".equals(deviceType)) {
            try {
                Gpio.gpioInt(gpiotext);
                Thread.sleep(400);
                Gpio.set(gpiotext, 48);
            TTSUtils.getInstance().speak("门已开");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Gpio.set(gpiotext, 49);
        } else if ("rk3288".equals(deviceType)) {
            try {
                Power.set_zysj_gpio_value(4,0);
                Thread.sleep(400);
           TTSUtils.getInstance().speak("门已开");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Power.set_zysj_gpio_value(4,1);
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.e("handleMessage: ", msg.what+"");
                    dialogUtils.dissMiss();
                    animator.start();
                    break;
                case 1:
                    if (dialogUtils.isShowing()) {
                        int state = MacApplication.getVenueUtils().getState();
                        if (state == 3) {
                            RealmResults<AllUser> all = realm.where(AllUser.class).equalTo("isadmin", 1).findAll();
                            final String uid = MacApplication.getVenueUtils().identifyNewImg(realm.copyFromRealm(all));
                            if (uid != null) {
                                showActivity(SettingActivity.class);
                                dialogUtils.dissMiss();
                                member.setBackground(getResources().getDrawable(R.drawable.border_red));
                                manager.setBackground(null);
                                member.setTextColor(getResources().getColor(R.color.almost_white));
                                manager.setTextColor(getResources().getColor(R.color.text_gray));
                            } else {
//                        ApiFactory.validateAdmin(uid).subscribe(new ProgressSubscriber<ApiResponse>(MainActivity.this) {
//                            @Override
//                            public void onNext(ApiResponse apiResponse) {
//                                super.onNext(apiResponse);
//                            }
//                        });}}
                                TTSUtils.getInstance().speak(getString(R.string.no_manager));
                            }
                        }
                        handler.sendEmptyMessageDelayed(1, 1000);
                    }
                    break;
            }

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        animator.end();
        isLessonin=false;
    }

    boolean isLessonin = false;

    protected void SendMsgToTcp(String msg) {
        nettyClientBootstrap.startNetty(msg);
        Log.e("SendMsgToTcp: ", msg);
    }

    @OnClick({R.id.member, R.id.manager, R.id.lesson_in, R.id.choose_lesson, R.id.buy, R.id.lesson_consume, R.id.register})
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.member:
                handler.removeMessages(1);
                member.setBackground(getResources().getDrawable(R.drawable.border_red));
                manager.setBackground(null);
                member.setTextColor(getResources().getColor(R.color.almost_white));
                manager.setTextColor(getResources().getColor(R.color.text_gray));
                break;
            case R.id.manager:
                animator.end();
                handler.removeMessages(0);
                handler.sendEmptyMessageDelayed(1, 1000);
                View view = View.inflate(MainActivity.this, R.layout.veune_dialog, null);
                dialogUtils.showManagerDialog(view);
                manager.setBackground(getResources().getDrawable(R.drawable.border_red));
                member.setBackground(null);
                member.setTextColor(getResources().getColor(R.color.text_gray));
                manager.setTextColor(getResources().getColor(R.color.almost_white));
                break;
            case R.id.lesson_in:
                isLessonin = true;
                animator.start();
                lessonIn.setBackground(getResources().getDrawable(R.drawable.border_red_half_right));
                chooseLesson.setBackground(null);
                lessonIn.setTextColor(getResources().getColor(R.color.almost_white));
                chooseLesson.setTextColor(getResources().getColor(R.color.red));
                group_lesson_fragment.setVisibility(View.VISIBLE);
                choose_lesson_fragment.setVisibility(View.GONE);
                initGroup();
                break;
            case R.id.choose_lesson:
                isLessonin = false;
                animator.end();
                getListDate(0);
                chooseLesson.setBackground(getResources().getDrawable(R.drawable.border_red_half_left));
                lessonIn.setBackground(null);
                lessonIn.setTextColor(getResources().getColor(R.color.red));
                chooseLesson.setTextColor(getResources().getColor(R.color.almost_white));
                choose_lesson_fragment.setVisibility(View.VISIBLE);
                group_lesson_fragment.setVisibility(View.GONE);
                break;

            case R.id.buy:
                isLessonin = false;
                showActivity(FunctionalSelectionActivity.class);
                break;
            case R.id.register:
                isLessonin = false;
                showActivity(RegisterActivity.class);
                break;
            case R.id.lesson_consume:
                isLessonin = false;
                showActivity(PrivateEducationConsumActivity.class);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        unRegisterReceiver();
    }

    @Override
    public void dialogCancel() {
        member.setBackground(getResources().getDrawable(R.drawable.border_red));
        manager.setBackground(null);
        member.setTextColor(getResources().getColor(R.color.almost_white));
        manager.setTextColor(getResources().getColor(R.color.text_gray));
        handler.removeMessages(1);
        animator.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (group_lesson_fragment.getVisibility() == View.VISIBLE) {
            Log.e("onResume: ", group_lesson_fragment.isShown() + "<<<<<");
            animator.start();
            isLessonin = true;
        }
    }

    @Override
    public void onVenuePay() {

    }

    @Override
    public void OnRefreshListener(int pos) {
        getListDate(pos);
        this.postion =pos;
    }
}
