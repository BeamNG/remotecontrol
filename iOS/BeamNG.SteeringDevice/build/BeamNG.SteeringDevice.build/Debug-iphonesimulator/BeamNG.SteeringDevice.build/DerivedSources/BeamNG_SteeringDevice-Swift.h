// Generated by Apple Swift version 2.1.1 (swiftlang-700.1.101.15 clang-700.1.81)
#pragma clang diagnostic push

#if defined(__has_include) && __has_include(<swift/objc-prologue.h>)
# include <swift/objc-prologue.h>
#endif

#pragma clang diagnostic ignored "-Wauto-import"
#include <objc/NSObject.h>
#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>

#if defined(__has_include) && __has_include(<uchar.h>)
# include <uchar.h>
#elif !defined(__cplusplus) || __cplusplus < 201103L
typedef uint_least16_t char16_t;
typedef uint_least32_t char32_t;
#endif

typedef struct _NSZone NSZone;

#if !defined(SWIFT_PASTE)
# define SWIFT_PASTE_HELPER(x, y) x##y
# define SWIFT_PASTE(x, y) SWIFT_PASTE_HELPER(x, y)
#endif
#if !defined(SWIFT_METATYPE)
# define SWIFT_METATYPE(X) Class
#endif

#if defined(__has_attribute) && __has_attribute(objc_runtime_name)
# define SWIFT_RUNTIME_NAME(X) __attribute__((objc_runtime_name(X)))
#else
# define SWIFT_RUNTIME_NAME(X)
#endif
#if defined(__has_attribute) && __has_attribute(swift_name)
# define SWIFT_COMPILE_NAME(X) __attribute__((swift_name(X)))
#else
# define SWIFT_COMPILE_NAME(X)
#endif
#if !defined(SWIFT_CLASS_EXTRA)
# define SWIFT_CLASS_EXTRA
#endif
#if !defined(SWIFT_PROTOCOL_EXTRA)
# define SWIFT_PROTOCOL_EXTRA
#endif
#if !defined(SWIFT_ENUM_EXTRA)
# define SWIFT_ENUM_EXTRA
#endif
#if !defined(SWIFT_CLASS)
# if defined(__has_attribute) && __has_attribute(objc_subclassing_restricted) 
#  define SWIFT_CLASS(SWIFT_NAME) SWIFT_RUNTIME_NAME(SWIFT_NAME) __attribute__((objc_subclassing_restricted)) SWIFT_CLASS_EXTRA
#  define SWIFT_CLASS_NAMED(SWIFT_NAME) __attribute__((objc_subclassing_restricted)) SWIFT_COMPILE_NAME(SWIFT_NAME) SWIFT_CLASS_EXTRA
# else
#  define SWIFT_CLASS(SWIFT_NAME) SWIFT_RUNTIME_NAME(SWIFT_NAME) SWIFT_CLASS_EXTRA
#  define SWIFT_CLASS_NAMED(SWIFT_NAME) SWIFT_COMPILE_NAME(SWIFT_NAME) SWIFT_CLASS_EXTRA
# endif
#endif

#if !defined(SWIFT_PROTOCOL)
# define SWIFT_PROTOCOL(SWIFT_NAME) SWIFT_RUNTIME_NAME(SWIFT_NAME) SWIFT_PROTOCOL_EXTRA
# define SWIFT_PROTOCOL_NAMED(SWIFT_NAME) SWIFT_COMPILE_NAME(SWIFT_NAME) SWIFT_PROTOCOL_EXTRA
#endif

#if !defined(SWIFT_EXTENSION)
# define SWIFT_EXTENSION(M) SWIFT_PASTE(M##_Swift_, __LINE__)
#endif

