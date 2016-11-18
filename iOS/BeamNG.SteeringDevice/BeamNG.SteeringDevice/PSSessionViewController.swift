//
//  PSSessionViewController.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel Sulik on 10.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import UIKit
import CoreMotion
import QuartzCore
import AVFoundation

class PSSessionViewController : UIViewController, AVCaptureMetadataOutputObjectsDelegate
{
    var searching : PSSearching!;
    var session : PSSession! = nil;
    var cm : CMMotionManager!;
    
    var connectionButton : UIButton!;
    
    var steeringWheelLayer : CAShapeLayer!;
    var steeringWheelView : UIView!;
//    var steeringWheelLabel : UILabel!;
    
    var speed : PSProgressBar!;
    var rpm : PSProgressBar!;
    var fuel : PSProgressBar!;
    var temperature : PSProgressBar!;
    
    var hudView : UIView!;
    var hudImage : UIImage! = nil;
    var hudImageView : UIImageView! = nil;
    
    var labelSpeed : UILabel! = nil;
    var labelGear : UILabel! = nil;
    var labelDist : UILabel! = nil;
    var labelLag : UILabel! = nil;
    var labelUnit : UILabel! = nil;
    
    var buttonDisconnect : UIButton! = nil;
    
    var buttonMenu : UIButton! = nil;
    
    var buttonAccelerate : UIButton! = nil;
    var buttonBrake : UIButton! = nil;
    
    var lightsBG : UIImage! = nil;
    var lightsBGView : UIImageView! = nil;
    
    var lowBeams : UIImage! = nil;
    var lowBeamView : UIImageView! = nil;
    
    var highBeams : UIImage! = nil;
    var highBeamView : UIImageView! = nil;
    
    var lBlinker : UIImage! = nil;
    var lBlinkerView : UIImageView! = nil;
    
    var rBlinker : UIImage! = nil;
    var rBlinkerView : UIImageView! = nil;
    
    var senSlider : UISlider! = nil;
    var unitSel : UISwitch! = nil;
    var senText : UILabel! = nil;
    var unitText : UILabel! = nil;
    var menuBG : UIImageView! = nil;
    
    var startScreen : UIImage! = nil;
    var startScreenView : UIImageView! = nil;
    var startMessage : UILabel! = nil;
    var startButton : UIButton! = nil;
    var camBlocker : UIImageView! = nil;
    
    //tilt input stuff, global so that its value is kept while its not being updated
    var angle : Double = 0;
    var translatedAngle : Double = 0;
    
    //qr scanner stuff
    var captureSession:AVCaptureSession?
    var videoPreviewLayer:AVCaptureVideoPreviewLayer?
    var qrCodeFrameView:UIView?
    
    //update timer for device motion input
    var timer : Timer! = nil;
    
