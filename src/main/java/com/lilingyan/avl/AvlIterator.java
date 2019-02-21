package com.lilingyan.avl;

import java.util.Iterator;
import java.util.Stack;

/**
 * 中序遍历
 * 从根节点开始
 * 按层递归 把所有左子节点压入栈中
 * 然后弹出一个(输出)，并把他的右子节点压入栈中
 * 重复操作 直到树为空(栈为空)
 * @Author: lilingyan
 * @Date 2019/2/17 10:40
 */
public class AvlIterator<K,V> implements Iterator<AvlMap.AvlEntry<K,V>> {

    private Stack<AvlMap.AvlEntry<K,V>> stack;

    public AvlIterator(AvlMap.AvlEntry<K,V> root) {
        this.stack = new Stack<>();
        addLeftPath(root);
    }

    private void addLeftPath(AvlMap.AvlEntry<K,V> p){
        while (p!=null){
            stack.push(p);
            p=p.left;
        }
    }

    @Override
    public boolean hasNext() {
        return !this.stack.isEmpty();
    }

    @Override
    public AvlMap.AvlEntry<K, V> next() {
        AvlMap.AvlEntry<K, V> p = this.stack.pop();
        addLeftPath(p.right);
        return p;
    }

}
