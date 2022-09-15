package net.mcmerdith.ormmicro;

import com.zaxxer.hikari.HikariConfig;
import net.mcmerdith.ormmicro.modeling.ColumnDefinition;
import net.mcmerdith.ormmicro.modeling.MappedSqlModel;
import net.mcmerdith.ormmicro.modeling.SqlModel;
import net.mcmerdith.ormmicro.testmodels.TestModel1;
import net.mcmerdith.ormmicro.testmodels.TestModel2;
import net.mcmerdith.ormmicro.typing.ColumnType;
import net.mcmerdith.ormmicro.typing.SqlType;
import org.junit.Test;
import org.sqlite.SQLiteDataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;


import static org.junit.Assert.*;

public class TestModelManager {
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

    @Test
    public void testModelManager() {
        for (HikariConfig config : configurations) {
            SessionFactory sessionFactory = new SessionFactory.Builder(config).build();

//            try (Session session = sessionFactory.getCurrentSession()) {
//                session.executeSql("CREATE TABLE IF NOT EXISTS test(id INTEGER PRIMARY KEY, test TEXT NOT NULL)");
//                assertEquals(1, session.executeSqlUpdate("INSERT INTO TEST (test) VALUES ('testvalue');"));
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

            ModelManager modelManager = sessionFactory.getModelManager();

            SqlModel<TestModel1> model1 = modelManager.getModel(TestModel1.class);
            SqlModel<TestModel2> model2 = modelManager.getModel(TestModel2.class);

        /*
        Model Validation
         */

            Map<String, ColumnDefinition> model1Definitions = model1.getColumnDefinitions();

            ColumnDefinition pByte = model1Definitions.get("pByte");
            ColumnDefinition bByte = model1Definitions.get("bByte");

            assertEquals("`byte` failed validation", ColumnType.TINYINT, pByte.getColumnType());
            assertEquals("`Byte` failed validation", ColumnType.TINYINT, bByte.getColumnType());

            ColumnDefinition pShort = model1Definitions.get("pShort");
            ColumnDefinition bShort = model1Definitions.get("bShort");
            assertEquals("`short` failed validation", ColumnType.MEDIUMINT, pShort.getColumnType());
            assertEquals("`Short` failed validation", ColumnType.MEDIUMINT, bShort.getColumnType());

            ColumnDefinition pInt = model1Definitions.get("pInt");
            ColumnDefinition bInt = model1Definitions.get("bInt");
            assertEquals("`int` failed validation", ColumnType.INTEGER, pInt.getColumnType());
            assertEquals("`Integer` failed validation", ColumnType.INTEGER, bInt.getColumnType());

            ColumnDefinition pLong = model1Definitions.get("pLong");
            ColumnDefinition bLong = model1Definitions.get("bLong");
            assertEquals("`long` failed validation", ColumnType.BIGINT, pLong.getColumnType());
            assertEquals("`Long` failed validation", ColumnType.BIGINT, bLong.getColumnType());

            ColumnDefinition pFloat = model1Definitions.get("pFloat");
            ColumnDefinition bFloat = model1Definitions.get("bFloat");
            assertEquals("`float` failed validation", ColumnType.FLOAT, pFloat.getColumnType());
            assertEquals("`Float` failed validation", ColumnType.FLOAT, bFloat.getColumnType());

            ColumnDefinition pDouble = model1Definitions.get("pDouble");
            ColumnDefinition bDouble = model1Definitions.get("bDouble");
            assertEquals("`double` failed validation", ColumnType.DOUBLE, pDouble.getColumnType());
            assertEquals("`Double` failed validation", ColumnType.DOUBLE, bDouble.getColumnType());

            ColumnDefinition pBoolean = model1Definitions.get("pBoolean");
            ColumnDefinition bBoolean = model1Definitions.get("bBoolean");
            assertEquals("`boolean` failed validation", ColumnType.BOOLEAN, pBoolean.getColumnType());
            assertEquals("`Boolean` failed validation", ColumnType.BOOLEAN, bBoolean.getColumnType());

            ColumnDefinition bigDecimal1 = model1Definitions.get("bigDecimal1");
            ColumnDefinition bigDecimal2 = model1Definitions.get("bigDecimal2");
            assertNull("`BigDecimal` failed validation", bigDecimal1.getColumnType());
            assertEquals("`BigDecimal`[DECIMAL(25,5)] failed validation", new ColumnType.Builder(SqlType.DECIMAL).setDigits(25).setDecimals(5).build(), bigDecimal2.getColumnType());

            ColumnDefinition string = model1Definitions.get("string");
            assertEquals("`String` failed validation", ColumnType.STRING, string.getColumnType());

            ColumnDefinition enum1 = model1Definitions.get("enum1");
            ColumnDefinition enum2 = model1Definitions.get("enum2");
            assertEquals("`Enum`[VALUE] failed validation", ColumnType.STRING, enum1.getColumnType());
            assertEquals("`Enum`[ORDINAL] failed validation", ColumnType.INTEGER, enum2.getColumnType());

            ColumnDefinition externalReferenceId = model1Definitions.get("externalReferenceId");
            assertEquals("`Foreign Key`[foreign(column)] failed validation", ColumnType.BIGINT, externalReferenceId.getColumnType());

        /*
        Mapping validations
         */

            Properties testProperties = new Properties();
            testProperties.setProperty("key1", "value1");
            testProperties.setProperty("key2", "value2");

            TestModel2 instance2 = new TestModel2(true, new BigDecimal(12345), testProperties);
            TestModel1 instance1 = new TestModel1("test string", instance2);

            MappedSqlModel<TestModel1> mappedModel1 = model1.mapObject(instance1);

            Map<String, Object> model1Columns = mappedModel1.getColumns();

        /*
        Basic Fields
         */



        /*
        Foreign Key
         */

            List<Object> fObjects = mappedModel1.getForeignObjects();

            assertEquals("Only 2 Foreign Key Expected", 2, fObjects.size());
            assertTrue("Expected Foreign Model to be TestModel2", fObjects.get(1) instanceof TestModel2);

            MappedSqlModel<TestModel2> mappedModel2 = model2.mapObject((TestModel2) fObjects.get(1));
        }
    }
}
