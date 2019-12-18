package com.nice.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClassScanner.class);

    /**
     * Ĭ�Ϲ���������ʵ�֣�
     */
    private final static Predicate<Class<?>> EMPTY_FILTER = clazz -> true;

    /**
     * ɨ��Ŀ¼�µ�����class�ļ�
     *
     * @param scanPackage �����İ���·��
     * @return
     */
    public static Set<Class<?>> getClasses(String scanPackage) {
        return getClasses(scanPackage, EMPTY_FILTER);
    }

    /**
     * �������е����ࣨ�����������ࣩ
     *
     * @param scanPackage �����İ���·��
     * @param parent
     * @return
     */
    public static Set<Class<?>> listAllSubclasses(String scanPackage, Class<?> parent) {
        return getClasses(scanPackage, (clazz) -> {
            return parent.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers());
        });
    }

    /**
     * �������д��ƶ�ע���class�б�
     *
     * @param scanPackage �����İ���·��
     * @param annotation
     * @return
     */
    public static <A extends Annotation> Set<Class<?>> listClassesWithAnnotation(String scanPackage,
                                                                                 Class<A> annotation) {
        return getClasses(scanPackage, (clazz) -> clazz.getAnnotation(annotation) != null);
    }

    /**
     * ɨ��Ŀ¼�µ�����class�ļ�
     *
     * @param pack   ��·��
     * @param filter �Զ����������
     * @return
     */
    public static Set<Class<?>> getClasses(String pack, Predicate<Class<?>> filter) {
        Set<Class<?>> result = new LinkedHashSet<Class<?>>();
        // �Ƿ�ѭ������
        boolean recursive = true;
        // ��ȡ�������� �������滻
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // ����һ��ö�ٵļ��� ������ѭ�����������Ŀ¼�µ�things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // ѭ��������ȥ
            while (dirs.hasMoreElements()) {
                // ��ȡ��һ��Ԫ��
                URL url = dirs.nextElement();
                // �õ�Э�������
                String protocol = url.getProtocol();
                // ��������ļ�����ʽ�����ڷ�������
                if ("file".equals(protocol)) {
                    // ��ȡ��������·��
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // ���ļ��ķ�ʽɨ���������µ��ļ� ����ӵ�������
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, result, filter);
                } else if ("jar".equals(protocol)) {
                    // �����jar���ļ�
                    Set<Class<?>> jarClasses = findClassFromJar(url, packageName, packageDirName, recursive, filter);
                    result.addAll(jarClasses);
                }
            }
        } catch (IOException e) {
            logger.error("", e);
        }

        return result;
    }

    private static Set<Class<?>> findClassFromJar(URL url, String packageName, String packageDirName, boolean recursive,
                                                  Predicate<Class<?>> filter) {
        Set<Class<?>> result = new LinkedHashSet<Class<?>>();
        try {
            // ��ȡjar
            JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
            // �Ӵ�jar�� �õ�һ��ö����
            Enumeration<JarEntry> entries = jar.entries();
            // ͬ���Ľ���ѭ������
            while (entries.hasMoreElements()) {
                // ��ȡjar���һ��ʵ�� ������Ŀ¼ ��һЩjar����������ļ� ��META-INF���ļ�
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                // �������/��ͷ��
                if (name.charAt(0) == '/') {
                    // ��ȡ������ַ���
                    name = name.substring(1);
                }
                // ���ǰ�벿�ֺͶ���İ�����ͬ
                if (name.startsWith(packageDirName)) {
                    int idx = name.lastIndexOf('/');
                    // �����"/"��β ��һ����
                    if (idx != -1) {
                        // ��ȡ���� ��"/"�滻��"."
                        packageName = name.substring(0, idx).replace('/', '.');
                    }
                    // ������Ե�����ȥ ������һ����
                    if ((idx != -1) || recursive) {
                        // �����һ��.class�ļ� ���Ҳ���Ŀ¼
                        if (name.endsWith(".class") && !entry.isDirectory()) {
                            // ȥ�������".class" ��ȡ����������
                            String className = name.substring(packageName.length() + 1, name.length() - 6);
                            try {
                                // ��ӵ�classes
                                Class<?> c = Class.forName(packageName + '.' + className);
                                if (filter.test(c)) {
                                    result.add(c);
                                }
                            } catch (ClassNotFoundException e) {
                                logger.error("", e);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }

    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath,
                                                         final boolean recursive, Set<Class<?>> classes, Predicate<Class<?>> filter) {
        // ��ȡ�˰���Ŀ¼ ����һ��File
        File dir = new File(packagePath);
        // ��������ڻ��� Ҳ����Ŀ¼��ֱ�ӷ���
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("�û�������� " + packageName + " ��û���κ��ļ�");
            return;
        }
        // ������� �ͻ�ȡ���µ������ļ� ����Ŀ¼
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // �Զ�����˹��� �������ѭ��(������Ŀ¼) ��������.class��β���ļ�(����õ�java���ļ�)
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // ѭ�������ļ�
        for (File file : dirfiles) {
            // �����Ŀ¼ �����ɨ��
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive,
                        classes, filter);
            } else {
                // �����java���ļ� ȥ�������.class ֻ��������
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // ��ӵ�������ȥ
                    Class<?> clazz = Thread.currentThread().getContextClassLoader()
                            .loadClass(packageName + '.' + className);
                    if (filter.test(clazz)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("", e);
                }
            }
        }
    }

}