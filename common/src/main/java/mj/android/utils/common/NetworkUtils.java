package mj.android.utils.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

public final class NetworkUtils {

    private NetworkUtils() {

    }

    /**
     * 네트워크가 사용 가능한지 확인한다.
     */
    public static boolean isConnectivityEnable(Context context) {
        context = context.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return isConnectivityEnableAPI23(context);
        else
            return isConnectivityEnableAPIUnder23(context);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static boolean isConnectivityEnableAPI23(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connectivityManager.getAllNetworks();
        for (Network network : networks) {
            if (isNetworkEnable(connectivityManager.getNetworkInfo(network)))
                return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.M)
    private static boolean isConnectivityEnableAPIUnder23(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo info : networkInfos) {
            if (isNetworkEnable(info))
                return true;
        }

        return false;
    }

    private static boolean isNetworkEnable(NetworkInfo info) {
        switch (info.getType()) {
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_WIFI:
                switch (info.getState()) {
                    case CONNECTED:
                    case CONNECTING:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }
}

