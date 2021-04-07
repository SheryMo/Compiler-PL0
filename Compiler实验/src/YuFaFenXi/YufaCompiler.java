package YuFaFenXi;

import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import WordsCompiler.SYM;
import WordsCompiler.WordsCompiler;

/**
 * 语法分析
 * */
public class YufaCompiler {
	private Vector<SYM> mySYM;
	private Vector<String> myID;
	private Vector<String> myNUM;
	private Iterator<SYM> SYMiterator;// 词法分析类型的遍历器
	private Iterator<String> IDiterator;// 标示符
	private Iterator<String> NUMiterator;// 常量
	private Vector<Vector> Table;// 符号说明表数组
	public Vector<Instruct> Code;// 中间代码数组
	public Instruct[] Code1;
	private int index;//Code 的索引
	private int myIndex;// 记录主程序入口
	private int offset;// 记录一开始空出的前两条指令的偏移
	private SYM current=null;// 记录当前单词的种别码
	private String id=null;
	private int num=0;
	private int tx=0;// 记录Table过程说明表的索引
	
	/** 构造函数
	 * 
	 * @ 在完成对给定文件的词法分析的基础上，实现语法分析
	 * @ 通过调用Compiler()完成语法分析
	 * */
	public YufaCompiler(String filename)
	{
		WordsCompiler te=new WordsCompiler(filename);
		//初始化需要使用的词法分析器得到的表
		this.myID=te.getMyID();
		this.myNUM=te.getMyValue();
		this.mySYM=te.getMySYM();
		//得到遍历器
		this.SYMiterator=this.mySYM.iterator();
		this.IDiterator=this.myID.iterator();
		this.NUMiterator=this.myNUM.iterator();
		//初始化符号说明表Table
		Table=new Vector<Vector>();
		//初始化中间代码Code
		Code=new Vector<Instruct>();
		Code1 = new Instruct[10000];
		index=0;
		offset=1;
		Compiler();//调用语法分析器
	}
	
	/** 语法分析器
	 * 
	 * @ 调用Block函数进行递归下降分析方法完成语法分析，并打印符号表和中间代码的内容
	 * */
	private void Compiler(){
		current=getNextSYM();
		Block(0);
		Code.add(0,new Instruct(FunctionCode.JMP,0,myIndex));// 调用Vector的库函数add实现在中间代码表中添加一个项目
		Code1[0] = new Instruct(FunctionCode.JMP,0,myIndex);
		System.out.println("语法分析结束！");
		System.out.println();
		System.out.println("说明表："+Table.size());
		System.out.println("name\tkind\tlevel\taddress\ttx");// 打印说明符号表的表头
        for(int j=0;j<Table.size();j++)
        {
	        Vector<TableItem> table=Table.elementAt(j);
	        for(int i=0;i<table.size();i++)
			{	
	        	System.out.print(table.elementAt(i).name+"\t"+table.elementAt(i).kind+"\t"+table.elementAt(i).level+"\t"+table.elementAt(i).address+"\t"+table.elementAt(i).tx+"\n");	
			}	
        }	
        System.out.println();
        System.out.println("中间代码：");
        System.out.println("序号\t功能码\t层次差\t位移量");
        for(int i=0;i<Code.size();i++)
        {
        	Instruct instruct=Code1[i];
        	System.out.println(i+"\t"+instruct.instruct+"\t"+instruct.abslevel+"\t"+instruct.addr);
        }
	 }  
	
	/** 处理过程
	 * 
	 * 按说明类型分为3类：常量、变量、过程（包括过程首部和过程语句体）
     * 分程序,构造过程说明表
	 * */
	private void Block(int level) 
	{
		Vector<TableItem> myTable=new Vector<TableItem>();// 声明一个符号表类型的对象数组
		Table.add(myTable);// 将新声明的一个符号说明添加到符号表中
		int addr=3;// 偏移量初始化为3
		int count=0;// 记录过程中局部变量的个数
		int currenttx=tx;// 记录当前过程所在的过程说明表所在Table中的索引
		
	    // 识别说明部分
		if(current!=null)
		{			
			if(current.equals(SYM._CONST))
			{// 常量说明
				Const(myTable,level);// 调用常量说明的处理
				if((current=getNextSYM())==null)
					return; 
			}
			if(current.equals(SYM._VAR))
			{// 变量说明
				count=Var(myTable,level,addr);// 处理变量说明
				if((current=getNextSYM())==null)
					return;
			}
			if(current.equals(SYM._procedure))
			{//过程说明,顺便将本层说明表插入到大表中
				Advanced();
				pro(currenttx,level);
			}
		}
		if(level==0)// 主程序层
		   myIndex=index+offset;
		GEN(FunctionCode.INT,0,3+count);//局部变量的个数+3
		Sentence(level,currenttx);// 语句
		//System.out.println("end procedure "+level+"\t"+current);
		GEN(FunctionCode.OPR,0,0);//退出数据域
	}
	
