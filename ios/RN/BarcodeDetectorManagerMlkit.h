#import <UIKit/UIKit.h>

#if __has_include(<GoogleMLKit/MLKBarcodeScanner.h>)
#import <GoogleMLKit/MLKBarcodeScanner.h>
#import <GoogleMLKit/MLKBarcode.h>
#import <GoogleMLKit/MLKVisionImage.h>
#else
// Forward declarations and type definitions
@class MLKBarcodeScanner;
@class MLKBarcodeScannerOptions;
@class MLKVisionImage;

@interface MLKVisionPoint : NSObject
@property(nonatomic, readonly) CGFloat x;
@property(nonatomic, readonly) CGFloat y;
@end

// MLKBarcode and related classes
@interface MLKBarcodeURL : NSObject
@property(nonatomic, readonly) NSString *title;
@property(nonatomic, readonly) NSString *url;
@end

@interface MLKBarcodeWiFi : NSObject
@property(nonatomic, readonly) NSString *ssid;
@property(nonatomic, readonly) NSString *password;
@property(nonatomic, readonly) NSInteger type;
@end

typedef NS_ENUM(NSInteger, MLKBarcodeWiFiEncryptionType) {
    MLKBarcodeWiFiEncryptionTypeOpen,
    MLKBarcodeWiFiEncryptionTypeWPA,
    MLKBarcodeWiFiEncryptionTypeWEP
};

@interface MLKBarcodeEmail : NSObject
@property(nonatomic, readonly) NSString *address;
@property(nonatomic, readonly) NSString *body;
@property(nonatomic, readonly) NSString *subject;
@property(nonatomic, readonly) NSInteger type;
@end

@interface MLKBarcodePhone : NSObject
@property(nonatomic, readonly) NSString *number;
@property(nonatomic, readonly) NSInteger type;
@end

@interface MLKBarcodeAddress : NSObject
@property(nonatomic, readonly) NSArray<NSString *> *addressLines;
@property(nonatomic, readonly) NSInteger type;
@end

@interface MLKBarcodeDriverLicense : NSObject
@property(nonatomic, readonly) NSString *firstName;
@property(nonatomic, readonly) NSString *middleName;
@property(nonatomic, readonly) NSString *lastName;
@property(nonatomic, readonly) NSString *gender;
@property(nonatomic, readonly) NSString *addressCity;
@property(nonatomic, readonly) NSString *addressState;
@property(nonatomic, readonly) NSString *addressStreet;
@property(nonatomic, readonly) NSString *addressZip;
@property(nonatomic, readonly) NSString *birthDate;
@property(nonatomic, readonly) NSString *documentType;
@property(nonatomic, readonly) NSString *licenseNumber;
@property(nonatomic, readonly) NSString *expiryDate;
@property(nonatomic, readonly) NSString *issuingDate;
@property(nonatomic, readonly) NSString *issuingCountry;
@end

@interface MLKBarcodeCalendarEvent : NSObject
@property(nonatomic, readonly) NSString *eventDescription;
@property(nonatomic, readonly) NSString *location;
@property(nonatomic, readonly) NSString *organizer;
@property(nonatomic, readonly) NSString *status;
@property(nonatomic, readonly) NSString *summary;
@property(nonatomic, readonly) NSDate *start;
@property(nonatomic, readonly) NSDate *end;
@end

@interface MLKBarcodeGeoPoint : NSObject
@property(nonatomic, readonly) double latitude;
@property(nonatomic, readonly) double longitude;
@end

@interface MLKBarcodeSMS : NSObject
@property(nonatomic, readonly) NSString *message;
@property(nonatomic, readonly) NSString *phoneNumber;
@end

@interface MLKBarcodePersonName : NSObject
@property(nonatomic, readonly) NSString *formattedName;
@property(nonatomic, readonly) NSString *first;
@property(nonatomic, readonly) NSString *last;
@property(nonatomic, readonly) NSString *middle;
@property(nonatomic, readonly) NSString *prefix;
@property(nonatomic, readonly) NSString *pronunciation;
@property(nonatomic, readonly) NSString *suffix;
@end

