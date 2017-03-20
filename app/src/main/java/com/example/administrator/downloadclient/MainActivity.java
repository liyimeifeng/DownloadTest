package com.example.administrator.downloadclient;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

public class MainActivity extends AppCompatActivity implements ProgressResponseBody.ProgressListener {

    public static final String TAG = "MainActivity";
    public static final String PACKAGE_URL = "http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk";
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    private long breakPoints;
    private ProgressDownloader downloader;
    private File file;
    private long totalBytes;
    private long contentLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.downloadButton, R.id.pause_button, R.id.continue_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.downloadButton:
                // 新下载前清空断点信息
                breakPoints = 0L;
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sample.apk");
                downloader = new ProgressDownloader(PACKAGE_URL, file, this);
                downloader.download(0L);
                break;
            case R.id.pause_button:
                downloader.pause();
                Toast.makeText(this, "下载暂停", Toast.LENGTH_SHORT).show();
                // 存储此时的totalBytes，即断点位置。
                breakPoints = totalBytes;
                break;
            case R.id.continue_button:
                downloader.download(breakPoints);
                break;
        }
    }

    @Override
    public void onPreExecute(long contentLength) {
        // 文件总长只需记录一次，要注意断点续传后的contentLength只是剩余部分的长度
        if (this.contentLength == 0L) {
            this.contentLength = contentLength;
            progressBar.setMax((int) (contentLength / 1024));
        }
    }

    @Override
    public void update(long totalBytes, boolean done) {
        // 注意加上断点的长度
        this.totalBytes = totalBytes + breakPoints;
        progressBar.setProgress((int) (totalBytes + breakPoints) / 1024);
        if (done) {
            // 切换到主线程
            Observable
                    .empty()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .subscribe();
        }
    }
}
//public class MainActivity extends AppCompatActivity {
//    private Button button;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
//
//        button = (Button)findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        int result=new FileDownloader().downloadFile("test/", "001.txt", "http://192.168.0.100:8080/dodownload?fname=test.txt");
//                        Log.i( "=======","result="+result);
//                    }
//                }).start();
//            }
//        });
//
//
//       final RequestManager manager = new RequestManager(this);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String url = "http://192.168.0.100:8080/dodownload?fname=test.txt";
//                String filePath= Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator ;
//                manager.downLoadFile(url, filePath, new ReqCallBack<Object>() {
//                    @Override
//                    public void onReqFailed(String errorMsg) {
//                        Log.i("================", "onReqFailed:====下载失败 ");
//                    }
//
//                    @Override
//                    public void onReqSuccess(Object result) {
//                        Log.i("================", "onReqSuccess: ========下载成功");
//                    }
//                });
//            }
//        }).start();
//
//
//    }
//
//
//}