    override func viewDidLoad()
    {
        super.viewDidLoad();
        self.view.backgroundColor = UIColor.black;
        
        hudImage = UIImage(named: "hud_single_nocolor")!;
        //hudImage = UIImage(named: "text_tester")!;
        let sizeRatio : CGFloat = hudImage.size.height / hudImage.size.width;
        var imgWidth : CGFloat = 500.0;
        var imgHeight : CGFloat = imgWidth * sizeRatio;
        imgWidth = hudImage.size.width;
        imgHeight = hudImage.size.height;
        hudView = UIImageView(frame: CGRect(x: self.view.frame.width * 0.5 - imgWidth * 0.5, y: self.view.frame.height * 0.5 - imgHeight * 0.5, width: imgWidth, height: imgHeight));
        self.view.addSubview(hudView);
        
        
        hudImageView = UIImageView(frame: CGRect(x: 0, y: 0, width: imgWidth, height: imgHeight));
        hudImageView.image = hudImage;
        hudView.addSubview(hudImageView);
        
        lightsBG = UIImage(named: "lightsbg")!;
        lightsBGView = UIImageView(frame: CGRect(x: 0, y: 0, width: imgWidth, height: imgHeight));
        lightsBGView.image = lightsBG;
        hudView.addSubview(lightsBGView);
        
        lowBeams = UIImage(named: "lowbeams")!;
        lowBeamView = UIImageView(frame: CGRect(x: 0, y: 0, width: imgWidth, height: imgHeight));
        lowBeamView.image = lowBeams;
        hudView.addSubview(lowBeamView);
        
        highBeams = UIImage(named: "highbeams")!;
        highBeamView = UIImageView(frame: CGRect(x: 0, y: 0, width: imgWidth, height: imgHeight));
        highBeamView.image = highBeams;
        hudView.addSubview(highBeamView);
        
        lBlinker = UIImage(named: "leftblinker")!;
        lBlinkerView = UIImageView(frame: CGRect(x: 0, y: 0, width: imgWidth, height: imgHeight));
        lBlinkerView.image = lBlinker;
        hudView.addSubview(lBlinkerView);
        
        rBlinker = UIImage(named: "rightblinker")!;
        rBlinkerView = UIImageView(frame: CGRect(x: 0, y: 0, width: imgWidth, height: imgHeight));
        rBlinkerView.image = rBlinker;
        hudView.addSubview(rBlinkerView);

        let wheelRadius : CGFloat = 100;
        steeringWheelLayer = CAShapeLayer();
        steeringWheelLayer.path = UIBezierPath(arcCenter: CGPoint(x: 0, y: 0), radius: wheelRadius, startAngle: 0, endAngle: 3.15 * 2.0, clockwise: true).cgPath;
        steeringWheelLayer.strokeColor = UIColor(red: 0, green: 0, blue: 0, alpha: 1).cgColor;
        steeringWheelLayer.fillColor = UIColor(red: 1, green: 1, blue: 1, alpha: 1).cgColor;
        steeringWheelLayer.lineWidth = 1.0;
        steeringWheelLayer.bounds = CGRect(x: -wheelRadius, y: -wheelRadius, width: wheelRadius * 2, height: wheelRadius * 2);
        steeringWheelLayer.anchorPoint = CGPoint(x: 0, y: 0);
        steeringWheelLayer.position = CGPoint(x: 0, y: 0);
        steeringWheelLayer.borderWidth = 1.0;
        steeringWheelLayer.masksToBounds = true;
        
        steeringWheelView = UIView(frame: CGRect(x: self.view.frame.height * 0.5 - wheelRadius, y: 50, width: wheelRadius * 2, height: wheelRadius * 2));
        steeringWheelView.layer.addSublayer(steeringWheelLayer);
        
        
        let beginAngle : CGFloat = CGFloat(-M_PI - M_PI_4 * 0.5);
        let endAngle : CGFloat = CGFloat(M_PI_4 * 0.5);
        //beginAngle = 0.0;
        
        //width and height = radius * 2
        
        //Those percent values are relative to the size of the HUDImage
        let percentSpeedRadius : CGFloat = 0.4520;
        let percentSpeedWidth : CGFloat = 0.117;
        
        let percentRPMRadius : CGFloat = 0.7083;
        let percentRPMWidth : CGFloat = 0.0777;
        
        let percentFuelRadius : CGFloat = 0.904355;
        let percentFuelWidth : CGFloat = 0.0796872;
        
        let percentTemperatureRadius : CGFloat = 0.901355;
        let percentTemperatureWidth : CGFloat = 0.0786872;
        
        let percentMiddle : CGPoint = CGPoint(x: 0.5, y: 0.693);
        
        speed = PSProgressBar(frame: CGRect(x: 0, y: 0, width: percentSpeedRadius * imgWidth, height: percentSpeedRadius * imgWidth));
        speed.center = CGPoint(x: hudView.frame.width * percentMiddle.x, y: hudView.frame.height * percentMiddle.y);
        speed.draw(beginAngle, endAngle: endAngle, lineWidth: percentSpeedWidth * imgWidth, strokeColor: UIColor(red: 0x14 / 255.0, green: 0x94 / 255.0, blue: 0x34 / 255.0, alpha: 1.0).cgColor, clockwise: true);
        hudView.addSubview(speed);
        
        rpm = PSProgressBar(frame: CGRect(x: 0, y: 0, width: percentRPMRadius * imgWidth, height: percentRPMRadius * imgWidth));
        rpm.center = CGPoint(x: hudView.frame.width * percentMiddle.x, y: hudView.frame.height * percentMiddle.y);
        rpm.draw(beginAngle, endAngle: endAngle, lineWidth: percentRPMWidth * imgWidth, strokeColor: UIColor(red: 0x2b / 255.0, green: 0x71 / 255.0, blue: 0xff / 255.0, alpha: 1.0).cgColor, clockwise: true);
        hudView.addSubview(rpm);
        
        fuel = PSProgressBar(frame: CGRect(x: 0, y: 0, width: percentFuelRadius * imgWidth, height: percentFuelRadius * imgWidth));
        fuel.center = CGPoint(x: hudView.frame.width * percentMiddle.x, y: hudView.frame.height * percentMiddle.y);
        fuel.draw(beginAngle, endAngle: -CGFloat(M_PI_2) * 1.4, lineWidth: percentFuelWidth * imgWidth, strokeColor: UIColor(red: 0x31 / 255.0, green: 0x32 / 255.0, blue: 0x6e / 255.0, alpha: 1.0).cgColor, clockwise: true);
        hudView.addSubview(fuel);
        
        temperature = PSProgressBar(frame: CGRect(x: 0, y: 0, width: percentTemperatureRadius * imgWidth, height: percentTemperatureRadius * imgWidth));
        temperature.center = CGPoint(x: hudView.frame.width * percentMiddle.x, y: hudView.frame.height * percentMiddle.y);
        temperature.draw(endAngle, endAngle: -CGFloat(M_PI_2) * 0.6, lineWidth: percentTemperatureWidth * imgWidth, strokeColor: UIColor(red: 0x6a / 255.0, green: 0x27 / 255.0, blue: 0x27 / 255.0, alpha: 1.0).cgColor, clockwise: false);
        hudView.addSubview(temperature);
        
        hudView.bringSubview(toFront: hudImageView);
        
        let percentageLabelWidth : CGFloat = 0.3;
        let labelWidth : CGFloat = percentageLabelWidth * imgWidth;
        
        labelSpeed = UILabel(frame: CGRect(x: 0.5029 * imgWidth - labelWidth * 0.5, y: 0.641 * imgHeight - labelWidth * 0.5, width: labelWidth, height: labelWidth));
        //labelSpeed.backgroundColor = UIColor.redColor();
        labelSpeed.text = "046";
        labelSpeed.textColor = UIColor.white;
        labelSpeed.font = UIFont(name: "OpenSans-Bold", size: 0.1215 * imgWidth);
        labelSpeed.textAlignment = NSTextAlignment.center;
        hudView.addSubview(labelSpeed);
        
        labelGear = UILabel(frame: CGRect(x: 0.47 * imgWidth - labelWidth * 0.5, y: 0.813 * imgHeight - labelWidth * 0.5, width: labelWidth, height: labelWidth));
        //labelGear.backgroundColor = UIColor.redColor();
        labelGear.text = "2";
        labelGear.textColor = UIColor.white;
        //labelGear.font = UIFont(name: "OpenSans-Bold", size: 0.162234 * imgWidth);
        labelGear.font = UIFont(name: "OpenSans-Bold", size: 0.156915 * imgWidth);
        labelGear.textAlignment = NSTextAlignment.center;
        hudView.addSubview(labelGear);
        
        labelUnit = UILabel(frame: CGRect(x: 0.5 * imgWidth - labelWidth * 0.5, y: 0.535 * imgHeight - labelWidth * 0.5, width: labelWidth, height: labelWidth));
        //labelGear.backgroundColor = UIColor.redColor();
        labelUnit.text = "MPH";
        labelUnit.textColor = UIColor.gray;
        //labelGear.font = UIFont(name: "OpenSans-Bold", size: 0.162234 * imgWidth);
        labelUnit.font = UIFont(name: "OpenSans-Bold", size: 0.065 * imgWidth);
        labelUnit.textAlignment = NSTextAlignment.center;
        hudView.addSubview(labelUnit);
        
        
        labelDist = UILabel(frame: CGRect(x: 0.535 * imgWidth - labelWidth * 0.5, y: 0.9593 * imgHeight - labelWidth * 0.5, width: labelWidth, height: labelWidth));
        //labelGear.backgroundColor = UIColor.redColor();
        labelDist.text = "000086";
        labelDist.textColor = UIColor.white;
        //labelGear.font = UIFont(name: "OpenSans-Bold", size: 0.162234 * imgWidth);
        labelDist.font = UIFont(name: "OpenSans-ExtraBold", size: 0.0532 * imgWidth);
        labelDist.textAlignment = NSTextAlignment.center;
        hudView.addSubview(labelDist);
        
        labelLag = UILabel(frame: CGRect(x: 0.5029 * imgWidth - labelWidth * 0.5, y: 0.641 * imgHeight - labelWidth * -0.5, width: labelWidth, height: labelWidth));
        //labelSpeed.backgroundColor = UIColor.redColor();
        labelLag.text = "Delay: 0ms";
        labelLag.textColor = UIColor.white;
        labelLag.font = UIFont(name: "OpenSans-Bold", size: 0.03 * imgWidth);
        labelLag.textAlignment = NSTextAlignment.center;
        hudView.addSubview(labelLag);
        
        buttonAccelerate = UIButton(type: UIButtonType.system) as UIButton;
        buttonAccelerate.frame = CGRect(x: 0, y: 0, width: self.view.frame.width * 0.5, height: self.view.frame.height);
        buttonAccelerate.setTitle("", for: UIControlState());
        buttonAccelerate.addTarget(self, action: #selector(PSSessionViewController.onButtonAccelerate0), for: UIControlEvents.touchDown);
        buttonAccelerate.addTarget(self, action: #selector(PSSessionViewController.onButtonAccelerate1), for: UIControlEvents.touchUpInside);
        buttonAccelerate.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.0);
        self.view.addSubview(buttonAccelerate);
        
        buttonBrake = UIButton(type: UIButtonType.system) as UIButton;
        buttonBrake.frame = CGRect(x: self.view.frame.width * 0.5, y: 0, width: self.view.frame.width * 0.5, height: self.view.frame.height);
        buttonBrake.setTitle("", for: UIControlState());
        buttonBrake.addTarget(self, action: #selector(PSSessionViewController.onButtonBrake0), for: UIControlEvents.touchDown);
        buttonBrake.addTarget(self, action: #selector(PSSessionViewController.onButtonBrake1), for: UIControlEvents.touchUpInside);
        buttonBrake.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.0);
        self.view.addSubview(buttonBrake);
        
        self.searching = PSSearching(connectionHandler: self.onConnected);
        
        buttonDisconnect = UIButton(type: UIButtonType.system) as UIButton;
        buttonDisconnect.frame = CGRect(x: self.view.frame.width-(self.view.frame.width * 0.1)-10, y: 20, width: self.view.frame.width * 0.1, height: self.view.frame.height * 0.08);
        buttonDisconnect.setTitle("Back", for: UIControlState());
        buttonDisconnect.addTarget(self, action: #selector(PSSessionViewController.onButtonDisconnect), for: UIControlEvents.touchUpInside);
        buttonDisconnect.backgroundColor = UIColor(red: 0.3, green: 0.3, blue: 0.3, alpha: 1.0);
        self.view.addSubview(buttonDisconnect);
        
        //menu stuff
        menuBG = UIImageView(frame: CGRect(x: 0, y: 0, width: 200, height: 200));
        menuBG.backgroundColor = UIColor.black;
        self.view.addSubview(menuBG);
        
        buttonMenu = UIButton(type: UIButtonType.system) as UIButton;
        buttonMenu.frame = CGRect(x: 10, y: 20, width: self.view.frame.width * 0.06, height: self.view.frame.height * 0.08);
        buttonMenu.setTitle("", for: UIControlState());
        buttonMenu.addTarget(self, action: #selector(PSSessionViewController.onButtonMenu), for: UIControlEvents.touchUpInside);
        buttonMenu.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.0);
        buttonMenu.setBackgroundImage(UIImage(named: "menubutton")!, for: UIControlState());
        self.view.addSubview(buttonMenu);
        
        /*self.connectionButton = UIButton(type: UIButtonType.system) as UIButton;
        self.connectionButton.setTitle("Connect", for: UIControlState());
        self.connectionButton.addTarget(self, action: #selector(PSSessionViewController.onButtonConnect), for: UIControlEvents.touchUpInside);
        self.connectionButton.frame = CGRect(x: 10.0, y: 130.0, width: 100, height: 100);
        self.view.addSubview(self.connectionButton);*/
        
        let defaults = UserDefaults.standard;

        self.senSlider = UISlider();
        self.senSlider.minimumValue = 0.2;
        self.senSlider.maximumValue = 1;
        self.senSlider.value = 0.5;
        self.senSlider.frame = CGRect(x: 20.0, y: 155.0, width: 150, height: 20);
        self.senSlider.addTarget(self, action: #selector(PSSessionViewController.onSliderChange), for: UIControlEvents.valueChanged);
        self.view.addSubview(senSlider);
        
        if (defaults.float(forKey: "Sensitivity") != 0) {
            senSlider.value = defaults.float(forKey: "Sensitivity");
        }
        
        self.senText = UILabel();
        self.senText.frame = CGRect(x: 20.0, y: 123.0, width: 150, height: 20);
        self.senText.text = "Sensitivity";
        self.senText.textColor = UIColor.white;
        self.view.addSubview(senText);
        
        self.unitSel = UISwitch();
        self.unitSel.frame = CGRect(x: 20.0, y: 85.0, width: 150, height: 20);
        self.unitSel.onTintColor = UIColor.white;
        self.unitSel.addTarget(self, action: #selector(PSSessionViewController.UnitSwitch), for: UIControlEvents.valueChanged);
        self.view.addSubview(unitSel);
        
        unitSel.isOn = defaults.bool(forKey: "UnitSetting");
        
        self.unitText = UILabel();
        self.unitText.frame = CGRect(x: 80.0, y: 90.0, width: 80, height: 20);
        self.unitText.text = "MPH";
        self.unitText.textColor = UIColor.white;
        self.view.addSubview(unitText);
        
        if(self.unitSel.isOn) {
            unitText.text = "KM/H";
            labelUnit.text = "KM/H"
        }
        else {
            unitText.text = "MPH";
            labelUnit.text = "MPH";
        }
        
        //qr scanner stuff
        
        // Get an instance of the AVCaptureDevice class to initialize a device object and provide the video
        // as the media type parameter.
        let captureDevice = AVCaptureDevice.defaultDevice(withMediaType: AVMediaTypeVideo)
        
        // Get an instance of the AVCaptureDeviceInput class using the previous device object.
        var error:NSError?
        var input : AnyObject! = nil;
        do {
            input = try AVCaptureDeviceInput(device: captureDevice)
        } catch {
            print(error);
        }
            
        if (error != nil) {
            // If any error occurs, simply log the description of it and don't continue any more.
            print("\(error?.localizedDescription)")
            return
        }
        
        // Initialize the captureSession object.
        captureSession = AVCaptureSession();
        // Set the input device on the capture session.
        self.captureSession?.addInput(input as! AVCaptureInput);
        
        // Initialize a AVCaptureMetadataOutput object and set it as the output device to the capture session.
        let captureMetadataOutput = AVCaptureMetadataOutput();
        self.captureSession?.addOutput(captureMetadataOutput);
        
        // Set delegate and use the default dispatch queue to execute the call back
        captureMetadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main);
        captureMetadataOutput.metadataObjectTypes = [AVMetadataObjectTypeQRCode];
        
        // Initialize the video preview layer and add it as a sublayer to the viewPreview view's layer.
        videoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession);
        self.videoPreviewLayer?.videoGravity = AVLayerVideoGravityResizeAspectFill;
        self.videoPreviewLayer?.frame = self.view.bounds;
        if(self.interfaceOrientation == UIInterfaceOrientation.landscapeLeft)
        {
            self.videoPreviewLayer?.connection.videoOrientation = AVCaptureVideoOrientation.landscapeLeft;
        }
        else {
            self.videoPreviewLayer?.connection.videoOrientation = AVCaptureVideoOrientation.landscapeRight;
        }
        self.view.layer.addSublayer(videoPreviewLayer!);
        
        
        // Start video capture.
        captureSession?.startRunning();
        
        // Initialize QR Code Frame to highlight the QR code
        qrCodeFrameView = UIView();
        self.qrCodeFrameView?.layer.borderColor = UIColor.green.cgColor;
        self.qrCodeFrameView?.layer.borderWidth = 2;
        self.view.addSubview(qrCodeFrameView!);
        self.view.bringSubview(toFront: qrCodeFrameView!);
        
        //start up screen and instructions
        camBlocker = UIImageView(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: self.view.frame.height));
        camBlocker.backgroundColor = UIColor.black;
        self.view.addSubview(camBlocker);
        self.view.bringSubview(toFront: camBlocker!);

        
        startScreen = UIImage(named: "startscreen")!;
        startScreenView = UIImageView(frame: CGRect(x: self.view.frame.width/2-(self.view.frame.width * 0.55/2), y: 20, width: self.view.frame.width * 0.55, height: self.view.frame.height * 0.35));
        startScreenView.image = startScreen;
        self.view.addSubview(startScreenView);
        self.view.bringSubview(toFront: startScreenView!);
        
        startMessage = UILabel();
        startMessage.frame = CGRect(x: self.view.frame.width/2-(self.view.frame.width * 0.8/2), y: self.view.frame.height * 0.35, width: self.view.frame.width * 0.8, height: self.view.frame.height * 0.35);
        startMessage.lineBreakMode = .byWordWrapping;
        startMessage.numberOfLines = 3;
        startMessage.text = "Open BeamNG.drive and select \"Controls\" from the main menu and then click on \"HARDWARE\". Scan the QR Code to use this device as a remote controller.";
        startMessage.textColor = UIColor.white;
        self.view.addSubview(startMessage);
        self.view.bringSubview(toFront: startMessage!);
        
        startButton = UIButton(type: UIButtonType.system) as UIButton;
        startButton.setTitle("Scan QR Code", for: UIControlState());
        startButton.addTarget(self, action: #selector(PSSessionViewController.onButtonScan), for: UIControlEvents.touchUpInside);
        startButton.frame = CGRect(x: self.view.frame.height * 0.8, y: self.view.frame.height * 0.65, width: self.view.frame.width * 0.25, height: self.view.frame.height * 0.15);
        startButton.setTitleColor(UIColor.white, for: UIControlState());
        startButton.backgroundColor = UIColor.gray;
        self.view.addSubview(self.startButton);
        self.view.bringSubview(toFront: self.startButton!);
        
        cm = CMMotionManager();
        cm.startDeviceMotionUpdates();
        cm.deviceMotionUpdateInterval = 0.05;
        
    }
    
    func Update () {
        if let deviceMotion = cm.deviceMotion {
        
            let gravity : PSVector = PSVector();
            gravity.x = deviceMotion.gravity.x;
            gravity.y = deviceMotion.gravity.y;
            gravity.z = deviceMotion.gravity.z;
            
            let upVec : PSVector = PSVector(x: 0, y: 1, z: 0);
            
            //keep value locked when going past 90 degrees (previous implementation had it going up to 90 and then back down as you past it)
            if(self.interfaceOrientation == UIInterfaceOrientation.landscapeLeft)
            {
                if (gravity.x > 0) {
                    angle = acos(gravity.dot(upVec));
                
                    angle -= M_PI_2;
                    angle *= -1;
                    angle += M_PI_2;
                
                    let angleDeg : Double = angle * 180.0 / 3.145;
                
                    translatedAngle = angleDeg - 135.0;
                }
                
            }
            else {
                if (gravity.x < 0) {
                    angle = acos(gravity.dot(upVec));
                    let angleDeg : Double = angle * 180.0 / 3.145;
                
                    translatedAngle = angleDeg - 135.0;
                }
            }
            
            
            
            //print(angle);
            //print(String(gravity.x)+" : "+String(gravity.y)+" : "+String(gravity.z));

            
            if(self.session != nil)
            {
                //print("get steer angle");
                //self.session.currentData.steer = round(Float(translatedAngle / 90.0) * -1.0);
                //print(self.senSlider.value);
                var steerVal : Float = ((Float(translatedAngle / 90.0) * -1.0)-0.5)*self.senSlider.value+0.5;
                if (steerVal > 1) {
                    steerVal = 1;
                }
                else if (steerVal < 0) {
                    steerVal = 0;
                }
                self.session.currentData.steer = steerVal;
                //print("session exists, send data");
                self.session.sendCurrentData();
            }
            
            let animDuration : Double = 0.07;
            UIView.animate(withDuration: animDuration, animations: { () -> Void in
                self.hudView.transform = CGAffineTransform(rotationAngle: CGFloat(-(self.angle - (90.0 / (180.0 / 3.145))))).concatenating(CGAffineTransform(scaleX: 1.0, y: 1.0));
                
                if(self.session != nil) {
                    
                    //m/s to mph
                    var Speed : Float = self.session.carData.speed*2.23694;
                    //mph to km/h
                    if(self.unitSel.isOn) {
                        Speed *= 1.60934;
                    }
                    
                    self.speed.progress = CGFloat(Speed/220);
                    self.rpm.progress = CGFloat(self.session.carData.rpm/8000);
                    
                    self.labelSpeed.text = String(format: "%03d", Int(Speed));
                    if(self.session.carData.gear == 0)
                    {
                        self.labelGear.text = String(format: "R");
                    }
                    else if(self.session.carData.gear == 1)
                    {
                        self.labelGear.text = String(format: "N");
                    }
                    else
                    {
                        self.labelGear.text = String(format: "%01d", Int(self.session.carData.gear-1));
                    }
                    self.labelDist.text = String(format: "%06d", Int(self.session.carData.distance));
                    self.fuel.progress = CGFloat(self.session.carData.fuel);
                    self.temperature.progress = CGFloat(self.session.carData.temperature);
                    
                    var lagNum : Int = Int(self.session.currentData.lagDelay.rounded());
                    //let lagDisplay : String = String(format: "%1f",lagNum);
                    self.labelLag.text = "Delay: "+String(lagNum)+"ms";
                    
                    var lights : Int = Int(self.session.carData.lights);
                    //print(self.session.carData.lights);
                    if (lights - 96 >= 0) {
                        //print("show hazards");
                        self.lBlinkerView.isHidden = false;
                        self.rBlinkerView.isHidden = false;
                        lights -= 96;
                    }
                    else if (lights - 64 >= 0) {
                        //print("show right blinker");
                        self.lBlinkerView.isHidden = true;
                        self.rBlinkerView.isHidden = false;
                        lights -= 64;
                    }
                    else if (lights - 32 >= 0) {
                        //print("show Left blinker");
                        self.rBlinkerView.isHidden = true;
                        self.lBlinkerView.isHidden = false;
                        lights -= 32;
                    }
                    else {
                        self.lBlinkerView.isHidden = true;
                        self.rBlinkerView.isHidden = true;
                    }
                    if (lights - 2 >= 0) {
                        //print("show high beams");
                        self.highBeamView.isHidden = false;
                        lights -= 2;
                    }
                    else {
                        self.highBeamView.isHidden = true;
                    }
                    if (lights - 1 >= 0) {
                        //print("show low beams");
                        self.lowBeamView.isHidden = false;
                        lights -= 1;
                    }
                    else {
                        self.lowBeamView.isHidden = true;
                    }
                }
            });
        }
    }
    
    

    func StartCM_OLD () {
        print("Starting device motion updates");
        cm = CMMotionManager().self;
        cm.deviceMotionUpdateInterval = 0.05;
        cm.startDeviceMotionUpdates(using: CMAttitudeReferenceFrame.xArbitraryZVertical, to: OperationQueue.main.self, withHandler: {
            (deviceMotion: CMDeviceMotion?, error: NSError?) in
            if let deviceMotion = deviceMotion {
                let gravity : PSVector = PSVector();
                gravity.x = deviceMotion.gravity.x;
                gravity.y = deviceMotion.gravity.y;
                gravity.z = deviceMotion.gravity.z;
                
                let upVec : PSVector = PSVector(x: 0, y: 1, z: 0);
                
                var angle : Double = acos(gravity.dot(upVec));
                if(self.interfaceOrientation == UIInterfaceOrientation.landscapeLeft)
                {
                    angle -= M_PI_2;
                    angle *= -1;
                    angle += M_PI_2;
                }
                let angleDeg : Double = angle * 180.0 / 3.145;
                var translatedAngle : Double = angleDeg - 135.0;
                
                if(self.session != nil)
                {
                    //print("get steer angle");
                    //self.session.currentData.steer = round(Float(translatedAngle / 90.0) * -1.0);
                    //print(self.senSlider.value);
                    self.session.currentData.steer = (Float(translatedAngle) / 90.0 * -1)*self.senSlider.value;
                    //print(translatedAngle);
                    //print("session exists, send data");
                    self.session.sendCurrentData();
                }
                
                let animDuration : Double = 0.07;
                UIView.animate(withDuration: animDuration, animations: { () -> Void in
                    self.hudView.transform = CGAffineTransform(rotationAngle: CGFloat(-(angle - (90.0 / (180.0 / 3.145))))).concatenating(CGAffineTransform(scaleX: 1.0, y: 1.0));
                    
                    if(self.session != nil) {
                        
                        //m/s to mph
                        var Speed : Float = self.session.carData.speed*2.23694;
                        //mph to km/h
                        if(self.unitSel.isOn) {
                            Speed *= 1.60934;
                        }
                        
                        self.speed.progress = CGFloat(Speed/220);
                        self.rpm.progress = CGFloat(self.session.carData.rpm/8000);
                        
                        self.labelSpeed.text = String(format: "%03d", Int(Speed));
                        if(self.session.carData.gear == 0)
                        {
                            self.labelGear.text = String(format: "R");
                        }
                        else if(self.session.carData.gear == 1)
                        {
                            self.labelGear.text = String(format: "N");
                        }
                        else
                        {
                            self.labelGear.text = String(format: "%01d", Int(self.session.carData.gear-1));
                        }
                        self.labelDist.text = String(format: "%06d", Int(self.session.carData.distance));
                        self.fuel.progress = CGFloat(self.session.carData.fuel);
                        self.temperature.progress = CGFloat(self.session.carData.temperature);
                        var lagNum : Int = Int(self.session.currentData.lagDelay.rounded());
                        let lagDisplay : String = String(format: "%1f",lagNum);
                        self.labelLag.text = "Delay: "+lagDisplay+"ms";
                        
                        var lights : Int = Int(self.session.carData.lights);
                        //print(self.session.carData.lights);
                        if (lights - 96 >= 0) {
                            //print("show hazards");
                            self.lBlinkerView.isHidden = false;
                            self.rBlinkerView.isHidden = false;
                            lights -= 96;
                        }
                        else if (lights - 64 >= 0) {
                            //print("show right blinker");
                            self.lBlinkerView.isHidden = true;
                            self.rBlinkerView.isHidden = false;
                            lights -= 64;
                        }
                        else if (lights - 32 >= 0) {
                            //print("show Left blinker");
                            self.rBlinkerView.isHidden = true;
                            self.lBlinkerView.isHidden = false;
                            lights -= 32;
                        }
                        else {
                            self.lBlinkerView.isHidden = true;
                            self.rBlinkerView.isHidden = true;
                        }
                        if (lights - 2 >= 0) {
                            //print("show high beams");
                            self.highBeamView.isHidden = false;
                            lights -= 2;
                        }
                        else {
                            self.highBeamView.isHidden = true;
                        }
                        if (lights - 1 >= 0) {
                            //print("show low beams");
                            self.lowBeamView.isHidden = false;
                            lights -= 1;
                        }
                        else {
                            self.lowBeamView.isHidden = true;
                        }
                    }
                });
            }
        } as! CMDeviceMotionHandler);
    }
    
    func onConnected(_ toHost: String, onPort: UInt16)
    {
        if(self.session == nil)
        {
            //self.connectionButton.isHidden = true;
            self.session = PSSession(host: toHost, port: onPort, sessionBrokenHandler: self.onDisconnected);
            onButtonMenu();
            self.captureSession?.stopRunning();
            self.captureSession = nil;
            self.videoPreviewLayer?.removeFromSuperlayer();
            self.qrCodeFrameView?.removeFromSuperview();
            CloseStartScreen();
            //StartCM();
            //start timer
            timer = Timer.scheduledTimer(timeInterval: 0.05, target: self, selector: Selector("Update"), userInfo: nil, repeats: true)
        }
        else
        {
            print("Tried to connect once more!");
        }
    }
    func onDisconnected(_ error: NSError)
    {
        //If the session invokes this method, it means that the user has to reconnect.
        self.session = nil;
        //self.connectionButton.isHidden = false;
    }
    func onButtonConnect()
    {
        self.searching.broadcast(1);
    }
    func onButtonAccelerate0()
    {
        buttonAccelerate.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.1);
        if(self.session != nil)
        {
            self.session.currentData.acceleration = 1.0;
        }
    }
    func onButtonAccelerate1()
    {
        buttonAccelerate.backgroundColor = UIColor.clear;
        if(self.session != nil)
        {
            self.session.currentData.acceleration = 0.0;
        }
    }
    func onButtonBrake0()
    {
        buttonBrake.backgroundColor = UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 0.1);
        if(self.session != nil)
        {
            self.session.currentData.brake = 1.0;
        }
    }
    func onButtonBrake1()
    {
        buttonBrake.backgroundColor = UIColor.clear;
        if(self.session != nil)
        {
            self.session.currentData.brake = 0.0;
        }
    }
    func onSliderChange () {
        let defaults = UserDefaults.standard;
        defaults.set(senSlider.value, forKey: "Sensitivity");
    }
    func UnitSwitch () {
        if(self.unitSel.isOn) {
            unitText.text = "KM/H";
            labelUnit.text = "KM/H"
        }
        else {
            unitText.text = "MPH";
            labelUnit.text = "MPH";
        }
        let defaults = UserDefaults.standard;
        defaults.set(unitSel.isOn, forKey: "UnitSetting");

    }
    func onButtonMenu () {
        if (!unitText.isHidden) {
            //connectionButton.isHidden = true;
            unitText.isHidden = true;
            unitSel.isHidden = true;
            senSlider.isHidden = true;
            senText.isHidden = true;
            menuBG.isHidden = true;
        }
        else {
            //if(self.session == nil) {
            //    connectionButton.isHidden = false;
            //}
            unitText.isHidden = false;
            unitSel.isHidden = false;
            senSlider.isHidden = false;
            senText.isHidden = false;
            menuBG.isHidden = false;
        }
    }
    func onButtonScan () {
        CloseStartScreen();
    }
    func CloseStartScreen () {
        self.startButton?.removeFromSuperview();
        self.startMessage?.removeFromSuperview();
        self.startScreenView?.removeFromSuperview();
        self.camBlocker?.removeFromSuperview();
    }
    func onButtonDisconnect () {
        self.view.subviews.forEach({ $0.removeFromSuperview()});
        //self.view.subviews.map({ $0.removeFromSuperview()});
        
        self.searching.listenSocket.close();
        self.searching.socket.close();
        self.session.listenSocket.close();
        self.session.sendSocket.close();
        
        self.searching = nil;
        self.session = nil;
        self.viewDidLoad();
        //self.searching = PSSearching(connectionHandler: self.onConnected);
    }
    func captureOutput(_ captureOutput: AVCaptureOutput!, didOutputMetadataObjects metadataObjects: [Any]!, from connection: AVCaptureConnection!) {
        
        // Check if the metadataObjects array is not nil and it contains at least one object.
        if metadataObjects == nil || metadataObjects.count == 0 {
            qrCodeFrameView?.frame = CGRect.zero
            //messageLabel.text = "No QR code is detected"
            return
        }
        
        // Get the metadata object.
        let metadataObj = metadataObjects[0] as! AVMetadataMachineReadableCodeObject
        
        if metadataObj.type == AVMetadataObjectTypeQRCode {
            // If the found metadata is equal to the QR code metadata then update the status label's text and set the bounds
            let barCodeObject = videoPreviewLayer?.transformedMetadataObject(for: metadataObj as AVMetadataMachineReadableCodeObject) as! AVMetadataMachineReadableCodeObject
            qrCodeFrameView?.frame = barCodeObject.bounds;
            
            if metadataObj.stringValue != nil {
                //messageLabel.text = metadataObj.stringValue
                //print(metadataObj.stringValue);
                var qrString = metadataObj.stringValue;
                var splitString = qrString?.components(separatedBy: "#");
                if (splitString?[1] != "") {
                    //print(splitString[1]);
                    self.searching.code = (splitString?[1])!;
                    self.searching.broadcast(1);
                }
            }
        }
    }
    override var shouldAutorotate : Bool {
        return true;
    }
    override var supportedInterfaceOrientations : UIInterfaceOrientationMask
    {
        return UIInterfaceOrientationMask.landscape;
    }
}
