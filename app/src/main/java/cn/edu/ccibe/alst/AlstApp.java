package cn.edu.ccibe.alst;

import android.app.Application;
import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;

public class AlstApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OCR.getInstance(this).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                //Log.d("GLog", "accessToken:" + accessToken);
            }

            @Override
            public void onError(OCRError ocrError) {
                Log.e("GLog", "OCRError:" + ocrError);
            }
        }, getApplicationContext());
    }
}
