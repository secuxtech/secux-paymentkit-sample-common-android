package com.secux.secuxpaymentkitsample.Dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.secux.secuxpaymentkitsample.R;
import com.secuxtech.paymentkit.SecuXStoreInfo;

public class PaymentDetailsDialog {
    private AlertDialog mAlertDialog;


    public void showDialog(Context context, SecuXStoreInfo storeInfo, String coin, String token, String amount) {

        mAlertDialog = new AlertDialog.Builder(context).create();
        View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_payment_details, null);

        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);


        TextView tvStoreName = loadView.findViewById(R.id.textview_payment_store_name);
        tvStoreName.setText(storeInfo.mName);

        ImageView ivStoreImg = loadView.findViewById(R.id.imageView_storelogo);
        ivStoreImg.setImageBitmap(storeInfo.mLogo);

        TextView tvCoinToken = loadView.findViewById(R.id.textview_payment_coin_token);
        tvCoinToken.setText(coin + ":" + token);

        TextView tvAmunt = loadView.findViewById(R.id.textview_payment_amount);
        tvAmunt.setText("Amount: " + amount);


        mAlertDialog.show();
    }


    public void dismiss() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }
}
