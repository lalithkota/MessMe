#include<opencv2/opencv.hpp>
#include<iostream>
#include<fstream>
#include<vector>
#include<unistd.h>
#include<stdlib.h>

using namespace cv;
using namespace std;

int VIDEO_HEIGHT = 480;
int VIDEO_WIDTH = 640;

int noof_images=0;

// 640*480*1.5 YUV-NV21 style data

extern "C" int call_vid();
extern "C" void* video_call_func1(void*);
extern "C" void* video_call_func2(void*);

void yv12_to_bgr(int width, int height, uchar* input, uchar* output);
void copy_bgr_to_arr(uchar* output, uchar* bgr, int index);
uchar* yuv_to_bgr(uchar y, uchar u, uchar v, uchar* bgr);
int read_jpeg_bytewise(int sd, int total_buff_size, uchar* dest, int* final_jpeg_size);

int call_vid(){
	VideoCapture camera(0);
	if(!camera.isOpened()){
		cout<<"Cant open"<<endl;
		return 10;
	}
	namedWindow("main");
	Mat frame;
	for(;;){
		camera >> frame;
		imshow("main",frame);
		if(waitKey(25) == 'q'){
			break;
		}
		printf("%lu\n",frame.total()*frame.elemSize());
	}
	destroyAllWindows();
	//cout<<"Released Camera"<<endl;
	return 0;
}

int get_1k_term_buffer(int sd, unsigned char* final_buff, int total_size){
	int i=0;
	uchar small_buff[1024];
	for(i=0;i<total_size/1024;i++){
		if(read(sd,small_buff,sizeof(small_buff))<=0){
			memcpy(&final_buff[i*1024],small_buff,sizeof(small_buff));
			return -1;
		}
		memcpy(&final_buff[i*1024],small_buff,sizeof(small_buff));
	}
	return total_size;
}

void* video_call_func1(void * dump){
	int** arg_dump=(int**)dump;
  int* call_server_sd = arg_dump[0];
	int* video_call_func1_close_flag = arg_dump[1];
	int* video_call_func2_close_flag = arg_dump[2];

  uchar buff[40960];
	int my_image_length=0;
	//uchar image_length_arr[4];
	//cout<<"Buff size: "<<sizeof(buff)<<endl;
	// namedWindow("other");
  while(true){
		// int temp1 = read_jpeg_bytewise(call_server_sd, sizeof(buff), buff, &my_image_length);
		// if(temp1!=0){
		// 	if(temp1==1){
		// 		cout<<"The other side quit"<<endl;
		// 		break;
		// 	}
		// 	else if(temp1==2){
		// 		cout<<"No proper Header found"<<endl;
		// 		break;
		// 	}
		// 	else if(temp1==3){
		// 		cout<<"No Proper footer found"<<endl;
		// 		break;
		// 	}
		// }
		// else{
		// 	cout<<"reeived proper image"<<endl;
		// }

		// first read the image size
		// if(read(call_server_sd, image_length_arr, 4)<=0){
		// 	break;
		// }
		// image_length=0;
		// for(int i=0;i<4;i++) image_length |= (((int)image_length_arr[i])<<(8*i));
		// //image_length=*((int*)image_length_arr);
		// cout<<"\njpeg size:"<<image_length<<endl;

		// then read the actual jpeg image into buffer
		if(*video_call_func2_close_flag==1){
      break;
    }
		if(read(*call_server_sd,buff,sizeof(buff))<=0){
			break;
		}
		// if(read(*call_server_sd, buff, sizeof(buff))<=0){
    //   break;
    // }

		// check the header and display right or wrong .. doest change the outcome
		// if(buff[0]==0xFF && buff[1]==0xD8 && buff[2]==0xFF){
		// 	cout<<"correct start-";
		// 	if(buff[image_length-2]==0xFF && buff[image_length-1]==0xD9){
		// 		cout<<"Corrent end";
		// 	}
		// 	else{
		// 		cout<<"bad footer";
		// 	}
		// 	cout<<endl;
		// }
		// else{
		// 	cout<<"Improper header footers"<<endl;
		// }

		// decompress the image (image in yuv format) and imshow it.
		// uchar* bgr_arr = (uchar*)malloc(VIDEO_WIDTH*VIDEO_HEIGHT*3*sizeof(uchar));
		// yv12_to_bgr(VIDEO_WIDTH, VIDEO_HEIGHT, buff, bgr_arr);
		// Mat bgr(VIDEO_HEIGHT, VIDEO_WIDTH, CV_8UC3, bgr_arr);

		// decompress the image (image in jpeg) and imshow it.
		if(noof_images<10){
			ofstream outputjpeg("media/fromoutside"+to_string(noof_images)+".jpeg");
			outputjpeg.write((char*)buff,sizeof(buff));
			noof_images++;
		}
		vector<uchar> encoded_vec(buff, buff + sizeof(buff)/sizeof(buff[0]));// my_image_length);
		cout<<"Size of encoded_vec:"<<encoded_vec.size()<<endl;
		Mat decoded_mat = imdecode(encoded_vec,CV_LOAD_IMAGE_COLOR);
		if(decoded_mat.rows>0 && decoded_mat.cols>0){
			cout<<"The shape of the final mat: "<<decoded_mat.size()<<endl;
		}
		else{
			cout<<"Quitting because bad file size."<<endl;
			continue;
		}
		imshow("other-side",decoded_mat);
		waitKey(20);
  }

  *video_call_func1_close_flag = 1;
  return NULL;
}

