package com.freeing.rpc.common.scanner.test;

import com.freeing.rpc.common.scanner.ClassScanner;
import org.junit.Test;

import java.util.List;

/**
 * @author yanggy
 */
public class ScannerTest {

    @Test
    public void testScannerClassNameList() throws Exception {
        List<String> classNameList = ClassScanner.getClassNameList("com.freeing.rpc.common.scanner.test");
        classNameList.forEach(System.out::println);
    }
}
