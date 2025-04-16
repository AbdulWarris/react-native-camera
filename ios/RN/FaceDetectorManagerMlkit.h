#import <UIKit/UIKit.h>

// Define enum values for when MLKit is not available
#if !__has_include(<GoogleMLKit/MLKFaceDetector.h>)
typedef NS_ENUM(NSInteger, MLKFaceDetectorPerformanceMode) {
    MLKFaceDetectorPerformanceModeFast = 0,
    MLKFaceDetectorPerformanceModeAccurate = 1
};

typedef NS_ENUM(NSInteger, MLKFaceDetectorLandmarkMode) {
    MLKFaceDetectorLandmarkModeNone = 0,
    MLKFaceDetectorLandmarkModeAll = 1
};

typedef NS_ENUM(NSInteger, MLKFaceDetectorClassificationMode) {
    MLKFaceDetectorClassificationModeNone = 0,
    MLKFaceDetectorClassificationModeAll = 1
};

typedef NS_ENUM(NSInteger, MLKFaceLandmarkType) {
    MLKFaceLandmarkTypeLeftEar,
    MLKFaceLandmarkTypeRightEar,
    MLKFaceLandmarkTypeMouthBottom,
    MLKFaceLandmarkTypeMouthRight,
    MLKFaceLandmarkTypeMouthLeft,
    MLKFaceLandmarkTypeLeftEye,
    MLKFaceLandmarkTypeRightEye,
    MLKFaceLandmarkTypeLeftCheek,
    MLKFaceLandmarkTypeRightCheek,
    MLKFaceLandmarkTypeNoseBase
};

@class MLKVisionPoint;
@class MLKFaceLandmark;
@class MLKFace;
#endif

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
