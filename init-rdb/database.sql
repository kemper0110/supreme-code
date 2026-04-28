create database supreme_code;
create user supreme_user with password 'password';
grant all privileges on database supreme_code to supreme_user;

create database keycloak_db;
create user keycloak_user with password 'password';
grant all privileges on database keycloak_db to keycloak_user;
ALTER DATABASE keycloak_db OWNER TO keycloak_user;

GRANT USAGE, CREATE ON SCHEMA public TO keycloak_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak_user;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO keycloak_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO keycloak_user;