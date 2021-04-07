package YuFaFenXi;

import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import WordsCompiler.SYM;
import WordsCompiler.WordsCompiler;

/**
 * �﷨����
 * */
public class YufaCompiler {
	private Vector<SYM> mySYM;
	private Vector<String> myID;
	private Vector<String> myNUM;
	private Iterator<SYM> SYMiterator;// �ʷ��������͵ı�����
	private Iterator<String> IDiterator;// ��ʾ��
	private Iterator<String> NUMiterator;// ����
	private Vector<Vector> Table;// ����˵��������
	public Vector<Instruct> Code;// �м��������
	public Instruct[] Code1;
	private int index;//Code ������
	private int myIndex;// ��¼���������
	private int offset;// ��¼һ��ʼ�ճ���ǰ����ָ���ƫ��
	private SYM current=null;// ��¼��ǰ���ʵ��ֱ���
	private String id=null;
	private int num=0;
	private int tx=0;// ��¼Table����˵���������
	
	/** ���캯��
	 * 
	 * @ ����ɶԸ����ļ��Ĵʷ������Ļ����ϣ�ʵ���﷨����
	 * @ ͨ������Compiler()����﷨����
	 * */
	public YufaCompiler(String filename)
	{
		WordsCompiler te=new WordsCompiler(filename);
		//��ʼ����Ҫʹ�õĴʷ��������õ��ı�
		this.myID=te.getMyID();
		this.myNUM=te.getMyValue();
		this.mySYM=te.getMySYM();
		//�õ�������
		this.SYMiterator=this.mySYM.iterator();
		this.IDiterator=this.myID.iterator();
		this.NUMiterator=this.myNUM.iterator();
		//��ʼ������˵����Table
		Table=new Vector<Vector>();
		//��ʼ���м����Code
		Code=new Vector<Instruct>();
		Code1 = new Instruct[10000];
		index=0;
		offset=1;
		Compiler();//�����﷨������
	}
	
	/** �﷨������
	 * 
	 * @ ����Block�������еݹ��½�������������﷨����������ӡ���ű���м���������
	 * */
	private void Compiler(){
		current=getNextSYM();
		Block(0);
		Code.add(0,new Instruct(FunctionCode.JMP,0,myIndex));// ����Vector�Ŀ⺯��addʵ�����м����������һ����Ŀ
		Code1[0] = new Instruct(FunctionCode.JMP,0,myIndex);
		System.out.println("�﷨����������");
		System.out.println();
		System.out.println("˵����"+Table.size());
		System.out.println("name\tkind\tlevel\taddress\ttx");// ��ӡ˵�����ű�ı�ͷ
        for(int j=0;j<Table.size();j++)
        {
	        Vector<TableItem> table=Table.elementAt(j);
	        for(int i=0;i<table.size();i++)
			{	
	        	System.out.print(table.elementAt(i).name+"\t"+table.elementAt(i).kind+"\t"+table.elementAt(i).level+"\t"+table.elementAt(i).address+"\t"+table.elementAt(i).tx+"\n");	
			}	
        }	
        System.out.println();
        System.out.println("�м���룺");
        System.out.println("���\t������\t��β�\tλ����");
        for(int i=0;i<Code.size();i++)
        {
        	Instruct instruct=Code1[i];
        	System.out.println(i+"\t"+instruct.instruct+"\t"+instruct.abslevel+"\t"+instruct.addr);
        }
	 }  
	
	/** �������
	 * 
	 * ��˵�����ͷ�Ϊ3�ࣺ���������������̣����������ײ��͹�������壩
     * �ֳ���,�������˵����
	 * */
	private void Block(int level) 
	{
		Vector<TableItem> myTable=new Vector<TableItem>();// ����һ�����ű����͵Ķ�������
		Table.add(myTable);// ����������һ������˵����ӵ����ű���
		int addr=3;// ƫ������ʼ��Ϊ3
		int count=0;// ��¼�����оֲ������ĸ���
		int currenttx=tx;// ��¼��ǰ�������ڵĹ���˵��������Table�е�����
		
	    // ʶ��˵������
		if(current!=null)
		{			
			if(current.equals(SYM._CONST))
			{// ����˵��
				Const(myTable,level);// ���ó���˵���Ĵ���
				if((current=getNextSYM())==null)
					return; 
			}
			if(current.equals(SYM._VAR))
			{// ����˵��
				count=Var(myTable,level,addr);// �������˵��
				if((current=getNextSYM())==null)
					return;
			}
			if(current.equals(SYM._procedure))
			{//����˵��,˳�㽫����˵������뵽�����
				Advanced();
				pro(currenttx,level);
			}
		}
		if(level==0)// �������
		   myIndex=index+offset;
		GEN(FunctionCode.INT,0,3+count);//�ֲ������ĸ���+3
		Sentence(level,currenttx);// ���
		//System.out.println("end procedure "+level+"\t"+current);
		GEN(FunctionCode.OPR,0,0);//�˳�������
	}
	