@interface MLKBarcodeContactInfo : NSObject
@property(nonatomic, readonly) NSArray<MLKBarcodeAddress *> *addresses;
@property(nonatomic, readonly) NSArray<MLKBarcodeEmail *> *emails;
@property(nonatomic, readonly) NSArray<MLKBarcodePhone *> *phones;
@property(nonatomic, readonly) NSArray<NSString *> *urls;
@property(nonatomic, readonly) NSString *organization;
@property(nonatomic, readonly) MLKBarcodePersonName *name;
@end

typedef NS_ENUM(NSInteger, MLKBarcodeValueType) {
    MLKBarcodeValueTypeEmail,
    MLKBarcodeValueTypePhone,
    MLKBarcodeValueTypeCalendarEvent,
    MLKBarcodeValueTypeDriversLicense,
    MLKBarcodeValueTypeGeographicCoordinates,
    MLKBarcodeValueTypeSMS,
    MLKBarcodeValueTypeContactInfo,
    MLKBarcodeValueTypeWiFi,
    MLKBarcodeValueTypeText,
    MLKBarcodeValueTypeISBN,
    MLKBarcodeValueTypeProduct,
    MLKBarcodeValueTypeURL
};

@interface MLKBarcode : NSObject
@property(nonatomic, readonly) CGRect frame;
@property(nonatomic, readonly) NSString *displayValue;
@property(nonatomic, readonly) NSString *rawValue;
@property(nonatomic, readonly) MLKBarcodeValueType valueType;
@property(nonatomic, readonly) MLKBarcodeWiFi *wifi;
@property(nonatomic, readonly) MLKBarcodeURL *URL;
@property(nonatomic, readonly) MLKBarcodeContactInfo *contactInfo;
@property(nonatomic, readonly) MLKBarcodeSMS *sms;
@property(nonatomic, readonly) MLKBarcodeGeoPoint *geoPoint;
@property(nonatomic, readonly) MLKBarcodeDriverLicense *driverLicense;
@property(nonatomic, readonly) MLKBarcodeCalendarEvent *calendarEvent;
@property(nonatomic, readonly) MLKBarcodePhone *phone;
@property(nonatomic, readonly) MLKBarcodeEmail *email;
@end

typedef NS_ENUM(NSInteger, MLKBarcodeFormat) {
    MLKBarcodeFormatCode128 = 0x0001,
    MLKBarcodeFormatCode39 = 0x0002,
    MLKBarcodeFormatCode93 = 0x0004,
    MLKBarcodeFormatCodaBar = 0x0008,
    MLKBarcodeFormatEAN13 = 0x0010,
    MLKBarcodeFormatEAN8 = 0x0020,
    MLKBarcodeFormatITF = 0x0040,
    MLKBarcodeFormatUPCA = 0x0080,
    MLKBarcodeFormatUPCE = 0x0100,
    MLKBarcodeFormatQRCode = 0x0200,
    MLKBarcodeFormatPDF417 = 0x0400,
    MLKBarcodeFormatAztec = 0x0800,
    MLKBarcodeFormatDataMatrix = 0x1000,
    MLKBarcodeFormatAll = 0xFFFF
};

typedef NS_ENUM(NSInteger, MLKBarcodePhoneType) {
    MLKBarcodePhoneTypeUnknown,
    MLKBarcodePhoneTypeFax,
    MLKBarcodePhoneTypeHome,
    MLKBarcodePhoneTypeWork,
    MLKBarcodePhoneTypeMobile
};

typedef NS_ENUM(NSInteger, MLKBarcodeEmailType) {
    MLKBarcodeEmailTypeUnknown,
    MLKBarcodeEmailTypeWork,
    MLKBarcodeEmailTypeHome
};

typedef NS_ENUM(NSInteger, MLKBarcodeAddressType) {
    MLKBarcodeAddressTypeUnknown,
    MLKBarcodeAddressTypeWork,
    MLKBarcodeAddressTypeHome
};
#endif

@interface BarcodeDetectorManagerMlkit : NSObject
typedef void(^postRecognitionBlock)(NSArray *barcodes);

- (instancetype)init;

-(BOOL)isRealDetector;
-(NSInteger)fetchDetectionMode;
-(void)setType:(id)json queue:(dispatch_queue_t)sessionQueue;
-(void)setMode:(id)json queue:(dispatch_queue_t)sessionQueue;
-(void)findBarcodesInFrame:(UIImage *)image scaleX:(float)scaleX scaleY:(float)scaleY completed:(postRecognitionBlock)completed;
+(NSDictionary *)constants;

@end
