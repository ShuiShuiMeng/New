package RegAlloc;

import Graph.GraphNodeInfo;
import Graph.Node;

import java.util.*;

public class Liveness extends InterferenceGraph{
	private FlowGraph.FlowGraph flowGraph = null;
	private Hashtable<Graph.Node, Graph.GraphNodeInfo> node2nodeInfo = new Hashtable<Graph.Node, Graph.GraphNodeInfo>();
	private Hashtable<Graph.Node, Temp.TempList> liveMap = new Hashtable<Graph.Node, Temp.TempList>();
	private Hashtable<Temp.Temp, Graph.Node> temp2node = new Hashtable<Temp.Temp, Graph.Node>();
	private Hashtable<Graph.Node, Temp.Temp> node2temp = new Hashtable<Graph.Node, Temp.Temp>();
	
	public MoveList moves()
	{
		return null;
	}	
	public Liveness(FlowGraph.FlowGraph f)
	{
		this.flowGraph = f;
		initNodeInfo();  //初始化
		calcLiveness();	 //计算活性变量
		buildGraph();	//生成干扰图
	}
	private void initNodeInfo()
	{
		for(Graph.NodeList nodes = flowGraph.nodes(); nodes != null; nodes = nodes.tail)
		{
			GraphNodeInfo ni = new Graph.GraphNodeInfo(nodes.head);
			node2nodeInfo.put(nodes.head, ni);
		}
	}
	private void calcLiveness()
	{
		boolean flag = false;		
		while (!flag)
		{
			flag = true;
			for(Graph.NodeList nodes = flowGraph.nodes(); nodes != null; nodes = nodes.tail)
			{ 
				GraphNodeInfo ni = node2nodeInfo.get(nodes.head);
				Set<Temp.Temp> inx = new HashSet<Temp.Temp>();
				inx.addAll(ni.out);
				inx.removeAll(ni.def);
				inx.addAll(ni.use);
				if (!inx.equals(ni.in)) flag = false; 
				node2nodeInfo.get(nodes.head).in = inx; 
				Set<Temp.Temp> outx = new HashSet<Temp.Temp>();
				for (Graph.NodeList succ = nodes.head.succ(); succ != null; succ = succ.tail)
				{
					GraphNodeInfo i = (GraphNodeInfo) node2nodeInfo.get(succ.head);
					outx.addAll(i.in);
				}
				if (!outx.equals(ni.out)) flag = false;
				node2nodeInfo.get(nodes.head).out = outx; 
			}
		}
		
		for (Graph.NodeList nodes = flowGraph.nodes(); nodes != null; nodes = nodes.tail) 
		{
			Temp.TempList tl = null;	
			for (Iterator i = ((GraphNodeInfo)node2nodeInfo.get(nodes.head)).out.iterator(); i.hasNext(); ) 
				tl = new Temp.TempList((Temp.Temp) i.next(), tl);
			if (tl != null)	liveMap.put(nodes.head, tl);
		}
	}
	private void buildGraph()
	{
		Set temps = new HashSet(); 
		for (Graph.NodeList node = flowGraph.nodes(); node != null; node = node.tail)
		{
			for (Temp.TempList t = flowGraph.use(node.head); t != null; t = t.tail)
				temps.add(t.head);
			for (Temp.TempList t = flowGraph.def(node.head); t != null; t = t.tail)
				temps.add(t.head);
		}

		Iterator i = temps.iterator();
		while (i.hasNext()) add(newNode(), (Temp.Temp) i.next());
		for(Graph.NodeList nodes = flowGraph.nodes(); nodes != null; nodes = nodes.tail)
			for (Temp.TempList t = flowGraph.def(nodes.head); t != null; t = t.tail)
				for (Temp.TempList t1 = (Temp.TempList) liveMap.get(nodes.head); t1 != null; t1 = t1.tail)
				{
					if (t.head != t1.head && !flowGraph.isMove(nodes.head))
					{
						addEdge(tnode(t.head), tnode(t1.head));
						addEdge(tnode(t1.head), tnode(t.head));
					}
					if (t.head != t1.head && flowGraph.isMove(nodes.head) && flowGraph.use(nodes.head).head != t1.head)
					{
						addEdge(tnode(t.head), tnode(t1.head));
						addEdge(tnode(t1.head), tnode(t.head));
					}
				}
	}
	void add(Graph.Node node, Temp.Temp temp)
	{
		temp2node.put(temp, node);
		node2temp.put(node, temp);
	}
	public Node tnode(Temp.Temp temp)
	{
		return temp2node.get(temp);
	}
	public Temp.Temp gtemp(Node node)
	{
		return node2temp.get(node);
	}
}

