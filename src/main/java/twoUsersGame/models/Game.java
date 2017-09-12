package twoUsersGame.models;

import java.util.*;
import org.javalite.activejdbc.Model;

import twoUsersGame.models.User;

public class Game extends Model {
  public String getChallenger() {
    return User.findFirst("id = ?", this.get("user1_id")).getString("username");
  }

  public String getId() {
    return this.getString("id");
  }

  public String getTurn() {
    return this.getString("turn");
  }
}
