package com.link.cloud.activity;

import android.content.Intent;
import android.util.Log;

import com.link.cloud.R;
import com.link.cloud.User;
import com.link.cloud.api.ApiFactory;
import com.link.cloud.api.BaseProgressSubscriber;
import com.link.cloud.api.bean.DeviceBean;
import com.link.cloud.api.dataSourse.UserList;
import com.link.cloud.api.request.GetUserPages;
import com.link.cloud.base.AppBarActivity;
import com.link.cloud.bean.AllUser;
import com.link.cloud.bean.DeviceInfo;
import com.link.cloud.utils.TTSUtils;
import com.zitech.framework.data.network.response.ApiResponse;
import com.zitech.framework.data.network.subscribe.NoProgressSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by 49488 on 2018/10/20.
 */

public class SplashActivity extends AppBarActivity {

    private RealmResults allLocal;
    private DeviceInfo deviceInfo;

    @Override
    protected void initViews() {
        hideToolbar();
        getToken();
        TTSUtils.getInstance().speak("");
    }

    int pageNum = 100;
    int total = 0;
    int local = 0;

    private void getTotal() {
        GetUserPages getUserPages = new GetUserPages();
        getUserPages.setContent("HJKF");
        getUserPages.setPageNo(1);
        getUserPages.setPageSize(pageNum);
        ApiFactory.getUsers(getUserPages).subscribe(new BaseProgressSubscriber<ApiResponse<UserList>>(this) {
            @Override
            public void onStart() {
                super.onStart();
                dismissProgressDialog();
            }

            @Override
            public void onNext(ApiResponse<UserList> apiResponse) {
                final UserList userList = apiResponse.getData();
                total = userList.getTotal();
                Log.e("onNext: ", local + ">>>" + total);
                if (local != total) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            allLocal.deleteAllFromRealm();
                            realm.copyToRealm(userList.getData());
                        }
                    });

                }else {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }

            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                if (local != total) {
                    getAllData();
                }
            }
        });
    }

    private void getAllData() {
        int totalPage = total / pageNum + 1;
        ExecutorService executorService = Executors.newFixedThreadPool(totalPage);
        List<Future<Boolean>> futures = new ArrayList();
        if (totalPage >= 2) {
            for (int i = 2; i < totalPage; i++) {
                final int finalI = i;
                Callable<Boolean> task = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        GetUserPages getUserPages = new GetUserPages();
                        getUserPages.setContent(deviceInfo.getDeviceId());
                        getUserPages.setPageNo(finalI);
                        getUserPages.setPageSize(pageNum);
                        ApiFactory.getUsers(getUserPages).subscribe(new NoProgressSubscriber<ApiResponse<UserList>>(SplashActivity.this) {

                            @Override
                            public void onNext(ApiResponse<UserList> apiResponse) {
                                final UserList userList = apiResponse.getData();
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.copyToRealm(userList.getData());
                                    }
                                });
                            }
                        });
                        return true;
                    }
                };

                futures.add(executorService.submit(task));
            }
        }
        for (Future<Boolean> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    private void getToken() {
        RealmResults<DeviceInfo> all = realm.where(DeviceInfo.class).findAll();
        final RealmResults<AllUser> peopleIn = realm.where(AllUser.class).equalTo("isIn",1).findAll();
        for(int x=0;x<peopleIn.size();x++){
            final int finalX = x;
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    AllUser person = peopleIn.get(finalX);
                    person.setIsIn(0);
                }
            });
        }
        if (all.size() != 0) {
            deviceInfo = all.get(0);
            ApiFactory.appLogin(deviceInfo.getDeviceId().trim(), deviceInfo.getPsw()).subscribe(new BaseProgressSubscriber<ApiResponse<DeviceBean>>(this) {
                @Override
                public void onStart() {
                    super.onStart();
                    dismissProgressDialog();/**/
                }

                @Override
                public void onNext(ApiResponse<DeviceBean> response) {
                    super.onNext(response);
                    User.get().setToken(response.getData().getToken());
                    allLocal = realm.where(AllUser.class).findAll();
                    local = allLocal.size();
                    getTotal();

                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                    skipActivity(SettingActivity.class);
                }
            });
        } else {
            skipActivity(SettingActivity.class);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }
}
