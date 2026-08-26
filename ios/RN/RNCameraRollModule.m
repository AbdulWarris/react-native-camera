#import "RNCameraRollModule.h"
#import <Photos/Photos.h>

// Saves an arbitrary local image file (e.g. a react-native-view-shot snapshot) into the
// user's Photos library. Independent of picture-taking — takes a file URI in. Uses the
// modern Photos framework (PHPhotoLibrary), not the deprecated AssetsLibrary API used by
// this fork's legacy RCTCameraManager camera-roll-save path.
@implementation RNCameraRollModule

RCT_EXPORT_MODULE(RNCameraRoll);

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

RCT_EXPORT_METHOD(save:(NSString *)uri
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSURL *fileUrl = [NSURL URLWithString:uri];
    if (fileUrl == nil || ![[NSFileManager defaultManager] fileExistsAtPath:fileUrl.path]) {
        reject(@"E_SAVE_TO_CAMERA_ROLL_FAILED", [NSString stringWithFormat:@"The file does not exist. Given URI: `%@`.", uri], nil);
        return;
    }

    __block NSString *localIdentifier = nil;
    [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
        PHAssetChangeRequest *request = [PHAssetChangeRequest creationRequestForAssetFromImageAtFileURL:fileUrl];
        localIdentifier = request.placeholderForCreatedAsset.localIdentifier;
    } completionHandler:^(BOOL success, NSError * _Nullable error) {
        if (success) {
            resolve(localIdentifier);
        } else {
            reject(@"E_SAVE_TO_CAMERA_ROLL_FAILED", @"Saving the image to the camera roll failed.", error);
        }
    }];
}

@end
