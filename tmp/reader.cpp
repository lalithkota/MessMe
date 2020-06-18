#include<opencv2/opencv.hpp>
#include<opencv2/videoio/videoio.hpp>
#include<iostream>

using namespace std;
using namespace cv;

int main(){
  VideoCapture cap;
  if(cap.open("media/myFile.avi",CAP_GSTREAMER)){
    cout<<"Using GSTREAMER "<<endl;
    return 4;
  }
  cout<<"VideoCapture stream backend: "<<cap.getBackendName()<<endl;
  if(!cap.isOpened()){
    cout<<"CAnt open"<<endl;
    return 1;
  }
  Mat frame;
  namedWindow("main");
  for(;;){
    cap >> frame;
    if(frame.empty()){
      cout<<"Empty frame"<<endl;
      continue;
    }
    imshow("main",frame);
    if(waitKey(33)=='q'){
      break;
    }
  }
  destroyAllWindows();
  return 0;
}
