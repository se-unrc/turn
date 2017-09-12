package twoUsersGame;

import twoUsersGame.models.User;
import twoUsersGame.models.Game;

import org.javalite.activejdbc.Base;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.List;
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
      if (session.attribute(SESSION_NAME) != null)
        return User.findById(session.attribute(SESSION_NAME));
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
        User currentUser = currentUser(request);

        List<Game> currentGames = Game.find("turn = ? AND state = ?", currentUser.get("id"), "running");
        currentGames.isEmpty();

        List<Game> challengedGames = Game.find("user2_id = ? AND state = ?", currentUser.get("id"), "challenged");
        challengedGames.isEmpty();

        Map attributes = new HashMap();
        attributes.put("name", currentUser.toString());
        attributes.put("games", currentGames);
        attributes.put("challenged", challengedGames);

        return new ModelAndView(attributes, "./views/dashboard.mustache");
      }, new MustacheTemplateEngine()
    );

    get("/l/game/new", (request, response) -> {
        Map attributes = new HashMap();
        List<User> users = User.findAll();

        attributes.put("currentUser", currentUser(request));
        attributes.put("users", users);

        return new ModelAndView(attributes, "./views/games/new.mustache");
      }, new MustacheTemplateEngine()
    );

    post("/l/challenge", (request, response) -> {
      User currentUser = currentUser(request);
      User u = User.findFirst("id = ?", request.queryParams("userId"));

      if (u != null) {
        Game g = new Game();
        g.set("user1_id", currentUser.get("id"));
        g.set("user2_id", u.get("id"));
        g.set("state", "challenged");
        g.saveIt();
      }

      response.redirect("/l/dashboard");
      return null;
    });

    post("/l/games/accept", (request, response) -> {
      User currentUser = currentUser(request);
      Game g = Game.findFirst("id = ?", request.queryParams("gameId"));

      if (g != null) {
        g.set("state", "running");
        g.set("turn", g.get("user1_id"));
        g.saveIt();
      }

      response.redirect("/l/dashboard");
      return null;
    });

    get("/l/games/play/:id", (request, response) -> {
        User currentUser = currentUser(request);
        Game g = Game.findById(request.params(":id"));

        Map attributes = new HashMap();
        attributes.put("currentUser", currentUser);
        attributes.put("turn", g.getTurn().equals(currentUser.getId()));
        attributes.put("gameId", g.getId());

        return new ModelAndView(attributes, "./views/games/show.mustache");
      }, new MustacheTemplateEngine()
    );

    post("/l/games/play/:id", (request, response) -> {
        User currentUser = currentUser(request);
        Game g = Game.findById(request.params(":id"));

        if (g.getTurn().equals(currentUser.getId())) {
          if (g.getTurn().equals(g.getString("user1_id")))
            g.set("turn", g.getString("user2_id"));
          else
            g.set("turn", g.getString("user1_id"));
        }
        g.saveIt();

        response.redirect("/l/dashboard");
        return null;
      }
    );
  }
}
