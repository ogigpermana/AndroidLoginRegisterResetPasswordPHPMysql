package com.ogi.androidclient.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.ogi.androidclient.Constants;
import com.ogi.androidclient.R;
import com.ogi.androidclient.RequestInterface;
import com.ogi.androidclient.models.ServerRequest;
import com.ogi.androidclient.models.ServerResponse;
import com.ogi.androidclient.models.User;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPasswordFragment extends Fragment implements View.OnClickListener {
    private Button btnReset;
    private EditText etEmail, etCode, etPassword;
    private TextView tvTimer;
    private ProgressBar progressBar;
    private boolean isResetInitiated = false;
    private String email;
    private CountDownTimer countDownTimer;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_reset, container, false);
        initialViews(view);
        return view;
    }

    private void initialViews(View view){
        btnReset = view.findViewById(R.id.btn_reset);
        etEmail = view.findViewById(R.id.et_email);
        etCode = view.findViewById(R.id.et_code);
        etPassword = view.findViewById(R.id.et_password);
        tvTimer = view.findViewById(R.id.timer);
        etPassword.setVisibility(View.GONE);
        etCode.setVisibility(View.GONE);
        tvTimer.setVisibility(View.GONE);
        btnReset.setOnClickListener(this);
        progressBar = view.findViewById(R.id.progress);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_reset){
                if(!isResetInitiated) {
                    email = etEmail.getText().toString();
                    if (!email.isEmpty()) {
                        progressBar.setVisibility(View.VISIBLE);
                        initiateResetPasswordProcess(email);
                    } else {
                        Snackbar.make(Objects.requireNonNull(getView()), "Fields are empty !", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    String code = etCode.getText().toString();
                    String password = etPassword.getText().toString();
                    if(!code.isEmpty() && !password.isEmpty()){
                        finishResetPasswordProcess(email,code,password);
                    } else {
                        Snackbar.make(Objects.requireNonNull(getView()), "Fields are empty !", Snackbar.LENGTH_LONG).show();
                    }
                }
        }
    }

    private void initiateResetPasswordProcess(String email){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        User user = new User();
        user.setEmail(email);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.RESET_PASSWORD_INITIATE);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                Snackbar.make(Objects.requireNonNull(getView()), resp.getMessage(), Snackbar.LENGTH_LONG).show();

                if(resp.getResult().equals(Constants.SUCCESS)){
                    Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                    etEmail.setVisibility(View.GONE);
                    etCode.setVisibility(View.VISIBLE);
                    etPassword.setVisibility(View.VISIBLE);
                    tvTimer.setVisibility(View.VISIBLE);
                    btnReset.setText(getResources().getString(R.string.change_password));
                    isResetInitiated = true;
                    startCountdownTimer();

                } else {
                    Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(Constants.TAG,"failed");
                Snackbar.make(Objects.requireNonNull(getView()), Objects.requireNonNull(t.getLocalizedMessage()), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void finishResetPasswordProcess(String email,String code, String password){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        User user = new User();
        user.setEmail(email);
        user.setCode(code);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.RESET_PASSWORD_FINISH);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                Snackbar.make(Objects.requireNonNull(getView()), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                if(resp.getResult().equals(Constants.SUCCESS)){

                    Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                    countDownTimer.cancel();
                    isResetInitiated = false;
                    goToLogin();
                } else {
                    Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(Constants.TAG,"failed");
                Snackbar.make(Objects.requireNonNull(getView()), Objects.requireNonNull(t.getLocalizedMessage()), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void startCountdownTimer(){
        countDownTimer = new CountDownTimer(120000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Time remaining : " + millisUntilFinished / 1000);
            }
            public void onFinish() {
                Snackbar.make(Objects.requireNonNull(getView()), "Time Out ! Request again to reset password.", Snackbar.LENGTH_LONG).show();
                goToLogin();
            }
        }.start();
    }

    private void goToLogin(){
        Fragment login = new LoginFragment();
        assert getFragmentManager() != null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,login);
        ft.commit();
    }



}
