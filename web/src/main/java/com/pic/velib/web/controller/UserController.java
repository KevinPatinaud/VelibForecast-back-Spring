package com.pic.velib.web.controller;

import com.pic.velib.entity.Station;
import com.pic.velib.entity.User;
import com.pic.velib.entity.UserFacebook;
import com.pic.velib.entity.UserMail;
import com.pic.velib.service.dto.exception.UserAlreadyExistException;
import com.pic.velib.service.dto.exception.UserNotExistException;
import com.pic.velib.service.dto.UserService;
import com.pic.velib.service.facebook.FacebookLogin;
import com.pic.velib.service.recaptcha.Recaptcha;
import com.pic.velib.web.exception.UserAlreadyExistHTTPException;
import com.pic.velib.web.exception.UserNotExistHTTPException;
import com.pic.velib.web.security.JWTUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private PasswordEncoder passwordEncoder;
    private FacebookLogin fbLogin;
    private Recaptcha recaptcha;

    private JWTUtils jwtUtils;

    private String jwtSecret;

    @Autowired
    public UserController(PasswordEncoder passwordEncoder, UserService userService, FacebookLogin fbLogin, Recaptcha recaptcha, JWTUtils jwtUtils) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.fbLogin = fbLogin;
        this.recaptcha = recaptcha;
        this.jwtUtils = jwtUtils;

    }


    @PostMapping("/mailuser")
    public String createMailUser(@RequestBody Map<String, Object> params) {

        if (!recaptcha.isValide(params.get("captchaToken").toString())) return null;


        try {
            UserMail user = userService.createUserMail(params.get("email").toString(), params.get("password").toString());
            return generateResponseUserConnected(user).toString();
        } catch (UserAlreadyExistException e) {
            throw new UserAlreadyExistHTTPException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/ismailalreadyrecorded")
    public boolean isMailUserExist(@RequestParam String mail) {

        return userService.getUserMail(mail) != null;

    }


    @PutMapping("/mailuser")
    public String connectMailUser(@RequestBody Map<String, Object> params) {

   //     if (!recaptcha.isValide(params.get("captchaToken").toString())) return null;

        try {
            UserMail user = userService.getUserMail(params.get("email").toString());
            return generateResponseUserConnected(user).toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/facebookuser")
    public String createFacebookUser(@RequestBody Map<String, Object> params) {

        try {
            UserFacebook user = userService.createUserFacebook(params.get("accessToken").toString());
            return generateResponseUserConnected(user).toString();
        } catch (UserAlreadyExistException e) {
            throw new UserAlreadyExistHTTPException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }


    @PutMapping("/facebookuser")
    public String connectFacebookUser(@RequestHeader("facebook_access_token") String facebook_access_token) {

        try {
            UserFacebook user = userService.getUserFacebook(facebook_access_token);
            return generateResponseUserConnected(user).toString();
        } catch (UserNotExistException e) {
            throw new UserNotExistHTTPException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }


    @PutMapping("/addfavoritestation")
    public boolean addFavoriteStation(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, Object> params) throws Exception {

        String jwtToken = authorization.replace("Bearer ", "");


        userService.addFavoriteStation(Integer.parseInt(params.get("id_station").toString()),
                UUID.fromString( jwtUtils.getPayload(jwtToken).getString("iduser") ));

        return true;
    }

    @PutMapping("/removefavoritestation")
    public boolean removeFavoriteStation(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, Object> params) throws Exception {

        String jwtToken = authorization.replace("Bearer ", "");

        userService.removeFavoriteStation(Integer.parseInt(params.get("id_station").toString()),
                UUID.fromString( jwtUtils.getPayload(jwtToken).getString("iduser") ));

        return true;
    }



    private JSONObject generateResponseUserConnected(User user) throws JSONException {

        if (user == null) return null;

        JSONObject response = new JSONObject();

        JSONObject payload = new JSONObject();
        payload.put("iduser", user.getId());

        JSONArray favoriteStations = new JSONArray();

        if (user.getFavoriteStations() != null) for (int i = 0; i < user.getFavoriteStations().size(); i++)
            favoriteStations.put(((Station) user.getFavoriteStations().toArray()[i]).toJSON());

        payload.put("favoriteStations", favoriteStations);

        try {
            response.put("JWT", jwtUtils.generateJwtToken(payload));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return response;
    }


}
