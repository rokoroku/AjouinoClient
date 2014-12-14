package kr.ac.ajou.ajouinoclient.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.ApiException;
import kr.ac.ajou.ajouinoclient.util.GcmUtils;
import kr.ac.ajou.ajouinoclient.persistent.PreferenceManager;


/**
 * A login screen that offers login
 */
public class LoginActivity extends ActionBarActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mHostAddrView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mHostAddrView = (EditText) findViewById(R.id.hostaddr);
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        PreferenceManager preferenceManager = PreferenceManager.getInstance();
        String hostAddr = preferenceManager.getString(PreferenceManager.PREFERENCE_HOST_ADDRESS);
        String username = preferenceManager.getString(PreferenceManager.PREFERENCE_USERNAME);
        String password = preferenceManager.getString(PreferenceManager.PREFERENCE_PASSWORD);

        if(hostAddr != null) mHostAddrView.setText(hostAddr);
        if(username != null) mUsernameView.setText(username);
        if(password != null) mPasswordView.setText(password);

        Button mSinginButton = (Button) findViewById(R.id.sign_in_button);
        mSinginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mHostAddrView.setError(null);
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String hostAddr = mHostAddrView.getText().toString();
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(hostAddr)) {
            mHostAddrView.setError(getString(R.string.error_field_required));
            focusView = mHostAddrView;
            cancel = true;
        } else if (isHostAddrValid(hostAddr)) {
            mHostAddrView.setError(getString(R.string.error_invalid_host_address));
            focusView = mHostAddrView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(hostAddr, username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isHostAddrValid(String hostAddr) {
        return URLUtil.isValidUrl(hostAddr);
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mHostAddr;
        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String hostAddr, String username, String password) {
            mHostAddr = hostAddr;
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            String authToken = Base64.encodeToString((mUsername + ":" + mPassword).getBytes(), Base64.NO_WRAP);
            try {
                ApiCaller apiCaller = new ApiCaller(mHostAddr);
                if(apiCaller.authenticate(authToken)) {
                    PreferenceManager preferenceManager = PreferenceManager.getInstance();
                    preferenceManager.put(PreferenceManager.PREFERENCE_HOST_ADDRESS, mHostAddr);
                    preferenceManager.put(PreferenceManager.PREFERENCE_USERNAME, mUsername);
                    preferenceManager.put(PreferenceManager.PREFERENCE_PASSWORD, mPassword);
                    ApiCaller.setStaticInstance(apiCaller);
                    return 200;
                }
            } catch (ApiException e) {
                e.printStackTrace();
                return e.getErrorCode();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(final Integer statusCode) {
            mAuthTask = null;
            showProgress(false);

            switch (statusCode) {
                case 200:
                    GcmUtils.register(getApplicationContext());
                    Intent intent = new Intent(LoginActivity.this, DeviceListActivity.class);
                    startActivity(intent);
                    finish();
                    break;

                case ApiException.ERROR_BAD_REQUEST:
                case ApiException.ERROR_SERVICE_UNAVAILABLE:
                    mHostAddrView.setError(getString(R.string.error_host_unreachable));
                    mHostAddrView.requestFocus();
                    break;

                case ApiException.ERROR_UNAUTHORIZED:
                default:
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



