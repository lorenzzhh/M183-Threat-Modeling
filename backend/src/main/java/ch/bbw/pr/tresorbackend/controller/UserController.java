package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptService;
import ch.bbw.pr.tresorbackend.service.UserService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
      User user = new User(
            null,
            registerUser.getFirstName(),
            registerUser.getLastName(),
            registerUser.getEmail(),
            passwordService.hashPassword(registerUser.getPassword())
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
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @GetMapping("{id}")
   public ResponseEntity<User> getUserById(@PathVariable("id") Long userId) {
      User user = userService.getUserById(userId);
      if (user == null) return ResponseEntity.notFound().build();
      return new ResponseEntity<>(user, HttpStatus.OK);
   }

   // Build Get All Users REST API
   // http://localhost:8080/api/users
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @GetMapping
   public ResponseEntity<List<User>> getAllUsers() {
      List<User> users = userService.getAllUsers();
      if (users.isEmpty()) return ResponseEntity.notFound().build();
      return new ResponseEntity<>(users, HttpStatus.OK);
   }

   // Build Update User REST API
   // http://localhost:8080/api/users/1
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PutMapping("{id}")
   public ResponseEntity<User> updateUser(@PathVariable("id") Long userId,
                                          @RequestBody User user) {
      user.setId(userId);
      User updatedUser = userService.updateUser(user);
      if (updatedUser == null) return ResponseEntity.notFound().build();
      return new ResponseEntity<>(updatedUser, HttpStatus.OK);
   }

   // Build Delete User REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @DeleteMapping("{id}")
   public ResponseEntity<String> deleteUser(@PathVariable("id") Long userId) {
      if( userService.deleteUser(userId))
         return new ResponseEntity<>("User successfully deleted!", HttpStatus.OK);
      return ResponseEntity.notFound().build();
   }

   // get user id by email
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

   // simple login with no websecurity, just name and password
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/login")
   public ResponseEntity<LoginResponse> doLoginUser(@RequestBody LoginUser loginUser, BindingResult bindingResult) {
      System.out.println("UserController.doLoginUser: " + loginUser);

      if (bindingResult.hasErrors()) {
         String errorMessage = bindingResult.getFieldErrors().stream()
                 .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                 .collect(Collectors.joining("; "));
         return ResponseEntity.badRequest().body(new LoginResponse(errorMessage, null));
      }

      User user = userService.findByEmail(loginUser.getEmail());
      if (user == null) {
         System.out.println("UserController.doLoginUser: user not found");
         return ResponseEntity.badRequest().body(new LoginResponse("No user found with this email", null));
      }

      //ToDo: add verification for password match: loginUser.getPassword() vs user.getPassword
      //todo add implementation

      System.out.println("UserController.doLoginUser: login successful");
      return ResponseEntity.ok(new LoginResponse("Login successful", user.getId()));
   }

}
