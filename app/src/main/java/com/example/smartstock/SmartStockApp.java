package com.example.smartstock;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class SmartStockApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                .setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
                .setGcmSenderId(BuildConfig.FIREBASE_PROJECT_NUMBER)
                .build();
                
        FirebaseApp.initializeApp(this, options);
    }
}
