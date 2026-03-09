package com.frytes.cloudstorage.users.security;

import com.frytes.cloudstorage.users.model.Provider;
import com.frytes.cloudstorage.users.model.Role;
import com.frytes.cloudstorage.users.model.User;
import com.frytes.cloudstorage.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String username = oAuth2User.getAttribute("email");

        if (username == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() != Provider.GOOGLE) {
                user.setProvider(Provider.GOOGLE);
                userRepository.save(user);
            }
            log.info("Существующий пользователь вошел через Google: {}", username);
        } else {
            user = User.builder()
                    .username(username)
                    .password(null)
                    .provider(Provider.GOOGLE)
                    .role(Role.USER)
                    .build();
            user = userRepository.save(user);
            log.info("Зарегистрирован новый пользователь через Google: {}", username);
        }

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword() == null ? "" : user.getPassword(),
                oAuth2User.getAuthorities(),
                oAuth2User.getAttributes()
        );
    }
}