#import <UIKit/UIKit.h>

// Import MLKit if available
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
#import <GoogleMLKit/MLKFaceDetector.h>
#import <GoogleMLKit/MLKFace.h>
#import <GoogleMLKit/MLKVisionImage.h>
#endif

// Define our wrapper enums that map to MLKit's enums
typedef NS_ENUM(NSInteger, RNFaceDetectionMode) {
    RNFaceDetectionFastMode = 0,      // Maps to MLKFaceDetectorPerformanceModeFast
    RNFaceDetectionAccurateMode = 1   // Maps to MLKFaceDetectorPerformanceModeAccurate
};

typedef NS_ENUM(NSInteger, RNFaceDetectionLandmarks) {
    RNFaceDetectAllLandmarks = 1,     // Maps to MLKFaceDetectorLandmarkModeAll
    RNFaceDetectNoLandmarks = 0       // Maps to MLKFaceDetectorLandmarkModeNone
};

typedef NS_ENUM(NSInteger, RNFaceDetectionClassifications) {
    RNFaceRunAllClassifications = 1,  // Maps to MLKFaceDetectorClassificationModeAll
    RNFaceRunNoClassifications = 0    // Maps to MLKFaceDetectorClassificationModeNone
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