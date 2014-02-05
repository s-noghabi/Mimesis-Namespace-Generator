package namespaceGenerator;

import java.util.ArrayList;

/* 
 * These are entries in the namespace. They are either directories or files.
 */
public class HierarchicalNamespaceEntry extends NamespaceEntry {
	private HierarchicalNamespaceEntry parent = null;
	private boolean isDir = false;
	private ArrayList<HierarchicalNamespaceEntry> children = null;

	public HierarchicalNamespaceEntry(long creationStamp, long name,
			HierarchicalNamespaceEntry p, boolean d) {
		super(creationStamp, name);
		this.parent = p;
		this.isDir = d;
		if (this.isDir) {
			children = new ArrayList<HierarchicalNamespaceEntry>();
		}
	}

	public HierarchicalNamespaceEntry getParent() {
		return this.parent;
	}

	public boolean isDir() {
		return this.isDir;
	}

	public void addChild(HierarchicalNamespaceEntry child) {
		if (!this.isDir)
			throw new java.lang.UnsupportedOperationException(
					"Files cannot have children.");

		children.add(child);
	}

	public ArrayList<HierarchicalNamespaceEntry> getChildren() {
		return children;
	}

	public int getNumChildren() {
		if (!this.isDir)
			throw new UnsupportedOperationException(
					"Files do not have children.");
		return this.children.size();
	}
}
