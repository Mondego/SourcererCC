import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class AllTests {

    public static void main(String[] args) throws Exception {
        Class testClass = AllTests.class;
        ArrayList<Method> setups = new ArrayList<Method>();
        ArrayList<Method> tearDowns = new ArrayList<Method>();
        for (Method method : testClass.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && method.getAnnotation(Ignore.class) == null) {
                if (method.getAnnotation(Before.class) != null) {
                    setups.add(method);
                }
                if (method.getAnnotation(After.class) != null) {
                    setups.add(method);
                }
            }
        }
        System.out.println("Starting all tests.");
        Object instance = testClass.newInstance();
        for (Method method : testClass.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && method.getAnnotation(Ignore.class) == null) {
                Test testAnnotation = method.getAnnotation(Test.class);
                if (testAnnotation != null) {
                    for (Method setup : setups) {
                        setup.invoke(instance, (Object[]) null);
                    }
                    Class expectedException = testAnnotation.expected();
                    if (expectedException.getName().equals("org.junit.Test$None")) {
                        expectedException = null;
                    }
                    try {
                        method.invoke(instance, (Object[]) null);
                    } catch (Exception e) {
                        if (expectedException == null) {
                            System.out.println(testClass.getName() + "." + method.getName() + ": " + e.getCause().getMessage());
                            new BufferedReader(new InputStreamReader(System.in)).readLine();
                        } else {
                            if (!e.getCause().getClass().equals(testAnnotation.expected())) {
                                System.out.println(testClass.getName() + "." + method.getName() + ": " + "Exception expected: " + testAnnotation.expected().getName() + ", Exception thrown: " + e.getCause().getMessage());
                                new BufferedReader(new InputStreamReader(System.in)).readLine();
                            }
                            expectedException = null;
                        }
                    }
                    if (expectedException != null) {
                        System.out.println(testClass.getName() + "." + method.getName() + ": " + "Expected exception not thrown: " + testAnnotation.expected().getName());
                        new BufferedReader(new InputStreamReader(System.in)).readLine();
                    }
                    for (Method tearDown : tearDowns) {
                        tearDown.invoke(instance, (Object[]) null);
                    }
                }
            }
        }
        System.out.println("Done with all tests.");
    }

    private static String generateString(char letter, int count) {
        StringBuffer buffer = new StringBuffer(count);
        for (int i = 0; i < count; i++) {
            buffer.append(letter);
        }
        return buffer.toString();
    }

    private static void assertException(Exception expected, Exception actual) {
        Assert.assertEquals(expected.getClass(), actual.getClass());
        Assert.assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    public void test1() throws Exception {
        CsvReader reader = CsvReader.parse("1,2");
        Assert.assertEquals("", reader.getRawRecord());
        Assert.assertEquals("", reader.get(0));
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals("2", reader.get(1));
        Assert.assertEquals(',', reader.getDelimiter());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals("1,2", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test2() throws Exception {
        String data = "\"bob said, \"\"Hey!\"\"\",2, 3 ";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("bob said, \"Hey!\"", reader.get(0));
        Assert.assertEquals("2", reader.get(1));
        Assert.assertEquals("3", reader.get(2));
        Assert.assertEquals(',', reader.getDelimiter());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(3, reader.getColumnCount());
        Assert.assertEquals("\"bob said, \"\"Hey!\"\"\",2, 3 ", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test3() throws Exception {
        String data = ",";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals("", reader.get(1));
        Assert.assertEquals(',', reader.getDelimiter());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals(",", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test4() throws Exception {
        String data = "1\r2";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("2", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("2", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test5() throws Exception {
        String data = "1\n2";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("2", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("2", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test6() throws Exception {
        String data = "1\r\n2";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("2", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("2", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test7() throws Exception {
        String data = "1\r";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test8() throws Exception {
        String data = "1\n";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test9() throws Exception {
        String data = "1\r\n";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test10() throws Exception {
        String data = "1\r2\n";
        CsvReader reader = CsvReader.parse(data);
        reader.setDelimiter('\r');
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals("2", reader.get(1));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals("1\r2", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test11() throws Exception {
        String data = "\"July 4th, 2005\"";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("July 4th, 2005", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("\"July 4th, 2005\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test12() throws Exception {
        String data = " 1";
        CsvReader reader = CsvReader.parse(data);
        reader.setTrimWhitespace(false);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(" 1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals(" 1", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test13() throws Exception {
        String data = "";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test14() throws Exception {
        String data = "user_id,name\r\n1,Bruce";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readHeaders());
        Assert.assertEquals("user_id,name", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals("Bruce", reader.get(1));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals(0, reader.getIndex("user_id"));
        Assert.assertEquals(1, reader.getIndex("name"));
        Assert.assertEquals("user_id", reader.getHeader(0));
        Assert.assertEquals("name", reader.getHeader(1));
        Assert.assertEquals("1", reader.get("user_id"));
        Assert.assertEquals("Bruce", reader.get("name"));
        Assert.assertEquals("1,Bruce", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test15() throws Exception {
        String data = "\"data \r\n here\"";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("data \r\n here", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("\"data \r\n here\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test16() throws Exception {
        String data = "\r\r\n1\r";
        CsvReader reader = CsvReader.parse(data);
        reader.setDelimiter('\r');
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals("", reader.get(1));
        Assert.assertEquals("", reader.get(2));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(3, reader.getColumnCount());
        Assert.assertEquals("\r\r", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals("", reader.get(1));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals("1\r", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test17() throws Exception {
        String data = "\"double\"\"\"\"double quotes\"";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("double\"\"double quotes", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("\"double\"\"\"\"double quotes\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test18() throws Exception {
        String data = "1\r";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test19() throws Exception {
        String data = "1\r\n";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test20() throws Exception {
        String data = "1\n";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test21() throws Exception {
        String data = "'bob said, ''Hey!''',2, 3 ";
        CsvReader reader = CsvReader.parse(data);
        reader.setTextQualifier('\'');
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("bob said, 'Hey!'", reader.get(0));
        Assert.assertEquals("2", reader.get(1));
        Assert.assertEquals("3", reader.get(2));
        Assert.assertEquals(',', reader.getDelimiter());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(3, reader.getColumnCount());
        Assert.assertEquals("'bob said, ''Hey!''',2, 3 ", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test22() throws Exception {
        String data = "\"data \"\" here\"";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("\"data \"\" here\"", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("\"data \"\" here\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test23() throws Exception {
        String data = generateString('a', 75) + "," + generateString('b', 75);
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(reader.get(0), generateString('a', 75));
        Assert.assertEquals(reader.get(1), generateString('b', 75));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals(generateString('a', 75) + "," + generateString('b', 75), reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test24() throws Exception {
        String data = "1\r\n\r\n1";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test25() throws Exception {
        String data = "1\r\n# bunch of crazy stuff here\r\n1";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        reader.setUseComments(true);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test26() throws Exception {
        String data = "\"Mac \"The Knife\" Peter\",\"Boswell, Jr.\"";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("Mac ", reader.get(0));
        Assert.assertEquals("Boswell, Jr.", reader.get(1));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals("\"Mac \"The Knife\" Peter\",\"Boswell, Jr.\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test27() throws Exception {
        String data = "\"1\",Bruce\r\n\"2\n\",Toni\r\n\"3\",Brian\r\n";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals("Bruce", reader.get(1));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals("\"1\",Bruce", reader.getRawRecord());
        Assert.assertTrue(reader.skipRecord());
        Assert.assertEquals("\"2\n\",Toni", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("3", reader.get(0));
        Assert.assertEquals("Brian", reader.get(1));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals("\"3\",Brian", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test28() throws Exception {
        String data = "\"bob said, \\\"Hey!\\\"\",2, 3 ";
        CsvReader reader = CsvReader.parse(data);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("bob said, \"Hey!\"", reader.get(0));
        Assert.assertEquals("2", reader.get(1));
        Assert.assertEquals("3", reader.get(2));
        Assert.assertEquals(',', reader.getDelimiter());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(3, reader.getColumnCount());
        Assert.assertEquals("\"bob said, \\\"Hey!\\\"\",2, 3 ", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test29() throws Exception {
        String data = "\"double\\\"\\\"double quotes\"";
        CsvReader reader = CsvReader.parse(data);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("double\"\"double quotes", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("\"double\\\"\\\"double quotes\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test30() throws Exception {
        String data = "\"double\\\\\\\\double backslash\"";
        CsvReader reader = CsvReader.parse(data);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("double\\\\double backslash", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("\"double\\\\\\\\double backslash\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test31() throws Exception {
        CsvWriter writer = new CsvWriter(new PrintWriter(new OutputStreamWriter(new FileOutputStream("temp.csv"), Charset.forName("UTF-8"))), ',');
        writer.write(" \t \t");
        writer.close();
        CsvReader reader = new CsvReader(new InputStreamReader(new FileInputStream("temp.csv"), Charset.forName("UTF-8")));
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals("\"\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
        new File("temp.csv").delete();
    }

    @Test
    public void test32() throws Exception {
        String data = "\"Mac \"The Knife\" Peter\",\"Boswell, Jr.\"";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("Mac ", reader.get(0));
        Assert.assertEquals("Boswell, Jr.", reader.get(1));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertEquals("\"Mac \"The Knife\" Peter\",\"Boswell, Jr.\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test33() throws Exception {
        String fileName = "somefile.csv";
        new File(fileName).createNewFile();
        try {
            CsvReader reader = new CsvReader(fileName);
            reader.close();
        } finally {
            new File(fileName).delete();
        }
    }

    @Test
    public void test34() throws Exception {
        String data = "\"Chicane\", \"Love on the Run\", \"Knight Rider\", \"This field contains a comma, but it doesn't matter as the field is quoted\"\r\n" + "\"Samuel Barber\", \"Adagio for Strings\", \"Classical\", \"This field contains a double quote character, \"\", but it doesn't matter as it is escaped\"";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("Chicane", reader.get(0));
        Assert.assertEquals("Love on the Run", reader.get(1));
        Assert.assertEquals("Knight Rider", reader.get(2));
        Assert.assertEquals("This field contains a comma, but it doesn't matter as the field is quoted", reader.get(3));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(4, reader.getColumnCount());
        Assert.assertEquals("\"Chicane\", \"Love on the Run\", \"Knight Rider\", \"This field contains a comma, but it doesn't matter as the field is quoted\"", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("Samuel Barber", reader.get(0));
        Assert.assertEquals("Adagio for Strings", reader.get(1));
        Assert.assertEquals("Classical", reader.get(2));
        Assert.assertEquals("This field contains a double quote character, \", but it doesn't matter as it is escaped", reader.get(3));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(4, reader.getColumnCount());
        Assert.assertEquals("\"Samuel Barber\", \"Adagio for Strings\", \"Classical\", \"This field contains a double quote character, \"\", but it doesn't matter as it is escaped\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test35() throws Exception {
        String data = "Chicane, Love on the Run, Knight Rider, \"This field contains a comma, but it doesn't matter as the field is quoted\"";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("Chicane", reader.get(0));
        Assert.assertEquals("Love on the Run", reader.get(1));
        Assert.assertEquals("Knight Rider", reader.get(2));
        Assert.assertEquals("This field contains a comma, but it doesn't matter as the field is quoted", reader.get(3));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(4, reader.getColumnCount());
        Assert.assertEquals("Chicane, Love on the Run, Knight Rider, \"This field contains a comma, but it doesn't matter as the field is quoted\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test36() throws Exception {
        String data = "\"some \\stuff\"";
        CsvReader reader = CsvReader.parse(data);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("some stuff", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("\"some \\stuff\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test37() throws Exception {
        String data = "  \" Chicane\"  junk here  , Love on the Run, Knight Rider, \"This field contains a comma, but it doesn't matter as the field is quoted\"";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(" Chicane", reader.get(0));
        Assert.assertEquals("Love on the Run", reader.get(1));
        Assert.assertEquals("Knight Rider", reader.get(2));
        Assert.assertEquals("This field contains a comma, but it doesn't matter as the field is quoted", reader.get(3));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(4, reader.getColumnCount());
        Assert.assertEquals("  \" Chicane\"  junk here  , Love on the Run, Knight Rider, \"This field contains a comma, but it doesn't matter as the field is quoted\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test38() throws Exception {
        String data = "1\r\n\r\n\"\"\r\n \r\n2";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("\"\"", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(2L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals(" ", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("2", reader.get(0));
        Assert.assertEquals(3L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("2", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test39() throws Exception {
        CsvReader reader = CsvReader.parse("user_id,name\r\n1,Bruce");
        Assert.assertTrue(reader.getSafetySwitch());
        reader.setSafetySwitch(false);
        Assert.assertFalse(reader.getSafetySwitch());
        Assert.assertEquals('#', reader.getComment());
        reader.setComment('!');
        Assert.assertEquals('!', reader.getComment());
        Assert.assertEquals(CsvReader.ESCAPE_MODE_DOUBLED, reader.getEscapeMode());
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertEquals(CsvReader.ESCAPE_MODE_BACKSLASH, reader.getEscapeMode());
        Assert.assertEquals('\0', reader.getRecordDelimiter());
        reader.setRecordDelimiter(';');
        Assert.assertEquals(';', reader.getRecordDelimiter());
        Assert.assertEquals('\"', reader.getTextQualifier());
        reader.setTextQualifier('\'');
        Assert.assertEquals('\'', reader.getTextQualifier());
        Assert.assertTrue(reader.getTrimWhitespace());
        reader.setTrimWhitespace(false);
        Assert.assertFalse(reader.getTrimWhitespace());
        Assert.assertFalse(reader.getUseComments());
        reader.setUseComments(true);
        Assert.assertTrue(reader.getUseComments());
        Assert.assertTrue(reader.getUseTextQualifier());
        reader.setUseTextQualifier(false);
        Assert.assertFalse(reader.getUseTextQualifier());
        reader.close();
    }

    @Test
    public void test40() throws Exception {
        String data = "Chicane, Love on the Run, Knight Rider, This field contains a comma\\, but it doesn't matter as the delimiter is escaped";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("Chicane", reader.get(0));
        Assert.assertEquals("Love on the Run", reader.get(1));
        Assert.assertEquals("Knight Rider", reader.get(2));
        Assert.assertEquals("This field contains a comma, but it doesn't matter as the delimiter is escaped", reader.get(3));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(4, reader.getColumnCount());
        Assert.assertEquals("Chicane, Love on the Run, Knight Rider, This field contains a comma\\, but it doesn't matter as the delimiter is escaped", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test41() throws Exception {
        String data = "double\\\\\\\\double backslash";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("double\\\\double backslash", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test42() throws Exception {
        String data = "some \\stuff";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("some stuff", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test43() throws Exception {
        String data = "\"line 1\\nline 2\",\"line 1\\\nline 2\"";
        CsvReader reader = CsvReader.parse(data);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("line 1\nline 2", reader.get(0));
        Assert.assertEquals("line 1\nline 2", reader.get(1));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test44() throws Exception {
        String data = "line 1\\nline 2,line 1\\\nline 2";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("line 1\nline 2", reader.get(0));
        Assert.assertEquals("line 1\nline 2", reader.get(1));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test45() throws Exception {
        String data = "\"Chicane\", \"Love on the Run\", \"Knight Rider\", \"This field contains a comma, but it doesn't matter as the field is quoted\"i" + "\"Samuel Barber\", \"Adagio for Strings\", \"Classical\", \"This field contains a double quote character, \"\", but it doesn't matter as it is escaped\"";
        CsvReader reader = CsvReader.parse(data);
        Assert.assertTrue(reader.getCaptureRawRecord());
        reader.setCaptureRawRecord(false);
        Assert.assertFalse(reader.getCaptureRawRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.setRecordDelimiter('i');
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("Chicane", reader.get(0));
        Assert.assertEquals("Love on the Run", reader.get(1));
        Assert.assertEquals("Knight Rider", reader.get(2));
        Assert.assertEquals("This field contains a comma, but it doesn't matter as the field is quoted", reader.get(3));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(4, reader.getColumnCount());
        Assert.assertEquals("", reader.getRawRecord());
        Assert.assertFalse(reader.getCaptureRawRecord());
        reader.setCaptureRawRecord(true);
        Assert.assertTrue(reader.getCaptureRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("\"Samuel Barber\", \"Adagio for Strings\", \"Classical\", \"This field contains a double quote character, \"\", but it doesn't matter as it is escaped\"", reader.getRawRecord());
        Assert.assertEquals("Samuel Barber", reader.get(0));
        Assert.assertEquals("Adagio for Strings", reader.get(1));
        Assert.assertEquals("Classical", reader.get(2));
        Assert.assertEquals("This field contains a double quote character, \", but it doesn't matter as it is escaped", reader.get(3));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(4, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        Assert.assertTrue(reader.getCaptureRawRecord());
        Assert.assertEquals("", reader.getRawRecord());
        reader.close();
    }

    @Test
    public void test46() throws Exception {
        String data = "Ch\\icane, Love on the Run, Kn\\ight R\\ider, Th\\is f\\ield conta\\ins an \\i\\, but \\it doesn't matter as \\it \\is escapedi" + "Samuel Barber, Adag\\io for Str\\ings, Class\\ical, Th\\is f\\ield conta\\ins a comma \\, but \\it doesn't matter as \\it \\is escaped";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        reader.setRecordDelimiter('i');
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("Chicane", reader.get(0));
        Assert.assertEquals("Love on the Run", reader.get(1));
        Assert.assertEquals("Knight Rider", reader.get(2));
        Assert.assertEquals("This field contains an i, but it doesn't matter as it is escaped", reader.get(3));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(4, reader.getColumnCount());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("Samuel Barber", reader.get(0));
        Assert.assertEquals("Adagio for Strings", reader.get(1));
        Assert.assertEquals("Classical", reader.get(2));
        Assert.assertEquals("This field contains a comma , but it doesn't matter as it is escaped", reader.get(3));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(4, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test47() throws Exception {
        byte[] buffer;
        String test = "M�nchen";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream, Charset.forName("UTF-8")));
        writer.println(test);
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        CsvReader reader = new CsvReader(new InputStreamReader(new ByteArrayInputStream(buffer), Charset.forName("UTF-8")));
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(test, reader.get(0));
        reader.close();
    }

    @Test
    public void test48() throws Exception {
        byte[] buffer;
        String test = "M�nchen";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream, Charset.forName("UTF-8")));
        writer.write(test);
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        CsvReader reader = new CsvReader(new InputStreamReader(new ByteArrayInputStream(buffer), Charset.forName("UTF-8")));
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(test, reader.get(0));
        reader.close();
    }

    @Test
    public void test49() throws Exception {
        String data = "\"\\n\\r\\t\\b\\f\\e\\v\\a\\z\\d065\\o101\\101\\x41\\u0041\"";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(true);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("\n\r\t\b\fzAAAAA", reader.get(0));
        Assert.assertEquals("\"\\n\\r\\t\\b\\f\\e\\v\\a\\z\\d065\\o101\\101\\x41\\u0041\"", reader.getRawRecord());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test50() throws Exception {
        String data = "\\n\\r\\t\\b\\f\\e\\v\\a\\z\\d065\\o101\\101\\x41\\u0041";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("\n\r\t\b\fzAAAAA", reader.get(0));
        Assert.assertEquals("\\n\\r\\t\\b\\f\\e\\v\\a\\z\\d065\\o101\\101\\x41\\u0041", reader.getRawRecord());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test51() throws Exception {
        String data = "\"\\xfa\\u0afa\\xFA\\u0AFA\"";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(true);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("úૺúૺ", reader.get(0));
        Assert.assertEquals("\"\\xfa\\u0afa\\xFA\\u0AFA\"", reader.getRawRecord());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test52() throws Exception {
        String data = "\\xfa\\u0afa\\xFA\\u0AFA";
        CsvReader reader = CsvReader.parse(data);
        reader.setUseTextQualifier(false);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("úૺúૺ", reader.get(0));
        Assert.assertEquals("\\xfa\\u0afa\\xFA\\u0AFA", reader.getRawRecord());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test54() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.write("1,2");
        writer.write("3");
        writer.write("blah \"some stuff in quotes\"");
        writer.endRecord();
        Assert.assertFalse(writer.getForceQualifier());
        writer.setForceQualifier(true);
        Assert.assertTrue(writer.getForceQualifier());
        writer.write("1,2");
        writer.write("3");
        writer.write("blah \"some stuff in quotes\"");
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"1,2\",3,\"blah \"\"some stuff in quotes\"\"\"\r\n\"1,2\",\"3\",\"blah \"\"some stuff in quotes\"\"\"", data);
    }

    @Test
    public void test55() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.write("");
        writer.write("1");
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"\",1", data);
    }

    @Test
    public void test56() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, '\t', Charset.forName("ISO-8859-1"));
        writer.write("1,2");
        writer.write("3");
        writer.write("blah \"some stuff in quotes\"");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("1,2\t3\t\"blah \"\"some stuff in quotes\"\"\"\r\n", data);
    }

    @Test
    public void test57() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, '\t', Charset.forName("ISO-8859-1"));
        Assert.assertTrue(writer.getUseTextQualifier());
        writer.setUseTextQualifier(false);
        Assert.assertFalse(writer.getUseTextQualifier());
        writer.write("1,2");
        writer.write("3");
        writer.write("blah \"some stuff in quotes\"");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("1,2\t3\tblah \"some stuff in quotes\"\r\n", data);
    }

    @Test
    public void test58() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, '\t', Charset.forName("ISO-8859-1"));
        writer.write("data\r\nmore data");
        writer.write(" 3\t", false);
        writer.write(" 3\t");
        writer.write(" 3\t", true);
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"data\r\nmore data\"\t3\t3\t\" 3\t\"\r\n", data);
    }

    @Test
    public void test70() throws Exception {
        String data = "\"1\",Bruce\r\n\"2\",Toni\r\n\"3\",Brian\r\n";
        CsvReader reader = CsvReader.parse(data);
        reader.setHeaders(new String[] { "userid", "name" });
        Assert.assertEquals(2, reader.getHeaderCount());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get("userid"));
        Assert.assertEquals("Bruce", reader.get("name"));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("2", reader.get("userid"));
        Assert.assertEquals("Toni", reader.get("name"));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("3", reader.get("userid"));
        Assert.assertEquals("Brian", reader.get("name"));
        Assert.assertEquals(2L, reader.getCurrentRecord());
        Assert.assertEquals(2, reader.getColumnCount());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test71() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.setForceQualifier(true);
        writer.write(" data ");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"data\"\r\n", data);
    }

    @Test
    public void test72() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        Assert.assertEquals('\0', writer.getRecordDelimiter());
        writer.setRecordDelimiter(';');
        Assert.assertEquals(';', writer.getRecordDelimiter());
        writer.write("a;b");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"a;b\";", data);
    }

    @Test
    public void test73() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        Assert.assertEquals(CsvWriter.ESCAPE_MODE_DOUBLED, writer.getEscapeMode());
        writer.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
        Assert.assertEquals(CsvWriter.ESCAPE_MODE_BACKSLASH, writer.getEscapeMode());
        writer.write("1,2");
        writer.write("3");
        writer.write("blah \"some stuff in quotes\"");
        writer.endRecord();
        writer.setForceQualifier(true);
        writer.write("1,2");
        writer.write("3");
        writer.write("blah \"some stuff in quotes\"");
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"1,2\",3,\"blah \\\"some stuff in quotes\\\"\"\r\n\"1,2\",\"3\",\"blah \\\"some stuff in quotes\\\"\"", data);
    }

    @Test
    public void test74() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
        writer.setUseTextQualifier(false);
        writer.write("1,2");
        writer.write("3");
        writer.write("blah \"some stuff in quotes\"");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("1\\,2,3,blah \"some stuff in quotes\"\r\n", data);
    }

    @Test
    public void test75() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.write("1");
        writer.endRecord();
        writer.writeComment("blah");
        writer.write("2");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("1\r\n#blah\r\n2\r\n", data);
    }

    @Test
    public void test76() throws Exception {
        CsvReader reader = CsvReader.parse("user_id,name\r\n1,Bruce");
        Assert.assertEquals(null, reader.getHeaders());
        Assert.assertEquals(-1, reader.getIndex("user_id"));
        Assert.assertEquals("", reader.getHeader(0));
        Assert.assertTrue(reader.readHeaders());
        Assert.assertEquals(0, reader.getIndex("user_id"));
        Assert.assertEquals("user_id", reader.getHeader(0));
        String[] headers = reader.getHeaders();
        Assert.assertEquals(2, headers.length);
        Assert.assertEquals("user_id", headers[0]);
        Assert.assertEquals("name", headers[1]);
        reader.setHeaders(null);
        Assert.assertEquals(null, reader.getHeaders());
        Assert.assertEquals(-1, reader.getIndex("user_id"));
        Assert.assertEquals("", reader.getHeader(0));
        reader.close();
    }

    @Test
    public void test77() {
        try {
            CsvReader.parse(null);
        } catch (Exception ex) {
            assertException(new IllegalArgumentException("Parameter data can not be null."), ex);
        }
    }

    @Test
    public void test78() throws Exception {
        CsvReader reader = CsvReader.parse("1,Bruce");
        Assert.assertTrue(reader.readRecord());
        Assert.assertFalse(reader.isQualified(999));
        reader.close();
    }

    @Test
    public void test79() {
        CsvReader reader;
        reader = CsvReader.parse("");
        reader.close();
        try {
            reader.readRecord();
        } catch (Exception ex) {
            assertException(new IOException("This instance of the CsvReader class has already been closed."), ex);
        }
    }

    @Test
    public void test81() throws Exception {
        CsvReader reader = CsvReader.parse(generateString('a', 100001));
        try {
            reader.readRecord();
        } catch (Exception ex) {
            assertException(new IOException("Maximum column length of 100,000 exceeded in column 0 in record 0. Set the SafetySwitch property to false if you're expecting column lengths greater than 100,000 characters to avoid this error."), ex);
        }
        reader.close();
    }

    @Test
    public void test82() throws Exception {
        StringBuilder holder = new StringBuilder(200010);
        for (int i = 0; i < 100000; i++) {
            holder.append("a,");
        }
        holder.append("a");
        CsvReader reader = CsvReader.parse(holder.toString());
        try {
            reader.readRecord();
        } catch (Exception ex) {
            assertException(new IOException("Maximum column count of 100,000 exceeded in record 0. Set the SafetySwitch property to false if you're expecting more than 100,000 columns per record to avoid this error."), ex);
        }
        reader.close();
    }

    @Test
    public void test83() throws Exception {
        CsvReader reader = CsvReader.parse(generateString('a', 100001));
        reader.setSafetySwitch(false);
        reader.readRecord();
        reader.close();
    }

    @Test
    public void test84() throws Exception {
        StringBuilder holder = new StringBuilder(200010);
        for (int i = 0; i < 100000; i++) {
            holder.append("a,");
        }
        holder.append("a");
        CsvReader reader = CsvReader.parse(holder.toString());
        reader.setSafetySwitch(false);
        reader.readRecord();
        reader.close();
    }

    @Test
    public void test85() throws Exception {
        CsvReader reader = CsvReader.parse(generateString('a', 100000));
        reader.readRecord();
        reader.close();
    }

    @Test
    public void test86() throws Exception {
        StringBuilder holder = new StringBuilder(200010);
        for (int i = 0; i < 99999; i++) {
            holder.append("a,");
        }
        holder.append("a");
        CsvReader reader = CsvReader.parse(holder.toString());
        reader.readRecord();
        reader.close();
    }

    @Test
    public void test87() throws Exception {
        CsvWriter writer = new CsvWriter("temp.csv");
        writer.write("1");
        writer.close();
        CsvReader reader = new CsvReader("temp.csv");
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
        new File("temp.csv").delete();
    }

    @Test
    public void test88() throws Exception {
        try {
            CsvReader reader = new CsvReader((String) null, ',', Charset.forName("ISO-8859-1"));
        } catch (Exception ex) {
            assertException(new IllegalArgumentException("Parameter fileName can not be null."), ex);
        }
    }

    @Test
    public void test89() throws Exception {
        try {
            CsvReader reader = new CsvReader("temp.csv", ',', null);
        } catch (Exception ex) {
            assertException(new IllegalArgumentException("Parameter charset can not be null."), ex);
        }
    }

    @Test
    public void test90() throws Exception {
        try {
            CsvReader reader = new CsvReader((Reader) null, ',');
        } catch (Exception ex) {
            assertException(new IllegalArgumentException("Parameter inputStream can not be null."), ex);
        }
    }

    @Test
    public void test91() throws Exception {
        byte[] buffer;
        String test = "test";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        writer.println(test);
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        CsvReader reader = new CsvReader(new ByteArrayInputStream(buffer), Charset.forName("ISO-8859-1"));
        reader.readRecord();
        Assert.assertEquals(test, reader.get(0));
        reader.close();
    }

    @Test
    public void test92() throws Exception {
        byte[] buffer;
        String test = "test";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        writer.println(test);
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        CsvReader reader = new CsvReader(new ByteArrayInputStream(buffer), ',', Charset.forName("ISO-8859-1"));
        reader.readRecord();
        Assert.assertEquals(test, reader.get(0));
        reader.close();
    }

    @Test
    public void test112() throws Exception {
        try {
            CsvWriter writer = new CsvWriter((String) null, ',', Charset.forName("ISO-8859-1"));
        } catch (Exception ex) {
            assertException(new IllegalArgumentException("Parameter fileName can not be null."), ex);
        }
    }

    @Test
    public void test113() throws Exception {
        try {
            CsvWriter writer = new CsvWriter("test.csv", ',', (Charset) null);
        } catch (Exception ex) {
            assertException(new IllegalArgumentException("Parameter charset can not be null."), ex);
        }
    }

    @Test
    public void test114() throws Exception {
        try {
            CsvWriter writer = new CsvWriter((Writer) null, ',');
        } catch (Exception ex) {
            assertException(new IllegalArgumentException("Parameter outputStream can not be null."), ex);
        }
    }

    @Test
    public void test115() throws Exception {
        try {
            CsvWriter writer = new CsvWriter("test.csv");
            writer.close();
            writer.write("");
        } catch (Exception ex) {
            assertException(new IOException("This instance of the CsvWriter class has already been closed."), ex);
        }
    }

    @Test
    public void test117() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        Assert.assertEquals('#', writer.getComment());
        writer.setComment('~');
        Assert.assertEquals('~', writer.getComment());
        writer.setRecordDelimiter(';');
        writer.write("1");
        writer.endRecord();
        writer.writeComment("blah");
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("1;~blah;", data);
    }

    @Test
    public void test118() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, '\t', Charset.forName("ISO-8859-1"));
        Assert.assertEquals('\"', writer.getTextQualifier());
        writer.setTextQualifier('\'');
        Assert.assertEquals('\'', writer.getTextQualifier());
        writer.write("1,2");
        writer.write("3");
        writer.write("blah \"some stuff in quotes\"");
        writer.write("blah \'some stuff in quotes\'");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("1,2\t3\tblah \"some stuff in quotes\"\t\'blah \'\'some stuff in quotes\'\'\'\r\n", data);
    }

    @Test
    public void test119() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.write("1,2");
        writer.write("3");
        writer.endRecord();
        Assert.assertEquals(',', writer.getDelimiter());
        writer.setDelimiter('\t');
        Assert.assertEquals('\t', writer.getDelimiter());
        writer.write("1,2");
        writer.write("3");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"1,2\",3\r\n1,2\t3\r\n", data);
    }

    @Test
    public void test120() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.write("1,2");
        writer.endRecord();
        buffer = stream.toByteArray();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("", data);
        writer.flush();
        buffer = stream.toByteArray();
        stream.close();
        data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"1,2\"\r\n", data);
        writer.close();
    }

    @Test
    public void test121() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.writeRecord(new String[] { " 1 ", "2" }, false);
        writer.writeRecord(new String[] { " 1 ", "2" });
        writer.writeRecord(new String[] { " 1 ", "2" }, true);
        writer.writeRecord(new String[0], true);
        writer.writeRecord(null, true);
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("1,2\r\n1,2\r\n\" 1 \",2\r\n", data);
    }

    @Test
    public void test122() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.write("1,2");
        writer.write(null);
        writer.write("3 ", true);
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"1,2\",,\"3 \"\r\n", data);
    }

    @Test
    public void test123() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.write("#123");
        writer.endRecord();
        writer.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
        writer.setUseTextQualifier(false);
        writer.write("#123");
        writer.endRecord();
        writer.write("#");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"#123\"\r\n\\#123\r\n\\#\r\n", data);
    }

    @Test
    public void test124() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.setRecordDelimiter(';');
        writer.setUseTextQualifier(false);
        writer.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
        writer.write("1;2");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("1\\;2;", data);
    }

    @Test
    public void test131() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.setUseTextQualifier(false);
        writer.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
        writer.write("1,\\\r\n2");
        writer.endRecord();
        writer.setRecordDelimiter(';');
        writer.write("1,\\;2");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("1\\,\\\\\\\r\\\n2\r\n1\\,\\\\\\;2;", data);
    }

    @Test
    public void test132() throws Exception {
        byte[] buffer;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(stream, ',', Charset.forName("ISO-8859-1"));
        writer.setEscapeMode(CsvWriter.ESCAPE_MODE_BACKSLASH);
        writer.write("1,\\2");
        writer.endRecord();
        writer.close();
        buffer = stream.toByteArray();
        stream.close();
        String data = Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(buffer)).toString();
        Assert.assertEquals("\"1,\\\\2\"\r\n", data);
    }

    @Test
    public void test135() throws Exception {
        CsvReader reader = CsvReader.parse("1\n\n1\r\r1\r\n\r\n1\n\r1");
        Assert.assertTrue(reader.getSkipEmptyRecords());
        reader.setSkipEmptyRecords(false);
        Assert.assertFalse(reader.getSkipEmptyRecords());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(2L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(3L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(4L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(5L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(6L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(7L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(8L, reader.getCurrentRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test136() throws Exception {
        CsvReader reader = CsvReader.parse("1\n\n1\r\r1\r\n\r\n1\n\r1");
        Assert.assertTrue(reader.getSkipEmptyRecords());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(2L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(3L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(4L, reader.getCurrentRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test137() throws Exception {
        CsvReader reader = CsvReader.parse("1;; ;1");
        reader.setRecordDelimiter(';');
        Assert.assertTrue(reader.getSkipEmptyRecords());
        reader.setSkipEmptyRecords(false);
        Assert.assertFalse(reader.getSkipEmptyRecords());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(2L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(3L, reader.getCurrentRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test138() throws Exception {
        CsvReader reader = CsvReader.parse("1;; ;1");
        reader.setRecordDelimiter(';');
        Assert.assertTrue(reader.getSkipEmptyRecords());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(0L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("", reader.get(0));
        Assert.assertEquals(1L, reader.getCurrentRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(1, reader.getColumnCount());
        Assert.assertEquals("1", reader.get(0));
        Assert.assertEquals(2L, reader.getCurrentRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test143() throws Exception {
        CsvReader reader = CsvReader.parse("\"" + generateString('a', 100001) + "\"");
        try {
            reader.readRecord();
        } catch (Exception ex) {
            assertException(new IOException("Maximum column length of 100,000 exceeded in column 0 in record 0. Set the SafetySwitch property to false if you're expecting column lengths greater than 100,000 characters to avoid this error."), ex);
        }
        reader.close();
    }

    @Test
    public void test144() throws Exception {
        CsvReader reader = CsvReader.parse("\"" + generateString('a', 100000) + "\"");
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(generateString('a', 100000), reader.get(0));
        Assert.assertEquals("\"" + generateString('a', 100000) + "\"", reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test145() throws Exception {
        CsvReader reader = CsvReader.parse("\"" + generateString('a', 100001) + "\"");
        reader.setSafetySwitch(false);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(generateString('a', 100001), reader.get(0));
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test146() throws Exception {
        CsvReader reader = CsvReader.parse("\"" + generateString('a', 10000) + "\r\nb");
        Assert.assertEquals("", reader.getRawRecord());
        Assert.assertTrue(reader.skipLine());
        Assert.assertEquals("", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals("b", reader.get(0));
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test147() throws Exception {
        StringBuilder data = new StringBuilder(20000);
        for (int i = 0; i < 10000; i++) {
            data.append("\\b");
        }
        CsvReader reader = CsvReader.parse(data.toString());
        reader.setUseTextQualifier(false);
        reader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(generateString('\b', 10000), reader.get(0));
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test148() throws Exception {
        CsvReader reader = CsvReader.parse("\"" + generateString('a', 100000) + "\"\r\n" + generateString('a', 100000));
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(generateString('a', 100000), reader.get(0));
        Assert.assertEquals("\"" + generateString('a', 100000) + "\"", reader.getRawRecord());
        Assert.assertTrue(reader.readRecord());
        Assert.assertEquals(generateString('a', 100000), reader.get(0));
        Assert.assertEquals(generateString('a', 100000), reader.getRawRecord());
        Assert.assertFalse(reader.readRecord());
        reader.close();
    }

    @Test
    public void test149() throws Exception {
        try {
            CsvReader reader = new CsvReader("C:\\somefilethatdoesntexist.csv");
        } catch (Exception ex) {
            assertException(new FileNotFoundException("File C:\\somefilethatdoesntexist.csv does not exist."), ex);
        }
    }

    @Test
    public void test173() throws Exception {
        FailingReader fail = new FailingReader();
        CsvReader reader = new CsvReader(fail);
        boolean exceptionThrown = false;
        Assert.assertFalse(fail.DisposeCalled);
        try {
            reader.readRecord();
        } catch (IOException ex) {
            Assert.assertTrue(fail.DisposeCalled);
            exceptionThrown = true;
            Assert.assertEquals("Read failed.", ex.getMessage());
        } finally {
            reader.close();
        }
        Assert.assertTrue(exceptionThrown);
        try {
            reader.getHeaders();
        } catch (Exception ex) {
            assertException(new IOException("This instance of the CsvReader class has already been closed."), ex);
        }
    }

    private class FailingReader extends Reader {

        public boolean DisposeCalled = false;

        public FailingReader() {
            super("");
        }

        public int read(char[] buffer, int index, int count) throws IOException {
            throw new IOException("Read failed.");
        }

        public void close() {
            DisposeCalled = true;
        }
    }
}
