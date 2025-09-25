CLIENT_OUT = ./out
CLIENT_FILES = *.java
CLIENT_MF = manifest.mf
CLIENT_JAR = client.jar

compile:
	javac -d $(CLIENT_OUT) $(CLIENT_FILES)
	@cd $(CLIENT_OUT) && jar cvfm ../$(CLIENT_JAR) ../$(CLIENT_MF) *.class

client: compile
	@echo 
	@java -jar $(CLIENT_JAR)

