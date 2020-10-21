package main

import (
	"Mao_Service_Framework/AAA"
	"Mao_Service_Framework/Node"
	"Mao_Service_Framework/Node/lib"
	"Mao_Service_Framework/gRPC"
	"log"
)

func main() {
	log.Printf("%v, %v\n", lib.NODE_UP, lib.NODE_DOWN)

	aaaManager := AAA.NewAaaManager()
	deviceManager := Node.NewNodeManager()

	apiServer := gRPC.ApiServer{}
	apiServer.LinkAaaManager(aaaManager)
	apiServer.LinkNodeManager(deviceManager)

	apiServer.StartServer()

	return

	devices := deviceManager.GetAllNodes()
	log.Printf("%v", devices)

	deviceManager.RegisterNode(*lib.NewNodeId("beijing-radar-usb"))
	deviceManager.RegisterNode(*lib.NewNodeId("shanghai-radar-i2c"))
	d := deviceManager.GetNode(*lib.NewNodeId("shanghai-radar-i2c"))
	if d != nil {
		d.GoUp()
	}
	devices = deviceManager.GetAllNodes()
	log.Printf("%v", devices)
	//log.Printf("%v, %v, %v\n", deviceManager.State, deviceManager.Devices, len(deviceManager.Devices))

}

