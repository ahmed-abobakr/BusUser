package arab_open_university.com.bususer;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class SigninFragment extends Fragment {


    Button btnRegister, btnLogin;
    EditText editEmail, editPassword;

    FirebaseAuth mAuth;

    public SigninFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signin, container, false);
        btnRegister = (Button) view.findViewById(R.id.signup_btn);
        btnLogin = (Button) view.findViewById(R.id.login_btn);
        editEmail = (EditText) view.findViewById(R.id.login_email);
        editPassword = (EditText) view.findViewById(R.id.login_password);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().beginTransaction().replace(R.id.signin_fragment, new SignUpFragment())
                        .commit();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editEmail.getText() != null && !editEmail.getText().toString().isEmpty() &&
                        editPassword.getText() != null && !editPassword.getText().toString().isEmpty()){
                    mAuth.signInWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                                        getActivity().finish();
                                    }else {
                                        Toast.makeText(getActivity(), "Auth Fialed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else {
                    Toast.makeText(getActivity(), "Please Enter the Email and Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
