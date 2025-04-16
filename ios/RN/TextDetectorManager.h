#import <UIKit/UIKit.h>

#if __has_include(<GoogleMLKit/MLKTextRecognizer.h>)
#import <GoogleMLKit/MLKTextRecognizer.h>
#import <GoogleMLKit/MLKText.h>
#import <GoogleMLKit/MLKVisionImage.h>
#else
@class MLKTextRecognizer;
@class MLKText;
@class MLKVisionImage;
#endif

@interface TextDetectorManager : NSObject 
typedef void(^postRecognitionBlock)(NSArray *textBlocks);

- (instancetype)init;
-(BOOL)isRealDetector;
-(void)findTextBlocksInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;

@end
