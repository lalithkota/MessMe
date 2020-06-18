#include<iostream>
#include<fstream>
#include<vector>
#include<unistd.h>
#include<stdlib.h>

using namespace std;

int VIDEO_HEIGHT = 480;
int VIDEO_WIDTH = 640;

int noof_images=0;

extern "C" int call_vid();
extern "C" void* video_call_func1(void*);
extern "C" void* video_call_func2(void*);

void* video_call_func1(void * dump){

}

void* video_call_func2(void* dump){

}
