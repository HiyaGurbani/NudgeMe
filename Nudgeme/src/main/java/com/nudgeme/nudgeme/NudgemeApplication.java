package com.nudgeme.nudgeme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.SQLException;

@SpringBootApplication
public class NudgemeApplication {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(NudgemeApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void testDbConnection() throws SQLException {
        System.out.println("Connected to DB: " + dataSource.getConnection().getMetaData().getURL());
    }
}
