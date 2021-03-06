package com.jatin.ds.trees;

// A BTree node
public class BTreeNode {
	int[] keys; // An array of keys
	int t; // Minimum degree (defines the range for number of keys)
	BTreeNode c[]; // An array of child pointers
	int n; // Current number of keys
	boolean leaf; // Is true when node is leaf. Otherwise false

	public BTreeNode(int _t, boolean _leaf) {
		// Copy the given minimum degree and leaf property
		t = _t;
		leaf = _leaf;

		// Allocate memory for maximum number of possible keys
		// and child pointers
		keys = new int[2 * t - 1];
		c = new BTreeNode[2 * t];

		// Initialize the number of keys as 0
		n = 0;
	}

	// A function to traverse all nodes in a subtree rooted with this node
	public void traverse() {
		// There are n keys and n+1 children, travers through n keys
		// and first n children
		int i;
		for (i = 0; i < n; i++) {
			// If this is not leaf, then before printing key[i],
			// traverse the subtree rooted with child c[i].
			if (leaf == false)
				c[i].traverse();
			System.out.print(keys[i]);
		}

		// Print the subtree rooted with last child
		if (leaf == false)
			c[i].traverse();
	}

	// A function to search a key in subtree rooted with this node.
	// returns null if k is not present.
	public BTreeNode search(int k) {

		// Find the first key greater than or equal to k
		int i = 0;
		while (i < n && k > keys[i])
			i++;

		// If the found key is equal to k, return this node
		if (keys[i] == k)
			return this;

		// If key is not found here and this is a leaf node
		if (leaf == true)
			return null;

		// Go to the appropriate child
		return c[i].search(k);
	}

	// A utility function to insert a new key in this node
	// The assumption is, the node must be non-full when this
	// function is called
	void insertNonFull(int k) {
		// Initialize index as index of rightmost element
		int i = n - 1;

		// If this is a leaf node
		if (leaf == true) {
			// The following loop does two things
			// a) Finds the location of new key to be inserted
			// b) Moves all greater keys to one place ahead
			while (i >= 0 && keys[i] > k) {
				keys[i + 1] = keys[i];
				i--;
			}

			// Insert the new key at found location
			keys[i + 1] = k;
			n = n + 1;
		} else // If this node is not leaf
		{
			// Find the child which is going to have the new key
			while (i >= 0 && keys[i] > k)
				i--;

			// See if the found child is full
			if (c[i + 1].n == 2 * t - 1) {
				// If the child is full, then split it
				splitChild(i + 1, c[i + 1]);

				// After split, the middle key of c[i] goes up and
				// c[i] is splitted into two. See which of the two
				// is going to have the new key
				if (keys[i + 1] < k)
					i++;
			}
			c[i + 1].insertNonFull(k);
		}
	}

	// A utility function to split the child y of this node
	// Note that y must be full when this function is called
	void splitChild(int i, BTreeNode y) {
		// Create a new node which is going to store (t-1) keys
		// of y
		BTreeNode z = new BTreeNode(y.t, y.leaf);
		z.n = t - 1;

		// Copy the last (t-1) keys of y to z
		for (int j = 0; j < t - 1; j++)
			z.keys[j] = y.keys[j + t];

		// Copy the last t children of y to z
		if (y.leaf == false) {
			for (int j = 0; j < t; j++)
				z.c[j] = y.c[j + t];
		}

		// Reduce the number of keys in y
		y.n = t - 1;

		// Since this node is going to have a new child,
		// create space of new child
		for (int j = n; j >= i + 1; j--)
			c[j + 1] = c[j];

		// Link the new child to this node
		c[i + 1] = z;

		// A key of y will move to this node. Find location of
		// new key and move all greater keys one space ahead
		for (int j = n - 1; j >= i; j--)
			keys[j + 1] = keys[j];

		// Copy the middle key of y to this node
		keys[i] = y.keys[t - 1];

		// Increment count of keys in this node
		n = n + 1;
	}

