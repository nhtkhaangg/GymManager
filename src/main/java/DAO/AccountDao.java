package DAO;

import Model.Account;
import db.DBcontext;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDao extends DBcontext {

    public boolean updateBasicInfo(Account account) {
        if (account == null) {
            System.err.println("Account is null");
            return false;
        }

        int accountId = account.getAccountId();
        String username = account.getUsername();
        byte[] avatarStream = account.getAvatar();

        if (accountId <= 0 || username == null || username.trim().isEmpty()) {
            System.err.println("Invalid account input: accountId=" + accountId + ", username=" + username);
            return false;
        }

        String sql;
        boolean hasAvatar = avatarStream != null;

        if (hasAvatar) {
            sql = "UPDATE accounts SET username = ?, avatar = ? WHERE account_id = ?";
        } else {
            sql = "UPDATE accounts SET username = ? WHERE account_id = ?";
        }

        try ( Connection conn = getConnection();  PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            if (hasAvatar) {
                stmt.setBytes(2, account.getAvatar());
                stmt.setInt(3, accountId);
            } else {
                stmt.setInt(2, accountId);
            }

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Account updated successfully: ID = " + accountId);
                return true;
            } else {
                System.err.println("No account updated. Possibly wrong accountId: " + accountId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("SQL error during account update:");
            System.err.println("accountId = " + accountId);
            System.err.println("username = " + username);
            System.err.println("hasAvatar = " + hasAvatar);
            e.printStackTrace();
            return false;
        }
    }

    public List<Account> getAllCustomerAccountsNotInCustomerTable() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.account_id, a.username "
                + "FROM accounts a "
                + "WHERE a.role = 'customer' "
                + "AND NOT EXISTS (SELECT 1 FROM customers c WHERE c.account_id = a.account_id)";

        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Account acc = new Account();
                acc.setAccountId(rs.getInt("account_id"));
                acc.setUsername(rs.getString("username"));
                accounts.add(acc);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi khi lấy danh sách account chưa là customer: " + e.getMessage());
        }

        return accounts;
    }

    public int countAccounts() {
        String sql = "SELECT COUNT(*) FROM accounts";
        try ( Connection conn = getConnection();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    // Phương thức lấy tài khoản theo accountId

    public Account getAccountById(int accountId) {
        Account account = null;
        String sql = "SELECT * FROM accounts WHERE account_id = ?"; // Câu truy vấn lấy thông tin tài khoản

        try ( Connection con = getConnection(); // Giả sử bạn có một lớp để kết nối CSDL
                  PreparedStatement pst = con.prepareStatement(sql)) {

            // Gán tham số vào câu truy vấn
            pst.setInt(1, accountId);

            // Thực hiện câu truy vấn và lấy kết quả
            ResultSet rs = pst.executeQuery();

            // Kiểm tra nếu có tài khoản
            if (rs.next()) {
                // Tạo đối tượng tài khoản và gán thông tin từ CSDL
                account = new Account();
                account.setAccountId(rs.getInt("account_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
                account.setRole(rs.getString("role"));
                // Gán các thuộc tính khác nếu cần
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return account;
    }
}
