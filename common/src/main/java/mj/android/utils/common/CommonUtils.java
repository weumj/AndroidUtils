package mj.android.utils.common;


import android.content.Intent;
import android.net.Uri;

public final class CommonUtils {
    private CommonUtils() {

    }

    /**
     * 인텐트를 통해 인터넷 페이지를 띄운다.
     *
     * @param webURL 접속하려는 페이지의 url
     * @return url 이 설정된 intent
     */
    public static Intent getWebPageIntent(String webURL) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(webURL));
        return intent;
    }
}