	/** 处理过程语句的主体
	 * 
	 * @ 识别合法语句，return 之后，current放的是下一个部分的第一个SYM
	 * */
	private void Sentence(int level,int currenttx) 
	{			
		if(current!=null)
		{// current 存放单词的种别码
			//System.out.println("what sentence "+level+" "+current);
			if(current.equals(SYM._begin))// begin-end复合语句
			{  
				//System.out.println("begin sentence "+level+" "+current);
			    Advanced();
			    while(current.equals(SYM._FH))
			    	Advanced();
			    if(current.equals(SYM._end))
			    {
			    	Advanced();
			    	return;
			    }
				Sentence(level,currenttx);// 递归处理过程Procedure的语句主体
				while(current!=null)
				{
					if(current.equals(SYM._FH))//current为";",说明后面还有语句
				    {
						Advanced();
						while(current.equals(SYM._FH))
							Advanced();
						if(current.equals(SYM._end))
						{
							Advanced();
							return;
						}
						else
						{
							Sentence(level,currenttx);
						}
						
					}
					else if(current.equals(SYM._end))//current为end，表示复合语句结束
					{
					   Advanced();
					   //System.out.println("begin sentence end at "+level+" "+current);
					   return;//正确结束唯一出口
					}
					else//出现其他字符，则说明语句错误
					{
						System.out.println("复合语句出错，缺少end");
						err2();
					}
				}
				System.out.println("复合语句出错，缺少end");
				err2();
			}
			else if(current.equals(SYM._ID))//赋值语句,第一个单词为变量标识符
			{
				//System.out.println("assignment sentence"+level+current);
				//查表
				id=getNextID();// 正常情况下，下一单词的种别码为_MD(：=)
				TableItem currentItem=getTableItem(id,level,currenttx);
				if(currentItem==null)//查表，查看标识符是否定义
				{
					System.out.println("赋值语句出错！使用尚未说明的标示符！！");					
					System.exit(0);
				}
				if(currentItem.kind==KIND.CON)// 常数，类型检查
				{
					System.out.println("赋值语句出错！赋给一个常量！！");					
					System.exit(0);
				}
				Advanced();
				if(current!=null && current.equals(SYM._MD))// 赋值号_MD(:=)
				{
					Advanced();
					Expression(level,currenttx); // 表达式
					GEN(FunctionCode.STO,level-currentItem.level,currentItem.address);//sto abslevel,id 
					//System.out.println("assignment sentence end at "+level+" "+current);
					return;//唯一正确结束出口
				}
				System.out.println("赋值语句出错！！");
				err2();
				
			}
			else if(current.equals(SYM._if))//条件语句
			{
				//System.out.println("if sentence "+level+" "+current);
				Condition(level,currenttx);//条件	
				
				//System.out.println("if jpc index "+index);
				index++;
				int hereOutif=index;//jpc语句占用的位置
				if(current!=null && current.equals(SYM._then))//then
				{
					Advanced();
					Sentence(level,currenttx);//语句
					
					Code.add(hereOutif, new Instruct(FunctionCode.JPC,0,index+offset));//回填then语句之后的代码地址
					Code1[hereOutif] =  new Instruct(FunctionCode.JPC,0,index+offset);
					//System.out.println("if sentence end at "+level+"\t"+current+"\t"+index);
					return;//正确结束
				}
				else
				{
					System.out.println("条件语句缺少then！！");
					err2();
				}
			}
			else if(current.equals(SYM._while))//当循环语句
			{
				//System.out.println("while sentence "+level+" "+current);
				int hereInwhile=index+1;//记录while循环开始的地方,无条件跳转到的地方
				Condition(level,currenttx);//条件
				
				index++;//占用一个Code位置	
				int hereOutwhile=index;//jpc语句插入的地方
				if(current!=null&&current.equals(SYM._do))//do
				{
					Advanced();
					Sentence(level,currenttx);//语句
					GEN(FunctionCode.JMP,0,hereInwhile+offset);
					Code.add(hereOutwhile, new Instruct(FunctionCode.JPC,0,index+offset));//回填then语句之后的代码地址
					Code1[hereOutwhile] = new Instruct(FunctionCode.JPC,0,index+offset);
					//System.out.println("while sentence end at "+level+" "+current);
					return;//正确结束
				}
				else
				{
					System.out.println("当循环缺少do！！");
					err2();
				}
			}
			else if(current.equals(SYM._write))//写语句
			{
				Advanced();
				//System.out.println("write sentence "+level+"\t"+current);
				wrd(1,level,currenttx);
				//System.out.println("write sentence end at "+level+"\t"+current);
				return;
			}
			else if(current.equals(SYM._read))//读语句
			{
				//System.out.println("read sentence "+level+"\t"+current);
				Advanced();
				wrd(0,level,currenttx);
				//System.out.println("read sentence end at "+level+"\t"+current);
				return;
			}
			else if(current.equals(SYM._call))//过程调用语句
			{
				//System.out.println("call sentence "+level+" "+current);
				if((current=getNextSYM())!=null&&current.equals(SYM._ID))//标示符
				{//查表
					id=getNextID();
					TableItem currentItem=getTableItem(id,level,currenttx);
					if(currentItem==null)
					{
						System.out.println("过程调用语句出错！使用尚未说明的过程标示符！！"+id);					
						System.exit(0);
					}
					if(currentItem.kind!=KIND.PRO)//类型检查
					{
						System.out.println("过程调用语句出错！调用的标示符不是过程名！！"+id);					
						System.exit(0);
					}
					if(currentItem.level>level)
					{
						System.out.println("不允许调用此过程 "+id);					
						System.exit(0);
					}
					GEN(FunctionCode.CAL,level,currentItem.address);
					Advanced();
					//System.out.println("call sentence end at "+level+" "+current);
					return;//正确结束
					
				}
				
				else
				{
					System.out.println("过程调用语句出错！！");
					err2();
				}
			}

			else//不是合法的语句
			{System.out.println("非法语句！！");
			   err2();
			}
		}
		else//空语句（合法）
		{
			Advanced();
			return;
		}
	}
	
