package compiler;

import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.lib.*;
import static compiler.lib.FOOLlib.*;

public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

	String indent;
    public boolean print;
	
    ASTGenerationSTVisitor() {}    
    ASTGenerationSTVisitor(boolean debug) { print=debug; }
        
    private void printVarAndProdName(ParserRuleContext ctx) {
        String prefix="";        
    	Class<?> ctxClass=ctx.getClass(), parentClass=ctxClass.getSuperclass();
        if (!parentClass.equals(ParserRuleContext.class)) // parentClass is the var context (and not ctxClass itself)
        	prefix=lowerizeFirstChar(extractCtxName(parentClass.getName()))+": production #";
    	System.out.println(indent+prefix+lowerizeFirstChar(extractCtxName(ctxClass.getName())));                               	
    }
        
    @Override
	public Node visit(ParseTree t) {
    	if (t==null) return null;
        String temp=indent;
        indent=(indent==null)?"":indent+"  ";
        Node result = super.visit(t);
        indent=temp;
        return result; 
	}

	@Override
	public Node visitProg(ProgContext c) {
		if (print) printVarAndProdName(c);
		return visit(c.progbody());
	}

	@Override
	public Node visitLetInProg(LetInProgContext c) {
		if (print) printVarAndProdName(c);
		List<DecNode> declist = new ArrayList<>();
		for (DecContext dec : c.dec()) declist.add((DecNode) visit(dec));
		return new ProgLetInNode(declist, visit(c.exp()));
	}

	@Override
	public Node visitNoDecProg(NoDecProgContext c) {
		if (print) printVarAndProdName(c);
		return new ProgNode(visit(c.exp()));
	}

	@Override
	public Node visitTimesDiv(TimesDivContext c) {
		if (print) printVarAndProdName(c);
        Node left = visit(c.exp(0));
        Node right = visit(c.exp(1));
        Node n;

        if (c.TIMES() != null) {
            n = new TimesNode(left, right);
            n.setLine(c.TIMES().getSymbol().getLine());
        } else {
            n = new DivNode(left, right);
            n.setLine(c.DIV().getSymbol().getLine());
        }
        return n;		
	}

	@Override
	public Node visitPlusMinus(PlusMinusContext c) {
        if (print) printVarAndProdName(c);
        Node left = visit(c.exp(0));
        Node right = visit(c.exp(1));
        Node n;

        if (c.PLUS() != null) {
            n = new PlusNode(left, right);
            n.setLine(c.PLUS().getSymbol().getLine());
        } else {
            n = new MinusNode(left, right);
            n.setLine(c.MINUS().getSymbol().getLine());
        }
        return n;
    }

	@Override
	public Node visitComp(CompContext c) {
        if (print) printVarAndProdName(c);
        Node left = visit(c.exp(0));
        Node right = visit(c.exp(1));
        Node n;

        if (c.EQ() != null) {
            n = new EqualNode(left, right);
            n.setLine(c.EQ().getSymbol().getLine());
        } else if (c.LE() != null) {
            n = new LessEqualNode(left, right);
            n.setLine(c.LE().getSymbol().getLine());
        } else {
            n = new GreaterEqualNode(left, right);
            n.setLine(c.GE().getSymbol().getLine());
        }
        return n;
	}

	@Override
	public Node visitVardec(VardecContext c) {
		if (print) printVarAndProdName(c);
		Node n = null;
		if (c.ID()!=null) { //non-incomplete ST
			n = new VarNode(c.ID().getText(), (TypeNode) visit(c.type()), visit(c.exp()));
			n.setLine(c.VAR().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitFundec(FundecContext c) {
		if (print) printVarAndProdName(c);
		List<ParNode> parList = new ArrayList<>();
		for (int i = 1; i < c.ID().size(); i++) { 
			ParNode p = new ParNode(c.ID(i).getText(),(TypeNode) visit(c.type(i)));
			p.setLine(c.ID(i).getSymbol().getLine());
			parList.add(p);
		}
		List<DecNode> decList = new ArrayList<>();
		for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));
		Node n = null;
		if (c.ID().size()>0) { //non-incomplete ST
			n = new FunNode(c.ID(0).getText(),(TypeNode)visit(c.type(0)),parList,decList,visit(c.exp()));
			n.setLine(c.FUN().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitIntType(IntTypeContext c) {
		if (print) printVarAndProdName(c);
		return new IntTypeNode();
	}

	@Override
	public Node visitBoolType(BoolTypeContext c) {
		if (print) printVarAndProdName(c);
		return new BoolTypeNode();
	}

	@Override
	public Node visitInteger(IntegerContext c) {
		if (print) printVarAndProdName(c);
		int v = Integer.parseInt(c.NUM().getText());
		return new IntNode(c.MINUS()==null?v:-v);
	}

	@Override
	public Node visitTrue(TrueContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(true);
	}

	@Override
	public Node visitFalse(FalseContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(false);
	}

	@Override
	public Node visitIf(IfContext c) {
		if (print) printVarAndProdName(c);
		Node ifNode = visit(c.exp(0));
		Node thenNode = visit(c.exp(1));
		Node elseNode = visit(c.exp(2));
		Node n = new IfNode(ifNode, thenNode, elseNode);
		n.setLine(c.IF().getSymbol().getLine());			
        return n;		
	}

	@Override
	public Node visitPrint(PrintContext c) {
		if (print) printVarAndProdName(c);
		return new PrintNode(visit(c.exp()));
	}

	@Override
	public Node visitPars(ParsContext c) {
		if (print) printVarAndProdName(c);
		return visit(c.exp());
	}

	@Override
	public Node visitId(IdContext c) {
		if (print) printVarAndProdName(c);
		Node n = new IdNode(c.ID().getText());
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitCall(CallContext c) {
		if (print) printVarAndProdName(c);		
		List<Node> arglist = new ArrayList<>();
		for (ExpContext arg : c.exp()) arglist.add(visit(arg));
		Node n = new CallNode(c.ID().getText(), arglist);
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}

    @Override
    public Node visitAndOr(AndOrContext c) {
        Node left = visit(c.exp(0));
        Node right = visit(c.exp(1));
        Node n;

        if (c.AND() != null) {
            n = new AndNode(left, right);
            n.setLine(c.AND().getSymbol().getLine());
        } else {
            n = new OrNode(left, right);
            n.setLine(c.OR().getSymbol().getLine());
        }
        return n;
    }

    @Override
    public Node visitNot(NotContext c) {
        if (print) printVarAndProdName(c);
        Node n = new NotNode(visit(c.exp()));
        n.setLine(c.NOT().getSymbol().getLine());
        return n;
    }

    /**
     cldec  : CLASS ID (EXTENDS ID)?
         LPAR (ID COLON type (COMMA ID COLON type)* )? RPAR
         CLPAR
            methdec*
         CRPAR ;
     */
    @Override
    public Node visitCldec(CldecContext c) {
        if (print) printVarAndProdName(c);

        // Get the class name
        String className = c.ID(0).getText();

        // Collect fields (the constructor parameters)
        List<FieldNode> fields = new ArrayList<>();
        if (c.type() != null) {
            for (int i = 1; i < c.type().size(); i++) {     // Start from 1 since we are not going to implement inheritance
                FieldNode f = new FieldNode(c.ID(i).getText(), (TypeNode) visit(c.type(i)));
                f.setLine(c.ID(i).getSymbol().getLine());
                fields.add(f);
            }
        }

        // Collect methods
        List<MethodNode> methods = new ArrayList<>();
        for (MethdecContext m: c.methdec()) {
            methods.add((MethodNode) visit(m));
        }

        ClassNode n = new ClassNode(className, fields, methods);
        n.setLine(c.ID(0).getSymbol().getLine());
        return n;
    }

    /**
     methdec : FUN ID COLON type
         LPAR (ID COLON type (COMMA ID COLON type)* )? RPAR
         (LET dec+ IN)? exp
         SEMIC ;
     */
    @Override
    public Node visitMethdec(MethdecContext c) {
        if (print) printVarAndProdName(c);
        List<ParNode> parList = new ArrayList<>();
        for (int i = 1; i < c.ID().size(); i++) {
            ParNode p = new ParNode(c.ID(i).getText(), (TypeNode) visit(c.type(i)));
            p.setLine(c.ID(i).getSymbol().getLine());
            parList.add(p);
        }
        List<DecNode> decList = new ArrayList<>();
        for (DecContext d: c.dec()) {
            decList.add((DecNode) visit(d));
        }
        return new MethodNode(c.ID(0).getText(),(TypeNode) visit(c.type(0)), parList, decList, visit(c.exp()));
    }

    /**
    NEW ID LPAR (exp (COMMA exp)* )? RPAR #new
     */
    @Override
    public Node visitNew(NewContext c) {
        if (print) printVarAndProdName(c);
        List<Node> argList = new ArrayList<>();
        for (ExpContext e : c.exp()) {
            argList.add(visit(e));
        }
        NewNode n = new NewNode(c.ID().getText(),argList);
        n.setLine(c.ID().getSymbol().getLine());
        return n;
    }

    /**
     NULL #null
     */
    @Override
    public Node visitNull(NullContext c) {
        if (print) printVarAndProdName(c);
        Node n = new EmptyNode();
        n.setLine(c.NULL().getSymbol().getLine());
        return n;
    }

    /**
     ID DOT ID LPAR (exp (COMMA exp)* )? RPAR #dotCall
     */
    @Override
    public Node visitDotCall(DotCallContext c) {
        if (print) printVarAndProdName(c);
        List<Node> argList = new ArrayList<>();
        for (ExpContext e : c.exp()) {
            argList.add(visit(e));
        }
        // ID(0) is the object, ID(1) is the method
        ClassCallNode n = new ClassCallNode(c.ID(0).getText(), c.ID(1).getText(), argList);
        n.setLine(c.ID(0).getSymbol().getLine());
        return n;
    }

    public Node visitIdType(IdTypeContext c) {
        if (print) printVarAndProdName(c);
        Node n = new RefTypeNode(c.ID().getText());
        n.setLine(c.ID().getSymbol().getLine());
        return n;
    }
}
