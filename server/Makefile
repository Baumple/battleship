SERVER_OUT = ./out
SERVER_FILES = *.java
SERVER_MF = manifest.mf
SERVER_JAR = server.jar

compile:
	javac -d $(SERVER_OUT) $(SERVER_FILES)
	@cd $(SERVER_OUT) && jar cvfm ../$(SERVER_JAR) ../$(SERVER_MF) *.class

server: compile
	@echo 
	@java -jar $(SERVER_JAR)
