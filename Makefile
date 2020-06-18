all: clearbin server client_vid client_novid

vid: server client_vid

novid: server client_novid

clearbin:
	@rm -rf bin/*

server:
	@gcc src/server/server.c -o bin/server -lpthread
	@#gcc src/server/server2.c -o bin/server_vid -lpthread

client_novid:
	@g++ -o bin/client_novid src/client/client_novid.c -lpthread -lncurses -lpulse -lpulse-simple

client_vid:
	@#g++ --std=c++11 src/client/client.c src/client/videor.cpp -o bin/client_vid $(shell pkg-config --cflags opencv) $(shell pkg-config --libs opencv) -lpthread -lncurses -lpulse -lpulse-simple
	@g++ -c --std=c++17 src/client/videor.cpp -o bin/videor.o $(shell pkg-config --cflags opencv)
	@#g++ -c --std=c++17 src/client/videor2.cpp -o bin/videor.o
	@gcc -c src/client/client_vid.c -o bin/client_vid.o
	@g++ -o bin/client_vid bin/client_vid.o bin/videor.o -lpthread -lncurses -lpulse -lpulse-simple $(shell pkg-config --libs opencv)
	@rm -rf bin/client_vid.o bin/videor.o

temp:
	g++ tmp/writer.cpp -o tmp/writer.o #$(shell pkg-config --libs opencv)
	g++ tmp/reader.cpp -o tmp/reader.o #$(shell pkg-config --libs opencv)
