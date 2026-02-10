# FOOL Compiler
**A Functional and Object-Oriented Language Compiler**

## Academic Context
This project was developed for the **Linguaggi, Compilatori e Modelli Computazionali** course, part of the Laurea Magistrale in **Ingegneria Informatica** at the **Università di Bologna (UNIBO)**.

## Core Features
* **Functional Logic:** Implements standard functional programming paradigms.
* **Object Orientation:** Capable of object-oriented programming (**without inheritance**).
* **Compiler Pipeline:** Includes lexing, parsing (via ANTLR), semantic analysis, and code generation.

## Requirements
* **JDK 21+**: Utilizes modern Java features.
* **ANTLR**: Specifically requires `antlr-runtime-4.13.2.jar`.

## Getting Started

### Recommended IDE
It is **heavily recommended** to use **IntelliJ IDEA**. The project structure is natively configured for IntelliJ, ensuring that paths and dependencies are recognized automatically upon opening the folder.

### Setup
1. Ensure your environment is running **JDK 21** or higher.
2. Add `antlr-runtime-4.13.2.jar` to your project libraries if it is not automatically detected.
3. **Generate Recognizers**: Use ANTLR to generate the lexer and parser code from the following grammar files:
    * `src/compiler/FOOL.g4`
    * `src/svm/SVM.g4`
    * `src/visualsvm/SVM.g4`
5. **Run**: Launch the compiler by running the main method in `src/compiler/Test.java`.