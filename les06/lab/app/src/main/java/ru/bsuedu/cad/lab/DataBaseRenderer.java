package ru.bsuedu.cad.lab;


import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component("DBRenderer")
public class DataBaseRenderer implements Renderer {
    final private JdbcTemplate jdbcTemplate;

    private Provider<Product> productProvider;
    private Provider<Category> categoryProvider;

    public DataBaseRenderer(JdbcTemplate jdbcTemplate, Provider<Product> productProvider, Provider<Category> categoryProvider){
        this.productProvider = productProvider;
        this.categoryProvider = categoryProvider;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void render() {
        for(Product p : productProvider.getEntitees())
        {
            insertProduct(p);
        }

        for(Category c : categoryProvider.getEntitees())
        {
            insertCategory(c);
        }
        
        for(Category c : categoryRequest())
        {

            System.out.println(c);
        }

        System.out.println("DB Renderer");
    }
    
    public void insertProduct(Product product) {
        String sql = "INSERT INTO PRODUCTS (product_id, name, description, price, stock_quantity, image_url, created_at, updated_at)" +
        " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, product.productId, product.name, product.description, product.price, 
        product.stockQuantity, product.imageUrl, product.createdAt, product.updatedAt);
    }

    public void insertCategory(Category category) {
        String sql = "INSERT INTO CATEGORIES (category_id, name, description)" +
        " VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, category.productId, category.name, category.description);
    }

    public List<Category> categoryRequest()
    {
        String sql = "SELECT * FROM CATEGORIES";
        return jdbcTemplate.query(sql, categoryRowMapper());
    }

    private RowMapper<Category> categoryRowMapper() {
        return (rs, rowNum) -> new Category(
                rs.getInt("category_id"),
                rs.getString("name"),
                rs.getString("description"));
    }
}
