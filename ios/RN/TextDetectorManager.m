#import "TextDetectorManager.h"

#if __has_include(<MLKitTextRecognition/MLKitTextRecognition.h>)
#import <MLKitTextRecognition/MLKitTextRecognition.h>
#import <MLKitVision/MLKitVision.h>
#import <MLKitTextRecognitionCommon/MLKitTextRecognitionCommon.h>

@interface TextDetectorManager ()

@property (nonatomic, strong) MLKTextRecognizer *textRecognizer;
@property (nonatomic, assign) float scaleX;
@property (nonatomic, assign) float scaleY;

@end

@implementation TextDetectorManager

- (instancetype)init {
    if (self = [super init]) {
        self.textRecognizer = [MLKTextRecognizer textRecognizer];
    }
    return self;
}

- (BOOL)isRealDetector {
    return YES;
}

- (void)findTextBlocksInFrame:(UIImage *)uiImage scaleX:(float)scaleX scaleY:(float)scaleY completed:(void (^)(NSArray *result))completed {
    self.scaleX = scaleX;
    self.scaleY = scaleY;
    MLKVisionImage *visionImage = [[MLKVisionImage alloc] initWithImage:uiImage];
    
    [self.textRecognizer processImage:visionImage completion:^(MLKText * _Nullable result, NSError * _Nullable error) {
        if (error != nil || result == nil) {
            completed(@[]); // Return an empty array on error or no result
        } else {
            completed([self processBlocks:result.blocks]);
        }
    }];
}

- (NSArray<NSDictionary *> *)processBlocks:(NSArray<MLKTextBlock *> *)blocks {
    NSMutableArray<NSDictionary *> *textBlocks = [NSMutableArray array];
    for (MLKTextBlock *textBlock in blocks) {
        NSDictionary *textBlockDict = @{
            @"type": @"block",
            @"value": textBlock.text,
            @"bounds": [self processBounds:textBlock.frame],
            @"components": [self processLines:textBlock.lines]
        };
        [textBlocks addObject:textBlockDict];
    }
    return [textBlocks copy]; // Return an immutable copy
}

- (NSArray<NSDictionary *> *)processLines:(NSArray<MLKTextLine *> *)lines {
    NSMutableArray<NSDictionary *> *lineBlocks = [NSMutableArray array];
    for (MLKTextLine *textLine in lines) {
        NSDictionary *textLineDict = @{
            @"type": @"line",
            @"value": textLine.text,
            @"bounds": [self processBounds:textLine.frame],
            @"components": [self processElements:textLine.elements]
        };
        [lineBlocks addObject:textLineDict];
    }
    return [lineBlocks copy]; // Return an immutable copy
}

- (NSArray<NSDictionary *> *)processElements:(NSArray<MLKTextElement *> *)elements {
    NSMutableArray<NSDictionary *> *elementBlocks = [NSMutableArray array];
    for (MLKTextElement *textElement in elements) {
        NSDictionary *textElementDict = @{
            @"type": @"element",
            @"value": textElement.text,
            @"bounds": [self processBounds:textElement.frame]
        };
        [elementBlocks addObject:textElementDict];
    }
    return [elementBlocks copy]; // Return an immutable copy
}

- (NSDictionary *)processBounds:(CGRect)bounds {
    CGFloat width = bounds.size.width * self.scaleX;
    CGFloat height = bounds.size.height * self.scaleY;
    CGFloat originX = bounds.origin.x * self.scaleX;
    CGFloat originY = bounds.origin.y * self.scaleY;
    
    return @{
        @"size": @{
            @"width": @(width),
            @"height": @(height)
        },
        @"origin": @{
            @"x": @(originX),
            @"y": @(originY)
        }
    };
}

@end

#else

@interface TextDetectorManager ()

@end

@implementation TextDetectorManager

- (instancetype)init {
    self = [super init];
    return self;
}

- (BOOL)isRealDetector {
    return NO;
}

- (void)findTextBlocksInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(void (^)(NSArray *result))completed {
    NSLog(@"TextDetector not installed, stub used!");
    completed(@[@"Error, Text Detector not installed"]);
}

@end

#endif