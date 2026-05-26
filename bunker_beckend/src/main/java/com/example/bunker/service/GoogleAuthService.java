package com.example.bunker.service;


import com.example.bunker.dto.User.UserResponse;
import com.example.bunker.model.AuthProvider;
import com.example.bunker.model.User;
import com.example.bunker.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;


@Service
public class GoogleAuthService {

    private static UserRepository userRepository;


    @Value("${google.client-id}")
    private String clientId;

    private GoogleIdToken.Payload verifyToken(String token) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(token);

        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google token");
        }

        return idToken.getPayload();
    }


    public UserResponse authenticate(String token) {
        GoogleIdToken.Payload payload = null;
        try {
            payload = verifyToken(token);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String googleId = payload.getSubject();
        String email    = payload.getEmail();
        String name     = (String) payload.get("name");

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGoogleId(googleId);
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setAuthProvider(AuthProvider.GOOGLE);
                    return userRepository.save(newUser);
                });

        //String jwt = jwtService.generateToken(user);
        return ;
    }

}
