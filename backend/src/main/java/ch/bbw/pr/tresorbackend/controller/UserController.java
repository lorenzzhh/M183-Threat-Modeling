package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.filter.JwtAuthFilter;
import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.LoginRateLimiterService;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptService;
import ch.bbw.pr.tresorbackend.service.SessionService;
import ch.bbw.pr.tresorbackend.service.UserService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserController
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/users")
public class UserController {

   private UserService userService;
   private PasswordEncryptService passwordService;
   private LoginRateLimiterService rateLimiter;
   private SessionService sessionService;
   private static final Logger logger = LoggerFactory.getLogger(UserController.class);

   // build create User REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping
   public ResponseEntity<String> createUser(@Valid @RequestBody RegisterUser registerUser, BindingResult bindingResult) {
      //captcha
      //todo add implementation

      System.out.println("UserController.createUser: captcha passed.");

      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
         System.out.println("UserController.createUser " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("UserController.createUser, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }
      System.out.println("UserController.createUser: input validation passed");

      //password validation
      //todo add implementation
      System.out.println("UserController.createUser, password validation passed");

      //transform registerUser to user
      //Role is always USER here - RegisterUser has no role field, so there
      //is no way for a client to request ADMIN on self-registration.
      User user = new User(
              null,
              registerUser.getFirstName(),
              registerUser.getLastName(),
              registerUser.getEmail(),
              passwordService.hashPassword(registerUser.getPassword()),
              Role.USER
      );

      User savedUser = userService.createUser(user);
      JsonObject obj = new JsonObject();
      if (savedUser != null) {
         System.out.println("UserController.createUser, user saved in db");
         obj.addProperty("answer", "User saved");
      } else {
         System.out.println("UserController.createUser, user not saved in db");
         obj.addProperty("answer", "User not saved");
      }
      String json = new Gson().toJson(obj);
      System.out.println("UserController.createUser " + json);
      return ResponseEntity.accepted().body(json);
   }

   // build get user by id REST API
   // http://localhost:8080/api/users/1
   // Only the authenticated user themselves may fetch their own profile.
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @GetMapping("{id}")
   public ResponseEntity<User> getUserById(@PathVariable("id") Long userId, HttpServletRequest request) {
      Long authUserId = currentUserId(request);
      if (!authUserId.equals(userId)) {
         return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      User user = userService.getUserById(userId);
      if (user == null) return ResponseEntity.notFound().build();
      return new ResponseEntity<>(user, HttpStatus.OK);
   }

   // Removed: "get all users" had no ownership check and leaked every
   // user's data to any caller. There is no legitimate use case for a
   // regular authenticated user to list all accounts, so this endpoint
   // was deleted rather than patched. Reintroduce only behind a real
   // admin role check if it's genuinely needed.

   // Build Update User REST API
   // http://localhost:8080/api/users/1
   // Users may only update their own profile.
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PutMapping("{id}")
   public ResponseEntity<User> updateUser(@PathVariable("id") Long userId,
                                          @RequestBody User user,
                                          HttpServletRequest request) {
      Long authUserId = currentUserId(request);
      if (!authUserId.equals(userId)) {
         return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      user.setId(userId);
      User updatedUser = userService.updateUser(user);
      if (updatedUser == null) return ResponseEntity.notFound().build();
      return new ResponseEntity<>(updatedUser, HttpStatus.OK);
   }

   // Build Delete User REST API
   // Users may only delete their own account.
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @DeleteMapping("{id}")
   public ResponseEntity<String> deleteUser(@PathVariable("id") Long userId, HttpServletRequest request) {
      Long authUserId = currentUserId(request);
      if (!authUserId.equals(userId)) {
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your account");
      }
      if( userService.deleteUser(userId))
         return new ResponseEntity<>("User successfully deleted!", HttpStatus.OK);
      return ResponseEntity.notFound().build();
   }

   // get user id by email
   // Public: needed pre-login (e.g. registration availability check).
   // Deliberately does not reveal anything beyond the numeric id.
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byemail")
   public ResponseEntity<String> getUserIdByEmail(@RequestBody EmailAdress email, BindingResult bindingResult) {
      System.out.println("UserController.getUserIdByEmail: " + email);
      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
         System.out.println("UserController.createUser " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("UserController.createUser, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }

      System.out.println("UserController.getUserIdByEmail: input validation passed");

      User user = userService.findByEmail(email.getEmail());
      if (user == null) {
         System.out.println("UserController.getUserIdByEmail, no user found with email: " + email);
         JsonObject obj = new JsonObject();
         obj.addProperty("message", "No user found with this email");
         String json = new Gson().toJson(obj);

         System.out.println("UserController.getUserIdByEmail, fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }
      System.out.println("UserController.getUserIdByEmail, user find by email");
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", user.getId());
      String json = new Gson().toJson(obj);
      System.out.println("UserController.getUserIdByEmail " + json);
      return ResponseEntity.accepted().body(json);
   }

   // login: verifies the password and hands out a session token that the
   // frontend must send as "Authorization: Bearer <token>" on every
   // subsequent request. That token - not any client-supplied
   // userId/email - is what the rest of the app uses to know who is
   // calling.
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/login")
   public ResponseEntity<LoginResponse> doLoginUser(@RequestBody LoginUser loginUser, BindingResult bindingResult,
                                                    HttpServletRequest request) {
      System.out.println("UserController.doLoginUser: " + loginUser);

      String ip = request.getRemoteAddr();
      if (rateLimiter.isBlocked(ip)) {
         logger.warn("UserController.doLoginUser: rate limit exceeded for IP {}", ip);
         return ResponseEntity.status(429).body(new LoginResponse("Too many login attempts. Please try again later.", null));
      }
      rateLimiter.recordAttempt(ip);

      if (bindingResult.hasErrors()) {
         String errorMessage = bindingResult.getFieldErrors().stream()
                 .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                 .collect(Collectors.joining("; "));
         return ResponseEntity.badRequest().body(new LoginResponse(errorMessage, null));
      }

      User user = userService.findByEmail(loginUser.getEmail());
      // Same generic error for "no such user" and "wrong password" so the
      // endpoint doesn't leak which emails are registered.
      if (user == null || !passwordService.matches(loginUser.getPassword(), user.getPassword())) {
         System.out.println("UserController.doLoginUser: invalid credentials");
         return ResponseEntity.badRequest().body(new LoginResponse("Invalid email or password", null));
      }

      String token = sessionService.createSession(user.getId(), user.getRole());
      System.out.println("UserController.doLoginUser: login successful");
      return ResponseEntity.ok(new LoginResponse("Login successful", user.getId(), token));
   }

   /**
    * Reads the userId that the auth filter attached to the request after
    * validating the session token. Controllers must use this - never a
    * client-supplied id/email - to decide what the caller is allowed to
    * see or change.
    */
   private Long currentUserId(HttpServletRequest request) {
      return (Long) request.getAttribute(JwtAuthFilter.AUTH_USER_ID_ATTRIBUTE);
   }


   private Role currentUserRole(HttpServletRequest request) {
      return (Role) request.getAttribute(JwtAuthFilter.AUTH_USER_ROLE_ATTRIBUTE);
   }

   private boolean isAdmin(HttpServletRequest request) {
      return currentUserRole(request) == Role.ADMIN;
   }
}