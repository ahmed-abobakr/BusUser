package arab_open_university.com.bususer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        getFragmentManager().beginTransaction().replace(R.id.signin_fragment, new SigninFragment())
                .commit();
    }
}