	/** 项的处理（乘法与除法也属于项）
	 * 
	 * return时current放的是后面部分
	 * */
	private void Item(int level,int currenttx)
	{
		//System.out.println("   item "+level+" "+current);
		Yinzi(level,currenttx);// 因子
		while((current=getNextSYM())!=null){// 如果还有，则必然是乘除运算符，因子
			if(current.equals(SYM._STAR)||current.equals(SYM._DIV))
			{
				int ii=0;
				if(current.equals(SYM._STAR))
					ii=OPR.MUL;
				else 
					ii=OPR.DIV;
				Advanced();
				Yinzi(level,currenttx);// 正确结束
				GEN(FunctionCode.OPR,0,ii);	// 进行ii运算			    
			}
			else return;
		}
		return;
	}
	
	/** 因子的处理
	 * 
	 * @ 因子包括：标识符、无符号整数、左右括号
	 * */
	private void Yinzi(int level,int currenttx)
	{
		//System.out.println("   yinzi "+level+" "+current);
		if(current!=null)
		{
			if(current.equals(SYM._ID))//标识符
			{
				//查表
				id=getNextID();
				TableItem currentItem=getTableItem(id,level,currenttx);
				if(currentItem==null)
				{
					System.out.println("表达式出错！使用尚未说明的标示符！！");					
					System.exit(0);
				}
				if(currentItem.kind==KIND.VAR)//类型检查
				    GEN(FunctionCode.LOD,level-currentItem.level,currentItem.address);//将变量放到运算栈顶
				else if(currentItem.kind==KIND.CON)//类型检查
					GEN(FunctionCode.LIT,0,currentItem.address);//将常数address放到运算栈顶
				 return;
			}			  
			if(current.equals(SYM._INT))//无符号整数
			{
				GEN(FunctionCode.LIT,0,getNextNUM());//将常量放到运算栈顶
				return;
			}		
			if(current.equals(SYM._LEFT))//左括号
			{
				Expression(level,currenttx);//表达式
				if(current!=null && current.equals(SYM._RIGHT))//右括号
					return;
				else
				{// 缺少右括号
					System.out.println("因子出错！！");
					err2();
				}
			}
			else
			{
				System.out.println("因子出错！！");
				err2();
			}	
		}
		else
		{// 符号尚未说明
			System.out.println("因子出错！！");
			err2();
		}	
		
	}
	
