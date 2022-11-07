package com.example.apkdownload;


import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN ACTIVITY";
    private static final int Request_code = 100;

    PackageManager packageManager;
    Api api;
    Button btnExtract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = ApiClient.getClient().create(Api.class);

        btnExtract = (Button) findViewById(R.id.check);

        btnExtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadApk();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Request_code);
            }
        }
    }

    private void UploadApk() {
        /////

        packageManager = getApplicationContext().getPackageManager();
        HashMap<String, String> installedApkFilePaths = new HashMap<>();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        if (isValid(packages)) {
            for (ApplicationInfo packageInfo : packages) {
                Log.d(TAG, "Installed package :" + packageInfo.packageName);
                Log.d(TAG, "Source dir : " + packageInfo.sourceDir);
                Log.d(TAG, "Launch Activity :" + packageManager.getLaunchIntentForPackage(packageInfo.packageName));
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    continue;
                } else {
                    if (getPackageManager().getInstallerPackageName(packageInfo.packageName) == null
                            || !getPackageManager().getInstallerPackageName(packageInfo.packageName).equalsIgnoreCase("com.android.vending")) {

                        Log.d(TAG, "getallapps: " + packageInfo.packageName);
                        File apkFile = new File(packageInfo.publicSourceDir);
                        if (apkFile.exists()) {
                            installedApkFilePaths.put(packageInfo.packageName, apkFile.getAbsolutePath());
                            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/MY_APKS/");
                            if (!dir.exists()) {
                                dir.mkdirs();
                                Log.d(TAG, "UploadApk: "+dir.exists());

                            } else {
                                Log.d("folder","folder already hai");

                                File yesFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/MY_APKS/" + packageInfo.packageName + ".apk");
                                if (yesFile.exists()){
                                    Log.d("folder","file already hai");
                                } else {
                                    Log.d("asf","dsfsk");
                                    File output = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/MY_APKS/" + packageInfo.packageName + ".apk");
                                    try {
                                        output.createNewFile();
                                        FileOutputStream fos;
                                        InputStream assetFile = new FileInputStream(apkFile);
                                        fos = new FileOutputStream(output);
                                        copyFile(assetFile, fos);
                                        fos.close();
                                        assetFile.close();

                                        Toast.makeText(getApplicationContext(), "APK EXTRACTED!", Toast.LENGTH_SHORT).show();

                                        exportApk(packageInfo.packageName.replace(".", "_"), packageInfo.publicSourceDir);

//                                deleteFolder();   /////

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }

                        }

                    } else {

                        Log.d(TAG, "getallapps:123 " + packageManager.getInstallerPackageName(packageInfo.packageName) + " package: " + packageInfo.packageName);
                    }
                }
            }
        }

        //////
    }

//    private void displatToast(String s) {
//        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
//    }

//    /////
//    private void deleteFolder() {
//        File apkFileDelete = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/MY_APKS/");
//
//        if (apkFileDelete.exists()) {
//            try {
//                File[] filesList = apkFileDelete.listFiles();
//
//                for (int i = 0; i < filesList.length; i++) {
//                    filesList[i].delete();
//                }
//                boolean isFileDeleted = apkFileDelete.delete();
//                if (isFileDeleted) {
//                    Log.d("deleteFolderOk:", "FIle deleted Successfully");
//                } else {
//                    Log.d("deleteFolderNo:", "FIle Not Deleted");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /////
    @Override
    protected void onStart() {
        super.onStart();

    }

    private void runShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }


    private void copyFile(InputStream in, java.io.OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private boolean isValid(List<ApplicationInfo> packageInfos) {
        return packageInfos != null && !packageInfos.isEmpty();
    }

    private ApplicationInfo getApplicationInfoFrom(PackageManager packageManager, PackageInfo packageInfo) {
        return packageInfo.applicationInfo;
    }


    public void exportApk(String apkName, String apkFilePath) {
        File file = new File(apkFilePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part multiPartBody = MultipartBody.Part.createFormData("APKS", file.getName(), requestBody);

        Call<ResponseBody> responseBodyCall = api.postFile(multiPartBody, apkName + ".apk");

        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: " + response.message());

                if (response.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "all ready exits", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
            }
        });
    }


    class getallapps extends AsyncTask<String, Void, View> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("TAG" + " PreExceute","On pre Exceute......");
        }

        @Override
        protected View doInBackground(String... strings) {

            UploadApk();

            return null;
        }

        @Override
        protected void onPostExecute(View view) {
            super.onPostExecute(view);
            Log.d("TAG" , " onPostExecute"+view);
        }

    }

}