void* video_call_func2(void* dump){
	int** arg_dump=(int**)dump;
  int* call_server_sd = arg_dump[0];
	int* video_call_func1_close_flag = arg_dump[1];
	int* video_call_func2_close_flag = arg_dump[2];

	unsigned char buff[40960];
	VideoCapture camera(0);
	while(true){
		vector<uchar> buff_vector(40960,0);
		Mat frame;
		camera>>frame;
		imencode(".jpeg",frame,buff_vector);

    if(*video_call_func1_close_flag==1){
      break;
    }
		if(write(*call_server_sd,buff_vector.data(),buff_vector.size())<=0){
			break;
		}
		imshow("myself",frame);
		waitKey(30);

  }
  *video_call_func2_close_flag = 1;
  return NULL;
}

int read_jpeg_bytewise(int sd, int total_buff_size, uchar* dest, int* final_jpeg_size){
	uchar buff1,buff2,buff3;
	int my_jpeg_size=0;
	bool footer_first_part_rec = false;
	if(read(sd,&buff1,1)>0){
		return 1;
	}
	if(read(sd,&buff2,1)>0){
		return 1;
	}
	if(read(sd,&buff3,1)>0){
		return 1;
	}
	if(buff1!=0xFF || buff2!=0xD8 || buff3!=0xFF){
		// no proper header received
		return 2;
	}
	*(dest++)=buff1;
	*(dest++)=buff2;
	*(dest++)=buff3;
	my_jpeg_size=3;
	while(true){
		if(my_jpeg_size==total_buff_size){
			//no proper footer received
			return 3;
		}

		if(read(sd,&buff1,1)<=0){
			return 1;
		}
		*(dest++)=buff1;
		my_jpeg_size++;
		if(footer_first_part_rec){
			if(buff1==0xD9){
				break;
			}
			else{
				footer_first_part_rec=false;
			}
		}
		else{
			if(buff1==0xFF){
				footer_first_part_rec = true;
			}
		}
	}
	for(int i=0;i<total_buff_size-my_jpeg_size;i++){
		if(read(sd,&buff1,1)<=0){
			return 1;
		}
		*(dest++)=buff1;
	}

	*final_jpeg_size=my_jpeg_size;
	return 0;
}

void yv12_to_bgr(int width, int height, uchar* input, uchar* output){
	int rgb_size = width*height;
	int yuv_size = width*((int)(height/2))*3;
	//uchar* output= malloc(rgb_size*3*sizeof(uchar));

	uchar y1,y2,y3,y4, v, u;
	uchar bgr[3];
	for(int i=0;i<width; i+=2){
		for(int j=0;j<height/2; j+=2){
			y1 = input[(j+0)*width + i+0];
			y2 = input[(j+0)*width + i+1];
			y3 = input[(j+1)*width + i+0];
			y4 = input[(j+1)*width + i+1];

			v = input[rgb_size + (j*width)/2+i];
			u = input[rgb_size + rgb_size/4 + (j*width)/2+i];

			copy_bgr_to_arr(output, yuv_to_bgr(y1, u, v, bgr), (j+0)*width + i+0);
			copy_bgr_to_arr(output, yuv_to_bgr(y2, u, v, bgr), (j+0)*width + i+1);
			copy_bgr_to_arr(output, yuv_to_bgr(y3, u, v, bgr), (j+1)*width + i+0);
			copy_bgr_to_arr(output, yuv_to_bgr(y4, u, v, bgr), (j+1)*width + i+1);
		}
	}
}

void copy_bgr_to_arr(uchar* output,uchar* bgr, int index){
	output[3*index+0]=bgr[0];
	output[3*index+1]=bgr[1];
	output[3*index+0]=bgr[2];
}

uchar* yuv_to_bgr(uchar y, uchar u, uchar v, uchar* bgr){
		int c = y-16;
		int d = u-128;
		int e = v-128;
		int r_int = (298*c + 409*e + 128)>>8;
		int g_int = (298*c - 100*d - 208*e +128)>>8;
		int b_int = (298*c + 516*d + 128)>>8;

		uchar b,g,r;

		if(b_int < 0)b=0;
		else if(b_int > 255)b=255;
		else b=b_int;

		if(g_int < 0)g=0;
		else if(g_int > 255)g=255;
		else g=g_int;

		if(r_int < 0)r=0;
		else if(r_int > 255)r=255;
		else r=r_int;

		bgr[0]=b;
		bgr[1]=g;
		bgr[2]=r;
		return bgr;
}
