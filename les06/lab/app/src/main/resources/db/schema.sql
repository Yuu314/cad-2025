CREATE TABLE CATEGORIES ( 
    category_id IDENTITY PRIMARY KEY,
    name VARCHAR(255), 
    description VARCHAR(255)
);

CREATE TABLE PRODUCTS (
    product_id IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    price DECFLOAT(20),
    stock_quantity INT,
    image_url VARCHAR(255),
    created_at DATE,
    updated_at DATE
);