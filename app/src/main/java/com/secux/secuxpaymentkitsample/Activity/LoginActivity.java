package com.secux.secuxpaymentkitsample.Activity;


import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.secux.secuxpaymentkitsample.Model.Setting;
import com.secux.secuxpaymentkitsample.R;
import com.secux.secuxpaymentkitsample.biometric.BiometricCallback;
import com.secux.secuxpaymentkitsample.biometric.BiometricManager;
import com.secux.secuxpaymentkitsample.biometric.BiometricUtils;
import com.secuxtech.paymentkit.SecuXAccountManager;
import com.secuxtech.paymentkit.SecuXServerRequestHandler;

import java.util.concurrent.Executor;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class LoginActivity extends BaseActivity {

    public static final int REQUEST_PWD_PROMPT = 1;

    EditText mEditTextAccount;
    EditText mEditTextPassword;

    TextView mTextViewBioLogin;
    View mViewUnderline;

    SecuXAccountManager mAccountManager = new SecuXAccountManager();
    protected BiometricManager mBioManager = null;

    protected boolean mAuthenicationScreenShow = false;
    protected BiometricCallback mBiometricCallback = new BiometricCallback() {
        @Override
        public void onSdkVersionNotSupported() {
            Log.i(TAG, "onSdkVersionNotSupported");
            showAuthenticationScreen();
        }

        @Override
        public void onBiometricAuthenticationNotSupported() {
            Log.i(TAG, "onBiometricAuthenticationNotSupported");
            showAuthenticationScreen();
        }

        @Override
        public void onBiometricAuthenticationNotAvailable() {
            Log.i(TAG, "onBiometricAuthenticationNotAvailable");
            showAuthenticationScreen();
        }

        @Override
        public void onBiometricAuthenticationPermissionNotGranted() {
            Log.i(TAG, "onBiometricAuthenticationPermissionNotGranted");
            showAuthenticationScreen();
        }

        @Override
        public void onBiometricAuthenticationInternalError(String error) {
            Log.i(TAG, "onBiometricAuthenticationInternalError");
            showAuthenticationScreen();
        }

        @Override
        public void onAuthenticationFailed() {
            Log.i(TAG, "onAuthenticationFailed");
        }

        @Override
        public void onAuthenticationCancelled() {
            Log.i(TAG, "onAuthenticationCancelled");
        }

        @Override
        public void onAuthenticationSuccessful() {

            biometricAuthenicationSuccessful();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {

        }

    };

    protected View.OnFocusChangeListener mViewFocusChangeListener = new View.OnFocusChangeListener(){
        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if (!hasFocus) {
                hideKeyboard(v);
            }
        }
    };

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager!=null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Setting.getInstance().loadSettings(mContext);

        mEditTextAccount = findViewById(R.id.editText_login_account);
        mEditTextPassword = findViewById(R.id.editText_login_password);

        mEditTextAccount.setOnFocusChangeListener(mViewFocusChangeListener);
        mEditTextPassword.setOnFocusChangeListener(mViewFocusChangeListener);

        mTextViewBioLogin = findViewById(R.id.textView_biometric_login);
        mViewUnderline = findViewById(R.id.view_login_underline);

        if (Setting.getInstance().mMerchantAccountName!="" && Setting.getInstance().mMerchantAccountPwd!="" &&
                mEditTextAccount.getText().toString().length()==0){

            if(BiometricUtils.isSdkVersionSupported() &&
                    BiometricUtils.isPermissionGranted(mContext) &&
                    BiometricUtils.isHardwareSupported(mContext) &&
                    BiometricUtils.isFingerprintAvailable(mContext)){

                onUseTouchIDFaceIDLoginClick(null);

            }else{
                KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (km.isDeviceSecure()){
                    //onUseTouchIDFaceIDLoginClick(null);
                }else{
                    mTextViewBioLogin.setVisibility(View.INVISIBLE);
                    mViewUnderline.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        mEditTextAccount.setText("");
        mEditTextPassword.setText("");

        if (Setting.getInstance().mMerchantAccountName!="" && Setting.getInstance().mMerchantAccountPwd!="" &&
                mEditTextAccount.getText().toString().length()==0){

            mTextViewBioLogin.setVisibility(View.VISIBLE);
            mViewUnderline.setVisibility(View.VISIBLE);

        }else{
            mTextViewBioLogin.setVisibility(View.INVISIBLE);
            mViewUnderline.setVisibility(View.INVISIBLE);
        }

    }


    protected void biometricAuthenicationSuccessful(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mEditTextAccount.setText(Setting.getInstance().mMerchantAccountName);
                mEditTextPassword.setText(Setting.getInstance().mMerchantAccountPwd);

                Button loginBtn = findViewById(R.id.button_login);
                onLoginButtonClick(loginBtn);
            }
        });

    }

    public void onLoginButtonClick(View v) {

        //hideKeyboard(null);

        if (mEditTextAccount.length()==0){
            showAlertInMain("Login failed!", "Please input name.");
            return;
        }

        if (mEditTextPassword.length() == 0){
            showAlert("Login failed!", "Please input password.");
            return;
        }


        showProgress("");
        new Thread(new Runnable() {
            @Override
            public void run() {


                String account = mEditTextAccount.getText().toString();
                String password = mEditTextPassword.getText().toString();

                Pair<Integer, String> loginRet = mAccountManager.loginMerchantAccount(account, password);
                Log.i(Setting.getInstance().mAPPName, "Merchant login " + account + " ret = " + loginRet.first + " " + loginRet.second);

                if (loginRet.first == SecuXServerRequestHandler.SecuXRequestOK) {

                    if (Setting.getInstance().mMerchantAccountName.compareTo(account)!=0 ||
                            Setting.getInstance().mMerchantAccountPwd.compareTo(password)!=0 ){

                        Setting.getInstance().mMerchantAccountName = account;
                        Setting.getInstance().mMerchantAccountPwd = password;
                        Setting.getInstance().saveSettings(mContext);
                    }

                    hideProgressInMain();
                    Intent newIntent = new Intent(mContext, MainActivity.class);
                    startActivity(newIntent);

                }else{
                    String error = "Invalid name/password";
                    if (loginRet.second.length() > 0){
                        error = "Error: " + loginRet.second;
                    }
                    hideProgressInMain();
                    showAlertInMain("Login failed!", error);
                }
            }
        }).start();

    }

    protected void showAuthenticationScreen(){


        if (mAuthenicationScreenShow){
            return;
        }

        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (!km.isDeviceSecure()){
            return;
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            // get the intent to prompt the user
            Intent intent = km.createConfirmDeviceCredentialIntent(Setting.getInstance().mAPPName, "Login with passcode");
            // launch the intent
            if (intent != null) {
                startActivityForResult(intent, REQUEST_PWD_PROMPT);
                mAuthenicationScreenShow = true;
            }
        }else{

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Login")
                    .setSubtitle(Setting.getInstance().mAPPName)
                    .setDescription("Login with passcode")
                    .setDeviceCredentialAllowed(true)
                    .build();

            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);

                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(200);
                            }catch (Exception e){

                            }
                            biometricAuthenicationSuccessful();
                        }
                    }).start();

                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();

                }
            });

            biometricPrompt.authenticate(promptInfo);

        }


    }


    private void loginAuthenication(){

        if (Setting.getInstance().mMerchantAccountName!="" && Setting.getInstance().mMerchantAccountPwd!=""){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBioManager = new BiometricManager.BiometricBuilder(mContext)
                            .setTitle("Login")
                            .setSubtitle(Setting.getInstance().mAPPName)
                            .setDescription("Auto login with you biometric ID")
                            .setNegativeButtonText("Cancel")
                            .build();

                    mBioManager.authenticate(mBiometricCallback);

                }
            });

        }
    }



    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(resultCode, resultCode, data);
        // see if this is being called from our password request..?
        if (requestCode == REQUEST_PWD_PROMPT) {
            // ..it is. Did the user get the password right?
            if (resultCode == RESULT_OK) {
                biometricAuthenicationSuccessful();
            } else {
                // they got it wrong/cancelled
            }
        }
        mAuthenicationScreenShow = false;
    }

    public void onUseTouchIDFaceIDLoginClick(View v){
        Log.i("MerchantApp", "onUseTouchIDFaceIDLoginClick");
        loginAuthenication();
    }
}