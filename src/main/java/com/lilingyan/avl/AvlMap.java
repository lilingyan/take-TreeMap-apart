package com.lilingyan.avl;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 平衡二叉搜索树
 * @Author: lilingyan
 * @Date 2019/2/17 17:22
 */
public class AvlMap<K,V> implements Iterable<AvlMap.AvlEntry<K,V>>{

    /**
     * 这颗树的根节点
     */
    private transient AvlEntry<K,V> root;
    /**
     * 自定义的Comparator 作用于树节点的排序(可以没有)
     * 如果没有 则用树节点的key compare直接比较(如果不存在自定义的Comparator  则key必须实现Comparable接口)
     */
    protected final Comparator<? super K> comparator;
    /**
     * 在这个树中存在的节点数量
     */
    private transient int size = 0;

    //=========================构造器==========================
    public AvlMap() {
        comparator = null;
    }
    public AvlMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }
    //=========================构造器==========================

    //=========================添加==========================
    /**
     * 加入一个节点
     * 解释参照BstMap#put
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("Duplicates")
    public V put(K key, V value) {
        AvlEntry<K,V> t = root;
        //如果根节点是空 则插入的就是根节点
        if (t == null) {

            //校验key类型(可能是null)
            compare(key, key);

            root = new AvlEntry<>(key, value, null);
            //树的节点数量置为1
            size = 1;
            return null;
        }
        //记录key与父节点比较的大小
        int cmp;
        //记录循环比较中的父节点指针
        AvlEntry<K,V> parent;
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
//                stack.push(parent);
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
//                stack.push(parent);
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
        AvlEntry<K,V> e = new AvlEntry<>(key, value, parent);
        //只可能大或者小  不可能存在等于
        if (cmp < 0)
            parent.left = e;
        else
            parent.right = e;
        //插入后处理(插入后平衡)
        fixAfterInsertion(e);
        //树节点数量增加1
        size++;
        return null;
    }
    //=========================添加==========================

    //=========================删除==========================
    /**
     * 删除节点
     * @param key   需要删除的节点的key
     * @return
     */
    public V remove(Object key) {
        AvlEntry<K,V> p = getEntry(key);
        if (p == null)
            return null;

        V oldValue = p.value;
        deleteEntry(p);
        return oldValue;
    }
    /**
     * 删除节点
     * 如果该节点有两个子节点，则取后继结点的值覆盖它，然后删除后继结点
     * 如果该节点只有一个子节点，则直接用子节点覆盖它
     * @param p 需要删除的节点对象
     */
    @SuppressWarnings("Duplicates")
    private void deleteEntry(AvlEntry<K,V> p) {
        size--;

        /**
         * 记录最终被删除的节点(只有删除的这个节点以上的节点，才可能不平衡)
         */
        AvlEntry<K,V> ns = p.parent;

        /**
         * 如果p节点有两个子孩子
         * 则查询出后后继节点
         * 用后继节点的值覆盖p，然后删除后继节点(因为有右子树，所以后继节点必定是右子树中最小的节点，必定是叶子节点)
         *
         * 这里也可以用p节点的前驱节点做同样的操作(bst也是正确的)
         */
        if (p.left != null && p.right != null) {
            AvlEntry<K,V> s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;

        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        AvlEntry<K,V> replacement = (p.left != null ? p.left : p.right);

        /**
         * p可能是要删除的节点，也可能是后继节点
         * 如果p有一个子节点replacement，则把p的子节点指针指向子节点replacement，子节点replacement也同时指向p(这样就清除了p的所有引用)
         */
        if (replacement != null) {

            /**
             * p的父子节点指针互指(跳过p)
             */
            // Link replacement to parent
            replacement.parent = p.parent;
            if (p.parent == null)
                //如果p是根节点，则把替换一下root指针
                root = replacement;
            else if (p == p.parent.left)
                p.parent.left  = replacement;
            else
                p.parent.right = replacement;

            /**
             * 清空p的指针(其实也没什么用)
             */
            p.left = p.right = p.parent = null;

            /**
             * 记录当前节点的父节点
             */
            ns = replacement.parent;

        } else if (p.parent == null) { // return if we are the only node.、
            /**
             * 如果p没有子节点也没有父节点
             * 则说明p是这颗树的最后节点
             * 直接把树置空
             */
            root = null;
        } else { //  No children. Use self as phantom replacement and unlink.
            /**
             * 如果p没有子节点，说明p是叶子节点
             * 直接把p的父节点指向p的指针置空
             */
            ns = p.parent;
            if (p == p.parent.left)
                p.parent.left = null;
            else if (p == p.parent.right)
                p.parent.right = null;
            /**
             * 清空p的指针(其实也没什么用)
             */
            p.parent = null;
        }
        //平衡操作过的节点
        fixAfterDeletion(ns);
    }
    //=========================删除==========================

    //=========================插入删除后的调整==========================
    /**
     * 插入后处理(插入后平衡)
     * @param x
     */
    @SuppressWarnings("Duplicates")
    private void fixAfterInsertion(AvlEntry<K,V> x) {
        //记录调整次数(因为只插入了一个节点，所以最多只可能调整一次)
        int bn = 0;
        //记录循环比较中的父节点指针
        AvlEntry<K,V> p;
        /**
         * 记录父节点的父节点指针信息
         * 父节点被旋转后，原父节点的父节点的子节点需要重新指向新父节点
         * 因为旋转后的新父节点不是原来的父节点
         * 所以需要记录下最早的父节点的父节点指针
         */
        AvlEntry<K,V> op;
        /**
         * 一直向上回溯
         * 直到根节点
         */
        p = x.parent;
        while (p!=null){
            //记录父节点的父节点
            op=p.parent;
            /**
             * 判断是否已经调整过(只可能一次)
             */
            if(bn <1){
                //重新计算高度
                p.height = Math.max(getHeight(p.left),getHeight(p.right))+1;
                //计算平衡因子
                int cmp = getHeight(p.left)-getHeight(p.right);
                /**
                 * 如果平衡因子小于一
                 * 说明此子树还是平衡的
                 * 继续计算上层节点
                 */
                if(Math.abs(cmp) <= 1){
                    p = op;
                    continue;
                }else{
                    //刷新调整次数
                    bn++;
                    if(cmp == 2){
                        //如果是左子树过高
                        if(compare(x.key,p.left.key)<0){
                            /**
                             * 如果x插入在p的左节点上
                             * 只需向右转一次
                             */
                            p = rotateRight(p);
                        }else{
                            /**
                             * 如果x插入在p的左节点上
                             * 则需要先把p的做节点左旋
                             * 再把p右旋
                             */
                            p.left = rotateLeft(p.left);
                            p = rotateRight(p);
                        }
                    }else{  //d==-2
                        if(compare(x.key,p.right.key)>0){
                            //如果是右子树过高
                            /**
                             * 与上面的左节点过高处理逻辑一样(镜像)
                             */
                            p = rotateLeft(p);
                        }else{
                            p.right = rotateRight(p.right);
                            p = rotateLeft(p);
                        }
                    }
                    /**
                     * 父节点节点旋转完成之后
                     * 需要把新父节点挂载到父节点的父节点上
                     */
                    if(op!=null){
                        if(compare(x.key,op.key)<0){
                            //直接把父节点的左节点指针指向p
                            op.left=p;
                        }else{
                            //与上同理
                            op.right=p;
                        }
                    }
                }
            }
            /**
             * 记录新父节点(如果旋转)
             * 直到回溯至根
             */
            root = p;
            p = op;
        }
    }
    /**
     * 删除后处理(删除后平衡)
     * @param x
     */
    private AvlEntry<K,V> fixAfterDeletion(AvlEntry<K,V> x) {
        while(x!=null){
            //重新计算高度
            x.height = Math.max(getHeight(x.left),getHeight(x.right))+1;
            //计算平衡因子
            int cmp = getHeight(x.left)-getHeight(x.right);
            if(cmp==2){
                //左子树高
                /**
                 * 有三种情况
                 * 左子树平衡因子=1        当前节点x右旋
                 * 左子树平衡因子=0        当前节点x右旋
                 * 左子树平衡因子=-1       当前节点x左子节点先左旋再当前节点右旋
                 */
                if(getHeight(x.left.left)-getHeight(x.left.right)>=0){
                    x = rotateRight(x);
                }else{
                    x.left = rotateLeft(x.left);
                    x = rotateRight(x);
                }
            }else if(cmp==-2){
                //同上(镜像)
                if(getHeight(x.right.right)-getHeight(x.right.left)>=0){
                    x = rotateLeft(x);
                }else{
                    x.right = rotateRight(x.right);
                    x = rotateLeft(x);
                }
            }
            x = x.parent;
        }
        return x;
    }
    //=========================插入删除后的调整==========================

    //=========================左右旋转==========================
    /**
     * 节点左旋
     * 参照算法导论
     * @param p
     */
    private AvlEntry<K, V> rotateLeft(AvlEntry<K,V> p) {
        if (p != null) {
            //先获取p的右子节点(既然是左旋，右子节点必须有)
            AvlEntry<K,V> r = p.right;
            //如果p的右子节点有左子节点，则把它挂载到p的右边(如果没有则挂null上去 所以jdk中没有判断)
            p.right = r.left;
            //如果p的右子节点有左子节点，则重置它的父节点为p
            if (r.left != null)
                r.left.parent = p;
            //把p的父节点赋给p的右子节点(用p的右子节点替代了p的位置)
            r.parent = p.parent;
            //如果p的父节点为空，说明p是根节点，而现在p的右子节点替换p的位置，所以记录根节点为p的右子节点
            if (p.parent == null)
                root = r;
                //因为要用p的右子节点替换p
                //所以判断p在父节点的位置是左子节点还是右子节点，p的右子节点要替换到同样的位置上
            else if (p.parent.left == p)
                p.parent.left = r;
            else
                p.parent.right = r;
            //最后把p挂载到p右子节点的左子节点上
            r.left = p;
            p.parent = r;

            /**
             * 旋转后
             * 重新计算p和p右子节点的高度（拿到最高的子节点加一，会从一开始，一直被加上去）
             * jdk中TreeMap是红黑树实现，所以没有这个
             */
            p.height = Math.max(getHeight(p.left),getHeight(p.right))+1;
            r.height = Math.max(getHeight(r.left),getHeight(r.right))+1;

            /**
             * 原来p节点的位置被p的右子节点取代
             */
            return r;
        }
        return null;
    }
    /**
     * 节点右旋
     * 与@rotateLeft()同理(镜像)
     * 参照算法导论
     * @param p
     */
    private AvlEntry<K, V> rotateRight(AvlEntry<K,V> p) {
        if (p != null) {
            AvlEntry<K,V> l = p.left;
            p.left = l.right;
            if (l.right != null) l.right.parent = p;
            l.parent = p.parent;
            if (p.parent == null)
                root = l;
            else if (p.parent.right == p)
                p.parent.right = l;
            else p.parent.left = l;
            l.right = p;
            p.parent = l;

            //
            p.height = Math.max(getHeight(p.left),getHeight(p.right))+1;
            l.height = Math.max(getHeight(l.left),getHeight(l.right))+1;
            return l;
        }
        return null;
    }
    //=========================左右旋转==========================

    //=========================查找==========================
    /**
     * 使用key查询节点对象的值
     * @param key
     * @return
     */
    public V get(Object key) {
        AvlEntry<K,V> p = getEntry(key);
        return (p==null ? null : p.value);
    }
    /**
     * 使用key查询节点对象
     * @param key
     * @return
     */
    @SuppressWarnings("Duplicates")
    final AvlEntry<K,V> getEntry(Object key) {
        // Offload comparator-based version for sake of performance
        if (comparator != null)
            return getEntryUsingComparator(key);
        if (key == null)
            throw new NullPointerException();
        //用实现了Comparable接口的key自己比较
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        AvlEntry<K,V> p = root;
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
    final AvlEntry<K,V> getEntryUsingComparator(Object key) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            //父节点指针
            AvlEntry<K,V> p = root;
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
     * 获取后继节点
     * @param t
     * @param <K>
     * @param <V>
     * @return
     */
    @SuppressWarnings("Duplicates")
    static <K,V> AvlEntry<K,V> successor(AvlEntry<K,V> t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            /**
             * 如果t节点有右子树
             * 则直接查询右子树中最小的节点
             * 与@getFirstEntry()方法逻辑一致
             */
            AvlEntry<K,V> p = t.right;
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
            AvlEntry<K,V> p = t.parent;
            //孩子节点指针
            AvlEntry<K,V> ch = t;
            while (p != null && ch == p.right) {
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
    //=========================查找==========================

    static final class AvlEntry<K,V> implements Map.Entry<K, V> {

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
        AvlEntry<K,V> left;
        /**
         * 右子节点
         */
        AvlEntry<K,V> right;
        /**
         * 父节点(可以没有)
         */
        AvlEntry<K,V> parent;
        /**
         * 当前节点的高度
         * jdk中TreeMap是红黑树实现，所以没有这个
         */
        int height = 1;

        AvlEntry(K key, V value, AvlEntry<K,V> parent) {
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
            String s ="";
            s+="key="+key;
            s+="height="+height;
            if(this.parent!=null){
                s+="parent="+this.parent.key;
            }
            if(this.left!=null){
                s+="left="+this.left.key;
            }
            if(this.right!=null){
                s+="right="+this.right.key;
            }
            return s;
        }

    }

    //=========================一些常用方法封装==========================
    /**
     * 树节点的比较方法
     * 如果存在自定义的Comparator 则用他比较
     * 如果不存在，则用key直接比较
     * @param k1        如果不存在自定义的Comparator  则key必须实现Comparable接口
     * @param k2
     * @return
     */
    protected final int compare(Object k1, Object k2) {
        return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
                : comparator.compare((K)k1, (K)k2);
    }
    public static final boolean valEquals(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }
    /**
     * 获取节点高度
     * @param p
     * @return
     */
    public int getHeight(AvlEntry<K,V> p){
        return p==null?0:p.height;
    }
    /**
     * map平衡校验
     * @throws Exception
     */
    public void checkBalance() throws Exception {
        postOrderCheckBalance(root);
    }
    /**
     * 节点平衡校验
     * @param p
     * @throws Exception
     */
    private void postOrderCheckBalance(AvlEntry<K,V> p) throws Exception {
        if(p!=null){
            postOrderCheckBalance(p.left);
            postOrderCheckBalance(p.right);
            if(Math.abs(getHeight(p.left)-getHeight(p.right))>1){
                throw new Exception("此树不平衡");
            }
        }
    }
    /**
     * 层序输出
     */
    public void levelOrder() {
        LinkedBlockingQueue<AvlEntry<K,V>> queue = new LinkedBlockingQueue<>();
        if(root == null){
            return;
        }
        queue.offer(root);
        int preCount=1;
        int pCount=0;
        while(!queue.isEmpty()){
            preCount--;
            AvlEntry<K, V> p = queue.poll();
            System.out.print(p);
            if(p.left!=null){
                queue.offer(p.left);
                pCount++;
            }
            if(p.right!=null){
                queue.offer(p.right);
                pCount++;
            }
            if(preCount==0){
                preCount=pCount;
                pCount=0;
                System.out.println();
            }
        }

    }
    //=========================一些常用方法封装==========================

    @Override
    public Iterator iterator() {
        return new AvlIterator<>(root);
    }

    public int size() {
        return this.size;
    }

}
