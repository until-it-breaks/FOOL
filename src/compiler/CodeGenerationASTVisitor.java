package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static svm.ExecuteVM.MEMSIZE;
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
		String argCode = null, getAR = null, retJoin = null;;
		for (int i=n.arglist.size()-1;i>=0;i--) argCode=nlJoin(argCode,visit(n.arglist.get(i)));
		for (int i = 0;i<n.nl-n.entry.nl;i++) getAR=nlJoin(getAR,"lw");

        retJoin = nlJoin("lfp", // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                "lfp", getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (pointer to frame of function "id" declaration)
                "ltm" // duplicate top of stack
        );
        if (n.entry.offset < 0) {
            retJoin = nlJoin(retJoin,
                    "push "+n.entry.offset, "add", // compute address of "id" declaration
                    "lw", // load address of "id" function
                    "js"  // jump to popped address (saving address of subsequent instruction in $ra)
            );
        } else {
            retJoin = nlJoin(retJoin,
                    "lw",
                    "push "+n.entry.offset, "add",
                    "lw",
                    "js"  // jump to popped address (saving address of subsequent instruction in $ra
            );
        }
        return retJoin;
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

    @Override
    public String visitNode(MinusNode n) {
        if (print) printNode(n);
        return nlJoin(
            visit(n.left),              // valuta operando sinistro e pusho sullo stack
            visit(n.right),             // valuta operando destro e pusho sullo stack
            "sub"                       // sottrae: pop right, pop left, push risultato (left - right)
        );
    }

    @Override
    public String visitNode(DivNode n) {
        if (print) printNode(n);
        return nlJoin(
            visit(n.left),              // valuta operando sinistro e pusho sullo stack
            visit(n.right),             // valuta operando destro e pusho sullo stack
            "div"                       // divide: pop right, pop left, pusho il risultato (left / right)
        );
    }

    @Override
    public String visitNode(NotNode n) {
        String lTrue = freshLabel();
        String lEnd = freshLabel();
        return nlJoin(
                visit(n.exp),
                "push 0",
                "beq " + lTrue, // Se è 0 (false), diventa 1 (true)
                "push 0",       // Se era 1 (true), diventa 0 (false)
                "b " + lEnd,
                lTrue + ":",
                "push 1",
                lEnd + ":"
        );
    }

    @Override
    public String visitNode(LessEqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();       // etichetta per il caso "vero"
        String l2 = freshLabel();       // etichetta per terminare operazione
        return nlJoin(
            visit(n.left),              // valuta operando sinistro e pusho sullo stack
            visit(n.right),             // valuta operando destro e pusho sullo stack
            "bleq " + l1,               // branch if left <= right: pop right, pop left, se cond vera salta a l1
            "push 0",                   // cond falsa (left > right)
            "b " + l2,                  // salta incondizionatamente all'uscita
            l1 + ":",                   // etichetta caso vero
            "push 1",                   // cond vera (left <= right)
            l2 + ":"                    // etichetta di terminazione per pushare sullo stack il risultato
        );
    }

    @Override
    public String visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();       // etichetta per il caso "vero"
        String l2 = freshLabel();       // etichetta per terminare l'operazione
        return nlJoin(
            visit(n.right),             // valuta operando destro (invertiamo l'ordine rispetto a LEQ) e pusho sullo stack
            visit(n.left),              // valuta operando sinistro e pusho sullo stack
            "bleq " + l1,               // branch if right <= left (cioè left >= right): pop left, pop right, se cond vera salta a l1
            "push 0",                   // cond falsa (left < right)
            "b " + l2,                  // salta incondizionatamente all'uscita
            l1 + ":",                   // etichetta caso vero
            "push 1",                   // cond vera (left >= right)risultato
            l2 + ":"                    // etichetta di terminazione per pushare sullo stack il
        );
    }

    @Override
    public String visitNode(AndNode n) {
        if (print) printNode(n);
        return nlJoin(
            visit(n.left),              // valuta operando sinistro e pusho sullo stack
            visit(n.right),             // valuta operando destro e pusho sullo stack
            "mult"                      // moltiplica: pop right, pop left, push risultato (left * right)
                                        //        risultato: 1 solo se entrambi 1
        );
    }

    @Override
    public String visitNode(OrNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();        // etichetta per il caso vero
        String l2 = freshLabel();        // etichetta per terminare l'operazione
        return nlJoin(
            visit(n.left),               // valuta operando sinistro e pusho sullo stack
            "push 1",                    // pusho 1 sullo stack per il confronto
            "beq " + l1,                 // se left == 1, salta a l1 (già vero, skip right): pop 1, pop left, se uguali salta
            visit(n.right),              // left era 0, valuta operando destro e pusho sullo stack
            "push 1",                    // push 1 sullo stack per il confronto
            "beq " + l1,                 // se right == 1, salta a l1 (vero): pop 1, pop right, se uguali salta
            "push 0",                    // entrambi erano 0, allora pusho 0 (falso)
            "b " + l2,                   // salta all'uscita
            l1 + ":",                    // etichetta caso vero
            "push 1",                    // cond vera (almeno uno era vero)
            l2 + ":"                     // etichetta di terminazione per pushare sullo stack il risultato
        );
    }

    @Override
    public String visitNode(ClassNode n) {
        if (print) printNode(n, n.id);
        List<String> dispatchTable = new ArrayList<>();
        String decClass = null;
        for (MethodNode meth : n.methodList) {
            visit(meth);
            String label = meth.label;
            int offset = meth.offset;
            dispatchTable.add(offset, label);
        }
        decClass = nlJoin(decClass, "lhp");
        for (String dt : dispatchTable) {
            decClass = nlJoin(decClass,
                    "push " + dt,
                    "lhp",
                    "sw",
                    "lhp",
                    "push 1",
                    "add",
                    "shp"
            );
        }
        return decClass;
    }

    @Override
    public String visitNode(MethodNode n) {
        if (print) printNode(n, n.id);
        n.label = freshFunLabel();
        String declCode = null, popDecl = null, popParl = null;
        for (Node dec : n.declist) {
            declCode = nlJoin(declCode,visit(dec));
            popDecl = nlJoin(popDecl,"pop");
        }
        for (int i=0;i<n.parlist.size();i++) popParl = nlJoin(popParl,"pop");
        putCode(
                nlJoin(
                        n.label+":",
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
                        "js"  // jump to popped address
                )
        );
        return null;
    }

    @Override
    public String visitNode(EmptyNode n) {
        if (print) printNode(n);
        return "push -1";
    }

    @Override
    public String visitNode(ClassCallNode n) {
        if (print) printNode(n, n.id1 + " " + n.id2);
        String argCode = null, getAR = null;
        for (int i=n.arglist.size()-1;i>=0;i--) argCode=nlJoin(argCode,visit(n.arglist.get(i)));
        for (int i = 0;i<n.nl-n.entry.nl;i++) getAR=nlJoin(getAR,"lw");
        return nlJoin(
                "lfp", // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                "lfp", getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "push "+n.entry.offset,
                "add",
                "lw",
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (pointer to frame of function "id" declaration)
                "ltm", // duplicate top of stack
                "lw",
                "push "+n.methodEntry.offset, "add", // compute address of "id" declaration
                "lw", // load address of "id" function
                "js"  // jump to popped address (saving address of subsequent instruction in $ra)
        );
    }

    @Override
    public String visitNode(NewNode n) {
        if (print) printNode(n, n.id);
        String retCode = null;
        for (Node nd : n.arglist) { retCode = nlJoin(retCode, visit(nd)); }
        for (int i = 0; i < n.arglist.size(); i++) {
            retCode = nlJoin(retCode,
                    "lhp",
                    "sw",
                    "lhp",
                    "push 1",
                    "add",
                    "shp");
        }
        int off = MEMSIZE + n.entry.offset;
        retCode = nlJoin(retCode,
                "push " + off,
                "lw",
                "lhp",
                "sw",
                "lhp",
                "lhp",
                "push 1",
                "add",
                "shp");
        return retCode;
    }
}