#if !defined(OBJC_DESIGNATED_INITIALIZER)
# if defined(__has_attribute) && __has_attribute(objc_designated_initializer)
#  define OBJC_DESIGNATED_INITIALIZER __attribute__((objc_designated_initializer))
# else
#  define OBJC_DESIGNATED_INITIALIZER
# endif
#endif
#if !defined(SWIFT_ENUM)
# define SWIFT_ENUM(_type, _name) enum _name : _type _name; enum SWIFT_ENUM_EXTRA _name : _type
#endif
typedef float swift_float2  __attribute__((__ext_vector_type__(2)));
typedef float swift_float3  __attribute__((__ext_vector_type__(3)));
typedef float swift_float4  __attribute__((__ext_vector_type__(4)));
typedef double swift_double2  __attribute__((__ext_vector_type__(2)));
typedef double swift_double3  __attribute__((__ext_vector_type__(3)));
typedef double swift_double4  __attribute__((__ext_vector_type__(4)));
typedef int swift_int2  __attribute__((__ext_vector_type__(2)));
typedef int swift_int3  __attribute__((__ext_vector_type__(3)));
typedef int swift_int4  __attribute__((__ext_vector_type__(4)));
#if defined(__has_feature) && __has_feature(modules)
@import UIKit;
@import ObjectiveC;
@import CoreGraphics;
@import CoreFoundation;
#endif

#import "/Users/Hondune/Desktop/Work And Stuff/BeamNG Work/remotecontrol/iOS/BeamNG.SteeringDevice/BeamNG.SteeringDevice/BeamNG.SteeringDevice-Bridging-Header.h"

#pragma clang diagnostic ignored "-Wproperty-attribute-mismatch"
#pragma clang diagnostic ignored "-Wduplicate-method-arg"
@class UIWindow;
@class UIApplication;
@class NSObject;

SWIFT_CLASS("_TtC21BeamNG_SteeringDevice11AppDelegate")
@interface AppDelegate : UIResponder <UIApplicationDelegate>
@property (nonatomic, strong) UIWindow * __nullable window;
- (BOOL)application:(UIApplication * __nonnull)application didFinishLaunchingWithOptions:(NSDictionary * __nullable)launchOptions;
- (void)applicationWillResignActive:(UIApplication * __null_unspecified)application;
- (void)applicationDidEnterBackground:(UIApplication * __null_unspecified)application;
- (void)applicationWillEnterForeground:(UIApplication * __null_unspecified)application;
- (void)applicationDidBecomeActive:(UIApplication * __null_unspecified)application;
- (void)applicationWillTerminate:(UIApplication * __null_unspecified)application;
- (nonnull instancetype)init OBJC_DESIGNATED_INITIALIZER;
@end


SWIFT_CLASS("_TtC21BeamNG_SteeringDevice9PSCarData")
@interface PSCarData : NSObject
@property (nonatomic) float speed;
@property (nonatomic) float rpm;
@property (nonatomic) float fuel;
@property (nonatomic) float temperature;
@property (nonatomic) NSInteger gear;
@property (nonatomic) NSInteger distance;
- (nonnull instancetype)init OBJC_DESIGNATED_INITIALIZER;
@end

@class CAShapeLayer;
@class NSCoder;

SWIFT_CLASS("_TtC21BeamNG_SteeringDevice13PSProgressBar")
@interface PSProgressBar : UIView
@property (nonatomic, strong) CAShapeLayer * __null_unspecified progressLayer;
@property (nonatomic, strong) CAShapeLayer * __null_unspecified progressLayer2;
@property (nonatomic) CGFloat _progress;
@property (nonatomic) CGFloat progress;
@property (nonatomic) CGFloat angleBegin;
@property (nonatomic) CGFloat angleEnd;
- (nullable instancetype)initWithCoder:(NSCoder * __nonnull)aDecoder OBJC_DESIGNATED_INITIALIZER;
- (nonnull instancetype)initWithFrame:(CGRect)frame OBJC_DESIGNATED_INITIALIZER;
- (void)draw:(CGFloat)startAngle endAngle:(CGFloat)endAngle lineWidth:(CGFloat)lineWidth strokeColor:(CGColorRef __nonnull)strokeColor clockwise:(BOOL)clockwise;
@end

@class AsyncUdpSocket;
@class NSError;
@class NSData;

