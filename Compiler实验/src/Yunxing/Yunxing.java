package Yunxing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.Vector;

import YuFaFenXi.YufaCompiler;
import YuFaFenXi.Instruct;
import YuFaFenXi.OPR;

public class Yunxing {
	private Instruct ireg;//指令寄存器
	private int pc;//程序计数器
	private int top;//栈顶寄存器
	private int base;//基址寄存器
	private int[] stack;//数据栈
	private Vector<Instruct> Code;
	private Instruct[] Code1;
	
	/**
	 *  构造函数，完成初始化
	*/
	public Yunxing(String name)
	{ 
		YufaCompiler compiler=new YufaCompiler(name);
		this.Code=compiler.Code;//指向语法分析得到的指令代码
		this.Code1 = compiler.Code1;
		ireg=null;
		pc=0;
		top=-1;
		base=0;
		stack=new int[1000000];
		going();
	}
	
	/**
	 * 循环解释目标代码
	 * */
	private void going(){
		while(pc<Code.size())
		{
			OneInstruct();
		}
	}
	
	/**
	 * 根据指令的功能码，解释一条指令
	 * */
	private void OneInstruct(){
		//ireg=Code.elementAt(pc);//取指
		ireg = Code1[pc];
		pc++;// pc+1
		int tmp;
		switch(ireg.instruct)
		{//根据指令寄存器中一条指令的功能码解释指令
		case LIT:
			stack[++top]=ireg.addr;//常量放栈顶
			printStack();
			break;
		case LOD:
			tmp=stack[ireg.addr];//变量放栈顶
			stack[++top]=tmp;
			printStack();
			break;
		case STO:
			tmp=stack[top];//栈顶内容存放到变量中
			stack[ireg.addr]=tmp;//存到变量地址中
			printStack();
			break;
		case CAL:
			stack[top+1]=pc;//返回地址
			stack[top+2]=base;//动态链
			stack[top+3]=stack[base+2]+ireg.abslevel;//静态链，被调用的过程的静态外层是调用层的静态外层+调用层差
			base=top+1;//记录被调用过程的基地址
			pc=ireg.addr;
			printStack();
			break;
		case INT:
			top=top+ireg.addr;//分配过程数据空间
			printStack();
			break;
		case JMP:
			pc=ireg.addr;
			printStack();
			break;
		case JPC:
			if(stack[top]==0)//判断条件，条件非真
				pc=ireg.addr;
			printStack();
			break;
		case OPR:// 关系和算术运算
			switch(ireg.addr)
			{
			case OPR.ADD:
				tmp=stack[top-1]+stack[top];//加法操作，次栈顶加上栈顶，结果放次栈顶
				stack[--top]=tmp;
				printStack();
				break;
			case OPR.SUB:
				tmp=stack[top-1]-stack[top];//减操作，次栈顶减去栈顶，结果放次栈顶
				stack[--top]=tmp;
				printStack();
				break;
			case OPR.DIV:
				tmp=stack[top-1]/stack[top];//除操作，次栈顶除以栈顶，结果放次栈顶
				stack[--top]=tmp;
				printStack();
				break;
			case OPR.MINUS:
				tmp=-stack[top];//取负操作，栈顶取负
				stack[top]=tmp;
				printStack();
				break;
			case OPR.MUL:
				tmp=stack[top-1]*stack[top];//乘操作，次栈顶乘以栈顶，结果放次栈顶
				stack[--top]=tmp;
				printStack();
				break;
			case OPR.EQ:
				tmp=stack[top-1]-stack[top];//关系等操作
				if(tmp==0)//相等，栈顶放1
				   stack[--top]=1;
				else//不相等，栈顶放0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.GE:
				tmp=stack[top-1]-stack[top];//关系大于等于操作
				if(tmp>=0)//大于等于，栈顶放1
				   stack[--top]=1;
				else//小于，栈顶放0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.GT://关系大于操作
				tmp=stack[top-1]-stack[top];
				if(tmp>0)//大于，栈顶放1
				   stack[--top]=1;
				else//小于等于，栈顶放0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.LE://关系小于等于操作
				tmp=stack[top-1]-stack[top];
				if(tmp<=0)//小于等于，栈顶放1
				   stack[--top]=1;
				else//不小于等于，栈顶放0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.LT://关系小于操作
				tmp=stack[top-1]-stack[top];
				if(tmp<0)//小于，栈顶放1
				   stack[--top]=1;
				else//不小于，栈顶放0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.WRITE://写操作
				writeMem(top);//将参数的内容打印出来
				printStack();
				break;
			case OPR.READ://读操作
				readMem();//输入数据，并将所输入的数据存入参数地址
				printStack();
				break;
			case OPR.UE://关系不等于操作
				tmp=stack[top-1]-stack[top];
				if(tmp!=0)//不等于，栈顶放1
				   stack[--top]=1;
				else//等于栈顶放0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.ODD:
				tmp = stack[top]%2;
				stack[top] =tmp;
				break;
			case 0://退出数据栈，退出子程序
				top=base-1;
				pc=stack[base];
				base=stack[base+1];//静态链的地址
				printStack();
				if(top==-1)System.exit(0);//主程序结束
				break;
			}
		}
	}
	
	/** 读语句
	 * 
	 * 从键盘输入得到一个常数，放到运行栈的栈顶
	 * */
	private void readMem() 
	{
		try{
			System.out.println("请输入读语句读到的内容：");
			 BufferedReader strin=new BufferedReader(new InputStreamReader(System.in));
			 int a=Integer.parseInt(strin.readLine());
			 stack[++top]=a;
		}
		catch(Exception e)
		{}
		
	}
	/** 写语句
	 * 
	 * */
    private void writeMem(int addr)
    {
    	System.out.println("输出写语句写的内容是："+stack[addr]);	
    }
    
	private int getBase(int nowBp, int lev) {
		int oldBp = nowBp;
		while (lev > 0) {
			oldBp = stack[oldBp + 1];
			lev--;
		}
		return oldBp;
	}
    /**
     * 打印各寄存器和栈顶内容
     * */
    private void printStack()
    {
    	/*
    	System.out.println();
    	System.out.println("当前top值："+top);//栈顶寄存器
    	System.out.println("当前base值："+base);//基址寄存器
    	System.out.println("当前pc值："+pc);//程序计数器
    	System.out.println("当前指令内容：");
    	System.out.println(ireg.instruct+"\t"+ireg.abslevel+"\t"+ireg.addr);//打印指令寄存器中的指令
    	System.out.println("当前栈内的内容是：");
    	for(int i=top;i>=0;i--)
    	{
    		System.out.println(stack[i]);
    	}*/
    }
}
