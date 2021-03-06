# take-TreeMap-apart
TreeMap是从二叉搜索树，平衡二叉树，红黑树一点点进化而来
*****

## bst(二叉搜索树)  
**在插入数据均匀分布时，就是折半查找**

**二叉搜索树的定义**
1. 若左子树不空，则左子树上所有结点的值均小于它的根结点的值；
2. 若右子树不空，则右子树上所有结点的值均大于它的根结点的值；
3. 左、右子树也分别为二叉排序树；
4. 没有键值相等的节点。

+ 插入:
    - 从根节点开始，递归比较大小，直至叶子叶子节点。
        1. 把新节点加上。
+ 删除:
    - 从根节点开始，递归比较大小，直至key相等。
        1. 如果该节点有两个孩子，则用后继节点的值替换它，然后删除后继节点(后继节点必然是叶子节点，执行c)
        2. 如果只有一个子节点，则把该节点的父节点和该节点的子节点相互指向(这样就没有指向该节点的了，就删除了)
        3. 如果是叶子节点，直接置空父节点指向改节点的指针
*****

## avl(平衡二叉树)  
**BST在key值是线性的时候，就又变成一维的查找了(就是数组)，AVL树能极大的提高线性key的查询效率**

**平衡二叉树的定义**
1. 首先它是BST
2. 左右子树高度差不超过1(递归)

平衡  
自底向上，一层一层的平衡，直到根。
+ 插入后情况  
    - 不需要调整
        1. 根节点或者左右子树高度差小于1
    - 需要调整(左右子树高度差大于1)
        1. 左子树高的情况(2)
            + case1: 插入节点是左子节点的左子节点
                1. 右旋父节点
            + case2:
                1. 把自己左旋
                2. 把原父节点右旋
        2. 右子树高的情况(-2)
            + 与上同理(镜像)
+ 删除后情况
    - 不需要调整
        1. 根节点或者左右子树高度差小于1
    - 需要调整(左右子树高度差大于1)
        1. 左子树高的情况(2)
            + case1: 插入节点是左子节点的左子节点
                1. 当前节点右旋
            + case1: 插入节点是左子节点的左子节点
                1. 当前节点右旋
            + case2:
                1. 当前节点x左子节点左旋
                2. 当前节点右旋
        2. 右子树高的情况(-2)
            + 与上同理(镜像)
*****

## rbt(红黑树)  
**AVL插入时平衡次数较多，RBT是AVL的折中方法(放宽平衡冗余，减少插入后平衡次数)，插入删除效率略高于AVL，查询效率略低于AVL**
+ 插入调整时(最坏的情况(需要回溯到顶))
    - avl是一层一层向上(logN)
    - rbt是两层两层向上(logN/2)
+ 插入调整时(最坏的情况(需要回溯到顶))
    - rbt在回溯的时候，只要碰到红色就结束了，所以略好于avl
+ 查询时(rbt最坏情况是左右子树差一倍高度)

rbt必定是bst  
rbt任意黑节点为根的子树必定是rbt(递归)

**红黑树的5个性质**  
1. 节点必须是红色或者黑色(所有叶子节点是NIL节点)。
2. 根节点必须是黑色。
3. 叶节点(NIL)是黑色的。（NIL节点无数据，是空节点）
4. 红色节点必须有两个黑色儿子节点 (所以父节点必定也是黑色)。
5. 从任一节点出发到其每个叶子节点的路径，黑色节点的数量是相等的(黑高 BlackHeight bh  实际应用中，可以忽略NIL节点，所以黑高少1)。

**以上条件能保证任意同级子树高度差不超过2倍**
+ BH(left)==BH(right)
+ 若H(left)>=H(right) 则H(left)<=2*H(right)+1
+ 若H(left)<=H(right) 则H(right)<=2*H(left)+1

定理:n个节点的RBT,最大高度是2log(n+1)

插入节点都默认红色(因为插入黑色，那必定黑高不平衡，就必须要调整了，就和avl一样了。所以插入红色，有部分几率不需要调整)

调整  
自底向上，一层一层的调整，直到父节点为黑色的时候，或者到根。

