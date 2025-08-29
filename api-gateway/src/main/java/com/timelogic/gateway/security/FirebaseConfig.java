package com.timelogic.gateway.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.FileInputStream;

/**
 * Inicializa Firebase Admin usando GOOGLE_APPLICATION_CREDENTIALS.
 * Se desactiva en el perfil "test".
 */
@Configuration
@Profile("!test")
public class FirebaseConfig {

  @PostConstruct
  public void init() throws Exception {
    if (FirebaseApp.getApps().isEmpty()) {
      String credPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
      if (credPath == null || credPath.isBlank()) {
        throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS no está definido");
      }
      try (FileInputStream serviceAccount = new FileInputStream(credPath)) {
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
        FirebaseApp.initializeApp(options);
      }
    }
  }
}
