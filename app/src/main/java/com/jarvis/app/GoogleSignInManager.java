package com.jarvis.app;

import android.app.Activity;
import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.security.SecureRandom;

public class GoogleSignInManager {

    /*
     * Web OAuth client ID.
     *
     * This is a client ID, not a client secret.
     * It is used as the audience for the Google ID token.
     */
    private static final String WEB_CLIENT_ID =
            "784714249372-tjuovghi43u13adj3519g2rjqg8r6i0o.apps.googleusercontent.com";

    private final Context context;
    private final CredentialManager credentialManager;
    private final Handler mainHandler;

    public GoogleSignInManager(Context context) {
        this.context = context.getApplicationContext();
        this.credentialManager =
                CredentialManager.create(this.context);
        this.mainHandler =
                new Handler(Looper.getMainLooper());
    }

    public interface Callback {
        void onSuccess(
                String idToken,
                String displayName,
                String email,
                String profilePictureUri
        );

        void onError(String message);
    }

    public void signIn(
            Activity activity,
            Callback callback
    ) {

        String nonce = generateNonce();

        GetGoogleIdOption googleIdOption =
                new GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(WEB_CLIENT_ID)
                        .setAutoSelectEnabled(false)
                        .setNonce(nonce)
                        .build();

        GetCredentialRequest request =
                new GetCredentialRequest.Builder()
                        .addCredentialOption(
                                googleIdOption
                        )
                        .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<
                        GetCredentialResponse,
                        GetCredentialException>() {

                    @Override
                    public void onResult(
                            GetCredentialResponse result
                    ) {

                        try {

                            Credential credential =
                                    result.getCredential();

                            if (!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                    .equals(
                                            credential.getType()
                                    )) {

                                callback.onError(
                                        "Google returned an unsupported credential."
                                );

                                return;
                            }

                            GoogleIdTokenCredential googleCredential =
                                    GoogleIdTokenCredential
                                            .createFrom(
                                                    credential.getData()
                                            );

                            callback.onSuccess(
                                    googleCredential.getIdToken(),
                                    googleCredential.getDisplayName(),
                                    googleCredential.getId(),
                                    googleCredential.getProfilePictureUri() == null
                                            ? null
                                            : googleCredential
                                                    .getProfilePictureUri()
                                                    .toString()
                            );

                        } catch (Exception e) {

                            callback.onError(
                                    "Google sign-in failed: "
                                            + safeMessage(e)
                            );
                        }
                    }

                    @Override
                    public void onError(
                            GetCredentialException e
                    ) {

                        callback.onError(
                                "Google sign-in failed: "
                                        + safeMessage(e)
                        );
                    }
                }
        );
    }

    private String generateNonce() {

        byte[] bytes =
                new byte[32];

        new SecureRandom().nextBytes(bytes);

        StringBuilder builder =
                new StringBuilder(
                        bytes.length * 2
                );

        for (byte value : bytes) {

            builder.append(
                    String.format(
                            "%02x",
                            value & 0xff
                    )
            );
        }

        return builder.toString();
    }

    private String safeMessage(
            Exception exception
    ) {

        String message =
                exception.getMessage();

        if (message == null ||
                message.trim().isEmpty()) {

            return exception
                    .getClass()
                    .getSimpleName();
        }

        return message;
    }
}
