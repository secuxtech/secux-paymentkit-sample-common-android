package com.secux.secuxpaymentkitsample.Activity;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.secux.secuxpaymentkitsample.Dialog.PaymentDetailsDialog;
import com.secux.secuxpaymentkitsample.Dialog.PromotionDetailsDialog;
import com.secux.secuxpaymentkitsample.Dialog.RefillDetailsDialog;
import com.secux.secuxpaymentkitsample.Model.Setting;
import com.secux.secuxpaymentkitsample.R;
import com.secux.secuxpaymentkitsample.Utility.SecuXQRCodeParser;
import com.secuxtech.paymentkit.SecuXAccountManager;
import com.secuxtech.paymentkit.SecuXPaymentManager;
import com.secuxtech.paymentkit.SecuXServerRequestHandler;
import com.secuxtech.paymentkit.SecuXStoreInfo;


public class MainActivity extends BaseActivity {

    private String mAccountName = "";
    private String mAccountPwd = "";

    private SecuXPaymentManager mPaymentManager = new SecuXPaymentManager();
    private SecuXAccountManager mAccountManager = new SecuXAccountManager();

    PromotionDetailsDialog mPromotionDetailsDlg = new PromotionDetailsDialog();
    PaymentDetailsDialog mPaymentDetailsDlg = new PaymentDetailsDialog();
    RefillDetailsDialog mRefillDetailsDlg = new RefillDetailsDialog();

    SecuXQRCodeParser mQRCodeParser = null;
    SecuXStoreInfo mStoreInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccountName = Setting.getInstance().mMerchantAccountName;
        mAccountPwd = Setting.getInstance().mMerchantAccountPwd;

        mAccountManager.setBaseServer("https://pmsweb-sandbox.secuxtech.com");
        checkBLESetting();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null){
            if (requestCode == 0x01){
                final String qrcode = data.getExtras().getString("qrcode");

                try{
                    mQRCodeParser = new SecuXQRCodeParser(qrcode);
                }catch (Exception e){
                    showAlertInMain("Unsupported QRCode!", "", true);
                    return;
                }

                showProgress("processing...");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!login(mAccountName, mAccountPwd)){
                            showAlertInMain("Login failed!", "", true);
                            return;
                        }

                        final Pair<Pair<Integer, String>, SecuXStoreInfo> storeInfo = mPaymentManager.getStoreInfo(mQRCodeParser.mDevIDHash);
                        if (storeInfo.first.first != SecuXServerRequestHandler.SecuXRequestOK){
                            showAlertInMain("Get store info. failed!", "", true);
                            return;
                        }

                        mStoreInfo = storeInfo.second;

                        if (mQRCodeParser.mAmount.length()>0) {
                            if (mQRCodeParser.mCoin.compareTo("$") == 0) {
                                showPromotionDetails();
                            }else{
                                showPaymentDetails();
                            }
                        }else if (mQRCodeParser.mRefill.length()>0) {
                            showRefillDetails();
                        }
                    }
                }).start();

            }
        }
    }


    public void onScanQRCodeButtonClick(View v){
        if (!checkBLESetting()) {
            return;
        }

        Intent newIntent = new Intent(mContext, ScanQRCodeActivity.class);
        //startActivity(newIntent);

        startActivityForResult(newIntent, 0x01);
    }


    public boolean login(String name, String pwd){
        Pair<Integer, String> ret = mAccountManager.loginMerchantAccount(name, pwd);
        if (ret.first == SecuXServerRequestHandler.SecuXRequestOK) {
            return true;
        }

        Log.i("", "Login merchant account failed! error:" + ret.second);
        return false;
    }


    public void showPromotionDetails(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                SecuXStoreInfo.SecuXPromotion promotion = mStoreInfo.getPromotionDetails(mQRCodeParser.mToken);
                if (promotion != null) {
                    mPromotionDetailsDlg.showDialog(mContext, mStoreInfo, promotion);
                }else{
                    showAlert("Unsupported promotion!", "");
                }
            }
        });

    }

    public void showPaymentDetails(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                mPaymentDetailsDlg.showDialog(mContext, mStoreInfo, mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mAmount);
            }
        });

    }

    public void showRefillDetails(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                mRefillDetailsDlg.showDialog(mContext, mStoreInfo, mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mRefill);
            }
        });

    }

    public void confirmOperation(SecuXStoreInfo storeInfo, String transID, String coin, String token, String amount, String nonce, String type){

        if (!this.checkWifi()){
            showAlert("No network!", "Please check the phone's network setting");
            return;
        }

        if (!this.checkBLESetting()){
            return;
        }

        Pair<Integer, String> verifyRet = mPaymentManager.doActivity(this, this.mAccountName, storeInfo.mDevID,
                coin, token, transID, amount, nonce, type);

        if (verifyRet.first == SecuXServerRequestHandler.SecuXRequestUnauthorized){
            if (!login(this.mAccountName, this.mAccountPwd)){
                showAlertInMain("Login failed!", "", true);
                return;
            }
            verifyRet = mPaymentManager.doActivity(this, this.mAccountName, storeInfo.mDevID,
                    coin, token, transID, amount, nonce, type);
        }

        if (verifyRet.first != SecuXServerRequestHandler.SecuXRequestOK){
            showAlertInMain("Verify the operation data to P22 failed!", "error: " + verifyRet.second, true);
        }else{
            showAlertInMain("Verify the operation data to P22 successfully!", "", true);
        }
    }


    public void onCancelPromotionButtonClick(View v){
        mPromotionDetailsDlg.dismiss();
    }

    public void onConfirmPromotionButtonClick(View v){
        mPromotionDetailsDlg.dismiss();

        showProgress("Confirm...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                confirmOperation(mStoreInfo, "Promotion00001", mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mAmount, mQRCodeParser.mNonce, "promotion");
            }
        }).start();
    }


    public void onCancelRefillButtonClick(View v){
        mRefillDetailsDlg.dismiss();
    }

    public void onConfirmRefillButtonClick(View v){
        mRefillDetailsDlg.dismiss();

        showProgress("Confirm...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                confirmOperation(mStoreInfo, "Refill00001", mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mRefill, mQRCodeParser.mNonce, "refill");
            }
        }).start();
    }

    public void onCancelPaymentButtonClick(View v){
        mPaymentDetailsDlg.dismiss();
    }

    public void onConfirmPaymentButtonClick(View v){
        mPaymentDetailsDlg.dismiss();

        showProgress("Confirm...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                confirmOperation(mStoreInfo, "Payment00001", mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mAmount, mQRCodeParser.mNonce, "payment");
            }
        }).start();
    }
}