+ 插入后情况  
    - 不需要调整(父节点为黑色;或者插入的是根节点)  
        1. 父节点是黑色的情况：  
              因为rbt基于bst，所以插入的新节点只可能是叶子节点  
              所以插入的节点如果父节点是黑色，就满足rbt5条性质，不需要调整  
        2. 如果是根节点，直接把该节点设置为黑色  
    - 需要调整(父节点为红色)  
        (由于性质4，祖父节点必定是黑色)  
        叔叔节点(当前节点的祖父节点的另一个子节点)
        1. ·父节点·为·祖父节点·的左孩子的情况
            + case1:·叔叔节点·是红色。(把父层同时置黑，试满足第4性质，然后祖父可能又有冲突(冲突向上抛)，所以继续递归)
                1. 将·父节点·和·叔叔节点·设为黑色
                2. 将·祖父节点·设为红色
                3. 从·祖父节点·进行递归调整
            + case2:叔叔节点是黑色。且当前节点是其父节点的右孩子。(旧父节点的树一直满足5条性质，把不满足的当前节点继续递归(冲突向上抛))
                1. 将·父节点·左旋
                2. 从·新父节点·执行case3
            + case3:叔叔节点是黑色。且当前节点是其父节点的左孩子。(因为父节点和叔叔节点都是黑色，所以右旋后，祖父节点必定是黑色，已经满足所有性质，不需要递归了)
                1. 将·父节点·设为黑色
                2. 将·祖父节点·设为红色
                3. 将·祖父节点·右旋
                4. 从·新当前节点的右节点·继续进行递归调整(其实到这里就结束了！)
        2. ·父节点·为·祖父节点·的右孩子的情况  
            与上同理(镜像)  
+ 删除后情况
    - 不需要调整(删除的是红色节点，上下都是黑色节点，黑高平衡) 
        1. 回溯时，如果·当前节点·是·根节点·或者是·红色节点·，直接置黑
    - 需要调整(删除的是黑色节点，黑高不平衡)
        兄弟节点(sib,sibling 当前节点的父节点的另一个子节点)
        左右侄子(nephew,ln,rn 当前节点的父节点的另一个子节点的左右子节点)
        1. 删除节点为·父节点·的左孩子情况(左黑高低)
            + case1:·兄弟节点·为红色。(右树的根节点为红色，所以它下面的两个子树黑高一定平衡。把它父节点左旋，不影响它黑高平衡)(最终右黑高还是比做黑高大)
                1. 将·兄弟节点·设为黑色
                2. 将·父节点·设为红色
                3. 将·父节点·左旋
            + case2:·兄弟节点·为黑色;·左右侄子节点·为黑色
                1. 将·兄弟节点·设为红色
                2. 从·父节点·进行递归调整。
            + case3:·兄弟节点·为黑色;·左侄子节点·为红色;·右侄子节点·为黑色。(兄弟子树的黑高被减，然后把多的黑高向上抛)(必定兄弟节点为黑色，右侄子节点为红色，后一步必定是case4)  
                1. 将·左侄子节点·设为黑色
                2. 将·兄弟节点·设为红色
                3. 将·兄弟节点·右旋
            + case4:·兄弟节点·为黑色;·右侄子节点·为红色。
                    (如果父节点为黑色，左旋的时候，带走了左侄子节点，然后右侄子节点又被置为了黑色(黑高加一，又因为父节点被左旋，黑高减一，所以不动)而原来黑高少的左子树因为被加了一个黑色的父节点，所以黑高和右子树一样了；
                     如果父节点是红色，左旋同时设置兄弟节点为红色(新父节点还是红色)，右子树黑高被减一，左侄子节点被带到左子树(同样挂到黑节点下，黑高不变)，左子树上方则加了一个黑色节点，最终左右平衡)  
                1. 将·兄弟节点·设为·父节点·的颜色
                2. 将·父节点·设为黑色
                3. 将·右侄子节点·设为黑色
                4. 将·父节点·左旋
                5. 结束(因为原本减去的黑高又被加回来了，所以没必要再继续调整了)
        执行意义{
            case2执行完后，如果执行case1，并且最后·父节点·是黑色(现在左右黑高已经相等，但是·父节点·是黑色，所以不能保证·父节点·还平衡，需要递归调整)  
            case2执行完后，如果执行case2，并且最后·父节点·是红色(直接把·父节点·置黑，刚好补全了因为删除和·兄弟节点·置红而降低的黑高，结束)  
        }
        2. 删除节点为·父节点·的右孩子情况
            + 与上同理(镜像)  







