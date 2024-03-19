create database supreme_code;
create user supreme_user with password 'password';
grant all privileges on database supreme_code to supreme_user;

create database keycloak_db;
create user keycloak_user with password 'password';
grant all privileges on database keycloak_db to keycloak_user;