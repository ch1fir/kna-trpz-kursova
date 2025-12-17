-- 0. Створюємо базу даних
CREATE DATABASE IF NOT EXISTS mindmapdb CHARACTER SET utf8mb4;
USE mindmapdb;

-- 1. Таблиця користувачів
CREATE TABLE IF NOT EXISTS Users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

-- 2. Таблиця категорій
CREATE TABLE IF NOT EXISTS Categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    color VARCHAR(7),
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

-- 3. Таблиця мап
CREATE TABLE IF NOT EXISTS MindMaps (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_favorite BOOLEAN DEFAULT FALSE,
    preview_image VARCHAR(255),
    user_id INT NOT NULL,
    category_id INT, -- Може бути NULL, якщо категорія не обрана
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES Categories(id) ON DELETE SET NULL
);

-- 4. Таблиця елементів мапи
CREATE TABLE IF NOT EXISTS MapElements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    map_id INT NOT NULL,
    x_coord FLOAT NOT NULL,
    y_coord FLOAT NOT NULL,

    -- Колонка-дискримінатор (визначає тип: 'TEXT' або 'IMAGE')
    element_type VARCHAR(10) NOT NULL,

    -- Поля для TextNode
    text_content TEXT,
    font_size INT,
    shape_type VARCHAR(20),

    -- Поля для ImageNode
    image_url VARCHAR(255),
    width INT,
    height INT,

    FOREIGN KEY (map_id) REFERENCES MindMaps(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS MapStrokes (
    stroke_id VARCHAR(36) PRIMARY KEY,
    map_id INT NOT NULL,
    color_argb INT NOT NULL,
    width FLOAT NOT NULL,
    dashed BOOLEAN NOT NULL,
    points LONGTEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (map_id) REFERENCES MindMaps(id) ON DELETE CASCADE
);

CREATE INDEX idx_mapstrokes_map_id ON MapStrokes(map_id);