	/** ���������������
	 * 
	 * @ ʶ��Ϸ���䣬return ֮��current�ŵ�����һ�����ֵĵ�һ��SYM
	 * */
	private void Sentence(int level,int currenttx) 
	{			
		if(current!=null)
		{// current ��ŵ��ʵ��ֱ���
			//System.out.println("what sentence "+level+" "+current);
			if(current.equals(SYM._begin))// begin-end�������
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
				Sentence(level,currenttx);// �ݹ鴦�����Procedure���������
				while(current!=null)
				{
					if(current.equals(SYM._FH))//currentΪ";",˵�����滹�����
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
					else if(current.equals(SYM._end))//currentΪend����ʾ����������
					{
					   Advanced();
					   //System.out.println("begin sentence end at "+level+" "+current);
					   return;//��ȷ����Ψһ����
					}
					else//���������ַ�����˵��������
					{
						System.out.println("����������ȱ��end");
						err2();
					}
				}
				System.out.println("����������ȱ��end");
				err2();
			}
			else if(current.equals(SYM._ID))//��ֵ���,��һ������Ϊ������ʶ��
			{
				//System.out.println("assignment sentence"+level+current);
				//���
				id=getNextID();// ��������£���һ���ʵ��ֱ���Ϊ_MD(��=)
				TableItem currentItem=getTableItem(id,level,currenttx);
				if(currentItem==null)//����鿴��ʶ���Ƿ���
				{
					System.out.println("��ֵ������ʹ����δ˵���ı�ʾ������");					
					System.exit(0);
				}
				if(currentItem.kind==KIND.CON)// ���������ͼ��
				{
					System.out.println("��ֵ����������һ����������");					
					System.exit(0);
				}
				Advanced();
				if(current!=null && current.equals(SYM._MD))// ��ֵ��_MD(:=)
				{
					Advanced();
					Expression(level,currenttx); // ���ʽ
					GEN(FunctionCode.STO,level-currentItem.level,currentItem.address);//sto abslevel,id 
					//System.out.println("assignment sentence end at "+level+" "+current);
					return;//Ψһ��ȷ��������
				}
				System.out.println("��ֵ��������");
				err2();
				
			}
			else if(current.equals(SYM._if))//�������
			{
				//System.out.println("if sentence "+level+" "+current);
				Condition(level,currenttx);//����	
				
				//System.out.println("if jpc index "+index);
				index++;
				int hereOutif=index;//jpc���ռ�õ�λ��
				if(current!=null && current.equals(SYM._then))//then
				{
					Advanced();
					Sentence(level,currenttx);//���
					
					Code.add(hereOutif, new Instruct(FunctionCode.JPC,0,index+offset));//����then���֮��Ĵ����ַ
					Code1[hereOutif] =  new Instruct(FunctionCode.JPC,0,index+offset);
					//System.out.println("if sentence end at "+level+"\t"+current+"\t"+index);
					return;//��ȷ����
				}
				else
				{
					System.out.println("�������ȱ��then����");
					err2();
				}
			}
			else if(current.equals(SYM._while))//��ѭ�����
			{
				//System.out.println("while sentence "+level+" "+current);
				int hereInwhile=index+1;//��¼whileѭ����ʼ�ĵط�,��������ת���ĵط�
				Condition(level,currenttx);//����
				
				index++;//ռ��һ��Codeλ��	
				int hereOutwhile=index;//jpc������ĵط�
				if(current!=null&&current.equals(SYM._do))//do
				{
					Advanced();
					Sentence(level,currenttx);//���
					GEN(FunctionCode.JMP,0,hereInwhile+offset);
					Code.add(hereOutwhile, new Instruct(FunctionCode.JPC,0,index+offset));//����then���֮��Ĵ����ַ
					Code1[hereOutwhile] = new Instruct(FunctionCode.JPC,0,index+offset);
					//System.out.println("while sentence end at "+level+" "+current);
					return;//��ȷ����
				}
				else
				{
					System.out.println("��ѭ��ȱ��do����");
					err2();
				}
			}
			else if(current.equals(SYM._write))//д���
			{
				Advanced();
				//System.out.println("write sentence "+level+"\t"+current);
				wrd(1,level,currenttx);
				//System.out.println("write sentence end at "+level+"\t"+current);
				return;
			}
			else if(current.equals(SYM._read))//�����
			{
				//System.out.println("read sentence "+level+"\t"+current);
				Advanced();
				wrd(0,level,currenttx);
				//System.out.println("read sentence end at "+level+"\t"+current);
				return;
			}
			else if(current.equals(SYM._call))//���̵������
			{
				//System.out.println("call sentence "+level+" "+current);
				if((current=getNextSYM())!=null&&current.equals(SYM._ID))//��ʾ��
				{//���
					id=getNextID();
					TableItem currentItem=getTableItem(id,level,currenttx);
					if(currentItem==null)
					{
						System.out.println("���̵���������ʹ����δ˵���Ĺ��̱�ʾ������"+id);					
						System.exit(0);
					}
					if(currentItem.kind!=KIND.PRO)//���ͼ��
					{
						System.out.println("���̵������������õı�ʾ�����ǹ���������"+id);					
						System.exit(0);
					}
					if(currentItem.level>level)
					{
						System.out.println("��������ô˹��� "+id);					
						System.exit(0);
					}
					GEN(FunctionCode.CAL,level,currentItem.address);
					Advanced();
					//System.out.println("call sentence end at "+level+" "+current);
					return;//��ȷ����
					
				}
				
				else
				{
					System.out.println("���̵�����������");
					err2();
				}
			}

			else//���ǺϷ������
			{System.out.println("�Ƿ���䣡��");
			   err2();
			}
		}
		else//����䣨�Ϸ���
		{
			Advanced();
			return;
		}
	}
	
	/** ��Ĵ����˷������Ҳ�����
	 * 
	 * returnʱcurrent�ŵ��Ǻ��沿��
	 * */
	private void Item(int level,int currenttx)
	{
		//System.out.println("   item "+level+" "+current);
		Yinzi(level,currenttx);// ����
		while((current=getNextSYM())!=null){// ������У����Ȼ�ǳ˳������������
			if(current.equals(SYM._STAR)||current.equals(SYM._DIV))
			{
				int ii=0;
				if(current.equals(SYM._STAR))
					ii=OPR.MUL;
				else 
					ii=OPR.DIV;
				Advanced();
				Yinzi(level,currenttx);// ��ȷ����
				GEN(FunctionCode.OPR,0,ii);	// ����ii����			    
			}
			else return;
		}
		return;
	}
	
	/** ���ӵĴ���
	 * 
	 * @ ���Ӱ�������ʶ�����޷�����������������
	 * */
	private void Yinzi(int level,int currenttx)
	{
		//System.out.println("   yinzi "+level+" "+current);
		if(current!=null)
		{
			if(current.equals(SYM._ID))//��ʶ��
			{
				//���
				id=getNextID();
				TableItem currentItem=getTableItem(id,level,currenttx);
				if(currentItem==null)
				{
					System.out.println("���ʽ����ʹ����δ˵���ı�ʾ������");					
					System.exit(0);
				}
				if(currentItem.kind==KIND.VAR)//���ͼ��
				    GEN(FunctionCode.LOD,level-currentItem.level,currentItem.address);//�������ŵ�����ջ��
				else if(currentItem.kind==KIND.CON)//���ͼ��
					GEN(FunctionCode.LIT,0,currentItem.address);//������address�ŵ�����ջ��
				 return;
			}			  
			if(current.equals(SYM._INT))//�޷�������
			{
				GEN(FunctionCode.LIT,0,getNextNUM());//�������ŵ�����ջ��
				return;
			}		
			if(current.equals(SYM._LEFT))//������
			{
				Expression(level,currenttx);//���ʽ
				if(current!=null && current.equals(SYM._RIGHT))//������
					return;
				else
				{// ȱ��������
					System.out.println("���ӳ�����");
					err2();
				}
			}
			else
			{
				System.out.println("���ӳ�����");
				err2();
			}	
		}
		else
		{// ������δ˵��
			System.out.println("���ӳ�����");
			err2();
		}	
		
	}
	
	/** ʶ��Ϸ����ʽ
	 * 
	 * @ returnʱcurrent�ŵ��Ǻ��沿��
	 * */
    private void Expression(int level,int currenttx) 
    {
    	//System.out.println("   expression "+level+" "+current);
    	if(current.equals(SYM._PLUS))//+
    	{
    		Advanced();
    		Item(level,currenttx);//��
    	}
    	else if(current.equals(SYM._SUB))//-
    	{
    		Advanced();
    		Item(level,currenttx);//��
    		GEN(FunctionCode.OPR,0,OPR.MINUS);//ȡ��
    	}
    	else 
    		Item(level,currenttx);//��
    	while(current!=null)//������У�������ǼӼ����������
		{
			if(current.equals(SYM._SUB)||current.equals(SYM._PLUS))//+����-
    		{
				int ii=0;
				if(current.equals(SYM._SUB))ii=OPR.SUB;
				else  ii=OPR.ADD;
				Advanced();
    			Item(level,currenttx);//��
    			GEN(FunctionCode.OPR,0,ii);//����ii����
    		}
    		else
    		{
    			return;//����Ĳ��ֲ����ڸñ��ʽ
    		}
		} 
    	return;
    }
    
    /** �������Ĵ���
     * 
     * returnʱcurrent�ŵ��Ǻ��沿��
     * */
	private void Condition(int level,int currenttx)
	{
		//System.out.println("   condition "+level+" "+current);
		if((current=getNextSYM())!=null && current.equals(SYM._odd))
		{// һԪ���㣺�ж���ż��odd��ʽ�ı��ʽ
			Advanced();
			Expression(level,currenttx);
			GEN(FunctionCode.OPR,0,OPR.ODD);
			//Condition(level,currenttx);
			return;//��ȷ��������һ
		}
		else//��һ����ʽ�ı��ʽ
		{  
			Expression(level,currenttx);//���ʽ
			//System.out.println("   condition current SYM: "+current);
			if(current!=null)
			{
				if(current.equals(SYM._ASSIGN)||current.equals(SYM._LESS)||//��ϵ�����
						current.equals(SYM._MORE)||current.equals(SYM._LESSEQ)
			            ||current.equals(SYM._MOREEQ)||current.equals(SYM._JH))
				{//�γ�Ŀ�����
					int ii=0;
					if(current.equals(SYM._ASSIGN))ii=OPR.EQ;//=
					else if(current.equals(SYM._LESS))ii=OPR.LT;//<
					else if(current.equals(SYM._MORE))ii=OPR.GT;//>
					else if(current.equals(SYM._LESSEQ))ii=OPR.LE;//<=
					else if(current.equals(SYM._MOREEQ))ii=OPR.GE;//>=	
					else if(current.equals(SYM._JH))ii=OPR.UE;//#,������	
					Advanced();
					Expression(level,currenttx);//���ʽ
					GEN(FunctionCode.OPR,0,ii);//����ii����
					return;//��ȷ��������
				}
				else
				{
					System.out.println("��������11����"); 
					err2();
				}									
			}
			else
			{
				System.out.println("��������22����"); 
				err2();
			}
		}
	}
	
	/**������д��䷽��,index=0,����䣬index=1��д���
	 * 
	 * ???????
	 * */
	private void wrd(int inagr,int level,int currenttx)
	{
		if(current!=null&&current.equals(SYM._LEFT))//������
		{
			if((current=getNextSYM())!=null&&current.equals(SYM._ID))//��ʾ��
			{
				//���
				id=getNextID();
				TableItem currentItem=getTableItem(id,level,currenttx);
				if(currentItem==null)
				{
					if(inagr==0)
						   System.out.println("�����ṹ���󣡣�ʹ����δ˵���ı�ʾ������");
						else
						   System.out.println("д�����󣡣���ʹ����δ˵���ı�ʾ������");
					    err2();
				}
				if(inagr==0)
				{
					GEN(FunctionCode.OPR,0,OPR.READ);//read����
					GEN(FunctionCode.STO,level-currentItem.level,currentItem.address);
				}
				else
				{
					GEN(FunctionCode.LOD,level-currentItem.level,currentItem.address);
					GEN(FunctionCode.OPR,0,OPR.WRITE);//write����
				}
				
				while((current=getNextSYM())!=null&&current.equals(SYM._DH))//���ţ�˵�����б�ʾ��
				{
					if((current=getNextSYM())!=null&&current.equals(SYM._ID))//��ʾ��
					{//������γ�Ŀ�����
						id=getNextID();
						if((currentItem=getTableItem(id,level,currenttx))==null)
						{
							if(inagr==0)
								   System.out.println("�����ṹ���󣡣�ʹ����δ˵���ı�ʾ������");
							else
								   System.out.println("д�����󣡣���ʹ����δ˵���ı�ʾ������");
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
						if((current=getNextSYM())!=null&&current.equals(SYM._DH))//TODO  ������м����û����
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
						   System.out.println("�����ṹ���󣡣�");
						else
						   System.out.println("д�����󣡣���");
					    err2();
					}
				}
				//����ѭ����������������
				if(current.equals(SYM._RIGHT))
				{
				    Advanced();
					return;
				}
					
				else
				{
					if(index==0)
					   System.out.println("�����ṹ���󣡣�");
					else
					   System.out.println("д�����󣡣���");
				    err2();
				}
			}
			else
			{
				if(index==0)
				   System.out.println("�����ṹ���󣡣�");
				else
				   System.out.println("д�����󣡣���");
			    err2();
			}
		}
		else
		{
			if(index==0)
			   System.out.println("�����ṹ���󣡣�");
			else
			   System.out.println("д�����󣡣���");
		    err2();
		}
	}
	
	/** ����Ŀ�����
	 * 
	 * @ �βΣ������룬��βƫ����
	 * @ ��������Ŀ�������ӵ�Code���У���ά��Code�������index
	 * */
	private void GEN(FunctionCode instruct,int abslevel,int addr )
	{
		Code.add(new Instruct(instruct,abslevel,addr));
		index++;
		Code1[index] = new Instruct(instruct,abslevel,addr);
	}	
	
	/**
	 * ����˵������,return ֮��current�ŵ�����һ�����ֵĵ�һ��SYM
	 * */
    private void pro(int currenttx,int level)
    {
    	prepro(currenttx,level);//�����ײ���txָ��Table����±꣬���ڲ����˵������ڵ�ַ
		Advanced();
		if(level+1>3)
		{
			System.out.println("�����������3���˳�");
			System.exit(0);
		}
		Block(level+1);//�ֳ����ڲ����˵����Ĺ���
		if(current!=null && current.equals(SYM._FH))
		{
			Advanced();
			while(current!=null && current.equals(SYM._procedure))//������в��еĹ���˵��
			{
				Advanced();
				pro(currenttx,level);
			}
		}
		else{
			System.out.println("����˵�����󣡣�ȱ�ٷֺţ���");
			System.exit(0);
		}
		
    	
    }
    
    /** �����ײ��Ĵ���
     * 
     *  txָ��Table����±꣬���ڲ����˵������ڵ�ַ
     * */
	private void prepro(int currenttx,int level)
	{
		Vector<TableItem> myTable=Table.elementAt(currenttx);
		if(current!=null && current.equals(SYM._ID))//��ʶ��
		{	
			if((current=getNextSYM())!=null && current.equals(SYM._FH))//�ֺ�
			{
				id=getNextID();
				if(id!=null)
				{
					KIND kind=KIND.PRO;
					TableItem item=new TableItem(id,kind,level,index+offset,currenttx);
					//System.out.println("insert proc\t"+id+"\t"+level);
					if(!legal(item,myTable,currenttx))
					{
						System.out.println("����"+item.name+"�ض��壡��");
						err();
					}
					myTable.add(item);
					tx++;
					//myTable.removeAllElements();//������һ���±�//���û��Ԥ�ڵ�Ч������ΪVector����ֻ�ǽ�ָ������ڴ�
					
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
	
	/**������˵��
	 * 
	 * */
	private void Const(Vector<TableItem>myTable, int level)
	{
		if((current=getNextSYM())!=null&&current.equals(SYM._ID))
			if((current=getNextSYM())!=null&&current.equals(SYM._ASSIGN))
				if((current=getNextSYM())!=null&&current.equals(SYM._INT))//��ʾ�� �Ⱥ� �����磨a=10��
					if((id=getNextID())!=null)//������ʾ��
						if((num=getNextNUM())!=-1)
						{
							KIND kind=KIND.CON;
							TableItem item=new TableItem(id,kind,level,num,tx);
							if(!legal(item,myTable,tx))
							{
								System.out.println("����"+item.name+"�ض��壡��");
								err();
							}
							myTable.add(item);
							//System.out.println("insert const\t"+id+"\t"+num);
						}
						else//���Ӳ�����
						{System.out.println("c1");
						   err();}
					else//���Ӳ�����
					{System.out.println("c2");
					   err();}
				else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
				{System.out.println("c3");
				   err();}
			else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
			{System.out.println("c4");
			   err();}
		else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
		{System.out.println("c5");
		   err();}
		while((current=getNextSYM())!=null&&current.equals(SYM._DH))//���ţ�˵�����滹�г�������
		{
			if((current=getNextSYM())!=null&&current.equals(SYM._ID))
				if((current=getNextSYM())!=null&&current.equals(SYM._ASSIGN))
					if((current=getNextSYM())!=null&&current.equals(SYM._INT))//��ʾ�� �Ⱥ� �����磨a=10��
						if((id=getNextID())!=null)//������ʾ��
							if((num=getNextNUM())!=-1)
							{
								KIND kind=KIND.CON;
								TableItem item=new TableItem(id,kind,level,num,tx);
								if(!legal(item,myTable,tx)){System.out.println("����"+item.name+"�ض��壡��");err();}
								myTable.add(item);
								//System.out.println("insert const\t"+num);
							}
							else//���Ӳ�����
							{System.out.println("c6");
							   err();}
							
						else//���Ӳ�����
						{System.out.println("c7");
						   err();}
					else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
					{System.out.println("c8");
					   err();}
				else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
				{System.out.println("c9");
				   err();}
			else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
			{System.out.println("c10");
			   err();}
		}
		if(current!=null&&current.equals(SYM._FH));//���Ƿֺţ���˵�������������
		else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
		{
		   System.out.println("c11");
		   err();}
	}
	
	/** ����˵���Ĵ���  
	 * @ ���������ı�����
	 * */
	private int Var(Vector<TableItem>myTable,int level,int addr)
	{
		int count=0;//��¼�����ĸ���
		if((current=getNextSYM())!=null&&current.equals(SYM._ID))//��ʾ�� 
			if((id=getNextID())!=null)//������ʾ��
			{
				count++;
				KIND kind=KIND.VAR;
				TableItem item=new TableItem(id,kind,level,addr++,tx);	
				if(!legal(item,myTable,tx))
				{
					System.out.println("����"+item.name+"�ض��壡��");
					err();
				}
				myTable.add(item);
				//System.out.println("insert var\t"+id+"\t"+level);
			}
			else{System.out.println("4");
			   err();}
		else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
		{
			System.out.println("5");
		    err();
		}
		while((current=getNextSYM())!=null&&current.equals(SYM._DH))//���ţ�˵�����滹�г�������
		{
			if((current=getNextSYM())!=null&&current.equals(SYM._ID))//��ʾ�� 
				if((id=getNextID())!=null)//������ʾ��
				{
					count++;
					KIND kind=KIND.VAR;
					TableItem item=new TableItem(id,kind,level,addr++,tx);
					if(!legal(item,myTable,tx)){System.out.println("����"+item.name+"�ض��壡��");err();}
					myTable.add(item);
					//System.out.println("insert var\t"+id+"\t"+level);
				}
				else//���Ӳ�����
				{System.out.println("6");
				   err();}
			else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
			{System.out.println("7");
			   err();}
		}
		if(current!=null&&current.equals(SYM._FH));//���Ƿֺţ���˵�������������
		else//�����Ǿ��Ӳ������������Ǿ��Ӵ���
		{System.out.println("8");
		   err();}
		return count;
	}
	
	/**����ֱ��������е���һ��
	 * 
	 * @ ���򷵻���һ�ֱ���
	 * @ û���򷵻�null
	 * */
	private SYM getNextSYM(){
		if(SYMiterator.hasNext())
		{
			SYM next=SYMiterator.next();
			return next;
		}
		return null;
	}
	
	/** ��ñ�ʶ�������е���һ��
	 * 
	 * @ ���򷵻���һ��ʶ��
	 * @ ���򷵻�null
	 * */
	private String getNextID(){
		if(IDiterator.hasNext())
		{
			String next=IDiterator.next();
			return next;
		}
		return null;
	}
	
	/** ����û��Զ������ݵ������е���һ��
	 * 
	 * @ ���򷵻���һ����
	 * @ ���򷵻�-1
	 * */
	private int getNextNUM(){
		if(NUMiterator.hasNext())
		{
			int next=String2int(NUMiterator.next());
			return next;
		}
		return -1;
	}
	
	/** ������ǰһ�������ĵ��ʣ�������һ��
	 * 
	 * @ ����ֱ�������(SYM)�л�����һ�������޸�currentΪ��һ���ʵ��ֱ���
	 * @ ������currentΪnull
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
	
	/**��string���͵���ת��Ϊint����
	 * 
	 * @ ���룺�ַ���
	 * @ �����ת�����int���͵���
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
	
	/**������
	 * 
	 * @ ����˵�����Ϸ�
	 * */
	private void err()
	{
		System.out.println("����������ڲ��Ϸ��Ĺ���˵��������");
		System.exit(0);
	}
	
	/**������
	 * 
	 * @ ��䲻�Ϸ�
	 * */
	private void err2()
	{
		System.out.println("����������ڲ��Ϸ�����䣡����");
		System.exit(0);
	}
	
	/**�õ����ű���ĿԪ��
	 * 
	 * @ �βΣ����ŵ����֣���Σ����ű�ָ��
	 * @ ���ҵ����򷵻�����ѯ�ķ�������<name,kind,level,address,tx>
	 * @ ��δ�ҵ����򷵻�null
	 * */
	private TableItem getTableItem(String name,int level,int tx)
	{
		Iterator<Vector> tt=Table.iterator();//�ܵķ���˵���������
		while(tt.hasNext())
		{
			Iterator<TableItem> table=tt.next().iterator();//С���������һ��˵�����Ľ���
			while(table.hasNext()){
				TableItem item=table.next();
				if(item.name.equals(name))
				{
					if(item.level<level)//�ڸù��̼��丸�����е����������ĳ��������������������ɼ�
					    return item;
					if(item.level==level)//�����ͬ
					{
						if(item.tx==tx)//��������ͬ�Ĺ�����˵����
						   return item;
						else 
							continue;
					}
					else
						return null;//���ɼ��ı�������
				}
				  
			}
		}		
		return null;
	}
	
	/** �жϲ����Ƿ�Ϸ�,��ʱ��tx����
	 * 
	 * @ ��������ķ������ڷ��ű��У����ʾ�˷�����������������false
	 * @ ���򣬷���true������ɲ���
	 * */
    private Boolean legal(TableItem item,Vector<TableItem>myTable,int tx)
    {
    	TableItem item1=getTableItem(item.name,item.level,tx);
    	if(item1==null)
    	{// ������ķ�����δ����
    		Iterator<TableItem> tt=myTable.iterator();//С�������
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
    		if(item1.kind.equals(item.kind))//����ڿɼ��ķ�Χ���ض���(������ͬ����������ͬ)
    			return false;
    		if(item1.kind.equals(KIND.VAR)&&item.kind.equals(KIND.CON))//����ڿɼ��ķ�Χ���ض���(������ͬ�������������)���򱨴�����
    		    return false;
    		if(item1.kind.equals(KIND.CON)&&item.kind.equals(KIND.VAR))//����ڿɼ��ķ�Χ���ض���(������ͬ�������������)���򱨴�����
    			return false;
    	}
    	return true;//�������볣�����������ͬ�������ض���
    }
}


