package twoUsersGame.models;

import java.util.*;
import org.javalite.activejdbc.Model;

public class User extends Model {
  static{
    validatePresenceOf("username").message("Please, provide your username");
    validatePresenceOf("password").message("Please, provide your password");
    validatePresenceOf("email").message("Please, provide your email");
    validateEmailOf("email");
  }

  @Override
  public String toString() {
    return this.getString("username");
  }

  public String getId() {
    return this.getString("id");
  }
}
