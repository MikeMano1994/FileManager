package com.amaze.file.activities.superclasses;

import android.support.v7.app.AppCompatActivity;

import com.amaze.file.ui.colors.ColorPreferenceHelper;
import com.amaze.file.utils.application.AppConfig;
import com.amaze.file.ui.colors.ColorPreference;
import com.amaze.file.utils.provider.UtilitiesProvider;
import com.amaze.file.utils.theme.AppTheme;

/**
 * Created by rpiotaix on 17/10/16.
 */
public class BasicActivity extends AppCompatActivity {

    protected AppConfig getAppConfig() {
        return (AppConfig) getApplication();
    }

    public ColorPreferenceHelper getColorPreference() {
        return getAppConfig().getUtilsProvider().getColorPreference();
    }

    public AppTheme getAppTheme() {
        return getAppConfig().getUtilsProvider().getAppTheme();
    }

    public UtilitiesProvider getUtilsProvider() {
        return getAppConfig().getUtilsProvider();
    }
}
