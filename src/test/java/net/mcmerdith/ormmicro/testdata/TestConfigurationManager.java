package net.mcmerdith.ormmicro.testdata;

import com.zaxxer.hikari.HikariConfig;
import net.mcmerdith.ormmicro.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestConfigurationManager {
    private static final List<HikariConfig> configurations = new ArrayList<>();

    static {
        // Make the logs easier to read
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        HikariConfig sqliteConfig = new HikariConfig();
        sqliteConfig.setDriverClassName("org.sqlite.JDBC");
        sqliteConfig.setJdbcUrl("jdbc:sqlite:signshop.db");
        sqliteConfig.setMaximumPoolSize(1);
        sqliteConfig.setMinimumIdle(1);
        sqliteConfig.setPoolName("Schema");

        configurations.add(sqliteConfig);
    }

    public static List<SessionFactory> getSessionFactories() {
        return configurations.stream().map(config -> new SessionFactory.Builder(config).build()).collect(Collectors.toList());
    }
}
