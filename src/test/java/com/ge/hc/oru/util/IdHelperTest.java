package com.ge.hc.oru.util;

import junit.framework.TestCase;
import org.junit.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class IdHelperTest extends TestCase {

    private Collection collection;

    @BeforeClass
    public static void oneTimeSetUp() {
        // one-time initialization code
        System.out.println("@BeforeClass - oneTimeSetUp");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        // one-time cleanup code
        System.out.println("@AfterClass - oneTimeTearDown");
    }

    @Before
    public void setUp() {
        collection = new ArrayList();

        System.out.println("@Before - setUp");
    }

    @After
    public void tearDown() {
        collection.clear();
        System.out.println("@After - tearDown");
    }


    @Test
    public void testOneItemCollection() {
        collection.add("DR0123405");
        collection.add("DR123405");
        collection.add("0123405");
        collection.add("123405");
        assertEquals(4, collection.size());
        Iterator itr = collection.iterator();
        while(itr.hasNext()) {
            String accessionNumber = (String) itr.next();
            System.out.println("Test with AccessionNumber: " + accessionNumber);
            assertEquals("123405",IdHelper.removePrefixFromAccessionNumber(accessionNumber));
        }
        //String accessionNumber = "DR0123405";
       // assertEquals("123405",IdHelper.removePrefixFromAccessionNumber(accessionNumber));
        System.out.println("@Test - testOneItemCollection");
    }

}