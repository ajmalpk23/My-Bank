package cf.projectspro.bank.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

import cf.projectspro.bank.R;

public class Money extends AppCompatActivity {
    private Button add_money;
    private EditText amt;
    private DatabaseReference ref, refamt;
    private long amount_to_add, prevamt, total;
    private FirebaseAuth mAuth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money);

        amt = findViewById(R.id.money_value);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(Money.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            uid = mAuth.getCurrentUser().getUid();
        }
        add_money = findViewById(R.id.process_payment);
        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        refamt = ref.child(uid);

        add_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (TextUtils.isEmpty(amt.getText().toString())) {
                    Toast.makeText(Money.this, "Enter Amount", Toast.LENGTH_SHORT).show();
                } else {
                    amount_to_add = Long.parseLong(amt.getText().toString());
                    if (amount_to_add <= 0 || amount_to_add > 10000) {
                        amount_to_add = Long.parseLong(amt.getText().toString().trim());
                        Toast.makeText(Money.this, "Enter Appropriate Amount", Toast.LENGTH_SHORT).show();
                    } else {
                        amount_to_add = Long.parseLong(amt.getText().toString().trim());
                        // add_money.setTextColor(getResources().getColor(R.color.colorPrimary));
                        add_money.setText("Processing ...");
                        add_money.setClickable(false);
                        refamt.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String name;
                                prevamt = (long) dataSnapshot.child("amount").getValue();
                                name = (String) dataSnapshot.child("name").getValue();
                                DatabaseReference reference = dataSnapshot.getRef();
                                total = prevamt + amount_to_add;
                                // Toast.makeText(Money.this, ""+total, Toast.LENGTH_SHORT).show();

                                reference.child("amount").setValue(total).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(Money.this, "Success", Toast.LENGTH_SHORT).show();
                                            DatabaseReference trans = FirebaseDatabase.getInstance().getReference().child("transactions").child(uid);
                                            String random = UUID.randomUUID().toString();
                                            // Toast.makeText(Money.this, random, Toast.LENGTH_SHORT).show();
                                            // String key = trans.push().toString();
                                            DatabaseReference temp = trans.child(random);
                                            temp.child("amount").setValue(amount_to_add);
                                            temp.child("from").setValue(false);
                                            temp.child("src").setValue(success());
                                            temp.child("status").setValue(true);
                                            temp.child("to").setValue("Self");


                                            long tid = timestamp();
                                            temp.child("trans_id").setValue(tid);
                                            temp.child("code").setValue(-tid);
                                            DatabaseReference transblock = FirebaseDatabase.getInstance().getReference().child("trans_ids").child(String.valueOf(tid));
                                            transblock.child("amount").setValue(amount_to_add);
                                            transblock.child("status").setValue(true);
                                            transblock.child("to").setValue("self");
                                            transblock.child("fromuid").setValue(uid);
                                            transblock.child("touid").setValue(uid);
                                            transblock.child("done_by").setValue(name);

                                        } else if (!isnetworkAvailabe()) {
                                            Toast.makeText(Money.this, "Failed", Toast.LENGTH_SHORT).show();
                                            DatabaseReference trans = FirebaseDatabase.getInstance().getReference().child("transactions").child(uid);
                                            String random = UUID.randomUUID().toString();
                                            // Toast.makeText(Money.this, random, Toast.LENGTH_SHORT).show();
                                            // String key = trans.push().toString();
                                            DatabaseReference temp = trans.child(random);
                                            temp.child("amount").setValue(amount_to_add);
                                            temp.child("from").setValue(false);
                                            //Toast.makeText(Money.this, imagelink(1), Toast.LENGTH_SHORT).show();
                                            temp.child("src").setValue(failed());
                                            temp.child("status").setValue(false);
                                            temp.child("to").setValue("Self");
                                            long tid = timestamp();
                                            temp.child("trans_id").setValue(tid);
                                            temp.child("code").setValue(-tid);
                                            DatabaseReference transblock = FirebaseDatabase.getInstance().getReference().child("trans_ids").child(String.valueOf(tid));
                                            transblock.child("amount").setValue(amount_to_add);
                                            transblock.child("status").setValue(false);
                                            transblock.child("fromuid").setValue(uid);
                                            transblock.child("touid").setValue(uid);
                                            transblock.child("to").setValue("self");
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(Money.this, "Money Adding Failed", Toast.LENGTH_SHORT).show();
                            }
                        });


                        Intent intent = new Intent(Money.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    }

                }
            }
        });
    }

    private boolean isnetworkAvailabe() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private String failed() {
        return "https://firebasestorage.googleapis.com/v0/b/bank-f7765.appspot.com/o/failed.png?alt=media&token=4b6ef20f-5958-4edf-ad6d-279bbee57879";
    }

    public Long timestamp() {
        Long tsLong = System.currentTimeMillis();
        return tsLong;
    }

    private String success() {
        return "https://firebasestorage.googleapis.com/v0/b/bank-f7765.appspot.com/o/done.png?alt=media&token=e7920069-13a3-499f-8818-75cb25bc77fb";
    }

}
