package com.tourismsv.security;

import com.tourismsv.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        var oAuth2Token = (OAuth2AuthenticationToken) authentication;
        var attributes = oAuth2Token.getPrincipal().getAttributes();
        var email = (String) attributes.get("email");
        var name = (String) attributes.get("name");
        var picture = (String) attributes.get("picture");

        var authResponse = authService.loginWithGoogle(email, name, picture);

        var redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", authResponse.accessToken())
                .queryParam("refreshToken", authResponse.refreshToken())
                .queryParam("expiresIn", authResponse.expiresIn())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
