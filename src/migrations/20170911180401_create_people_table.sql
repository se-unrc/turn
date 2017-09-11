CREATE TABLE users (
  id INT(11) NOT NULL auto_increment PRIMARY KEY,
  username VARCHAR(128),
  password  VARCHAR(128),
  email VARCHAR(128),
  admin BOOLEAN,
  created_at DATETIME,
  updated_at DATETIME
)ENGINE=InnoDB;
