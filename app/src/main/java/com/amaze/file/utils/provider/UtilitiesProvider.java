package com.amaze.file.utils.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.amaze.file.ui.colors.ColorPreferenceHelper;
import com.amaze.file.ui.colors.ColorPreference;
import com.amaze.file.utils.theme.AppTheme;
import com.amaze.file.utils.theme.AppThemeManager;

/**
 * Created by piotaixr on 16/01/17.
 */

public class UtilitiesProvider {
    private ColorPreferenceHelper colorPreference;
    private AppThemeManager appThemeManager;

    public UtilitiesProvider(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        colorPreference = new ColorPreferenceHelper();
        colorPreference.getCurrentUserColorPreferences(context, sharedPreferences);
        appThemeManager = new AppThemeManager(sharedPreferences);
    }

    public ColorPreferenceHelper getColorPreference() {
        return colorPreference;
    }

    public AppTheme getAppTheme() {
        return appThemeManager.getAppTheme();
    }

    public AppThemeManager getThemeManager() {
        return appThemeManager;
    }
}
