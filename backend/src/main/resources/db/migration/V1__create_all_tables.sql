CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(10) NOT NULL,
    password VARCHAR(60) NOT NULL,
    real_name VARCHAR(20) NOT NULL,
    age TINYINT UNSIGNED NOT NULL,
    email VARCHAR(50),
    phone VARCHAR(20),
    status BOOLEAN DEFAULT TRUE,
    role VARCHAR(30) NOT NULL,
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_username (username),
    CHECK (CHAR_LENGTH(username) BETWEEN 6 AND 10),
    CHECK (email IS NULL OR email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
    CHECK (phone IS NULL OR phone REGEXP '^1[3-9][0-9]{9}$'),
    CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

CREATE TABLE flight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_plane BIGINT NOT NULL,
    id_airport_dep BIGINT NOT NULL,
    id_airport_arr BIGINT NOT NULL,
    code VARCHAR(10) NOT NULL,
    datetime_dep DATETIME NOT NULL,
    datetime_arr DATETIME NOT NULL,
    region_dep VARCHAR(20) NOT NULL,
    region_arr VARCHAR(20) NOT NULL,
    distance INT UNSIGNED NOT NULL,
    seat_first_class SMALLINT UNSIGNED NOT NULL,
    seat_business_class SMALLINT UNSIGNED NOT NULL,
    seat_economy_class SMALLINT UNSIGNED NOT NULL,
    price DECIMAL(10, 2) UNSIGNED NOT NULL,
    cancellation_fee DECIMAL(10, 2) UNSIGNED NOT NULL,
    gate VARCHAR(10),
    status VARCHAR(30) NOT NULL,
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_feature (code, datetime_dep)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_flight BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    id_channel BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    total_tax DECIMAL(10, 2) NOT NULL,
    pay_status VARCHAR(30) NOT NULL,
    order_status VARCHAR(30) NOT NULL,
    pay_time DATETIME,
    issue_time DATETIME,
    cancel_time DATETIME,
    remark VARCHAR(100),
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_code (code)
);

CREATE TABLE channel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel_name VARCHAR(50) NOT NULL,
    api_gateway_url VARCHAR(255) NOT NULL,
    UNIQUE INDEX idx_channel_name (channel_name)
);

CREATE TABLE route (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_flight BIGINT NOT NULL,
    distance_remain INT UNSIGNED NOT NULL,
    time_remain INT UNSIGNED NOT NULL,
    altitude DECIMAL(10, 2) UNSIGNED NOT NULL,
    speed DECIMAL(10, 2) UNSIGNED NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    time_stamp DATETIME NOT NULL,
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (latitude BETWEEN -90 AND 90),
    CHECK (longitude BETWEEN -180 AND 180),
    UNIQUE INDEX idx_route_flight (id_flight)
);

CREATE TABLE plane (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_airline BIGINT NOT NULL,
    model_name VARCHAR(30) NOT NULL,
    length DECIMAL(10, 2) UNSIGNED NOT NULL,
    wingspan DECIMAL(10, 2) UNSIGNED NOT NULL,
    height DECIMAL(10, 2) UNSIGNED NOT NULL,
    max_takeoff_weight_kg INT UNSIGNED NOT NULL,
    max_landing_weight_kg INT UNSIGNED NOT NULL,
    max_seat_first_class SMALLINT UNSIGNED NOT NULL,
    max_seat_business_class SMALLINT UNSIGNED NOT NULL,
    max_seat_economy_class SMALLINT UNSIGNED NOT NULL,
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_model_name (model_name),
    CHECK (max_landing_weight_kg <= max_takeoff_weight_kg)
);

CREATE TABLE airline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_airline_name (name)
);

CREATE TABLE airport (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    region VARCHAR(20) NOT NULL,
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_airport_name (name),
    INDEX idx_airport_region (region)
);

CREATE TABLE passenger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_passenger (user_id, passenger_id)
);
