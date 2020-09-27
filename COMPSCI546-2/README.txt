Steps I followed to build and run this code:

I used maven and IntelliJ to develop this project. IntelliJ allows you to right-click on a project and choose to build the project after which it automatically downloads dependencies and starts the maven build after detecting the pom.xml file
Most IDEs with maven support should let you build the project through the UI itself

In order to run the programs that require command line parameter inputs, most IDE's should let you configure program arguments ( On IntelliJ this can be found under Run > Edit Configurations > Program Arguments )
through a dialogue box at run time. Since compiling through the CLI (regular as well as maven CLI) was giving me issues (Because the CLI couldn't find the dependencies the way maven does), I used this feature from IntelliJ to pass in program arguments at runtime. 
Most IDEs should let you do this as well 

Runtime arguments for each program : 

1. IndexBuilder.java - args[0] = uncompressed
4. QueryRetrieval.java - args[0] = input file name, args[1] = output file name, args[2] = retrieval model ("vs"/ "bm25"/ "ql-jm"/ "ql-dir")

Steps to run the project :-

1. Run IndexBuilder.java to generate the index
2. Create a file with each query as "Q1: birds of a feather" for example
3. Run QueryRetrieval.java to perform retrieval on the query files against an index one at a time for each retrieval model 

All the files generated and written to disk as a part of this project can be found in the generated folder "COMPSCI546-2/filesWrittenToDisk" 