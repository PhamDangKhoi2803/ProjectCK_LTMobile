package ute.nhom27.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import ute.nhom27.android.model.User;


public class SharedPrefManager {
    private static final String PREF_NAME = "MyChatAppPrefs";
    private SharedPreferences prefs;

    public SharedPrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("userId", user.getId());
        editor.putString("username", user.getUsername());
        editor.putString("email", user.getEmail());
        editor.putString("phone", user.getPhone());
        editor.putString("themePreference", user.getThemePreference() != null ? user.getThemePreference() : "light");
        editor.apply();
    }

    public User getUser() {
        User user = new User();
        user.setId(prefs.getLong("userId", 0));
        user.setUsername(prefs.getString("username", null));
        user.setEmail(prefs.getString("email", null));
        user.setPhone(prefs.getString("phone", null));
        user.setThemePreference(prefs.getString("themePreference", "light"));
        return user;
    }

    public boolean isLoggedIn() {
        return prefs.getLong("userId", 0) != 0;
    }

    public void clear() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}