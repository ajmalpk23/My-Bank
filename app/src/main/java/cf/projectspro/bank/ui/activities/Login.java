package cf.projectspro.bank.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import cf.projectspro.bank.R;
import cf.projectspro.bank.databinding.ActivityLoginBinding;


public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email, password;
    private Button signin, signup_click;
    private ProgressDialog pd;
    private boolean session;
    private DatabaseReference df;
    private TextView multiple_session;

    ActivityLoginBinding activityLoginBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLoginBinding = ActivityLoginBinding.inflate(LayoutInflater.from(this));
        setContentView(activityLoginBinding.getRoot());
        signin = findViewById(R.id.signup_now);
        email = findViewById(R.id.email);
        multiple_session = findViewById(R.id.multiple_sessions);
        password = findViewById(R.id.pass);
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait ...");
        pd.hide();
        signup_click = findViewById(R.id.sign_up_screen);
        df = FirebaseDatabase.getInstance().getReference().child("Users");
        if (mAuth.getCurrentUser() != null) {

            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        signup_click.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Signup.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        activityLoginBinding.forgotPassword.setOnClickListener(v->{
            Intent forgotIntent = new Intent(this,ForgotPassword.class);
            startActivity(forgotIntent);
        });
        signin.setOnClickListener(view -> {
            pd.show();
            String Email = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            signin.setText("Logging In ...");
            signin.setClickable(false);

            if (!TextUtils.isEmpty(Email) && !TextUtils.isEmpty(pass)) {
                mAuth.signInWithEmailAndPassword(Email, pass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        final String uid = mAuth.getCurrentUser().getUid();

                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        pd.dismiss();

                    } else {
                        Toast.makeText(Login.this, "Incorrect Details", Toast.LENGTH_SHORT).show();
                        signin.setText("Log In");
                        signin.setClickable(true);
                        pd.dismiss();
                    }
                });
            } else {
                signin.setText("Login");
                signin.setClickable(true);
                Toast.makeText(Login.this, "Fill All Details", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
