package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;
import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

  CodeGenerationASTVisitor() {}
  CodeGenerationASTVisitor(boolean debug) {super(false,debug);} //enables print for debugging

	@Override
	public String visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		String declCode = null;
		for (Node dec : n.declist) declCode=nlJoin(declCode,visit(dec));
		return nlJoin(
			"push 0",	
			declCode, // generate code for declarations (allocation)			
			visit(n.exp),
			"halt",
			getCode()
		);
	}

	@Override
	public String visitNode(ProgNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"halt"
		);
	}

	@Override
	public String visitNode(FunNode n) {
		if (print) printNode(n,n.id);
		String declCode = null, popDecl = null, popParl = null;
		for (Node dec : n.declist) {
			declCode = nlJoin(declCode,visit(dec));
			popDecl = nlJoin(popDecl,"pop");
		}
		for (int i=0;i<n.parlist.size();i++) popParl = nlJoin(popParl,"pop");
		String funl = freshFunLabel();
		putCode(
			nlJoin(
				funl+":",
				"cfp", // set $fp to $sp value
				"lra", // load $ra value
				declCode, // generate code for local declarations (they use the new $fp!!!)
				visit(n.exp), // generate code for function body expression
				"stm", // set $tm to popped value (function result)
				popDecl, // remove local declarations from stack
				"sra", // set $ra to popped value
				"pop", // remove Access Link from stack
				popParl, // remove parameters from stack
				"sfp", // set $fp to popped value (Control Link)
				"ltm", // load $tm value (function result)
				"lra", // load $ra value
				"js"  // jump to to popped address
			)
		);
		return "push "+funl;		
	}

	@Override
	public String visitNode(VarNode n) {
		if (print) printNode(n,n.id);
		return visit(n.exp);
	}

	@Override
	public String visitNode(PrintNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"print"
		);
	}

	@Override
	public String visitNode(IfNode n) {
		if (print) printNode(n);
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();		
		return nlJoin(
			visit(n.cond),
			"push 1",
			"beq "+l1,
			visit(n.el),
			"b "+l2,
			l1+":",
			visit(n.th),
			l2+":"
		);
	}

	@Override
	public String visitNode(EqualNode n) {
		if (print) printNode(n);
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"beq "+l1,
			"push 0",
			"b "+l2,
			l1+":",
			"push 1",
			l2+":"
		);
	}

	@Override
	public String visitNode(TimesNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"mult"
		);	
	}

	@Override
	public String visitNode(PlusNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"add"				
		);
	}

	@Override
	public String visitNode(CallNode n) {
		if (print) printNode(n,n.id);
		String argCode = null, getAR = null;
		for (int i=n.arglist.size()-1;i>=0;i--) argCode=nlJoin(argCode,visit(n.arglist.get(i)));
		for (int i = 0;i<n.nl-n.entry.nl;i++) getAR=nlJoin(getAR,"lw");
		return nlJoin(
			"lfp", // load Control Link (pointer to frame of function "id" caller)
			argCode, // generate code for argument expressions in reversed order
			"lfp", getAR, // retrieve address of frame containing "id" declaration
                          // by following the static chain (of Access Links)
            "stm", // set $tm to popped value (with the aim of duplicating top of stack)
            "ltm", // load Access Link (pointer to frame of function "id" declaration)
            "ltm", // duplicate top of stack
            "push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw", // load address of "id" function
            "js"  // jump to popped address (saving address of subsequent instruction in $ra)
		);
	}

	@Override
	public String visitNode(IdNode n) {
		if (print) printNode(n,n.id);
		String getAR = null;
		for (int i = 0;i<n.nl-n.entry.nl;i++) getAR=nlJoin(getAR,"lw");
		return nlJoin(
			"lfp", getAR, // retrieve address of frame containing "id" declaration
			              // by following the static chain (of Access Links)
			"push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw" // load value of "id" variable
		);
	}

	@Override
	public String visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+(n.val?1:0);
	}

	@Override
	public String visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+n.val;
	}

    // New Operators

    @Override
    public String visitNode(LessEqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
            visit(n.left),
            visit(n.right),
            "bleq " + l1,       // Checks if LEFT <= RIGHT, if true go to l1
            "push 0",           // LEFT isn't <= RIGHT, push false (0)
            "b " + l2,          // LEFT isn't <= RIGHT, go to l2
            l1 + ":",
            "push 1",           // LEFT is <= RIGHT, push true (1)
            l2 + ":"
        );
    }

    @Override
    public String visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
            visit(n.right),
            visit(n.left),
            "bleq " + l1,       // Checks if LEFT >= RIGHT, if true go to l1
            "push 0",           // LEFT isn't >= RIGHT, push false (0)
            "b " + l2,          // LEFT isn't >= RIGHT, go to l2
            l1 + ":",
            "push 1",           // LEFT is >= RIGHT, push true (1)
            l2 + ":"
        );
    }

    @Override
    public String visitNode(OrNode n) {
        if (print) printNode(n);
        String lTrue = freshLabel();
        String lEnd = freshLabel();
        return nlJoin(
            visit(n.left),
            "push 1",
            "beq " + lTrue,     // Pops LEFT and 1 (true), if beq evaluates to true, then LEFT has to be true and we go to lTrue
            visit(n.right),
            "push 1",
            "beq " + lTrue,     // Pops RIGHT and 1 (true), if beq evaluates to true, then RIGHT has to be true and we go to lTrue
            "push 0",           // None of LEFT or RIGHT was true, therefore OR evaluates to 0 (false)
            "b " + lEnd,        // We go to lEnd and do nothing else
            lTrue + ":",
            "push 1",           // Either LEFT or RIGHT was true, and that's enough for an OR to return 1 (true)
            lEnd + ":"
        );
    }

    @Override
    public String visitNode(AndNode n) {
        if (print) printNode(n);
        String lFalse = freshLabel();
        String lEnd = freshLabel();
        return nlJoin(
            visit(n.left),
            "push 0",
            "beq " + lFalse,    // Pops LEFT and 0 (false), if beq evaluates to true, then LEFT has to be false and we go to lFalse
            visit(n.right),
            "push 0",
            "beq " + lFalse,    // Pops RIGHT and 0 (false), if beq evaluates to true, then RIGHT has to be true and we go to lFalse
            "push 1",           // None of LEFT or RIGHT was false, therefore both are true and AND evaluates to 1 (true)
            "b " + lEnd,        // We go to lEnd and do nothing else
            lFalse + ":",
            "push 0",           // Either LEFT or RIGHT was false, and that's enough for an AND to return 0 (false)
            lEnd + ":"
        );
    }

    @Override
    public String visitNode(DivNode n) {
        if (print) printNode(n);
        return nlJoin(
            visit(n.left),
            visit(n.right),
            "div"
        );
    }

    @Override
    public String visitNode(MinusNode n) {
        if (print) printNode(n);
        return nlJoin(
            visit(n.left),
            visit(n.right),
            "sub"
        );
    }

    @Override
    public String visitNode(NotNode n) {
        if (print) printNode(n);
        String lTrue = freshLabel();
        String lEnd = freshLabel();
        return nlJoin(
            visit(n.exp),
            "push 1",
            "beq " + lTrue,     // Check if 'exp' equals to true, if so go to 'lTrue'
            "push 1",           // Otherwise 'exp' has to be false and we push the opposite, that is 'true' (1)
            "b " + lEnd,        // There is nothing else to do and we go to 'lEnd'
            lTrue + ":",
            "push 0",           // Since 'exp' is true, we push the opposite, that is 'false' (0)
            lEnd + ":"
        );
    }
}