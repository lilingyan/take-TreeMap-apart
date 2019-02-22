package com.lilingyan.bst;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 * 二叉搜索树
 * @Author: lilingyan
 * @Date 2019/2/17 9:40
 */
public class BstMap<K,V> implements Iterable<BstMap.BstEntry<K,V>>{

    /**
     * 在这个树中存在的节点数量
     */
    private transient int size = 0;

    /**
     * 这颗树的根节点
     */
    private transient BstEntry<K,V> root;

    /**
     * 自定义的Comparator 作用于树节点的排序(可以没有)
     * 如果没有 则用树节点的key compare直接比较(如果不存在自定义的Comparator  则key必须实现Comparable接口)
     */
    private final Comparator<? super K> comparator;

    //=========================构造器==========================
    public BstMap() {
        comparator = null;
    }
    public BstMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }
    //=========================构造器==========================

    //=========================添加==========================
    /**
     * 加入一个节点
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("Duplicates")
    public V put(K key, V value) {
        BstEntry<K,V> t = root;
        //如果根节点是空 则插入的就是根节点
        if (t == null) {

            //校验key类型(可能是null)
            compare(key, key);

            root = new BstEntry<>(key, value, null);
            //树的节点数量置为1
            size = 1;
            return null;
        }
        //记录key与父节点比较的大小
        int cmp;
        //记录循环比较中的父节点指针
        BstEntry<K,V> parent;
        // split comparator and comparable paths
        Comparator<? super K> cpr = comparator;
        //如果是有自定义比较器的
        if (cpr != null) {
            /**
             * 父节点指针从头开始(根节点开始)
             * 拿插入的key与父节点指针相比
             * 如果小于 则把要比较的父节点指针向左子节点移动
             * 如果大于 则把要比较的父节点指针向右子节点移动
             * 如果等于 则覆盖值
             */
            do {
                parent = t;
                cmp = cpr.compare(key, t.key);
                if (cmp < 0)
                    t = t.left;
                else if (cmp > 0)
                    t = t.right;
                else
                    return t.setValue(value);
            } while (t != null);
        }
        //如果没有自定义比较器
        else {
            if (key == null)
                throw new NullPointerException();
            //则先要把key强转Comparable类型
            @SuppressWarnings("unchecked")
            Comparable<? super K> k = (Comparable<? super K>) key;
            //比较过程与上同理
            do {
                parent = t;
                cmp = k.compareTo(t.key);
                if (cmp < 0)
                    t = t.left;
                else if (cmp > 0)
                    t = t.right;
                else
                    return t.setValue(value);
            } while (t != null);
        }

        /**
         * 如果走到这里，说明原树中没有相同的key，并且父节点指针也指向了叶子节点
         * 直接判断cmp(当前父节点指针)大小 小就新建一个左叶子节点插入 大就新建一个右叶子节点插入
         */
        BstEntry<K,V> e = new BstEntry<>(key, value, parent);
        //只可能大或者小  不可能存在等于
        if (cmp < 0)
            parent.left = e;
        else
            parent.right = e;
        //树节点数量增加1
        size++;
        return null;
    }

    //=========================删除==========================
    /**
     * 删除节点
     * @param key   需要删除的节点的key
     * @return
     */
    public V remove(Object key) {
        BstEntry<K,V> p = getEntry(key);
        if (p == null)
            return null;

        V oldValue = p.value;
        deleteEntry(p);
        return oldValue;
    }
    /**
     * 删除节点
     * @param p 需要删除的节点对象
     */
    @SuppressWarnings("Duplicates")
    private void deleteEntry(BstEntry<K,V> p) {
        size--;

        /**
         * 如果p节点有两个子孩子
         * 则查询出后后继节点
         * 用后继节点的值覆盖p，然后删除后继节点(因为有右子树，所以后继节点必定是右子树中最小的节点，必定是叶子节点)
         *
         * 这里也可以用p节点的前驱节点做同样的操作(bst也是正确的)
         */
        if (p.left != null && p.right != null) {
            BstEntry<K,V> s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        BstEntry<K,V> replacement = (p.left != null ? p.left : p.right);

        /**
         * 如果p有左子节点或者右子节点
         * 则直接把左子节点或者右子节点覆盖到p节点的位置
         */
        if (replacement != null) {
            // Link replacement to parent
            replacement.parent = p.parent;
            /**
             * 如果p是根节点
             * 则把替换节点置为根节点
             */
            if (p.parent == null)
                root = replacement;
            else if (p == p.parent.left)
                p.parent.left  = replacement;
            else
                p.parent.right = replacement;

            /**
             * 删除p节点
             */
            p.left = p.right = p.parent = null;

        } else if (p.parent == null) { // return if we are the only node.、
            /**
             * 如果p没有子节点也没有父节点
             * 则说明p是这颗树的最后节点
             * 直接把树置空
             */
            root = null;
        } else { //  No children. Use self as phantom replacement and unlink.

            if (p.parent != null) {
                /**
                 * 如果p没有子节点，但有父节点 说明p是叶子节点
                 * 直接把p的父节点指向p的指针置空
                 */
                if (p == p.parent.left)
                    p.parent.left = null;
                else if (p == p.parent.right)
                    p.parent.right = null;
                /**
                 * 删除p节点
                 */
                p.parent = null;
            }
        }
    }
    //=========================删除==========================

    //=========================查找==========================
    /**
     * 使用key查询节点对象的值
     * @param key
     * @return
     */
    public V get(Object key) {
        BstEntry<K,V> p = getEntry(key);
        return (p==null ? null : p.value);
    }
    /**
     * 使用key查询节点对象
     * @param key
     * @return
     */
    @SuppressWarnings("Duplicates")
    final BstEntry<K,V> getEntry(Object key) {
        // Offload comparator-based version for sake of performance
        if (comparator != null)
            return getEntryUsingComparator(key);
        if (key == null)
            throw new NullPointerException();
        //用实现了Comparable接口的key自己比较
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        BstEntry<K,V> p = root;
        /**
         * 比较逻辑与@getEntryUsingComparator()逻辑一致
         */
        while (p != null) {
            int cmp = k.compareTo(p.key);
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
        return null;
    }
    /**
     * 使用比较器查找
     * @param key   需要查找的key
     * @return
     */
    @SuppressWarnings("Duplicates")
    final BstEntry<K,V> getEntryUsingComparator(Object key) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            //父节点指针
            BstEntry<K,V> p = root;
            /**
             * 递归判断 需要查询的key和父节点指针所指的key大小
             * 如果小于 则把父节点指针向左子节点移动
             * 如果大于 则把父节点指针向右子节点移动
             * 如果等于 则父节点指针指向的对象就是需要查询的
             */
            while (p != null) {
                int cmp = cpr.compare(k, p.key);
                if (cmp < 0)
                    p = p.left;
                else if (cmp > 0)
                    p = p.right;
                else
                    return p;
            }
        }
        return null;
    }
    /**
     * 获取树中的最小节点
     * @return
     */
    final BstEntry<K,V> getFirstEntry() {
        /**
         * 从根节点开始
         * 递归判断是否有左子节点
         * 有就继续
         * 直到最后一个
         */
        BstEntry<K,V> p = root;
        if (p != null)
            while (p.left != null)
                p = p.left;
        return p;
    }
    /**
     * 与@getFirstEntry()同理(镜像操作)
     * @return
     */
    final BstEntry<K,V> getLastEntry() {
        BstEntry<K,V> p = root;
        if (p != null)
            while (p.right != null)
                p = p.right;
        return p;
    }
    /**
     * 获取后继节点
     * @param t
     * @param <K>
     * @param <V>
     * @return
     */
    static <K,V> BstEntry<K,V> successor(BstEntry<K,V> t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            /**
             * 如果t节点有右子树
             * 则直接查询右子树中最小的节点
             * 与@getFirstEntry()方法逻辑一致
             */
            BstEntry<K,V> p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } else {
            /**
             * 如果没有右子树
             * 则向上回溯
             * 直到孩子节点是父节点的左孩子(这样父节点正好是大于t的最小节点)
             */
            //父节点指针
            BstEntry<K,V> p = t.parent;
            //孩子节点指针
            BstEntry<K,V> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }
    /**
     * 获取前驱节点
     * 逻辑与@successor()相同(镜像)
     * @param t
     * @param <K>
     * @param <V>
     * @return
     */
    static <K,V> BstEntry<K,V> predecessor(BstEntry<K,V> t) {
        if (t == null)
            return null;
        else if (t.left != null) {
            BstEntry<K,V> p = t.left;
            while (p.right != null)
                p = p.right;
            return p;
        } else {
            BstEntry<K,V> p = t.parent;
            BstEntry<K,V> ch = t;
            while (p != null && ch == p.left) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }
    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }
    /**
     * 查询树中是否包含这个值
     * 就是一个线性遍历
     * @param value
     * @return
     */
    public boolean containsValue(Object value) {
        for (BstEntry<K,V> e = getFirstEntry(); e != null; e = successor(e))
            if (valEquals(value, e.value))
                return true;
        return false;
    }
    //=========================查找==========================

    /**
     * 树中的节点对象
     * @param <K>
     * @param <V>
     */
    static final class BstEntry<K,V> implements Map.Entry<K, V> {

        /**
         * 用于定位及排序的key
         */
        K key;
        /**
         * 节点需要存放的具体内容(可以没有)
         */
        V value;
        /**
         * 左子节点
         */
        BstEntry<K,V> left;
        /**
         * 右子节点
         */
        BstEntry<K,V> right;
        /**
         * 父节点(可以没有)
         */
        BstEntry<K,V> parent;

        BstEntry(K key, V value, BstEntry<K,V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;

            return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
        }

        public int hashCode() {
            int keyHash = (key==null ? 0 : key.hashCode());
            int valueHash = (value==null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        public String toString() {
            return key + "=" + value;
        }

    }

    //=========================一些常用方法封装==========================
    static final boolean valEquals(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }
    /**
     * 树节点的比较方法
     * 如果存在自定义的Comparator 则用他比较
     * 如果不存在，则用key直接比较
     * @param k1        如果不存在自定义的Comparator  则key必须实现Comparable接口
     * @param k2
     * @return
     */
    final int compare(Object k1, Object k2) {
        return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
                : comparator.compare((K)k1, (K)k2);
    }
    //=========================一些常用方法封装==========================

    /**
     * jdk中map是没有直接的iterator
     * 这里写个迭代器方便查看树中的节点信息
     * @return
     */
    @Override
    public Iterator<BstEntry<K, V>> iterator() {
        return new BstIterator<>(root);
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

}
