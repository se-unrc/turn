CREATE TABLE games (
  id  int(11) NOT NULL auto_increment PRIMARY KEY,
  user1_id int(11),
  user2_id int(11),
  state VARCHAR(12),
  turn int(11),
  created_at DATETIME,
  updated_at DATETIME
)ENGINE=InnoDB;