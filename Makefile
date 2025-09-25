SERVER_OUT = ./server/out
SERVER_FILES = ./server/*.java
SERVER_MF = manifest.mf
SERVER_JAR = server.jar

CLIENT_OUT = ./client/out
CLIENT_FILES = ./client/*.java
CLIENT_MF = manifest.mf
CLIENT_JAR = client.jar

compile-server:
	javac -d $(SERVER_OUT) $(SERVER_FILES)
	cd $(SERVER_OUT) && jar cvfm ../../$(SERVER_JAR) ../$(SERVER_MF) *.class


compile-client:
	javac -d $(CLIENT_OUT) $(CLIENT_FILES)
	cd $(CLIENT_OUT) && jar cvfm ../../$(CLIENT_JAR) ../$(CLIENT_MF) *.class

server: compile-server
	@echo 
	@java -jar $(SERVER_JAR)

client: compile-client
	@echo 
	@java -jar $(CLIENT_JAR)
