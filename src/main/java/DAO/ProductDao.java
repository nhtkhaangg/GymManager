/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import Model.Categories;
import Model.Product_Images;
import Model.Products;
import db.DBcontext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class ProductDao extends DBcontext {

    public Product_Images getPrimaryImage(int productId) {
        String sql = "SELECT image_id FROM product_images WHERE product_id = ? AND is_primary = 1";
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Product_Images img = new Product_Images();
                img.setImageId(rs.getInt("image_id"));
                return img;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertImage(Product_Images image) {
        String sql = "INSERT INTO product_images (product_id, image_url, is_primary) VALUES (?, ?, ?)";

        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, image.getProductId());
            stmt.setBytes(2, image.getImageUrl());
            stmt.setBoolean(3, image.isIsPrimary());
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Products> getAllProducts() throws SQLException {
        List<Products> list = new ArrayList<>();
        String sql
                = "SELECT p.*, c.name AS category_name, img.image_id AS primary_image_id "
                + "FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "LEFT JOIN product_images img ON p.product_id = img.product_id AND img.is_primary = 1";

        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Products p = new Products();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setActive(rs.getBoolean("is_active"));

                Categories cat = new Categories();
                cat.setCategory_id(rs.getInt("category_id"));
                cat.setName(rs.getString("category_name"));
                p.setCategoryId(cat);
                p.setCategoryName(rs.getString("category_name"));
                // Gán thêm ID ảnh đại diện chính
                int primaryImageId = rs.getInt("primary_image_id");
                if (!rs.wasNull()) {
                    p.setPrimaryImageId(primaryImageId);
                }

                list.add(p);
            }
        }
        return list;
    }

    public void deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id=?";
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    public void updateProduct(Products p) throws SQLException {
        String sql = "UPDATE products SET name=?, description=?, price=?, stock_quantity=?, category_id=?, is_active=? WHERE product_id=?";
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getStockQuantity());
            ps.setInt(5, p.getCategoryId().getCategory_id());
            ps.setBoolean(6, p.isActive());
            ps.setInt(7, p.getProductId());

            ps.executeUpdate();
        }
    }

    public int createProduct(Products p) throws SQLException {
        String sql = "INSERT INTO products (name, description, price, stock_quantity, category_id, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getStockQuantity());
            ps.setInt(5, p.getCategoryId().getCategory_id());
            ps.setBoolean(6, p.isActive());

            ps.executeUpdate();

            // Lấy ID sinh tự động
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        // Trường hợp không có ID → trả về -1 hoặc throw exception tùy bạn
        return -1;
    }

    public Products getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Products p = new Products();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setActive(rs.getBoolean("is_active"));

                Categories cat = new Categories();
                cat.setCategory_id(rs.getInt("category_id"));
                p.setCategoryId(cat);

                return p;
            }
        }
        return null;
    }
    
    

    public void addProductImage(int productId, byte[] imageData, boolean isPrimary) throws SQLException {
        String sql = "INSERT INTO product_images (product_id, image_url, is_primary) VALUES (?, ?, ?)";
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setBytes(2, imageData);
            ps.setBoolean(3, isPrimary);
            ps.executeUpdate();
        }
    }

    public List<Product_Images> getImagesByProductId(int productId) throws SQLException {
        List<Product_Images> list = new ArrayList<>();
        String sql = "SELECT * FROM product_images WHERE product_id = ?";
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product_Images img = new Product_Images();
                img.setImageId(rs.getInt("image_id"));
                img.setProductId(rs.getInt("product_id"));
                img.setImageUrl(rs.getBytes("image_url"));
                img.setIsPrimary(rs.getBoolean("is_primary"));
                img.setUploadedAt(rs.getTimestamp("uploaded_at"));
                list.add(img);
            }
        }
        return list;
    }

    public void deleteImageById(int imageId) throws SQLException {
        String sql = "DELETE FROM product_images WHERE image_id = ?";
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, imageId);
            ps.executeUpdate();
        }
    }

    public void setPrimaryImage(int productId, int imageId) throws SQLException {
        String resetAll = "UPDATE product_images SET is_primary = 0 WHERE product_id = ?";
        String setOne = "UPDATE product_images SET is_primary = 1 WHERE image_id = ?";

        try ( Connection conn = new DBcontext().getConnection()) {
            try ( PreparedStatement ps1 = conn.prepareStatement(resetAll)) {
                ps1.setInt(1, productId);
                ps1.executeUpdate();
            }

            try ( PreparedStatement ps2 = conn.prepareStatement(setOne)) {
                ps2.setInt(1, imageId);
                ps2.executeUpdate();
            }
        }
    }

    public List<Product_Images> getAllImagesByProductId(int productId) {
        List<Product_Images> images = new ArrayList<>();
        String sql = "SELECT * FROM product_images WHERE product_id = ?";

        try ( Connection conn = getConnection(); // 🔧 mở kết nối tại đây
                  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product_Images img = new Product_Images();
                img.setImageId(rs.getInt("image_id"));
                img.setProductId(rs.getInt("product_id"));
                img.setImageUrl(rs.getBytes("image_url")); // dùng đúng tên cột
                img.setIsPrimary(rs.getBoolean("is_primary"));
                images.add(img);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return images;
    }

    public byte[] getImageDataById(int imageId) {
        String sql = "SELECT image_url FROM product_images WHERE image_id = ?";
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, imageId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBytes("image_url");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Products> getProductsByPage(int page, int productsPerPage) throws SQLException {
        List<Products> list = new ArrayList<>();
        int start = (page - 1) * productsPerPage;

        // Câu lệnh SQL sử dụng OFFSET và FETCH NEXT cho SQL Server
        String sql = "SELECT p.*, c.name AS category_name, img.image_id AS primary_image_id "
                + "FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "LEFT JOIN product_images img ON p.product_id = img.product_id AND img.is_primary = 1 "
                + "ORDER BY p.product_id " // Cần phải có ORDER BY trong SQL Server khi sử dụng OFFSET
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";  // Sử dụng OFFSET và FETCH NEXT thay vì LIMIT

        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, start); // Điểm bắt đầu (OFFSET)
            ps.setInt(2, productsPerPage); // Số sản phẩm mỗi trang (FETCH NEXT)

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Products p = new Products();
                    p.setProductId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setDescription(rs.getString("description"));
                    p.setPrice(rs.getDouble("price"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    p.setActive(rs.getBoolean("is_active"));

                    Categories cat = new Categories();
                    cat.setCategory_id(rs.getInt("category_id"));
                    cat.setName(rs.getString("category_name"));
                    p.setCategoryId(cat);
                    p.setCategoryName(rs.getString("category_name"));

                    int primaryImageId = rs.getInt("primary_image_id");
                    if (!rs.wasNull()) {
                        p.setPrimaryImageId(primaryImageId);
                    }

                    list.add(p);
                }
            }
        }
        return list;
    }

    public int getTotalProducts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";  // Đếm tổng số sản phẩm trong bảng products
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);  // Trả về tổng số sản phẩm
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;  // Ném lại exception nếu có lỗi
        }
        return 0;  // Nếu không có sản phẩm nào
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//||                        DUY KHANG ( Product Detail )                                                                                   ||
//||/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    

    public Products getProductByIdDetail(int productId) {
        Products p = null;
        String sql = "SELECT p.*, c.name AS category_name, "
                + "(SELECT TOP 1 image_id FROM product_images WHERE product_id = p.product_id AND is_primary = 1) AS primary_image_id "
                + "FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.product_id = ?";

        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                p = new Products();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setActive(rs.getBoolean("is_active"));

                // Set Category object hoặc chỉ tên, tùy bạn dùng
                Categories cat = new Categories();
                cat.setCategory_id(rs.getInt("category_id"));
                cat.setName(rs.getString("category_name"));
                p.setCategoryId(cat);

                p.setCategoryName(rs.getString("category_name"));

                int primaryImageId = rs.getInt("primary_image_id");
                if (!rs.wasNull()) {
                    p.setPrimaryImageId(primaryImageId);
                }

                // Lấy list images
                p.setImages(getImagesByProductId(conn, productId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    private List<Product_Images> getImagesByProductId(Connection conn, int productId) {
        List<Product_Images> images = new ArrayList<>();
        String sql = "SELECT * FROM product_images WHERE product_id = ?";
        try ( PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product_Images img = new Product_Images();
                img.setImageId(rs.getInt("image_id"));
                img.setProductId(rs.getInt("product_id"));
                img.setImageUrl(rs.getBytes("image_url")); // hoặc field ảnh trong DB của bạn
                img.setIsPrimary(rs.getBoolean("is_primary"));
                images.add(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    public List<Products> getRelatedProducts(int categoryId, int excludeProductId, int limit) {
        List<Products> related = new ArrayList<>();
        String sql = "SELECT TOP (?) p.*, c.name AS category_name, "
                + "(SELECT TOP 1 image_id FROM product_images WHERE product_id = p.product_id AND is_primary = 1) AS primary_image_id "
                + "FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.category_id = ? AND p.product_id <> ?";

        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, categoryId);
            ps.setInt(3, excludeProductId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Products p = new Products();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setActive(rs.getBoolean("is_active"));

                Categories cat = new Categories();
                cat.setCategory_id(rs.getInt("category_id"));
                cat.setName(rs.getString("category_name"));
                p.setCategoryId(cat);
                p.setCategoryName(rs.getString("category_name"));

                int primaryImageId = rs.getInt("primary_image_id");
                if (!rs.wasNull()) {
                    p.setPrimaryImageId(primaryImageId);
                }

                related.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return related;
    }
//////////////////////////////////////////////////////////////////////
//
//HoangKhang
//
//////////////////////////////////////////////////////////////////////

    /**
     * Returns a paginated list of products, optionally filtered by category and
     * sorted by price or ID.
     *
     * @param categoryId
     * @param sortOrder
     * @param page
     * @param pageSize
     * @return
     * @throws SQLException
     */
    public List<Products> getProductsByPageAndFilter(
            Integer categoryId, String sortOrder, int page, int pageSize) throws SQLException {
        List<Products> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name, img.image_id AS primary_image_id "
                + "FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "LEFT JOIN product_images img ON p.product_id = img.product_id AND img.is_primary = 1 ";

        //Add WHERE if there's cate id
        if (categoryId != null) {
            sql += "WHERE p.category_id = ? ";
        }

        //Sort
        if ("asc".equalsIgnoreCase(sortOrder)) {
            sql += "ORDER BY p.price ASC ";
        } else if ("desc".equalsIgnoreCase(sortOrder)) {
            sql += "ORDER BY p.price DESC ";
        } else {
            sql += "ORDER BY p.product_id ";
        }
        sql += "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (categoryId != null) {
                ps.setInt(paramIndex++, categoryId);
            }
            int offset = (page - 1) * pageSize;
            ps.setInt(paramIndex++, offset);
            ps.setInt(paramIndex, pageSize);

            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Products p = new Products();
                    p.setProductId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setDescription(rs.getString("description"));
                    p.setPrice(rs.getDouble("price"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    p.setActive(rs.getObject("is_active") != null ? rs.getBoolean("is_active") : null);
                    Categories cat = new Categories();
                    cat.setCategory_id(rs.getInt("category_id"));
                    cat.setName(rs.getString("category_name"));
                    p.setCategoryId(cat);
                    p.setCategoryName(rs.getString("category_name"));
                    int primaryImageId = rs.getInt("primary_image_id");
                    if (!rs.wasNull()) {
                        p.setPrimaryImageId(primaryImageId);
                    }
                    list.add(p);
                }
            }
        }
        return list;
    }

    /**
     * Counts the total number of products in a given category to calculate the
     * total number of pages when paginating by category.
     *
     * @param categoryId
     * @return
     * @throws SQLException
     */
    public int getTotalProductsByCategory(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Returns list of products matched keyword (name or description), category
     * optional. Can also be sorted by price or ID.
     *
     * @param keyword
     * @param categoryId
     * @param sortOrder
     * @param page
     * @param pageSize
     * @return
     * @throws SQLException
     */
    public List<Products> getProductsBySearch(String keyword, Integer categoryId, String sortOrder, int page, int pageSize) throws SQLException {
        List<Products> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name, img.image_id AS primary_image_id "
                + "FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "LEFT JOIN product_images img ON p.product_id = img.product_id AND img.is_primary = 1 "
                + "WHERE (p.name LIKE ? OR p.description LIKE ?)";
        if (categoryId != null) {
            sql += " AND p.category_id = ? ";
        }
        if ("asc".equalsIgnoreCase(sortOrder)) {
            sql += "ORDER BY p.price ASC ";
        } else if ("desc".equalsIgnoreCase(sortOrder)) {
            sql += "ORDER BY p.price DESC ";
        } else {
            sql += "ORDER BY p.product_id ";
        }
        sql += "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, "%" + keyword + "%");
            ps.setString(idx++, "%" + keyword + "%");
            if (categoryId != null) {
                ps.setInt(idx++, categoryId);
            }
            int offset = (page - 1) * pageSize;
            ps.setInt(idx++, offset);
            ps.setInt(idx, pageSize);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Products p = new Products();
                    p.setProductId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setDescription(rs.getString("description"));
                    p.setPrice(rs.getDouble("price"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    p.setActive(rs.getObject("is_active") != null ? rs.getBoolean("is_active") : null);
                    Categories cat = new Categories();
                    cat.setCategory_id(rs.getInt("category_id"));
                    cat.setName(rs.getString("category_name"));
                    p.setCategoryId(cat);
                    p.setCategoryName(rs.getString("category_name"));
                    int primaryImageId = rs.getInt("primary_image_id");
                    if (!rs.wasNull()) {
                        p.setPrimaryImageId(primaryImageId);
                    }
                    list.add(p);
                }
            }
        }
        return list;
    }

    /**
     * Counts the total number of products that match a keyword (name or
     * description) and specific category (optional).
     *
     * @param keyword
     * @param categoryId
     * @return
     * @throws SQLException
     */
    public int getTotalProductsBySearch(String keyword, Integer categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE (name LIKE ? OR description LIKE ?)";
        if (categoryId != null) {
            sql += " AND category_id = ?";
        }
        try ( Connection conn = new DBcontext().getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, "%" + keyword + "%");
            ps.setString(idx++, "%" + keyword + "%");
            if (categoryId != null) {
                ps.setInt(idx, categoryId);
            }
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countProductsInStock() {
        String sql = "SELECT SUM(stock_quantity) FROM products WHERE stock_quantity > 0 AND is_active = 1";
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1); // Trả về tổng số lượng hàng còn trong kho
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

     // Phương thức lấy sản phẩm theo productId
    public Products getProductByIdPage(int productId) {
        Products product = null;
        String sql = "SELECT * FROM products WHERE product_id = ?"; // Câu truy vấn lấy thông tin sản phẩm

        try (Connection con = getConnection();  // Giả sử bạn có một lớp để kết nối CSDL
             PreparedStatement pst = con.prepareStatement(sql)) {

            // Gán tham số vào câu truy vấn
            pst.setInt(1, productId);

            // Thực hiện câu truy vấn và lấy kết quả
            ResultSet rs = pst.executeQuery();

            // Kiểm tra nếu có sản phẩm
            if (rs.next()) {
                // Tạo đối tượng sản phẩm và gán thông tin từ CSDL
                product = new Products();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setStockQuantity(rs.getInt("stock_quantity"));
                // Gán các thuộc tính khác nếu cần
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return product;
    }
}
