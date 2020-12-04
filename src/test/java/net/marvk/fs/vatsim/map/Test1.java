package net.marvk.fs.vatsim.map;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Test1 {
    @Test
    public void test() throws IOException {
        final InputStream resourceAsStream = IAmAClassInMainJava.class.getResourceAsStream("test.txt");

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));

        System.out.println(bufferedReader.readLine());
    }
}
