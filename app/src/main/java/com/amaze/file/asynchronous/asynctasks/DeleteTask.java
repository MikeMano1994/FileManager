/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.file.asynchronous.asynctasks;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;
import android.widget.Toast;

import com.amaze.file.R;
import com.amaze.file.activities.MainActivity;
import com.amaze.file.database.CryptHandler;
import com.amaze.file.exceptions.ShellNotRunningException;
import com.amaze.file.filesystem.HybridFileParcelable;
import com.amaze.file.fragments.CompressedExplorerFragment;
import com.amaze.file.fragments.preference_fragments.PreferencesConstants;
import com.amaze.file.ui.notifications.NotificationConstants;
import com.amaze.file.utils.DataUtils;
import com.amaze.file.utils.OTGUtil;
import com.amaze.file.utils.OpenMode;
import com.amaze.file.utils.cloud.CloudUtil;
import com.amaze.file.utils.files.CryptUtil;
import com.amaze.file.utils.files.FileUtils;
import com.cloudrail.si.interfaces.CloudStorage;

import java.util.ArrayList;

public class DeleteTask extends AsyncTask<ArrayList<HybridFileParcelable>, String, Boolean> {

    private ArrayList<HybridFileParcelable> files;
    private Context cd;
    private boolean rootMode;
    private CompressedExplorerFragment compressedExplorerFragment;
    private DataUtils dataUtils = DataUtils.getInstance();

    public DeleteTask(ContentResolver c, Context cd) {
        this.cd = cd;
        rootMode = PreferenceManager.getDefaultSharedPreferences(cd).getBoolean(PreferencesConstants.PREFERENCE_ROOTMODE, false);
    }

    public DeleteTask(ContentResolver c, Context cd, CompressedExplorerFragment compressedExplorerFragment) {
        this.cd = cd;
        rootMode = PreferenceManager.getDefaultSharedPreferences(cd).getBoolean(PreferencesConstants.PREFERENCE_ROOTMODE, false);
        this.compressedExplorerFragment = compressedExplorerFragment;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Toast.makeText(cd, values[0], Toast.LENGTH_SHORT).show();
    }

    protected Boolean doInBackground(ArrayList<HybridFileParcelable>... p1) {
        files = p1[0];
        boolean wasDeleted = true;
        if(files.size()==0)return true;

        if (files.get(0).isOtgFile()) {
            for (HybridFileParcelable file : files) {
                DocumentFile documentFile = OTGUtil.getDocumentFile(file.getPath(), cd, false);
                wasDeleted = documentFile.delete();
            }
        } else if (files.get(0).isDropBoxFile()) {
            CloudStorage cloudStorageDropbox = dataUtils.getAccount(OpenMode.DROPBOX);
            for (HybridFileParcelable baseFile : files) {
                try {
                    cloudStorageDropbox.delete(CloudUtil.stripPath(OpenMode.DROPBOX, baseFile.getPath()));
                } catch (Exception e) {
                    e.printStackTrace();
                    wasDeleted = false;
                    break;
                }
            }
        } else if (files.get(0).isBoxFile()) {
            CloudStorage cloudStorageBox = dataUtils.getAccount(OpenMode.BOX);
            for (HybridFileParcelable baseFile : files) {
                try {
                    cloudStorageBox.delete(CloudUtil.stripPath(OpenMode.BOX, baseFile.getPath()));
                } catch (Exception e) {
                    e.printStackTrace();
                    wasDeleted = false;
                    break;
                }
            }
        } else if (files.get(0).isGoogleDriveFile()) {
            CloudStorage cloudStorageGdrive = dataUtils.getAccount(OpenMode.GDRIVE);
            for (HybridFileParcelable baseFile : files) {
                try {
                    cloudStorageGdrive.delete(CloudUtil.stripPath(OpenMode.GDRIVE, baseFile.getPath()));
                } catch (Exception e) {
                    e.printStackTrace();
                    wasDeleted = false;
                    break;
                }
            }
        } else if (files.get(0).isOneDriveFile()) {
            CloudStorage cloudStorageOnedrive = dataUtils.getAccount(OpenMode.ONEDRIVE);
            for (HybridFileParcelable baseFile : files) {
                try {
                    cloudStorageOnedrive.delete(CloudUtil.stripPath(OpenMode.ONEDRIVE, baseFile.getPath()));
                } catch (Exception e) {
                    e.printStackTrace();
                    wasDeleted = false;
                    break;
                }
            }
        } else {
            for(HybridFileParcelable file : files) {
                try {
                    if (file.delete(cd, rootMode)) {
                        wasDeleted = true;
                    } else {
                        wasDeleted = false;
                        break;
                    }
                } catch (ShellNotRunningException e) {
                    e.printStackTrace();
                    wasDeleted = false;
                    break;
                }
            }
        }

        // delete file from media database
        if(!files.get(0).isSmb()) {
            try {
                for (HybridFileParcelable f : files) {
                    delete(cd,f.getPath());
                }
            } catch (Exception e) {
                for (HybridFileParcelable f : files) {
                    FileUtils.scanFile(f.getFile(), cd);
                }
            }
        }

        // delete file entry from encrypted database
        for (HybridFileParcelable file : files) {
            if (file.getName().endsWith(CryptUtil.CRYPT_EXTENSION)) {
                CryptHandler handler = new CryptHandler(cd);
                handler.clear(file.getPath());
            }
        }

        return wasDeleted;
    }

    @Override
    public void onPostExecute(Boolean wasDeleted) {

        Intent intent = new Intent(MainActivity.KEY_INTENT_LOAD_LIST);
        String path = files.get(0).getParent(cd);
        intent.putExtra(MainActivity.KEY_INTENT_LOAD_LIST_FILE, path);
        cd.sendBroadcast(intent);

        if (!wasDeleted) {
            Toast.makeText(cd, cd.getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        } else if (compressedExplorerFragment == null) {
            Toast.makeText(cd, cd.getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
        }

        if (compressedExplorerFragment!=null) {
            compressedExplorerFragment.files.clear();
        }

        // cancel any processing notification because of cut/paste operation
        NotificationManager notificationManager = (NotificationManager) cd.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationConstants.COPY_ID);
    }

    private void delete(final Context context, final String file) {
        final String where = MediaStore.MediaColumns.DATA + "=?";
        final String[] selectionArgs = new String[] {
                file
        };
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri filesUri = MediaStore.Files.getContentUri("external");
        // Delete the entry from the media database. This will actually delete media files.
        contentResolver.delete(filesUri, where, selectionArgs);
    }
}



