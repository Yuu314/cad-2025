<<<<<<< HEAD
CREATE TABLE CATEGORIES (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255)

);

CREATE TABLE PRODUCTS (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    category_id INT,
=======
CREATE TABLE CATEGORIES ( 
    category_id IDENTITY PRIMARY KEY,
    name VARCHAR(255), 
    description VARCHAR(255)
);

CREATE TABLE PRODUCTS (
    product_id IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
>>>>>>> 03b6caa1575cb6309a7bb9bd48c1c39ce018db3f
    price DECFLOAT(20),
    stock_quantity INT,
    image_url VARCHAR(255),
    created_at DATE,
<<<<<<< HEAD
    updated_at DATE,
    FOREIGN KEY (category_id) REFERENCES CATEGORIES(id)
=======
    updated_at DATE
>>>>>>> 03b6caa1575cb6309a7bb9bd48c1c39ce018db3f
);