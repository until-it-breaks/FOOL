package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;
import visualsvm.ExecuteVM;
import java.util.ArrayList;
import java.util.List;
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
				"cfp", // imposta $fp al valore di $sp
				"lra", // carica il valore di $ra
				declCode, // genera codice per dichiarazioni locali (usano il nuovo $fp!!!)
				visit(n.exp), // genera codice per l'espressione corpo della funzione
				"stm", // imposta $tm al valore poppato (risultato della funzione)
				popDecl, // rimuove dichiarazioni locali dallo stack
				"sra", // imposta $ra al valore poppato
				"pop", // rimuove Access Link dallo stack
				popParl, // rimuove parametri dallo stack
				"sfp", // imposta $fp al valore poppato (Control Link)
				"ltm", // carica il valore di $tm (risultato della funzione)
				"lra", // carica il valore di $ra
				"js"  // salta all'indirizzo poppato
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

        if (n.entry.offset < 0) {
            return nlJoin(
                    "lfp", 		    // carica Control Link (puntatore al frame del chiamante della funzione "id")
                    argCode, 				// genera codice per espressioni argomento in ordine inverso
                    "lfp", 					// recupera indirizzo del frame contenente dichiarazione di "id"
                    getAR, 					// seguendo la catena statica (degli Access Link)
                    "stm", 					// imposta $tm al valore poppato (con lo scopo di duplicare il top dello stack)
                    "ltm", 					// carica Access Link (puntatore al frame della dichiarazione della funzione "id")
                    "ltm", 					// duplica il top dello stack
                    "push "+n.entry.offset, // pusha l'offset del metodo
                    "add", 					// calcola indirizzo della dichiarazione di "id"
                    "lw", 					// carica indirizzo della funzione "id"
                    "js"  					// salta all'indirizzo poppato (salvando indirizzo dell'istruzione successiva in $ra)
            );
        } else {	// caso metodo
            return nlJoin(
                    "lfp", 		    // carica Control Link (puntatore al frame del chiamante della funzione "id")
                    argCode, 				// genera codice per espressioni argomento in ordine inverso
                    "lfp", 					// recupera indirizzo del frame contenente dichiarazione di "id"
                    getAR, 					// seguendo la catena statica (degli Access Link)
                    "stm", 					// imposta $tm al valore poppato (con lo scopo di duplicare il top dello stack)
                    "ltm", 					// carica Access Link (puntatore al frame della dichiarazione della funzione "id")
                    "ltm", 					// duplica il top dello stack
                    "lw",					// carica in cima allo stack il dispatch pointer
                    "push "+n.entry.offset, // pusha l'offset del metodo
                    "add", 					// calcola indirizzo della dichiarazione di "id"
                    "lw", 					// carica indirizzo della funzione "id"
                    "js"  					// salta all'indirizzo poppato (salvando indirizzo dell'istruzione successiva in $ra)
            );
        }
	}

	@Override
	public String visitNode(IdNode n) {
		if (print) printNode(n,n.id);
		String getAR = null;
		for (int i = 0;i<n.nl-n.entry.nl;i++) getAR=nlJoin(getAR,"lw");
		return nlJoin(
			"lfp", getAR, // recupera indirizzo del frame contenente dichiarazione di "id"
			              // seguendo la catena statica (degli Access Link)
			"push "+n.entry.offset, "add", // calcola indirizzo della dichiarazione di "id"
			"lw" // carica valore della variabile "id"
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
            visit(n.left),              // valuta operando sinistro e pusha sullo stack
            visit(n.right),             // valuta operando destro e pusha sullo stack
            "sub"                       // sottrae: pop right, pop left, pusha risultato (left - right)
        );
    }

    @Override
    public String visitNode(DivNode n) {
        if (print) printNode(n);
        return nlJoin(
            visit(n.left),              // valuta operando sinistro e pusha sullo stack
            visit(n.right),             // valuta operando destro e pusha sullo stack
            "div"                       // divide: pop right, pop left, pusha il risultato (left / right)
        );
    }

    @Override
    public String visitNode(NotNode n) {
        String lTrue = freshLabel();
        String lEnd = freshLabel();
        return nlJoin(
                visit(n.exp),
                "push 0",
                "beq " + lTrue, // se è 0 (false), diventa 1 (true)
                "push 0",       // se era 1 (true), diventa 0 (false)
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
            visit(n.left),              // valuta operando sinistro e pusha sullo stack
            visit(n.right),             // valuta operando destro e pusha sullo stack
            "bleq " + l1,               // salta se left <= right: pop right, pop left, se condizione vera salta a l1
            "push 0",                   // condizione falsa (left > right)
            "b " + l2,                  // salta incondizionatamente all'uscita
            l1 + ":",                   // etichetta caso vero
            "push 1",                   // condizione vera (left <= right)
            l2 + ":"                    // etichetta di terminazione per pushare sullo stack il risultato
        );
    }

    @Override
    public String visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();       // etichetta per il caso "vero"
        String l2 = freshLabel();       // etichetta per terminare l'operazione
        return nlJoin(
            visit(n.right),             // valuta operando destro (invertiamo l'ordine rispetto a LEQ) e pusha sullo stack
            visit(n.left),              // valuta operando sinistro e pusha sullo stack
            "bleq " + l1,               // salta se right <= left (cioè left >= right): pop left, pop right, se condizione vera salta a l1
            "push 0",                   // condizione falsa (left < right)
            "b " + l2,                  // salta incondizionatamente all'uscita
            l1 + ":",                   // etichetta caso vero
            "push 1",                   // condizione vera (left >= right)
            l2 + ":"                    // etichetta di terminazione per pushare sullo stack il risultato
        );
    }

    @Override
    public String visitNode(AndNode n) {
        if (print) printNode(n);
        return nlJoin(
            visit(n.left),              // valuta operando sinistro e pusha sullo stack
            visit(n.right),             // valuta operando destro e pusha sullo stack
            "mult"                      // moltiplica: pop right, pop left, pusha risultato (left * right)
                                        // risultato: 1 solo se entrambi 1
        );
    }

    @Override
    public String visitNode(OrNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();        // etichetta per il caso vero
        String l2 = freshLabel();        // etichetta per terminare l'operazione
        return nlJoin(
            visit(n.left),               // valuta operando sinistro e pusha sullo stack
            "push 1",                    // pusha 1 sullo stack per il confronto
            "beq " + l1,                 // se left == 1, salta a l1 (già vero, salta right): pop 1, pop left, se uguali salta
            visit(n.right),              // left era 0, valuta operando destro e pusha sullo stack
            "push 1",                    // pusha 1 sullo stack per il confronto
            "beq " + l1,                 // se right == 1, salta a l1 (vero): pop 1, pop right, se uguali salta
            "push 0",                    // entrambi erano 0, allora pusha 0 (falso)
            "b " + l2,                   // salta all'uscita
            l1 + ":",                    // etichetta caso vero
            "push 1",                    // condizione vera (almeno uno era vero)
            l2 + ":"                     // etichetta di terminazione per pushare sullo stack il risultato
        );
    }

    @Override
    public String visitNode(MethodNode n) {
        if (print) printNode(n);

        String declCode = null, popDecl = null, popParl = null;
        for (Node dec : n.declist) {
            declCode = nlJoin(declCode, visit(dec));
            popDecl = nlJoin(popDecl, "pop");
        }
        for (int i = 0; i < n.parlist.size(); i++) popParl = nlJoin(popParl, "pop");
        String label = freshFunLabel();
        n.label = label;
        putCode(
                nlJoin(
                        label + ":",
                        "cfp", // imposta $fp al valore di $sp
                        "lra", // carica il valore di $ra
                        declCode, // genera codice per dichiarazioni locali (usano il nuovo $fp!!!)
                        visit(n.exp), // genera codice per l'espressione corpo della funzione
                        "stm", // imposta $tm al valore poppato (risultato della funzione)
                        popDecl, // rimuove dichiarazioni locali dallo stack
                        "sra", // imposta $ra al valore poppato
                        "pop", // rimuove Access Link dallo stack
                        popParl, // rimuove parametri dallo stack
                        "sfp", // imposta $fp al valore poppato (Control Link)
                        "ltm", // carica il valore di $tm (risultato della funzione)
                        "lra", // carica il valore di $ra
                        "js"  // salta all'indirizzo poppato
                )
        );
        return "";
    }

    @Override
    public String visitNode(ClassNode n) {
        List<String> dispatchTable = new ArrayList<>();

        for (MethodNode dec : n.methodList) {
            visit(dec);
            dispatchTable.add(dec.offset, dec.label);
        }

        String pushCode = "";
        for (String method : dispatchTable) {
            pushCode = nlJoin(
                    pushCode,
                    "push " + method,
                    "lhp",
                    "sw",
                    "lhp",
                    "push 1",
                    "add",
                    "shp"
            );
        }

        return nlJoin(
                "lhp",
                pushCode
        );
    }

    @Override
    public String visitNode(EmptyNode n) {
        return nlJoin(
                "push -1"
        );
    }

    @Override
    public String visitNode(ClassCallNode n) {
        String argCode = null, getAR = null;
        for (int i = n.arglist.size() - 1; i >= 0; i--) argCode = nlJoin(argCode, visit(n.arglist.get(i)));
        for (int i = 0; i < n.nl - n.entry.nl; i++) getAR = nlJoin(getAR, "lw");

        return nlJoin(
                "lfp",
                argCode, // genera codice per espressioni argomento in ordine inverso
                "lfp",
                getAR, // recupera indirizzo del frame contenente la dichiarazione di ID1
                "push " + n.entry.offset, "add", // calcola indirizzo della dichiarazione di ID1
                "lw", // carica puntatore oggetto (ID1)
                "stm", // imposta $tm al valore poppato (puntatore oggetto)
                "ltm", // carica Access Link (puntatore oggetto)
                "ltm", // duplica il top dello stack (puntatore oggetto)
                "lw", // carica valore sullo stack dalla memoria
                "push " + n.methodEntry.offset, "add", // calcola indirizzo del metodo nella dispatch table
                "lw", // carica indirizzo del metodo
                "js"  // salta all'indirizzo poppato (salvando indirizzo dell'istruzione successiva in $ra)
        );
    }

    @Override
    public String visitNode(NewNode n) {
        String argCode = null;
        for (Node arg: n.arglist) argCode = nlJoin(argCode, visit(arg));
        // memorizza argomenti nell'heap e incrementa heap pointer
        for (int i = 0; i < n.arglist.size(); i++) {
            argCode = nlJoin(
                    argCode,
                    "lhp", // carica heap pointer
                    "sw",  // memorizza word all'heap pointer
                    "lhp", // carica heap pointer
                    "push 1",
                    "add", // incrementa heap pointer
                    "shp"  // memorizza heap pointer aggiornato
            );
        }

        int address = ExecuteVM.MEMSIZE + n.entry.offset;
        return nlJoin(
                argCode,
                "push " + address,	// carica sullo stack l'indirizzo
                "lw", 				// mette sullo stack il valore in 'address' dalla memoria
                "lhp", 				// carica sullo stack il valore di hp (come indirizzo dispatch pointer)
                "sw", 				// memorizza all'indirizzo 'hp' il dispatch pointer
                "lhp", 				// carica sullo stack il valore di hp
                "lhp",				// carica hp con lo scopo di incrementarlo
                "push 1",			// pusha 1
                "add",				// calcola nuovo valore di hp
                "shp"				// poppa il nuovo valore e lo mette in hp
        );
    }
}