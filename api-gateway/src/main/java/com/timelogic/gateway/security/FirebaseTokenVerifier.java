package com.timelogic.gateway.security;

import com.google.firebase.auth.FirebaseToken;

/**
 * Abstracción para verificar ID Tokens de Firebase (facilita testear).
 */
public interface FirebaseTokenVerifier {
  FirebaseToken verify(String idToken) throws Exception;
}
