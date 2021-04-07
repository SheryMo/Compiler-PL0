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
	private Instruct ireg;//ָ��Ĵ���
	private int pc;//���������
	private int top;//ջ���Ĵ���
	private int base;//��ַ�Ĵ���
	private int[] stack;//����ջ
	private Vector<Instruct> Code;
	private Instruct[] Code1;
	
	/**
	 *  ���캯������ɳ�ʼ��
	*/
	public Yunxing(String name)
	{ 
		YufaCompiler compiler=new YufaCompiler(name);
		this.Code=compiler.Code;//ָ���﷨�����õ���ָ�����
		this.Code1 = compiler.Code1;
		ireg=null;
		pc=0;
		top=-1;
		base=0;
		stack=new int[1000000];
		going();
	}
	
	/**
	 * ѭ������Ŀ�����
	 * */
	private void going(){
		while(pc<Code.size())
		{
			OneInstruct();
		}
	}
	
	/**
	 * ����ָ��Ĺ����룬����һ��ָ��
	 * */
	private void OneInstruct(){
		//ireg=Code.elementAt(pc);//ȡָ
		ireg = Code1[pc];
		pc++;// pc+1
		int tmp;
		switch(ireg.instruct)
		{//����ָ��Ĵ�����һ��ָ��Ĺ��������ָ��
		case LIT:
			stack[++top]=ireg.addr;//������ջ��
			printStack();
			break;
		case LOD:
			tmp=stack[ireg.addr];//������ջ��
			stack[++top]=tmp;
			printStack();
			break;
		case STO:
			tmp=stack[top];//ջ�����ݴ�ŵ�������
			stack[ireg.addr]=tmp;//�浽������ַ��
			printStack();
			break;
		case CAL:
			stack[top+1]=pc;//���ص�ַ
			stack[top+2]=base;//��̬��
			stack[top+3]=stack[base+2]+ireg.abslevel;//��̬���������õĹ��̵ľ�̬����ǵ��ò�ľ�̬���+���ò��
			base=top+1;//��¼�����ù��̵Ļ���ַ
			pc=ireg.addr;
			printStack();
			break;
		case INT:
			top=top+ireg.addr;//����������ݿռ�
			printStack();
			break;
		case JMP:
			pc=ireg.addr;
			printStack();
			break;
		case JPC:
			if(stack[top]==0)//�ж���������������
				pc=ireg.addr;
			printStack();
			break;
		case OPR:// ��ϵ����������
			switch(ireg.addr)
			{
			case OPR.ADD:
				tmp=stack[top-1]+stack[top];//�ӷ���������ջ������ջ��������Ŵ�ջ��
				stack[--top]=tmp;
				printStack();
				break;
			case OPR.SUB:
				tmp=stack[top-1]-stack[top];//����������ջ����ȥջ��������Ŵ�ջ��
				stack[--top]=tmp;
				printStack();
				break;
			case OPR.DIV:
				tmp=stack[top-1]/stack[top];//����������ջ������ջ��������Ŵ�ջ��
				stack[--top]=tmp;
				printStack();
				break;
			case OPR.MINUS:
				tmp=-stack[top];//ȡ��������ջ��ȡ��
				stack[top]=tmp;
				printStack();
				break;
			case OPR.MUL:
				tmp=stack[top-1]*stack[top];//�˲�������ջ������ջ��������Ŵ�ջ��
				stack[--top]=tmp;
				printStack();
				break;
			case OPR.EQ:
				tmp=stack[top-1]-stack[top];//��ϵ�Ȳ���
				if(tmp==0)//��ȣ�ջ����1
				   stack[--top]=1;
				else//����ȣ�ջ����0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.GE:
				tmp=stack[top-1]-stack[top];//��ϵ���ڵ��ڲ���
				if(tmp>=0)//���ڵ��ڣ�ջ����1
				   stack[--top]=1;
				else//С�ڣ�ջ����0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.GT://��ϵ���ڲ���
				tmp=stack[top-1]-stack[top];
				if(tmp>0)//���ڣ�ջ����1
				   stack[--top]=1;
				else//С�ڵ��ڣ�ջ����0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.LE://��ϵС�ڵ��ڲ���
				tmp=stack[top-1]-stack[top];
				if(tmp<=0)//С�ڵ��ڣ�ջ����1
				   stack[--top]=1;
				else//��С�ڵ��ڣ�ջ����0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.LT://��ϵС�ڲ���
				tmp=stack[top-1]-stack[top];
				if(tmp<0)//С�ڣ�ջ����1
				   stack[--top]=1;
				else//��С�ڣ�ջ����0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.WRITE://д����
				writeMem(top);//�����������ݴ�ӡ����
				printStack();
				break;
			case OPR.READ://������
				readMem();//�������ݣ���������������ݴ��������ַ
				printStack();
				break;
			case OPR.UE://��ϵ�����ڲ���
				tmp=stack[top-1]-stack[top];
				if(tmp!=0)//�����ڣ�ջ����1
				   stack[--top]=1;
				else//����ջ����0
				   stack[--top]=0;
				printStack();
				break;
			case OPR.ODD:
				tmp = stack[top]%2;
				stack[top] =tmp;
				break;
			case 0://�˳�����ջ���˳��ӳ���
				top=base-1;
				pc=stack[base];
				base=stack[base+1];//��̬���ĵ�ַ
				printStack();
				if(top==-1)System.exit(0);//���������
				break;
			}
		}
	}
	
	/** �����
	 * 
	 * �Ӽ�������õ�һ���������ŵ�����ջ��ջ��
	 * */
	private void readMem() 
	{
		try{
			System.out.println("������������������ݣ�");
			 BufferedReader strin=new BufferedReader(new InputStreamReader(System.in));
			 int a=Integer.parseInt(strin.readLine());
			 stack[++top]=a;
		}
		catch(Exception e)
		{}
		
	}
	/** д���
	 * 
	 * */
    private void writeMem(int addr)
    {
    	System.out.println("���д���д�������ǣ�"+stack[addr]);	
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
     * ��ӡ���Ĵ�����ջ������
     * */
    private void printStack()
    {
    	/*
    	System.out.println();
    	System.out.println("��ǰtopֵ��"+top);//ջ���Ĵ���
    	System.out.println("��ǰbaseֵ��"+base);//��ַ�Ĵ���
    	System.out.println("��ǰpcֵ��"+pc);//���������
    	System.out.println("��ǰָ�����ݣ�");
    	System.out.println(ireg.instruct+"\t"+ireg.abslevel+"\t"+ireg.addr);//��ӡָ��Ĵ����е�ָ��
    	System.out.println("��ǰջ�ڵ������ǣ�");
    	for(int i=top;i>=0;i--)
    	{
    		System.out.println(stack[i]);
    	}*/
    }
}
