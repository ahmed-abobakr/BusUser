package arab_open_university.com.bususer;

import android.app.Application;

import com.google.firebase.FirebaseApp;

/**
 * Created by akhalaf on 5/3/2017.
 */

public class BusUserApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
