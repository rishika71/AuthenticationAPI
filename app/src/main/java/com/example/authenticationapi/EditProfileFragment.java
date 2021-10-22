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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.authenticationapi.databinding.FragmentEditProfileBinding;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class EditProfileFragment extends Fragment {

    private final OkHttpClient client = new OkHttpClient();
    FragmentEditProfileBinding binding;
    NavController navController;
    IEditUser am;

    private String firstname, lastname, age, weight, address, email;
    private String userId, token;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString(Utils.TOKEN);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Edit Profile");
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);

        User user = am.getUser();
        userId = user.getUserId();
        binding.editTextFirstname.setText(user.getFirstname());
        binding.editTextLastname.setText(user.getLastname());
        binding.editTextAge.setText(user.getAge());
        binding.editTextWeight.setText(user.getWeight());
        binding.editTextAddress.setText(user.getAddress());
        binding.editTextEmail.setText(user.getEmail());

        //Edit User Profile
        binding.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstname = binding.editTextFirstname.getText().toString();
                lastname = binding.editTextLastname.getText().toString();
                age = binding.editTextAge.getText().toString();
                weight = binding.editTextWeight.getText().toString();
                address = binding.editTextAddress.getText().toString();
                email = binding.editTextEmail.getText().toString();


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
                }else{
                    onProfileEdit(new User(firstname,lastname, age, weight, address, email));
                }

            }
        });

        //Cancel
        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.popBackStack();
            }
        });
        return view;
    }

    /*.....On Profile Edit .....*/
    public void onProfileEdit(User user){
        RequestBody requestBody = new FormBody.Builder()
                .add("_id", userId)
                .add("firstname", user.getFirstname())
                .add("lastname", user.getLastname())
                .add("age", user.getAge())
                .add("weight", user.getWeight())
                .add("address",user.getAddress() )
                .add("email", user.getEmail() )
                .build();

        Request request = new Request.Builder()
                .url(Utils.BASE_URL+"update/profile")
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle bundle = new Bundle();
                            bundle.putString(Utils.TOKEN, token);
                            navController.navigate(R.id.action_editProfileFragment_to_profileFragment, bundle);
                        }
                    });

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
        if (context instanceof IEditUser) {
            am = (IEditUser) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    interface IEditUser {
        User getUser();
        void setUser(User user);
    }
}