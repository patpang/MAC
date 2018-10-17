package com.link.cloud.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AbsoluteSizeSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.link.cloud.MacApplication;
import com.link.cloud.R;
import com.link.cloud.api.ApiFactory;
import com.link.cloud.base.AppBarActivity;
import com.link.cloud.bean.People;
import com.link.cloud.utils.Utils;
import com.zitech.framework.data.network.response.ApiResponse;
import com.zitech.framework.data.network.subscribe.ProgressSubscriber;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

/**
 * Created by OFX002 on 2018/9/21.
 */

public class RegisterActivity extends AppBarActivity {
    @BindView(R.id.register_introduce_one)
    TextView registerIntroduceOne;
    @BindView(R.id.register_introduce_two)
    TextView registerIntroduceTwo;
    @BindView(R.id.register_introduce_three)
    TextView registerIntroduceThree;
    @BindView(R.id.register_introduce_five)
    TextView registerIntroduceFive;
    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.bind_way)
    TextView bindWay;
    @BindView(R.id.input_tel)
    EditText inputTel;
    @BindView(R.id.verify_code)
    EditText verifyCode;
    @BindView(R.id.send)
    TextView send;
    @BindView(R.id.bind_keypad_1)
    Button bindKeypad1;
    @BindView(R.id.bind_keypad_2)
    Button bindKeypad2;
    @BindView(R.id.bind_keypad_3)
    Button bindKeypad3;
    @BindView(R.id.bind_keypad_4)
    Button bindKeypad4;
    @BindView(R.id.bind_keypad_5)
    Button bindKeypad5;
    @BindView(R.id.bind_keypad_6)
    Button bindKeypad6;
    @BindView(R.id.bind_keypad_7)
    Button bindKeypad7;
    @BindView(R.id.bind_keypad_8)
    Button bindKeypad8;
    @BindView(R.id.bind_keypad_9)
    Button bindKeypad9;
    @BindView(R.id.bind_keypad_delect)
    Button bindKeypadDelect;
    @BindView(R.id.bind_keypad_0)
    Button bindKeypad0;
    @BindView(R.id.bind_keypad_ok)
    Button bindKeypadOk;
    @BindView(R.id.bind_way_one)
    LinearLayout bindWayOne;
    @BindView(R.id.bind_way_two)
    LinearLayout bindWayTwo;
    @BindView(R.id.code_intro)
    TextView codeIntro;
    @BindView(R.id.tel_intro)
    TextView telIntro;
    @BindView(R.id.custom_progress)
    CircleProgressBar customProgress;
    @BindView(R.id.bind_veune)
    TextView bindVeune;
    @BindView(R.id.bind_face)
    TextView bindFace;
    @BindView(R.id.choose_bind_way)
    LinearLayout chooseBindWay;
    @BindView(R.id.confirm_bind)
    TextView confirmBind;
    @BindView(R.id.bind_middle_one)
    RelativeLayout bindMiddleOne;
    @BindView(R.id.venue_image)
    CircleImageView venueImage;
    @BindView(R.id.bind_venue_intro)
    TextView bindVenueIntro;
    @BindView(R.id.bind_venue_intro_below)
    TextView bindVenueIntroBelow;
    @BindView(R.id.bind_middle_two)
    RelativeLayout bindMiddleTwo;
    @BindView(R.id.card_info_re)
    RelativeLayout cardInfoRe;
    @BindView(R.id.card_num)
    TextView cardNum;
    @BindView(R.id.bind_finish_info)
    TextView bindFinishInfo;
    @BindView(R.id.bind_middle_three)
    RelativeLayout bindMiddleThree;
    Realm realm;
    private ValueAnimator animator;
    boolean tel_input =true;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void initViews() {
        customProgress.setProgressFormatter(null);
        customProgress.setMax(100);
        registerIntroduceTwo.setTextColor(getResources().getColor(R.color.red));
        hideToolbar();
        realm=Realm.getDefaultInstance();
        setHintSize(inputTel,36,getResources().getString(R.string.please_input_tel));
        setHintSize(verifyCode,30,getResources().getString(R.string.please_input_verify));
        verifyCode.setShowSoftInputOnFocus(false);
        inputTel.setShowSoftInputOnFocus(false);
    }
    public void setHintSize(EditText editText,int size,String hint){
        String hintStr = hint;
        SpannableString ss =  new SpannableString(hintStr);
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(size, true);
        editText.setHintTextColor(getResources().getColor(R.color.dark_black));
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(new SpannedString(ss));

    }

    private void simulateProgress() {
        animator = ValueAnimator.ofInt(0, 100);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                if(customProgress!=null){
                    customProgress.setProgress(progress);
                    int state = MacApplication.getVenueUtils().getState();
                    if(state==3){
                        MacApplication.getVenueUtils().workModel();
                        animator.setCurrentPlayTime(0);
                    }
                    if(state==4){
                        bindVenueIntro.setText(getResources().getString(R.string.move_finger));
                    }
                    if(state!=4&&state!=3){
                        bindVenueIntro.setText(getResources().getString(R.string.right_finger));
                    }
                    if(progress==99){
                        finish();
                    }
                }
            }
        });
        animator.setDuration(40000);
        animator.start();
    }
    StringBuilder builder = new StringBuilder();
    StringBuilder verify = new StringBuilder();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_register;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.bind_keypad_0,R.id.bind_keypad_1,R.id.bind_keypad_2,R.id.bind_keypad_3,R.id.bind_keypad_4,R.id.bind_keypad_5,R.id.bind_keypad_6,R.id.bind_keypad_7,R.id.bind_keypad_8,
            R.id.bind_keypad_9,R.id.bind_keypad_ok,R.id.bind_keypad_delect,R.id.confirm_bind,R.id.bind_venue_intro,R.id.back,R.id.input_tel,R.id.verify_code,R.id.send})
    public void OnClick(View v){
        switch (v.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.bind_keypad_0:
            case R.id.bind_keypad_1:
            case R.id.bind_keypad_2:
            case R.id.bind_keypad_3:
            case R.id.bind_keypad_4:
            case R.id.bind_keypad_5:
            case R.id.bind_keypad_6:
            case R.id.bind_keypad_7:
            case R.id.bind_keypad_8:
            case R.id.bind_keypad_9:
                if(inputTel.isFocused()){
                    builder.append(((TextView)v).getText());
                    inputTel.setText(builder.toString());
                    inputTel.setSelection(builder.length());
                }else {
                    verify.append(((TextView)v).getText());
                    verifyCode.setText(verify.toString());
                    verifyCode.setSelection(verify.length());
                }
            break;
            case R.id.bind_keypad_ok:
                if(inputTel.isFocused()){
                    builder.delete(0,builder.length());
                    inputTel.setText(builder.toString());
                    inputTel.setSelection(builder.length());
                    setHintSize(inputTel,36,getResources().getString(R.string.please_input_tel));
                }else {
                    verify.delete(0,verify.length());
                    verifyCode.setText(verify.toString());
                    verifyCode.setSelection(verify.length());
                    setHintSize(verifyCode,30,getResources().getString(R.string.please_input_verify));
                }
                break;
            case R.id.bind_keypad_delect:
                    if(inputTel.isFocused()){
                        if(builder.length()>=1){
                            builder.deleteCharAt(builder.length() - 1);
                            inputTel.setText(builder.toString());
                            inputTel.setSelection(builder.length());
                        }
                    }else {
                        if(verify.length()>=1){
                            verify.deleteCharAt(verify.length() - 1);
                            verifyCode.setText(verify.toString());
                            verifyCode.setSelection(verify.length());
                    }
                }
                    break;
            case R.id.confirm_bind:
                bindMiddleOne.setVisibility(View.INVISIBLE);
                bindMiddleTwo.setVisibility(View.VISIBLE);
                String tel = inputTel.getText().toString();
                registerIntroduceThree.setTextColor(getResources().getColor(R.color.red));
                registerIntroduceTwo.setTextColor(getResources().getColor(R.color.text_register));
                bindWay.setText(getResources().getString(R.string.bind_veune));
                simulateProgress();
                break;
            case R.id.bind_venue_intro:
                bindMiddleTwo.setVisibility(View.INVISIBLE);
                bindMiddleThree.setVisibility(View.VISIBLE);
                registerIntroduceFive.setTextColor(getResources().getColor(R.color.red));
                registerIntroduceThree.setTextColor(getResources().getColor(R.color.text_register));
                bindWay.setText(getResources().getString(R.string.bind_finish));
                break;
            case R.id.send:
                String tel_num = inputTel.getText().toString();
                Utils.isPhoneNum(tel_num);
                ApiFactory.sendVCode(tel_num).subscribe(new ProgressSubscriber<ApiResponse>(this) {
                    @Override
                    public void onNext(ApiResponse apiResponse) {

                    }
                });

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
    @Override
    public void modelMsg(int state, String msg) {
        if(state==3){
            final People userBean = new People();
            userBean.setUid(System.currentTimeMillis()+"");
            userBean.setFeature(msg);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealm(userBean);
                }
            });
            bindMiddleTwo.setVisibility(View.INVISIBLE);
            bindMiddleThree.setVisibility(View.VISIBLE);
            registerIntroduceFive.setTextColor(getResources().getColor(R.color.red));
            registerIntroduceThree.setTextColor(getResources().getColor(R.color.text_register));
            bindWay.setText(getResources().getString(R.string.bind_finish));
            animator.cancel();
        }
        if(state==2){
            bindVenueIntro.setText(getResources().getString(R.string.same_finger));
        }
        if(state==1){
            bindVenueIntro.setText(getResources().getString(R.string.again_finger));
        }
    }

}
