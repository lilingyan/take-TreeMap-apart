package com.lilingyan.avl;

import org.junit.Assert;
import org.junit.Test;
import java.util.Random;
import java.util.TreeMap;

/**
 * @Author: lilingyan
 * @Date 2019/2/17 21:30
 */
public class AvlTest {

    private static Random random = new Random();

    /**
     * 测试有序key插入及查询效率
     * 比普通bst树快几百倍
     */
    @SuppressWarnings("Duplicates")
    @Test
    public void putAndGetIncrementTest(){
        AvlMap<Integer,String> avlMap = new AvlMap<>();
        for (int i = 0; i < 65535; i++) {
            avlMap.put(i,String.valueOf(random.nextInt(65535)));
        }
        for (int i = 0; i < 65535; i++) {
            avlMap.get(i);
        }
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void putAndGetIncrementWithTreeMapTest() throws Exception {
        AvlMap<Integer,String> avlMap = new AvlMap<>();
        TreeMap<Integer,String> treeMap = new TreeMap<>();
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            String value = String.valueOf(random.nextInt(65535));
            avlMap.put(key,String.valueOf(key));
            treeMap.put(key,String.valueOf(key));
        }
        Assert.assertTrue(avlMap.size() == treeMap.size());
        System.out.println(avlMap.size());
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            Assert.assertTrue(avlMap.containsKey(key) == treeMap.containsKey(key));
            if(avlMap.get(key) == null){
                Assert.assertTrue(treeMap.get(key) == null);
            }else{
                Assert.assertTrue(avlMap.get(key).equals(treeMap.get(key)));
            }
        }
        avlMap.checkBalance();
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void putAndRemoveAndGetIncrementWithTreeMapTest() throws Exception {
        int max = 65535;
        AvlMap<Integer,String> avlMap = new AvlMap<>();
        TreeMap<Integer,String> treeMap = new TreeMap<>();
        for (int i = 0; i < max; i++) {
            int key = random.nextInt(max);
            avlMap.put(key,String.valueOf(key));
            treeMap.put(key,String.valueOf(key));
        }
        Assert.assertTrue(avlMap.size() == treeMap.size());
        System.out.println(avlMap.size());
        avlMap.checkBalance();
        for (int i = 0; i < max/2; i++) {
            int key = random.nextInt(max);
            if(avlMap.containsKey(key)){
                Assert.assertTrue(avlMap.remove(key).equals(treeMap.remove(key)));
            }else{
                Assert.assertTrue(avlMap.remove(key)==null&&treeMap.remove(key)==null);
            }
        }
        System.out.println(avlMap.size());
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            Assert.assertTrue(avlMap.containsKey(key) == treeMap.containsKey(key));
            if(avlMap.get(key) == null){
                Assert.assertTrue(treeMap.get(key) == null);
            }else{
                Assert.assertTrue(avlMap.get(key).equals(treeMap.get(key)));
            }
        }
        avlMap.checkBalance();
    }


}
