package com.mrkai.estick;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Registeration extends Fragment {

    Button save;
    EditText name, address, age, guardianname, guardianaddress, guardianphone;
    RadioGroup radioGroup;
    String sex;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {

        super.onStop();
    }


    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registeration, container, false);
        save = view.findViewById(R.id.savebutton);
        name = view.findViewById(R.id.name);
        address = view.findViewById(R.id.address);
        age = view.findViewById(R.id.age);
        guardianname = view.findViewById(R.id.guardianname);
        guardianaddress = view.findViewById(R.id.guardianaddress);
        guardianphone = view.findViewById(R.id.guardianphonenumber);
        radioGroup = view.findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb = radioGroup.findViewById(i);
                sex = (String) rb.getText();
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE).edit();
                editor.putString("name", String.valueOf(name.getText()));
                editor.putString("address", String.valueOf(address.getText()));
                editor.putString("age", String.valueOf(age.getText()));
                editor.putString("sex", sex);
                editor.putString("guardianname", String.valueOf(guardianname.getText()));
                editor.putString("guardianaddress", String.valueOf(guardianaddress.getText()));
                editor.putString("guardianphone", String.valueOf(guardianphone.getText()));
                editor.putBoolean("Saved", true);

                editor.apply();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new TerminalFragment(), "devices").commit();

            }
        });
        return view;
    }


}
