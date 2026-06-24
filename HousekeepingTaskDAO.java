package com.example.demo10.raffles.hotelmgmt.dao;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.model.HousekeepingTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp; // For LocalDateTime mapping
import java.sql.Types;
import java.sql.Date;

public class HousekeepingTaskDAO {

    public ObservableList<HousekeepingTask> getAllHousekeepingTasks() {
        ObservableList<HousekeepingTask> tasks = FXCollections.observableArrayList();
        String sql = "SELECT ht.TaskID, ht.RoomID, r.RoomNumber, ht.AssignedEmployeeID, " +
                "e.FirstName || ' ' || e.LastName AS EmployeeName, " +
                "ht.TaskType, ht.ScheduledDate, ht.Status, ht.CompletionTime, ht.Notes " +
                "FROM HousekeepingTask ht " +
                "LEFT JOIN Room r ON ht.RoomID = r.RoomID " +
                "LEFT JOIN Employee e ON ht.AssignedEmployeeID = e.EmployeeID " +
                "ORDER BY ht.ScheduledDate DESC, ht.Status, ht.TaskID DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(mapRowToHousekeepingTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all housekeeping tasks: " + e.getMessage());
        }
        return tasks;
    }

    public boolean addHousekeepingTask(HousekeepingTask task) {
        String sql = "INSERT INTO HousekeepingTask (RoomID, AssignedEmployeeID, TaskType, ScheduledDate, Status, CompletionTime, Notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, task.getRoomID());
            if (task.getAssignedEmployeeID() != null) pstmt.setInt(2, task.getAssignedEmployeeID()); else pstmt.setNull(2, Types.INTEGER);
            pstmt.setString(3, task.getTaskType());
            if (task.getScheduledDate() != null) pstmt.setDate(4, Date.valueOf(task.getScheduledDate())); else pstmt.setNull(4, Types.DATE);
            pstmt.setString(5, task.getStatus() != null ? task.getStatus() : "Pending");
            if (task.getCompletionTime() != null) pstmt.setTimestamp(6, Timestamp.valueOf(task.getCompletionTime())); else pstmt.setNull(6, Types.TIMESTAMP);
            pstmt.setString(7, task.getNotes());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setTaskID(generatedKeys.getInt(1));
                    }
                }
                // Optionally, update the Room status based on the task (e.g., if task is 'Full Clean', room status might change)
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding housekeeping task: " + e.getMessage());
            return false;
        }
    }

    public boolean updateHousekeepingTaskStatus(int taskId, String newStatus, java.time.LocalDateTime completionTime) {
        String sql = "UPDATE HousekeepingTask SET Status = ?, CompletionTime = ? WHERE TaskID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            if (completionTime != null) {
                pstmt.setTimestamp(2, Timestamp.valueOf(completionTime));
            } else {
                pstmt.setNull(2, Types.TIMESTAMP);
            }
            pstmt.setInt(3, taskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating housekeeping task status for ID " + taskId + ": " + e.getMessage());
            return false;
        }
    }

    // TODO: Add more specific query methods (e.g., getTasksByDate, getTasksByEmployee, getTasksByRoom)

    private HousekeepingTask mapRowToHousekeepingTask(ResultSet rs) throws SQLException {
        HousekeepingTask task = new HousekeepingTask();
        task.setTaskID(rs.getInt("TaskID"));
        task.setRoomID(rs.getInt("RoomID"));
        task.setRoomNumber(rs.getString("RoomNumber")); // From join

        int empId = rs.getInt("AssignedEmployeeID");
        if (!rs.wasNull()) {
            task.setAssignedEmployeeID(empId);
        }
        task.setEmployeeName(rs.getString("EmployeeName")); // From join

        task.setTaskType(rs.getString("TaskType"));
        Date scheduledDateSql = rs.getDate("ScheduledDate");
        if (scheduledDateSql != null) {
            task.setScheduledDate(scheduledDateSql.toLocalDate());
        }
        task.setStatus(rs.getString("Status"));
        Timestamp completionTs = rs.getTimestamp("CompletionTime");
        if (completionTs != null) {
            task.setCompletionTime(completionTs.toLocalDateTime());
        }
        task.setNotes(rs.getString("Notes"));
        return task;
    }
}