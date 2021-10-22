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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.authenticationapi.databinding.FragmentProfileBinding;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ProfileFragment extends Fragment {

    private final OkHttpClient client = new OkHttpClient();
    FragmentProfileBinding binding;
    NavController navController;
    IProfile am;

    String token = "";
    String userId = "";
    User user;

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

        getActivity().setTitle(R.string.profile);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView);
        am.toggleDialog(true);
        getProfile(token);// calling profile API

        //Edit User Profile
        binding.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Utils.TOKEN, token);
                    navController.navigate(R.id.action_profileFragment_to_editProfileFragment, bundle);

            }
        });

        //Logout
        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delete token
                //navigate to login screen
            }
        });

        return view;
    }

    /*.....To get profile of logged in user.....*/
    public void getProfile(String token){

        Request request = new Request.Builder()
                .url(Utils.BASE_URL+"api/protected/profile")
                .addHeader("x-jwt-token", token)
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
                        String userKey = responseObject.getString(Utils.USER);
                        Gson gson = new Gson();
                        user = gson.fromJson(userKey, User.class);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                am.toggleDialog(false);
                                am.setUser(user);
                                binding.nameTextView.setText(user.getFirstname() + " "+ user.getLastname());
                                binding.ageTextView.setText(user.getAge());
                                binding.weightTextView.setText(user.getWeight()+" pounds");
                                binding.addressTextView.setText(user.getAddress());
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    getAlertDialogBox(response.body().string());
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
        if (context instanceof CreateNewAccountFragment.IRegister) {
            am = (IProfile) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    interface IProfile {
        void setUser(User user);
        void toggleDialog(boolean show);
    }
}