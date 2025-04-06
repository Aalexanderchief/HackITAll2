# IntelliJ Plugin

A smart plugin for IntelliJ IDEA, created by a passionate team at HackItAll, that improves the productivity of Kotlin developers through a suite of actions and custom windows.

## What the plugin does

This plugin adds a number of innovative tools to your IDE, including:

### Parallel Universe Debugger
Simulates all possible branches of execution of a Kotlin function, without actually running it. Ideal for:
- Static debugging
- Understanding complex logic
- Generating automated test cases

This functionality is based on a text-based parser that analyzes the code of the selected function. We have built a parsing function that extracts the logical branches from a Kotlin function and transforms them into a symbolic decision tree.

Current process:
- A complete function is selected from the editor
- The code is analyzed line by line to identify if / else if / else branches
- Each branch is recorded as a logical constraint (e.g. user == null, age < 18 etc.)
- All possible paths are constructed through a symbolic execution (depth-first traversal)
- A list of ExecutionPaths is generated, each containing:
- the list of conditions (constraints)
- the final result of that branch (return, throw)

In addition, JUnit test case suggestions are automatically generated for each path:
- assertFailsWith<ExceptionType> for branches that throw exceptions
- assertEquals for those that return values

At this point, the parser is a POC (proof of concept), built to work for simple functions. However, it can be easily extended for:
- support for try/catch blocks
- nested branches and multiple functions
- automatic generation of test files or mocking code

✔ Select a function
✔ Right click → "Parallel Universe Debugger"
✔ Get a detailed report with all possible execution paths and output test suggestions

### GenerateTestsAction
Automatically generate JUnit test functions for selected Kotlin functions using AI.

✔ Select a Kotlin function in the editor
✔ Right click → "Generate Tests"
✔ Enter a short explanation: "What should this function do?"
✔ The plugin makes an API call to OpenAI for test case suggestions
✔ Automatically creates:
- a test folder (if it doesn't exist)
- a JUnit Kotlin file with the generated tests

How it works:
- We analyze the signature of the selected function (name, parameters, types)
- We ask for a text input: what should the function test?
- We send this context to OpenAI to generate relevant test cases
- We insert the code into a JUnit Kotlin class

The goal is to speed up the testing process, especially for pure functions and simple logic. This system is extensible and can support mocking, parameterized testing or complex exception checking in the future.

### Generate KDoc
Analyzes the selected method and generates KDoc documentation based on the signature and internal logic.

Right click → “Generate Kdoc type documentation”

How it works:
- Select a Kotlin function in the editor
- The plugin parses the function signature using regular expressions (regex):
- identifies the function name, parameters and return type
- Analyzes the function interior to detect:
- calls to other methods
- if checks, loops, returns, etc.
- For each component found, adds a semantic description in an auto-generated KDoc block

The goal is to ease the documentation process for Kotlin functions, especially in teams where documentation standards are important. In the future, it may also include support for generating KDoc for classes, properties or extensions.

### GenerateBoilerplateCodeAction (Speech To Code)
Transforms the spoken description of a function into Kotlin boilerplate code using Speech-to-Text + NLP.

✔ Tools → “Speech To Text”
✔ Generates the function skeleton directly from the voice

How it works:
- A Python script (included in the project) is called, which starts an audio recording session
- After the recording is complete, Python uses a Speech-to-Text library to convert the voice to text and saves the result in a .txt file
- The Kotlin plugin reads the generated file, extracts the voice command, and sends it to OpenAI
- OpenAI responds with a Kotlin boilerplate code snippet, generated based on the voice request
- The code is automatically inserted into the editor, at the current position

This functionality combines speech recognition with automatic code generation, accelerating prototyping and providing increased accessibility for developers.

✔ Tools → “Speech To Text”
✔ Generates the function skeleton directly from the voice

### ToolWindow: MascotView
Auxiliary ToolWindow (e.g. mascot, hints, animations, or future extensions).

## Install and run

1. Clone the project:
   bash
   git clone https://github.com/<your-name>/parallel-universe-intellij-plugin.git


2. Open project in IntelliJ IDEA
3. Run the Gradle task:
   bash
   ./gradlew runIde


4. In the test IDE, you can access the plugin functions from:
- Editor (right click on the code)
- Tools Menu
- ToolWindow (bottom, in the IDE)

## Technologies used
- IntelliJ Platform SDK
- Kotlin
- PSI Parsing (for static analysis)
- Terminal API (for running commands in the IDE terminal)
- Gradle

## Made at HackItAll 2024
Created with passion by our team to make life easier for Kotlin programmers.

---

🤖 Made with ☕, Kotlin and ❤ at HackItAll