	/** 识别合法表达式
	 * 
	 * @ return时current放的是后面部分
	 * */
    private void Expression(int level,int currenttx) 
    {
    	//System.out.println("   expression "+level+" "+current);
    	if(current.equals(SYM._PLUS))//+
    	{
    		Advanced();
    		Item(level,currenttx);//项
    	}
    	else if(current.equals(SYM._SUB))//-
    	{
    		Advanced();
    		Item(level,currenttx);//项
    		GEN(FunctionCode.OPR,0,OPR.MINUS);//取负
    	}
    	else 
    		Item(level,currenttx);//项
    	while(current!=null)//如果还有，则必须是加减运算符，项
		{
			if(current.equals(SYM._SUB)||current.equals(SYM._PLUS))//+或者-
    		{
				int ii=0;
				if(current.equals(SYM._SUB))ii=OPR.SUB;
				else  ii=OPR.ADD;
				Advanced();
    			Item(level,currenttx);//项
    			GEN(FunctionCode.OPR,0,ii);//进行ii运算
    		}
    		else
    		{
    			return;//后面的部分不属于该表达式
    		}
		} 
    	return;
    }
    
    /** 条件语句的处理
     * 
     * return时current放的是后面部分
     * */
	private void Condition(int level,int currenttx)
	{
		//System.out.println("   condition "+level+" "+current);
		if((current=getNextSYM())!=null && current.equals(SYM._odd))
		{// 一元运算：判断奇偶，odd形式的表达式
			Advanced();
			Expression(level,currenttx);
			GEN(FunctionCode.OPR,0,OPR.ODD);
			//Condition(level,currenttx);
			return;//正确结束出口一
		}
		else//另一种形式的表达式
		{  
			Expression(level,currenttx);//表达式
			//System.out.println("   condition current SYM: "+current);
			if(current!=null)
			{
				if(current.equals(SYM._ASSIGN)||current.equals(SYM._LESS)||//关系运算符
						current.equals(SYM._MORE)||current.equals(SYM._LESSEQ)
			            ||current.equals(SYM._MOREEQ)||current.equals(SYM._JH))
				{//形成目标代码
					int ii=0;
					if(current.equals(SYM._ASSIGN))ii=OPR.EQ;//=
					else if(current.equals(SYM._LESS))ii=OPR.LT;//<
					else if(current.equals(SYM._MORE))ii=OPR.GT;//>
					else if(current.equals(SYM._LESSEQ))ii=OPR.LE;//<=
					else if(current.equals(SYM._MOREEQ))ii=OPR.GE;//>=	
					else if(current.equals(SYM._JH))ii=OPR.UE;//#,不等于	
					Advanced();
					Expression(level,currenttx);//表达式
					GEN(FunctionCode.OPR,0,ii);//进行ii运算
					return;//正确结束出口
				}
				else
				{
					System.out.println("条件出错11！！"); 
					err2();
				}									
			}
			else
			{
				System.out.println("条件出错22！！"); 
				err2();
			}
		}
	}
	
