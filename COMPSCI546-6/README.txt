Steps I followed to build and run this code:

I used maven and IntelliJ to develop this project. IntelliJ allows you to right-click on a project and choose to build the project after which it automatically downloads dependencies and starts the maven build after detecting the pom.xml file
Most IDEs with maven support should let you build the project through the UI itself

In order to run the programs that require command line parameter inputs, most IDE's should let you configure program arguments ( On IntelliJ this can be found under Run > Edit Configurations > Program Arguments )
through a dialogue box at run time. Since compiling through the CLI (regular as well as maven CLI) was giving me issues (Because the CLI couldn't find the dependencies the way maven does), I used this feature from IntelliJ to pass in program arguments at runtime. 
Most IDEs should let you do this as well 

Steps to run the project :-

1. Place the shakespeare-scenes.json file within ..java/index/index and Run IndexBuilder.java to generate the index
2. Run CreatePriors.java to create "uniform.prior" and "random.prior"
3. Create a file called Queries.txt with the query and place it in ..java/retrieval/apps
4. Run ExecutePriorQueries.java to perform retrieval on the query files with both priors
5. Provide args[0] = name of file with prior values, args[1] = name of output file to ExecutePriorQueries.java

