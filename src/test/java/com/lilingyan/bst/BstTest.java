package com.lilingyan.bst;

import org.junit.Assert;
import org.junit.Test;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * @Author: lilingyan
 * @Date 2019/2/17 10:59
 */
public class BstTest {

    private Random random = new Random();

    /**
     * 测试插入输出是否有序
     */
    @Test
    public void putAndIterateTest(){
        BstMap<Integer,String> map = new BstMap<>();
        for (int i = 0; i < 16; i++) {
            map.put(random.nextInt(16),"你好");
        }
        Iterator<BstMap.BstEntry<Integer, String>> iterator = map.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }

    /**
     * 与TreeMap进行插入校验
     */
    @SuppressWarnings("Duplicates")
    @Test
    public void putAndIterateWithTreeMapTest(){
        BstMap<Integer,String> bstMap = new BstMap<>();
        TreeMap<Integer,String> treeMap = new TreeMap<>();
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            String value = String.valueOf(random.nextInt(65535));
            bstMap.put(key,value);
            treeMap.put(key,value);
        }
        Assert.assertTrue(bstMap.size() == treeMap.size());
        System.out.println(bstMap.size());
        Iterator<BstMap.BstEntry<Integer, String>> bstMapIterator = bstMap.iterator();
        Iterator<Map.Entry<Integer, String>> treeMapIterator = treeMap.entrySet().iterator();
        while (bstMapIterator.hasNext()&&treeMapIterator.hasNext()){
            Assert.assertTrue(bstMapIterator.next().equals(treeMapIterator.next()));
        }
    }

    /**
     * 与TreeMap进行查询校验
     */
    @SuppressWarnings("Duplicates")
    @Test
    public void queryWithTreeMapTest(){
        BstMap<Integer,String> bstMap = new BstMap<>();
        TreeMap<Integer,String> treeMap = new TreeMap<>();
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            String value = String.valueOf(random.nextInt(65535));
            bstMap.put(key,value);
            treeMap.put(key,value);
        }
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            Assert.assertTrue(bstMap.containsKey(key) == treeMap.containsKey(key));
            if(bstMap.get(key) == null){
                Assert.assertTrue(treeMap.get(key) == null);
            }else{
                Assert.assertTrue(bstMap.get(key).equals(treeMap.get(key)));
            }
        }
        for (int i = 0; i < 10; i++) {
            String value = String.valueOf(random.nextInt(65535));
            Assert.assertTrue(bstMap.containsValue(value)==treeMap.containsValue(value));
        }
    }

    /**
     * 与TreeMap进行删除校验
     */
    @SuppressWarnings("Duplicates")
    @Test
    public void removeWithTreeMapTest(){
        BstMap<Integer,String> bstMap = new BstMap<>();
        TreeMap<Integer,String> treeMap = new TreeMap<>();
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            String value = String.valueOf(random.nextInt(65535));
            bstMap.put(key,value);
            treeMap.put(key,value);
        }
        System.out.println(bstMap.size());
        for (int i = 0; i < 65535/2; i++) {
            int key = random.nextInt(65535);
            if(bstMap.containsKey(key)){
                Assert.assertTrue(bstMap.remove(key).equals(treeMap.remove(key)));
            }else{
                Assert.assertTrue(bstMap.remove(key)==null&&treeMap.remove(key)==null);
            }
        }
        System.out.println(bstMap.size());
        Assert.assertTrue(bstMap.size() == treeMap.size());
        Iterator<BstMap.BstEntry<Integer, String>> bstMapIterator = bstMap.iterator();
        Iterator<Map.Entry<Integer, String>> treeMapIterator = treeMap.entrySet().iterator();
        while (bstMapIterator.hasNext()&&treeMapIterator.hasNext()){
            Assert.assertTrue(bstMapIterator.next().equals(treeMapIterator.next()));
        }
    }

}