SWIFT_CLASS("_TtC21BeamNG_SteeringDevice11PSSearching")
@interface PSSearching : NSObject <AsyncUdpSocketDelegate>
@property (nonatomic, strong) AsyncUdpSocket * __null_unspecified socket;
@property (nonatomic, strong) AsyncUdpSocket * __null_unspecified listenSocket;
@property (nonatomic, copy) void (^ __null_unspecified onConnectToHost)(NSString * __nonnull, uint16_t);
- (nonnull instancetype)init OBJC_DESIGNATED_INITIALIZER;
- (nonnull instancetype)initWithConnectionHandler:(void (^ __nonnull)(NSString * __nonnull, uint16_t))connectionHandler;
- (void)broadcast:(CFTimeInterval)timeout;
- (void)onUdpSocket:(AsyncUdpSocket * __null_unspecified)sock didNotReceiveDataWithTag:(NSInteger)tag dueToError:(NSError * __null_unspecified)error;
- (void)onUdpSocket:(AsyncUdpSocket * __null_unspecified)sock didNotSendDataWithTag:(NSInteger)tag dueToError:(NSError * __null_unspecified)error;
- (BOOL)onUdpSocket:(AsyncUdpSocket * __null_unspecified)sock didReceiveData:(NSData * __null_unspecified)data withTag:(NSInteger)tag fromHost:(NSString * __null_unspecified)host port:(uint16_t)port;
- (void)onUdpSocket:(AsyncUdpSocket * __null_unspecified)sock didSendDataWithTag:(NSInteger)tag;
- (void)onUdpSocketDidClose:(AsyncUdpSocket * __null_unspecified)sock;
@end

@class CMMotionManager;
@class UIButton;
@class UIImage;
@class UIImageView;
@class UILabel;
@class NSBundle;

SWIFT_CLASS("_TtC21BeamNG_SteeringDevice23PSSessionViewController")
@interface PSSessionViewController : UIViewController
@property (nonatomic, strong) PSSearching * __null_unspecified searching;
@property (nonatomic, strong) CMMotionManager * __null_unspecified cm;
@property (nonatomic, strong) UIButton * __null_unspecified connectionButton;
@property (nonatomic, strong) CAShapeLayer * __null_unspecified steeringWheelLayer;
@property (nonatomic, strong) UIView * __null_unspecified steeringWheelView;
@property (nonatomic, strong) PSProgressBar * __null_unspecified speed;
@property (nonatomic, strong) PSProgressBar * __null_unspecified rpm;
@property (nonatomic, strong) PSProgressBar * __null_unspecified fuel;
@property (nonatomic, strong) PSProgressBar * __null_unspecified temperature;
@property (nonatomic, strong) UIView * __null_unspecified hudView;
@property (nonatomic, strong) UIImage * __null_unspecified hudImage;
@property (nonatomic, strong) UIImageView * __null_unspecified hudImageView;
@property (nonatomic, strong) UILabel * __null_unspecified labelSpeed;
@property (nonatomic, strong) UILabel * __null_unspecified labelGear;
@property (nonatomic, strong) UILabel * __null_unspecified labelDist;
@property (nonatomic, strong) UIButton * __null_unspecified buttonAccelerate;
@property (nonatomic, strong) UIButton * __null_unspecified buttonBrake;
- (void)viewDidLoad;
- (void)onConnected:(NSString * __nonnull)toHost onPort:(uint16_t)onPort;
- (void)onDisconnected:(NSError * __nonnull)error;
- (void)onButtonConnect;
- (void)onButtonAccelerate0;
- (void)onButtonAccelerate1;
- (void)onButtonBrake0;
- (void)onButtonBrake1;
- (BOOL)shouldAutorotate;
- (UIInterfaceOrientationMask)supportedInterfaceOrientations;
- (nonnull instancetype)initWithNibName:(NSString * __nullable)nibNameOrNil bundle:(NSBundle * __nullable)nibBundleOrNil OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * __nonnull)aDecoder OBJC_DESIGNATED_INITIALIZER;
@end


SWIFT_CLASS("_TtC21BeamNG_SteeringDevice11PSSteerData")
@interface PSSteerData : NSObject
@property (nonatomic) float acceleration;
@property (nonatomic) float brake;
@property (nonatomic) float steer;
- (nonnull instancetype)init OBJC_DESIGNATED_INITIALIZER;
@end

#pragma clang diagnostic pop
