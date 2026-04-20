package ie.setu.hcs.dao.impl;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.interfaces.DepartmentDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;


public class DepartmentDAOImpl implements DepartmentDAO {
    public Integer findByName(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            return null;
        }

        // creating sql variable with sql statement
        String sql = "SELECT dep_id FROM departments WHERE name = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setString(1, name);

            // executing the query in the database
            ResultSet rs = pstmt.executeQuery();

            // validating if there is a result
            if (rs.next()) {
                // inserting the departmentId taken from executed query
                return rs.getInt(1); // or rs.getInt(1)

            }

            // return null
            return createDepartment(name);
        }
    }

    private Integer createDepartment(String name) throws SQLException {
        String sql = "INSERT INTO departments (name) VALUES (?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name.trim());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return null;
    }

    @Override
    public LinkedHashMap<Integer, String> findAllDepartments() throws SQLException {
        LinkedHashMap<Integer, String> departments = new LinkedHashMap<>();
        String sql = "SELECT dep_id, name FROM departments ORDER BY name";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                departments.put(rs.getInt("dep_id"), rs.getString("name"));
            }
        }

        return departments;
    }

    @Override
    public String findNameById(Integer depId) throws SQLException {
        if (depId == null) {
            return null;
        }

        String sql = "SELECT name FROM departments WHERE dep_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, depId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        }
    }
}
