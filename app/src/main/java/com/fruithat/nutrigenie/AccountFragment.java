package com.fruithat.nutrigenie;

import android.accounts.Account;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import android.content.Intent;

import java.util.HashMap;

public class AccountFragment extends Fragment {

    private Button save;
    private Button reset;
    private Button calculate;
    private EditText heightText;
    private EditText weightText;
    private EditText ageText;
    private TextView result;
    private RadioGroup gender;
    private RadioButton male;
    private RadioButton female;

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Inflate the layout defined in quote_fragment.xml
        // The last parameter is false because the returned view does not need to be attached to the container ViewGroup
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        save = view.findViewById(R.id.save);
        reset = view.findViewById(R.id.reset);
        heightText = view.findViewById(R.id.height_value);
        weightText = view.findViewById(R.id.weight_value);
        ageText = view.findViewById(R.id.age_value);
        result = view.findViewById(R.id.result_value);
        gender = view.findViewById(R.id.gender_group);
        male = view.findViewById(R.id.male);
        female = view.findViewById(R.id.female);
        calculate = view.findViewById(R.id.calculate);

        save.setOnClickListener(v -> saveAccount());

        reset.setOnClickListener(v -> {
            gender.clearCheck();
            gender.check(R.id.male);

            heightText.setText(null);
            weightText.setText(null);
            ageText.setText(null);
            result.setText(null);
        });

        calculate.setOnClickListener(v -> {

            int weight = 0;
            double height = 0.0;
            int age = 0;

                if (!weightText.getText().toString().equals("")) {
                    weight = Integer.parseInt(weightText.getText().toString());
                }
                else if (weightText.getText().toString().equals("") && !heightText.getText().toString().equals("") &&
                        !ageText.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Please enter your weight", Toast.LENGTH_LONG).show();
                }

                if (!heightText.getText().toString().equals("")) {
                    height = Double.parseDouble(heightText.getText().toString());
                }
                else if(!weightText.getText().toString().equals("") && heightText.getText().toString().equals("") &&
                    !ageText.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Please enter your height", Toast.LENGTH_LONG).show();
                }

                if (!ageText.getText().toString().equals("")) {
                    age = Integer.parseInt(ageText.getText().toString());
                }
                else if (!weightText.getText().toString().equals("") && !heightText.getText().toString().equals("") &&
                        ageText.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Please enter your age", Toast.LENGTH_LONG).show();
                }

                if(!weightText.getText().toString().equals("") && !heightText.getText().toString().equals("") &&
                        !ageText.getText().toString().equals("")) {
                    if (female.isChecked()) {
                        double women_BMR = 10 * (weight * 0.45359237) + 6.25 * height - 5 * age - 161;
                        women_BMR = women_BMR * 1.2;

                        result.setText(String.valueOf((int) women_BMR));

                    } else if (male.isChecked()) {
                        double men_BMR = 10 * (weight * 0.45359237) + 6.25 * height - 5 * age + 5;
                        men_BMR = men_BMR * 1.2;

                        result.setText(String.valueOf((int) men_BMR));
                    }
                } else if((weightText.getText().toString().equals("") && heightText.getText().toString().equals("") &&
                        ageText.getText().toString().equals(""))){
                    Toast.makeText(getContext(), "Please enter your information", Toast.LENGTH_LONG).show();
                }else if((weightText.getText().toString().equals("") && heightText.getText().toString().equals(""))){
                    Toast.makeText(getContext(), "Please enter your information", Toast.LENGTH_LONG).show();
                }else if((weightText.getText().toString().equals("")  && ageText.getText().toString().equals(""))){
                    Toast.makeText(getContext(), "Please enter your information", Toast.LENGTH_LONG).show();
                }else if(heightText.getText().toString().equals("") && ageText.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Please enter your information", Toast.LENGTH_LONG).show();
                }
        });


        mDatabase.child("account").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    switch (data.getKey()) {
                        case "height":
                            heightText.setText(String.valueOf(data.getValue()));
                            break;
                        case "weight":
                            weightText.setText(String.valueOf(data.getValue()));
                            break;
                        case "age":
                            ageText.setText(String.valueOf(data.getValue()));
                            break;
                        case "gender":
                            if (((Long) data.getValue()) == 1) {
                                male.setChecked(true);
                            } else {
                                female.setChecked(true);
                            }
                            break;
                        case "calories":
                            result.setText(String.valueOf(data.getValue()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Don't destroy Fragment on reconfiguration
        setRetainInstance(true);
    }

    private void saveAccount() {
        if (result.getText() == null) {
            Toast.makeText(getContext(), "Please calculate calories before saving.", Toast.LENGTH_LONG).show();
        } else {
            HashMap<String, Object> data = new HashMap<>(4);

            data.put("height", Integer.parseInt(heightText.getText().toString()));
            data.put("weight", Integer.parseInt(weightText.getText().toString()));
            data.put("age", Integer.parseInt(ageText.getText().toString()));
            data.put("gender", male.isChecked() ? 1 : 0);
            data.put("calories", Integer.parseInt(result.getText().toString()));

            mDatabase.child("account").child(mCurrentUser.getUid()).updateChildren(data);

            CoordinatorLayout coordinatorLayout = getActivity().findViewById(R.id.fragment_container);
            Snackbar.make(coordinatorLayout, "Profile information saved", Snackbar.LENGTH_SHORT).show();

            BottomNavigationView nav = getActivity().findViewById(R.id.navigation);
            nav.setSelectedItemId(R.id.navigation_home);
        }
    }

}
