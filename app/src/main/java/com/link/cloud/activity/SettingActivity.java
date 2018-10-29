package com.link.cloud.activity;

import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.link.cloud.R;
import com.link.cloud.base.AppBarActivity;
import com.link.cloud.bean.DeviceInfo;
import com.link.cloud.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by OFX002 on 2018/9/28.
 */

public class SettingActivity extends AppBarActivity {
    @BindView(R.id.member)
    TextView member;
    @BindView(R.id.manager)
    TextView manager;
    @BindView(R.id.device_id)
    TextView deviceId;
    @BindView(R.id.psw_ll)
    LinearLayout pswLl;
    @BindView(R.id.open)
    TextView open;
    @BindView(R.id.close)
    TextView close;
    @BindView(R.id.open_or_close)
    LinearLayout openOrClose;
    @BindView(R.id.back_system_setting)
    TextView backSystemSetting;
    @BindView(R.id.back_app)
    TextView backApp;
    @BindView(R.id.back_system_main)
    TextView backSystemMain;
    @BindView(R.id.save)
    TextView save;
    @BindView(R.id.edit_psw)
    TextView edit_psw;
    Realm realm;
    private String mac;

    @Override
    protected void initViews() {
        hideToolbar();
        realm = Realm.getDefaultInstance();
        mac = Utils.getMac();
        deviceId.setText(getResources().getString(R.string.device_id) + mac);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @OnClick({R.id.back_app, R.id.member, R.id.save, R.id.back_system_main,
            R.id.back_system_setting, R.id.restart_app, R.id.close, R.id.open})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_app:
            case R.id.member:
                finish();
                break;
            case R.id.save:
                String edit_pswText = edit_psw.getText().toString();
                String fisrt = Utils.getMD5(edit_pswText).toUpperCase();
                final String second = Utils.getMD5(fisrt).toUpperCase();

                final RealmResults<DeviceInfo> all = realm.where(DeviceInfo.class).findAll();
                if (all.size() != 0) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            DeviceInfo deviceInfo = all.get(0);
                            deviceInfo.setPsw(second);
                        }
                    });
                } else {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            DeviceInfo deviceBean = new DeviceInfo();
                            deviceBean.setDeviceId(mac);
                            deviceBean.setPsw(second);
                            realm.copyToRealm(deviceBean);
                        }
                    });
                }
                Toast.makeText(this, getResources().getString(R.string.save_success), Toast.LENGTH_LONG).show();
                break;
            case R.id.back_system_main:
                Intent intent1 = new Intent(Intent.ACTION_MAIN, null);
                intent1.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent1);

                break;
            case R.id.back_system_setting:
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                break;
            case R.id.restart_app:
                Intent intent2 = new Intent(this, SplashActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
            case R.id.close:
                close.setTextColor(getResources().getColor(R.color.almost_white));
                close.setBackgroundResource(R.drawable.border_red_gradient);
                open.setBackgroundResource(R.drawable.border_gray_gradient);
                open.setTextColor(getResources().getColor(R.color.dark_black));
                break;
            case R.id.open:
                open.setTextColor(getResources().getColor(R.color.almost_white));
                open.setBackgroundResource(R.drawable.border_red_gradient);
                close.setBackgroundResource(R.drawable.border_gray_gradient);
                close.setTextColor(getResources().getColor(R.color.dark_black));
                break;
        }
    }


}
