package com.cm.timovil2.bl.utilities.zebra_datawedge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cm.timovil2.R;

import java.util.Set;

import static android.provider.ContactsContract.Intents.Insert.ACTION;

public class DWScanner {

    public static final String CAMERA_TYPE = "CAMARA";
    public static final String ZEBRAESCANER_TYPE = "ZEBRAESCANER";

    // DataWedge Sample supporting DataWedge APIs up to DW 7.0
    private static final String EXTRA_PROFILENAME = "DWTiMovil";

    // DataWedge Extras
    private static final String EXTRA_GET_VERSION_INFO = "com.symbol.datawedge.api.GET_VERSION_INFO";
    private static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    private static final String EXTRA_KEY_APPLICATION_NAME = "com.symbol.datawedge.api.APPLICATION_NAME";
    private static final String EXTRA_KEY_NOTIFICATION_TYPE = "com.symbol.datawedge.api.NOTIFICATION_TYPE";
    private static final String EXTRA_SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";
    private static final String EXTRA_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION";
    private static final String EXTRA_REGISTER_NOTIFICATION = "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION";
    private static final String EXTRA_UNREGISTER_NOTIFICATION = "com.symbol.datawedge.api.UNREGISTER_FOR_NOTIFICATION";
    private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";

    private static final String EXTRA_RESULT_NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private static final String EXTRA_KEY_VALUE_SCANNER_STATUS = "SCANNER_STATUS";
    private static final String EXTRA_KEY_VALUE_PROFILE_SWITCH = "PROFILE_SWITCH";
    private static final String EXTRA_KEY_VALUE_CONFIGURATION_UPDATE = "CONFIGURATION_UPDATE";
    private static final String EXTRA_KEY_VALUE_NOTIFICATION_STATUS = "STATUS";
    private static final String EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME = "PROFILE_NAME";
    private static final String EXTRA_SEND_RESULT = "SEND_RESULT";

    private static final String EXTRA_EMPTY = "";

    private static final String EXTRA_RESULT_GET_VERSION_INFO = "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO";
    private static final String EXTRA_RESULT = "RESULT";
    private static final String EXTRA_RESULT_INFO = "RESULT_INFO";
    private static final String EXTRA_COMMAND = "COMMAND";

    // DataWedge Actions
    private static final String ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION";
    private static final String ACTION_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";
    private static final String ACTION_RESULT = "com.symbol.datawedge.api.RESULT_ACTION";

    // private variables
    private Boolean bRequestSendResult = false;
    private final String TAG = "DWScanner";
    private final ContextWrapper contextWrapper;
    private OnDataWedgeScannerDecodedData decodedDataListener;
    private boolean alreadyScaneedAvalue;

    public DWScanner(ContextWrapper contextWrapper){
        this.contextWrapper = contextWrapper;
        alreadyScaneedAvalue = false;
        registerForScanner();
    }

    public void setDecodedDataListener(OnDataWedgeScannerDecodedData decodedDataListener) {
        this.decodedDataListener = decodedDataListener;
    }

    private void registerForScanner(){
        // Register for status change notification
        // Use REGISTER_FOR_NOTIFICATION: http://techdocs.zebra.com/datawedge/latest/guide/api/registerfornotification/
        Bundle b = new Bundle();
        b.putString(EXTRA_KEY_APPLICATION_NAME, contextWrapper.getPackageName());
        b.putString(EXTRA_KEY_NOTIFICATION_TYPE, "SCANNER_STATUS");     // register for changes in scanner status
        sendDataWedgeIntentWithExtra(EXTRA_REGISTER_NOTIFICATION, b);

        registerReceivers();

        // Get DataWedge version
        // Use GET_VERSION_INFO: http://techdocs.zebra.com/datawedge/latest/guide/api/getversioninfo/
        sendDataWedgeIntentWithExtra(EXTRA_GET_VERSION_INFO, EXTRA_EMPTY);// must be called after registering BroadcastReceiver
    }

    // Create filter for the broadcast intent
    private void registerReceivers() {

        Log.d(TAG, "registerReceivers()");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RESULT_NOTIFICATION);   // for notification result
        filter.addAction(ACTION_RESULT);                // for error code result
        filter.addCategory(Intent.CATEGORY_DEFAULT);    // needed to get version info

