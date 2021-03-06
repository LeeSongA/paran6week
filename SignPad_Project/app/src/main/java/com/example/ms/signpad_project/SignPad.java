package com.example.ms.signpad_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ms.resource.StringResource;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by ms on 2018-02-08.
 */

public class SignPad extends Activity{
    private Context context;
    private SpenNoteDoc spenNoteDoc;
    private SpenPageDoc spenPageDoc;
    private SpenSurfaceView spenSurfaceView;
    private SpenSettingPenInfo spenSettingPenInfo;

    private int stroke;
    double dp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);      // 타이틀바 없애기
        context = this;

        // Get html canvas size
        Intent intent = getIntent();
        int width = intent.getIntExtra("width", 0);
        int height = intent.getIntExtra("height", 0);

        // Get the dimension of the device screen.
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int widthRatio = size.x / width;      // Set Ratio
//        int heightRatio = (size.y / 2) / height;

        // Set Ratio
        DisplayMetrics displayMetrics2 = getApplicationContext().getResources().getDisplayMetrics();
        float widthRatio = (displayMetrics2.widthPixels * 9 / 10) / width;                       // (최대 너비) / canvas 너비
        float heightRatio = (displayMetrics2.heightPixels * 2 / 5) / height;                     // (최대 높이) / canvas 높이

        // Set SignPad Position
        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        wmlp.gravity = Gravity.BOTTOM;
        wmlp.y = displayMetrics2.heightPixels * 1 / 10;

        // Set SpenSurfaceView Size
        int screenWidth;
        int screenHeight;
        if(widthRatio > heightRatio) {
            screenWidth = (int)(width * heightRatio);
            screenHeight = (int)(height * heightRatio);
        }
        else {
            screenWidth = (int)(width * widthRatio);
            screenHeight = (int)(height * widthRatio);
        }

        // px to dp
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dp = displayMetrics.density;

        // layout
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        String BackgroundColor = "#eeeeee";
        linearLayout.setBackgroundColor(Color.parseColor((BackgroundColor)));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (60 * dp));
        textView.setText("SignPad");
        String BackgroundColor1 = "#888888";
        textView.setBackgroundColor(Color.parseColor(BackgroundColor1));
        textView.setGravity(Gravity.CENTER);
        String TextColor = "#ffffff";
        textView.setTextColor(Color.parseColor(TextColor));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        linearLayout.addView(textView, textViewParams);

        // SignPad
        LinearLayout linearLayout1 = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(screenWidth, screenHeight);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayout1, layoutParams1);

        View view = new View(this);
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * dp));
        String BackgroundColor2 = "#888888";
        view.setBackgroundColor(Color.parseColor(BackgroundColor2));
        linearLayout.addView(view, viewParams);

        SeekBar seekBar = new SeekBar(this);
        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        seekBar.setMax(100);
        seekBarParams.setMarginEnd((int) (5 * dp));
        seekBarParams.setMarginStart((int) (5 * dp));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                stroke = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(), "" + stroke, Toast.LENGTH_SHORT).show();
                spenSettingPenInfo.size = stroke;
                spenSurfaceView.setPenSettingInfo(spenSettingPenInfo);
            }
        });
        linearLayout.addView(seekBar, seekBarParams);

        LinearLayout linearLayout2 = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button button_Save = new Button(this);
        LinearLayout.LayoutParams button_SaveParams = new LinearLayout.LayoutParams((int) (90 * dp), ViewGroup.LayoutParams.WRAP_CONTENT);
        button_Save.setText("OK");
        button_SaveParams.gravity = Gravity.CENTER;
        button_SaveParams.weight = 1.0f;
        button_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_Save_event(view);
            }
        });
        linearLayout2.addView(button_Save, button_SaveParams);

        Button button_Clear = new Button(this);
        LinearLayout.LayoutParams button_ClearParams = new LinearLayout.LayoutParams((int) (90 * dp), ViewGroup.LayoutParams.WRAP_CONTENT);
        button_Clear.setText("Clear");
        button_ClearParams.gravity = Gravity.CENTER;
        button_ClearParams.weight = 1.0f;
        button_Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_Clear_event(view);
            }
        });
        linearLayout2.addView(button_Clear, button_ClearParams);

        Button button_Cancel = new Button(this);
        LinearLayout.LayoutParams button_CancelParams = new LinearLayout.LayoutParams((int) (90 * dp), ViewGroup.LayoutParams.WRAP_CONTENT);
        button_Cancel.setText("Cancel");
        button_CancelParams.gravity = Gravity.CENTER;
        button_CancelParams.weight = 1.0f;
        button_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_Cancel_event(view);
            }
        });
        linearLayout2.addView(button_Cancel, button_CancelParams);

        linearLayout.addView(linearLayout2, layoutParams2);

        setContentView(linearLayout, layoutParams);

        // Initialize Spen
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            if( processUnsupportedException(e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(context, StringResource.INITIALIZE_NOT_SPEN_MSG, Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

        // Create Spen View
        spenSurfaceView = new SpenSurfaceView(context);
        spenSurfaceView.setZoomable(false);
        if (spenSurfaceView == null) {
            Toast.makeText(context, StringResource.CREATE_NOT_SPEN_VIEW_MSG, Toast.LENGTH_SHORT).show();
            finish();
        }
        linearLayout1.addView(spenSurfaceView);

        // Create Spen Setting Pen Info
        spenSettingPenInfo = new SpenSettingPenInfo();

        // Get the dimension of the device screen.
//        Display display = getWindowManager().getDefaultDisplay();
//        Rect rect = new Rect();
//        display.getRectSize(rect);

        // Create SpenNoteDoc
        try {
            spenNoteDoc = new SpenNoteDoc(context, screenWidth, screenHeight);
//                    new SpenNoteDoc(context, rect.width(), rect.height());

        } catch (IOException e) {
            Toast.makeText(context, StringResource.CREATE_NOT_SPENNOTEDOC_MSG, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        this.setPage();

        if(isSpenFeatureEnabled == false) {
            spenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(context, StringResource.DEVICE_NOT_SUPPORTED_MSG,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {
        e.printStackTrace();
        int errType = e.getType();
        // If the device is not a Samsung device or if the device does not support Pen.
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            Toast.makeText(context, StringResource.DEVICE_NOT_SUPPORTED_MSG, Toast.LENGTH_SHORT).show();
            finish();
        }
        else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            // If SpenSDK APK is not installed.
            showAlertDialog(StringResource.LIBRARY_NOT_INSTALLED_MSG, true);
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            // SpenSDK APK must be updated.
            showAlertDialog(StringResource.LIBRARY_UPDATE_IS_REQUIRED_MSG, true);
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            // Update of SpenSDK APK to an available new version is recommended.
            showAlertDialog(StringResource.LIBRARY_UPDATE_IS_RECOMMENDED_MSG, false);
            return false;
        }
        return true;
    }

    private void showAlertDialog(String msg, final boolean closeActivity) {

        AlertDialog.Builder dlg = new AlertDialog.Builder(context);
        dlg.setIcon(getResources().getDrawable(
                android.R.drawable.ic_dialog_alert));
        dlg.setTitle("알림")    // Upgrade Notification
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                // Go to the market website and install/update APK.
                                Uri uri = Uri.parse(StringResource.MARKETURL);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                                dialog.dismiss();
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                if(closeActivity == true) {
                                    // Terminate the activity if APK is not installed.
                                    finish();
                                }
                                dialog.dismiss();
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if(closeActivity == true) {
                            // Terminate the activity if APK is not installed.
                            finish();
                        }
                    }
                })
                .show();
        dlg = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (spenSurfaceView != null) {
            spenSurfaceView.close();
            spenSurfaceView = null;
        }

        if(spenNoteDoc != null) {
            try {
                spenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            spenNoteDoc = null;
        }
    };

    public void setPage() {
        // Add a Page to NoteDoc, get an instance, and set it to the member variable.
        spenPageDoc = spenNoteDoc.appendPage();
        spenPageDoc.setBackgroundColor(0xFFFFFFFF);
        spenPageDoc.clearHistory();

        // Set PageDoc to View.
        spenSurfaceView.setPageDoc(spenPageDoc, true);
    }

    public void btn_Save_event(View v) {      // 서명 html canvas 로 전달
        Intent intent = new Intent();
        intent.putExtra("result", "Save");
        intent.putExtra("data", saveSign());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void btn_Clear_event(View v) {       // 지우기
        this.setPage();
    }

    public void btn_Cancel_event(View v) {      // Screen 화면 닫기
        Intent intent = new Intent();
        intent.putExtra("result", "Cancel");
        setResult(RESULT_OK, intent);
        finish();
    }

    public String saveSign() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            spenSurfaceView.captureCurrentView(true).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            return StringResource.BASE + Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
