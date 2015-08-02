import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public class MagicSquares {
	
	private final GenerateMagicSquares magic_squares;
	private List<MagicTree> trees;
	private final Permutations sum_permutations_list;
	private List<Matrix> result;
	
	public MagicSquares(GenerateMagicSquares magicSquares, Permutations sum_permutations_list) {
		this.magic_squares = magicSquares;
		this.sum_permutations_list = sum_permutations_list;
		this.result = new ArrayList<Matrix>();

		this.trees = new ArrayList<MagicTree>();
		for (Entry<Integer, List<int[]>> e: sum_permutations_list.set_map.entrySet())
			this.trees.add(new MagicTree(e.getKey().intValue(), e.getValue()));
	}
	
	public class NodeBuilderAction extends RecursiveAction {

		private static final long serialVersionUID = 7218910311926378380L;
		
		private List<MagicTree> nodes;
		private int from;
		private int to;
		
		public NodeBuilderAction(List<MagicTree> nodes) {
			this.nodes = nodes;
		}
		
		public NodeBuilderAction(List<MagicTree> nodes, int from, int to) {
			this.nodes = nodes;
			this.from = from;
			this.to = to;
		}
		
		public void compute() {
			if ((to - from) <= 22) {
				for (int i = from; i < to; i++) {
					result.addAll(nodes.get(i).build());
				}
			} else {
				
				int half = (from + to) / 2;
				
				NodeBuilderAction worker1 = new NodeBuilderAction(nodes, from, half);
				NodeBuilderAction worker2 = new NodeBuilderAction(nodes, from + half, to);
				
				worker1.fork();
				worker2.compute();
				worker1.join();
			}
			//System.out.println("Task found "+result.size()+" magic matrices");
		}
	}
	
	public void build_tree() {
		int processors = Runtime.getRuntime().availableProcessors();
		
		NodeBuilderAction task = new NodeBuilderAction(trees,0,trees.size());
		ForkJoinPool pool = new ForkJoinPool(processors);
		pool.invoke(task);
		
		Map<List<Integer>, List<Matrix>> iso_map = new HashMap<List<Integer>, List<Matrix>>();
		for (Matrix m: this.result) {
			List<Integer> id = new ArrayList<Integer>();
			for (int i: m.equivalent_data)
				id.add(i);
			
			if (!iso_map.containsKey(id))
				iso_map.put(id, new LinkedList<Matrix>());
			iso_map.get(id).add(m);
		}
		
		String s = "";
		Iterator<Entry<List<Integer>, List<Matrix>>> iter = iso_map.entrySet().iterator();
		for (int i = 1; iter.hasNext(); i++) {
			Entry<List<Integer>, List<Matrix>> e = iter.next();
			s += i + ":\n";
			for (Matrix m: e.getValue())
				s += Arrays.toString(m.data) + "\n";
			s += "\n";
		}
		System.out.println(s);
				
		
		System.out.println("Computed Result: " + this.result.size());
	}
	
	public class MagicTree {
		int key;
		List<MagicTreeNode> children;
		
		public MagicTree(int key, List<int[]> diagonals) {
			this.key = key;
			this.children = new ArrayList<MagicTreeNode>();
			for (int[] l: diagonals)
				this.children.add(new MagicTreeNode(l));
		}
		
		public List<Matrix> build() {
			List<Matrix> r = new ArrayList<Matrix>();
			for (MagicTreeNode n: this.children)
				r.addAll(n.build());
			return r;
		}
	}
	
	public enum ChildType {
		DIAGONAL, ROW, COLUMN
	}
	
	public class MagicTreeNode {
		public final int[] data;
		public ArrayList<MagicTreeNode> children = new ArrayList<MagicTreeNode>();
		public final ChildType type; // 0 for root, 1 for main diagonal, 2 for row, 3 for column.
		public final int index;
		public final MagicTreeNode parent;
		public final int elements;
		
		public MagicTreeNode(int[] data) {
			this.type = ChildType.DIAGONAL;
			this.data = data;
			this.index = 0;
			this.parent = null;
			int elements = 0;
			for (int i: this.data)
				elements |= 1 << i;
			this.elements = elements;
		}
		
		public MagicTreeNode(int[] data, ChildType type, int index, MagicTreeNode parent) {
			this.data = data;
			this.type = type;
			this.index = index;
			this.parent = parent;
			int elements = this.parent.elements;
			for (int i: this.data)
				elements |= 1 << i;
			this.elements = elements;
		}
		
		public MagicTreeNode add_child(int[] data) {
			ChildType child_type;
			int child_index;
			switch (this.type) {
				case DIAGONAL: 
					child_type = ChildType.ROW;
					child_index = 0;
				break;
				case ROW: 
					child_type = ChildType.COLUMN;
					child_index = this.index;
				break;
				case COLUMN: 
					child_type = ChildType.ROW;
					child_index = this.index+1;
				break;
				default: 
					child_type = null;
					child_index = -1;
			}
			MagicTreeNode child = new MagicTreeNode(data, child_type, child_index, this);
			this.children.add(child);
			return child;
		}
		
		public int[] get_main_diagonal() {
			MagicTreeNode current_node = this;
			while (current_node.data != null) {
				if (current_node.type == ChildType.DIAGONAL)
					return current_node.data;
				current_node = current_node.parent;
			}
			return null;
		}
		
		public int[] get_row(int m) {
			int[] r = new int[this.index+2];
			MagicTreeNode current_node = this;
			while (current_node != null) {
				if (current_node.type == ChildType.COLUMN) 
					r[current_node.index] = current_node.data[m];
				else if (current_node.type == ChildType.DIAGONAL)
					r[m] = current_node.data[m];
				current_node = current_node.parent;
			}
			return r;
		}
		
		public int[] get_column(int n) {
			int[] r = new int[this.index+1];
			MagicTreeNode current_node = this;
			while (current_node != null) {
				if (current_node.type == ChildType.ROW)
					r[current_node.index] = current_node.data[n];
				current_node = current_node.parent;
			}
			return r;
		}
	
		public int get_elements() {
			return this.elements;
		}
		
		public Matrix to_matrix() {
			int[][] matrix_data = new int[MagicSquares.this.magic_squares.order][MagicSquares.this.magic_squares.order];
			MagicTreeNode current_node = this;
			while (current_node != null) {
				if (current_node.type == ChildType.ROW) {
					matrix_data[current_node.index] = current_node.data;
				}
				current_node = current_node.parent;
			}
			matrix_data[MagicSquares.this.magic_squares.order-1] = this.get_row(MagicSquares.this.magic_squares.order-1);
			return new Matrix(magic_squares, matrix_data);
		}
		
		public List<Matrix> build() {
			List<Matrix> r = new ArrayList<Matrix>();
			
			int forbidden_elements = this.get_elements();
			
			int[] child_begin = new int[] {};
			if (this.type == ChildType.DIAGONAL) {
				child_begin = new int[] {this.data[0]};
			} else if (this.type == ChildType.ROW) {
				child_begin = this.get_column(this.index);
			} else {
				if (MagicSquares.this.magic_squares.order == 1 || this.index == MagicSquares.this.magic_squares.order-2) {
					// this is a potentially magic square
					Matrix matrix = this.to_matrix();
					if (matrix.is_magic_lazy()) {
						r.add(matrix);
						return r;
					}
				} else {
					child_begin = this.get_row(this.index+1);
				}
			}
			
			for (int i: child_begin)
				forbidden_elements ^= 1 << i;
				
			List<int[]> child_possibilities = sum_permutations_list.query(child_begin, forbidden_elements);
			for (int[] c: child_possibilities) {
				MagicTreeNode child = this.add_child(c);
				r.addAll(child.build());
				this.children.remove(child);
			}
			
			return r;
		}
		
	}
}
