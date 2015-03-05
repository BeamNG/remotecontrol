//
//  ViewController.swift
//  BeamNG.SteeringDevice
//
//  Created by Pawel on 09.10.14.
//  Copyright (c) 2014 28Apps. All rights reserved.
//

import UIKit

class PSMainViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, PSSteeringDelegate
{
    @IBOutlet weak var indicator: UIActivityIndicatorView!;
    var tableView: UITableView!;
    var tempFrame : CGRect!;
    var steering : PSSteering!;
    override func viewDidLoad()
    {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        steering = (UIApplication.sharedApplication().delegate as AppDelegate).steering;
        indicator.startAnimating();
        
        tableView = UITableView(frame: CGRectMake(0, 200, self.view.frame.width, 100));
        tableView.delegate = self;
        tableView.dataSource = self;
        self.view.addSubview(tableView);
        
        tempFrame = tableView.frame;
        tableView.frame = CGRectMake(tempFrame.minX, tempFrame.minY, tempFrame.width, 0);
        tableView.reloadData();
        steering.delegate = self;
    }

    override func didReceiveMemoryWarning()
    {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBAction func onSearch(sender: AnyObject)
    {
        /*println(PSNetUtil.localIPAddress());
        println(PSNetUtil.broadcastAddress());
        println(PSNetUtil.netmask());*/
        steering.broadcast(-1);
        tableView.reloadData();
    }
    
    //*********TableViewDataSource
    func tableView(tableView: UITableView!, numberOfRowsInSection section: Int) -> Int
    {
        return steering.hostsPending.count;
    }
    
    // Row display. Implementers should *always* try to reuse cells by setting each cell's reuseIdentifier and querying for available reusable cells with dequeueReusableCellWithIdentifier:
    // Cell gets various attributes set automatically based on table (separators) and data source (accessory views, editing controls)
    
    func tableView(tableView: UITableView!, cellForRowAtIndexPath indexPath: NSIndexPath!) -> UITableViewCell
    {
        var retVal : UITableViewCell = UITableViewCell();
        retVal.textLabel.text = "\(steering.hostsPending[indexPath.row].ip):\(steering.hostsPending[indexPath.row].port)";
        return retVal;
    }
    
    //********TableViewDelegate
    func tableView(tableView: UITableView!, didSelectRowAtIndexPath indexPath: NSIndexPath!)
    {
        steering.sendRequestForSession(steering.hostsPending[indexPath.row]);
    }
    
    //********SteeringDelegate
    func onSteering(newElementAdded element: PSHostListElement)
    {
        self.tableView.reloadData();
        UIView.animateWithDuration(0.5, animations: { () -> Void in self.tableView.frame = self.tempFrame; });
    }
    
    func onSteering(sessionApproved session: PSSession, withHost host: PSHostListElement)
    {
        println("Accepted!");
        var sb = UIStoryboard(name: "Main", bundle: NSBundle.mainBundle());
        var nextView : PSSessionViewController = sb.instantiateViewControllerWithIdentifier("PSSession") as PSSessionViewController;
        nextView.session = steering.currentSession;
        self.presentViewController(nextView, animated: true, completion: nil);
    }
    func onSteering(sessionDeniedByHost host: PSHostListElement)
    {
        println("Denied!");
    }
}