	/**辅助读写语句方法,index=0,读语句，index=1，写语句
	 * 
	 * ???????
	 * */
	private void wrd(int inagr,int level,int currenttx)
	{
		if(current!=null&&current.equals(SYM._LEFT))//左括号
		{
			if((current=getNextSYM())!=null&&current.equals(SYM._ID))//标示符
			{
				//查表
				id=getNextID();
				TableItem currentItem=getTableItem(id,level,currenttx);
				if(currentItem==null)
				{
					if(inagr==0)
						   System.out.println("读语句结构错误！！使用尚未说明的标示符！！");
						else
						   System.out.println("写语句错误！！！使用尚未说明的标示符！！");
					    err2();
				}
				if(inagr==0)
				{
					GEN(FunctionCode.OPR,0,OPR.READ);//read操作
					GEN(FunctionCode.STO,level-currentItem.level,currentItem.address);
				}
				else
				{
					GEN(FunctionCode.LOD,level-currentItem.level,currentItem.address);
					GEN(FunctionCode.OPR,0,OPR.WRITE);//write操作
				}
				
				while((current=getNextSYM())!=null&&current.equals(SYM._DH))//逗号，说明还有标示符
				{
					if((current=getNextSYM())!=null&&current.equals(SYM._ID))//标示符
					{//查表，并形成目标代码
						id=getNextID();
						if((currentItem=getTableItem(id,level,currenttx))==null)
						{
							if(inagr==0)
								   System.out.println("读语句结构错误！！使用尚未说明的标示符！！");
							else
								   System.out.println("写语句错误！！！使用尚未说明的标示符！！");
							err2();
						}	
						if(inagr == 0)
						{
							GEN(FunctionCode.OPR,0,OPR.READ);
							GEN(FunctionCode.STO,level-currentItem.level,currentItem.address);
						}
						else
						{
							GEN(FunctionCode.LOD,level-currentItem.level,currentItem.address);
							GEN(FunctionCode.OPR,0,OPR.WRITE);
						}
						if((current=getNextSYM())!=null&&current.equals(SYM._DH))//TODO  这里的中间代码没填上
						{

							continue;
						}
						else
						{
							break;
						}
						
					}
					else
					{
						if(inagr==0)
						   System.out.println("读语句结构错误！！");
						else
						   System.out.println("写语句错误！！！");
					    err2();
					}
				}
				//跳出循环，必须是右括号
				if(current.equals(SYM._RIGHT))
				{
				    Advanced();
					return;
				}
					
				else
				{
					if(index==0)
					   System.out.println("读语句结构错误！！");
					else
					   System.out.println("写语句错误！！！");
				    err2();
				}
			}
			else
			{
				if(index==0)
				   System.out.println("读语句结构错误！！");
				else
				   System.out.println("写语句错误！！！");
			    err2();
			}
		}
		else
		{
			if(index==0)
			   System.out.println("读语句结构错误！！");
			else
			   System.out.println("写语句错误！！！");
		    err2();
		}
	}
	
	/** 生成目标代码
	 * 
	 * @ 形参：功能码，层次差，偏移量
	 * @ 将产生的目标代码添加到Code表中，并维护Code表的索引index
	 * */
	private void GEN(FunctionCode instruct,int abslevel,int addr )
	{
		Code.add(new Instruct(instruct,abslevel,addr));
		index++;
		Code1[index] = new Instruct(instruct,abslevel,addr);
	}	
	
	/**
	 * 过程说明部分,return 之后，current放的是下一个部分的第一个SYM
	 * */
    private void pro(int currenttx,int level)
    {
    	prepro(currenttx,level);//过程首部，tx指向Table表的下标，即内层过程说明的入口地址
		Advanced();
		if(level+1>3)
		{
			System.out.println("程序层数大于3，退出");
			System.exit(0);
		}
		Block(level+1);//分程序，内层过程说明表的构造
		if(current!=null && current.equals(SYM._FH))
		{
			Advanced();
			while(current!=null && current.equals(SYM._procedure))//如果还有并列的过程说明
			{
				Advanced();
				pro(currenttx,level);
			}
		}
		else{
			System.out.println("过程说明错误！！缺少分号！！");
			System.exit(0);
		}
		
    	
    }
    
    /** 过程首部的处理
     * 
     *  tx指向Table表的下标，即内层过程说明的入口地址
     * */
	private void prepro(int currenttx,int level)
	{
		Vector<TableItem> myTable=Table.elementAt(currenttx);
		if(current!=null && current.equals(SYM._ID))//标识符
		{	
			if((current=getNextSYM())!=null && current.equals(SYM._FH))//分号
			{
				id=getNextID();
				if(id!=null)
				{
					KIND kind=KIND.PRO;
					TableItem item=new TableItem(id,kind,level,index+offset,currenttx);
					//System.out.println("insert proc\t"+id+"\t"+level);
					if(!legal(item,myTable,currenttx))
					{
						System.out.println("过程"+item.name+"重定义！！");
						err();
					}
					myTable.add(item);
					tx++;
					//myTable.removeAllElements();//构造下一层新表//这句没有预期的效果！因为Vector好像只是将指针存入内存
					
				}
				else
				{
					System.out.println("p1");
				    err();
				}
			}
			else
			{
				System.out.println("p2");
			    err();
			}
		}
		else
		{
			System.out.println("p3");
		    err();
		}
	}
	
