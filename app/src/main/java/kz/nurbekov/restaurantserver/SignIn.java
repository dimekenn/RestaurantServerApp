package kz.nurbekov.restaurantserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kz.nurbekov.restaurantserver.Common.Common;
import kz.nurbekov.restaurantserver.model.User;

public class SignIn extends AppCompatActivity {

    EditText edtPhone, edtPassword;
    Button btnSign;

    FirebaseDatabase db;
    DatabaseReference users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword = (EditText)findViewById(R.id.edtPassword);
        edtPhone = (EditText)findViewById(R.id.edtPhone);
        btnSign = (Button)findViewById(R.id.btnsignin2);

        //Firebase init
        db = FirebaseDatabase.getInstance();
        users = db.getReference("User");

        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               signInUser(edtPhone.getText().toString(), edtPassword.getText().toString());
            }
        });

    }

    private void signInUser(String phone, String password)
    {
        final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
        mDialog.setMessage("Пожалуйста подаждите!");
        mDialog.show();

        final String localPhone = phone;
        final String localPassword = password;
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(localPhone).exists()){
                    mDialog.dismiss();
                    User user = dataSnapshot.child(localPhone).getValue(User.class);
                    user.setPhone(localPhone);
                    if (Boolean.parseBoolean(user.getIsStaff())){
                        //isstaff true
                        if (user.getPassword().equals(localPassword)){
                            //login ok
                            Intent signInIntent = new Intent(SignIn.this, Home.class);
                            Common.currentUser = user;
                            startActivity(signInIntent);
                            finish();
                        } else {
                            Toast.makeText(SignIn.this, "Неправильный пароль", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignIn.this, "Зайдите с акаунта сотрудника", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mDialog.dismiss();
                    Toast.makeText(SignIn.this, "Такого акаунта нет", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
