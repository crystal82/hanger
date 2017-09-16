package com.uascent.jz.ua420r.ConfigModule;

import com.uascent.jz.ua420r.DeviceModule.GosMainActivity;
import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.CommonModule.GosDeploy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GosConfigFailedActivity extends GosConfigModuleBaseActivity implements OnClickListener {

    /**
     * The btn Again
     */
    Button btnAgain;

    /**
     * The soft SSID
     */
    String softSSID;

    /**
     * The data
     */
    String promptText, cancelBesureText, beSureText, cancelText;

    TextView msg;

    LinearLayout ll_msg1, ll_msg2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gos_config_failed);
        // 设置ActionBar
        setActionBar(true, true, R.string.join_failed_title);

        initView();
        initEvent();
        initData();
    }

    private void initView() {
        msg = (TextView) findViewById(R.id.msg);
        btnAgain = (Button) findViewById(R.id.btnAgain);
        ll_msg1 = (LinearLayout) findViewById(R.id.ll_msg1);
        ll_msg2 = (LinearLayout) findViewById(R.id.ll_msg2);
        String txt_msg = getIntent().getStringExtra("msg");
        if (!TextUtils.isEmpty(txt_msg)) {
            msg.setText(txt_msg);
            if (txt_msg.equals(getString(R.string.set_net_error))) {
                ll_msg1.setVisibility(View.GONE);
                ll_msg2.setVisibility(View.VISIBLE);
            } else {
                ll_msg1.setVisibility(View.VISIBLE);
                ll_msg2.setVisibility(View.GONE);
            }
        }
        // 配置文件部署
        btnAgain.setBackgroundDrawable(GosDeploy.setButtonBackgroundColor());
        btnAgain.setTextColor(GosDeploy.setButtonTextColor());
    }

    private void initEvent() {
        btnAgain.setOnClickListener(this);
    }

    private void initData() {

        promptText = (String) getText(R.string.prompt);
        cancelBesureText = (String) getText(R.string.cancel_besure);
        beSureText = (String) getText(R.string.besure);
        cancelText = (String) getText(R.string.no);
    }

    // 屏蔽掉返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            quitAlert(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case android.R.id.home:
                quitAlert(this);
                break;

            default:
                break;
        }

        return true;
    }

    public void onBack() {
        Intent intent = new Intent(
                GosConfigFailedActivity.this,
                GosMainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAgain:
                if (getIntent().getStringExtra("mode").equals("smartlink")) {
                    Intent intent = new Intent(
                            GosConfigFailedActivity.this,
                            GosAirlinkConfigCountdownActivity.class);

                    startActivity(intent);
                } else {
                    Intent intent = new Intent(
                            GosConfigFailedActivity.this,
                            GosConfigCountdownActivity.class);
                    startActivity(intent);
                }
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                finish();
                break;

            default:
                break;
        }
    }
}