	/**处理常量说明
	 * 
	 * */
	private void Const(Vector<TableItem>myTable, int level)
	{
		if((current=getNextSYM())!=null&&current.equals(SYM._ID))
			if((current=getNextSYM())!=null&&current.equals(SYM._ASSIGN))
				if((current=getNextSYM())!=null&&current.equals(SYM._INT))//标示符 等号 常量如（a=10）
					if((id=getNextID())!=null)//常量标示符
						if((num=getNextNUM())!=-1)
						{
							KIND kind=KIND.CON;
							TableItem item=new TableItem(id,kind,level,num,tx);
							if(!legal(item,myTable,tx))
							{
								System.out.println("常量"+item.name+"重定义！！");
								err();
							}
							myTable.add(item);
							//System.out.println("insert const\t"+id+"\t"+num);
						}
						else//句子不完整
						{System.out.println("c1");
						   err();}
					else//句子不完整
					{System.out.println("c2");
					   err();}
				else//可能是句子不完整，或者是句子错误
				{System.out.println("c3");
				   err();}
			else//可能是句子不完整，或者是句子错误
			{System.out.println("c4");
			   err();}
		else//可能是句子不完整，或者是句子错误
		{System.out.println("c5");
		   err();}
		while((current=getNextSYM())!=null&&current.equals(SYM._DH))//逗号，说明后面还有常量定义
		{
			if((current=getNextSYM())!=null&&current.equals(SYM._ID))
				if((current=getNextSYM())!=null&&current.equals(SYM._ASSIGN))
					if((current=getNextSYM())!=null&&current.equals(SYM._INT))//标示符 等号 常量如（a=10）
						if((id=getNextID())!=null)//常量标示符
							if((num=getNextNUM())!=-1)
							{
								KIND kind=KIND.CON;
								TableItem item=new TableItem(id,kind,level,num,tx);
								if(!legal(item,myTable,tx)){System.out.println("常量"+item.name+"重定义！！");err();}
								myTable.add(item);
								//System.out.println("insert const\t"+num);
							}
							else//句子不完整
							{System.out.println("c6");
							   err();}
							
						else//句子不完整
						{System.out.println("c7");
						   err();}
					else//可能是句子不完整，或者是句子错误
					{System.out.println("c8");
					   err();}
				else//可能是句子不完整，或者是句子错误
				{System.out.println("c9");
				   err();}
			else//可能是句子不完整，或者是句子错误
			{System.out.println("c10");
			   err();}
		}
		if(current!=null&&current.equals(SYM._FH));//若是分号，则说明常量定义完毕
		else//可能是句子不完整，或者是句子错误
		{
		   System.out.println("c11");
		   err();}
	}
	
	/** 变量说明的处理  
	 * @ 返回声明的变量数
	 * */
	private int Var(Vector<TableItem>myTable,int level,int addr)
	{
		int count=0;//记录变量的个数
		if((current=getNextSYM())!=null&&current.equals(SYM._ID))//标示符 
			if((id=getNextID())!=null)//变量标示符
			{
				count++;
				KIND kind=KIND.VAR;
				TableItem item=new TableItem(id,kind,level,addr++,tx);	
				if(!legal(item,myTable,tx))
				{
					System.out.println("变量"+item.name+"重定义！！");
					err();
				}
				myTable.add(item);
				//System.out.println("insert var\t"+id+"\t"+level);
			}
			else{System.out.println("4");
			   err();}
		else//可能是句子不完整，或者是句子错误
		{
			System.out.println("5");
		    err();
		}
		while((current=getNextSYM())!=null&&current.equals(SYM._DH))//逗号，说明后面还有常量定义
		{
			if((current=getNextSYM())!=null&&current.equals(SYM._ID))//标示符 
				if((id=getNextID())!=null)//变量标示符
				{
					count++;
					KIND kind=KIND.VAR;
					TableItem item=new TableItem(id,kind,level,addr++,tx);
					if(!legal(item,myTable,tx)){System.out.println("变量"+item.name+"重定义！！");err();}
					myTable.add(item);
					//System.out.println("insert var\t"+id+"\t"+level);
				}
				else//句子不完整
				{System.out.println("6");
				   err();}
			else//可能是句子不完整，或者是句子错误
			{System.out.println("7");
			   err();}
		}
		if(current!=null&&current.equals(SYM._FH));//若是分号，则说明常量定义完毕
		else//可能是句子不完整，或者是句子错误
		{System.out.println("8");
		   err();}
		return count;
	}
	
