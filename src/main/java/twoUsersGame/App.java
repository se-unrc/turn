package twoUsersGame;

import twoUsersGame.models.User;

import org.javalite.activejdbc.Base;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import spark.Request;
import spark.Session;

/**
 * Basic App
 *
 */
public class App {
  private static final String SESSION_NAME = "currentuser";

  private static final Properties props = new Properties();

  public static User currentUser(Request request) {
    Session session = request.session(false);
    if (session != null) {
      Set<String> sessionAttributes = request.session(false).attributes();
      if (sessionAttributes.contains(SESSION_NAME))
        return User.findById(sessionAttributes.contains(SESSION_NAME));
    }
    return null;
  }

  public static void main(String[] args) throws IOException {
    try {
      FileInputStream in = new FileInputStream("./src/main/resources/database.properties");
      props.load(in);
      in.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    before((request, response) -> {
      if (!Base.hasConnection())
        Base.open(props.getProperty("development.driver"),
                  props.getProperty("development.url"),
                  props.getProperty("development.username"),
                  props.getProperty("development.password"));
    });

    after((request, response) -> {
      if (Base.hasConnection())
        Base.close();
    });

    before("/l/*", (request, response) -> {
      // check if authenticated
      boolean authenticated = currentUser(request) != null;
      if (!authenticated) {
        halt(401, "You are not welcome here");
      }
    });

    get("/", (request, response) -> {
        String name = request.session().attribute(SESSION_NAME);
        Map map = new HashMap();
        if (name == null) {
            return new ModelAndView(map, "./views/landing.mustache");
        } else {
          map.put("nam", name);
          return new ModelAndView(map, "./views/logedin.mustache");
        }
      }, new MustacheTemplateEngine()
    );

    post("/login", (request, response) -> {
      User u = User.findFirst("username = ?", request.queryParams("username"));

      // Check password and best security politics...
      if(u != null) {
        request.session().attribute(SESSION_NAME, u.get("id"));
      }
      response.redirect("/l/dashboard");
      return null;
    });

    post("/l/logout", (request, response) -> {
      request.session().removeAttribute(SESSION_NAME);
      response.redirect("/");
      return null;
    });

    get("/l/dashboard", (request, response) -> {
        Map map = new HashMap();
        map.put("currentUser", currentUser(request));
        return new ModelAndView(map, "./views/dashboard.mustache");
      }, new MustacheTemplateEngine()
    );
  }
}
