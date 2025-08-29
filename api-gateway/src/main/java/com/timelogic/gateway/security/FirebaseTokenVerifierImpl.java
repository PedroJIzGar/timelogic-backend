package com.timelogic.gateway.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

/**
 * Implementación real contra Firebase Admin SDK.
 */
@Component
public class FirebaseTokenVerifierImpl implements FirebaseTokenVerifier {
  @Override
  public FirebaseToken verify(String idToken) throws Exception {
    return FirebaseAuth.getInstance().verifyIdToken(idToken, true);
    // segundo parámetro = checkRevoked
  }
}
