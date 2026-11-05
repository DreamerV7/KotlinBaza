-- Initial data only: rerunning does not overwrite existing records.
INSERT INTO app_users (login, password_hash, salt)
SELECT 'alice', 'cc09a9884adc41d7d26fa39899c8e5da13330de003bcda215063c80730d6a16b', 'd05a3a4e-f6af-4436-976d-7e37094a9d7e'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE login = 'alice');
INSERT INTO app_users (login, password_hash, salt)
SELECT 'bob', 'd392273a5272991bffa517fcec7eab0a3656b72b9dbacd2094ac5593cde107f6', 'b3faf95a-568f-43fd-93f9-e60ecb6d4392'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE login = 'bob');

INSERT INTO resources (path, max_volume)
SELECT v.path, v.max_volume FROM (VALUES
    ('A', 1000), ('A.B', 500), ('A.B.C', 100), ('A.B.D', 200),
    ('A.X', 300), ('B', 800), ('B.C', 150)
) AS v(path, max_volume)
WHERE NOT EXISTS (SELECT 1 FROM resources r WHERE r.path = v.path);

INSERT INTO permissions (user_login, resource_path, action)
SELECT v.user_login, v.resource_path, v.action FROM (VALUES
    ('alice', 'A', 'read'), ('alice', 'A.B', 'write'), ('bob', 'A.B.C', 'execute')
) AS v(user_login, resource_path, action)
WHERE NOT EXISTS (SELECT 1 FROM permissions p
    WHERE p.user_login = v.user_login AND p.resource_path = v.resource_path AND p.action = v.action);