        // register to received broadcasts via DataWedge scanning
        filter.addAction(contextWrapper.getResources().getString(R.string.activity_intent_filter_action));
        filter.addAction(contextWrapper.getResources().getString(R.string.activity_action_from_service));
        contextWrapper.registerReceiver(myBroadcastReceiver, filter);
    }

    public void stopScanner(){
        try{
            stopSoftScanTrigger();
            contextWrapper.unregisterReceiver(myBroadcastReceiver);
            unRegisterScannerStatus();
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    // Unregister scanner status notification
    private void unRegisterScannerStatus() {
        Log.d(TAG, "unRegisterScannerStatus()");
        Bundle b = new Bundle();
        b.putString(EXTRA_KEY_APPLICATION_NAME, contextWrapper.getPackageName());
        b.putString(EXTRA_KEY_NOTIFICATION_TYPE, EXTRA_KEY_VALUE_SCANNER_STATUS);
        Intent i = new Intent();
        i.setAction(ACTION);
        i.putExtra(EXTRA_UNREGISTER_NOTIFICATION, b);
        contextWrapper.sendBroadcast(i);
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "DataWedge Action:" + action);
            // Get DataWedge version info
            if (intent.hasExtra(EXTRA_RESULT_GET_VERSION_INFO)){
                Bundle versionInfo = intent.getBundleExtra(EXTRA_RESULT_GET_VERSION_INFO);
                String DWVersion = versionInfo.getString("DATAWEDGE");
                Log.i(TAG, "DataWedge Version: " + DWVersion);
            }

            if (action != null && action.equals(contextWrapper.getResources().getString(R.string.activity_intent_filter_action)))
            {
                //  Received a barcode scan
                try
                {
                    stopSoftScanTrigger();
                    String decodedData = scanResult(intent);
                    if(decodedData != null && !TextUtils.isEmpty(decodedData)){
                        //Toast.makeText(contextWrapper, "Decoded: " + decodedData, Toast.LENGTH_SHORT).show();
                        if(decodedDataListener != null && !alreadyScaneedAvalue){
                            alreadyScaneedAvalue = true;
                            decodedDataListener.onDecodedData(decodedData);
                        }
                    }
                } catch (Exception e){
                    //  Catch error if the UI does not exist when we receive the broadcast...
                }
            }

            else if (action != null && action.equals(ACTION_RESULT)) {
                // Register to receive the result code
                if ((intent.hasExtra(EXTRA_RESULT)) && (intent.hasExtra(EXTRA_COMMAND))) {
                    String command = intent.getStringExtra(EXTRA_COMMAND);
                    String result = intent.getStringExtra(EXTRA_RESULT);
                    StringBuilder info = new StringBuilder();

                    if (intent.hasExtra(EXTRA_RESULT_INFO))
                    {
                        Bundle result_info = intent.getBundleExtra(EXTRA_RESULT_INFO);
                        Set<String> keys = result_info.keySet();
                        for (String key : keys) {
                            Object object = result_info.get(key);
                            if (object instanceof String) {
                                info.append(key).append(": ").append(object).append("\n");
                            } else if (object instanceof String[]) {
                                String[] codes = (String[]) object;
                                for (String code : codes) {
                                    info.append(key).append(": ").append(code).append("\n");
                                }
                            }
                        }
                        String resumenInfo = "Command: " + command + "\n" + "Result: " + result + "\n" + "Result Info: " + info.toString() + "\n";
                        Log.d(TAG, resumenInfo);
                        //Toast.makeText(contextWrapper, "Error Resulted. " + resumenInfo, Toast.LENGTH_LONG).show();
                    }
                }
            }

            // Register for scanner change notification
            else if (action != null && action.equals(ACTION_RESULT_NOTIFICATION)) {
                if (intent.hasExtra(EXTRA_RESULT_NOTIFICATION)) {
                    Bundle extras = intent.getBundleExtra(EXTRA_RESULT_NOTIFICATION);
                    String notificationType = extras.getString(EXTRA_RESULT_NOTIFICATION_TYPE);
                    if (notificationType != null) {
                        switch (notificationType) {
                            case EXTRA_KEY_VALUE_SCANNER_STATUS:
                                // Change in scanner status occurred
                                String status = extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_STATUS);
                                String displayScannerStatusText = status + ", profile: " + extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME);
                                Log.i(TAG, "Scanner status: " + displayScannerStatusText);
                                //Toast.makeText(contextWrapper, "Scanner status: " + displayScannerStatusText, Toast.LENGTH_LONG).show();
                                if(status != null && status.equals("WAITING")){
                                    startSoftScanTrigger();
                                    Toast.makeText(contextWrapper, "START SCANNER", Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case EXTRA_KEY_VALUE_PROFILE_SWITCH:
                                // Received change in profile
                                // For future enhancement
                                break;

                            case  EXTRA_KEY_VALUE_CONFIGURATION_UPDATE:
                                // Configuration change occurred
                                // For future enhancement
                                break;
                        }
                    }
                }
            }
        }
    };

    // Create profile
    public void createProfile (){

        // Send DataWedge intent with extra to create profile
        // Use CREATE_PROFILE: http://techdocs.zebra.com/datawedge/latest/guide/api/createprofile/
        sendDataWedgeIntentWithExtra(EXTRA_CREATE_PROFILE, EXTRA_PROFILENAME);

        // Configure created profile to apply to this app
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", EXTRA_PROFILENAME);
        profileConfig.putString("PROFILE_ENABLED", "true");
        profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");  // Create profile if it does not exist

        // Configure barcode input plugin
        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        barcodeConfig.putString("RESET_CONFIG", "true"); //  This is the default
        Bundle barcodeProps = new Bundle();
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);

        // Associate profile with this app
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", contextWrapper.getPackageName());
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
        profileConfig.remove("PLUGIN_CONFIG");

        // Apply configs
        // Use SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
        sendDataWedgeIntentWithExtra(EXTRA_SET_CONFIG, profileConfig);

        // Configure intent output for captured data to be sent to this app
        Bundle intentConfig = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT");
        intentConfig.putString("RESET_CONFIG", "true");
        Bundle intentProps = new Bundle();
        intentProps.putString("intent_output_enabled", "true");
        intentProps.putString("intent_action", "com.cm.timovil2.ACTION");
        intentProps.putString("intent_delivery", "2");
        intentConfig.putBundle("PARAM_LIST", intentProps);
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig);
        sendDataWedgeIntentWithExtra(EXTRA_SET_CONFIG, profileConfig);

        Log.d(TAG, "Created profile.  Check DataWedge app UI.");
    }

    public void setDecoders (){
        // Main bundle properties
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", EXTRA_PROFILENAME);
        profileConfig.putString("PROFILE_ENABLED", "true");
        profileConfig.putString("CONFIG_MODE", "UPDATE");  // Update specified settings in profile

        // PLUGIN_CONFIG bundle properties
        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        barcodeConfig.putString("RESET_CONFIG", "true");

        // PARAM_LIST bundle properties
        Bundle barcodeProps = new Bundle();
        barcodeProps.putString("scanner_selection", "auto");
        barcodeProps.putString("scanner_input_enabled", "true");
        barcodeProps.putString("decoder_code128", "true");
        barcodeProps.putString("decoder_code39", "true");
        barcodeProps.putString("decoder_ean13", "true");
        barcodeProps.putString("decoder_upca", "true");

        // Bundle "barcodeProps" within bundle "barcodeConfig"
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
        // Place "barcodeConfig" bundle within main "profileConfig" bundle
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);

        // Create APP_LIST bundle to associate app with profile
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", contextWrapper.getPackageName());
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
        sendDataWedgeIntentWithExtra(EXTRA_SET_CONFIG, profileConfig);

        Log.d(TAG, "In profile " + EXTRA_PROFILENAME + " the decoders are being set: \nCode128 " + "\nCode39 " + "\nEAN13 " + "\nUPCA ");
    }

    // Use SOFT_SCAN_TRIGGER: http://techdocs.zebra.com/datawedge/latest/guide/api/softscantrigger/
    private void startSoftScanTrigger(){
        //sendDataWedgeIntentWithExtra(EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING");
        sendDataWedgeIntentWithExtra(EXTRA_SOFT_SCAN_TRIGGER, "START_SCANNING");
    }

    private void stopSoftScanTrigger(){
        //sendDataWedgeIntentWithExtra(EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING");
        sendDataWedgeIntentWithExtra(EXTRA_SOFT_SCAN_TRIGGER, "STOP_SCANNING");
    }

    private void sendDataWedgeIntentWithExtra(String extraKey, Bundle extras) {
        Intent dwIntent = new Intent();
        dwIntent.setAction(ACTION_DATAWEDGE);
        dwIntent.putExtra(extraKey, extras);
        if (bRequestSendResult)
            dwIntent.putExtra(EXTRA_SEND_RESULT, "true");
        contextWrapper.sendBroadcast(dwIntent);
    }

    private void sendDataWedgeIntentWithExtra(String extraKey, String extraValue) {
        Intent dwIntent = new Intent();
        dwIntent.setAction(ACTION_DATAWEDGE);
        dwIntent.putExtra(extraKey, extraValue);
        if (bRequestSendResult)
            dwIntent.putExtra(EXTRA_SEND_RESULT, "true");
        contextWrapper.sendBroadcast(dwIntent);
    }

    private String scanResult(Intent initiatingIntent){
        return initiatingIntent.getStringExtra(contextWrapper.getResources().getString(R.string.datawedge_intent_key_data));
    }
}
