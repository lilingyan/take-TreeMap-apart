package com.lilingyan.rbt;

import org.junit.Assert;
import org.junit.Test;
import java.util.Random;
import java.util.TreeMap;

/**
 * @Author: lilingyan
 * @Date 2019/2/22 14:45
 */
public class RbtTest {

    private Random random = new Random();

    @SuppressWarnings("Duplicates")
    @Test
    public void putAndGetIncrementWithTreeMapTest(){
        RbtMap<Integer,String> rbtMap = new RbtMap<>();
        TreeMap<Integer,String> treeMap = new TreeMap<>();
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            String value = String.valueOf(random.nextInt(65535));
            rbtMap.put(key,String.valueOf(key));
            treeMap.put(key,String.valueOf(key));
        }
        Assert.assertTrue(rbtMap.size() == treeMap.size());
        System.out.println(rbtMap.size());
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            Assert.assertTrue(rbtMap.containsKey(key) == treeMap.containsKey(key));
            if(rbtMap.get(key) == null){
                Assert.assertTrue(treeMap.get(key) == null);
            }else{
                Assert.assertTrue(rbtMap.get(key).equals(treeMap.get(key)));
            }
        }
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void putAndRemoveAndGetIncrementWithTreeMapTest() throws Exception {
        int max = 65535;
        RbtMap<Integer,String> rbtMap = new RbtMap<>();
        TreeMap<Integer,String> treeMap = new TreeMap<>();
        for (int i = 0; i < max; i++) {
            int key = random.nextInt(max);
            rbtMap.put(key,String.valueOf(key));
            treeMap.put(key,String.valueOf(key));
        }
        Assert.assertTrue(rbtMap.size() == treeMap.size());
        System.out.println(rbtMap.size());
        for (int i = 0; i < max/2; i++) {
            int key = random.nextInt(max);
            if(rbtMap.containsKey(key)){
                Assert.assertTrue(rbtMap.remove(key).equals(treeMap.remove(key)));
            }else{
                Assert.assertTrue(rbtMap.remove(key)==null&&treeMap.remove(key)==null);
            }
        }
        System.out.println(rbtMap.size());
        for (int i = 0; i < 65535; i++) {
            int key = random.nextInt(65535);
            Assert.assertTrue(rbtMap.containsKey(key) == treeMap.containsKey(key));
            if(rbtMap.get(key) == null){
                Assert.assertTrue(treeMap.get(key) == null);
            }else{
                Assert.assertTrue(rbtMap.get(key).equals(treeMap.get(key)));
            }
        }
    }

}
