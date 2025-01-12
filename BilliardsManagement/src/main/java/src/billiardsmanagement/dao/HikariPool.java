package src.billiardsmanagement.dao;

import com.sun.source.tree.TryTree;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariPool {
    private static final String URL = "jdbc:mysql://localhost:3306/biamanagement";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static HikariDataSource dataSource;

    static{
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(5);  // maximum connection amount in the pool
        config.setMinimumIdle(2);     //  minimum connection amount in the pool
        config.setIdleTimeout(60000); // = 60s, 1min
        config.setMaxLifetime(1800000); // = 180min

        dataSource = new HikariDataSource(config);
    }

    // if getConnection() fail, return null (required null check when using)
    public static Connection getHikariConnection(){
        try{
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println("Error : "+e.getMessage());
            return null;
        }
    }

    public static void closeHikariPool(){
        if(dataSource!=null) dataSource.close();
    }
}
