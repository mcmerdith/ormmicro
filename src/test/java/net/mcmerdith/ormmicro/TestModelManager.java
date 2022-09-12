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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;


import static org.junit.Assert.*;

public class TestModelManager {
    private static final HikariConfig sqliteConfig = new HikariConfig();
    private static final SessionFactory sessionFactory;

    static {
        // Make the logs easier to read
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        sqliteConfig.setDriverClassName("org.sqlite.JDBC");
        sqliteConfig.setJdbcUrl("jdbc:sqlite:signshop.db");

        sessionFactory = new SessionFactory.Builder(sqliteConfig).build();
    }

    @Test
    public void testModelManager() {
        ModelManager modelManager = sessionFactory.getModelManager();

        SqlModel<TestModel1> model1 = modelManager.getModel(TestModel1.class);
        SqlModel<TestModel2> model2 = modelManager.getModel(TestModel2.class);

        /*
        Model Validation
         */

        Map<String, ColumnDefinition> model1Definitions = model1.getColumnDefinitions();

        assertEquals("`byte` failed validation", ColumnType.TINYINT, model1Definitions.get("pByte").getColumnType());
        assertEquals("`Byte` failed validation", ColumnType.TINYINT, model1Definitions.get("bByte").getColumnType());

        assertEquals("`short` failed validation", ColumnType.MEDIUMINT, model1Definitions.get("pShort").getColumnType());
        assertEquals("`Short` failed validation", ColumnType.MEDIUMINT, model1Definitions.get("bShort").getColumnType());

        assertEquals("`int` failed validation", ColumnType.INTEGER, model1Definitions.get("pInt").getColumnType());
        assertEquals("`Integer` failed validation", ColumnType.INTEGER, model1Definitions.get("bInt").getColumnType());

        assertEquals("`long` failed validation", ColumnType.BIGINT, model1Definitions.get("pLong").getColumnType());
        assertEquals("`Long` failed validation", ColumnType.BIGINT, model1Definitions.get("bLong").getColumnType());

        assertEquals("`float` failed validation", ColumnType.FLOAT, model1Definitions.get("pFloat").getColumnType());
        assertEquals("`Float` failed validation", ColumnType.FLOAT, model1Definitions.get("bFloat").getColumnType());

        assertEquals("`double` failed validation", ColumnType.DOUBLE, model1Definitions.get("pDouble").getColumnType());
        assertEquals("`Double` failed validation", ColumnType.DOUBLE, model1Definitions.get("bDouble").getColumnType());

        assertEquals("`boolean` failed validation", ColumnType.BOOLEAN, model1Definitions.get("pBoolean").getColumnType());
        assertEquals("`Boolean` failed validation", ColumnType.BOOLEAN, model1Definitions.get("bBoolean").getColumnType());

        assertEquals("`BigDecimal` failed validation", null, model1Definitions.get("bigDecimal1").getColumnType());
        assertEquals("`BigDecimal`[DECIMAL(25,5)] failed validation", new ColumnType.Builder(SqlType.DECIMAL).setDigits(25).setDecimals(5).build(), model1Definitions.get("bigDecimal2").getColumnType());

        assertEquals("`String` failed validation", ColumnType.STRING, model1Definitions.get("string").getColumnType());

        assertEquals("`Enum`[VALUE] failed validation", ColumnType.STRING, model1Definitions.get("enum1").getColumnType());
        assertEquals("`Enum`[ORDINAL] failed validation", ColumnType.INTEGER, model1Definitions.get("enum2").getColumnType());

        assertEquals("`Foreign Key`[foreign(column)] failed validation", ColumnType.BIGINT, model1Definitions.get("externalReferenceId").getColumnType());

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
