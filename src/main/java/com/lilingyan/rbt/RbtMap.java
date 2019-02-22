package com.lilingyan.rbt;

import java.util.Comparator;
import java.util.Map;

/**
 * 红黑树
 * @Author: lilingyan
 * @Date 2019/2/22 11:15
 */
public class RbtMap<K,V> {

    /**
     * 自定义的Comparator 作用于树节点的排序(可以没有)
     * 如果没有 则用树节点的key compare直接比较(如果不存在自定义的Comparator  则key必须实现Comparable接口)
     */
    protected final Comparator<? super K> comparator;
    /**
     * 这颗树的根节点
     */
    private transient RbtEntry<K,V> root;
    /**
     * 在这个树中存在的节点数量
     */
    private transient int size = 0;

    private static final boolean RED   = false;
    private static final boolean BLACK = true;

    //=========================构造器==========================
    public RbtMap() {
        comparator = null;
    }
    public RbtMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }
    //=========================构造器==========================

    //=========================添加==========================
    @SuppressWarnings("Duplicates")
    public V put(K key, V value) {
        RbtEntry<K,V> t = root;
        //如果根节点是空 则插入的就是根节点
        if (t == null) {

            //校验key类型(可能是null)
            compare(key, key);

            root = new RbtEntry<>(key, value, null);
            //树的节点数量置为1
            size = 1;
            return null;
        }
        //记录key与父节点比较的大小
        int cmp;
        //记录循环比较中的父节点指针
        RbtEntry<K,V> parent;
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
        RbtEntry<K,V> e = new RbtEntry<>(key, value, parent);
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
        RbtEntry<K,V> p = getEntry(key);
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
    private void deleteEntry(RbtEntry<K,V> p) {
        size--;

        /**
         * 如果p节点有两个子孩子
         * 则查询出后后继节点
         * 用后继节点的值覆盖p，然后删除后继节点(因为有右子树，所以后继节点必定是右子树中最小的节点，必定是叶子节点)
         *
         * 这里也可以用p节点的前驱节点做同样的操作(bst也是正确的)
         */
        if (p.left != null && p.right != null) {
            RbtEntry<K,V> s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        RbtEntry<K,V> replacement = (p.left != null ? p.left : p.right);

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
                root = replacement;
            else if (p == p.parent.left)
                p.parent.left  = replacement;
            else
                p.parent.right = replacement;

            /**
             * 清空p的指针(其实也没什么用)
             */
            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = p.right = p.parent = null;

            /**
             * 只有删除的是黑节点，才需要调整
             */
            if (p.color == BLACK)
                fixAfterDeletion(replacement);
        } else if (p.parent == null) { // return if we are the only node.
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
            if (p.color == BLACK)
                /**
                 * 只有删除的是黑节点，才需要调整
                 */
                fixAfterDeletion(p);

            if (p.parent != null) {
                if (p == p.parent.left)
                    p.parent.left = null;
                else if (p == p.parent.right)
                    p.parent.right = null;
                p.parent = null;
            }
        }
    }
    //=========================删除==========================

    //=========================插入删除后的调整==========================
    /**
     * 插入后的调整
     * @param x
     */
    private void fixAfterInsertion(RbtEntry<K,V> x) {
        //先默认插入的节点是红色
        x.color = RED;

        /**
         * 只有根节点以下，并且父节点是红色的节点才需要调整
         */
        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                //·父节点·是·祖父节点·的左孩子(为了知道·叔叔节点·(当前节点的祖父节点的另一个子节点)在·祖父节点·的左边还是右边)
                //获取·叔叔节点·
                RbtEntry<K,V> y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {    //case1
                    //·叔叔节点·是红色   ·当前节点·不管是左是右
                    /**
                     * 将·父节点·和·叔叔节点·设为黑色
                     */
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    //将·祖父节点·设为红色
                    setColor(parentOf(parentOf(x)), RED);
                    //从·祖父节点·继续进行递归调整。
                    x = parentOf(parentOf(x));
                } else {    //如果·叔叔节点·是黑色，分·当前节点·在·父节点·的左边还是右边两种情况
                    if (x == rightOf(parentOf(x))) {    //case2
                        //当前节点是其父节点的右孩子。
                        /**
                         * 将·父节点·左旋
                         * 从·新父节点·执行case3
                         */
                        x = parentOf(x);
                        rotateLeft(x);
                    }

                    /**
                     * case3
                     * 将·父节点·设为黑色
                     * 将·祖父节点·设为红色
                     * 将·祖父节点·右旋
                     * 从·新当前节点的右节点·继续进行递归调整(其实到这里就结束了！)。
                     */
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {    //与上同理(镜像)
                RbtEntry<K,V> y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        //根节点必须是黑色。
        root.color = BLACK;
    }
    /**
     *删除后调整
     * @param x
     */
    private void fixAfterDeletion(RbtEntry<K,V> x) {
        //只有非根的黑色节点才需要调整(指针回溯时用)
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                //删除节点为·父节点·的左孩子情况
                //获取兄弟节点(sib,sibling 当前节点的父节点的另一个子节点)
                RbtEntry<K,V> sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {  //case1
                    //·兄弟节点·为红色(所以两个侄子节点必定是黑色的)。
                    /**
                     * 1.将·兄弟节点·设为黑色
                     * 2.将·父节点·设为红色
                     * 3.将·父节点·左旋
                     */
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    //重置·原左侄子·为兄弟节点(必定是黑色)
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib))  == BLACK &&
                        colorOf(rightOf(sib)) == BLACK) {   //case2
                    //·兄弟节点·为黑色;·左右侄子节点·为黑色
                    /**
                     * 1.将·兄弟节点·设为红色
                     * 2.从·父节点·进行递归调整。
                     */
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {   //case3
                        //兄弟节点·为黑色;·左侄子节点·为红色(如果两个都是黑色，则执行了上面的if，不会执行到这句话了);·右侄子节点·为黑色。
                        /**
                         * 1.将·左侄子节点·设为黑色
                         * 2.将·兄弟节点·设为红色
                         * 3.将·兄弟节点·右旋
                         */
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        //重置·原左侄子·为兄弟节点(必定是红色)
                        sib = rightOf(parentOf(x));
                    }
                    //case4
                    /**
                     * 1.将·兄弟节点·设为·父节点·的颜色
                     * 2.将·父节点·设为黑色
                     * 3.将·右侄子节点·设为黑色
                     * 4.将·父节点·左旋
                     * 5.结束(因为原本减去的黑高又被加回来了，所以没必要再继续调整了)
                     */
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    //直接结束
                    x = root;
                }
            } else { // symmetric
                //与上面逻辑相等(镜像)
                RbtEntry<K,V> sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK &&
                        colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        //根节点必定黑色
        setColor(x, BLACK);
    }
    //=========================插入删除后的调整==========================

    //=========================左右旋转==========================
    /**
     * 节点左旋
     * 参照算法导论
     * @param p
     */
    private void rotateLeft(RbtEntry<K,V> p) {
        if (p != null) {
            //先获取p的右子节点(既然是左旋，右子节点必须有)
            RbtEntry<K,V> r = p.right;
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
        }
    }
    /**
     * 节点右旋
     * 与@rotateLeft()同理(镜像)
     * 参照算法导论
     * @param p
     */
    private void rotateRight(RbtEntry<K,V> p) {
        if (p != null) {
            RbtEntry<K,V> l = p.left;
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
        }
    }
    //=========================左右旋转==========================

    //=========================查找==========================
    /**
     * 使用key查询节点对象的值
     * @param key
     * @return
     */
    public V get(Object key) {
        RbtEntry<K,V> p = getEntry(key);
        return (p==null ? null : p.value);
    }
    /**
     * 使用key查询节点对象
     * @param key
     * @return
     */
    @SuppressWarnings("Duplicates")
    final RbtEntry<K,V> getEntry(Object key) {
        // Offload comparator-based version for sake of performance
        if (comparator != null)
            return getEntryUsingComparator(key);
        if (key == null)
            throw new NullPointerException();
        //用实现了Comparable接口的key自己比较
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        RbtEntry<K,V> p = root;
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
    final RbtEntry<K,V> getEntryUsingComparator(Object key) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            //父节点指针
            RbtEntry<K,V> p = root;
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
    static <K,V> RbtEntry<K,V> successor(RbtEntry<K,V> t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            /**
             * 如果t节点有右子树
             * 则直接查询右子树中最小的节点
             * 与@getFirstEntry()方法逻辑一致
             */
            RbtEntry<K,V> p = t.right;
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
            RbtEntry<K,V> p = t.parent;
            //孩子节点指针
            RbtEntry<K,V> ch = t;
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

    /**
     * 节点类
     * @param <K>
     * @param <V>
     */
    static final class RbtEntry<K,V> implements Map.Entry<K,V> {
        K key;
        V value;
        RbtEntry<K,V> left;
        RbtEntry<K,V> right;
        RbtEntry<K,V> parent;
        boolean color = BLACK;

        /**
         * Make a new cell with given key, value, and parent, and with
         * {@code null} child links, and BLACK color.
         */
        RbtEntry(K key, V value, RbtEntry<K,V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

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
    private static <K,V> boolean colorOf(RbtEntry<K,V> p) {
        return (p == null ? BLACK : p.color);
    }
    private static <K,V> RbtEntry<K,V> parentOf(RbtEntry<K,V> p) {
        return (p == null ? null: p.parent);
    }
    private static <K,V> void setColor(RbtEntry<K,V> p, boolean c) {
        if (p != null)
            p.color = c;
    }
    private static <K,V> RbtEntry<K,V> leftOf(RbtEntry<K,V> p) {
        return (p == null) ? null: p.left;
    }
    private static <K,V> RbtEntry<K,V> rightOf(RbtEntry<K,V> p) {
        return (p == null) ? null: p.right;
    }
    final int compare(Object k1, Object k2) {
        return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
                : comparator.compare((K)k1, (K)k2);
    }
    //=========================一些常用方法封装==========================

    public int size() {
        return this.size;
    }

}
