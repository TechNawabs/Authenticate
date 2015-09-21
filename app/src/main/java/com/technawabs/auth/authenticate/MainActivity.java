package com.technawabs.auth.authenticate;

import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.StaticLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.PersonBuffer;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.util.ServiceException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;


    String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(Plus.API)
//                .addScope(Plus.SCOPE_PLUS_LOGIN)
//                .build();
//
        // Build GoogleApiClient to request access to the basic user profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();


    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onConnected(Bundle bundle) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.

        email = Plus.AccountApi.getAccountName(mGoogleApiClient);


//        Make the loadVisiblePeople request after the GoogleApiClient is connected.
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);

        List<String> userIds = new ArrayList<String>();
        userIds.add("107117483540235115863");
        userIds.add("+LarryPage");
        Plus.PeopleApi.load(mGoogleApiClient, userIds).setResultCallback(this);


    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {


        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }

    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }


    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try {
                int count = personBuffer.getCount();
                for (int i = 0; i < count; i++) {
                    //Log.d(TAG, "Display name: " + personBuffer.get(i).getDisplayName());
                    Toast.makeText(getApplicationContext(), "Display name: " + personBuffer.get(i).getDisplayName() + peopleData.getStatus(), Toast.LENGTH_LONG).show();
                }
            } finally {
                personBuffer.release();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Error requesting visible circles: " + peopleData.getStatus() + email, Toast.LENGTH_LONG).show();
            //Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
        }

    }



//    class UserInfo {
//        String id;
//        String email;
//        String verified_email;
//    }
//
//    final String account = Plus.AccountApi.getAccountName(mGoogleApiClient);
//
//
//    AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
//
//        @Override
//        protected UserInfo doInBackground(Void... params) {
//            HttpURLConnection urlConnection = null;
//
//            try {
//                URL url = new URL("https://www.googleapis.com/plus/v1/people/me");
//                String sAccessToken = GoogleAuthUtil.getToken(EmailTest.this, account,
//                        "oauth2:" + Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/plus.profile.emails.read");
//
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestProperty("Authorization", "Bearer " + sAccessToken);
//
//                String content = CharStreams.toString(new InputStreamReader(urlConnection.getInputStream(),
//                        Charsets.UTF_8));
//
//                if (!TextUtils.isEmpty(content)) {
//                    JSONArray emailArray =  new JSONObject(content).getJSONArray("emails");
//
//                    for (int i = 0; i < emailArray.length; i++) {
//                        JSONObject obj = (JSONObject)emailArray.get(i);
//
//                        // Find and return the primary email associated with the account
//                        if (obj.getString("type") == "account") {
//                            return obj.getString("value");
//                        }
//                    }
//                }
//            } catch (UserRecoverableAuthException userAuthEx) {
//                // Start the user recoverable action using the intent returned by
//                // getIntent()
//                startActivityForResult(userAuthEx.getIntent(), RC_SIGN_IN);
//                return;
//            } catch (Exception e) {
//                // Handle error
//                // e.printStackTrace(); // Uncomment if needed during debugging.
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String info) {
//            // Store or use the user's email address
//        }
//
//    };
//
//    task.execute();


        //Errors Hard to decide use or not

//    private class GetGoogleContacts extends AsyncTask<String,String,List<ContactEntry>>{
//
//
//        private ProgressDialog pDialog;
//        private Context context;
//
//        public GetGoogleContacts(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            pDialog = new ProgressDialog(context);
//            pDialog.setMessage("Authenticated. Getting Google Contacts ...");
//            pDialog.setIndeterminate(false);
//            pDialog.setCancelable(true);
//            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    finish();
//                }
//            });
//            pDialog.show();
//
//        }
//
//        @Override
//        protected List<ContactEntry> doInBackground(String... params) {
//
//            HttpURLConnection urlConnection = null;
//
//            try {
//                URL url = new URL("https://www.googleapis.com/plus/v1/people/me");
//                Account acn = null;
//                acn.getAccountName(mGoogleApiClient);
//                String sAccessToken = GoogleAuthUtil.getToken(MainActivity.this, (android.accounts.Account) acn, "oauth2:" + Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/plus.profile.emails.read");
//
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestProperty("Authorization", "Bearer " + sAccessToken);
//
//
//                InputStreamReader reader = new InputStreamReader(supplier.get(), Charsets.UTF_8);
//                String content = CharStreams.toString(new InputStreamReader(urlConnection.getInputStream(), Charsets.UTF_8));
//
//
//                if (!TextUtils.isEmpty(content)) {
//                    JSONArray emailArray =  new JSONObject(content).getJSONArray("emails");
//
//                    for (int i = 0; i < emailArray.length(); i++) {
//                        JSONObject obj = (JSONObject)emailArray.get(i);
//
//                        // Find and return the primary email associated with the account
//                        if (obj.getString("type") == "account") {
//                            return obj.getString("value");
//                        }
//                    }
//                }
//            } catch (UserRecoverableAuthException userAuthEx) {
//                // Start the user recoverable action using the intent returned by
//                // getIntent()
//                startActivityForResult(userAuthEx.getIntent(), RC_SIGN_IN);
//                return;
//            } catch (Exception e) {
//                // Handle error
//                // e.printStackTrace(); // Uncomment if needed during debugging.
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//            }
//
//            return null;
//        }
//
//
//    }
}
