package cool.ldr.pnet.utils;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;

import com.blankj.utilcode.util.LogUtils;

import java.util.Collections;

import cool.ldr.pnet.views.tastytoast.SimToast;

public class WebUtils {


    public static void openWeb(Context mContext, String url) {

        String packageName = CustomTabsClient.getPackageName(
                mContext,
                Collections.emptyList()
        );
        LogUtils.i(packageName);
        if (packageName == null) {
            SimToast.toastEL("建议安装 edge 或者 chrome 浏览器\n并设置为默认浏览器，会有更好的体验～");
            openBrowser(url);
        } else {

            new CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(mContext, Uri.parse(url));
        }
//        CustomTabColorSchemeParams colorSchemeParams = new CustomTabColorSchemeParams.Builder()
//                .setToolbarColor(ContextCompat.getColor(mContext, getThemeColor()))
//                .setSecondaryToolbarColor(ContextCompat.getColor(mContext, getAccentThemeColor()))
//                .build();
    }

    public static void openBrowser(String targetUrl) {
        if (TextUtils.isEmpty(targetUrl) || targetUrl.startsWith("file://")) {
            SimToast.toastEe(targetUrl + " 该链接无法使用浏览器打开。");

            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri mUri = Uri.parse(targetUrl);
        intent.setData(mUri);
        startActivity(intent);


    }
}
