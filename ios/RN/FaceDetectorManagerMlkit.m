#import "FaceDetectorManagerMlkit.h"
#import <React/RCTConvert.h>

NSString *const LOG_TAG = @"FaceDetectorMLKit";

// Only import MLKit, don't define stubs
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
#import <GoogleMLKit/MLKFaceDetector.h>
#import <GoogleMLKit/MLKFace.h>
#import <GoogleMLKit/MLKVisionImage.h>
#endif

@interface FaceDetectorManagerMlkit ()
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
@property(nonatomic, strong) MLKFaceDetector *faceRecognizer;
@property(nonatomic, strong) MLKFaceDetectorOptions *options;
#else
@property(nonatomic, strong) id faceRecognizer;
@property(nonatomic, strong) id options;
#endif
@property(nonatomic, assign) float scaleX;
@property(nonatomic, assign) float scaleY;
@end

@implementation FaceDetectorManagerMlkit

- (instancetype)init 
{
    if (self = [super init]) {
        NSLog(@"%@: Initializing face detector", LOG_TAG);
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
        @try {
            self.options = [[MLKFaceDetectorOptions alloc] init];
            self.options.performanceMode = MLKFaceDetectorPerformanceModeFast;
            self.options.landmarkMode = MLKFaceDetectorLandmarkModeAll;
            self.options.classificationMode = MLKFaceDetectorClassificationModeAll;
            self.options.minFaceSize = 0.15;
            self.faceRecognizer = [MLKFaceDetector faceDetectorWithOptions:self.options];
            if (self.faceRecognizer) {
                NSLog(@"%@: Face detector initialized successfully", LOG_TAG);
            } else {
                NSLog(@"%@: Failed to initialize face detector", LOG_TAG);
            }
        } @catch (NSException *exception) {
            NSLog(@"%@: Exception during initialization: %@", LOG_TAG, exception);
        }
#else
        NSLog(@"%@: MLKit not available", LOG_TAG);
#endif
    }
    return self;
}

- (BOOL)isRealDetector 
{
  return true;
}

+ (NSDictionary *)constants
{
    return @{
             @"Mode" : @{
                     @"fast" : @(RNFaceDetectionFastMode),
                     @"accurate" : @(RNFaceDetectionAccurateMode)
                     },
             @"Landmarks" : @{
                     @"all" : @(RNFaceDetectAllLandmarks),
                     @"none" : @(RNFaceDetectNoLandmarks)
                     },
             @"Classifications" : @{
                     @"all" : @(RNFaceRunAllClassifications),
                     @"none" : @(RNFaceRunNoClassifications)
                     }
             };
}

- (void)setTracking:(id)json queue:(dispatch_queue_t)sessionQueue 
{
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
  BOOL requestedValue = [RCTConvert BOOL:json];
  if (requestedValue != self.options.trackingEnabled) {
      if (sessionQueue) {
          dispatch_async(sessionQueue, ^{
              self.options.trackingEnabled = requestedValue;
              self.faceRecognizer = [MLKFaceDetector faceDetectorWithOptions:self.options];
          });
      }
  }
#endif
}

- (void)setLandmarksMode:(id)json queue:(dispatch_queue_t)sessionQueue 
{
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
    long requestedValue = [RCTConvert NSInteger:json];
    if (requestedValue != self.options.landmarkMode) {
        if (sessionQueue) {
            dispatch_async(sessionQueue, ^{
                self.options.landmarkMode = requestedValue;
                self.faceRecognizer = [MLKFaceDetector faceDetectorWithOptions:self.options];
            });
        }
    }
#endif
}

- (void)setPerformanceMode:(id)json queue:(dispatch_queue_t)sessionQueue 
{
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
    long requestedValue = [RCTConvert NSInteger:json];
    if (requestedValue != self.options.performanceMode) {
        if (sessionQueue) {
            dispatch_async(sessionQueue, ^{
                self.options.performanceMode = requestedValue;
                self.faceRecognizer = [MLKFaceDetector faceDetectorWithOptions:self.options];
            });
        }
    }
#endif
}

- (void)setClassificationMode:(id)json queue:(dispatch_queue_t)sessionQueue 
{
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
    long requestedValue = [RCTConvert NSInteger:json];
    if (requestedValue != self.options.classificationMode) {
        if (sessionQueue) {
            dispatch_async(sessionQueue, ^{
                self.options.classificationMode = requestedValue;
                self.faceRecognizer = [MLKFaceDetector faceDetectorWithOptions:self.options];
            });
        }
    }
#endif
}

- (void)findFacesInFrame:(UIImage *)uiImage
                  scaleX:(float)scaleX
                  scaleY:(float)scaleY
               completed:(void (^)(NSArray *result))completed 
{
    NSLog(@"%@: Starting face detection on image %@", LOG_TAG, uiImage);
    self.scaleX = scaleX;
    self.scaleY = scaleY;
    NSMutableArray *emptyResult = [[NSMutableArray alloc] init];
    
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
    MLKVisionImage *visionImage = [[MLKVisionImage alloc] initWithImage:uiImage];
    [self.faceRecognizer
     processImage:visionImage
     completion:^(NSArray<MLKFace *> *faces, NSError *error) {
         if (error != nil) {
             NSLog(@"%@: Face detection error: %@", LOG_TAG, error);
             completed(emptyResult);
         } else if (faces == nil) {
             NSLog(@"%@: No faces detected", LOG_TAG);
             completed(emptyResult);
         } else {
             NSLog(@"%@: Found %lu faces", LOG_TAG, (unsigned long)faces.count);
             completed([self processFaces:faces]);
         }
     }];
#else
    completed(emptyResult);
#endif
}

