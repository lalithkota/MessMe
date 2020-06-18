#include<opencv2/opencv.hpp>
#include<iostream>

using namespace std;
using namespace cv;

int VIDEO_WIDTH = 640;
int VIDEO_HEIGHT= 480;
double FRAME_RATE = 24.0;
String VIDEO_NAME = "media/myFile.avi";
int VIDEO_CODEC = CV_FOURCC('X','V','I','D');

int main(){
  VideoCapture cap(0);
  //cap.set(CV_CAP_PROP_FRAME_WIDTH, VIDEO_WIDTH);
  //cap.set(CV_CAP_PROP_FRAME_WIDTH, VIDEO_HEIGHT);
  //cap.set(CV_CAP_PROP_FPS, FRAME_RATE);
  //cap.set(CV_CAP_PROP_FOURCC, VIDEO_CODEC);
  cout<<"Get width out: "<<cap.get(CV_CAP_PROP_FRAME_WIDTH)<<endl;
  cout<<"Get height out: "<<cap.get(CV_CAP_PROP_FRAME_HEIGHT)<<endl;
  cout<<"Get fps out: "<<cap.get(CV_CAP_PROP_FPS)<<endl;
  cout<<"Get fourcc out: "<<(cap.get(CV_CAP_PROP_FOURCC)==VIDEO_CODEC?"XVID":"Something else")<<endl;
  VideoWriter writer(VIDEO_NAME, VIDEO_CODEC, cap.get(CV_CAP_PROP_FPS), Size((int)cap.get(CV_CAP_PROP_FRAME_WIDTH),(int)cap.get(CV_CAP_PROP_FRAME_HEIGHT)));
  if(!cap.isOpened()){
    cout<<"Could not open camera"<<endl;
  }
  if(!writer.isOpened()){
    cout<<"could not open writer"<<endl;
  }
  Mat frame;
  for(;;){
    cap >> frame;
    if(frame.empty()){
      cout<<"Empty Frame"<<endl;
      continue;
    }
    writer<<frame;
    imshow("writer",frame);
    if(waitKey(1)==27){
        break;
    }
  }
  writer.release();
  cap.release();
  return 0;
}
