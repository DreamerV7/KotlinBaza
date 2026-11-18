CREATE TABLE IF NOT EXISTS app_users (
    login VARCHAR(255) PRIMARY KEY,
    password_hash CHAR(64) NOT NULL,
    salt VARCHAR(255) NOT NULL
);
CREATE TABLE IF NOT EXISTS resources (
    path VARCHAR(1024) PRIMARY KEY,
    max_volume INTEGER NOT NULL CHECK (max_volume >= 0)
);
CREATE TABLE IF NOT EXISTS permissions (
    user_login VARCHAR(255) NOT NULL REFERENCES app_users(login),
    resource_path VARCHAR(1024) NOT NULL REFERENCES resources(path),
    action VARCHAR(16) NOT NULL CHECK (action IN ('read', 'write', 'execute')),
    PRIMARY KEY (user_login, resource_path, action)
);