	// A utility function that returns the index of the first key that is
	// greater than or equal to k
	int findKey(int k) {
		int idx = 0;
		while (idx < n && keys[idx] < k)
			++idx;
		return idx;
	}

	// A function to remove the key k from the sub-tree rooted with this node
	void remove(int k) {
		int idx = findKey(k);

		// The key to be removed is present in this node
		if (idx < n && keys[idx] == k) {

			// If the node is a leaf node - removeFromLeaf is called
			// Otherwise, removeFromNonLeaf function is called
			if (leaf)
				removeFromLeaf(idx);
			else
				removeFromNonLeaf(idx);
		} else {

			// If this node is a leaf node, then the key is not present in tree
			if (leaf) {
				System.out.println("The key " + k
						+ " is does not exist in the tree\n");
				return;
			}

			// The key to be removed is present in the sub-tree rooted with this
			// node
			// The flag indicates whether the key is present in the sub-tree
			// rooted
			// with the last child of this node
			boolean flag = ((idx == n) ? true : false);

			// If the child where the key is supposed to exist has less that t
			// keys,
			// we fill that child
			if (c[idx].n < t)
				fill(idx);

			// If the last child has been merged, it must have merged with the
			// previous
			// child and so we recurse on the (idx-1)th child. Else, we recurse
			// on the
			// (idx)th child which now has atleast t keys
			if (flag && idx > n)
				c[idx - 1].remove(k);
			else
				c[idx].remove(k);
		}
		return;
	}

	// A function to remove the idx-th key from this node - which is a leaf node
	void removeFromLeaf(int idx) {

		// Move all the keys after the idx-th pos one place backward
		for (int i = idx + 1; i < n; ++i)
			keys[i - 1] = keys[i];

		// Reduce the count of keys
		n--;

		return;
	}

	// A function to remove the idx-th key from this node - which is a non-leaf
	// node
	void removeFromNonLeaf(int idx) {

		int k = keys[idx];

		// If the child that precedes k (c[idx]) has atleast t keys,
		// find the predecessor 'pred' of k in the subtree rooted at
		// c[idx]. Replace k by pred. Recursively delete pred
		// in c[idx]
		if (c[idx].n >= t) {
			int pred = getPred(idx);
			keys[idx] = pred;
			c[idx].remove(pred);
		}

		// If the child c[idx] has less that t keys, examine c[idx+1].
		// If c[idx+1] has atleast t keys, find the successor 'succ' of k in
		// the subtree rooted at c[idx+1]
		// Replace k by succ
		// Recursively delete succ in c[idx+1]
		else if (c[idx + 1].n >= t) {
			int succ = getSucc(idx);
			keys[idx] = succ;
			c[idx + 1].remove(succ);
		}

		// If both c[idx] and c[idx+1] has less that t keys,merge k and all of
		// c[idx+1]
		// into c[idx]
		// Now c[idx] contains 2t-1 keys
		// Free c[idx+1] and recursively delete k from c[idx]
		else {
			merge(idx);
			c[idx].remove(k);
		}
		return;
	}

	// A function to get predecessor of keys[idx]
	int getPred(int idx) {
		// Keep moving to the right most node until we reach a leaf
		BTreeNode cur = c[idx];
		while (!cur.leaf)
			cur = cur.c[cur.n];

		// Return the last key of the leaf
		return cur.keys[cur.n - 1];
	}

	int getSucc(int idx) {

		// Keep moving the left most node starting from c[idx+1] until we reach
		// a leaf
		BTreeNode cur = c[idx + 1];
		while (!cur.leaf)
			cur = cur.c[0];

		// Return the first key of the leaf
		return cur.keys[0];
	}

