# react-native-phone-number

## Getting started

`$ npm install react-native-phone-number --save`

## Note
- This module works only for android at the moment.

## How it works
-- This module provides 2 methods both of which provide a native overlay from which the user can select a phone number.
- requestPhoneNumber: which uses [GetPhoneNumberHintIntentRequest](https://developers.google.com/android/reference/com/google/android/gms/auth/api/identity/GetPhoneNumberHintIntentRequest) and [Identity](https://developers.google.com/android/reference/com/google/android/gms/auth/api/identity/Identity) APIs to provide the phone number.
&
- hintRequestPhoneNumber: which uses [GoogleApiClient](https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient) and [HintRequest](https://developers.google.com/android/reference/com/google/android/gms/auth/api/credentials/HintRequest) which are deprecated APIs to provide the phone number.

## Usage
- Goto app level build.gradle in android/app/build.gradle and add the following dependency.
```groovy
dependencies {
    ...
    implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.0.0"
    implementation "com.google.android.gms:play-services-auth:20.2.0" // Add this line
    }
```

```javascript
import PhoneNumber from 'react-native-phone-number';
    
// Get status constants from the module
const {STATUS_CANCELLED, STATUS_ERROR, STATUS_SUCCESS} = PhoneNumber.getConstants();

// Recommended method for obtaining the phone number
PhoneNumber.requestPhoneNumber((res) => {
      if(res.status === STATUS_SUCCESS){
        //process res.data
      }else if(res.status === STATUS_CANCELLED){
        //handle cancel res.data
      }else if(res.status === STATUS_ERROR){
        //handle error res.data
      }
});

// Deprecated method for obtaining the phone number
PhoneNumber.hintRequestPhoneNumber((res) => {
      if(res.status === STATUS_SUCCESS){
        //process res.data
      }else if(res.status === STATUS_CANCELLED){
        //handle cancel res.data
      }else if(res.status === STATUS_ERROR){
        //handle error res.data
      }
});
```
