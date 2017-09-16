package com.uascent.jz.ua420r.ConfigModule;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.uascent.jz.ua420r.CommonModule.GosBaseActivity;
import com.uascent.jz.ua420r.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by maxiao on 2017/7/13.
 */

public class GosAddDeviceListActivity extends GosBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        setActionBar(true, true, getString(R.string.add_devices));
        ButterKnife.bind(this);
    }

    @OnClick({R.id.ll_chaz, R.id.ll_lamp})
    public void onClick() {
        Intent intent = new Intent(GosAddDeviceListActivity.this, GosAirlinkChooseDeviceWorkWiFiActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
