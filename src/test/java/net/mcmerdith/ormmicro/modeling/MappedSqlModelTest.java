package net.mcmerdith.ormmicro.modeling;

import net.mcmerdith.ormmicro.SessionFactory;
import net.mcmerdith.ormmicro.testdata.TestConfigurationManager;
import net.mcmerdith.ormmicro.annotations.Id;
import net.mcmerdith.ormmicro.annotations.Model;
import org.junit.Test;

import static org.junit.Assert.*;

public class MappedSqlModelTest {
    @Model(tableName = "one")
    private static class One {
        @Id
        public long id;

        public String test;

        public One(long id, String test) {
            this.id = id;
            this.test = test;
        }
    }

    @Model(tableName = "two")
    private static class Two {
        @Id
        public long id;

        public String test;

        public Two(long id, String test) {
            this.id = id;
            this.test = test;
        }
    }

    @Model(tableName = "three")
    private static class Three {
        public String test;

        public Three(String test) {
            this.test = test;
        }
    }

    private MappedSqlModel<One> mOne;
    private MappedSqlModel<One> mOneEqual;
    private MappedSqlModel<One> mOneNonEqual;

    private MappedSqlModel<Two> mTwo;
    private MappedSqlModel<Two> mTwoEqual;
    private MappedSqlModel<Two> mTwoNonEqual;

    private MappedSqlModel<Three> mThree;
    private MappedSqlModel<Three> mThreeSimilar;
    private MappedSqlModel<Three> mThreeDifferent;

    private static final One one = new One(1, "hi");
    private static final One oneEqual = new One(1, "bye");
    private static final One oneNonEqual = new One(2, "hi");

    private static final Two two = new Two(1, "hi");
    private static final Two twoEqual = new Two(1, "bye");
    private static final Two twoNonEqual = new Two(2, "hi");

    private static final Three three = new Three("hi");
    private static final Three threeSimilar = new Three("hi");
    private static final Three threeDifferent = new Three("bye");

    private void buildFor(SessionFactory sessionFactory) {
        mOne = sessionFactory.getModelManager().mapObject(one);
        mOneEqual = sessionFactory.getModelManager().mapObject(oneEqual);
        mOneNonEqual = sessionFactory.getModelManager().mapObject(oneNonEqual);
        mTwo = sessionFactory.getModelManager().mapObject(two);
        mTwoEqual = sessionFactory.getModelManager().mapObject(twoEqual);
        mTwoNonEqual = sessionFactory.getModelManager().mapObject(twoNonEqual);
        mThree = sessionFactory.getModelManager().mapObject(three);
        mThreeSimilar = sessionFactory.getModelManager().mapObject(threeSimilar);
        mThreeDifferent = sessionFactory.getModelManager().mapObject(threeDifferent);
    }

    @Test
    public void testHashCode() {
        for (SessionFactory sessionFactory : TestConfigurationManager.getSessionFactories()) {
            buildFor(sessionFactory);

            assertEquals(mOne.hashCode(), mOneEqual.hashCode());
            assertNotEquals(mOne.hashCode(), mOneNonEqual.hashCode());
            assertNotEquals(mOneEqual.hashCode(), mOneNonEqual.hashCode());

            assertEquals(mTwo.hashCode(), mTwoEqual.hashCode());
            assertNotEquals(mTwo.hashCode(), mTwoNonEqual.hashCode());
            assertNotEquals(mTwoEqual.hashCode(), mTwoNonEqual.hashCode());

            assertNotEquals(mThree.hashCode(), mThreeSimilar.hashCode());
            assertNotEquals(mThree.hashCode(), mThreeDifferent.hashCode());
            assertNotEquals(mThreeSimilar.hashCode(), mThreeDifferent.hashCode());

            assertNotEquals(mOne.hashCode(), mTwo.hashCode());
            assertNotEquals(mOne.hashCode(), mTwoEqual.hashCode());
            assertNotEquals(mOne.hashCode(), mTwoNonEqual.hashCode());
            assertNotEquals(mOne.hashCode(), mThree.hashCode());
            assertNotEquals(mOne.hashCode(), mThreeSimilar.hashCode());
            assertNotEquals(mOne.hashCode(), mThreeDifferent.hashCode());

            assertNotEquals(mTwo.hashCode(), mThree.hashCode());
            assertNotEquals(mTwo.hashCode(), mThreeSimilar.hashCode());
            assertNotEquals(mTwo.hashCode(), mThreeDifferent.hashCode());
        }
    }

    @Test
    public void testEquals() {
        for (SessionFactory sessionFactory : TestConfigurationManager.getSessionFactories()) {
            buildFor(sessionFactory);

            assertEquals(mOne, mOneEqual);
            assertNotEquals(mOne, mOneNonEqual);
            assertNotEquals(mOneEqual, mOneNonEqual);

            assertEquals(mTwo, mTwoEqual);
            assertNotEquals(mTwo, mTwoNonEqual);
            assertNotEquals(mTwoEqual, mTwoNonEqual);

            assertNotEquals(mThree, mThreeSimilar);
            assertNotEquals(mThree, mThreeDifferent);
            assertNotEquals(mThreeSimilar, mThreeDifferent);

            assertNotEquals(mOne, mTwo);
            assertNotEquals(mOne, mTwoEqual);
            assertNotEquals(mOne, mTwoNonEqual);
            assertNotEquals(mOne, mThree);
            assertNotEquals(mOne, mThreeSimilar);
            assertNotEquals(mOne, mThreeDifferent);

            assertNotEquals(mTwo, mThree);
            assertNotEquals(mTwo, mThreeSimilar);
            assertNotEquals(mTwo, mThreeDifferent);
        }
    }
}