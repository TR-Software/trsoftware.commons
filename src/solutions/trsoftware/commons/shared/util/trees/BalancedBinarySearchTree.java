package solutions.trsoftware.commons.shared.util.trees;

/**
 * Implementation of an <a href="https://en.wikipedia.org/wiki/AVL_tree">AVL Tree</a> data structure.
 *
 * @see <a href="https://www.geeksforgeeks.org/avl-tree-set-1-insertion/">AVL insertion example</a>
 *
 * @author Alex
 * @since 2/9/2018
 */
public class BalancedBinarySearchTree<K extends Comparable<K>, V> {

  // TODO: fix the parent references (see unit test)

  private BSTNode<K, V> root;

  public BSTNode<K, V> getRoot() {
    return root;
  }

  // A utility function to get height of the tree
  private int height(BSTNode<K, V> node) {
    if (node == null)
      return 0;

    return node.getHeight();
  }

  // A utility function to right rotate subtree rooted with y
  // See the diagram given above.
  private BSTNode<K, V> rotateRight(BSTNode<K, V> x) {
    BSTNode<K, V> z = x.getLeft();
    BSTNode<K, V> t23 = z.getRight();

    // Perform rotation
    x.setLeft(t23);
    if (t23 != null)
      t23.setParent(x);
    z.setRight(x);
    x.setParent(z);

    // Update heights
    x.setHeight(Math.max(height(x.getLeft()), height(x.getRight())) + 1);
    z.setHeight(Math.max(height(z.getLeft()), height(z.getRight())) + 1);

    // Return new root
    return z;
  }

  // A utility function to left rotate subtree rooted with x
  // See the diagram given above.
  private BSTNode<K, V> rotateLeft(BSTNode<K, V> x) {
    BSTNode<K, V> z = x.getRight();
    BSTNode<K, V> t23 = z.getLeft();

    // Perform rotation
    x.setRight(t23);
    if (t23 != null)
      t23.setParent(x);
    z.setLeft(x);
    x.setParent(z);

    //  Update heights
    x.setHeight(Math.max(height(x.getLeft()), height(x.getRight())) + 1);
    z.setHeight(Math.max(height(z.getLeft()), height(z.getRight())) + 1);

    // Return new root
    return z;
  }

  // Get Balance factor of node N
  int getBalance(BSTNode<K, V> N) {
    if (N == null)
      return 0;
    // TODO: rewrite this as height(right) - height(left) to match the wikipedia def (be sure to update all usages)
    return height(N.getLeft()) - height(N.getRight());
  }

  public void put(K key, V value) {
    root = insert(root, key, value);
  }

  private BSTNode<K, V> insert(BSTNode<K, V> node, K key, V value) {

    /* 1.  Perform the normal BST insertion */
    if (node == null)
      return (new BSTNode<K, V>(key, value, null, 1));

    int cmp = key.compareTo(node.getKey());
    if (cmp < 0)
      node.setLeft(insert(node.getLeft(), key, value));
    else if (cmp > 0)
      node.setRight(insert(node.getRight(), key, value));
    else {
      // found a node with matching key; update its value
      node.setValue(value);
      return node;
    }

    // 2. Update height of this ancestor node
    node.setHeight(1 + Math.max(height(node.getLeft()), height(node.getRight())));

    // 3. Get the balance factor of this ancestor node to check whether this node became unbalanced
    int balance = getBalance(node);

    // If this node becomes unbalanced, then there are 4 cases:
    if (balance > 1) {
      // Left Left Case
      if (key.compareTo(node.getLeft().getKey()) < 0)
        return rotateRight(node);
      // Left Right Case
      if (key.compareTo(node.getLeft().getKey()) > 0) {
        BSTNode<K, V> y = rotateLeft(node.getLeft());
        node.setLeft(y);
        y.setParent(node);
        return rotateRight(node);
      }
    }
    else if (balance < -1) {
      // Right Right Case
      if (key.compareTo(node.getRight().getKey()) > 0)
        return rotateLeft(node);
      // Right Left Case
      if (key.compareTo(node.getRight().getKey()) < 0) {
        BSTNode<K, V> y = rotateRight(node.getRight());
        node.setRight(y);
        y.setParent(node);
        return rotateLeft(node);
      }
    }

    // return the (unchanged) node pointer
    return node;
  }
}
