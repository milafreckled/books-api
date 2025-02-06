package org.liudmylamalomuzh.service;

import org.liudmylamalomuzh.dto.LoginUserDto;
import org.liudmylamalomuzh.dto.RegisterUserDto;
import org.liudmylamalomuzh.entity.Privilege;
import org.liudmylamalomuzh.entity.Role;
import org.liudmylamalomuzh.entity.User;
import org.liudmylamalomuzh.repository.RoleRepository;
import org.liudmylamalomuzh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Service("userDetailsService")
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSource messages;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return new org.springframework.security.core.userdetails.User(
                    " ", " ", true, true, true, true,
                    getAuthorities(Collections.singletonList(
                            roleRepository.findByName("ROLE_USER"))));
        }

        return new User(
                user.get().getEmail(), user.get().getPassword(), user.get().getFullName(),
                new Date(), user.get().getRoles());
    }

//    private Collection<? extends GrantedAuthority> getAuthorities(
//            Collection<Role> roles) {
//
//        return getGrantedAuthorities(getPrivileges(roles));
//    }

    private List<String> getPrivileges(Collection<Role> roles) {

        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        for (Role role : roles) {
            privileges.add(role.getName());
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<Role> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role: roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            authorities.addAll(role.getPrivileges()
                    .stream()
                    .map(p -> new SimpleGrantedAuthority(p.getName()))
                    .toList());
        }

        return authorities;
    }

    @Transactional
    public void updateUser(Long id, RegisterUserDto userDto, String loggedInUserEmail) throws AccessDeniedException {
        // Fetch the user to be updated
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if the logged-in user matches the user being updated
        if (!user.getEmail().equals(loggedInUserEmail)) {
            throw new AccessDeniedException("You can only edit your own data");
        }

        // Update user details
        if (userDto.getFullName() != null) {
            user.setFullName(userDto.getFullName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        userRepository.save(user); // Save changes
    }
}
