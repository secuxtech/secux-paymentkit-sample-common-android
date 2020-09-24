package com.secux.secuxpaymentkitsample.biometric;

import android.annotation.TargetApi;
import android.content.Context;

import android.os.Build;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import com.secux.secuxpaymentkitsample.Model.Setting;
import com.secux.secuxpaymentkitsample.R;

import java.util.concurrent.Executor;

public class BiometricManager extends BiometricManagerV23 {

    protected androidx.biometric.BiometricPrompt mBiometricPrompt = null;
    //protected CancellationSignal mCancellationSignal = new CancellationSignal();

    protected BiometricManager(final BiometricBuilder biometricBuilder) {
        this.context = biometricBuilder.context;
        this.title = biometricBuilder.title;
        this.subtitle = biometricBuilder.subtitle;
        this.description = biometricBuilder.description;
        this.negativeButtonText = biometricBuilder.negativeButtonText;
    }


    public void authenticate(@NonNull final BiometricCallback biometricCallback) {

        if(title == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog title cannot be null");
            return;
        }


        if(subtitle == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog subtitle cannot be null");
            return;
        }


        if(description == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog description cannot be null");
            return;
        }

        if(negativeButtonText == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog negative button text cannot be null");
            return;
        }


        if(!BiometricUtils.isSdkVersionSupported()) {
            biometricCallback.onSdkVersionNotSupported();
            return;
        }

        if(!BiometricUtils.isPermissionGranted(context)) {
            biometricCallback.onBiometricAuthenticationPermissionNotGranted();
            return;
        }

        if(!BiometricUtils.isHardwareSupported(context)) {
            biometricCallback.onBiometricAuthenticationNotSupported();
            return;
        }

        if(!BiometricUtils.isFingerprintAvailable(context)) {
            biometricCallback.onBiometricAuthenticationNotAvailable();
            return;
        }

        displayBiometricDialog(biometricCallback);
    }

    public void cancelAuthentication(){
        if(BiometricUtils.isBiometricPromptEnabled()) {
            //if (!mCancellationSignal.isCanceled())
            //   mCancellationSignal.cancel();
            if (mBiometricPrompt!=null){
                mBiometricPrompt.cancelAuthentication();
            }
        }else{
            if (!mCancellationSignalV23.isCanceled())
                mCancellationSignalV23.cancel();
        }
    }



    private void displayBiometricDialog(BiometricCallback biometricCallback) {
        if(BiometricUtils.isBiometricPromptEnabled()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                displayBiometricPromptV23(biometricCallback);
            }else {
                displayBiometricPrompt(new BiometricCallbackV28(biometricCallback));
            }
        } else {
            displayBiometricPromptV23(biometricCallback);
        }
    }



    /*
    @TargetApi(Build.VERSION_CODES.P)
    private void displayBiometricPrompt(final BiometricCallback biometricCallback) {

        new BiometricPrompt.Builder(context)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                //.setDeviceCredentialAllowed(true)

                .setNegativeButton(negativeButtonText, context.getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //dismissDialog();
                        //biometricCallback.onAuthenticationCancelled();
                        mCancellationSignal.cancel();
                    }
                })


                .build()
                .authenticate(mCancellationSignal, context.getMainExecutor(),
                        new BiometricCallbackV28(biometricCallback));
    }

     */

    @TargetApi(Build.VERSION_CODES.P)
    private void displayBiometricPrompt(final androidx.biometric.BiometricPrompt.AuthenticationCallback biometricCallback) {

        androidx.biometric.BiometricPrompt.PromptInfo promptInfo = new androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login")
                .setSubtitle(Setting.getInstance().mAPPName)
                .setDescription(context.getResources().getString(R.string.login_with_bio_id))
                .setNegativeButtonText(negativeButtonText)
                .build();

        Executor executor = ContextCompat.getMainExecutor(context);
        mBiometricPrompt = new androidx.biometric.BiometricPrompt((FragmentActivity)context,
                executor, biometricCallback);

        mBiometricPrompt.authenticate(promptInfo);
    }


    public static class BiometricBuilder {

        private String title;
        private String subtitle;
        private String description;
        private String negativeButtonText;

        private Context context;
        public BiometricBuilder(Context context) {
            this.context = context;
        }

        public BiometricBuilder setTitle(@NonNull final String title) {
            this.title = title;
            return this;
        }

        public BiometricBuilder setSubtitle(@NonNull final String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public BiometricBuilder setDescription(@NonNull final String description) {
            this.description = description;
            return this;
        }


        public BiometricBuilder setNegativeButtonText(@NonNull final String negativeButtonText) {
            this.negativeButtonText = negativeButtonText;
            return this;
        }

        public BiometricManager build() {
            return new BiometricManager(this);
        }
    }
}
