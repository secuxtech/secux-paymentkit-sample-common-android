package com.secux.secuxpaymentkitsample.Model;

import android.content.Context;
import android.content.SharedPreferences;



import static android.content.Context.MODE_PRIVATE;


/**
 * Created by maochuns.sun@gmail.com on 2020/5/11
 */
public class Setting {

    public String                               mAPPName = "";


    public String                               mMerchantAccountName = "";
    public String                               mMerchantAccountPwd = "";


    private static final Setting ourInstance = new Setting();
    public static Setting getInstance() {
        return ourInstance;
    }
    private Setting() {

    }

    public void saveSettings(Context context){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(mAPPName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        editor.putString("MerchantAccountName", mMerchantAccountName);
        editor.putString("MerchantAccountPwd", mMerchantAccountPwd);
        editor.apply();


    }

    public void loadSettings(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(mAPPName, MODE_PRIVATE);


        mMerchantAccountName = settings.getString("MerchantAccountName", "");
        mMerchantAccountPwd = settings.getString("MerchantAccountPwd", "");

    }

    public String getAccountName(){
        int idx = mMerchantAccountName.indexOf('@');
        if (idx == -1){
            return mMerchantAccountName;
        }

        return mMerchantAccountName.substring(0, idx);
    }

}
