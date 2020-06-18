From lalithkota/messme_depend
ADD . /MessMe
RUN g++ /MessMe/videor.cpp $(pkg-config --cflags opencv4) $(pkg-config --libs opencv4) -o /MessMe/videor
