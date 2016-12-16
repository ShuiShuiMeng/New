package FlowGraph;

import Graph.*;

public class AssemFlowGraph extends FlowGraph{
	private java.util.Hashtable<Node, Assem.Instr> node2instr = new java.util.Hashtable<Node, Assem.Instr>();
	private java.util.Hashtable<Temp.Label, Node> label2node = new java.util.Hashtable<Temp.Label, Node>();


	public AssemFlowGraph(Assem.InstrList instrs)
	{
				
		for (Assem.InstrList i = instrs; i != null; i = i.tail) 
		{
			Node node = newNode();
			node2instr.put(node, i.head);

			if (i.head instanceof Assem.LABEL)
			label2node.put(((Assem.LABEL) i.head).label, node);
		}
		
		for (NodeList node = nodes(); node != null; node = node.tail)
		{
			Assem.Targets next = instr(node.head).jumps(); 
			if (next == null) 
			{ 
				if (node.tail != null) 
					addEdge(node.head, node.tail.head);
			} 
			else 
			{
				for (Temp.LabelList l = next.labels; l != null; l = l.tail)
					addEdge(node.head, (Node) label2node.get(l.head));
			}
		}
	}

	public Assem.Instr instr(Node n)
	{	return (Assem.Instr)node2instr.get(n);	}
		
	public Temp.TempList def(Node node) 
	{	return instr(node).def();	}

	public Temp.TempList use(Node node)
	{	return instr(node).use();	}
		
	public boolean isMove(Node node) 
	{
		Assem.Instr instr = instr(node);
		return instr.assem.startsWith("move");
	}	
}