	/**获得种别码数组中的下一个
	 * 
	 * @ 有则返回下一种别码
	 * @ 没有则返回null
	 * */
	private SYM getNextSYM(){
		if(SYMiterator.hasNext())
		{
			SYM next=SYMiterator.next();
			return next;
		}
		return null;
	}
	
	/** 获得标识符数组中的下一个
	 * 
	 * @ 有则返回下一标识符
	 * @ 无则返回null
	 * */
	private String getNextID(){
		if(IDiterator.hasNext())
		{
			String next=IDiterator.next();
			return next;
		}
		return null;
	}
	
	/** 获得用户自定义数据的数组中的下一个
	 * 
	 * @ 有则返回下一数据
	 * @ 无则返回-1
	 * */
	private int getNextNUM(){
		if(NUMiterator.hasNext())
		{
			int next=String2int(NUMiterator.next());
			return next;
		}
		return -1;
	}
	
	/** 跳过当前一分析过的单词，分析下一个
	 * 
	 * @ 如果种别码数组(SYM)中还有下一个，则修改current为下一单词的种别码
	 * @ 否则置current为null
	 * */
	private void Advanced()
	{
		if(SYMiterator.hasNext())
		{
			current=SYMiterator.next();	
			return;
		}
		current=null;
	}
	
	/**将string类型的数转换为int的数
	 * 
	 * @ 输入：字符串
	 * @ 输出：转换后的int类型的数
	 * */
	private int String2int(String str)
	{
		int sum=0;
		for(int i=0;i<str.length();i++)
		{
			int a=str.charAt(i)-'0';
			sum=sum*10+a;
		}
		return sum;	
	}
	
	/**错误处理
	 * 
	 * @ 过程说明不合法
	 * */
	private void err()
	{
		System.out.println("编译出错！存在不合法的过程说明！！！");
		System.exit(0);
	}
	
	/**错误处理
	 * 
	 * @ 语句不合法
	 * */
	private void err2()
	{
		System.out.println("编译出错！存在不合法的语句！！！");
		System.exit(0);
	}
	
	/**得到符号表项目元素
	 * 
	 * @ 形参：符号的名字，层次，符号表指针
	 * @ 若找到，则返回所查询的符号声明<name,kind,level,address,tx>
	 * @ 若未找到，则返回null
	 * */
	private TableItem getTableItem(String name,int level,int tx)
	{
		Iterator<Vector> tt=Table.iterator();//总的符号说明表遍历器
		while(tt.hasNext())
		{
			Iterator<TableItem> table=tt.next().iterator();//小表遍历器，一个说明语句的解释
			while(table.hasNext()){
				TableItem item=table.next();
				if(item.name.equals(name))
				{
					if(item.level<level)//在该过程即其父过程中的所有声明的常量、变量、过程名都可见
					    return item;
					if(item.level==level)//层次相同
					{
						if(item.tx==tx)//并且在相同的过程体说明中
						   return item;
						else 
							continue;
					}
					else
						return null;//不可见的变量不算
				}
				  
			}
		}		
		return null;
	}
	
	/** 判断插入是否合法,此时的tx无用
	 * 
	 * @ 若待插入的符号已在符号表中，则表示此符号已声明过，返回false
	 * @ 否则，返回true，并完成插入
	 * */
    private Boolean legal(TableItem item,Vector<TableItem>myTable,int tx)
    {
    	TableItem item1=getTableItem(item.name,item.level,tx);
    	if(item1==null)
    	{// 所插入的符号尚未声明
    		Iterator<TableItem> tt=myTable.iterator();//小表遍历器
    		while(tt.hasNext()){
				TableItem iitem=tt.next();
				if(iitem.name.equals(item.name))
				{
					item1=iitem;
				}
    		}
    	}
    	if(item1!=null)
    	{
    		if(item1.kind.equals(item.kind))//如果在可见的范围内重定义(名字相同并且类型相同)
    			return false;
    		if(item1.kind.equals(KIND.VAR)&&item.kind.equals(KIND.CON))//如果在可见的范围内重定义(名字相同（常量与变量）)，则报错！！！
    		    return false;
    		if(item1.kind.equals(KIND.CON)&&item.kind.equals(KIND.VAR))//如果在可见的范围内重定义(名字相同（常量与变量）)，则报错！！！
    			return false;
    	}
    	return true;//过程名与常量或变量名相同，不算重定义
    }
}


