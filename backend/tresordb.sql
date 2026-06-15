DROP DATABASE IF EXISTS tresordb;
CREATE DATABASE tresordb;
USE tresordb;

CREATE USER IF NOT EXISTS 'tresoruser'@'%' IDENTIFIED BY 'tresorpassword';
GRANT ALL PRIVILEGES ON tresordb.* TO 'tresoruser'@'%';
FLUSH PRIVILEGES;

CREATE TABLE user (
    id INT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(30) NOT NULL,
    last_name VARCHAR(30) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password LONGTEXT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE secret (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    content JSON NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

INSERT INTO `user` (`id`, `first_name`, `last_name`, `email`, `password`) VALUES
(1, 'Hans', 'Muster', 'hans.muster@bbw.ch', 'abcd'),
(2, 'Paula', 'Kuster', 'paula.kuster@bbw.ch', 'efgh'),
(3, 'Andrea', 'Oester', 'andrea.oester@bbw.ch', 'ijkl');

INSERT INTO `secret` (`id`, `user_id`, `content`) VALUES
    (1, 1, '{"kindid":1,"kind":"credential","userName":"muster","password":"1234","url":"www.bbw.ch"}'),
    (2, 1, '{"kindid":2,"kind":"creditcard","cardtype":"Visa","cardnumber":"4242 4242 4242 4241","expiration":"12/27","cvv":"789"}'),
    (3, 1, '{"kindid":3,"kind":"note","title":"Eragon","content":"Und Eragon ging auf den Drachen zu."}');
