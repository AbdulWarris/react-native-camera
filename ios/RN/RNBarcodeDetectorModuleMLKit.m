#import "RNBarcodeDetectorModuleMLKit.h"
#if __has_include(<GoogleMLKit/MLKBarcodeScanner.h>)
#import "BarcodeDetectorManagerMlkit.h"
#import "RNFileSystem.h"
#import "RNImageUtils.h"

@implementation RNBarcodeDetectorModuleMLKit

static NSFileManager *fileManager = nil;

- (instancetype)init
{
    self = [super init];
    if (self) {
        fileManager = [NSFileManager defaultManager];
    }
    return self;
}

RCT_EXPORT_MODULE(RNBarcodeDetector);

@synthesize bridge = _bridge;

- (void)setBridge:(RCTBridge *)bridge
{
    _bridge = bridge;
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

RCT_EXPORT_METHOD(detectBarcodes:(nonnull NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *uri = options[@"uri"];
    if (uri == nil) {
        reject(@"E_BARCODE_DETECTION_FAILED", @"You must define a URI.", nil);
        return;
    }

    NSURL *url = [NSURL URLWithString:uri];
    NSString *path = [url.path stringByStandardizingPath];

    @try {
        if (![fileManager fileExistsAtPath:path]) {
            reject(@"E_BARCODE_DETECTION_FAILED", [NSString stringWithFormat:@"The file does not exist. Given path: `%@`.", path], nil);
            return;
        }

        UIImage *image = [[UIImage alloc] initWithContentsOfFile:path];
        UIImage *rotatedImage = [RNImageUtils forceUpOrientation:image];

        BarcodeDetectorManagerMlkit *barcodeDetector = [[BarcodeDetectorManagerMlkit alloc] init];
        [barcodeDetector findBarcodesInFrame:rotatedImage scaleX:1 scaleY:1 completed:^(NSArray *barcodes) {
            resolve(@{
                        @"barcodes" : barcodes,
                        @"image" : @{
                                @"uri" : uri,
                                @"width" : @(image.size.width),
                                @"height" : @(image.size.height)
                                }
                        });
            }];
    } @catch (NSException *exception) {
        reject(@"E_BARCODE_DETECTION_FAILED", [exception description], nil);
    }
}

@end
#else
@implementation RNBarcodeDetectorModuleMLKit

@synthesize bridge = _bridge;

- (void)setBridge:(RCTBridge *)bridge
{
    _bridge = bridge;
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

RCT_EXPORT_MODULE(RNBarcodeDetector);

RCT_EXPORT_METHOD(detectBarcodes:(nonnull NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    reject(@"E_BARCODE_DETECTION_FAILED", @"Barcode detection has not been included in this build.", nil);
}

@end
#endif
