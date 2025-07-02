package DAO;

import Model.Account;
import Model.Customer;
import Model.Package;
import Model.CustomerMembership;
import Model.MembershipPackage;
import db.DBcontext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao extends DBcontext {

//    // Lấy kết nối từ DBcontext
//    public Connection getConnection() throws SQLException {
//        return super.getConnection(); // Dùng phương thức getConnection() từ lớp DBcontext
//    }
    // Thêm khách hàng mới
    public void createCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (account_id, full_name, email, phone, customer_code, address) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try ( Connection conn = getConnection();  PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customer.getAccount().getAccountId());
            stmt.setString(2, customer.getFullName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getCustomerCode());
            stmt.setString(6, customer.getAddress());

            stmt.executeUpdate();
            System.out.println("✅ Đã thêm mới customer: " + customer.getFullName());
        }
    }

    // Chỉnh sửa thông tin khách hàng
    public boolean updateCustomer(Customer customer) {
        String query = "UPDATE customers SET full_name = ?, email = ?, phone = ?, customer_code = ?, address = ? WHERE customer_id = ?";
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getCustomerCode());
            ps.setString(5, customer.getAddress());
            ps.setInt(6, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertCustomer(Customer customer) {
        String sql = "INSERT INTO customers (account_id, full_name, email, phone, customer_code, address) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customer.getAccount().getAccountId());
            ps.setString(2, customer.getFullName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setString(5, customer.getCustomerCode());
            ps.setString(6, customer.getAddress());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isMemberCodeExists(String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE customer_code = ?";
        try ( Connection conn = getConnection();  PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Xóa khách hàng
    public boolean deleteCustomer(int customerId) {
        String getAccountIdSql = "SELECT account_id FROM customers WHERE customer_id = ?";
        String deleteCustomerSql = "DELETE FROM customers WHERE customer_id = ?";
        String updateRoleSql = "UPDATE accounts SET role = 'customer' WHERE account_id = ?";

        try ( Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            int accountId = -1;

            // 1. Lấy account_id từ customer_id
            try ( PreparedStatement psGet = conn.prepareStatement(getAccountIdSql)) {
                psGet.setInt(1, customerId);
                ResultSet rs = psGet.executeQuery();
                if (rs.next()) {
                    accountId = rs.getInt("account_id");
                } else {
                    conn.rollback(); // Không tìm thấy => rollback
                    return false;
                }
            }

            // 2. Xóa khách hàng
            try ( PreparedStatement psDelete = conn.prepareStatement(deleteCustomerSql)) {
                psDelete.setInt(1, customerId);
                int deleted = psDelete.executeUpdate();
                if (deleted == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 3. Update role của account
            try ( PreparedStatement psUpdate = conn.prepareStatement(updateRoleSql)) {
                psUpdate.setInt(1, accountId);
                int updated = psUpdate.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit(); // Tất cả thành công => commit
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy danh sách tất cả khách hàng
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT c.*, a.account_id, a.username, a.avatar "
                + "FROM customers c "
                + "JOIN accounts a ON c.account_id = a.account_id "
                + "WHERE a.role = 'customer'";

        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(query);  ResultSet rs = ps.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                Account account = new Account();
                account.setAccountId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setAvatar(rs.getBytes("avatar")); // BLOB dùng cho AvatarServlet

                Customer customer = new Customer();
                customer.setCustomerId(rs.getInt("customer_id"));
                customer.setFullName(rs.getString("full_name"));
                customer.setEmail(rs.getString("email"));
                customer.setPhone(rs.getString("phone"));
                customer.setCustomerCode(rs.getString("customer_code"));
                customer.setAccount(account);

                customers.add(customer);

                // DEBUG LOG
                System.out.println("Customer #" + (++count));
                System.out.println("  ID: " + customer.getCustomerId());
                System.out.println("  Name: " + customer.getFullName());
                System.out.println("  Email: " + customer.getEmail());
                System.out.println("  Phone: " + customer.getPhone());
                System.out.println("  Code: " + customer.getCustomerCode());
                System.out.println("  Account ID: " + account.getAccountId());
                System.out.println("  Username: " + account.getUsername());
                System.out.println("  Avatar bytes: " + (account.getAvatar() != null ? account.getAvatar().length : 0));
                System.out.println("--------------------------------------------------");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy danh sách khách hàng:");
            e.printStackTrace();
        }

        System.out.println("Tổng số khách hàng được load: " + customers.size());
        return customers;
    }

    // Tìm khách hàng theo ID
    public Customer getCustomerById(int customerId) {
        Customer customer = null;
        String query = "SELECT * FROM customers WHERE customer_id = ?"; // sửa
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, customerId);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    customer = new Customer();
                    customer.setCustomerId(rs.getInt("customer_id"));
                    customer.setFullName(rs.getString("full_name"));
                    customer.setEmail(rs.getString("email"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setCustomerCode(rs.getString("customer_code"));
                    customer.setAddress(rs.getString("address"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customer;
    }

    public List<Account> getAllCustomerAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_id, username FROM accounts WHERE role = 'customer'";
        try ( Connection conn = getConnection();  PreparedStatement stmt = conn.prepareStatement(sql);  ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Account acc = new Account();
                acc.setAccountId(rs.getInt("account_id"));
                acc.setUsername(rs.getString("username"));
                accounts.add(acc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public List<Account> getCustomerThatNotStaffYet() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE role = 'customer' AND account_id NOT IN (SELECT account_id FROM customers)";
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Account acc = new Account();
                acc.setAccountId(rs.getInt("account_id"));
                acc.setUsername(rs.getString("username"));
                acc.setRole(rs.getString("role"));
                acc.setCreatedAt(rs.getTimestamp("created_at"));

                list.add(acc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Account getCustomerAccountById(int id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ? AND role = 'customer'";
        try ( Connection conn = getConnection();  PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Account acc = new Account();
                acc.setAccountId(rs.getInt("account_id"));
                acc.setUsername(rs.getString("username"));
                acc.setPassword(rs.getString("password"));
                acc.setAvatar(rs.getBytes("avatar"));
                acc.setRole(rs.getString("role"));
                return acc;
            }
        }
        return null;
    }
public boolean addMembership(CustomerMembership membership) {
        String sql = "INSERT INTO customer_memberships (account_id, package_id, start_date, end_date, payment_status) VALUES (?, ?, ?, ?, ?)";
        try (
                 Connection conn = getConnection();  PreparedStatement stmt = conn.prepareStatement(sql)) {

            int accountId = membership.getCustomer().getAccount().getAccountId(); // <-- CHỈ DÙNG accountId!
            int packageId = membership.getMembershipPackage().getPackageId();

            stmt.setInt(1, accountId);
            stmt.setInt(2, packageId);
            stmt.setDate(3, java.sql.Date.valueOf(membership.getStartDate()));
            stmt.setDate(4, java.sql.Date.valueOf(membership.getEndDate()));
            stmt.setString(5, membership.getPaymentStatus());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CustomerMembership getActiveMembershipByAccountId(int accountId) {
        CustomerMembership membership = null;
        String sql = "SELECT * FROM customer_memberships "
                + "WHERE account_id = ? AND end_date >= GETDATE() "
                + "AND (payment_status = 'paid' OR payment_status = 'cancelled')";

        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                membership = new CustomerMembership();
                membership.setMembershipId(rs.getInt("membership_id"));

                // Set customer
                Customer customer = new Customer();
                Account account = new Account();
                account.setAccountId(rs.getInt("account_id"));
                customer.setAccount(account);
                membership.setCustomer(customer);

                // ⭐️ Bổ sung: SET membershipPackage đầy đủ
                int packageId = rs.getInt("package_id");
                PackageDao packageDao = new PackageDao();
                Model.Package pkg = packageDao.getPackageById(packageId);
                MembershipPackage mPackage = null;
                if (pkg != null) {
                    mPackage = new MembershipPackage(
                            pkg.getId(),
                            pkg.getName(),
                            pkg.getDescription(),
                            pkg.getDurationDays(),
                            pkg.getPrice(),
                            pkg.isIsActive()
                    );
                }
                membership.setMembershipPackage(mPackage); // QUAN TRỌNG

                membership.setStartDate(rs.getDate("start_date").toLocalDate());
                membership.setEndDate(rs.getDate("end_date").toLocalDate());
                membership.setPaymentStatus(rs.getString("payment_status"));

                System.out.println("DEBUG DAO: membership.getCustomer() = " + membership.getCustomer());
                System.out.println("DEBUG DAO: membership.getMembershipPackage() = " + membership.getMembershipPackage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return membership;
    }

    // 1. Lấy membership theo id (để lấy thông tin gói hiện tại)
    public CustomerMembership getMembershipById(int membershipId) {
        CustomerMembership membership = null;
        String sql = "SELECT * FROM customer_memberships WHERE membership_id = ?";
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, membershipId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                membership = new CustomerMembership();
                membership.setMembershipId(rs.getInt("membership_id"));
                // Gán các trường khác nếu cần...
                membership.setStartDate(rs.getDate("start_date").toLocalDate());
                membership.setEndDate(rs.getDate("end_date").toLocalDate());
                membership.setPaymentStatus(rs.getString("payment_status"));

                // Lấy cả package nếu cần (gọi lại PackageDao)
                int packageId = rs.getInt("package_id");
                PackageDao packageDao = new PackageDao();
                // Lấy ra đối tượng Package trước
                Package pkg = packageDao.getPackageById(packageId);
                // Chuyển sang MembershipPackage
                MembershipPackage memPkg = packageDao.convertToMembershipPackage(pkg);
                membership.setMembershipPackage(memPkg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return membership;
    }

// 2. Update ngày hết hạn (renew)
    public boolean updateMembershipEndDate(int membershipId, java.time.LocalDate newEndDate) {
        String sql = "UPDATE customer_memberships SET end_date = ? WHERE membership_id = ?";
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(newEndDate));
            ps.setInt(2, membershipId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

// 3. Cancel membership (set trạng thái)
    public boolean cancelMembership(int membershipId) {
        String sql = "UPDATE customer_memberships SET payment_status = 'cancelled' WHERE membership_id = ?";
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, membershipId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
