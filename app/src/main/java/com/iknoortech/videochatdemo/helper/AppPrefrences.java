package com.iknoortech.videochatdemo.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPrefrences {

    private static SharedPreferences mPrefs;
    private static SharedPreferences.Editor mPrefsEditor;

    public static boolean isUserLoggedOut(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getBoolean("id_logged_in", true);
    }

    public static void setUserLoggedOut(Context ctx, Boolean value) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putBoolean("id_logged_in", value);
        mPrefsEditor.commit();
    }

    public static boolean isRequestLoad(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getBoolean("requestLoad", false);
    }

    public static void setRequestLoad(Context ctx, Boolean value) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putBoolean("requestLoad", value);
        mPrefsEditor.commit();
    }

    public static boolean isChatLoad(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getBoolean("chatLoad", false);
    }

    public static void setChatLoad(Context ctx, Boolean value) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putBoolean("chatLoad", value);
        mPrefsEditor.commit();
    }

    public static boolean isFriendLoad(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getBoolean("friendLoad", false);
    }

    public static void setFriendLoad(Context ctx, Boolean value) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putBoolean("friendLoad", value);
        mPrefsEditor.commit();
    }

    public static String getUserImage(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getString("userImage", "");
    }

    public static void setUserImage(Context ctx, String value) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString("userImage", value);
        mPrefsEditor.commit();
    }

    public static String getMobileNumber(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getString("MobileNumber", "");
    }

    public static void setMobileNumber(Context ctx, String value) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString("MobileNumber", value);
        mPrefsEditor.commit();
    }

    public static String getUserName(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getString("name", "");
    }

    public static void setUserName(Context ctx, String value) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString("name", value);
        mPrefsEditor.commit();
    }

    public static String getFirebaseUserID(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getString("FirebaseUserID", "");
    }

    public static void setFirebaseUserID(Context ctx, String value) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString("FirebaseUserID", value);
        mPrefsEditor.commit();
    }

    public static String getFirebaseToken(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getString("FirebaseToken", "");
    }

    public static void setFirebaseToken(Context ctx, String value) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString("FirebaseToken", value);
        mPrefsEditor.commit();
    }

    public static void clearAllPreferences(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.clear();
        mPrefsEditor.commit();
    }
}
