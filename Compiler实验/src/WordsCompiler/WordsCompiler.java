package WordsCompiler;
import java.io.*;
import java.util.Vector;

/**词法分析器  
 * @ 输入：用户程序(一堆字符串)
 * @ 输出：单词串
 * 
 * @ 返回：单词的名字，种别码，所在的行号
 * @ 如果出错，则报告错误号，并做相应处理
 * */
public class WordsCompiler {
	private char ch;
	private int pointer,length;
	private String Buffer;
	private String strToken;
	private Vector<SYM> mySYM;// 种别码
	private Vector<String> myID;// 变量标识符
	private Vector<String> myNUM;// 常数数值
	private File inputfile;// 用户源文件
	private BufferedReader reader;
	private Boolean isEnd;// 标记是否结束
	
	
	/**构造器
	 * 
	 * @ 完成初始化
	 * @ 调用Compiler预处理子程序，完成对给定文件的词法分析
	 * */ 
	public WordsCompiler(String filename)
	{
		Buffer="";
		mySYM=new Vector();
		myID=new Vector();
		myNUM=new Vector();
		length=0;// 初始化一行长度为0
		strToken=new String();// 存放获得的单词
		pointer=-1; 
		inputfile=new File(filename);
		isEnd=false;
		Compiler();
	}
	
	/** 读字符过程
	 * 
	 * @ 每遍从用户程序中读出一个字符
	 * */
	public void GETSYS(){
		strToken="";
		SYM code=null;//定义种别码
		String warn="出现不合法的符号！编译停止！";
		GetChar();// 得到一个字符，并处理指针
		GetBC();// 去掉空格或Tab
		if(isLetter())
		{//得到的第一个字符是字母
			while(isLetter()||isDigital())
			{
				Concat();//拼接字符
				GetChar();//如果在行末，则无法读出下一个字符。
			}
			Retract();//回调
			code=Reserve();//获得种别码
			if(code==SYM._ID)
			{//得到的是标识符
				InsertSYM(SYM._ID);
				InsertId(strToken);
				if (isEnd)
					return;
				GETSYS();//递归调用
			}
			else
			{//关键字
				InsertSYM(code);
				if (isEnd)
					return;
				GETSYS();
			}
		}
		else if(isDigital())
		{//常数
			while(isDigital())
			{
				Concat();
				GetChar();
			}
			Retract();
			InsertConst(strToken);
			InsertSYM(SYM._INT);//常量
			if (isEnd)
				return;
			GETSYS();
		}
		//其他符号
		else if(ch=='='){InsertSYM(SYM._ASSIGN);if (isEnd)return;GETSYS();}
		else if(ch==','){InsertSYM(SYM._DH);if (isEnd)return;GETSYS();}
		else if(ch=='+'){InsertSYM(SYM._PLUS);if (isEnd)return;GETSYS();}
		else if(ch=='-'){InsertSYM(SYM._SUB);if (isEnd)return;GETSYS();}
		else if(ch=='*'){InsertSYM(SYM._STAR);if (isEnd)return;GETSYS();}
		else if(ch=='/'){InsertSYM(SYM._DIV);if (isEnd)return;GETSYS();}
		else if(ch=='('){InsertSYM(SYM._LEFT);if (isEnd)return;GETSYS();}
		else if(ch==')'){InsertSYM(SYM._RIGHT);	if (isEnd)return;GETSYS();}
		else if(ch=='#'){InsertSYM(SYM._JH);if (isEnd)return;GETSYS();}
		else if(ch==';'){InsertSYM(SYM._FH);if (isEnd)return;GETSYS();System.out.println("fenhao,"+pointer);}
		else if(ch=='<'){
			GetChar();
			if(ch=='='){InsertSYM(SYM._LESSEQ);if (isEnd)return;GETSYS();}
			else {InsertSYM(SYM._LESS);Retract();if (isEnd)return;GETSYS();}
		}
		else if(ch=='>')
		{
			GetChar();
			if(ch=='='){InsertSYM(SYM._MOREEQ);if (isEnd)return;GETSYS();}
			else {InsertSYM(SYM._MORE);Retract();if (isEnd)return;GETSYS();}
		}
		else if(ch==':')
		{
			GetChar();
			if(ch=='='){InsertSYM(SYM._MD);if (isEnd)return;GETSYS();}
			else {Retract();if (isEnd)return;GETSYS();}
		}
		else if(ch=='\n')return;
		else System.out.println(warn);
		
	}
	
