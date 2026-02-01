-- 1. Roles Table
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(30) NOT NULL UNIQUE,
                       description VARCHAR(100)
);

-- Pre-populate roles
INSERT INTO roles (name, description) VALUES
                                          ('ROLE_STUDENT', 'University student'),
                                          ('ROLE_TEACHER', 'Academic staff / Professor'),
                                          ('ROLE_COORDINATOR', 'Program coordinator'),
                                          ('ROLE_ADMIN', 'System administrator')
    ON CONFLICT (name) DO NOTHING;

-- 2. Users Table (Auth Credentials)
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       enabled BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Users_Roles Table (Many-to-Many)
CREATE TABLE users_roles (
                             user_id UUID NOT NULL,
                             role_id INTEGER NOT NULL,
                             PRIMARY KEY (user_id, role_id),
                             CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. User Details Table (Formerly Profiles)
CREATE TABLE user_details (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id UUID NOT NULL UNIQUE,
                              first_name VARCHAR(50) NOT NULL,
                              last_name VARCHAR(50) NOT NULL,
                              avatar_url VARCHAR(255),
                              bio TEXT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_user_details_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);