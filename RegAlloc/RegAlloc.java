package RegAlloc;

public class RegAlloc implements Temp.TempMap{
	private Assem.InstrList instrs;
	private Color color;

	public String tempMap(Temp.Temp t)
	{
		return color.tempMap(t);
	}
	public RegAlloc(Frame.Frame f, Assem.InstrList instrs) 
	{
		this.instrs = instrs;
		FlowGraph.FlowGraph flowGraph = new FlowGraph.AssemFlowGraph(instrs);
		//根据汇编指令生成流图
		InterferenceGraph interGraph=new Liveness(flowGraph);
		//进行活性分析并生成冲突图
		color = new Color(interGraph, f, f.registers());
		//用着色法分配寄存器
	}
}
