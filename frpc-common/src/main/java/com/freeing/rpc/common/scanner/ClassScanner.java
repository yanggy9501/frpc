package com.freeing.rpc.common.scanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类扫描器
 *
 * @author yanggy
 */
public class ClassScanner {
    /**
     * 文件
     */
    private static final String PROTOC_FILE = "file";

    /**
     * jar包
     */
    private static final String PROTOC_JAR = "jar";

    /**
     * class文件的后缀
     */
    private static final String CLASS_FILE_SUFFIX = "class";

    public static List<String> getClassNameList(String packageName) throws IOException {
        List<String> classNameList = new ArrayList<>();
        // 是否递归迭代
        boolean recursive = true;
        // 包名转换为路径
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);

        while (dirs.hasMoreElements()) {
            // 获取下一个元素
            URL url = dirs.nextElement();
            // 获取文件的协议名称
            String protocol = url.getProtocol();
            if (PROTOC_FILE.equals(protocol)) {
                // 获取包的物理路径
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                // 以文件的方式扫描整个包下的文件，病添加到集合中
                findAndAddClassesInPackageByFile(packageName, filePath, recursive, classNameList);
            }
            else if (PROTOC_JAR.equals(protocol)) {
                findAndAddClassesInPackageByJar(packageName, classNameList, recursive, packageDirName, url);
            }
        }

        return classNameList;
    }

    /**
     * 扫描当前工程中指定包下的所有类信息
     *
     * @param packageName 扫描的包名
     * @param packagePath 包在磁盘上的完整路径
     * @param recursive 是否递归调用
     * @param classNameList 类名称的集合
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, boolean recursive,
            List<String> classNameList) {
        File dir = new File(packagePath);
        // 如果不存在或者不是目录则直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // TODO 日志
            return;
        }
        // 过滤：筛选目录和.class 文件
        File[] dirOrFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirOrFiles) {
            // 如果是目录则递归继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                    file.getAbsolutePath(),
                    recursive,
                    classNameList);
            } else {
                // 如果是Java类文件，去掉后缀.class 只保留类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                // 添加到集合中
                classNameList.add(packageName + "." + className);
            }
        }
    }

    /**
     * 扫描Jar文件中指定包下的所有类信息
     *
     * @param packageName 扫描的包名
     * @param classNameList 完成类名存放的List集合
     * @param recursive 是否递归调用
     * @param packageDirName 当前包名的前面部分的名称
     * @param url 包的url地址
     * @throws IOException
     */
    private static void findAndAddClassesInPackageByJar(String packageName, List<String> classNameList,
            boolean recursive, String packageDirName, URL url) throws IOException {
        // 如果是jar文件，定义一个 JarFile
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            // 获取 jar 里的一个实体，可以是目录或者一些jar里的其他文件，如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }
            // 如果前半部分与定义的包名相同
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                // 如果以"/"结尾 是一个包
                if (idx != -1) {
                    //获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                //如果可以迭代下去 并且是一个包
                if ((idx != -1) || recursive){
                    // 如果是一个.class文件而且不是目录
                    if (name.endsWith(CLASS_FILE_SUFFIX) && !entry.isDirectory()) {
                        // 去掉后面的".class" 获取真正的类名
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        classNameList.add(packageName + '.' + className);
                    }
                }
            }
        }
    }
}
