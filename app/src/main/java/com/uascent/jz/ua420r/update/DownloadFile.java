package com.uascent.jz.ua420r.update;


import com.uascent.jz.ua420r.utils.Lg;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by maxiao on 2017/8/5.
 */

public class DownloadFile {

    String url = "https://www.baidu.com/";
    OkHttpClient okHttpClient = new OkHttpClient();

    public void run() throws Exception {
        Request request = new Request.Builder()
                .url("http://mxgit.com/u-home.txt")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Lg.e("response="+response.toString());
            }

        });
    }
}