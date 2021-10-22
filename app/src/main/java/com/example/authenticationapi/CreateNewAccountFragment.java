package com.example.authenticationapi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.authenticationapi.databinding.FragmentCreateNewAccountBinding;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class CreateNewAccountFragment extends Fragment {

    private final OkHttpClient client = new OkHttpClient();
    private FragmentCreateNewAccountBinding binding;
    NavController navController;
    IRegister am;

    private String firstname, lastname, age, weight, address, email, password;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.createAccount);

        binding = FragmentCreateNewAccountBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        //....Register Button......
        binding.registerButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstname = binding.createFragmentFirstNameId.getText().toString();
                lastname = binding.createFragmentLastNameId.getText().toString();
                age = binding.createFragmentAgeNameId.getText().toString();
                weight = binding.createFragmentWeightId.getText().toString();
                address = binding.createFragmentAddressId.getText().toString();
                email = binding.createFragmentEmailId.getText().toString();
                password = binding.createFragmentPasswordId.getText().toString();

                if(firstname.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterFirstName));
                }else if(lastname.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterLastName));
                }else if(age.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterAge));
                }else if(weight.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterWeight));
                }else if(address.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterWeight));
                }else if(email.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterEmail));
                }else if(password.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterPassword));
                }else{
                    createUser(new User(firstname,lastname, age, weight, address, email, password ));
                }

            }
        });

        //....Cancel Button......
        binding.cancelButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_createNewAccountFragment_to_loginFragment);
            }
        });

     return view;
    }

    /*.....Creating new user account .....*/
    public void createUser(User user){
        RequestBody requestBody = new FormBody.Builder()
                .add("firstname", user.getFirstname())
                .add("lastname", user.getLastname())
                .add("age", user.getAge())
                .add("weight", user.getWeight())
                .add("address",user.getAddress() )
                .add("email", user.getEmail() )
                .add("password", user.getPassword())
                .build();

        Request request = new Request.Builder()
                .url(Utils.BASE_URL+"register")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    try {
                        JSONObject responseObject = new JSONObject(response.body().string());
                        String token = responseObject.getString(Utils.TOKEN);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bundle bundle = new Bundle();
                                bundle.putString(Utils.TOKEN, token );
                                navController.navigate(R.id.action_createNewAccountFragment_to_profileFragment, bundle);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        JSONObject responseObject = new JSONObject(response.body().string());
                        String errorMessage = responseObject.getString(Utils.ERROR_KEY);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getAlertDialogBox(errorMessage);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    /* Alert Dialog Box */
    public void getAlertDialogBox(String errorMessage){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.errorMessage))
                .setMessage(errorMessage);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IRegister) {
            am = (IRegister) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    interface IRegister {
        void setUser(User user);
    }

}

