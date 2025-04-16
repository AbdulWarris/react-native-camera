#import <UIKit/UIKit.h>

#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
#import <GoogleMLKit/MLKFaceDetector.h>
#import <GoogleMLKit/MLKFace.h>
#import <GoogleMLKit/MLKVisionImage.h>
#else
@class MLKFaceDetector;
@class MLKFace; 
@class MLKVisionImage;
#endif

typedef NS_ENUM(NSInteger, RNFaceDetectionMode) {
    RNFaceDetectionFastMode = MLKFaceDetectorPerformanceModeFast,
    RNFaceDetectionAccurateMode = MLKFaceDetectorPerformanceModeAccurate
};

typedef NS_ENUM(NSInteger, RNFaceDetectionLandmarks) {
    RNFaceDetectAllLandmarks = MLKFaceDetectorLandmarkModeAll,
    RNFaceDetectNoLandmarks = MLKFaceDetectorLandmarkModeNone
};

typedef NS_ENUM(NSInteger, RNFaceDetectionClassifications) {
    RNFaceRunAllClassifications = MLKFaceDetectorClassificationModeAll,
    RNFaceRunNoClassifications = MLKFaceDetectorClassificationModeNone
};

@interface FaceDetectorManagerMlkit : NSObject
typedef void(^postRecognitionBlock)(NSArray *faces);

- (instancetype)init;

-(BOOL)isRealDetector;
-(void)setTracking:(id)json queue:(dispatch_queue_t)sessionQueue;
-(void)setLandmarksMode:(id)json queue:(dispatch_queue_t)sessionQueue;
-(void)setPerformanceMode:(id)json queue:(dispatch_queue_t)sessionQueue;
-(void)setClassificationMode:(id)json queue:(dispatch_queue_t)sessionQueue;
-(void)findFacesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
+(NSDictionary *)constants;

@end
