package YuFaFenXi;

/** 定义指令格式
 * 
 * @ 第一个参数：instruct 功能码
 * @ 第二个参数：abslevel 层次或常量的值
 * @ 第三个参数： addr 偏移量
 * */
public class Instruct {
	public FunctionCode instruct;// 功能码
	public int abslevel;// 层次或常量的值
	public int addr;// 偏移量
	
	/**	构造函数
	 * 	
	 * @ 三个参数，分别是：功能码，层次，偏移量
	 * */
	public Instruct(FunctionCode instruct,int abslevel,int addr)
	{
		this.instruct=instruct;
		this.abslevel=abslevel;
		this.addr=addr;
	}
}
