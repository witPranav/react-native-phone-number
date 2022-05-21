package com.androidphonenumber;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.androidphonenumber.PhoneNumberConstants;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PhoneNumberModule extends ReactContextBaseJavaModule
{
    private static final int PHONE_NUMBER_REQUEST_CODE = 1;
    private static final int HINT_PHONE_NUMBER_REQUEST_CODE = 2;

    private Callback callback;
    private GoogleApiClient googleApiClient;
    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener()
    {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent)
        {
            if (requestCode == PHONE_NUMBER_REQUEST_CODE)
            {
                WritableMap params = Arguments.createMap();
                if (resultCode == RESULT_OK)
                {
                    try
                    {
                        String phoneNumber = Identity.getSignInClient(activity).getPhoneNumberFromIntent(intent);
                        sendMessageToJS(PhoneNumberConstants.SUCCESS, phoneNumber);
                    } catch (ApiException e)
                    {
                        sendMessageToJS(PhoneNumberConstants.ERROR, e.toString());
                    }
                } else if (resultCode == RESULT_CANCELED)
                {
                    sendMessageToJS(PhoneNumberConstants.CANCELLED, PhoneNumberConstants.CANCEL_MESSAGE);
                } else
                {
                    sendMessageToJS(PhoneNumberConstants.ERROR, PhoneNumberConstants.GENERIC_ERROR);
                }
            } else if (requestCode == HINT_PHONE_NUMBER_REQUEST_CODE)
            {
                WritableMap params = Arguments.createMap();
                if (resultCode == RESULT_OK)
                {
                    Credential cred = intent.getParcelableExtra(Credential.EXTRA_KEY);
                    if (callback != null)
                    {
                        sendMessageToJS(PhoneNumberConstants.SUCCESS, cred.getId());
                    }
                } else if (resultCode == RESULT_CANCELED)
                {
                    sendMessageToJS(PhoneNumberConstants.CANCELLED, PhoneNumberConstants.CANCEL_MESSAGE);
                } else
                {
                    sendMessageToJS(PhoneNumberConstants.ERROR, PhoneNumberConstants.GENERIC_ERROR);
                }
            }
        }
    };

    PhoneNumberModule(ReactApplicationContext reactContext)
    {
        super(reactContext);
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @NonNull
    @Override
    public String getName()
    {
        return PhoneNumberConstants.PACKAGE_NAME;
    }

    @ReactMethod
    public void requestPhoneNumber(Callback cb)
    {
        callback = cb;
        Activity currentActivity = getCurrentActivity();
        GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();
        Identity.getSignInClient(currentActivity).getPhoneNumberHintIntent(request).addOnSuccessListener(result -> {
            try
            {
                currentActivity.startIntentSenderForResult(result.getIntentSender(), PHONE_NUMBER_REQUEST_CODE, null, 0, 0, 0);
            } catch (Exception e)
            {
                sendMessageToJS(PhoneNumberConstants.ERROR, e.toString());
            }
        }).addOnFailureListener(res -> {
            sendMessageToJS(PhoneNumberConstants.ERROR, res.toString());
        }).addOnCanceledListener(() -> {
            sendMessageToJS(PhoneNumberConstants.CANCELLED, PhoneNumberConstants.CANCEL_MESSAGE);
        });
    }

    private void sendMessageToJS(String status, String data)
    {
        WritableMap params = Arguments.createMap();
        params.putString(PhoneNumberConstants.STATUS, status);
        params.putString(PhoneNumberConstants.DATA, data);

        callback.invoke(params);
        callback = null;
    }

    @Override
    public Map<String, Object> getConstants()
    {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(PhoneNumberConstants.RN_SUCCESS, PhoneNumberConstants.SUCCESS);
        constants.put(PhoneNumberConstants.RN_ERROR, PhoneNumberConstants.ERROR);
        constants.put(PhoneNumberConstants.RN_CANCELLED, PhoneNumberConstants.CANCELLED);
        return constants;
    }

    @ReactMethod
    public void hintRequestPhoneNumber(Callback cb)
    {
        Activity currentActivity = getCurrentActivity();
        googleApiClient = new GoogleApiClient.Builder(currentActivity)
                .addApi(Auth.CREDENTIALS_API)
                .build();
        callback = cb;
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();
        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest);
        try
        {
            currentActivity.startIntentSenderForResult(intent.getIntentSender(), HINT_PHONE_NUMBER_REQUEST_CODE, null, 0, 0, 0);
        } catch (Exception e)
        {
            sendMessageToJS(PhoneNumberConstants.ERROR, e.toString());
        }
    }

}
