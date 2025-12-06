package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataAccess.BadRequestException;
import dataAccess.DataAccessException;
import dataAccess.UnauthorizedException;
import model.AuthData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import service.UserService;
import spark.Request;
import spark.Response;

public class UserHandler {

    UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object register(Request req, Response resp) throws BadRequestException {

        UserData userData = new Gson().fromJson(req.body(), UserData.class);

        if (userData.username() == null || userData.password() == null) {
            throw new BadRequestException("No username and/or password given");
        }

        try {
            AuthData authData = userService.createUser(userData);
            resp.status(200);
            return new Gson().toJson(authData);
        } catch (BadRequestException e) {
            resp.status(403);
            return "{ \"message\": \"Error: already taken\" }";
        }
    }

    public Object login(Request req, Response resp) throws UnauthorizedException, BadRequestException {


        UserData userData = new Gson().fromJson(req.body(), UserData.class);
        if (userData.username() == null || userData.username().isEmpty() ||
                userData.password() == null || userData.password().isEmpty()) {
            resp.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }
        AuthData authData = userService.loginUser(userData);

        resp.status(200);
        return new Gson().toJson(authData);
    }

    public Object logout(Request req, Response resp) throws UnauthorizedException {
        String authToken = req.headers("authorization");

        userService.logoutUser(authToken);

        resp.status(200);
        return "{}";
    }

}