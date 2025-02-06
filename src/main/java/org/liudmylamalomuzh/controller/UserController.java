package org.liudmylamalomuzh.controller;

import jakarta.validation.Valid;
import org.liudmylamalomuzh.dto.RegisterUserDto;
import org.liudmylamalomuzh.entity.User;
import org.liudmylamalomuzh.repository.UserRepository;
import org.liudmylamalomuzh.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    private final CustomUserDetailsService userService;

    public UserController(CustomUserDetailsService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "User deleted successfully");
                    response.put("id", id);

                    return ResponseEntity.ok().body(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "User not found");
                    errorResponse.put("id", id);

                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.status(HttpStatus.OK).body(userRepository.findAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Ensure the user is logged in
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid RegisterUserDto userDto,
            Principal principal) {
        System.out.println("NAZWA USERA: "+principal.getName());
        try {
            userService.updateUser(id, userDto, principal.getName());
            return ResponseEntity.ok("User updated successfully");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own data");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update user: " + e.getMessage());
        }
    }

}
