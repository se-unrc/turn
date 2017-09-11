package twoUsersGame;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

/**
 * Hello world!
 *
 */
public class App {
  public static void main(String[] args) {
    Map map = new HashMap();
    map.put("name", "Sam");
    map.put("value", 1000);
    map.put("taxed_value", 1000 - (1000 * 0.4));
    map.put("in_ca", true);

    get("/hello", (req, res) -> {
        return new ModelAndView(map, "./views/hello.mustache");
      }, new MustacheTemplateEngine()
    );
  }
}
