package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RelationshipTest {
    private final class Master {
        final ReadOnlyListWrapper<TestA> testAs = RelationshipReadOnlyListWrapper.withOtherList(this, e -> e.masters);
        final ReadOnlyListWrapper<TestB> testBs = RelationshipReadOnlyListWrapper.withOtherProperty(this, e -> e.master);

        final ReadOnlyObjectWrapper<TestC> testC = RelationshipReadOnlyObjectWrapper.withOtherList(this, e -> e.masters);
        final ReadOnlyObjectWrapper<TestD> testD = RelationshipReadOnlyObjectWrapper.withOtherProperty(this, e -> e.master);
    }

    private final class TestA {
        final ReadOnlyListWrapper<Master> masters = RelationshipReadOnlyListWrapper.withOtherList(this, e -> e.testAs);
    }

    private final class TestB {
        final ReadOnlyObjectWrapper<Master> master = RelationshipReadOnlyObjectWrapper.withOtherList(this, e -> e.testBs);
    }

    private final class TestC {
        final ReadOnlyListWrapper<Master> masters = RelationshipReadOnlyListWrapper.withOtherProperty(this, e -> e.testC);
    }

    private final class TestD {
        final ReadOnlyObjectWrapper<Master> master = RelationshipReadOnlyObjectWrapper.withOtherProperty(this, e -> e.testD);
    }

    @Test
    void testListToList() {
        final Master m = new Master();
        final TestA t1 = new TestA();
        final TestA t2 = new TestA();

        Assertions.assertDoesNotThrow(() -> m.testAs.add(t1));

        Assertions.assertTrue(m.testAs.contains(t1));
        Assertions.assertTrue(t1.masters.contains(m));
        Assertions.assertEquals(m.testAs.size(), 1);
        Assertions.assertEquals(t1.masters.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testAs.add(t1));

        Assertions.assertTrue(m.testAs.contains(t1));
        Assertions.assertTrue(t1.masters.contains(m));
        Assertions.assertEquals(m.testAs.size(), 1);
        Assertions.assertEquals(t1.masters.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testAs.add(t2));

        Assertions.assertTrue(m.testAs.contains(t1));
        Assertions.assertTrue(m.testAs.contains(t2));
        Assertions.assertTrue(t1.masters.contains(m));
        Assertions.assertTrue(t2.masters.contains(m));
        Assertions.assertEquals(m.testAs.size(), 2);
        Assertions.assertEquals(t1.masters.size(), 1);
        Assertions.assertEquals(t2.masters.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testAs.remove(t1));

        Assertions.assertFalse(m.testAs.contains(t1));
        Assertions.assertTrue(m.testAs.contains(t2));
        Assertions.assertFalse(t1.masters.contains(m));
        Assertions.assertTrue(t2.masters.contains(m));
        Assertions.assertEquals(m.testAs.size(), 1);
        Assertions.assertEquals(t1.masters.size(), 0);
        Assertions.assertEquals(t2.masters.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testAs.remove(t2));

        Assertions.assertFalse(m.testAs.contains(t1));
        Assertions.assertFalse(m.testAs.contains(t2));
        Assertions.assertFalse(t1.masters.contains(m));
        Assertions.assertFalse(t2.masters.contains(m));
        Assertions.assertEquals(m.testAs.size(), 0);
        Assertions.assertEquals(t1.masters.size(), 0);
        Assertions.assertEquals(t2.masters.size(), 0);
    }

    @Test
    void testListToProperty() {
        final Master m = new Master();
        final TestB t1 = new TestB();
        final TestB t2 = new TestB();

        Assertions.assertDoesNotThrow(() -> m.testBs.add(t1));

        Assertions.assertTrue(m.testBs.contains(t1));
        Assertions.assertEquals(t1.master.get(), m);
        Assertions.assertEquals(m.testBs.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testBs.add(t1));

        Assertions.assertTrue(m.testBs.contains(t1));
        Assertions.assertEquals(t1.master.get(), m);
        Assertions.assertEquals(m.testBs.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testBs.add(t2));

        Assertions.assertTrue(m.testBs.contains(t1));
        Assertions.assertTrue(m.testBs.contains(t2));
        Assertions.assertEquals(t1.master.get(), m);
        Assertions.assertEquals(t2.master.get(), m);
        Assertions.assertEquals(m.testBs.size(), 2);

        Assertions.assertDoesNotThrow(() -> m.testBs.remove(t1));

        Assertions.assertFalse(m.testBs.contains(t1));
        Assertions.assertTrue(m.testBs.contains(t2));
        Assertions.assertNull(t1.master.get());
        Assertions.assertEquals(t2.master.get(), m);
        Assertions.assertEquals(m.testBs.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testBs.remove(t2));

        Assertions.assertFalse(m.testBs.contains(t1));
        Assertions.assertFalse(m.testBs.contains(t2));
        Assertions.assertNull(t1.master.get());
        Assertions.assertNull(t2.master.get());
        Assertions.assertEquals(m.testBs.size(), 0);
    }

    @Test
    void testPropertyToList() {
        final Master m = new Master();
        final TestC t1 = new TestC();
        final TestC t2 = new TestC();

        Assertions.assertDoesNotThrow(() -> m.testC.set(t1));

        Assertions.assertEquals(m.testC.get(), t1);
        Assertions.assertTrue(t1.masters.contains(m));
        Assertions.assertEquals(t1.masters.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testC.set(t1));

        Assertions.assertEquals(m.testC.get(), t1);
        Assertions.assertTrue(t1.masters.contains(m));
        Assertions.assertEquals(t1.masters.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testC.set(t2));

        Assertions.assertEquals(m.testC.get(), t2);
        Assertions.assertFalse(t1.masters.contains(m));
        Assertions.assertTrue(t2.masters.contains(m));
        Assertions.assertEquals(t1.masters.size(), 0);
        Assertions.assertEquals(t2.masters.size(), 1);

        Assertions.assertDoesNotThrow(() -> m.testC.set(null));

        Assertions.assertNull(m.testC.get());
        Assertions.assertFalse(t1.masters.contains(m));
        Assertions.assertFalse(t2.masters.contains(m));
        Assertions.assertEquals(t1.masters.size(), 0);
        Assertions.assertEquals(t2.masters.size(), 0);
    }

    @Test
    void testPropertyToProperty() {
        final Master m = new Master();
        final TestD t1 = new TestD();
        final TestD t2 = new TestD();

        Assertions.assertDoesNotThrow(() -> m.testD.set(t1));

        Assertions.assertEquals(m.testD.get(), t1);
        Assertions.assertEquals(t1.master.get(), m);

        Assertions.assertDoesNotThrow(() -> m.testD.set(t1));

        Assertions.assertEquals(m.testD.get(), t1);
        Assertions.assertEquals(t1.master.get(), m);

        Assertions.assertDoesNotThrow(() -> m.testD.set(t2));

        Assertions.assertEquals(m.testD.get(), t2);
        Assertions.assertNull(t1.master.get());
        Assertions.assertEquals(t2.master.get(), m);

        Assertions.assertDoesNotThrow(() -> m.testD.set(null));

        Assertions.assertNull(m.testD.get());
        Assertions.assertNull(t1.master.get());
        Assertions.assertNull(t2.master.get());
    }
}