	/** 预处理子程序
	 * 
	 * @ 处理出一串确定长度（120个字符）的输入字符，并将其装进词法分析器所指定的缓冲区中
	 * */
	private void Compiler(){		
		try{
			if(!inputfile.exists()||inputfile.isDirectory())
			{//文件不存在
				throw new FileNotFoundException();
			}
			reader=new BufferedReader(new FileReader(inputfile) );
			Buffer=reader.readLine();// temp返回读到的字符个数
			length=Buffer.length();
			while(Buffer!=null)
			{
				System.out.println(Buffer.toString());
				GETSYS();// 调用每次读出一个字符
				if(isEnd)
				{
					isEnd=false;
					Buffer=reader.readLine();//每次从用户程序中读出一行
				}
				length=Buffer.length();
				pointer=-1;
			}
		}
		catch(FileNotFoundException e){
			System.out.println("未找到需要编译的文件！词法分析结束！");
		}
		catch(Exception e){
			System.out.println("");
			System.out.println("词法分析完成！");
            System.out.println("SYM:"+mySYM);
            System.out.println("ID:"+myID);
            System.out.println("NUM:"+myNUM);
            System.out.println("");
		}
		finally
		{
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	/** 子程序过程，将下一个输入字符读到ch中，搜索指示器前移一个字符位置
	 * 
	 * @ 如果pointer==length-1，则标记到达行尾
	 * */
	private void GetChar()
	{
		if(pointer<length-1)
		{
			pointer++;//指针前移
			ch=Buffer.charAt(pointer);
			System.out.println(ch+","+pointer);	
			if(pointer==length-1)//行尾
				isEnd=true;
		}	
		else 	
		{//到达行尾
			ch='\n';
			isEnd=true;		
		}			
	}
	
	/** 子程序过程，检查ch中的字符是否为空白或Tab或行结束标志
	 *
	 * @ 若是空白或Tab，则调用GetChar直至ch中进入一个非空白字符
	 * @ 若是行结束标志\n，则直接返回退出
	 * */
	private void GetBC(){
		if (ch=='\n')
		{
			isEnd=true;
			return;
		}
		while (ch==' '||ch== 9)
			GetChar();	
	}
	
	/**子程序过程，检查是否为字母
	 * 
	 * @ 实则返回ture
	 * @ 否则返回 false
	 * */
	private Boolean isLetter(){
		if(ch=='q'||ch=='w'||ch=='e'||ch=='r'||ch=='t'||ch=='y'||ch=='u'||ch=='i'||ch=='o'||ch=='p'||
				ch=='a'||ch=='s'||ch=='d'||ch=='f'||ch=='g'||ch=='h'||ch=='j'||ch=='k'||ch=='l'||
				ch=='z'||ch=='x'||ch=='c'||ch=='v'||ch=='b'||ch=='n'||ch=='m'||ch=='Q'||ch=='W'||
				ch=='E'||ch=='R'||ch=='T'||ch=='Y'||ch=='U'||ch=='I'||ch=='O'||ch=='P'||ch=='A'||
				ch=='S'||ch=='D'||ch=='F'||ch=='G'||ch=='H'||ch=='J'||ch=='K'||ch=='L'||ch=='Z'||
				ch=='X'||ch=='C'||ch=='V'||ch=='B'||ch=='N'||ch=='M')
			return true;
		else
			return false;
			
	}
	
	/**子程序过程，检查是否为数字
	 * 
	 * @ 实则返回ture
	 * @ 否则返回 false
	 * */
	private Boolean isDigital(){
		if(ch=='1'||ch=='2'||ch=='3'||ch=='4'||ch=='5'||ch=='6'||ch=='7'||ch=='8'||ch=='9'||ch=='0')
			return true;
		else 
			return false;
	}
	
	
	/**
	 * 子程序过程，将ch中的字符连接到strToken之后
	 * */
	private void Concat()
	{
		strToken=strToken+ch;		
	}
	
	/** 整型过程，对strToken中的字符串查找关键字表
	 * 
	 *  @ 若是一个保留字、标识符，则返回它的编码
	 *  @ 否则返回0
	 * */
	private SYM Reserve(){
		SYM flag=SYM._ID;//默认为标识符
		//如果是关键字，则重置为关键字
		if(strToken.toUpperCase().compareTo("CONST")==0)
			flag=SYM._CONST;
		if(strToken.toUpperCase().compareTo("VAR")==0)
			flag=SYM._VAR;
		if(strToken.toLowerCase().compareTo("procedure")==0)
			flag=SYM._procedure;
		if(strToken.toLowerCase().compareTo("begin")==0)
			flag=SYM._begin;
		if(strToken.toLowerCase().compareTo("end")==0)
			flag=SYM._end;
		if(strToken.toLowerCase().compareTo("odd")==0)
			flag=SYM._odd;
		if(strToken.toLowerCase().compareTo("if")==0)
			flag=SYM._if;
		if(strToken.toLowerCase().compareTo("then")==0)
			flag=SYM._then;
		if(strToken.toLowerCase().compareTo("call")==0)
			flag=SYM._call;
		if(strToken.toLowerCase().compareTo("while")==0)
			flag=SYM._while;
		if(strToken.toLowerCase().compareTo("do")==0)
			flag=SYM._do;
		if(strToken.toLowerCase().compareTo("read")==0)
			flag=SYM._read;
		if(strToken.toLowerCase().compareTo("write")==0)
			flag=SYM._write;
	    return flag;
	}
	
	/**回调一个字符
	 * 
	 * @ 遇到分号、右括号时回调字符
	 * @ 并置当前获得字符为空格
	 * */
	private void Retract(){
		pointer--;
		if(isEnd && ch==';')
			isEnd=false;
		if(isEnd && ch==')')
			isEnd=false;
		ch=' ';
	}
	
	//添加标识符至myID
	private void InsertId(String strToken){
		myID.add(strToken);
	}
	
	//添加用户定义的数至myNUM
	private void InsertConst(String strToken){
		myNUM.add(strToken);
		
	}
	
	//添加种别码至mySYM
	private void InsertSYM(SYM sym){
		mySYM.add(sym);
	}
	
	public Vector<String> getMyID(){
		return this.myID;
	}
	
    public Vector<String> getMyValue(){
		return this.myNUM;
	}
    
    public Vector<SYM> getMySYM(){
	    return this.mySYM;
    }
}