- (NSArray *)processFaces:(NSArray *)faces 
{
    NSMutableArray *result = [[NSMutableArray alloc] init];
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
    for (MLKFace *face in faces) {
        NSMutableDictionary *resultDict =
        [[NSMutableDictionary alloc] initWithCapacity:20];
        NSDictionary *bounds = [self processBounds:face.frame];
        [resultDict setObject:bounds forKey:@"bounds"];
        if (face.hasTrackingID) {
            NSInteger trackingID = face.trackingID;
            [resultDict setObject:@(trackingID) forKey:@"faceID"];
        }
        if (face.hasHeadEulerAngleY) {
            CGFloat rotY = face.headEulerAngleY;
            [resultDict setObject:@(rotY) forKey:@"yawAngle"];
        }
        if (face.hasHeadEulerAngleZ) {
            CGFloat rotZ = -1 * face.headEulerAngleZ;
            [resultDict setObject:@(rotZ) forKey:@"rollAngle"];
        }
        MLKFaceLandmark *leftEar = [face landmarkOfType:MLKFaceLandmarkTypeLeftEar];
        if (leftEar != nil) {
            [resultDict setObject:[self processPoint:leftEar.position]
                           forKey:@"leftEarPosition"];
        }
        MLKFaceLandmark *rightEar = [face landmarkOfType:MLKFaceLandmarkTypeRightEar];
        if (rightEar != nil) {
            [resultDict setObject:[self processPoint:rightEar.position]
                           forKey:@"rightEarPosition"];
        }
        MLKFaceLandmark *mouthBottom = [face landmarkOfType:MLKFaceLandmarkTypeMouthBottom];
        if (mouthBottom != nil) {
            [resultDict setObject:[self processPoint:mouthBottom.position]
                           forKey:@"bottomMouthPosition"];
        }
        MLKFaceLandmark *mouthRight = [face landmarkOfType:MLKFaceLandmarkTypeMouthRight];
        if (mouthRight != nil) {
            [resultDict setObject:[self processPoint:mouthRight.position]
                           forKey:@"rightMouthPosition"];
        }
        MLKFaceLandmark *mouthLeft = [face landmarkOfType:MLKFaceLandmarkTypeMouthLeft];
        if (mouthLeft != nil) {
            [resultDict setObject:[self processPoint:mouthLeft.position]
                           forKey:@"leftMouthPosition"];
        }
        MLKFaceLandmark *eyeLeft = [face landmarkOfType:MLKFaceLandmarkTypeLeftEye];
        if (eyeLeft != nil) {
            [resultDict setObject:[self processPoint:eyeLeft.position]
                           forKey:@"leftEyePosition"];
        }
        MLKFaceLandmark *eyeRight = [face landmarkOfType:MLKFaceLandmarkTypeRightEye];
        if (eyeRight != nil) {
            [resultDict setObject:[self processPoint:eyeRight.position]
                           forKey:@"rightEyePosition"];
        }
        MLKFaceLandmark *cheekLeft = [face landmarkOfType:MLKFaceLandmarkTypeLeftCheek];
        if (cheekLeft != nil) {
            [resultDict setObject:[self processPoint:cheekLeft.position]
                           forKey:@"leftCheekPosition"];
        }
        MLKFaceLandmark *cheekRight = [face landmarkOfType:MLKFaceLandmarkTypeRightCheek];
        if (cheekRight != nil) {
            [resultDict setObject:[self processPoint:cheekRight.position]
                           forKey:@"rightCheekPosition"];
        }
        MLKFaceLandmark *noseBase = [face landmarkOfType:MLKFaceLandmarkTypeNoseBase];
        if (noseBase != nil) {
            [resultDict setObject:[self processPoint:noseBase.position]
                           forKey:@"noseBasePosition"];
        }
        if (face.hasSmilingProbability) {
            CGFloat smileProb = face.smilingProbability;
            NSLog(@"%@: smileProb: %f", LOG_TAG, smileProb);
            [resultDict setObject:@(smileProb) forKey:@"smilingProbability"];
        }
        if (face.hasRightEyeOpenProbability) {
            CGFloat rightEyeOpenProb = face.rightEyeOpenProbability;
            NSLog(@"%@: rightEyeOpenProb: %f", LOG_TAG, rightEyeOpenProb);
            [resultDict setObject:@(rightEyeOpenProb)
                           forKey:@"rightEyeOpenProbability"];
        }
        if (face.hasLeftEyeOpenProbability) {
            CGFloat leftEyeOpenProb = face.leftEyeOpenProbability;
            NSLog(@"%@: leftEyeOpenProb: %f", LOG_TAG, leftEyeOpenProb);
            [resultDict setObject:@(leftEyeOpenProb)
                           forKey:@"leftEyeOpenProbability"];
        }
        [result addObject:resultDict];
    }
#endif
    return result;
}

- (NSDictionary *)processBounds:(CGRect)bounds 
{
    float width = bounds.size.width * _scaleX;
    float height = bounds.size.height * _scaleY;
    float originX = bounds.origin.x * _scaleX;
    float originY = bounds.origin.y * _scaleY;
    NSDictionary *boundsDict = @{
                                 @"size" : @{@"width" : @(width), @"height" : @(height)},
                                 @"origin" : @{@"x" : @(originX), @"y" : @(originY)}
                                 };
    return boundsDict;
}

- (NSDictionary *)processPoint:(id)point
{
    float originX = 0;
    float originY = 0;
    
#if __has_include(<GoogleMLKit/MLKFaceDetector.h>)
    MLKVisionPoint *visionPoint = (MLKVisionPoint *)point;
    originX = visionPoint.x * _scaleX;
    originY = visionPoint.y * _scaleY;
#endif
    
    NSDictionary *pointDict = @{
                                @"x" : @(originX),
                                @"y" : @(originY)
                                };
    return pointDict;
}

@end