	// A function to fill child c[idx] which has less than t-1 keys
	void fill(int idx) {

		// If the previous child(c[idx-1]) has more than t-1 keys, borrow a key
		// from that child
		if (idx != 0 && c[idx - 1].n >= t)
			borrowFromPrev(idx);

		// If the next child(c[idx+1]) has more than t-1 keys, borrow a key
		// from that child
		else if (idx != n && c[idx + 1].n >= t)
			borrowFromNext(idx);

		// Merge c[idx] with its sibling
		// If c[idx] is the last child, merge it with with its previous sibling
		// Otherwise merge it with its next sibling
		else {
			if (idx != n)
				merge(idx);
			else
				merge(idx - 1);
		}
		return;
	}

	// A function to borrow a key from c[idx-1] and insert it
	// into c[idx]
	void borrowFromPrev(int idx) {

		BTreeNode child = c[idx];
		BTreeNode sibling = c[idx - 1];

		// The last key from c[idx-1] goes up to the parent and key[idx-1]
		// from parent is inserted as the first key in c[idx]. Thus, the loses
		// sibling one key and child gains one key

		// Moving all key in c[idx] one step ahead
		for (int i = child.n - 1; i >= 0; --i)
			child.keys[i + 1] = child.keys[i];

		// If c[idx] is not a leaf, move all its child pointers one step ahead
		if (!child.leaf) {
			for (int i = child.n; i >= 0; --i)
				child.c[i + 1] = child.c[i];
		}

		// Setting child's first key equal to keys[idx-1] from the current node
		child.keys[0] = keys[idx - 1];

		// Moving sibling's last child as c[idx]'s first child
		if (!leaf)
			child.c[0] = sibling.c[sibling.n];

		// Moving the key from the sibling to the parent
		// This reduces the number of keys in the sibling
		keys[idx - 1] = sibling.keys[sibling.n - 1];

		child.n += 1;
		sibling.n -= 1;

		return;
	}

	// A function to borrow a key from the c[idx+1] and place
	// it in c[idx]
	void borrowFromNext(int idx) {

		BTreeNode child = c[idx];
		BTreeNode sibling = c[idx + 1];

		// keys[idx] is inserted as the last key in c[idx]
		child.keys[(child.n)] = keys[idx];

		// Sibling's first child is inserted as the last child
		// into c[idx]
		if (!(child.leaf))
			child.c[(child.n) + 1] = sibling.c[0];

		// The first key from sibling is inserted into keys[idx]
		keys[idx] = sibling.keys[0];

		// Moving all keys in sibling one step behind
		for (int i = 1; i < sibling.n; ++i)
			sibling.keys[i - 1] = sibling.keys[i];

		// Moving the child pointers one step behind
		if (!sibling.leaf) {
			for (int i = 1; i <= sibling.n; ++i)
				sibling.c[i - 1] = sibling.c[i];
		}

		// Increasing and decreasing the key count of c[idx] and c[idx+1]
		// respectively
		child.n += 1;
		sibling.n -= 1;

		return;
	}

	// A function to merge c[idx] with c[idx+1]
	// c[idx+1] is freed after merging
	void merge(int idx) {
		BTreeNode child = c[idx];
		BTreeNode sibling = c[idx + 1];

		// Pulling a key from the current node and inserting it into (t-1)th
		// position of c[idx]
		child.keys[t - 1] = keys[idx];

		// Copying the keys from c[idx+1] to c[idx] at the end
		for (int i = 0; i < sibling.n; ++i)
			child.keys[i + t] = sibling.keys[i];

		// Copying the child pointers from c[idx+1] to c[idx]
		if (!child.leaf) {
			for (int i = 0; i <= sibling.n; ++i)
				child.c[i + t] = sibling.c[i];
		}

		// Moving all keys after idx in the current node one step before -
		// to fill the gap created by moving keys[idx] to c[idx]
		for (int i = idx + 1; i < n; ++i)
			keys[i - 1] = keys[i];

		// Moving the child pointers after (idx+1) in the current node one
		// step before
		for (int i = idx + 2; i <= n; ++i)
			c[i - 1] = c[i];

		// Updating the key count of child and the current node
		child.n += sibling.n + 1;
		n--;
	}
}